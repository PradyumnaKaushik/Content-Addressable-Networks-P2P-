package com.CAN.Connections;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.CAN.Nodes.Peer;
import com.CAN.Nodes.Peer.CommandType;
import com.CAN.Serializables.JoinConfirmation;
import com.CAN.Serializables.JoinUpdateBootstrap;
import com.CAN.Serializables.JoinUpdateNeighbours;
import com.CAN.Serializables.LeaveUpdateBootstrap;
import com.CAN.Serializables.LeaveUpdateNeighbours;
import com.CAN.Serializables.NeighbourInfo;
import com.CAN.Serializables.PeerInfo;
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

public class RevisedSend {

	public static void sendMessage(Object wiredObject){

		final Thread sendThread = new Thread(new Runnable(){

			Socket socket = null;
			ObjectOutputStream objectOutputStream = null;
			@Override
			public void run() {

				if(wiredObject instanceof WiredInsert){
					//call send for INSERT
					send((WiredInsert)wiredObject);
				}
				else if(wiredObject instanceof WiredSearch){
					//call send for SEARCH
					send((WiredSearch)wiredObject);
				}
				else if(wiredObject instanceof WiredJoin){
					//call send for JOIN
					send((WiredJoin)wiredObject);
				}
				else if(wiredObject instanceof JoinUpdateNeighbours){
					//call send for JOIN_UPDATE
					send((JoinUpdateNeighbours)wiredObject);
				}
				else if(wiredObject instanceof TemporaryZoneReleaseUpdateNeighbours){
					//call send for Temporary release of node udpate to the neighbours
					send((TemporaryZoneReleaseUpdateNeighbours)wiredObject);
				}
				else if(wiredObject instanceof JoinUpdateBootstrap){
					//call send for JOIN_UPDATE_BOOTSTRAP
					send((JoinUpdateBootstrap)wiredObject);
				}
				else if(wiredObject instanceof JoinConfirmation){
					//call send for join confirmation
					send((JoinConfirmation)wiredObject);
				}
				else if(wiredObject instanceof LeaveUpdateBootstrap){
					//call send for leave update to be sent to the bootstrap node
					send((LeaveUpdateBootstrap)wiredObject);
				}
				else if(wiredObject instanceof LeaveUpdateNeighbours){
					//call send for leave update to be sent to the neighbours
					send((LeaveUpdateNeighbours)wiredObject);
				}
				else if(wiredObject instanceof WiredZoneTransfer){
					//call send for transferring zone when node is leaving
					send((WiredZoneTransfer)wiredObject);
				}
				else if(wiredObject instanceof TakeoverUpdate){
					//call send for updating neighbours about the change of state of current peer after taking over
					send((TakeoverUpdate)wiredObject);
				}
				else if(wiredObject instanceof TakeoverConfirmation){
					//call send for sending takeoverConfirmation back to the leaving node
					send((TakeoverConfirmation)wiredObject);
				}
				else if(wiredObject instanceof WiredViewActivePeersRequest){
					//call send for sending WiredViewActivePeersRequest to the bootstrap node
					send((WiredViewActivePeersRequest)wiredObject);
				}
				else if(wiredObject instanceof WiredView){
					//call send for sending WiredView object to all the nodes in the network
					send((WiredView)wiredObject);
				}
				else if(wiredObject instanceof WiredSuccess){
					//call send for success and failure
					send((WiredSuccess)wiredObject);
				}
				else if(wiredObject instanceof WiredFailure){
					//call send for success and failure
					send((WiredFailure)wiredObject);
				}
			}

			/*
			 * functions to send messages for each type of object passed
			 */
			private void send(WiredInsert wiredInsert){
				int n = 0;
				NeighbourInfo neighbourToRoute = wiredInsert.getNeighbourToRoute();
				try {

					//trying to connect every 2 seconds until connection is established
					while(socket == null){
						try{
							socket = new Socket(neighbourToRoute.getHostname(), 49161);
						}
						catch(IOException e){
							try {
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredInsert.getNeighbourToRoute().getHostname());
									sourceInfo.setIpAddress(wiredInsert.getNeighbourToRoute().getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.INSERT, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}
						}
					}
					//connection established
					//seriliazing wiredInsert
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredInsert);
					objectOutputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * forward SEARCH
			 */
			private void send(WiredSearch wiredSearch){
				int n = 0;
				NeighbourInfo neighbourToRoute = wiredSearch.getNeighbourToRoute();

				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(neighbourToRoute.getHostname(),49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredSearch.getSourceInfo().getHostname());
									sourceInfo.setIpAddress(wiredSearch.getSourceInfo().getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.SEARCH, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//connection established
					//serializing wiredSearch
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredSearch);

				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * forward JOIN request 
			 */
			private void send(WiredJoin wiredJoin){

				int n = 0;
				try{

					/*
					 * if numberOfHops = 0 then route to Bootstrap node
					 * else if numberOfHops = 1 the route to source
					 * else route to neighbourToRoute
					 */
					if(wiredJoin.getNumberOfHops() == 0){

						while(socket == null){
							//trying to connect every 2 seconds until connection is established
							try{
								socket = new Socket(Peer.getBootstrapHostname(), 49161);
							}
							catch(IOException e){
								try{
									n++;
									if(n == 3){
										PeerInfo sourceInfo = new PeerInfo();
										sourceInfo.setHostName(wiredJoin.getHostnameToRoute());
										sourceInfo.setIpAddress(wiredJoin.getIpAddressToRoute());
										String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
										WiredFailure connectionFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
										Utils.printErrorMessage(connectionFailure.toString());
										
										Peer.possibleCommands.put(CommandType.INSERT, false);
										Peer.possibleCommands.put(CommandType.SEARCH, false);
										Peer.possibleCommands.put(CommandType.JOIN, true);
										Peer.possibleCommands.put(CommandType.LEAVE, false);
										Peer.possibleCommands.put(CommandType.VIEW, true);
										
										break;
									}
									Utils.printErrorMessage("Couldn't connect. Trying again...");
									Thread.sleep(2000);
								}
								catch(InterruptedException ie){
									ie.printStackTrace();
								}
							}
						}
						//connection established
						//serializing wiredJoin to Bootstrap node
						objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
						objectOutputStream.writeObject(wiredJoin);
						objectOutputStream.flush();

					}
					else if(wiredJoin.getNumberOfHops() == 1){
						n = 0;
						while(socket == null){
							try{
								socket = new Socket(wiredJoin.getSourceHostname(), 49161);
							}
							catch(IOException e){
								try{
									n++;
									if(n == 3){
										PeerInfo sourceInfo = new PeerInfo();
										sourceInfo.setHostName(wiredJoin.getHostnameToRoute());
										sourceInfo.setIpAddress(wiredJoin.getIpAddressToRoute());
										String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
										WiredFailure connectionFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
										Utils.printErrorMessage(connectionFailure.toString());
										break;
									}
									Utils.printErrorMessage("Couldn't connect. Trying again...");
									Thread.sleep(2000);
								}
								catch(InterruptedException ie){
									ie.printStackTrace();
								}
							}
						}
						//serializing wiredJoin from Bootstrap node to new node
						objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
						objectOutputStream.writeObject(wiredJoin);
						objectOutputStream.flush();

					}
					else{
						n = 0;
						while(socket == null){
							//trying to connect every 2 seconds until connection us established
							try{
								socket = new Socket(wiredJoin.getHostnameToRoute(), 49161);
							}
							catch(IOException e){
								try{
									n++;
									if(n == 3){
										PeerInfo sourceInfo = new PeerInfo();
										sourceInfo.setHostName(wiredJoin.getHostnameToRoute());
										sourceInfo.setIpAddress(wiredJoin.getIpAddressToRoute());
										String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
										WiredFailure connectionFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
										Utils.printErrorMessage(connectionFailure.toString());
										break;
									}
									Utils.printErrorMessage("Couldn't connect. Trying again...");
									Thread.sleep(2000);
								}
								catch(InterruptedException ie){
									ie.printStackTrace();
								}
							}
							//serializing wiredJoin from new node to peer
							objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
							objectOutputStream.writeObject(wiredJoin);
							objectOutputStream.flush();
						}

					}


				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send JOIN_UPDATE message to neighbours
			 */
			private void send(JoinUpdateNeighbours wiredJoinUpdate){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(wiredJoinUpdate.getNeighbourToRoute().getHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredJoinUpdate.getNeighbourToRoute().getHostname());
									sourceInfo.setIpAddress(wiredJoinUpdate.getNeighbourToRoute().getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing wiredJoinUpdate from affected neighbour (whose zone has gotten split) to its neighbours
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredJoinUpdate);
					objectOutputStream.flush();

				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send temporary zone release (during join) update to the neighbours
			 */
			private void send(TemporaryZoneReleaseUpdateNeighbours tempZoneReleaseUpdateNeighbours){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(tempZoneReleaseUpdateNeighbours.getHostnameToRoute(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(tempZoneReleaseUpdateNeighbours.getHostnameToRoute());
									sourceInfo.setIpAddress(tempZoneReleaseUpdateNeighbours.getIpAddressToRoute());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing wiredJoinUpdate from affected neighbour (whose zone has gotten split) to its neighbours
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(tempZoneReleaseUpdateNeighbours);
					objectOutputStream.flush();

				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send join update to Bootstrap node
			 */
			private void send(JoinUpdateBootstrap joinUpdateBootstrap){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(Peer.getBootstrapHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(Peer.getBootstrapHostname());
									sourceInfo.setIpAddress(Peer.getBootstrapIp());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing joinUpdateBootstrap to Bootstrap node
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(joinUpdateBootstrap);
					objectOutputStream.flush();

				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send join confirmation
			 */
			private void send(JoinConfirmation joinConfirmation){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(joinConfirmation.getSourceHostName(),49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(joinConfirmation.getSourceHostName());
									sourceInfo.setIpAddress(joinConfirmation.getSourceIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing joinConfirmation from peer to new joining node
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(joinConfirmation);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send leave update to bootstrap
			 */
			private void send(LeaveUpdateBootstrap leaveUpdateBootstrap){

				int  n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(Peer.getBootstrapHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(leaveUpdateBootstrap.getHostname());
									sourceInfo.setIpAddress(leaveUpdateBootstrap.getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									
									Peer.possibleCommands.put(CommandType.INSERT, true);
									Peer.possibleCommands.put(CommandType.SEARCH, true);
									Peer.possibleCommands.put(CommandType.VIEW, true);
									Peer.possibleCommands.put(CommandType.JOIN, false);
									Peer.possibleCommands.put(CommandType.LEAVE, true);
									
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing leaveUpdateBootstrap to Bootstrap node from leaving peer
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(leaveUpdateBootstrap);
					objectOutputStream.flush();
				}
				catch(IOException e){
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send leave update to the neighbours
			 */
			private void send(LeaveUpdateNeighbours leaveUpdateNeighbours){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(leaveUpdateNeighbours.getDestinationHostName(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(leaveUpdateNeighbours.getDestinationHostName());
									sourceInfo.setIpAddress(leaveUpdateNeighbours.getDestinationIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									
									Peer.possibleCommands.put(CommandType.INSERT, true);
									Peer.possibleCommands.put(CommandType.SEARCH, true);
									Peer.possibleCommands.put(CommandType.VIEW, true);
									Peer.possibleCommands.put(CommandType.JOIN, false);
									Peer.possibleCommands.put(CommandType.LEAVE, true);
									
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing leaveUpdateNeighbours to the neighbours from the leaving node
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(leaveUpdateNeighbours);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send WiredZoneTransfer
			 */
			private void send(WiredZoneTransfer wiredZoneTransfer){

				int n = 0;
				try{
					while(socket == null){
						try{
							//trying to connect every 2 seconds until connection is established
							socket = new Socket(wiredZoneTransfer.getNeighbourToRoute().getHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredZoneTransfer.getNeighbourToRoute().getHostname());
									sourceInfo.setIpAddress(wiredZoneTransfer.getNeighbourToRoute().getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}

						}
					}
					//connection established
					//serializing wiredZoneTransfer to the neighbour
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredZoneTransfer);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}

			}

			/*
			 * send TakeoverUpdate message to the neighbours
			 */
			private void send(TakeoverUpdate takeoverUpdate){
				int n = 0;
				try{
					while(socket == null){
						try{
							//trying to connect every 2 seconds until connection is established
							socket = new Socket(takeoverUpdate.getDestinationHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(takeoverUpdate.getUpdatedHostname());
									sourceInfo.setIpAddress(takeoverUpdate.getUpdatedIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}

						}
					}
					//connection established
					//serializing wiredZoneTransfer to the neighbour
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(takeoverUpdate);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}

			}

			/*
			 * send TakeoverConfirmation back to the leaving node
			 */
			private void send(TakeoverConfirmation takeoverConfirmation){

				int n = 0;
				try{
					while(socket == null){
						try{
							//trying to connect every 2 seconds until connection is established
							socket = new Socket(takeoverConfirmation.getDestinationHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(Peer.getInstance().getHostName());
									sourceInfo.setIpAddress(Peer.getInstance().getIPaddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);

							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}

						}
					}
					//connection established
					//serializing wiredZoneTransfer to the neighbour
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(takeoverConfirmation);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send WiredViewActivePeersRequest to the bootstrap node
			 */
			private void send(WiredViewActivePeersRequest activePeersRequest){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							if(!Peer.isBootstrap()){
								//if it is not the bootstrap then we need to send it to the bootstrap node
								socket = new Socket(Peer.getBootstrapHostname(), 49161);
							}
							else{
								//if it is the bootstrap node then we need to send the message back to the source hostname
								socket = new Socket(activePeersRequest.getSourceHostname(), 49161);
							}
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(activePeersRequest.getSourceHostname());
									sourceInfo.setIpAddress(activePeersRequest.getSourceIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.VIEW, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}

					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(activePeersRequest);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send WiredView message to all the nodes from the current node
			 */
			private void send(WiredView wiredView){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(wiredView.getHostnameToRoute(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredView.getSourceHostname());
									sourceInfo.setIpAddress(wiredView.getSourceIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(CommandType.VIEW, sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}

					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredView);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send SUCCESS message
			 */
			private void send(WiredSuccess wiredSuccess){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(wiredSuccess.getSourcePeer().getHostname(), 49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredSuccess.getSourcePeer().getHostname());
									sourceInfo.setIpAddress(wiredSuccess.getSourcePeer().getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(wiredSuccess.getCommand(), sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing wiredSuccess back to the source node
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredSuccess);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

			/*
			 * send failure message
			 */
			private void send(WiredFailure wiredFailure){

				int n = 0;
				try{
					while(socket == null){
						//trying to connect every 2 seconds until connection is established
						try{
							socket = new Socket(wiredFailure.getSourceInfo().getHostname(),49161);
						}
						catch(IOException e){
							try{
								n++;
								if(n == 3){
									PeerInfo sourceInfo = new PeerInfo();
									sourceInfo.setHostName(wiredFailure.getSourceInfo().getHostname());
									sourceInfo.setIpAddress(wiredFailure.getSourceInfo().getIpAddress());
									String statusMessage = "FAILURE : Hostname does not exist in the netork.\n";
									WiredFailure connectionFailure = new WiredFailure(wiredFailure.getCommand(), sourceInfo, statusMessage);
									Utils.printErrorMessage(connectionFailure.toString());
									break;
								}
								Utils.printErrorMessage("Couldn't connect. Trying again...");
								Thread.sleep(2000);
							}
							catch(InterruptedException ie){
								ie.printStackTrace();
							}
						}
					}
					//serializing wiredFailure back to the source node
					objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
					objectOutputStream.writeObject(wiredFailure);
					objectOutputStream.flush();
				}
				catch(IOException e){
					e.printStackTrace();
				}
				finally{
					if(socket != null){
						try{
							socket.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
					if(objectOutputStream != null){
						try{
							objectOutputStream.close();
						}
						catch(IOException e){
							e.printStackTrace();
						}
					}
				}
			}

		});
		sendThread.start();



	}

}
