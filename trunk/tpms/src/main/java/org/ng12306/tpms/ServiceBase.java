package org.ng12306.tpms;

import java.util.*;


public abstract class ServiceBase implements IService, IObjectWithSite
{	
	public void initializeService() throws Exception
	{
		EventObject e = new EventObject(this);
		for(IServiceListener listener : this._listeners)
		{
			listener.serviceInitialized(e);
		}
	}

	public void uninitializeService() throws Exception
	{
		EventObject e = new EventObject(this);
		for(IServiceListener listener : this._listeners)
		{
			listener.serviceUninitialized(e);
		}
	}
	
	
	private HashSet<IServiceListener> _listeners = new  HashSet<IServiceListener>();
	

	public void addServiceListener(IServiceListener listener)
	{
		this._listeners.add(listener);
	}
	
	public void removeServiceListener(IServiceListener listener)
	{
		this._listeners.remove(listener);
	}

	private IServiceProvider privateSite;
	public final IServiceProvider getSite()
	{
		return privateSite;
	}
	public final void setSite(IServiceProvider value)
	{
		privateSite = value;
	}

}
