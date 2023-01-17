# Build and deploy

## Environment
```
Java jdk-11.0.15.1
Maven 3.8.5
Docker 20.10.17
```

## Build
```
maven clean package
```

## Dockerize
Writer
```
docker image build --platform=linux/amd64 --tag vasyldockeracc/ed-writer-web:latest .
docker push vasyldockeracc/ed-writer-web:latest
```
Reader
```
docker image build --platform=linux/amd64 --tag vasyldockeracc/ed-reader-web:latest .
docker push vasyldockeracc/ed-reader-web:latest
```
Processing
```
docker image build --platform=linux/amd64 --tag vasyldockeracc/ed-processing:latest .
docker push vasyldockeracc/ed-processing:latest
```

## Run project
- Pick up infrastructure docker containers ``postgis``, ``kafka``, ``zookeeper`` and ``aerospike`` using compose file ``docker_compose/infra-compose.yml``
- Execute DB scrips from ``db_scripts`` folder.
- Run application docker containers ``ed-writer-web``, ``ed-reader-web`` and ``ed-processing`` using compose file ``docker_compose/ed-atm-compose.yml`` or run containers separately from compose files just ``replace ED_HOST_IP variable`` with ip address of your host machine.

```
export ED_HOST_IP=192.168.2.10
docker run --rm -it -e "dbHost=${ED_HOST_IP}" -e "kafkaBootstrapServers=${ED_HOST_IP}:9092" -p 8081:8081 --name ed-writer-web vasyldockeracc/ed-writer-web
docker run --rm -it -e "dbHost=${ED_HOST_IP}" -p 8080:8080 --name ed-reader-web vasyldockeracc/ed-reader-web
docker run --rm -it -e "dbHost=${ED_HOST_IP}" -e "kafkaBootstrapServers=${ED_HOST_IP}:9092" --name ed-processing vasyldockeracc/ed-processing
```

## REST API 
Application already hosted here:

#### writer http://ed-geospatial.myset.io:8091
#### reader http://ed-geospatial.myset.io:8090

Create atm
```
curl --location --request POST 'http://ed-geospatial.myset.io:8091/rest/v1/write/create' \
--header 'Content-Type: application/json' \
--data-raw '{
    "active": true,
    "name": "BMO1",
    "point": {
        "type": "Point",
        "coordinates": [
            -123.11431,
            49.2643111
        ]
    },
    "address": "Some addres of this atm machine",
    "version": 0
}
'
```
Get atm by id
```
curl --location --request GET 'http://ed-geospatial.myset.io:8090/rest/v1/read/get/{ATM_ID}'
```
Update atm
```
curl --location --request PUT 'http://ed-geospatial.myset.io:8091/rest/v1/write/update' \
--header 'Content-Type: application/json' \
--data-raw '{
    "id": "cf228ff9-369c-4190-831c-c32348661095",
    "active": true,
    "name": "BMO2",
    "point": {
        "type": "Point",
        "coordinates": [
            -123.11431,
            49.2643211
        ]
    },
    "address": "some address of this bmo 4",
    "version": 1
}'
```
Find atms
```
curl --location --request POST 'http://ed-geospatial.myset.io:8090/rest/v1/read/find' \
--header 'Content-Type: application/json' \
--data-raw '{
    "point": {
        "type": "Point",
        "coordinates": [
            -123.1741,
            49.264232
        ]
    },
    "radius": 1200000,
    "limit" : 5,
    "offset" :0
}
'
```