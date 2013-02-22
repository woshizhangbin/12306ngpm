package org.ng12306.tpms;

public class ServiceManager extends ServiceContainer
{
	private ServiceManager()
	{

	}

	private static ServiceManager _services = new ServiceManager();

	public static ServiceManager getServices()
	{
		return _services;
	}


}