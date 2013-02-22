package org.ng12306.tpms;

import java.io.Serializable;

import org.joda.time.LocalDate;
import org.jboss.netty.channel.Channel;

public class TicketQueryEvent implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7163070002185653449L;
	
	public long sequence;
	
	public String trainNumber;

	public LocalDate date;
	
	public String departureStation;

	public String destinationStation;

	public long seatType;

	public int count;

	
    // 在做序列化的时候,可以忽略这个属性
    // 因为我们没有必要把客户端与服务器的连接也备份下来.
    public transient Channel channel;

	public TicketQueryAction action;

	public void copyTo(TicketQueryEvent other)
	{
		other.action = this.action;
		other.count = this.count;
		other.date = this.date;
		other.departureStation = this.departureStation;
		other.destinationStation = this.destinationStation;
		other.seatType = this.seatType;
		other.sequence = this.sequence;
		other.trainNumber = this.trainNumber;
		other.channel = this.channel;
	}
	
	
}
