package com.CAN.Serializables;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PeerInfo implements Serializable{

	private static final long serialVersionUID = 1L;

	private String hostname;
	private InetAddress ipAddress;
	private Zone zone;
	private List<NeighbourInfo> neighbours = new ArrayList<NeighbourInfo>();
	private HashSet<String> fileNames = new HashSet<String>();

	public String getHostname() {
		return hostname;
	}
	public void setHostName(String hostname) {
		this.hostname = hostname;
	}
	public InetAddress getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(InetAddress ipAddress) {
		this.ipAddress = ipAddress;
	}
	public Zone getZone() {
		return zone;
	}
	public void setZone(Zone zone) {
		this.zone = zone;
	}
	public List<NeighbourInfo> getNeighbours() {
		return neighbours;
	}
	public void setNeighbours(List<NeighbourInfo> neighbours) {
		this.neighbours = neighbours;
	}
	public HashSet<String> getFileNames() {
		return fileNames;
	}
	public void setFileNames(HashSet<String> fileNames) {
		this.fileNames = fileNames;
	}

	@Override
	public String toString() {

		StringBuilder builder = new StringBuilder("");
		if(this.getHostname() != null)
			builder.append("Hostname : "+this.getHostname()+"\n");
		if(this.getIpAddress() != null)
			builder.append("Ip Address ; "+this.getIpAddress()+"\n");
		if(this.getZone() != null)
			builder.append("Zone : "+this.getZone().toString()+"\n");
		if(this.getFileNames() != null){
			builder.append("Files : ");
			for(String s : fileNames){
				builder.append(s+",");
			}
		}
		builder.setLength(builder.length()-1);
		builder.append("\n");
		if(this.neighbours != null){
			builder.append("Neighbours : \n");
			builder.append("------------------------------------------------------------------\n");
			for(NeighbourInfo neighbour : this.neighbours){
				builder.append(neighbour.getHostname()+",");
			}
			builder.setLength(builder.length()-1);
		}
		return builder.toString();
	}



}
