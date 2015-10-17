package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class LeaveUpdateBootstrap implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String hostname;
	private InetAddress ipAddress;
	
	public LeaveUpdateBootstrap(String hostname, InetAddress ipAddress){
		
		this.hostname = hostname;
		this.ipAddress = ipAddress;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getHostname() {
		return hostname;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}
	
}
