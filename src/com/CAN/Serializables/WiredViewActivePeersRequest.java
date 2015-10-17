package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WiredViewActivePeersRequest implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Map<String, InetAddress> activePeers = new ConcurrentHashMap<String, InetAddress>(10,0.75f,10);
	private String sourceHostname;
	private InetAddress sourceIpAddress;
	
	public Map<String, InetAddress> getActivePeers(){
		
		return this.activePeers;
	}
	
	public void setActivePeers(Map<String, InetAddress> activePeers){
		
		this.activePeers = activePeers;
	}
	
	public static long getSerialVersionUID(){
		
		return serialVersionUID;
	}
	
	public void setSourceHostname(String sourceHostname){
		
		this.sourceHostname = sourceHostname;
	}
	
	public void setSourceIpAddress(InetAddress sourceIpAddress){
		
		this.sourceIpAddress = sourceIpAddress;
	}
	
	public String getSourceHostname(){
		
		return this.sourceHostname;
	}
	
	public InetAddress getSourceIpAddress(){
		
		return this.sourceIpAddress;
	}

}
