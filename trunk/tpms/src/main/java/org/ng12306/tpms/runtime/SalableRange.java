package org.ng12306.tpms.runtime;

public class SalableRange {

	public int departureStart;

	
	public int departureEnd;

	public int destinationStart;

	
	public int destinationEnd;

		
	public void copyTo(SalableRange other)
	{
		other.departureStart = this.departureStart;
		other.departureEnd = this.departureEnd;
		other.destinationStart = this.destinationStart;
		other.destinationEnd = this.destinationEnd;
	}

	
	
}
