package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class NeighbourInfo implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private Zone zone, tempZone;
	private InetAddress ipAddress;
	private String hostname;
	private int portNumber;
	
	public NeighbourInfo(Zone zone, InetAddress ipAddress, int portNumber, String hostname) {
		
		if(zone != null && ipAddress != null){
			this.zone = zone;
			this.ipAddress = ipAddress;
			this.portNumber = portNumber;
			this.hostname = hostname;
		}
	}
	
	public Zone getZone() {
		return zone;
	}
	public void setZone(Zone zone) {
		this.zone = zone;
	}
	public Zone getTempZone() {
		return tempZone;
	}

	public void setTempZone(Zone tempZone) {
		this.tempZone = tempZone;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	
	public String getHostname(){
		
		return this.hostname;
	}
	
	public boolean hasContents(){
		
		if(this.zone != null && this.ipAddress != null){
			
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		builder.append("hostname : "+this.getHostname()+"\n");
		builder.append("ipAddress : "+this.getIpAddress().getHostAddress()+"\n");
		builder.append("zone : "+this.getZone()+"\n");
		return builder.toString();
		
	}
	
	@Override
	public boolean equals(Object neighbour) {
		
		if(neighbour instanceof NeighbourInfo){
			NeighbourInfo n = (NeighbourInfo)neighbour;
			if(this.hostname.equals(n.hostname) && this.ipAddress.equals(n.ipAddress)){
				
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
	}
}
