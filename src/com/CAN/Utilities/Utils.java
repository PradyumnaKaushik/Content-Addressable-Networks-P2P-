package com.CAN.Utilities;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.CAN.Serializables.Coordinate;
import com.CAN.Serializables.Zone;

public class Utils {

	public synchronized static void printErrorMessage(String message){
		
		System.err.println(message);
	}

	public synchronized static void printToConsole(String message){

		System.out.println(message);
	}


	public static double computeDistance(Zone zone1, Coordinate destCoord){
		double dist;

		double lowX1 = zone1.getStartX();
		double highX1 = zone1.getEndX();

		double lowY1 = zone1.getStartY();
		double highY1 = zone1.getEndY();

		/*
		 * computing mid point in the zones
		 */
		double x1 = lowX1 + Math.abs(lowX1 - highX1)/2;
		double x2 = destCoord.getXCoord();
		double y1 = lowY1 + Math.abs(lowY1 - highY1)/2;
		double y2 = destCoord.getYCoord();

		dist = Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2), 2));

		return dist;

	}

	public static Coordinate mapKeyToCoordinate(String keyword){

		double xCoord = 0.0;
		double yCoord = 0.0;
		Coordinate mappedCoordinate;

		char[] charArray = keyword.toCharArray();

		for(int i = 0;i < charArray.length;i++){

			if(i%2 == 0){

				yCoord += charArray[i];
			}
			else{

				xCoord += charArray[i];
			}
		}

		xCoord = xCoord%10;
		yCoord = yCoord%10;
		mappedCoordinate = new Coordinate(xCoord, yCoord);
		return mappedCoordinate;
	}


	public static InetAddress getIpAddress(String hostName) throws UnknownHostException{

		InetAddress address = Inet4Address.getByName(hostName);
		return address;

	}

}
