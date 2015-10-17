package com.CAN.Serializables;

import java.io.Serializable;

public class Coordinate implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private double xCoord;
	private double yCoord;
	
	public Coordinate(double x, double y){
		
		this.xCoord = x;
		this.yCoord = y;
	}

	public double getXCoord() {
		return xCoord;
	}

	public void setXCoord(double xCoord) {
		this.xCoord = xCoord;
	}

	public double getYCoord() {
		return yCoord;
	}

	public void setYCoord(double yCoord) {
		this.yCoord = yCoord;
	}
	
	public String toString(){
		
		String message = xCoord+","+yCoord;
		return message;
	}
	

}
