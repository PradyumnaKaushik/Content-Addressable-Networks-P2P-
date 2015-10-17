package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

public class RouteInformation implements Serializable {

	private static final long serialVersionUID = 1L;

	private Map<String, InetAddress> routeMap = new LinkedHashMap<String, InetAddress>();

	public void addPeerToRoute(String identifier, InetAddress ipAddress){

		this.routeMap.put(identifier, ipAddress);
	}

	public Map<String, InetAddress> getRoute(){
		return this.routeMap;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		for(String hostname : this.routeMap.keySet()){
			builder.append(hostname+" -> ");
		}
		
		builder.setLength(builder.length()-4);
		return builder.toString();
	}

}
