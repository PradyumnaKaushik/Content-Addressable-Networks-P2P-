package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class JoinUpdateNeighbours implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String activePeerHostname;
	private String newNodeHostname;
	private InetAddress activePeerIpAddress;
	private InetAddress newNodeIpAddress;
	private Zone activePeerUpdatedZone;
	private Zone newNodeZone;
	private NeighbourInfo neighbourToRoute;
	
	public JoinUpdateNeighbours(String activePeerHostname, String newNodeHostname, InetAddress activePeerIpAddress, InetAddress newNodeIpAddress, Zone activePeerUpdatedZone, Zone newNodeZone, NeighbourInfo neighbourToRoute){
		
		this.activePeerHostname = activePeerHostname;
		this.newNodeHostname = newNodeHostname;
		this.activePeerIpAddress = activePeerIpAddress;
		this.newNodeIpAddress = newNodeIpAddress;
		this.activePeerUpdatedZone = activePeerUpdatedZone;
		this.newNodeZone = newNodeZone;
		this.neighbourToRoute = neighbourToRoute;
	}

	public NeighbourInfo getNeighbourToRoute() {
		return neighbourToRoute;
	}

	public String getActivePeerHostname() {
		return activePeerHostname;
	}

	public String getNewNodeHostname() {
		return newNodeHostname;
	}

	public InetAddress getActivePeerIpAddress() {
		return activePeerIpAddress;
	}

	public InetAddress getNewNodeIpAddress() {
		return newNodeIpAddress;
	}

	public Zone getActivePeerUpdatedZone() {
		return activePeerUpdatedZone;
	}

	public Zone getNewNodeZone() {
		return newNodeZone;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
	

}
