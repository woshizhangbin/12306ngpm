package org.ng12306.tpms;


public class ObjectWithSite implements IObjectWithSite
{
	private IServiceProvider _site;
	public final IServiceProvider getSite()
	{
		return _site;
	}
	public final void setSite(IServiceProvider value)
	{
		_site = value;
	}
}