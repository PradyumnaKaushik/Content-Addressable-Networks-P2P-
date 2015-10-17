package com.CAN.Serializables;

import java.io.Serializable;

import com.CAN.Nodes.Peer.CommandType;

public class WiredInsert implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private CommandType command;
	private String keyword;
	private NeighbourInfo neighbourToRoute;
	private PeerInfo sourceInfo;
	private RouteInformation routeInformation;
	
	public WiredInsert(CommandType command, String keyword, NeighbourInfo neighbourToRoute, PeerInfo sourceInfo, RouteInformation routeInformation) {

		this.command = command;
		this.keyword = keyword;
		this.neighbourToRoute = neighbourToRoute;
		this.sourceInfo = sourceInfo;
		this.routeInformation = routeInformation;
	}
	
	public CommandType getCommand() {
		return command;
	}
	public void setCommand(CommandType command) {
		this.command = command;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public NeighbourInfo getNeighbourToRoute() {
		return neighbourToRoute;
	}
	public void setNeighbourToRoute(NeighbourInfo neighbourToRoute) {
		this.neighbourToRoute = neighbourToRoute;
	}
	public PeerInfo getSourceInfo() {
		return sourceInfo;
	}
	public void setSourceInfo(PeerInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}
	public RouteInformation getRouteInformation() {
		return routeInformation;
	}
	public void setRouteInformation(RouteInformation routeInformation) {
		this.routeInformation = routeInformation;
	}

}
