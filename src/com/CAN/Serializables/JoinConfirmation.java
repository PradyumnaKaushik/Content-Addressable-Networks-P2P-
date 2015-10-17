package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JoinConfirmation implements Serializable{

	private static final long serialVersionUID = 1L;

	private Zone zone;
	private ConcurrentHashMap<String, NeighbourInfo> routingTable;
	private InetAddress sourceIpAddress;
	private String sourceHostName;
	private int numberOfSplits;
	private HashSet<String> transferedFiles;
	
	public JoinConfirmation(Zone zone, ConcurrentHashMap<String, NeighbourInfo> routingTable, InetAddress sourceIpAddress, String sourceHostName, int numberOfSplits, HashSet<String> transferedFiles){
		
		this.zone = zone;
		this.routingTable = routingTable;
		this.sourceIpAddress = sourceIpAddress;
		this.sourceHostName = sourceHostName;
		this.numberOfSplits = numberOfSplits;
		if(transferedFiles != null){
			this.transferedFiles = transferedFiles;
		}
	}
	
	public Zone getZone(){
		
		return this.zone;
	}
	
	public ConcurrentHashMap<String, NeighbourInfo> getRoutingTable(){
		
		return this.routingTable;
	}
	
	public InetAddress getSourceIpAddress(){
		
		return this.sourceIpAddress;
	}
	
	public String getSourceHostName(){
		
		return this.sourceHostName;
	}
	
	public int getNumberOfSplits(){
		
		return this.numberOfSplits;
	}
	
	public HashSet<String> getTransferedFiles(){
		
		if(this.transferedFiles == null){
			return null;
		}
		else{
			return this.transferedFiles;
		}
	}
}
