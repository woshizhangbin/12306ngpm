package org.ng12306.tpms.runtime;

import java.util.*;

import org.ng12306.tpms.ObjectWithSite;

public class TicketCountFilter extends ObjectWithSite
    implements IPlanTicketFilter
{

	@Override
	public Iterable<PlanTicket> filter(Iterable<PlanTicket> source,
			TicketPoolQueryArgs args) {
		ArrayList<PlanTicket> rs = new ArrayList<PlanTicket>(args.count);
		Iterator<PlanTicket> iter = source.iterator();
		for(int i = 0; i < args.count && iter.hasNext(); i ++)
		{
		    rs.add(iter.next()); 	
		}
		
		return rs.size() == args.count ? rs : _empty;
		
		
	}
	
	private static ArrayList<PlanTicket> _empty = new ArrayList<PlanTicket>(0);

    
}
