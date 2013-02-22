package org.ng12306.tpms;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import org.jboss.netty.bootstrap.*;
import org.jboss.netty.channel.*;

import org.jboss.netty.channel.group.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.serialization.*;

public class NettyTicketQueryService extends ServiceBase implements
		ITicketQueryEventProducer, ITicketQueryResultConsumer {

	private static int RING_SIZE = 2 << 13;
	private Channel[] _channelBuffer = new Channel[RING_SIZE];
	private long _sequence = 0;

	private int _port;
	private ChannelGroup _channels;
	private ChannelFactory _factory;

	private ArrayList<ITicketQueryEventProducerListener> _listeners = new ArrayList<ITicketQueryEventProducerListener>();

	@Override
	public void addListener(ITicketQueryEventProducerListener listener) {
		this._listeners.add(listener);

	}

	@Override
	public void removeListener(ITicketQueryEventProducerListener listener) {
		this._listeners.remove(listener);

	}

	@Override
	public void initializeService() throws Exception {
		// Get port number from configuration file or command arguments or
		// somewhere.
		this._port = 12306;
		_channels = new DefaultChannelGroup("ticket-pool");
		IExecutorServiceProvider ep = this.getSite().getRequiredService(
				IExecutorServiceProvider.class);
		_factory = new NioServerSocketChannelFactory(
		// TODO: 需要写性能测试用例已验证cached thread pool是否够用？
				ep.getExecutor(), ep.getExecutor());
		ServerBootstrap bootstrap = new ServerBootstrap(_factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() throws Exception {
				// 这个就是发送消息包的Handler栈 - 虽然名字叫管道！
				return Channels.pipeline(
						new ObjectEncoder(),
						new ObjectDecoder(ClassResolvers
								.cacheDisabled(getClass().getClassLoader())),
						new QueryTrainServerHandler());
			}
		});
		_channels.add(bootstrap.bind(new InetSocketAddress(_port)));

		super.initializeService();
	}

	@Override
	public void uninitializeService() throws Exception {
		super.uninitializeService();
		ChannelGroupFuture future = _channels.close();
		future.awaitUninterruptibly();
		_factory.releaseExternalResources();

	}

	class QueryTrainServerHandler extends SimpleChannelUpstreamHandler {
		@Override
		public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
			// 票池服务器采用异步网络io的方式接受消息
			// 因为我们的Handler是从SimpleChannelUpstreamHandler继承下来的
			// Netty会帮我们将多个零散的数据包整合一个完整的原始的客户端请求数据包
			// 另外，由于在其之前我们已经放置了序利化方面的Handler了，所以可以
			// 直接通过e.getMessage()获取客户端发送的对象。
			TicketQueryEvent event = (TicketQueryEvent) e.getMessage();
			event.sequence = _sequence;
			_sequence++;
			Channel channel = e.getChannel();

			int bufferIndex = (int) (event.sequence % RING_SIZE);

			if (_channelBuffer[bufferIndex] == null) {
				_channelBuffer[bufferIndex] = channel;
				for (ITicketQueryEventProducerListener listener : _listeners) {
					listener.query(event);
				}
			} else {
				// TODO: throw error when buffer overflow or send result
				// immediately.
			}

		}
	}

	@Override
	public void sendResult(TicketQueryResult result) {
		int bufferIndex = (int) (result.sequence % RING_SIZE);
		Channel channel = _channelBuffer[bufferIndex];
		_channelBuffer[bufferIndex] = null;
		ChannelFuture future = channel.write(result);
		future.addListener(ChannelFutureListener.CLOSE);

	}

}
