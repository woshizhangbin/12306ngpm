package org.ng12306.tpms;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.*;
import org.ng12306.tpms.runtime.TicketPoolQueryArgs;
import org.ng12306.tpms.runtime.ITicketPoolManager;
import org.ng12306.tpms.runtime.ITicketPool;
import org.ng12306.tpms.runtime.TicketPoolTicket;

public class EventBus extends ServiceBase{
     private ITicketQueryEventJournal _journal;
     private ITicketQueryEventReplicator _replicator;
     private ITicketQueryEventProducer _queryProducer;
     private ITicketQueryResultConsumer _resultConsumer;
     
     private RingBuffer<TicketQueryEvent> _ringBuffer;
     private Disruptor<TicketQueryEvent> _disruptor;
     private ITicketPoolManager _poolManger;
     
     @Override
     public void initializeService() throws Exception
     {
    	 this._journal = this.getSite().getService(ITicketQueryEventJournal.class);
    	 this._replicator = this.getSite().getService(ITicketQueryEventReplicator.class);
    	 this._poolManger = this.getSite().getRequiredService(ITicketPoolManager.class);
    	 this._queryProducer = this.getSite().getRequiredService(ITicketQueryEventProducer.class);
    	 this._resultConsumer = this.getSite().getRequiredService(ITicketQueryResultConsumer.class);
    	 this.startDisruptor();
    	 this._queryProducer.addListener(this._producerListener);
    	 super.initializeService();
     }
     
     private ITicketQueryEventProducerListener _producerListener = new ITicketQueryEventProducerListener(){

		@Override
		public void query(TicketQueryEvent event) {
			
			publishQueryEvent(event);
		}};
     
     
     @Override
     public void uninitializeService() throws Exception
     {
    	 this._queryProducer.removeListener(this._producerListener);
    	 _disruptor.shutdown();
    	 super.uninitializeService();
     }
     
     // 日志线程
     private EventHandler<TicketQueryEvent> _journalHandler = 
	  new EventHandler<TicketQueryEvent>() {
	  public void onEvent(final TicketQueryEvent event, 
			      final long sequence,
			      final boolean endOfBatch) throws Exception {
	       // TODO: 需要确保程序崩溃的时候，所有的数据都在硬盘上
	       // 因为在硬盘上有一个缓冲区，需要确保即使程序崩溃也能把缓冲里的数据
	       // 写到硬盘上，不过貌似现代操作系统能够做到在程序崩溃时flush缓存，这点
	       // 需要测试验证。

	       // brucesea的反馈: 查询不改变状态，Journalist和Replicator感觉就用不着了，
	       // 对改变状态的操作Journalist和Replicator一下
	       // 因此需要根据TicketQueryArgs的类型来决定是否做日志和备份
	        if(event.action == TicketQueryAction.Book)
	        {
	        	_journal.Write(event);
	        }
	  }
     };
     
     // 将事件发送到备份服务器保存的备份线程
     private EventHandler<TicketQueryEvent> _replicateHandler = new EventHandler<TicketQueryEvent>() {
	  public void onEvent(final TicketQueryEvent event, final long sequence,
			      final boolean endOfBatch) throws Exception {
		  if(event.action == TicketQueryAction.Book)
	        {
			  _replicator.Write(event);
	        }
	  }
     };


    private EventHandler<TicketQueryEvent> _eventProcessor = 
	new EventHandler<TicketQueryEvent>() {
	public void onEvent(final TicketQueryEvent event,
			    final long sequence,
			    final boolean endOfBatch) throws Exception {
	     // 根据车次号查询车次详细信息	       
	     ITicketPool pool = _poolManger.getPool(event);
	     TicketQueryResult result = new TicketQueryResult();
         result.sequence = event.sequence;
	     if (pool != null) {
		  TicketPoolQueryArgs poolArgs = pool
		       .toTicketPoolQueryArgs(event);
		  if (event.action == TicketQueryAction.Query) {
		      result.hasTicket = pool.hasTickets(poolArgs);
		  }
		  else
		  {
			  TicketPoolTicket[] poolTickets = pool.book(poolArgs);
			  result.tickets = pool.toTicket(poolTickets);
			  result.hasTicket = result.tickets.length > 0;  
		  }
	     }
	     
	     _resultConsumer.sendResult(result);
	  }
    };
    
    // 默认的请求消息和响应消息队列的大小是2的13次方
    private static int RING_SIZE = 2 << 13;
  
    // 向消息队列发布一个查询请求事件
    // TODO: 将publicXXXEvent改成异步的，应该返回void类型，异步返回查询结果。
    private  void publishQueryEvent(TicketQueryEvent args) {
	long sequence = _ringBuffer.next();
	TicketQueryEvent event = _ringBuffer.get(sequence);
	args.copyTo(event);
	// event.sequence = sequence;
	event.sequence = sequence;

	// 将消息放到车轮队列里，以便处理
	_ringBuffer.publish(sequence);
    }

	@SuppressWarnings("unchecked")
	private  void startDisruptor() throws Exception {       
	// 创建处理查询消息的disruptor
    	IExecutorServiceProvider esp = this.getSite().getRequiredService(IExecutorServiceProvider.class);
    	
	_disruptor = 
	    new Disruptor<TicketQueryEvent>
	    (
	     TicketPoolService.QueryFactory,
	     esp.getExecutor(),
	     new SingleThreadedClaimStrategy(RING_SIZE),
	     new BlockingWaitStrategy()
	     );

	// @brucesea
	// 另外用Disruptor，推荐用DisruptorWizard，
	// 这样EventProcessor之间的关系会比较清晰
	
	
	EventHandlerGroup<TicketQueryEvent> eventGroup = null;
	
	// 注册日志线程
	if(this._journal != null)
	{
		eventGroup = _disruptor.handleEventsWith(this._journalHandler);
	}
	
	//注册备份线程
	if(this._replicator != null)
	{
		if(eventGroup != null)
		{
			eventGroup = eventGroup.and(this._replicateHandler);
		}
		else
		{
			eventGroup = _disruptor.handleEventsWith(this._replicateHandler);
		}
	}
	
	// 事件处理线程只能在日志和备份线程之后处理它
	if(eventGroup != null)
	{
		eventGroup.then(this._eventProcessor);
	}
	else
	{
		_disruptor.handleEventsWith(this._eventProcessor);
	}

	// 启动disruptor,等待publish事件
	_ringBuffer = _disruptor.start();
    }
}
