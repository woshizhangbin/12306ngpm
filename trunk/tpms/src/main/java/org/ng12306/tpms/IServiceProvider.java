package org.ng12306.tpms;

@SuppressWarnings("rawtypes")
public interface IServiceProvider {
    public Object getService2( Class serviceType) throws Exception;
    
    public <T> T getService(Class<T> serviceType) throws Exception;
   
    public Object getRequiredService2(Class serviceType) throws Exception;
    
    public <T> T getRequiredService(Class<T> serviceType) throws Exception;
  
}
