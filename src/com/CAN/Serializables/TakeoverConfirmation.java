package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class TakeoverConfirmation implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String destinationHostname;
	private InetAddress destinationIpAddress;
	
	public TakeoverConfirmation(String destinationHostname, InetAddress destinationIpAddress) {
		
		this.destinationHostname = destinationHostname;
		this.destinationIpAddress = destinationIpAddress;
	}
	
	public String getDestinationHostname(){
		
		return this.destinationHostname;
	}
	
	public InetAddress getDestinationIpAddress(){
		
		return this.destinationIpAddress;
	}

}
