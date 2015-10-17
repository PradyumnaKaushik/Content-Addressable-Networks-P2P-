package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

public class JoinUpdateBootstrap implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private String newHostname;
	private InetAddress newIpAddress;
	
	public JoinUpdateBootstrap(String hostname, InetAddress ipAddress){
		
		this.newHostname = hostname;
		this.newIpAddress = ipAddress;
	}
	
	public String getNewHostname(){
		
		return this.newHostname;
	}
	
	public InetAddress getNewIpAddress(){
		
		return this.newIpAddress;
	}

}
