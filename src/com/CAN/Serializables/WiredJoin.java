package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WiredJoin implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String sourceHostName;
	private InetAddress sourceIpAddress;
	private Coordinate randomCoordinate;
	private String hostnameToRoute;
	private InetAddress ipAddressToRoute;
	private Map<String, InetAddress> activePeersInfo;
	private ConcurrentHashMap<String , NeighbourInfo> routingTable;
	private int numberOfHops;
	private RouteInformation routeInformation;

	
	public WiredJoin(String sourceHostname, InetAddress sourceIpAddress, String hostnameToRoute, InetAddress ipAddressToRoute, RouteInformation routeInformation){
		
		this.sourceHostName = sourceHostname;
		this.sourceIpAddress = sourceIpAddress;
		this.hostnameToRoute = hostnameToRoute;
		this.ipAddressToRoute = ipAddressToRoute;
		this.numberOfHops = 0;
		this.routeInformation = routeInformation;
	}


	public Coordinate getRandomCoordinate() {
		return randomCoordinate;
	}


	public void setRandomCoordinate(Coordinate randomCoordinate) {
		this.randomCoordinate = randomCoordinate;
	}


	public String getSourceHostname() {
		return this.sourceHostName;
	}


	public void setSourceHostname(String hostname) {
		this.sourceHostName = hostname;
	}


	public InetAddress getSourceIpAddress() {
		return sourceIpAddress;
	}


	public void setSourceIpAddress(InetAddress ipAddress) {
		this.sourceIpAddress = ipAddress;
	}


	public Map<String, InetAddress> getActivePeersInfo() {
		return activePeersInfo;
	}


	public void setActivePeersInfo(Map<String, InetAddress> activePeersInfo) {
		this.activePeersInfo = activePeersInfo;
	}


	public ConcurrentHashMap<String, NeighbourInfo> getRoutingTable() {
		return routingTable;
	}


	public void setRoutingTable(
			ConcurrentHashMap<String, NeighbourInfo> routingTable) {
		this.routingTable = routingTable;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public void setNumberofHops(int n){
		
		this.numberOfHops = n;
	}
	
	public void setHostnameToRoute(String hostnameToRoute){
		
		this.hostnameToRoute = hostnameToRoute;
	}
	
	public void setIpAddressToRoute(InetAddress ipAddressToRoute){
		this.ipAddressToRoute = ipAddressToRoute;
	}
	
	public int getNumberOfHops(){
		
		return this.numberOfHops;
	}
	
	public String getHostnameToRoute(){
		
		return this.hostnameToRoute;
	}
	
	public InetAddress getIpAddressToRoute(){
		
		return this.ipAddressToRoute;
	}
	
	public void setRouteInformation(String hostname, InetAddress ipAddress){
		this.routeInformation.addPeerToRoute(hostname, ipAddress);
	}
	
	public RouteInformation getRouteInformation(){
		return this.routeInformation;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		builder.append("------------------------------------------------------------------\n");
		builder.append("Source hostname : "+this.sourceHostName+"\n");
		builder.append("Source ipAddress : "+this.sourceIpAddress+"\n");
		builder.append("hostname to route : "+this.hostnameToRoute+"\n");
		builder.append("ipAddress to route : "+this.ipAddressToRoute);
		builder.append("------------------------------------------------------------------\n");
		
		return builder.toString();
	}
}
