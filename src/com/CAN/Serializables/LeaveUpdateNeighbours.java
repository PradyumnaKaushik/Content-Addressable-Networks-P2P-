package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class LeaveUpdateNeighbours implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String leavingHostname;
	private InetAddress leavingIpAddress;
	private InetAddress destinationIpAddress;
	private String destinationHostName;
	private NeighbourInfo neighbourTakingOver;
	
	public LeaveUpdateNeighbours(String leavingHostname, InetAddress leavingIpAddress, InetAddress destinationIpAddress,String destinationHostName, NeighbourInfo neighbourTakingOver){
		
		this.leavingHostname = leavingHostname;
		this.leavingIpAddress = leavingIpAddress;
		this.destinationIpAddress = destinationIpAddress;
		this.destinationHostName = destinationHostName;
		this.neighbourTakingOver = neighbourTakingOver;
	}
	
	public String getDestinationHostName() {
		return destinationHostName;
	}

	public String getLeavingHostname(){
		
		return this.leavingHostname;
	}
	
	public InetAddress getLeavingIpAddress(){
		
		return this.leavingIpAddress;
	}

	public NeighbourInfo getNeighbourTakingOver(){
		
		return this.neighbourTakingOver;
	}
	
	public InetAddress getDestinationIpAddress(){
		
		return this.destinationIpAddress;
	}
	
}
