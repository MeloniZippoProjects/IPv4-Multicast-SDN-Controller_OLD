# IPv4-Multicast-SDN-Controller
An SDN controller to manage IPv4 multicast groups

Goal: Design and implement an SDN controller to manage IPv4 multicast groups. 
Consider a local network in which a known set of hosts is connected. The SDN controller will enable the management of IPv4 multicast groups through the following operations (exposed by a proper RESTful interface):
-	Create: creates a new (empty) IPv4 multicast group
-	Delete: deletes an existing multicast group
-	Join: adds a given host to a multicast group
-	Unjoin: removes a given host from a multicast group
Multicast communication is achieved by installing on SDN switches the proper forwarding rules.
Reference material: https://github.com/lab-anaws/lab14-2016. 
