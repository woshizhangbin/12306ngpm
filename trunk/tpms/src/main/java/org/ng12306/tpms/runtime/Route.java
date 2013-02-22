package org.ng12306.tpms.runtime;

import java.util.*;


import org.joda.time.*;

public class Route extends Entity {

	public LocalDate startDate;
	
	public LocalDate endDate;

	public ArrayList<RouteStop> stops = new ArrayList<RouteStop>();
	
}
