package org.ng12306.tpms;

import org.ng12306.tpms.runtime.MissingRequiredServiceException;



@SuppressWarnings({ "rawtypes" })
public class ServiceContainer implements IServiceProvider {
	private IServiceProvider _parentProvider;

	public ServiceContainer() {

	}

	private boolean _initialized = false;
	private java.util.ArrayList<Object> _list = new java.util.ArrayList<Object>();

	public final void initializeServices(Object[] services) throws Exception {
		this.initializeServices(null, services);
	}

	public void initializeServices(IServiceProvider parentProvider,
			Object[] services) throws Exception {
		this._parentProvider = parentProvider;

		for (Object s1 : services) {
			this._list.add(s1);
		}

		

		for (Object o : this._list) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			if (o instanceof IObjectWithSite) {
				((IObjectWithSite) o).setSite(this);
			}
			if (o instanceof IService) {

			
				((IService) o).initializeService();
				
			}
		}
		this._initialized = true;
	}

	public void AddService(Object service) throws Exception {
		if (service == null) {
			throw new IllegalArgumentException("service");
		}

		this._list.add(service);
		if (service instanceof IObjectWithSite) {
			((IObjectWithSite) service).setSite(this);
		}
		if (service instanceof IService && this._initialized) {
			((IService) service).initializeService();
		}

	}

	public void removeService(Object service) throws Exception {
		int index = this._list.indexOf(service);
		if (index >= 0) {
			this._list.remove(index);
			if (service instanceof IService) {
				((IService) service).uninitializeService();
			}
		}

	}

	public void uninitializeServices() throws Exception {
		
		for (int i = this._list.size() - 1; i >= 0; i--) {
			Object o = this._list.get(i);
			if (o instanceof IService) {
				
				((IService) o).uninitializeService();

			}
		}
	    this._list.clear();
	}

	public Object getService2(java.lang.Class serviceType) throws Exception {
		if (serviceType == null) {
			return new IllegalArgumentException("serviceType");
		}
		for (Object o : this._list) {
			if (serviceType.isInstance(o)) {
				return o;
			}
		}

		if (this._parentProvider != null) {
			return this._parentProvider.getService2(serviceType);
		}

		return null;
	}

	@Override
	public Object getRequiredService2(Class serviceType) throws Exception {
		Object rs = this.getService2(serviceType);
		if (rs == null) {
			throw new MissingRequiredServiceException(serviceType);
		}
		return rs;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(Class<T> serviceType) throws Exception {
		return (T) this.getService2(serviceType);

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getRequiredService(Class<T> serviceType) throws Exception {
		return (T) this.getRequiredService2(serviceType);
	}

}