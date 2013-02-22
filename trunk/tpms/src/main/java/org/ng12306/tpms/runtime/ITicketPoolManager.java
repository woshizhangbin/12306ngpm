package org.ng12306.tpms.runtime;

import org.ng12306.tpms.TicketQueryEvent;

public interface ITicketPoolManager {
	
	
	ITicketPool getPool(TicketQueryEvent args);
	
	void addPool(ITicketPool pool) throws Exception;
	
	void removePool(ITicketPool pool) throws Exception;
	
	ITicketPool[] getAlivePools();

}
