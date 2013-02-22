package org.ng12306.tpms;

import java.util.*;

import junit.framework.Assert;

import org.diting.collections.*;

import org.joda.time.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ng12306.tpms.runtime.*;

public class TestTicketPoolTest {

	
	 @BeforeClass
	 public static void initServices() throws Exception
	 {
		 ServiceManager.getServices().initializeServices(
					new Object[] { new TestRailwayRepository()});
	 }
	
	/**
	 * Create G101 ticket pool for "today".
	 *
	 */
	private TestTicketPool createTicketPool() throws Exception {

		
		
		IRailwayRepository repo = ServiceManager.getServices()
				.getRequiredService(IRailwayRepository.class);

		TrainNumber tn = Queries.query(repo.getTrainNumbers()).first(
				new Predicate<TrainNumber>() {

					@Override
					public boolean evaluate(TrainNumber obj) throws Exception {
						return obj.name.equals("G101");
					}
				});

		LocalDate today = LocalDate.now();

		org.ng12306.tpms.runtime.Train train = new org.ng12306.tpms.runtime.Train();
		train.id = UUID.randomUUID();
		train.departureDate = today;
		train.trainNumber = tn;

		TestTicketPool pool = new TestTicketPool(train);
		pool.setSite(ServiceManager.getServices());
		pool.initialize();

		return pool;

	}

	/**
	 * Test book 1 ticket from Beijingnan station to Nanjingnan station with any seat.
	 * @throws Exception
	 */
	@Test
	public void testBook() throws Exception {
		//Create query arguments. TicketQueryArgs is used for external client with human readable parameters.
		TicketQueryEvent query = new TicketQueryEvent();
		query.date = LocalDate.now();
		query.departureStation = "北京南";
		query.destinationStation = "南京南";
		query.trainNumber = "G101";
		query.seatType = -1;
		query.count = 1;

		
		
		TestTicketPool pool = this.createTicketPool();
		
		//Because book is the only operation which may change data, so in the future, toTicketPoolQueryArgs, book and toTicket will
		//be executed by different disruptor consumers.  

		
		//Convert query to pool query. pool query is only used inside pool system and holds seat, train as ids or java references.
				
		TicketPoolQueryArgs poolQuery = pool.toTicketPoolQueryArgs(query);
		
		TicketPoolTicket[] poolTickets = pool.book(poolQuery);
		
		
		//Convert pool ticket to human readable ticket.
		Ticket[] tickets = pool.toTicket(poolTickets);
		
		Assert.assertEquals(1, tickets.length);
		
		Ticket ticket = tickets[0];
		
		Assert.assertEquals(LocalDate.now(), ticket.departureDate);
		
		Assert.assertEquals("北京南", ticket.departureStation);
		Assert.assertEquals("南京南", ticket.destinationStation);
		Assert.assertEquals("G101", ticket.trainNumber);
		

	}
	
	
	

	/**
	 * This test randomly generates booking request until all tickets are sold out. 
	 * Finally, it checks every ticket of every seat has been sold and  no range overlapping between tickets on same seat.
	 * @throws Exception
	 */
	@Test
	public void testSaleAll() throws Exception
	{


		TestTicketPool pool = this.createTicketPool();
		
	
		int stopCount = pool.route.stops.size();
		
		TicketPoolQueryArgs query = new TicketPoolQueryArgs();
		
		query.count = 1;
		query.seatType = ~0;
	
		
		ArrayList<TicketPoolTicket> soldTickets = new ArrayList<TicketPoolTicket>(10000);
		
		Random random = new Random(new Date().getTime());
		
		while(!pool.getIsSoldOut())
		{
			int v1 = random.nextInt(stopCount);
			int v2 = random.nextInt(stopCount);
			
			if(v1 != v2)
			{
				int departure = Math.min(v1, v2);
				int destination = Math.max(v1, v2);
				query.departureStop = departure;
				query.destinationStop = destination;
				
				TicketPoolTicket[] tickets = pool.book(query);
				for(TicketPoolTicket t : tickets)
				{
					soldTickets.add(t);
				}
			}
		}
		
		Iterable<IGrouping<OperatingSeat, TicketPoolTicket>> groups = Queries.query(soldTickets).groupBy(new Selector<TicketPoolTicket, OperatingSeat>(){

			@Override
			public OperatingSeat select(TicketPoolTicket item) {
				return item.seat;
			}});
		
		for(IGrouping<OperatingSeat, TicketPoolTicket> g : groups)
		{
			
			
			Assert.assertTrue(g.toQuery().count()> 0);
			
			int stop = 0;
			for(TicketPoolTicket t : g.toQuery().orderBy(new Selector<TicketPoolTicket, Integer>(){

				@Override
				public Integer select(TicketPoolTicket item) {
					return item.departureStop;
				}}))
			{
				
				
				Assert.assertEquals(stop, t.departureStop);
				stop = t.destinationStop;
			}
			
			Assert.assertEquals(stopCount - 1, stop);
			
			
		}
		
		
		
		
	}
	

}
