Aditya Kulkarni [ak8650]

The program supports multiple clients sending files to same / different servers. 

The RIP packets are sent over multicast IP. 

The data packets are sent over port 8080 (hardcoded). 

### File
The file is saved on the server as client_ip.jpg 
e.g. 
client_ip = 10.0.1.0/24
and server_ip = 10.0.3.0/24

Then file for this connection will be saved on the server as 10.0.1.0.jpg in the same directory as the input image. 

The directory for the input image is added as a volume to the container. The command for that is given in the build section. 

### Arguments to the container
CLIENT args : unique number destination_ip filename.jpg
e.g. router 1 10.0.3.0/24 /usr/src/myapp/sent.jpg

NODE (either Router or Server) args : unique number
e.g. router 3


### To build
unzip zip file

go to folder routing-information-protocol-v2/src

docker build -t router .

### To create the node network
Only needs to be done once.

docker network create --subnet=172.18.0.0/16 pipernet

### Client --- Router --- Server

### Substitute the volume "/Users/csci-651/routing-information-protocol-v2" for the directory which has the image. 

docker run -it -p 8082:8080 --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.22 router 2

docker run -it -p 8081:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.21 router 1

docker run -it -p 8083:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.23 router 3 10.0.1.0/24 /usr/src/myapp/sent.jpg

### Client --- Client --- Server

docker run -it -p 8081:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.21 router 1

docker run -it -p 8083:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.23 router 3 10.0.1.0/24 /usr/src/myapp/sent.jpg

docker run -it -p 8082:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.22 router 2 10.0.1.0/24 /usr/src/myapp/small.jpg


curl "http://localhost:8083/?block=172.18.0.21"

curl "http://localhost:8081/?block=172.18.0.23"

### Client 1 --- Client 2 --- Server 1 --- Server 2 (clockwise)

docker run -it -p 8083:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.23 router 3

docker run -it -p 8084:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.24 router 4

docker run -it -p 8081:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.21 router 1 10.0.3.0/24 /usr/src/myapp/sent.jpg

docker run -it -p 8082:8080 -v /Users/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.22 router 2 10.0.4.0/24 /usr/src/myapp/space.jpg


curl "http://localhost:8081/?block=172.18.0.24&block=172.18.0.23"

curl "http://localhost:8082/?block=172.18.0.24"

curl "http://localhost:8083/?block=172.18.0.21"

curl "http://localhost:8084/?block=172.18.0.21&block=172.18.0.22"


