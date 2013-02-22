package org.ng12306.tpms.runtime;

import java.util.UUID;

public class SeatType extends Entity {

	
	public SeatType() {
		
	}
	
	public SeatType(UUID id, String name, long code) {
		this.id = id;
		this.name = name;
		this.code = code;
	}
	
	public String name;

	
	public long code;

		
}
