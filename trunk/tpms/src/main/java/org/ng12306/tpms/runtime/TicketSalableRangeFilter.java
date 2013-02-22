package org.ng12306.tpms.runtime;

import org.diting.collections.*;
import org.ng12306.tpms.ObjectWithSite;

public class TicketSalableRangeFilter extends ObjectWithSite
    implements IPlanTicketFilter 
{

	@Override
	public Iterable<PlanTicket> filter(Iterable<PlanTicket> source,
			final TicketPoolQueryArgs args) throws Exception {
		return Queries.query(source).where(new Predicate<PlanTicket>(){

			@Override
			public boolean evaluate(PlanTicket ticket) throws Exception {
				SalableRange range =  ticket.salableRange;
				
				return range.departureStart <= args.destinationStop && args.departureStop <= range.departureEnd
						&& range.destinationStart <= ticket.endStop && ticket.endStop <= range.destinationEnd;
				
			}});
	}
	
	
	

}
