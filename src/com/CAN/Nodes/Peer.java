package com.CAN.Nodes;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.CAN.Connections.RevisedReceive;
import com.CAN.Connections.RevisedSend;
import com.CAN.Exceptions.ClosestNeighbourUnavailableException;
import com.CAN.Serializables.Coordinate;
import com.CAN.Serializables.JoinConfirmation;
import com.CAN.Serializables.JoinUpdateBootstrap;
import com.CAN.Serializables.JoinUpdateNeighbours;
import com.CAN.Serializables.LeaveUpdateBootstrap;
import com.CAN.Serializables.LeaveUpdateNeighbours;
import com.CAN.Serializables.NeighbourInfo;
import com.CAN.Serializables.PeerInfo;
import com.CAN.Serializables.RouteInformation;
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
import com.CAN.Serializables.Zone;
import com.CAN.Utilities.Utils;

public class Peer {
	
	//HashMap to store the commands possible
	public static HashMap<CommandType, Boolean> possibleCommands = new HashMap<Peer.CommandType, Boolean>();
	
	//HashMap to store the input formats for the commands
	public static HashMap<CommandType, String> formats = new HashMap<Peer.CommandType, String>();

	//IP address of this node
	private InetAddress IPaddress = getIpAddress();

	//Identifier of this peer on the network
	private String hostname;

	//The zone that the peer belongs to.
	private Zone zone;

	//temporary zone that has been taken over
	private Zone tempZone;

	//specifying the boundaries of the coordinate space
	private static final int LOWER_BOUND_X = 0;
	private static final int LOWER_BOUND_Y = 0;
	private static final int UPPER_BOUND_X = 10;
	private static final int UPPER_BOUND_Y = 10;

	/*
	 * variable to hold the identifiers and the IP addresses of all the active peers in the CAN.
	 * This variable will contain values only in the case of a bootstrap node.
	 */
	private static Map<String,InetAddress> activePeers;

	//constant variable to hold the number of peers possible in the network
	private static int peerLimit = 1;


	//List of all the files that are stored in the peer.
	//private List<String> fileNames = new ArrayList<String>();
	private HashSet<String> fileNames = new HashSet<String>();

	//List of all files stored at the temporary zone taken over
	private HashSet<String> tempFileNames = new HashSet<String>();

	//final variable to store the bootstrap hostname
	private static final String BOOTSTRAP_HOSTNAME = "remote07.cs.binghamton.edu";

	//ipAddress of Bootstrap node
	private static InetAddress bootstrapIp;

	//number of times split
	private static int numberOfSplits;

	/*
	 * HashTable to map id to IPaddress. 
	 * The IPaddress in in turn mapped to a zone. This is done for all the neighbors only.
	 * Here we are implementing ConcurrentHashMap because HashTables are not used anymore.
	 */
	private ConcurrentHashMap<String , NeighbourInfo> routingTable = new ConcurrentHashMap<String , NeighbourInfo>(10,0.75f,10);

	//temporary routing table to store the neighbour information of the temporary zone taken over
	private ConcurrentHashMap<String, NeighbourInfo> tempRoutingTable = new ConcurrentHashMap<String, NeighbourInfo>(10,0.75f,10);

	//singleton instance
	private static Peer instance = null;

	//creating private singleton constructor
	private Peer() throws UnknownHostException{
		setBootstrapIp();
		setHostName(InetAddress.getLocalHost().getHostName());
		setIPaddress(InetAddress.getLocalHost());
		Peer.numberOfSplits = 0;
	}

	//method to return the singleton instance of Peer.
	public static Peer getInstance() throws UnknownHostException{

		if(instance != null){
			return instance;
		}
		else{
			instance = new Peer();
			return instance;
		}
	}

	//enum of commands
	public static enum CommandType{

		INSERT,
		SEARCH,
		VIEW,
		UPDATE,
		JOIN,
		LEAVE,
		SUCCESS,
		FAILURE;
	}

	//enum of view categories
	public static enum ViewCategory{

		SINGLE,
		MULTI;
	}

	public synchronized void insert(WiredInsert wiredInsert){

		Coordinate mappedCoordinate;
		String successMessage;

		/*
		 * if source is null then it means that the current peer is the source.
		 * Hence we create a new PeerInfo object and then set all the required information of the current peer.
		 * Else if source is not null then it will contain the information of the peer that initiated this message.
		 */
		if(wiredInsert.getSourceInfo() == null){
			PeerInfo sourceInfo = new PeerInfo();
			sourceInfo.setHostName(this.getHostName());
			sourceInfo.setIpAddress(this.getIpAddress());
			sourceInfo.setFileNames(this.getFileNames());
			sourceInfo.setNeighbours(this.getNeighbours());
			sourceInfo.setZone(this.getZone());

			wiredInsert.setSourceInfo(sourceInfo);

		}

		//adding current peer's identifier and ipAddress to the routeMap
		wiredInsert.getRouteInformation().addPeerToRoute(this.getHostName(), this.getIpAddress());

		/*
		 * use the hash function to map the keyword to a coordinate.
		 * Check if coordinate is present in the current peer's zone. If yes then perform the operation and return status.
		 * Else find the closest neighbour, add current peer's ipAddress to the routeMap, and forward the request.
		 */
		mappedCoordinate = Utils.mapKeyToCoordinate(wiredInsert.getKeyword());

		if(isDestination(mappedCoordinate, this.getZone())){

			//adding the keyword (file name) to the list of filenames in the current Peer
			this.fileNames.add(wiredInsert.getKeyword());

			//creating the success message
			successMessage = "INSERT operation successful.\nInserted file : "+wiredInsert.getKeyword()+".\nPeer hostName : "+this.getHostName()+".\nPeer ipAddress : "+this.getIpAddress();

			/*
			 * checking whether current peer is the source.
			 * If yes then we print the success of the insert operation to the console. (this happens only when success message comes back to the source)
			 * Else we send the success message back to the previous peer until it reaches the source.
			 */
			if(wiredInsert.getSourceInfo().getIpAddress().equals(this.getIpAddress())){

				//creating a WiredSuccess object and routing it to the same machine (as this machine is the source)
				WiredSuccess wiredSuccess = new WiredSuccess(CommandType.INSERT, wiredInsert.getSourceInfo(), wiredInsert.getSourceInfo(), successMessage, wiredInsert.getRouteInformation());
				RevisedSend.sendMessage(wiredSuccess);
			}
			else{

				/*
				 * create PeerInfo object for the affected peer (current peer).
				 * create WiredSuccess object and send the success message to the source.
				 * spawn new thread to send the WiredSuccess object to the source.
				 */
				PeerInfo affectedPeer = new PeerInfo();
				affectedPeer.setHostName(this.getHostName());
				affectedPeer.setIpAddress(this.getIpAddress());
				affectedPeer.setNeighbours(this.getNeighbours());
				affectedPeer.setZone(this.getZone());
				affectedPeer.setFileNames(this.getFileNames());

				WiredSuccess wiredSuccess = new WiredSuccess(CommandType.INSERT, affectedPeer, wiredInsert.getSourceInfo(), successMessage, wiredInsert.getRouteInformation());

				RevisedSend.sendMessage(wiredSuccess);
			}
		}
		else if(this.tempZone != null && isDestination(mappedCoordinate, this.getTempZone())){

			//adding the keyword (file name) to the list of filenames in the current Peer
			this.tempFileNames.add(wiredInsert.getKeyword());

			//creating the success message
			successMessage = "INSERT operation successful.\nInserted file : "+wiredInsert.getKeyword()+".\nPeer hostName : "+this.getHostName()+".\nPeer ipAddress : "+this.getIpAddress();

			/*
			 * checking whether current peer is the source.
			 * If yes then we print the success of the insert operation to the console. (this happens only when success message comes back to the source)
			 * Else we send the success message back to the previous peer until it reaches the source.
			 */
			if(wiredInsert.getSourceInfo().getIpAddress().equals(this.getIpAddress())){

				//setting the zone in the sourceInfo to the tempZone
				wiredInsert.getSourceInfo().setZone(this.tempZone);

				//creating a WiredSuccess object and routing it to the same machine (as this machine is the source)
				WiredSuccess wiredSuccess = new WiredSuccess(CommandType.INSERT, wiredInsert.getSourceInfo(), wiredInsert.getSourceInfo(), successMessage, wiredInsert.getRouteInformation());
				RevisedSend.sendMessage(wiredSuccess);
			}
			else{

				/*
				 * create PeerInfo object for the affected peer (current peer).
				 * create WiredSuccess object and send the success message to the source.
				 * spawn new thread to send the WiredSuccess object to the source.
				 */
				PeerInfo affectedPeer = new PeerInfo();
				affectedPeer.setHostName(this.getHostName());
				affectedPeer.setIpAddress(this.getIpAddress());
				affectedPeer.setNeighbours(this.getNeighbours());
				affectedPeer.setZone(this.getTempZone());
				affectedPeer.setFileNames(this.getFileNames());

				WiredSuccess wiredSuccess = new WiredSuccess(CommandType.INSERT, affectedPeer, wiredInsert.getSourceInfo(), successMessage, wiredInsert.getRouteInformation());

				RevisedSend.sendMessage(wiredSuccess);
			}
		}
		else{
			NeighbourInfo neighbourToRoute;
			try {

				neighbourToRoute = findClosestNeighbour(mappedCoordinate, wiredInsert.getRouteInformation());
				wiredInsert.setNeighbourToRoute(neighbourToRoute);
				wiredInsert.getRouteInformation().addPeerToRoute(this.getHostName(), this.getIpAddress());

				//routing insert command to neighbour
				RevisedSend.sendMessage(wiredInsert);
			} catch (ClosestNeighbourUnavailableException e) {
				WiredFailure wiredFailure = new WiredFailure(CommandType.INSERT, wiredInsert.getSourceInfo(), "Sorry! Neighbour unavailable");
				RevisedSend.sendMessage(wiredFailure);
			}


		}
	}

	public synchronized void search(WiredSearch wiredSearch){

		/*
		 * Map keyword to coordinate space.
		 * Check whether sourceInfo is null. If yes then the source is the current peer.
		 * Check whether current peer is the destination peer.
		 * If yes then,
		 * 		Check whether the current peer contains the keyword
		 * 		If yes then add current peer to the routeInformation and create WiredSuccess object and send it back to the source
		 * 		If not then create WiredFailure object and spawn new thread to send to the source of the request.
		 * If not then,
		 * 		find the closest neighbour to route the request to.
		 * 		add current peer to the routeInformation and then forward the WiredSearch object to the closest neighbour.
		 */

		String keyword = wiredSearch.getKeyword();
		Coordinate mappedCoordinate = Utils.mapKeyToCoordinate(keyword);

		if(wiredSearch.getSourceInfo() == null){

			PeerInfo sourceInfo = new PeerInfo();
			sourceInfo.setHostName(this.getHostName());
			sourceInfo.setIpAddress(this.getIpAddress());
			sourceInfo.setFileNames(this.getFileNames());
			sourceInfo.setNeighbours(this.getNeighbours());
			sourceInfo.setZone(this.zone);

			wiredSearch.setSourceInfo(sourceInfo);
		}

		if(isDestination(mappedCoordinate, this.getZone())){

			if(this.getFileNames().size() > 0){
				if(this.getFileNames().contains(keyword)){

					PeerInfo affectedPeer = new PeerInfo();
					affectedPeer.setHostName(this.getHostName());
					affectedPeer.setIpAddress(this.getIpAddress());
					affectedPeer.setNeighbours(this.getNeighbours());
					affectedPeer.setFileNames(this.getFileNames());
					affectedPeer.setZone(this.getZone());

					String successMessage = "Search successful";
					wiredSearch.getRouteInformation().addPeerToRoute(this.getHostName(), this.getIpAddress());
					WiredSuccess wiredSuccess = new WiredSuccess(CommandType.SEARCH, affectedPeer, wiredSearch.getSourceInfo(), successMessage, wiredSearch.getRouteInformation());

					/*
					 * if source is the current peer then we print the success on the console.
					 * Else we serialize the WiredSuccess object to the source peer.
					 */
					if(wiredSearch.getSourceInfo().getIpAddress().equals(this.getIpAddress())){

						Utils.printToConsole(wiredSuccess.toString());
					}
					else{

						RevisedSend.sendMessage(wiredSuccess);
					}
				}
			}
			else{

				String failureMessage = "Failure";
				WiredFailure wiredFailure = new WiredFailure(CommandType.SEARCH, wiredSearch.getSourceInfo(), failureMessage);

				RevisedSend.sendMessage(wiredFailure);
			}
		}
		else if(this.tempZone != null && isDestination(mappedCoordinate, this.tempZone)){

			if(this.tempFileNames.size() > 0){

				PeerInfo affectedPeer = new PeerInfo();
				affectedPeer.setHostName(this.getHostName());
				affectedPeer.setIpAddress(this.getIpAddress());
				affectedPeer.setNeighbours(this.getNeighbours());
				affectedPeer.setFileNames(this.tempFileNames);
				affectedPeer.setZone(this.tempZone);

				String successMessage = "Search successful";
				wiredSearch.getRouteInformation().addPeerToRoute(this.getHostName(), this.getIpAddress());
				WiredSuccess wiredSuccess = new WiredSuccess(CommandType.SEARCH, affectedPeer, wiredSearch.getSourceInfo(), successMessage, wiredSearch.getRouteInformation());
				RevisedSend.sendMessage(wiredSuccess);
			}
			else{

				String failureMessage = "Failure";
				WiredFailure wiredFailure = new WiredFailure(CommandType.SEARCH, wiredSearch.getSourceInfo(), failureMessage);

				RevisedSend.sendMessage(wiredFailure);
			}
		}
		else{

			NeighbourInfo neighbourToRoute;
			try{
				neighbourToRoute = findClosestNeighbour(mappedCoordinate, wiredSearch.getRouteInformation());
				wiredSearch.getRouteInformation().addPeerToRoute(this.getHostName(), this.getIpAddress());
				wiredSearch.setNeighbourToRoute(neighbourToRoute);

				RevisedSend.sendMessage(wiredSearch);
			}
			catch(Exception e){

				String failureMessage = "Failure";
				WiredFailure wiredFailure = new WiredFailure(CommandType.SEARCH, wiredSearch.getSourceInfo(), failureMessage);

				RevisedSend.sendMessage(wiredFailure);
			}
		}
	}

	/*
	 * function that is called by new node to join the network
	 */
	public synchronized void join(WiredJoin wiredJoin){

		/*
		 * check if current peer is bootstrap and whether neighbourToRoute information is null.
		 * if yes then we select random ipAddresses of some active peers and add them to wiredJoin
		 * 		We also set boolean isSendBackToSource to indicate that the information needs to go back to the new node
		 * else forward the WiredJoin object to neighbourToRoute
		 */
		try {


			/*
			 * executed when the WiredJoin is routed back from Bootstrap node to NewNode
			 * here numberOfHops = 0
			 */
			if(isBootstrap() && wiredJoin.getNumberOfHops() == 0){

				/*
				 * check if number of active peers is less than 10
				 * if not then send back a WiredFailure message indicating JOIN not possible
				 */
				if(Peer.peerLimit < 10){

					/*
					 * select some random ipAddress of active peers
					 * add them to wiredJoin
					 * set neighbourToRoute to newNode
					 */
					int numberOfActivePeers = Peer.activePeers.size();

					if(numberOfActivePeers == 1){

						wiredJoin.setActivePeersInfo(Peer.activePeers);
					}
					else{

						Map<String, InetAddress> someActivePeers = new HashMap<String, InetAddress>();
						int i = 0;
						for(Map.Entry<String, InetAddress> entry : Peer.activePeers.entrySet()){

							if(i < Math.ceil(numberOfActivePeers/2)){
								someActivePeers.put(entry.getKey(), entry.getValue());
							}
							else{
								break;
							}
							i++;
						}
						wiredJoin.setActivePeersInfo(someActivePeers);
					}

					wiredJoin.setNumberofHops(wiredJoin.getNumberOfHops()+1);
					RevisedSend.sendMessage(wiredJoin);
				}
				else{
					PeerInfo sourceInfo = new PeerInfo();
					sourceInfo.setHostName(wiredJoin.getSourceHostname());
					sourceInfo.setIpAddress(wiredJoin.getSourceIpAddress());
					String statusMessage = "Sorry! Peer limit of "+Peer.peerLimit+" has already been reached.";
					WiredFailure wiredFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
					RevisedSend.sendMessage(wiredFailure);
				}
			}
			/*
			 * executed when the WiredJoin is route from NewNode to Peer in the network
			 * here numberOfHops = 1
			 */
			else if(!wiredJoin.getActivePeersInfo().isEmpty() && wiredJoin.getNumberOfHops() == 1){

				/*
				 * select random point in space and add it to wiredJoin
				 * select a peer from active peers to route the join request
				 * set neighbourToRoute to peer selected 
				 * set isSendBackToSource to false
				 * send wiredJoin to peer
				 */
				Coordinate randomPoint = computeRandomPointInSpace();
				Set<String> activePeersKeySet = wiredJoin.getActivePeersInfo().keySet();
				Object[] keys = activePeersKeySet.toArray();
				int randomInt = (int)((Math.random()*10)%keys.length);
				String randomKey = (String)keys[randomInt];
				InetAddress randomIpAddress = wiredJoin.getActivePeersInfo().get(randomKey);

				wiredJoin.setRandomCoordinate(randomPoint);
				wiredJoin.setHostnameToRoute(randomKey);
				wiredJoin.setIpAddressToRoute(randomIpAddress);
				wiredJoin.setNumberofHops(wiredJoin.getNumberOfHops()+1);
				wiredJoin.setRouteInformation(this.getHostName(), this.getIpAddress());

				RevisedSend.sendMessage(wiredJoin);

			}
			/*
			 * executed when WiredJoin has come to an active Peer
			 * here numberOfHope >= 2
			 */
			else if(wiredJoin.getRandomCoordinate() != null && wiredJoin.getNumberOfHops() >= 2){

				/*
				 * check whether random point present in current peer's zone.
				 * if yes then,
				 * 		split zone
				 * 		determine neighbours of new node and add it to the list
				 * 		initialize routing table of new node
				 * 		update neighbours list of current node
				 * 		update routing table
				 * 		update current peer's zone
				 * 		update neighbours of the change
				 * 		notify Bootstrap node and send ipAddress and hostName of the new node
				 * else
				 * 		increment numberOfHops by 1
				 * 		find closest neighbour to random point and route the WiredJoin object to it 
				 */
				if(isDestination(wiredJoin.getRandomCoordinate(), this.getZone())){

					Zone[] zoneSplits = splitZone(this.getZone(), wiredJoin);
					Zone updatedPeerZone = zoneSplits[0];
					Zone newZoneOfNewNode = zoneSplits[1];
					
					ConcurrentHashMap<String, NeighbourInfo> updatedRoutingTable = new ConcurrentHashMap<String, NeighbourInfo>(10,0.75f,10);
					ConcurrentHashMap<String, NeighbourInfo> newRoutingTable = new ConcurrentHashMap<String, NeighbourInfo>(10,0.75f,10);
					//initializing updatedRoutingTable and newRoutingTable
					for(Map.Entry<String, NeighbourInfo> routingTableEntry : Peer.getInstance().routingTable.entrySet()){

						//checking whether neighbour of peer (now with updated zone)
						if(isNeighbour(updatedPeerZone, routingTableEntry.getValue().getZone())){

							updatedRoutingTable.put(routingTableEntry.getKey(), routingTableEntry.getValue());
						}
						//checking whether neighbour of new node
						if(isNeighbour(newZoneOfNewNode, routingTableEntry.getValue().getZone())){

							newRoutingTable.put(routingTableEntry.getKey(), routingTableEntry.getValue());
						}
					}

					/*
					 * creating WiredJoinUpdate object
					 * initializing it with the update information
					 * sending it to all the neighbours of current peer (spawning thread for each neighbour)
					 */
					for(NeighbourInfo neighbour : getNeighbours()){

						JoinUpdateNeighbours joinUpdateNeighbours = new JoinUpdateNeighbours(this.getHostName(), wiredJoin.getSourceHostname(), this.getIpAddress(), wiredJoin.getSourceIpAddress(), updatedPeerZone, newZoneOfNewNode, neighbour);
						RevisedSend.sendMessage(joinUpdateNeighbours);
					}

					/*
					 * creating JoinUpdateBootstrap object
					 * initializing it with the hostname and ipaddress of the new node
					 * sending it to the Bootstrap node to update it's list of active peers
					 */
					JoinUpdateBootstrap updateBootstrap = new JoinUpdateBootstrap(wiredJoin.getSourceHostname(), wiredJoin.getSourceIpAddress());
					RevisedSend.sendMessage(updateBootstrap);

					/*
					 * updating routing table for current Peer
					 * for each neighbour in the current routing table, check whether it is present in the updated routing table
					 * if yes then ignore
					 * else remove corresponding neighbour from the current routing table
					 */
					if(Peer.getInstance().routingTable.size() != 0){
						for(Map.Entry<String, NeighbourInfo> currentNeighbourEntry : Peer.getInstance().routingTable.entrySet()){

							if(!updatedRoutingTable.containsKey(currentNeighbourEntry.getKey())){

								Peer.getInstance().routingTable.remove(currentNeighbourEntry.getKey());
							}
						}
					}

					//adding new node to the current peer's routing table
					this.routingTable.put(wiredJoin.getSourceHostname(),new NeighbourInfo(newZoneOfNewNode, wiredJoin.getSourceIpAddress(), 49161, wiredJoin.getSourceHostname()));

					//adding peer to new node's routing table because it has now become a neighbour
					newRoutingTable.put(Peer.getInstance().getHostName(), new NeighbourInfo(Peer.getInstance().getZone(), Peer.getInstance().getIpAddress(), 49161, Peer.getInstance().getHostName()));

					//updating current peer's zone
					Peer.getInstance().setZone(updatedPeerZone);

					/*
					 * need to find all the files that need to be stored in the new node that just joined.
					 * we loop over the current peer's filename and then check if the mapped coordinate lies in the new node's zone.
					 * if yes then we add the file to the list of files to be stored at the new node.
					 */
					HashSet<String> filesToBeTransfered = new HashSet<String>();
					Coordinate mappingForFile;
					for(String file : this.fileNames){

						mappingForFile = Utils.mapKeyToCoordinate(file);
						if(isDestination(mappingForFile, newZoneOfNewNode)){

							//adding file to the new list
							filesToBeTransfered.add(file);
						}
					}
					//removing all the files, that were transfered, from the current list of filenames
					this.fileNames.removeAll(filesToBeTransfered);

					/*
					 * creating JoinConfirmation object
					 * initializing it with newZone and newRoutingTable
					 * sending it to the new node
					 */
					JoinConfirmation joinConfirmation = new JoinConfirmation(newZoneOfNewNode, newRoutingTable, wiredJoin.getSourceIpAddress(), wiredJoin.getSourceHostname(), Peer.numberOfSplits, filesToBeTransfered);
					RevisedSend.sendMessage(joinConfirmation);
				}
				else if(this.tempZone != null && isDestination(wiredJoin.getRandomCoordinate(), this.tempZone)){

					/*
					 * need to update the neighbours of the tempZone to update their routing tables
					 * need to update the bootstrap of the joining of the new node
					 * need to transfer the tempRoutingTable to the new joining node
					 * need to transfer the tempFiles to the new joining node
					 * set tempZone to null
					 * setTempRoutingTable to null
					 */
					TemporaryZoneReleaseUpdateNeighbours zoneReleaseUpdateNeighbours;
					for(Map.Entry<String, NeighbourInfo> tempRoutingEntry : this.tempRoutingTable.entrySet()){

						zoneReleaseUpdateNeighbours = new TemporaryZoneReleaseUpdateNeighbours(this.getHostName(), this.getIpAddress(), wiredJoin.getSourceHostname(), wiredJoin.getSourceIpAddress(), this.getTempZone(), tempRoutingEntry.getValue().getHostname(), tempRoutingEntry.getValue().getIpAddress());
						RevisedSend.sendMessage(zoneReleaseUpdateNeighbours);
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					JoinUpdateBootstrap joinUpdateBootstrap = new JoinUpdateBootstrap(wiredJoin.getSourceHostname(), wiredJoin.getSourceIpAddress());
					RevisedSend.sendMessage(joinUpdateBootstrap);

					JoinConfirmation joinConfirmation = new JoinConfirmation(this.tempZone, this.tempRoutingTable, wiredJoin.getSourceIpAddress(), wiredJoin.getSourceHostname(), Peer.numberOfSplits-1, this.tempFileNames);
					RevisedSend.sendMessage(joinConfirmation);

					this.tempZone = null;
					this.tempRoutingTable.clear();
					this.tempFileNames.clear();

				}
				else{

					wiredJoin.setNumberofHops(wiredJoin.getNumberOfHops()+1);
					NeighbourInfo neighbourToRoute;
					try {
						neighbourToRoute = findClosestNeighbour(wiredJoin.getRandomCoordinate(), wiredJoin.getRouteInformation());
						if(neighbourToRoute != null){

							wiredJoin.setHostnameToRoute(neighbourToRoute.getHostname());
							wiredJoin.setIpAddressToRoute(neighbourToRoute.getIpAddress());
							RevisedSend.sendMessage(wiredJoin);
						}
						else{

							PeerInfo sourceInfo = new PeerInfo();
							sourceInfo.setHostName(wiredJoin.getSourceHostname());
							sourceInfo.setIpAddress(wiredJoin.getSourceIpAddress());
							String failureMessage = "JOIN Failure";
							WiredFailure wiredFailure = new WiredFailure(CommandType.FAILURE, sourceInfo, failureMessage);

							RevisedSend.sendMessage(wiredFailure);
						}
					} catch (ClosestNeighbourUnavailableException e) {
						PeerInfo sourceInfo = new PeerInfo();
						sourceInfo.setHostName(wiredJoin.getSourceHostname());
						sourceInfo.setIpAddress(wiredJoin.getSourceIpAddress());
						String statusMessage = "JOIN Failure";
						WiredFailure wiredFailure = new WiredFailure(CommandType.JOIN, sourceInfo, statusMessage);
						RevisedSend.sendMessage(wiredFailure);
						e.printStackTrace();
					}
				}
			}
			/*
			 * executed when neither of the above 2 conditions satisfy
			 */
			else{

				/*
				 * create WiredFailure and send it back to the source
				 */
				PeerInfo sourceInfo = new PeerInfo();
				sourceInfo.setHostName(wiredJoin.getSourceHostname());
				sourceInfo.setIpAddress(wiredJoin.getSourceIpAddress());
				String failureMessage = "JOIN Failure";
				WiredFailure wiredFailure = new WiredFailure(CommandType.FAILURE, sourceInfo, failureMessage);

				RevisedSend.sendMessage(wiredFailure);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/*
	 * updating routing table for new node that has joined
	 * this is done for all the neighbours of the node that split its zone
	 */
	public void updateRoutingTableForNewNode(JoinUpdateNeighbours joinUpdateNeighbours) {

		NeighbourInfo neighbourInfo;
		/*
		 * check whether newZone is neighbour of current peer
		 * if yes then add it's information to the routing table
		 * check whether oldPeer's updated zone is neighbour of current peer
		 * if not then remove it from the routing table
		 */
		if(isNeighbour(this.zone, joinUpdateNeighbours.getNewNodeZone())){
			//checking if current peer's zone is neighbour of new node's zone
			
			neighbourInfo = new NeighbourInfo(joinUpdateNeighbours.getNewNodeZone(), 
					joinUpdateNeighbours.getNewNodeIpAddress(), 49161, joinUpdateNeighbours.getNewNodeHostname());
			this.routingTable.put(joinUpdateNeighbours.getNewNodeHostname(), neighbourInfo);
		}

		if(isNeighbour(this.zone, joinUpdateNeighbours.getActivePeerUpdatedZone())){
			//checking if current peer's zone is still neighbour of node that split its zone

			neighbourInfo = new NeighbourInfo(joinUpdateNeighbours.getActivePeerUpdatedZone(), 
					joinUpdateNeighbours.getActivePeerIpAddress(), 49161, joinUpdateNeighbours.getActivePeerHostname());
			
			this.routingTable.put(joinUpdateNeighbours.getActivePeerHostname(), neighbourInfo);
		}
		else if(this.routingTable.get(joinUpdateNeighbours.getActivePeerHostname()).getTempZone() != null && 
				!isNeighbour(this.zone, this.routingTable.get(joinUpdateNeighbours.getActivePeerHostname()).getTempZone())){
			
			this.routingTable.remove(joinUpdateNeighbours.getActivePeerHostname());
		}
		else if(this.tempZone != null && isNeighbour(this.tempZone, joinUpdateNeighbours.getNewNodeZone())){
			
			neighbourInfo = new NeighbourInfo(joinUpdateNeighbours.getNewNodeZone(), joinUpdateNeighbours.getNewNodeIpAddress(),
					49161, Peer.getBootstrapHostname());
			
			this.tempRoutingTable.put(joinUpdateNeighbours.getNewNodeHostname(), neighbourInfo);
		}
	}

	/*
	 * setting the temp zone information of the node that released the temp zone to null
	 * checking whether the released zone is a neighbour and if yes then adding the new node to the routing table
	 * this is done for all neighbours of the node that release a temporarily taken over zone to the new node
	 */
	public void updateTempZoneRelease(TemporaryZoneReleaseUpdateNeighbours temporaryZoneReleaseUpdateNeighbours){

		this.routingTable.get(temporaryZoneReleaseUpdateNeighbours.getReleasingHostname()).setTempZone(null);

		if(isNeighbour(this.getZone(), temporaryZoneReleaseUpdateNeighbours.getReleasedZone())){

			NeighbourInfo newNodeInfo = new NeighbourInfo(temporaryZoneReleaseUpdateNeighbours.getReleasedZone(), 
					temporaryZoneReleaseUpdateNeighbours.getNewNodeIpAddress(), 49161, Peer.getBootstrapHostname());
			this.routingTable.put(temporaryZoneReleaseUpdateNeighbours.getNewNodeHostname(), newNodeInfo);
		}
		else if(this.tempZone != null && isNeighbour(this.tempZone, temporaryZoneReleaseUpdateNeighbours.getReleasedZone())){

			NeighbourInfo newNodeInfo = new NeighbourInfo(temporaryZoneReleaseUpdateNeighbours.getReleasedZone(), 
					temporaryZoneReleaseUpdateNeighbours.getNewNodeIpAddress(), 49161, Peer.getBootstrapHostname());
			this.tempRoutingTable.put(temporaryZoneReleaseUpdateNeighbours.getNewNodeHostname(), newNodeInfo);
		}
	}

	/*
	 * updating active peers (this is executed only for the bootstrap node)
	 */
	public void updateActivePeers(JoinUpdateBootstrap joinUpdateBootstrap){

		if(Peer.activePeers == null){
			Peer.activePeers = new HashMap<String, InetAddress>();
		}
		Peer.activePeers.put(joinUpdateBootstrap.getNewHostname(), joinUpdateBootstrap.getNewIpAddress());
		Peer.peerLimit++;
	}

	/*
	 * initializing zone and routing table (this is executed only when a new node, having made a join request, gets a confirmation)
	 */
	public void initializeState(JoinConfirmation joinConfirmation){

		this.setZone(joinConfirmation.getZone());
		this.tempZone = null;
		this.routingTable = joinConfirmation.getRoutingTable();
		this.tempRoutingTable.clear();
		Peer.numberOfSplits = joinConfirmation.getNumberOfSplits();
		HashSet<String> filesTransfered;
		if((filesTransfered = joinConfirmation.getTransferedFiles()).size() > 0){
			this.fileNames.addAll(filesTransfered);
		}
		this.tempFileNames.clear();

		/*
		 * notifying user of join success
		 * notifying ipAddress, hostname, zone and neighbours to the user
		 */
		Utils.printToConsole("----------------------------------------------------------------------");
		Utils.printToConsole("JOIN SUCCESSUL!");
		Utils.printToConsole("IP of New Peer : "+joinConfirmation.getSourceIpAddress());
		Utils.printToConsole("Hostname of New Peer : "+joinConfirmation.getSourceHostName());
		Utils.printToConsole("Zone of New Peer : "+joinConfirmation.getZone());
		Utils.printToConsole("----------------------------------------------------------------------");
	}


	/*
	 * function that is called by node when leaving the network
	 * this function is called from the main() function.
	 */
	public void leave(){

		boolean isCompleteTakeOver = false;
		int i;
		boolean alongX = false;
		boolean alongY = false;

		if(this.tempZone != null){
			PeerInfo sourceInfo = new PeerInfo();
			sourceInfo.setHostName(this.hostname);
			sourceInfo.setIpAddress(this.IPaddress);
			sourceInfo.setNeighbours(this.getNeighbours());
			sourceInfo.setZone(this.zone);
			sourceInfo.setFileNames(this.fileNames);
			String statusMessage = "Sorry! Cannot leave the network. Taking over a temporary zone.";
			WiredFailure leaveFailure = new WiredFailure(CommandType.LEAVE, sourceInfo, statusMessage);
			Utils.printToConsole(leaveFailure.toString());
			
			possibleCommands.put(CommandType.INSERT, true);
			possibleCommands.put(CommandType.SEARCH, true);
			possibleCommands.put(CommandType.VIEW, true);
			possibleCommands.put(CommandType.JOIN, false);
			possibleCommands.put(CommandType.LEAVE, true);
		}
		else{

			/*
			 * loop over neighbours (routing table)
			 * 	check whether neighbour can takeover zone.
			 * 		if yes then,
			 * 			update bootstrap node (here the bootstrap node will remove entry corresponding to leaving node from its list of active peers)
			 * 			handover zone to neighbour
			 * 			update all the current neighbours about the takeover (provide information about the neighbour that is going to takeover)
			 * 			stop checking further
			 * If no neighbour can takeover zone then,
			 * 		update bootstrap node (here the bootstrap node will entry corresponding to leaving node from its list of active peers)
			 * 		select neighbour with the smallest zone and send takeover message to it.
			 * 		update all the current neighbours about the takeover (provide information about the neighbour that is going to takeover)
			 * update current neighbours to change the their routing table
			 */
			for(Map.Entry<String, NeighbourInfo> routingTableEntry : this.routingTable.entrySet()){

				if((i = canTakeOver(routingTableEntry.getValue())) > 0){

					isCompleteTakeOver = true;

					LeaveUpdateBootstrap leaveUpdateBootstrap = new LeaveUpdateBootstrap(this.getHostName(), this.getIpAddress());
					RevisedSend.sendMessage(leaveUpdateBootstrap);

					if(i == 1){
						alongX = true;
					}
					else{
						//i=2
						alongY = true;
					}
					WiredZoneTransfer wiredZoneTransfer = new WiredZoneTransfer(this.getZone(), this.fileNames, routingTableEntry.getValue(), 
							alongX, alongY, isCompleteTakeOver, this.routingTable, this.getHostName(), this.getIpAddress());
					RevisedSend.sendMessage(wiredZoneTransfer);

					/*
					 * updating all current neighbours to remove entry corresponding to this peer from their routing tables
					 */
					LeaveUpdateNeighbours leaveUpdateNeighbours;
					for(NeighbourInfo neighbour : getNeighbours()){

						if(!(routingTableEntry.getKey().equals(neighbour.getHostname()))){
							leaveUpdateNeighbours = new LeaveUpdateNeighbours(this.getHostName(), this.getIpAddress(), neighbour.getIpAddress(), neighbour.getHostname(), routingTableEntry.getValue());
							RevisedSend.sendMessage(leaveUpdateNeighbours);
						}

						try{
							Thread.sleep(500);
						}
						catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}

					break;
				}
			}

			/*
			 * if no neighbour is possible to takeover the zone then,
			 * 		 we need to find the neighbour with the smallest zone and then ask it temporarily takeover the zone 
			 */
			if(!isCompleteTakeOver){

				LeaveUpdateBootstrap leaveUpdateBootstrap = new LeaveUpdateBootstrap(this.getHostName(), this.getIpAddress());
				RevisedSend.sendMessage(leaveUpdateBootstrap);


				NeighbourInfo smallestZoneNeighbour = retrieveNeighbourWithSmallestZone();
				if(smallestZoneNeighbour == null){
					PeerInfo sourceInfo = new PeerInfo();
					sourceInfo.setHostName(this.getHostName());
					sourceInfo.setIpAddress(this.getIpAddress());
					sourceInfo.setZone(this.getZone());
					WiredFailure wiredFailure = new WiredFailure(CommandType.FAILURE, sourceInfo, "No Neighbours exist for take over.");
					Utils.printErrorMessage(wiredFailure.toString());
				}
				else{
					WiredZoneTransfer wiredZoneTransfer = new WiredZoneTransfer(this.getZone(), this.fileNames, smallestZoneNeighbour,
							false, false, isCompleteTakeOver, this.routingTable, this.getHostName(), this.getIpAddress());
					RevisedSend.sendMessage(wiredZoneTransfer);

					/*
					 * updating all current neighbours to remove entry corresponding to this peer from their routing tables
					 */
					LeaveUpdateNeighbours leaveUpdateNeighbours;
					for(NeighbourInfo neighbour : getNeighbours()){

						leaveUpdateNeighbours = new LeaveUpdateNeighbours(this.getHostName(), this.getIpAddress(),neighbour.getIpAddress(), neighbour.getHostname(), smallestZoneNeighbour);
						RevisedSend.sendMessage(leaveUpdateNeighbours);

						try{
							Thread.sleep(500);
						}
						catch(InterruptedException ie){
							ie.printStackTrace();
						}
					}
				}
			}
		}

	}

	/*
	 * takeover zone when a node leaves the CAN
	 */
	public synchronized void takeover(WiredZoneTransfer wiredZoneTransfer){

		Zone mergedZone;
		/*
		 * if complete takeover
		 * 		need to merge the zones
		 * 		need to update all the neighbours about change of state of the current peer
		 * 		update routing table entry to include union of neighbours
		 * 		send a wiredSuccess message
		 * else
		 * 		need to add new zone into the temporary zone
		 * 		need to update all the neighbours about the change of state of the current peer
		 * 		update the routing table entry to include union of neighbours
		 * 
		 */
		if(wiredZoneTransfer.isCompleteTakeOver()){

			//merging the zones
			mergedZone = mergeZones(this.getZone(), wiredZoneTransfer);
			this.setZone(mergedZone);

			/*
			 * updating current set of files names by adding all the files that were transfered from the leaving node.
			 */
			this.fileNames.addAll(wiredZoneTransfer.getFilesToBeTransfered());
			for(Map.Entry<String, NeighbourInfo> leavingNodeRoutingEntry : wiredZoneTransfer.getLeavingNodeRoutingTable().entrySet()){

				if(!(leavingNodeRoutingEntry.getKey().equals(this.getHostName()))){

					if(leavingNodeRoutingEntry.getKey().equals(wiredZoneTransfer.getSourceHostname())){
						this.routingTable.remove(leavingNodeRoutingEntry.getKey());
					}
					else{
						this.routingTable.put(leavingNodeRoutingEntry.getKey(), leavingNodeRoutingEntry.getValue());
					}
				}
			}

			this.routingTable.remove(wiredZoneTransfer.getSourceHostname());

			/*
			 * updating neighbours about the change of state of the current peer.
			 * we won't be updating the leaving node.
			 */
			for(NeighbourInfo neighbour : this.routingTable.values()){

				TakeoverUpdate takeoverUpdate = new TakeoverUpdate(this.getHostName(), this.getIpAddress(), neighbour.getHostname(), this.getZone(), true);
				RevisedSend.sendMessage(takeoverUpdate);
			}

			TakeoverConfirmation takeoverConfirmation = new TakeoverConfirmation(wiredZoneTransfer.getSourceHostname(), wiredZoneTransfer.getSourceIpAddress());
			RevisedSend.sendMessage(takeoverConfirmation);
		}
		else{

			//adding new zone to temporary zone. Not merging because merge into one complete zone is not possible.
			this.setTempZone(wiredZoneTransfer.getZoneToTakeover());

			//creating takeover update message and sending it to all the neighbours
			for(NeighbourInfo neighbour : getNeighbours()){

				if(!(this.getHostName().equals(neighbour.getHostname()))){

					TakeoverUpdate takeoverUpdate = new TakeoverUpdate(this.getHostName(), this.getIpAddress(), neighbour.getHostname(), this.getTempZone(), false);
					RevisedSend.sendMessage(takeoverUpdate);
				}
			}

			//creating takeover update message and sending it to all the neighbours of the temp zone
			for(NeighbourInfo neighbour : wiredZoneTransfer.getLeavingNodeRoutingTable().values()){

				if(!isNeighbour(this.zone, neighbour.getZone()) || (neighbour.getTempZone() != null && 
						!isNeighbour(this.zone, neighbour.getTempZone()))){

					TakeoverUpdate takeoverUpdate = new TakeoverUpdate(this.getHostName(), this.getIpAddress(), neighbour.getHostname(), this.tempZone, false);
					RevisedSend.sendMessage(takeoverUpdate);
				}
			}

			//updating adding information to the tempRoutingTable table entry to include new neighbours
			this.tempRoutingTable.putAll(wiredZoneTransfer.getLeavingNodeRoutingTable());

			//adding all the files names transfered to tempFileNames
			this.tempFileNames.addAll(wiredZoneTransfer.getFilesToBeTransfered());

			TakeoverConfirmation takeoverConfirmation = new TakeoverConfirmation(wiredZoneTransfer.getSourceHostname(), wiredZoneTransfer.getSourceIpAddress());
			RevisedSend.sendMessage(takeoverConfirmation);
		}
	}

	/*
	 * merging zones for complete takeover
	 */
	private Zone mergeZones(Zone currentZone, WiredZoneTransfer wiredZoneTransfer){

		Zone mergedZone = null;
		double startX = currentZone.getStartX();
		double startY = currentZone.getStartY();
		double endX = currentZone.getEndX();
		double endY = currentZone.getEndY();
		/*
		 * if zones abut along x-axis
		 * 		change startY to smallest startY
		 * 		change endY to largest endY
		 * else if zones abut along y-axis
		 * 		change startX to smallest startX
		 * 		change endX to largest endX
		 * else
		 * 		create WiredFailure object and send it back to the leaving node indicating failure of takeover
		 * 		return null
		 */
		if(wiredZoneTransfer.isAlongX()){

			if(currentZone.getStartY() < wiredZoneTransfer.getZoneToTakeover().getStartY()){

				endY = wiredZoneTransfer.getZoneToTakeover().getEndY();
				mergedZone = new Zone(startX, startY, endX, endY);
				return mergedZone;
			}
			else{

				startY = wiredZoneTransfer.getZoneToTakeover().getStartY();
				mergedZone = new Zone(startX, startY, endX, endY);
				return mergedZone;
			}
		}
		else if(wiredZoneTransfer.isAlongY()){

			if(currentZone.getStartX() < wiredZoneTransfer.getZoneToTakeover().getStartX()){

				endX = wiredZoneTransfer.getZoneToTakeover().getEndX();
				mergedZone = new Zone(startX, startY, endX, endY);
				return mergedZone;
			}
			else{

				startX = wiredZoneTransfer.getZoneToTakeover().getStartX();
				mergedZone = new Zone(startX, startY, endX, endY);
				return mergedZone;
			}
		}
		else{

			PeerInfo sourceInfo = new PeerInfo();
			sourceInfo.setHostName(this.getHostName());
			sourceInfo.setIpAddress(this.getIpAddress());
			String statusMessage = "Takeover Failed.";
			WiredFailure wiredFailure = new WiredFailure(CommandType.FAILURE, sourceInfo, statusMessage);
			RevisedSend.sendMessage(wiredFailure);
			return null;
		}
	}

	/*
	 * function to remove entry pertaining to the leaving node from the list of active peers
	 * this is executed only for the bootstrap node
	 */
	public void removeActivePeerEntry(LeaveUpdateBootstrap leaveUpdateBootstrap){

		try {
			if(Peer.isBootstrap() && Peer.activePeers.containsKey(leaveUpdateBootstrap.getHostname())){

				Peer.activePeers.remove(leaveUpdateBootstrap.getHostname());
			}
			else{

				/*
				 * create WiredFailure object and send it back to the leaving node
				 */
				PeerInfo sourceInfo = new PeerInfo();
				sourceInfo.setHostName(leaveUpdateBootstrap.getHostname());
				sourceInfo.setIpAddress(leaveUpdateBootstrap.getIpAddress());
				String statusMessage = "Cannot leave the network.\n Please try again";
				WiredFailure wiredFailure = new WiredFailure(CommandType.FAILURE, sourceInfo, statusMessage);
				RevisedSend.sendMessage(wiredFailure);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	/*
	 * function to remove entry pertaining to the leaving node from the routing table
	 * function also adds neighbour taking over into the routingTable if not present. (If present that means it has already updated information regarding leave and takeover)
	 * this is executed only for the neighbours of the leaving node (the bootstrap node could happen to be a neighbour)
	 */
	public void removeNeighbourFromRoutingTable(LeaveUpdateNeighbours leaveUpdateNeighbours){

		if(this.routingTable.containsKey(leaveUpdateNeighbours.getLeavingHostname())){
			this.routingTable.remove(leaveUpdateNeighbours.getLeavingHostname());
		}
		if(!this.routingTable.containsKey(leaveUpdateNeighbours.getNeighbourTakingOver().getHostname())){
			this.routingTable.put(leaveUpdateNeighbours.getNeighbourTakingOver().getHostname(), leaveUpdateNeighbours.getNeighbourTakingOver());
		}
	}

	/*
	 * function to update change of state of neighbour (after taking over)
	 */
	public void updateNeighbourState(TakeoverUpdate takeoverUpdate){

		if(this.routingTable.containsKey(takeoverUpdate.getUpdatedHostname())){

			if(takeoverUpdate.isCompleteTakeover()){

				this.routingTable.get(takeoverUpdate.getUpdatedHostname()).setZone(takeoverUpdate.getUpdatedZone());
			}
			else{

				this.routingTable.get(takeoverUpdate.getUpdatedHostname()).setTempZone(takeoverUpdate.getUpdatedZone());
			}
		}
		else if(isNeighbour(this.zone, takeoverUpdate.getUpdatedZone())){
			//means that the node is the neighbour of the temporarily taken over zone
			if(!this.getHostName().equals(takeoverUpdate.getUpdatedHostname())){
				this.routingTable.put(takeoverUpdate.getUpdatedHostname(), new NeighbourInfo(takeoverUpdate.getUpdatedZone(), 
						takeoverUpdate.getUpdatedIpAddress(), 49161, Peer.BOOTSTRAP_HOSTNAME));
			}
		}
		else if( (this.tempZone != null && isNeighbour(this.tempZone, 
				takeoverUpdate.getUpdatedZone()))){

			this.tempRoutingTable.put(takeoverUpdate.getUpdatedHostname(), new NeighbourInfo(takeoverUpdate.getUpdatedZone(), takeoverUpdate.getUpdatedIpAddress(), 49161, Peer.getBootstrapHostname()));
		}
		else{

			//create failure message and send it back to the leaving node
			String statusMessage = "Could not update routing table.\nHostname : "+this.getHostName()+"\nIpAddress : "+this.getIpAddress();
			PeerInfo sourceInfo = new PeerInfo();
			sourceInfo.setHostName(takeoverUpdate.getUpdatedHostname());
			sourceInfo.setIpAddress(takeoverUpdate.getUpdatedIpAddress());
			WiredFailure wiredFailure = new WiredFailure(CommandType.FAILURE, sourceInfo, statusMessage);
			RevisedSend.sendMessage(wiredFailure);
		}
	}

	/*
	 * check whether node can takeover
	 */
	private int canTakeOver(NeighbourInfo neighbour){

		Zone currentPeerZone = getZone();
		Zone zoneOfNeighbourThatWillTakeOver = neighbour.getZone();
		/*
		 * check whether the zones abut along one side. If they do then a takeover is possible
		 * first check whether they abut along x-axis
		 * 		if yes then return 1
		 * then check whether they abut along y-axis
		 * 		if yes then return 2
		 * if both are not possible then return 0
		 */

		if(currentPeerZone.getStartX() == zoneOfNeighbourThatWillTakeOver.getStartX() && currentPeerZone.getEndX() == zoneOfNeighbourThatWillTakeOver.getEndX()){

			return 1;
		}
		else if(currentPeerZone.getStartY() == zoneOfNeighbourThatWillTakeOver.getStartY() && currentPeerZone.getEndY() == zoneOfNeighbourThatWillTakeOver.getEndY()){

			return 2;
		}

		return 0;
	}

	/*
	 * function to perform de-initialization of information and exit of node from the network
	 */
	public void deinitializeState(){

		StringBuilder builder = new StringBuilder("");
		builder.append("------------------------------------------------------------------\n");
		builder.append("Leave Successful\n");
		builder.append("-----------------\n");
		builder.append("Hostname of node that left : "+this.getHostName()+"\n");
		builder.append("Ip address of node that left : "+this.getIpAddress()+"\n");
		builder.append("------------------------------------------------------------------\n\n");

		Utils.printToConsole(builder.toString());

		this.routingTable.clear();
		this.tempRoutingTable.clear();
		this.setZone(null);
		this.setTempZone(null);
		this.fileNames.clear();
		this.tempFileNames.clear();
		Peer.numberOfSplits = 0;

		return;
	}

	/*
	 * function to retrieve neighbour with smallest zone
	 * here we are retrieving neighbour who has not temporarily taken over any zone
	 */
	private NeighbourInfo retrieveNeighbourWithSmallestZone(){

		List<NeighbourInfo> neighbours = getNeighbours();
		if(neighbours.size() == 0){
			return null;
		}
		double area = computeZoneArea(neighbours.get(0).getZone());
		double temp;
		NeighbourInfo neighbourWithSmallestZone = neighbours.get(0);

		for(int i = 1;i < neighbours.size();i++){

			if(neighbours.get(i).getTempZone() == null){

				temp = computeZoneArea(neighbours.get(i).getZone());
				if(temp < area){
					area = temp;
					neighbourWithSmallestZone = neighbours.get(i);
				}
			}
		}
		return neighbourWithSmallestZone;
	}

	/*
	 * function to compute area of zone
	 */
	private double computeZoneArea(Zone zone){

		double length = Math.abs(zone.getEndX()-zone.getStartX());
		double breadth = Math.abs(zone.getEndY()-zone.getStartY());

		double area = length*breadth;
		return area;
	}

	private boolean isNeighbour(Zone peerZone, Zone neighbourZone){

		/*
		 * checking whether neighbours abuts in more than 1 dimension
		 * if yes then return false
		 * else return true
		 */
		double peerStartX = peerZone.getStartX();
		double peerStartY = peerZone.getStartY();
		double peerEndX = peerZone.getEndX();
		double peerEndY = peerZone.getEndY();

		double neighbourStartX = neighbourZone.getStartX();
		double neighbourStartY = neighbourZone.getStartY();
		double neighbourEndX = neighbourZone.getEndX();
		double neighbourEndY = neighbourZone.getEndY();

		//checking top right corner abut
		if(peerEndX == neighbourStartX && peerEndY == neighbourStartY){

			if(peerEndX == neighbourStartY && peerEndY == neighbourStartX){

				return false;
			}
		}
		//checking top left corner abut
		if(peerStartX == neighbourEndX && peerEndY == neighbourEndX){

			if(peerStartX == neighbourStartY && peerEndY == neighbourStartY){

				return false;
			}
		}
		//checking for bottom left corner abut
		if(peerStartX == neighbourEndX && peerStartY == neighbourEndX){

			if(peerStartX == neighbourEndY && peerStartY == neighbourEndY){

				return false;
			}
		}
		//checking for bottom right corner abut
		if(peerEndX == neighbourStartX && peerStartY == neighbourStartX){

			if(peerEndX == neighbourEndY && peerStartY == neighbourEndY){

				return false;
			}
		}
		/*
		 * checking whether the zones abut along the y-axis or the x-axis
		 * trying out all possibilities
		 */
		if(peerStartY == neighbourStartY){

			if(peerEndX == neighbourStartX){
				return true;
			}
			else if(peerStartX == neighbourEndX){
				return true;
			}
		}
		if(peerStartX == neighbourStartX){

			if(peerStartY == neighbourEndY){
				return true;
			}
			else if(peerEndY == neighbourStartY){
				return true;
			}
		}
		if(peerEndX == neighbourEndX){

			if(peerEndY == neighbourStartY){
				return true;
			}
			else if(neighbourEndY == peerStartY){
				return true;
			}
		}
		if(peerEndY == neighbourEndY){

			if(peerEndX == neighbourStartX){
				return true;
			}
			else if(neighbourEndX == peerStartX){
				return true;
			}
		}
		if(peerEndX == neighbourStartX || peerStartX == neighbourEndX){
			if(peerStartY >= neighbourStartY && peerEndY <= neighbourEndY){
				return true;
			}
			else if(peerStartY <= neighbourStartY && peerEndY >= neighbourEndY){
				return true;
			}
		}
		if(peerStartY == neighbourEndY || peerEndY == neighbourStartY){
			if(peerStartX >= neighbourStartX && peerEndX <= neighbourEndX){
				return true;
			}
			else if(peerStartX <= neighbourStartX && peerEndX >= neighbourEndX){
				return true;
			}
		}
		//if here then it is not a neighbour
		return false;
	}

	private Coordinate computeRandomPointInSpace(){

		double x,y;
		Coordinate randomCoordinate;

		x = new BigDecimal(Math.random()*10).setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
		y = new BigDecimal(Math.random()*10).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

		randomCoordinate = new Coordinate(x, y);

		return randomCoordinate;
	}

	public void retrieveActivePeers(WiredViewActivePeersRequest activePeersRequest){

		activePeersRequest.setActivePeers(Peer.activePeers);
		RevisedSend.sendMessage(activePeersRequest);
	}

	public void forwardWiredView(WiredViewActivePeersRequest activePeersRequest){

		RevisedReceive.setTotalViewsRequired(activePeersRequest.getActivePeers().size());

		for(String peerHostname : activePeersRequest.getActivePeers().keySet()){

			if(!(this.getHostName().equals(peerHostname))){
				WiredView wiredView = new WiredView(this.getHostName(), this.getIpAddress(), peerHostname, ViewCategory.MULTI);
				RevisedSend.sendMessage(wiredView);
			}
			try{
				Thread.sleep(50);
			}
			catch(InterruptedException ie){
				ie.printStackTrace();
			}
		}
	}

	public void retrievePeerInformation(WiredView view){

		view.setPeerInformation(this.toString());
		view.setHostnameToRoute(view.getSourceHostname());
		RevisedSend.sendMessage(view);
	}

	public void view(String hostname){

		if(hostname == null){

			WiredViewActivePeersRequest activePeersRequest = new WiredViewActivePeersRequest();
			activePeersRequest.setSourceHostname(this.getHostName());
			activePeersRequest.setSourceIpAddress(this.getIpAddress());

			try {
				if(isBootstrap()){

					activePeersRequest.setActivePeers(Peer.activePeers);
					forwardWiredView(activePeersRequest);
				}
				else{
					RevisedSend.sendMessage(activePeersRequest);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}

		else if(hostname.equals(this.getHostName())){
			Utils.printToConsole(this.toString());
		}
		else{

			WiredView wiredView = new WiredView(this.getHostName(), this.getIpAddress(), hostname, ViewCategory.SINGLE);
			RevisedSend.sendMessage(wiredView);
		}
	}

	/*
	 * splitting the zone for new node.
	 */
	private Zone[] splitZone(Zone peerZone, WiredJoin wiredJoin){

		Coordinate joiningCoordinate = wiredJoin.getRandomCoordinate();

		double startX, startY, endX, endY;
		Zone[] zoneHalvesArray = new Zone[2];
		Zone newZone;
		/*
		 * if numberOfSplits is even then split vertically (along the y-axis)
		 * else split horizontally (along the x-axis)
		 */
		if(Peer.numberOfSplits % 2 == 0){

			startX = peerZone.getStartX();
			startY = peerZone.getStartY();
			endX = peerZone.getEndX();
			endY = peerZone.getEndY();

			/*
			 * check which half of peerZone joiningCoordinate is present in (left half or right half)
			 */
			if(joiningCoordinate.getXCoord() >= ((endX - startX)/2)){

				/*
				 * creating zone for right half of peerZone
				 */
				double newStartX = startX+((endX - startX)/2);

				newZone = new Zone(newStartX, startY, endX, endY);

				/*
				 * changing the endX of peerZone to startX or new zone
				 * assigning endX to peerZone.endX
				 */
				endX = newStartX;
				peerZone.setEndX(endX);

			}
			else{

				/*
				 * creating zone for left half of peerZone
				 */
				double newEndX = startX+((endX - startX)/2);

				newZone = new Zone(startX,startY,newEndX,endY);

				/*
				 * changing the startX of peerZone to endX of new zone
				 * assigning startX to peerZone.startX
				 */
				startX = newEndX;
				peerZone.setStartX(startX);

			}

			Peer.numberOfSplits++;

		}
		else{

			startX = peerZone.getStartX();
			startY = peerZone.getStartY();
			endX = peerZone.getEndX();
			endY = peerZone.getEndY();

			/*
			 * checking which half of the peerZone joiningCoordinate is present in (bottom half or upper half)
			 */
			if(joiningCoordinate.getYCoord() >= (endY - startY)/2){

				/*
				 * creating zone for top half of peerZone
				 */
				double newStartY = startY+((endY - startY)/2);

				newZone = new Zone(startX, newStartY, endX, endY);

				/*
				 * changing the endY of peerZone to endY of new zone
				 * assigning endY to peerZone.endY
				 */
				endY = newStartY;
				peerZone.setEndY(endY);
			}
			else{

				/*
				 * creating zone for bottom half of peerZone
				 */
				double newEndY = startY+((endY - startY)/2);

				newZone = new Zone(startX, startY, endX, newEndY);

				/*
				 * changing the startY of peerZone to endY of new zone
				 * assigning startY to peerZone.startY
				 */
				startY = newEndY;
				peerZone.setStartY(startY);

			}

			//if current node is the bootstrap then increment the number of splits
			Peer.numberOfSplits++;
		}

		/*
		 * storing the peerZone at location i=0 and storing newZone at location i=1
		 * returning zoneHalvesArray
		 */
		zoneHalvesArray[0] = peerZone;
		zoneHalvesArray[1] = newZone;

		return zoneHalvesArray;

	}

	/*
	 * If one of the neighbours' zone/tempZone contains  the coordinate then return that neighbour
	 * Else
	 * 	return the closest neighbor to route
	 */
	public NeighbourInfo findClosestNeighbour(Coordinate destinationCoordinate, RouteInformation routeInformation) throws ClosestNeighbourUnavailableException{
		NeighbourInfo neighbourInfo = null;
		double minDist = -999999;
		double dist;
		Zone tempZone;


		try {
			for(Map.Entry<String, NeighbourInfo> routinTableEntry : routingTable.entrySet()){

				if(!routeInformation.getRoute().containsKey(routinTableEntry.getKey())){

					//checking if neighbour's zone contains the coordinates
					if(isDestination(destinationCoordinate, routinTableEntry.getValue().getZone())){
						return routinTableEntry.getValue();
					}
					else if((tempZone = routinTableEntry.getValue().getTempZone()) != null){
						if(isDestination(destinationCoordinate, tempZone)){
							return routinTableEntry.getValue();
						}
					}

					dist = Utils.computeDistance(routinTableEntry.getValue().getZone(), destinationCoordinate);
					if(minDist == -999999){

						minDist = dist;
						neighbourInfo = routinTableEntry.getValue();
					}
					else if(dist < minDist){

						minDist = dist;
						neighbourInfo = routinTableEntry.getValue();
					}

					if(routinTableEntry.getValue().getTempZone() != null){

						dist = Utils.computeDistance(routinTableEntry.getValue().getTempZone(), destinationCoordinate);
						if(dist < minDist){
							minDist = dist;
							neighbourInfo = routinTableEntry.getValue();
						}
					}
				}
			}

			if(!neighbourInfo.hasContents())
				throw new ClosestNeighbourUnavailableException();

		} catch (ClosestNeighbourUnavailableException closestNeighbourUnavailableException) {

			throw closestNeighbourUnavailableException;
		}

		return neighbourInfo;

	}

	private InetAddress getIpAddress(){

		try {
			return InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			getIpAddress();
		}

		return null;
	}

	/*
	 * checks whether the destination has been reached
	 */
	public synchronized boolean isDestination(Coordinate destCoord, Zone zone){

		/*
		 * checking whether the x coordinate of destCoord lies within the Peer's zone
		 * if destCoord on the boundaries of the entire coordinate space then we need to take the boundary into the zone
		 * else
		 * 	take value > startX (for along y)
		 * 	take value > startY (for along x)
		 */
		if(destCoord.getXCoord() == Peer.LOWER_BOUND_X){

			if(destCoord.getYCoord() == Peer.LOWER_BOUND_Y){
				//checking for (0,0)

				if(zone.getStartX() == destCoord.getXCoord()){

					//checking whether the y coordinate of destCoord lies within the Peer's zone
					if(zone.getStartY() == destCoord.getYCoord()){

						return true;
					}
					else{
						return false;
					}
				}
			}
			else if(destCoord.getYCoord() == Peer.UPPER_BOUND_Y){
				//checking for (0,10)

				if(zone.getStartX() == destCoord.getXCoord()){

					//checking whether the y coordinate of destCoord lies within the Peer's zone
					if(destCoord.getYCoord() == zone.getEndY()){

						return true;
					}
					else{
						return false;
					}
				}
			}
			else{
				//checking for (0, (LOWER_BOUND_Y,UPPER_BOUND_Y))
				if(zone.getStartX() == destCoord.getXCoord()){

					//checking whether the y coordinate of destCoord lies within the Peer's zone
					if(zone.getStartY() < destCoord.getYCoord() && destCoord.getYCoord() < zone.getEndY()){

						return true;
					}
					else{
						return false;
					}
				}
			}
		}
		if(destCoord.getXCoord() == Peer.UPPER_BOUND_X){

			if(destCoord.getYCoord() == Peer.LOWER_BOUND_Y){
				//checking for (10,0)
				if(zone.getEndX() == destCoord.getXCoord()){

					//checking whether the y coordinate of destCoord lies within the Peer.s zone
					if(zone.getStartY() == destCoord.getYCoord()){

						return true;
					}
					else{
						return false;
					}
				}
			}
			else if(destCoord.getYCoord() == Peer.UPPER_BOUND_Y){
				//checking for (10,10)

				if(zone.getEndX() == destCoord.getXCoord()){

					//checking whether the y coordinate of destCoord lies within the Peer's zone
					if(zone.getEndY() == destCoord.getYCoord()){

						return true;
					}
					else{
						return false;
					}
				}
			}
			else{
				//means that coordinate is in (10, (LOWER_BOUND_Y,UPPER_BOUND_Y))

				if(zone.getEndX() == destCoord.getXCoord()){

					//checking whether the y coordinate of destCoord lies within the Peer's zone
					if(zone.getStartY() < destCoord.getYCoord() && destCoord.getYCoord() < zone.getEndY()){

						return true;
					}
					else{
						return false;
					}
				}
			}
		}
		if(destCoord.getYCoord() == Peer.LOWER_BOUND_Y){
			//here x coordinate is in (LOWER_BOUND_X, UPPER_BOUND_X)

			if(zone.getStartY() == destCoord.getYCoord()){

				if(zone.getStartX() < destCoord.getXCoord() && destCoord.getXCoord() < zone.getEndX()){

					return true;
				}
				else{
					return false;
				}
			}
		}
		if(destCoord.getYCoord() == Peer.UPPER_BOUND_Y){
			//here x coordinate is in (LOWER_BOUND_X, UPPER_BOUND_X)

			if(zone.getEndY() == destCoord.getYCoord()){

				if(zone.getStartX() < destCoord.getXCoord() && destCoord.getXCoord() < zone.getEndX()){

					return true;
				}
				else{
					return false;
				}
			}
		}
		if(zone.getStartX() < destCoord.getXCoord() && destCoord.getXCoord() <= zone.getEndX()){
			//means that the x coordinate and the y coordinate are in ((LOWER_BOUND_X,UPPER_BOUND_X) , (LOWER_BOUND_Y,UPPER_BOUND_Y))

			//checking whether the y coordinate of destCoord lies within the Peer's zone
			if(zone.getStartY() < destCoord.getYCoord() && destCoord.getYCoord() <= zone.getEndY()){

				return true;
			}
			else{
				return false;
			}
		}

		//destCoord does not lie within Peer's zone and hence the message needs to be further routed to another peer
		return false;
	}

	public List<NeighbourInfo> getNeighbours(){

		List<NeighbourInfo> neighbours = new ArrayList<NeighbourInfo>();
		for(Map.Entry<String, NeighbourInfo> entry : this.routingTable.entrySet()){
			neighbours.add(entry.getValue());
		}
		if(this.tempRoutingTable.size() > 0){
			for(Map.Entry<String, NeighbourInfo> tempEntry : this.tempRoutingTable.entrySet()){
				if(!this.routingTable.containsKey(tempEntry.getKey())){
					neighbours.add(tempEntry.getValue());
				}
			}
		}

		return neighbours;
	}

	public InetAddress getIPaddress() {
		return IPaddress;
	}

	public void setIPaddress(InetAddress iPaddress) {
		IPaddress = iPaddress;
	}

	public String getHostName() {
		return hostname;
	}

	public void setHostName(String identifier) {
		this.hostname = identifier;
	}

	public Zone getZone() {
		return zone;
	}

	public void setZone(Zone zone) {
		this.zone = zone;
	}

	public Zone getTempZone(){

		return this.tempZone;
	}

	public void setTempZone(Zone zone){

		this.tempZone = zone;
	}

	public HashSet<String> getFileNames() {
		return fileNames;
	}

	public static String getBootstrapHostname(){
		return Peer.BOOTSTRAP_HOSTNAME;
	}

	public static InetAddress getBootstrapIp() {
		return bootstrapIp;
	}

	public static void setBootstrapIp() throws UnknownHostException {

		InetAddress bootstrapAddress = InetAddress.getByName(Peer.BOOTSTRAP_HOSTNAME);
		Peer.bootstrapIp = bootstrapAddress;
	}

	public static boolean isBootstrap() throws UnknownHostException{

		InetAddress localHostAddress = InetAddress.getLocalHost();
		String localHostName = localHostAddress.getHostName();

		if(localHostName.equals(Peer.BOOTSTRAP_HOSTNAME)){
			return true;
		}
		else{
			return false;
		}

	}

	public String toString(){

		StringBuilder builder = new StringBuilder("");
		builder.append("Hostname : "+this.getHostName()+"\n");
		builder.append("------------------------------------------------------------------\n");
		builder.append("Ip Address : "+this.getIpAddress().getHostAddress()+"\n");
		builder.append("Zone : "+this.getZone()+"\n");
		builder.append("Temp Zone : "+this.getTempZone()+"\n");
		builder.append("Files : "+this.fileNames+"\n");
		builder.append("Temp files : "+this.tempFileNames+"\n");
		builder.append("Neighbours : ");
		for(NeighbourInfo neighbour : this.getNeighbours()){
			builder.append(neighbour.getHostname()+", ");
		}
		builder.append("\n");
		builder.append("------------------------------------------------------------------\n");

		return builder.toString();
	}

	public static void main(String[] args) {

		possibleCommands.put(CommandType.INSERT, false);
		possibleCommands.put(CommandType.SEARCH, false);
		possibleCommands.put(CommandType.JOIN, true);
		possibleCommands.put(CommandType.LEAVE, false);
		possibleCommands.put(CommandType.VIEW, true);

		formats.put(CommandType.INSERT, "INSERT filename");
		formats.put(CommandType.SEARCH, "SEARCH filename");
		formats.put(CommandType.JOIN, "JOIN");
		formats.put(CommandType.LEAVE, "LEAVE");
		formats.put(CommandType.VIEW, "VIEW [hostname]");

		try {

			boolean isBootstrap = isBootstrap();
			if(isBootstrap){

				Peer bootstrap = Peer.getInstance();
				/*
				 * Setting bootstrap zone
				 * Here lowX = 0, lowY = 0, highX = 10, highY = 10
				 */
				Zone bootstrapZone = new Zone(0, 0, 10, 10);
				bootstrap.setZone(bootstrapZone);

				//setting active peers
				Peer.activePeers = new HashMap<String, InetAddress>();
				Peer.activePeers.put(Peer.BOOTSTRAP_HOSTNAME, Peer.bootstrapIp);

				//setting number of splits to 0
				Peer.numberOfSplits = 0;

				//disabling JOIN command
				possibleCommands.put(CommandType.JOIN, false);
				possibleCommands.put(CommandType.INSERT,true);
				possibleCommands.put(CommandType.SEARCH,true);
				possibleCommands.put(CommandType.LEAVE, false);

				Utils.printToConsole("Bootstrap loaded and Initialized.");

			}


		} catch (UnknownHostException e) {

			Utils.printToConsole("Couldn't load the bootstrap node. Try again.");
		}

		/*
		 * Spawning  thread to listen on a port
		 */
		RevisedReceive.startServer(49161);

		Scanner scanner = new Scanner(System.in);
		Peer peerInstance;
		try {
			peerInstance = Peer.getInstance();
			peerInstance.setHostName(InetAddress.getLocalHost().getHostName());
			peerInstance.setIPaddress(InetAddress.getLocalHost());

			//setting bootstrap ip
			Peer.bootstrapIp = InetAddress.getByName(Peer.BOOTSTRAP_HOSTNAME);

			while(true){

				System.out.println("Please provide a command. The possible commands are :");
				for(CommandType command : possibleCommands.keySet()){

					if(possibleCommands.get(command)){
						System.out.println(command+" -- "+formats.get(command));
					}
				}

				String[] input = scanner.nextLine().split(" ");
				switch (input[0].toLowerCase()) {
				case "insert":
					if(possibleCommands.get(CommandType.INSERT) == false){

						Utils.printToConsole("Illegal command");
					}
					else{
						if(input.length != 2){
							Utils.printErrorMessage("Wrong format on INSERT command.");
							Utils.printToConsole("Correct format : INSERT "+formats.get(CommandType.INSERT));
						}
						else{
							String filename = input[1];
							WiredInsert wiredInsert = new WiredInsert(CommandType.INSERT, filename, null, null, new RouteInformation());
							peerInstance.insert(wiredInsert);
						}
					}
					Thread.sleep(500);
					break;
				case "search":
					if(possibleCommands.get(CommandType.SEARCH) == false){

						Utils.printToConsole("Illegal command.");
					}
					else{
						if(input.length != 2){
							Utils.printErrorMessage("Wrong format for SEARCH command.");
							Utils.printToConsole("Correct format : SEARCH "+formats.get(CommandType.SEARCH));
						}
						else{
							String filename = input[1];
							WiredSearch wiredSearch = new WiredSearch(CommandType.SEARCH, filename , null, null, new RouteInformation());
							peerInstance.search(wiredSearch);
						}
					}
					Thread.sleep(500);
					break;
				case "join":
					if(possibleCommands.get(CommandType.JOIN) == false){

						Utils.printToConsole("Illegal command");
					}
					else{
						if(input.length > 1){
							Utils.printErrorMessage("Wrong format for JOIN command");
							Utils.printToConsole("Correct format : "+formats.get(CommandType.JOIN));
						}
						else{

							possibleCommands.put(CommandType.INSERT, true);
							possibleCommands.put(CommandType.SEARCH, true);
							possibleCommands.put(CommandType.VIEW, true);
							possibleCommands.put(CommandType.JOIN, false);
							possibleCommands.put(CommandType.LEAVE, true);

							WiredJoin wiredJoin = new WiredJoin(peerInstance.getHostName(), peerInstance.getIpAddress(), Peer.getBootstrapHostname(), Peer.getBootstrapIp(), new RouteInformation());
							RevisedSend.sendMessage(wiredJoin);

						}
					}
					Thread.sleep(500);
					break;
				case "leave":
					if(possibleCommands.get(CommandType.LEAVE) == false){

						Utils.printToConsole("Illegal command");
					}
					else{
						if(input.length > 1){
							Utils.printErrorMessage("Wrong format for LEAVE command");
							Utils.printToConsole("Correct format : "+formats.get(CommandType.LEAVE));
						}
						else{
							if(peerInstance.getTempZone() == null){
								possibleCommands.put(CommandType.INSERT, false);
								possibleCommands.put(CommandType.SEARCH, false);
								possibleCommands.put(CommandType.VIEW, true);
								possibleCommands.put(CommandType.JOIN, true);
								possibleCommands.put(CommandType.LEAVE, false);
								peerInstance.leave();
							}
							else{
								Utils.printErrorMessage("Sorry! Cannot leave the network due to temporary take over of another zone.");
							}
						}
					}

					Thread.sleep(500);
					break;
				case "view":
					if(possibleCommands.get(CommandType.VIEW) == false){

						Utils.printToConsole("Illegal command");
					}
					else{
						if(input.length == 2){
							peerInstance.view(input[1].toString());
						}
						else if(input.length == 1){
							peerInstance.view(null);
						}
						else{
							Utils.printErrorMessage("Wrong format for VIEW command");
							Utils.printToConsole("Correct format : "+formats.get(CommandType.VIEW));
						}
					}
					Thread.sleep(500);
					break;


				default:
					Utils.printErrorMessage("Please enter a valid command.");
				}

			}
		} catch (UnknownHostException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
