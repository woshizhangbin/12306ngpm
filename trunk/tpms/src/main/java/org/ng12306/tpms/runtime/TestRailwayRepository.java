package org.ng12306.tpms.runtime;

import java.util.Arrays;
import java.util.UUID;

import org.diting.collections.Queries;
import org.diting.collections.Selector;
import org.joda.time.LocalDate;
import org.ng12306.tpms.ServiceBase;

public class TestRailwayRepository extends ServiceBase 
    implements IRailwayRepository {

	
	private Station[] _stations;
	private SeatType[] _seatTypes;
	private CarType[] _carTypes;
	private TrainNumber[] _trainNumbers;
	
	@Override
	public Station[] getStations() {
		return this._stations;
	}

	@Override
	public TrainNumber[] getTrainNumbers() {
		return this._trainNumbers;
	}

	@Override
	public SeatType[] getSeatTypes() {
		return this._seatTypes;
	}

	@Override
	public CarType[] getCarTypes() {
		return this._carTypes;
	}
	
	@Override
	public void initializeService() throws Exception {
		
		this.initStations();
		this.initSeatTypes();
		this.initCarTypes();
		this.initTrainNumber();
		
		super.initializeService();
	}

	private void initTrainNumber() {
		this._trainNumbers = new TrainNumber[2];
		
		this._trainNumbers[0] = this.CreateG101();
		this._trainNumbers[1] = this.CreateK600K597();
		
	}

	
	private TrainNumber CreateK600K597() {
        
		
		TrainNumber rs = new TrainNumber();
		rs.id = UUID.randomUUID();
		rs.name = "K600/K597";
		Route route = new Route();
		route.id = UUID.randomUUID();
		route.startDate = new LocalDate(0L);
		route.endDate = LocalDate.now().plusYears(10);
		rs.routes.add(route);
	
		Station[] stations = Arrays.copyOfRange(this._stations, 8, this._stations.length);
		
		
		for(int i = 0; i <stations.length; i ++)
		{
		    RouteStop stop = new RouteStop();
		    stop.id = UUID.randomUUID();
		    stop.sequence = i;
		    stop.station = stations[i];
		    route.stops.add(stop);
		}
		
		
		return rs;
	}

	private TrainNumber CreateG101() {
		TrainNumber rs = new TrainNumber();
		
		rs.id = UUID.randomUUID();
		rs.name = "G101";
		Route route = new Route();
		route.id = UUID.randomUUID();
		route.startDate = new LocalDate(0L);
		route.endDate = LocalDate.now().plusYears(10);
		rs.routes.add(route);
	
		for(int i = 0; i <=7; i ++)
		{
		    RouteStop stop = new RouteStop();
		    stop.id = UUID.randomUUID();
		    stop.sequence = i;
		    stop.station = this._stations[i];
		    route.stops.add(stop);
		}
		
		
		return rs;
	}

	private void initCarTypes() {
		this._carTypes = new CarType[4];
		this._carTypes[0] = new CarType(UUID.randomUUID(), "硬座车", 1L);
		for(int i = 1; i <= 118; i ++)
		{
			Seat seat = new Seat();
			seat.id = UUID.randomUUID();
			seat.number = Integer.toString(i);
			seat.type = (1L);
		    this._carTypes[0].seats.add(seat);
		}
		
		this._carTypes[1] = new CarType(UUID.randomUUID(), "硬卧车", 4L);
		for(int i = 1; i <= 66; i ++)
		{
			Seat seat = new Seat();
			seat.id = UUID.randomUUID();
			seat.number = Integer.toString(i);
			seat.type = 4L;
		    this._carTypes[1].seats.add(seat);
		}
		
	
		this._carTypes[2] = new CarType(UUID.randomUUID(), "软座车", 2L);
		for(int i = 1; i <= 118; i ++)
		{
			Seat seat = new Seat();
			seat.id = UUID.randomUUID();
			seat.number = Integer.toString(i);
			seat.type = 2L;
		    this._carTypes[2].seats.add(seat);
		}
		
		this._carTypes[3] = new CarType(UUID.randomUUID(), "二级软卧", 8L);
		for(int i = 1; i <= 44; i ++)
		{
			Seat seat = new Seat();
			seat.id = UUID.randomUUID();
			seat.number = Integer.toString(i);
			seat.type = 8L;
		    this._carTypes[3].seats.add(seat);
		}
		
		
	}

	private void initSeatTypes() {
	
		this._seatTypes = new SeatType[] {
				new SeatType(UUID.randomUUID(), "硬座", 1L),
				new SeatType(UUID.randomUUID(), "软座", 2L),
				new SeatType(UUID.randomUUID(), "硬卧", 4L),
				new SeatType(UUID.randomUUID(), "二级软卧", 8L),
				new SeatType(UUID.randomUUID(), "一级软卧 ", 0x10L)
		};
	}

	private void initStations() {
		String[] names = new String[] {"北京南", "济南西","曲阜东", "枣庄", "南京南","常州北","无锡东","上海虹桥", 
				"韶关东","郴州","衡阳","株洲", "长沙","岳阳", "赤壁", "武昌", "孝感", "广水", "信阳","驻马店", "遂平",
				"漯河", "许昌", "郑州", "新乡", "安阳", "邯郸", "邢台", "石家庄", "定州", "保定", "高碑店", "北京西",
				"沙城", "张家口南", "大同", "丰镇", "集宁南", "呼和浩特东", "包头东", "包头"};
		this._stations = Queries.query(names).select(new Selector<String, Station>(){

			@Override
			public Station select(String name) {
				Station rs = new Station();
				rs.id = UUID.randomUUID();
				rs.name = name;
				return rs;
			}}).toArray(new Station[0]);
		
		
	} 

}
