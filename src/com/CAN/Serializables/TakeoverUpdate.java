package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class TakeoverUpdate implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String updatedHostname;
	private InetAddress updatedIpAddress;
	private String destinationHostname;
	private Zone updatedZone;
	private boolean isCompleteTakeover;

	public TakeoverUpdate(String updatedNodeHostname, InetAddress updateNodeIpAddress, String destinationHostname,
			Zone updatedZone, boolean isCompleteTakeover) {
		
		this.updatedHostname = updatedNodeHostname;
		this.updatedIpAddress = updateNodeIpAddress;
		this.destinationHostname = destinationHostname;
		this.updatedZone = updatedZone;
		this.isCompleteTakeover = isCompleteTakeover;
	}

	public String getUpdatedHostname() {
		return this.updatedHostname;
	}

	public InetAddress getUpdatedIpAddress() {
		return this.updatedIpAddress;
	}

	public Zone getUpdatedZone() {
		return this.updatedZone;
	}

	public String getDestinationHostname() {
		return this.destinationHostname;
	}

	public boolean isCompleteTakeover(){
		
		return this.isCompleteTakeover;
	}
}
