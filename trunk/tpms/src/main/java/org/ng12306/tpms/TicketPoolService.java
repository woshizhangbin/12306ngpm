package org.ng12306.tpms;

import com.lmax.disruptor.*;

public class TicketPoolService {
    public final static EventFactory<TicketQueryEvent> QueryFactory =
	new EventFactory<TicketQueryEvent>() {
	public TicketQueryEvent newInstance() {
	    return new TicketQueryEvent();
	}
    };
}
