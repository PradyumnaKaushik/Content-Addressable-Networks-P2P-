package com.CAN.Connections;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.CAN.Nodes.Peer;
import com.CAN.Nodes.Peer.ViewCategory;
import com.CAN.Serializables.JoinConfirmation;
import com.CAN.Serializables.JoinUpdateBootstrap;
import com.CAN.Serializables.JoinUpdateNeighbours;
import com.CAN.Serializables.LeaveUpdateBootstrap;
import com.CAN.Serializables.LeaveUpdateNeighbours;
import com.CAN.Serializables.TakeoverConfirmation;
import com.CAN.Serializables.TakeoverUpdate;
import com.CAN.Serializables.TemporaryZoneReleaseUpdateNeighbours;
import com.CAN.Serializables.WiredFailure;
import com.CAN.Serializables.WiredInsert;
import com.CAN.Serializables.WiredJoin;
import com.CAN.Serializables.WiredSearch;
import com.CAN.Serializables.WiredSuccess;
import com.CAN.Serializables.WiredView;
import com.CAN.Serializables.WiredViewActivePeersRequest;
import com.CAN.Serializables.WiredZoneTransfer;
import com.CAN.Utilities.Utils;

public class RevisedReceive {

	private volatile static int viewsReturned = 1;
	private static int totalViewsRequired;
	private volatile static List<String> peerInformation = new ArrayList<String>();

	public static List<String> getPeerInformation(){

		return peerInformation;
	}

	public static void setTotalViewsRequired(int n){

		totalViewsRequired = n;
	}

	public static void startServer(int port){

		final Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {

				try{
					final ServerSocket serverSocket = new ServerSocket(port);

					while(true){

						final Socket socket = serverSocket.accept();
						ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

						Object wiredObject = objectInputStream.readObject();

						Peer peerInstance = Peer.getInstance();



						if(wiredObject instanceof WiredInsert){

							WiredInsert wiredInsert = (WiredInsert)wiredObject;

							peerInstance.insert(wiredInsert);
						}
						else if(wiredObject instanceof WiredSearch){

							WiredSearch wiredSearch = (WiredSearch)wiredObject;
							peerInstance.search(wiredSearch);
						}
						else if(wiredObject instanceof WiredJoin){

							WiredJoin wiredJoin = (WiredJoin)wiredObject;
							peerInstance.join(wiredJoin);
						}
						else if(wiredObject instanceof JoinUpdateNeighbours){

							JoinUpdateNeighbours joinUpdateNeighbours = (JoinUpdateNeighbours)wiredObject;
							peerInstance.updateRoutingTableForNewNode(joinUpdateNeighbours);
						}
						else if(wiredObject instanceof TemporaryZoneReleaseUpdateNeighbours){

							TemporaryZoneReleaseUpdateNeighbours temporaryZoneReleaseUpdateNeighbours = (TemporaryZoneReleaseUpdateNeighbours)wiredObject;
							peerInstance.updateTempZoneRelease(temporaryZoneReleaseUpdateNeighbours);
						}
						else if(wiredObject instanceof JoinUpdateBootstrap){

							JoinUpdateBootstrap joinUpdateBootstrap = (JoinUpdateBootstrap)wiredObject;
							peerInstance.updateActivePeers(joinUpdateBootstrap);
						}
						else if(wiredObject instanceof JoinConfirmation){
							JoinConfirmation joinConfirmation = (JoinConfirmation)wiredObject;
							peerInstance.initializeState(joinConfirmation);
						}
						else if(wiredObject instanceof LeaveUpdateBootstrap){

							LeaveUpdateBootstrap leaveUpdateBootstrap = (LeaveUpdateBootstrap)wiredObject;
							peerInstance.removeActivePeerEntry(leaveUpdateBootstrap);
						}
						else if(wiredObject instanceof LeaveUpdateNeighbours){

							LeaveUpdateNeighbours leaveUpdateNeighbours = (LeaveUpdateNeighbours)wiredObject;
							peerInstance.removeNeighbourFromRoutingTable(leaveUpdateNeighbours);
						}
						else if(wiredObject instanceof TakeoverUpdate){

							TakeoverUpdate takeoverUpdate = (TakeoverUpdate)wiredObject;
							peerInstance.updateNeighbourState(takeoverUpdate);
						}
						else if(wiredObject instanceof TakeoverConfirmation){

							peerInstance.deinitializeState();
						}
						else if(wiredObject instanceof WiredZoneTransfer){

							WiredZoneTransfer wiredZoneTransfer = (WiredZoneTransfer)wiredObject;
							peerInstance.takeover(wiredZoneTransfer);
						}
						else if(wiredObject instanceof WiredViewActivePeersRequest){

							WiredViewActivePeersRequest activePeersRequest = (WiredViewActivePeersRequest)wiredObject;
							if(Peer.isBootstrap()){
								peerInstance.retrieveActivePeers(activePeersRequest);
							}
							else{
								peerInstance.forwardWiredView(activePeersRequest);
							}
						}
						else if(wiredObject instanceof WiredView){

							WiredView view = (WiredView)wiredObject;
							if(view.getViewCategory().equals(ViewCategory.MULTI)){
								if(view.getSourceHostname().equals(peerInstance.getHostName())){
									peerInformation.add(view.getPeerInformation());
									viewsReturned++;
									StringBuilder builder = new StringBuilder("");
									if(viewsReturned == totalViewsRequired){

										for(String peerInfo : peerInformation){
											builder.append(peerInfo);
										}
										
										//adding current peer's information to builder
										builder.append(peerInstance.toString());
										
										Utils.printToConsole(builder.toString());
										totalViewsRequired = 0;
										viewsReturned = 1;
										peerInformation.clear();
									}
								}
								else{
									peerInstance.retrievePeerInformation(view);
								}
							}
							else{
								if(peerInstance.getHostName().equals(view.getSourceHostname())){
									Utils.printToConsole(view.getPeerInformation());
								}
								else{
									peerInstance.retrievePeerInformation(view);
								}
							}

						}
						else if(wiredObject instanceof WiredSuccess){

							WiredSuccess wiredSuccess = (WiredSuccess)wiredObject;
							Utils.printToConsole(wiredSuccess.toString());
						}
						else if(wiredObject instanceof WiredFailure){

							WiredFailure wiredFailure = (WiredFailure)wiredObject;
							Utils.printErrorMessage(wiredFailure.toString());
						}
					}

				}
				catch(IOException e){

					e.printStackTrace();
				}
				catch(ClassNotFoundException e){

					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
}
