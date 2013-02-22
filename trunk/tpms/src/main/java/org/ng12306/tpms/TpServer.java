package org.ng12306.tpms;

import org.ng12306.tpms.runtime.*;

// ITpServer的默认实现，如果要做A/B测试的话，应该是
// 从TpServer继承实现两种方式
public class TpServer {

	private static boolean _started = false;

	public static void start() throws Exception {
		if (!_started)
			_started = true;
		ServiceManager.getServices().initializeServices(
				new Object[] { new CachedThreadPoolProvider(),
						new TestRailwayRepository(),
						new TestTicketPoolManager(), new EventBus(),
						new NettyTicketQueryService() });
	}

	public static void stop() throws Exception {
		if (_started) {
			_started = false;
			ServiceManager.getServices().uninitializeServices();

		}
	}
}
