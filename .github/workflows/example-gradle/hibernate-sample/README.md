# Hibernate sample app

[![Java CI](https://github.com/mfvanek/hibernate-sample/actions/workflows/tests.yml/badge.svg)](https://github.com/mfvanek/hibernate-sample/actions/workflows/tests.yml)
[![codecov](https://codecov.io/gh/mfvanek/hibernate-sample/branch/master/graph/badge.svg?token=S86JZL3IOR)](https://codecov.io/gh/mfvanek/hibernate-sample)

## Requirements
Java 19+

## Docker Compose
### Start
```shell
docker-compose --project-name="hibernate-test" up -d
```

### Stop
```shell
docker-compose --project-name="hibernate-test" down
```

## Explore volumes
### List all volumes
```shell
docker volume ls
```

### Delete specified volume
```shell
docker volume rm hibernate-test_hibernatedb-data
```
