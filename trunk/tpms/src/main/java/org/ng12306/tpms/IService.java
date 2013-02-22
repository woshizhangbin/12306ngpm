package org.ng12306.tpms;

public interface IService
{
	void initializeService() throws Exception;
	void uninitializeService() throws Exception;;
	
	void addServiceListener(IServiceListener listener) throws Exception;;
	void removeServiceListener(IServiceListener listener) throws Exception;;

}