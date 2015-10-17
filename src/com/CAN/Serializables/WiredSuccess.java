package com.CAN.Serializables;

import java.io.Serializable;

import com.CAN.Nodes.Peer.CommandType;
import com.CAN.Utilities.Utils;

public class WiredSuccess implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private CommandType command;
	private PeerInfo affectedPeer, sourcePeer;
	private String statusMessage;
	private RouteInformation routeInformation;
	
	public WiredSuccess(CommandType command, PeerInfo affectedPeer, PeerInfo sourcePeer, String statusMessage, RouteInformation routeInformation){

		this.command = command;
		this.affectedPeer = affectedPeer;
		this.sourcePeer = sourcePeer;
		this.statusMessage = statusMessage;
		this.routeInformation = routeInformation;
	}

	public RouteInformation getRouteInformation() {
		return routeInformation;
	}

	public void setRouteInformation(RouteInformation routeInformation) {
		this.routeInformation = routeInformation;
	}

	public CommandType getCommand() {
		return command;
	}

	public void setCommand(CommandType command) {
		this.command = command;
	}

	public PeerInfo getAffectedPeer() {
		return affectedPeer;
	}

	public void setAffectedPeer(PeerInfo resultantPeer) {
		this.affectedPeer = resultantPeer;
	}
	
	public PeerInfo getSourcePeer(){
		return this.sourcePeer;
	}
	
	public void setSourcePeer(PeerInfo sourcePeer){
		this.sourcePeer = sourcePeer;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder("");
		builder.append("----------------------------------------------------------------------------------------\n");
		builder.append("Command : "+this.getCommand()+"\n");
		builder.append("Status : "+this.getStatusMessage()+"\n");
		builder.append("Affected Peer hostname : "+this.getAffectedPeer().getHostname()+"\n");
		builder.append("Affected Peer ipAddress : "+this.getAffectedPeer().getIpAddress()+"\n");
		builder.append("Affected Peer zone : "+this.getAffectedPeer().getZone()+"\n");
		builder.append("Route taken : "+this.getRouteInformation()+"\n");
		builder.append("----------------------------------------------------------------------------------------\n");
		
		return builder.toString();
	}

}
