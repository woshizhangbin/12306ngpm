package org.ng12306.tpms;

import java.io.Serializable;

import org.ng12306.tpms.runtime.Ticket;

public class TicketQueryResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4496121679957168355L;
    public boolean hasTicket = false;
	
	public long sequence;

	public Ticket[] tickets;
	
}
