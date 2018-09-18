# Flowing Retail Docker Compose Files

The docker compose files relate to various default configurations useful to play around or demo the flowing-retail application. 

It references docker files released on https://hub.docker.com/u/flowingretail/ (automatically pushed by Travis CI on every push to this GitHub repo).

You can start the environment you like by

```
docker-compose -f docker-compose-small.yml up -d
```

You can show logs by

```
docker-compose logs -f
```

The cool thing is that cou can "switch" to another config by exectuting this, which will kill all containers that are not needed any more and start the ones missing:

```
docker-compose -f docker-compose-small.yml up -d --remove-orphans
```



