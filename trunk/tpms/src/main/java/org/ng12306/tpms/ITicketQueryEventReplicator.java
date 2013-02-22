package org.ng12306.tpms;

public interface ITicketQueryEventReplicator {
   public void Write(TicketQueryEvent event);
}
