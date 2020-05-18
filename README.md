# Communication-Network-for-Moving-Pods


![Demo-Gif](demo-gif.gif)

A radio network simulated by a multicast IP for inter-pod communication.
A distance-vector ​routing protocol RIPv2 handles node failures and topology changes within the network.
A ​reliable data transfer protocol​ on top of UDP more efficient than TCP (for this scenario). 

## RIPv2 Protocol
Each pod on the network executes RIP to exchange routing information with its neighbors, and based on this information, the pod computes the shortest paths from itself to all the other pods and the container. Pods may move around (appearing and disappearing to the other pods). 

## RDP Protocol
The implementation is based on RFC 1151 which itself is an extension of the core Relaiable Data Protocol RFC 908. 

### Connection

The connection process is similar to TCP, 

1. SYN from client to server
2. SYN/ACK from server to client
3. ACK (piggybacked with data) from client to server

In this process, sliding windows on both sides are initialised with a finite length. 

### Data Transfer

Client => In the third packet of the handshake (ACK) from the client, the client

1. Reads a portion of the file

2. Creates MAX_WINDOW_SIZE * RDP Packets

3. Sends the new packets to server

4. Starts a timer. 

    

Server => The server starts a timer when the first packet arrives. The server buffers packets in the window as they arrive. The server may receive packets out of order. Packets may also get lost in the network. 

Once the timer ends, 

1. The server writes the contiguous packets available from the left edge of the window. 

2. Creates a RDPPacket with ACK NO = left edge of its window = next anticipated packet (Cumulative ACK)

3. Includes a list of EACKs, which are the non-cumulative packets received. 

    

Client => Client process the ACKs and fills its window accordingly. 

Once the timer at the client expires, 

1. It removes the contiguous packets available from the left edge of the window. 
2. Marks the non-cumulative packets from the list of EACKs received. 
3. Creates new packets to send along with unacknowledged packets. 



And so on ...



### Termination

Client => Once the file reader reaches EOF and all the packets in the window are acknowledged, it sends a packet with the FIN flag enabled and terminates. 

Server => Server receives the FIN flag and terminates. 



## Why better than TCP

"Since TCP does not allow a byte to be acknowledged  until all  prior  bytes have been acknowledged, it often forces unnecessary retransmission of data."" 

Solution - EACKs (mentioned above)



"RDP protocol supports a much simpler set of functions than TCP.   The flow control, buffering, and connection management schemes of RDP are considerably  simpler  and  less  complex."

for this scenario. 


## Reference

```
@misc{rfc908,
	series =	{Request for Comments},
	number =	908,
	howpublished =	{RFC 908},
	publisher =	{RFC Editor},
	doi =		{10.17487/RFC0908},
	url =		{https://rfc-editor.org/rfc/rfc908.txt},
        author =	{},
	title =		{{Reliable Data Protocol}},
	pagetotal =	62,
	year =		1984,
	month =		jul,
	abstract =	{The Reliable Data Protocol (RDP) is designed to provide a reliable data transport service for packet-based applications. This RFC specifies a proposed protocol for the ARPA-Internet and DARPA research community, and requests discussion and suggestions for improvemts.},
}
```