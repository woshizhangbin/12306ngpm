package org.ng12306.tpms.runtime;

import java.util.ArrayList;

import org.joda.time.*;

public class Train extends Entity {

	public LocalDate departureDate;

	public TrainNumber trainNumber;
	
	public ArrayList<Car> cars = new ArrayList<Car>();
	
}


