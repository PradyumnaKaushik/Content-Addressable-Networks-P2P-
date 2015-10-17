package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;

import com.CAN.Nodes.Peer.ViewCategory;

public class WiredView implements Serializable{

	private static final long serialVersionUID = 1L;
	private String peerInformation;
	private String sourceHostname;
	private InetAddress sourceIpAddress;
	private String hostnameToRoute;
	private ViewCategory viewCategory;
	
	public WiredView(String sourceHostname, InetAddress sourceIpAddress, String hostnameToRoute, ViewCategory viewCategory){
		
		this.sourceHostname = sourceHostname;
		this.sourceIpAddress = sourceIpAddress;
		this.hostnameToRoute = hostnameToRoute;
		this.viewCategory = viewCategory;
	}
	
	public static long getSerialVersionUID(){
		
		return serialVersionUID;
	}
	
	public String getSourceHostname(){
		
		return this.sourceHostname;
	}
	
	public InetAddress getSourceIpAddress(){
		
		return this.sourceIpAddress;
	}
	
	public void setPeerInformation(String peerInfo){
		
		this.peerInformation = peerInfo;
	}
	
	public String getPeerInformation(){
		
		return this.peerInformation;
	}
	
	public String getHostnameToRoute(){
		
		return this.hostnameToRoute;
	}
	
	public void setHostnameToRoute(String hostnameToRoute){
		
		this.hostnameToRoute = hostnameToRoute;
	}
	
	public ViewCategory getViewCategory(){
		
		return this.viewCategory;
	}

}
