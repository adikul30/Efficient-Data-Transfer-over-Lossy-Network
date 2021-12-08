# Running in detached mode

docker run -d -p 8083:8080 -v /Users/maverick/Dev/ms/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.23 router 3

docker run -d -p 8084:8080 -v /Users/maverick/Dev/ms/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.24 router 4

sleep 4

docker run -d -p 8081:8080 -v /Users/maverick/Dev/ms/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.21 router 1 10.0.3.0/24 /usr/src/myapp/sent.jpg

docker run -d -p 8082:8080 -v /Users/maverick/Dev/ms/csci-651/routing-information-protocol-v2:/usr/src/myapp --cap-add=NET_ADMIN --net pipernet --ip 172.18.0.22 router 2 10.0.4.0/24 /usr/src/myapp/space.jpg

sleep 8

curl "http://localhost:8081/?block=172.18.0.24&block=172.18.0.23"

curl "http://localhost:8082/?block=172.18.0.24"

curl "http://localhost:8083/?block=172.18.0.21"

curl "http://localhost:8084/?block=172.18.0.21&block=172.18.0.22"