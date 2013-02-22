package org.ng12306.tpms;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPoolProvider
    implements IExecutorServiceProvider
{

	
	private ExecutorService _executor = Executors.newCachedThreadPool();
	
	@Override
	public ExecutorService getExecutor() {
		return _executor;
	}

}
