package org.ng12306.tpms;

/**
 * 
 * @author Bin Zhang
 *
 */
public interface ITicketQueryEventJournal {
    public void Write(TicketQueryEvent event);
}
