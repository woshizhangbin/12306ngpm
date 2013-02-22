package org.ng12306.tpms.runtime;

import org.ng12306.tpms.TicketQueryEvent;

public interface ITicketPool {

	boolean hasTickets(TicketPoolQueryArgs args) throws Exception;
	
	TicketPoolTicket[] book(TicketPoolQueryArgs args) throws Exception; 
	
	TicketPoolQueryArgs toTicketPoolQueryArgs(TicketQueryEvent args) throws Exception;
	
	Ticket[] toTicket(TicketPoolTicket[] poolTickets) throws Exception;
	
	Boolean getIsSoldOut() throws Exception;

	void initialize() throws Exception;
}


