package org.ng12306.tpms.runtime;

import java.util.UUID;

public class PlanTicket extends Entity {
	
	public int startStop;
		
	public int endStop;

	public UUID originalId;

	public OperatingSeat seat;
	
	public SalableRange salableRange = new SalableRange();

	public StopRangeGroup group;
}
