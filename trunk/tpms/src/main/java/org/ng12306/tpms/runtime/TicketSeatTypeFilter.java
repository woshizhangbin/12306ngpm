package org.ng12306.tpms.runtime;

import org.diting.collections.*;
import org.ng12306.tpms.ObjectWithSite;

public class TicketSeatTypeFilter extends ObjectWithSite
   implements IPlanTicketFilter
{

	@Override
	public Iterable<PlanTicket> filter(Iterable<PlanTicket> source,
			final TicketPoolQueryArgs args) throws Exception {
		return Queries.query(source).where(new Predicate<PlanTicket>(){

			@Override
			public boolean evaluate(PlanTicket ticket) throws Exception {
				
				long t1 = ticket.seat.seatType;
				long t2 = args.seatType;
				
				return (t1 & t2) != 0;
				
			}});
	}

}
