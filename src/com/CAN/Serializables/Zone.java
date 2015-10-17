package com.CAN.Serializables;

import java.io.Serializable;

import com.CAN.Utilities.Utils;

public class Zone implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private double startX;
	private double startY;
	private double endX;
	private double endY;
	
	public Zone(double startX, double startY, double endX, double endY){
		
		if(startX > 10 || startX < 0 || endX > 10 || endX < 0 || startY > 10 || startY < 0 || endY > 10 || endY < 0 ){
			Utils.printErrorMessage("Error : value of coordinates out of range.\nPlease enter a value between 0 and 10.");
		}
		else{
			this.startX = startX;
			this.startY = startY;
			this.endX = endX;
			this.endY = endY;
		}
	}
	
	public void setStartX(double startX){
		
//		if(startX > 10 || startX < 0){
//			Utils.printErrorMessage("Error : value of coordinates out of range.\nPlease enter a value between 0 and 10.");
//		}
//		else{
			this.startX = startX;
		//}
	}
	
	public void setStartY(double startY){
		
//		if(startY > 10 || startY < 0){
//			Utils.printErrorMessage("Error : value of coordinates out of range.\nPlease enter a value between 0 and 10.");
//		}
//		else{
			this.startY = startY;
		//}
	}
	
	public void setEndX(double endX){
		
//		if(endX > 10 || endX < 0){
//			Utils.printErrorMessage("Error : value of coordinates out of range.\nPlease enter a value between 0 and 10.");
//		}
//		else{
			this.endX = endX;
		//}
	}
	
	public void setEndY(double endY){
		
//		if(endY > 10 || endY < 0){
//			Utils.printErrorMessage("Error : value of coordinates out of range.\nPlease enter a value between 0 and 10.");
//		}
//		else{
			this.endY = endY;
		//}
	}
	
	public double getStartX(){
		return this.startX;
	}
	
	public double getStartY(){
		return this.startY;
	}
	
	public double getEndX(){
		return this.endX;
	}
	
	public double getEndY(){
		return this.endY;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof Zone)){
			
			return false;
		}
		else{
			Zone zone = (Zone)obj;
			if(this.startX == zone.startX && this.startY == zone.startY){
				
				if(this.endX == zone.endX && this.endY == zone.endY){
					
					return true;
				}
				else{
					
					return false;
				}
			}
			else{
				
				return false;
			}
		}
	}
	
	@Override
	public String toString() {
		
		String zoneString = "("+this.startX+"-"+this.endX+","+this.startY+"-"+this.endY+")";
		return zoneString;
	}
	
}
