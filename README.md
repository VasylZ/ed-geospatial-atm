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
Execute DB scrips from ``db_migratin`` folder
Run docker compose files in ``docker_compose`` folder or containers separately from compose files just ``replace dbHost and kafkaBootstrapServers`` to ip address of your localhost machine
```
docker run --rm -it -e "dbHost=192.168.2.10" -e "kafkaBootstrapServers=192.168.2.10:9092" -p 8081:8081 --name ed-writer-web vasyldockeracc/ed-writer-web
docker run --rm -it -e "dbHost=192.168.2.10" -p 8080:8080 --name ed-reader-web vasyldockeracc/ed-reader-web
docker run --rm -it -e "dbHost=192.168.2.10" -e "kafkaBootstrapServers=192.168.2.10:9092" --name ed-processing vasyldockeracc/ed-processing
```