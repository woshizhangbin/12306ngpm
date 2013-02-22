package org.ng12306.tpms.runtime;

import java.util.*;

import org.diting.collections.*;
import org.ng12306.tpms.IObjectWithSite;
import org.ng12306.tpms.ObjectWithSite;
import org.ng12306.tpms.ServiceContainer;
import org.ng12306.tpms.TicketQueryEvent;


public class TestTicketPool extends ObjectWithSite 
    implements ITicketPool
{

	public TestTicketPool(Train train) {
		this.train = train;
	}
	
	public Train train;
	
	public Route route;

	private ArrayList<StopRangeGroup> stopRangeGroups = new ArrayList<StopRangeGroup>();

	private void createPlanTickets() throws Exception
	{
		
		//Here we are creating plan tickets those can be free sold between any two stations. 
		int maxStopSeq = this.route.stops.size() - 1;
		
		StopRangeGroup fullRangeGroup = new StopRangeGroup();
		StopRange stopRange = new StopRange();
		stopRange.start = 0;
		stopRange.end = maxStopSeq;
		 
		fullRangeGroup.range = stopRange;
		this.stopRangeGroups.add(fullRangeGroup);
		
		for(OperatingSeat seat : Queries.query(this.train.cars).selectMany(new Selector<Car, Iterable<OperatingSeat>>(){

			@Override
			public Iterable<OperatingSeat> select(Car item) {
				return item.seats;
			}}))
		{
		     PlanTicket pt = new PlanTicket();
		     pt.startStop = 0;
		     pt.endStop = maxStopSeq;
		     pt.id = UUID.randomUUID();
		     pt.originalId = pt.id;
		     pt.seat = seat;
		     
		     SalableRange range = pt.salableRange;
		     range.departureStart = 0;
		     range.departureEnd = maxStopSeq;
		     range.destinationStart = 0;
		     range.destinationEnd = maxStopSeq;
		     
		     fullRangeGroup.tickets.add(pt);
		     pt.group = fullRangeGroup;
		    
		}
	}
	

	private void buildTrain() throws Exception
	{
        IRailwayRepository repo = this.getSite().getRequiredService(IRailwayRepository.class);
		
		
		this.route = Queries.query(this.train.trainNumber.routes).first(new Predicate<Route>(){

			@Override
			public boolean evaluate(Route r) throws Exception {
				return  (r.startDate.isBefore(train.departureDate) || r.startDate.isEqual(train.departureDate))
						&& r.endDate.isAfter(train.departureDate);
			}});
		
		
		int seatSeq = 0;
		for(int i = 0; i < 18; i ++)
		{
			Car car = new Car();
			car.id = UUID.randomUUID();
			car.train = this.train;
			CarType ct = repo.getCarTypes()[ i % repo.getCarTypes().length];
			car.carType = ct;
			train.cars.add(car);
			
			for(Seat seat : ct.seats)
			{
			     OperatingSeat oseat = new OperatingSeat();
			     oseat.id = UUID.randomUUID();
			     oseat.car = car;
			     oseat.seatNumber = seat.number;
			     oseat.sequence = seatSeq;
			     oseat.seatType = ct.seatType;
			     seatSeq++;
			     car.seats.add(oseat);
			}
		}
	}
	
	@Override
	public void initialize() throws Exception
	{
		
		this.buildTrain();
		this.createPlanTickets();

	}
	
	private int getStationSequence(final String name) throws Exception
	{
		return Queries.query(this.route.stops).first(new Predicate<RouteStop>(){

			@Override
			public boolean evaluate(RouteStop stop) throws Exception {
				return stop.station.name.equals(name);
			}}).sequence;
	}
	

	@Override
	public boolean hasTickets(TicketPoolQueryArgs args) throws Exception{
		
		Iterable<PlanTicket> planTickets = this.query(args);
		return Queries.query(planTickets).any();
		
		
	}

	@Override
	public TicketPoolTicket[] book(final TicketPoolQueryArgs args) throws Exception {
		
		
		PlanTicket[] planTickets = Queries.query(this.query(args)).toArray(new PlanTicket[0]);
		
		
		
		for(PlanTicket pt : planTickets)
		{
			pt.group.tickets.remove(pt);
			if(pt.group.tickets.size() == 0)
			{
			    this.stopRangeGroups.remove(pt.group);	
			}
			
			if(args.departureStop > pt.startStop)
			{
				PlanTicket pre = new PlanTicket();
				pre.id = UUID.randomUUID();
				pre.originalId = pt.originalId;
				pre.startStop = pt.startStop;
				pre.endStop = args.departureStop;
				pre.seat = pt.seat;
				pt.salableRange.copyTo(pre.salableRange);
				
				this.addPlanTicketToGroup(pre);
				
			}
			
			
			if(args.destinationStop < pt.endStop)
			{
				PlanTicket after = new PlanTicket();
				after.id = UUID.randomUUID();
				after.originalId = pt.originalId;
				after.startStop = args.destinationStop;
				after.endStop = pt.endStop;
				after.seat = pt.seat;
				pt.salableRange.copyTo(after.salableRange);
				this.addPlanTicketToGroup(after);
			}
			
			
		}
		
		
		if(this.stopRangeGroups.size() == 0)
		{
			this._isSoldOut = true;
		}
		
		
		TicketPoolTicket[] rs = new TicketPoolTicket[planTickets.length];
		
		for(int i = 0; i < planTickets.length; i++)
		{
			PlanTicket pt = planTickets[i];
			rs[i] = new TicketPoolTicket();
			rs[i].seat = pt.seat;
			rs[i].departureStop = args.departureStop;
			rs[i].destinationStop = args.destinationStop;
			rs[i].pool = this;
		}

		return rs;
	}
	
	private void addPlanTicketToGroup(PlanTicket pt)
	{
		StopRange range = new StopRange();
		range.start = pt.startStop;
		range.end = pt.endStop;
		
		int pos = CollectionUtils.binarySearchBy(this.stopRangeGroups, range, new Selector<StopRangeGroup, StopRange>(){

			@Override
			public StopRange select(StopRangeGroup item) {
				return item.range;
			}}, StopRangeComparator.Default);
		
		
		StopRangeGroup group;
		
		if(pos >= 0)
		{
			group = this.stopRangeGroups.get(pos);
		}
		else
		{
			group = new StopRangeGroup();
			group.range = range;
			this.stopRangeGroups.add(~pos, group);
		}
		
		pt.group = group;
		group.tickets.add(pt);
	}
	
	
	private Iterable<PlanTicket> query(final TicketPoolQueryArgs args) throws Exception
	{
	
		IPlanTicketFilter[] filters = this.createFilters();
		
		Iterable<PlanTicket> rs = Queries.query(this.stopRangeGroups)
				.where(new Predicate<StopRangeGroup>(){

					@Override
					public boolean evaluate(StopRangeGroup group)
							throws Exception {
						return group.range.start <= args.departureStop && args.destinationStop <= group.range.end;
					}})
				.selectMany(new Selector<StopRangeGroup, Iterable<PlanTicket>>(){

					@Override
					public Iterable<PlanTicket> select(StopRangeGroup item) {
						return item.tickets;
					}});
		
		
		for(IPlanTicketFilter filter : filters)
		{
			rs = filter.filter(rs, args);
		}
		
		return rs;
		
	}
	
	
	private IPlanTicketFilter[] createFilters() throws Exception
	{
		
		ServiceContainer site = new ServiceContainer();
		site.initializeServices(this.getSite(), new Object[]{this});
		
		
		IPlanTicketFilter[] rs = new IPlanTicketFilter[_filterTypes.length];
		
		for(int i = 0; i < _filterTypes.length; i ++)
		{
		    rs[i] = (IPlanTicketFilter)_filterTypes[i].newInstance();
		    if(rs[i] instanceof IObjectWithSite)
		    {
		    	((IObjectWithSite)rs[i]).setSite(site);
		    }
		}
		
		return rs;
	}
	
	
	@SuppressWarnings("rawtypes")
	private static Class[] _filterTypes = new Class[] {TicketSalableRangeFilter.class, TicketSeatTypeFilter.class, TicketCountFilter.class};

	@Override
	public TicketPoolQueryArgs toTicketPoolQueryArgs(TicketQueryEvent args)
			throws Exception {
		TicketPoolQueryArgs rs = new TicketPoolQueryArgs();
		rs.departureStop = this.getStationSequence(args.departureStation);
		rs.destinationStop = this.getStationSequence(args.destinationStation);
		
		if(rs.destinationStop <= rs.departureStop)
		{
			throw new IllegalArgumentException("Destination station must be after the departure station.");
		}
		
		rs.count = args.count;
		rs.seatType = args.seatType;
		
		
		
		
		return rs;
	}


	@Override
	public Ticket[] toTicket(TicketPoolTicket[] poolTickets) throws Exception {
		
		
		IRailwayRepository repo = this.getSite().getRequiredService(IRailwayRepository.class);
		
		Ticket[] rs = new Ticket[poolTickets.length];
		
		for(int i = 0; i < poolTickets.length; i ++)
		{
			final TicketPoolTicket pt = poolTickets[i];
		    Ticket ticket = new Ticket();
		    ticket.id = UUID.randomUUID();
		    ticket.trainNumber = this.train.trainNumber.name;
		    ticket.seatNumber = pt.seat.seatNumber;
		    ticket.car = Integer.toString(pt.seat.car.carNumber);
		    String seatTypeName = Queries.query(repo.getSeatTypes())
		        .where(new Predicate<SeatType>(){

				    @Override
				    public boolean evaluate(SeatType seatType) throws Exception {
					    return seatType.code == pt.seat.seatType;
				    }})
				.select(new Selector<SeatType, String>(){

					@Override
					public String select(SeatType seatType) {
						return seatType.name;
					}})
				.first();
		    ticket.seatType = seatTypeName;
		    
		    ticket.departureStation = this.route.stops.get(pt.departureStop).station.name;
		    ticket.destinationStation = this.route.stops.get(pt.destinationStop).station.name;
		    ticket.departureDate = this.train.departureDate;
		    
		    rs[i] = ticket;
		   
		}
		
		
		return rs;
	}


	
	private Boolean _isSoldOut = false;
	
	@Override
	public Boolean getIsSoldOut() throws Exception {
		return this._isSoldOut;
	}
	
}
