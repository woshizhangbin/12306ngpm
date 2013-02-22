package org.ng12306.tpms;

import java.util.concurrent.ExecutorService;

public interface IExecutorServiceProvider {

	public ExecutorService getExecutor();
}
