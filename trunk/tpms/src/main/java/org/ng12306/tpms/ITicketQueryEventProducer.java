package org.ng12306.tpms;


/**
 * Interface for TicketQueryEvent generating 
 * @author Bin Zhang
 *
 */
public interface ITicketQueryEventProducer {

	public void addListener(ITicketQueryEventProducerListener listener);
	
	public void removeListener(ITicketQueryEventProducerListener listener);
	
}
