package org.ng12306.tpms.runtime;

import java.util.ArrayList;


public class Car extends Entity {

	
	public CarType carType;
	
	public int carNumber;
	
	public ArrayList<OperatingSeat> seats = new ArrayList<OperatingSeat>();

	public Train train;


	
	
}
