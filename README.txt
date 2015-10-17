Name : Pradyumna Kaushik
BNumber : B00594994
Email : pkaushi1@binghamton.edu


Programming language
--------------------
Java


How to Compile the source code
------------------------------
1. Unzip the .tar.gz file in any directory using the command

	tar -xvzf pkaushi1-project1.tar.gz

2. Type 'make' and it will create a pkaushi1-project1.jar in the current directory. 

3. I have considered the Bootstrap node to be remote07.cs.binghamton.edu. So run the program first on remote07.cs.binghamton.edu using the following command

	java -cp pkaushi1-project1.jar com.CAN.Nodes.Peer

4. The Bootstrap node should now be up and running. Now go to any other machine in remote.cs.binghamton.edu and go to the directory that contains the .jar file and run it using the command given in 3.

Testing the program
-------------------
1. Type JOIN and press enter on all the machines except the bootstrap node (it is already part of the network).

2. The JOIN success message is displayed on all the nodes.

3. Now all the nodes have joined the network and we can start performing operations.

4. Inserting a keyword can be done from any peer. The following is the command for INSERT,

	INSERT keyword

The INSERT success message, along with information of the peer in which the keyword was inserted, is displayed. If the INSERT was not successful then a FAILURE message is displayed.

5. We can now search for the keyword inserted from any of the peers. The following is the command for SEARCH,

	SEARCH keyword

The SEARCH success message, along with the information of the peer from which the keyword was retrieved, is displayed. The route taken by the SEARCH request is alsp displayed. If the SEARCH failed (file not present) then a FAILURE message is displayed. 

6. A node can decide to leave the network. Type 'LEAVE' for the node to leave the network. A success message corresponding to the LEAVE operation is displayed. 

7. The node can now rejoin the network by typing 'JOIN'


Sample testing of the program and corresponding outputs
-------------------------------------------------------

1. Running the program on the following nodes
	a. remote07.cs.binghamton.edu (bootstrap node)
	b. remote02.cs.binghamton.edu
	c. remote03.cs.binghamton.edu
	d. remote04.cs.binghamton.edu

2. Initial display when the program is run on remote07.cs.binghamton.edu (bootstrap node)

	Bootstrap loaded and Initialized.
	Please provide a command. The possible commands are :
	VIEW -- VIEW [hostname]
	INSERT -- INSERT filename
	SEARCH -- SEARCH filename

  Initial display when the program is run on the other nodes (remote02.cs.binghamton.edu, remote03.cs.binghamton.edu and remote04.cs.binghamton.edu)

	Please provide a command. The possible commands are :
	JOIN -- JOIN
	VIEW -- VIEW [hostname]	

3. Joining from remote02.cs.binghamton.edu. Display of JOIN success is as shown below

	JOIN SUCCESSUL!
	IP of New Peer : remote02.cs.binghamton.edu/128.226.180.164
	Hostname of New Peer : remote02.cs.binghamton.edu
	Zone of New Peer : (5.0-10.0,0.0-10.0)

 4. Viewing from remote07.cs.binghamton (VIEW)(this will display information of all the peers in the network)

	Hostname : remote02.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.164
	Zone : (5.0-10.0,0.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote07.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote07.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.169
	Zone : (0.0-5.0,0.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote02.cs.binghamton.edu, 
	------------------------------------------------------------------

5. Joining from remote03.cs.binghamton.edu. Display of JOIN success is as shown below

	JOIN SUCCESSUL!
	IP of New Peer : remote03.cs.binghamton.edu/128.226.180.165
	Hostname of New Peer : remote03.cs.binghamton.edu
	Zone of New Peer : (5.0-10.0,0.0-5.0)

6. Viewing from remote07.cs.binghamton (VIEW)(this will display information of all the peers in the network)

	Hostname : remote02.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.164
	Zone : (5.0-10.0,5.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote03.cs.binghamton.edu, remote07.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote03.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.165
	Zone : (5.0-10.0,0.0-5.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote02.cs.binghamton.edu, remote07.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote07.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.169
	Zone : (0.0-5.0,0.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote02.cs.binghamton.edu, remote03.cs.binghamton.edu, 
	------------------------------------------------------------------

7. Joining from remote04.cs.binghamton.edu. Display of success is as shown below.

	JOIN SUCCESSUL!
	IP of New Peer : remote04.cs.binghamton.edu/128.226.180.166
	Hostname of New Peer : remote04.cs.binghamton.edu
	Zone of New Peer : (7.5-10.0,5.0-10.0)

8. Viewing from remote07.cs.binghamton (VIEW)(this will display information of all the peers in the network)

	Hostname : remote02.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.164
	Zone : (5.0-7.5,5.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote03.cs.binghamton.edu, remote04.cs.binghamton.edu, remote07.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote04.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.166
	Zone : (7.5-10.0,5.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote02.cs.binghamton.edu, remote03.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote03.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.165
	Zone : (5.0-10.0,0.0-5.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote02.cs.binghamton.edu, remote04.cs.binghamton.edu, remote07.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote07.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.169
	Zone : (0.0-5.0,0.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote02.cs.binghamton.edu, remote03.cs.binghamton.edu, 
	------------------------------------------------------------------

9. Inserting filename abc.txt at remote02.cs.binghamton.edu. Display of INSERT success message is as shown below.

	Command : INSERT
	Status : INSERT operation successful.
	Inserted file : abc.txt.
	Peer hostName : remote07.cs.binghamton.edu.
	Peer ipAddress : remote07.cs.binghamton.edu/128.226.180.169
	Affected Peer hostname : remote07.cs.binghamton.edu
	Affected Peer ipAddress : remote07.cs.binghamton.edu/128.226.180.169
	Affected Peer zone : (0.0-5.0,0.0-10.0)
	Route taken : remote02.cs.binghamton.edu -> remote07.cs.binghamton.edu

10. Searching for file from filename from remote04.cs.binghamton.edu. Display of SEARCH success message is as shown below.

	Command : SEARCH
	Status : Search successful
	Affected Peer hostname : remote07.cs.binghamton.edu
	Affected Peer ipAddress : remote07.cs.binghamton.edu/128.226.180.169
	Affected Peer zone : (0.0-5.0,0.0-10.0)
	Route taken : remote04.cs.binghamton.edu -> remote02.cs.binghamton.edu -> remote07.cs.binghamton.edu

11. LEAVE request from remote02.cs.binghamton.edu. Display of LEAVE success message is as shown below.

	Leave Successful
	-----------------
	Hostname of node that left : remote02.cs.binghamton.edu
	Ip address of node that left : remote02.cs.binghamton.edu/128.226.180.164

12. Viewing all the peers' information from remote03.cs.binghamton.edu. The display of information of all the peers is as shown below.

	Hostname : remote04.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.166
	Zone : (5.0-10.0,5.0-10.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote03.cs.binghamton.edu, remote07.cs.binghamton.edu, 
	------------------------------------------------------------------
	Hostname : remote07.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.169
	Zone : (0.0-5.0,0.0-10.0)
	Temp Zone : null
	Files : [abc.txt]
	Temp files : []
	Neighbours : remote07.cs.binghamton.edu, remote03.cs.binghamton.edu, 
	Hostname : remote03.cs.binghamton.edu
	------------------------------------------------------------------
	Ip Address : 128.226.180.165
	Zone : (5.0-10.0,0.0-5.0)
	Temp Zone : null
	Files : []
	Temp files : []
	Neighbours : remote04.cs.binghamton.edu, remote07.cs.binghamton.edu,
















