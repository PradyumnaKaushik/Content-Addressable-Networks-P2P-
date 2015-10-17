package com.CAN.Serializables;

import java.io.Serializable;

import com.CAN.Nodes.Peer.CommandType;

public class WiredFailure implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private PeerInfo sourceInfo;
	private CommandType command;
	private String statusMessage;
	
	public WiredFailure(CommandType command, PeerInfo sourceInfo, String statusMessage){
		
		this.command = command;
		this.sourceInfo = sourceInfo;
		this.statusMessage = statusMessage;
	}

	public PeerInfo getSourceInfo() {
		return sourceInfo;
	}

	public void setSourceInfo(PeerInfo sourceInfo) {
		this.sourceInfo = sourceInfo;
	}

	public CommandType getCommand() {
		return command;
	}

	public void setCommand(CommandType command) {
		this.command = command;
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
		builder.append("------------------------------------------------------------------\n");
		builder.append("Command : "+this.command+"\n");
		builder.append("Source hostname : "+this.sourceInfo.getHostname()+"\n");
		builder.append("Source ipAddress : "+this.sourceInfo.getIpAddress()+"\n");
		builder.append("Status Message : "+this.statusMessage+"\n");
		builder.append("------------------------------------------------------------------\n");
		return builder.toString();
	}

}
