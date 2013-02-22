package org.ng12306.tpms.runtime;

import java.util.ArrayList;

import java.util.UUID;

public class CarType extends Entity {

	public CarType() {
		
	}
	
	
	public CarType(UUID id, String name, long seatType) {
		
		this.id = id;
		this.name = name;
		this.seatType = seatType;
	}
	
	public String name;

	public long seatType;

	public ArrayList<Seat> seats = new ArrayList<Seat>();
	
}
