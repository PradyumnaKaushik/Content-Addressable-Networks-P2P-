package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class TemporaryZoneReleaseUpdateNeighbours implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String releasingHostname;
	private InetAddress releasingIpAddress;
	private String newNodeHostname;
	private InetAddress newNodeIpAddress;
	private Zone releasedZone;
	private String hostnameToRoute;
	private InetAddress ipAddressToRoute;
	
	public TemporaryZoneReleaseUpdateNeighbours(String releasingHostname, InetAddress releasingIpAddress, String newNodeHostname, InetAddress newNodeIpAddress, Zone releasedZone, String hostnameToRoute, InetAddress ipAddressToRoute) {
		
		this.releasingHostname = releasingHostname;
		this.releasingIpAddress = releasingIpAddress;
		this.newNodeHostname = newNodeHostname;
		this.newNodeIpAddress = newNodeIpAddress;
		this.hostnameToRoute = hostnameToRoute;
		this.ipAddressToRoute = ipAddressToRoute;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getReleasingHostname() {
		return releasingHostname;
	}

	public InetAddress getReleasingIpAddress() {
		return releasingIpAddress;
	}

	public String getNewNodeHostname() {
		return newNodeHostname;
	}

	public InetAddress getNewNodeIpAddress() {
		return newNodeIpAddress;
	}

	public Zone getReleasedZone() {
		return releasedZone;
	}

	public String getHostnameToRoute() {
		return hostnameToRoute;
	}

	public InetAddress getIpAddressToRoute() {
		return ipAddressToRoute;
	}
	
	
}
