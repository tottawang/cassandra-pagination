# cassandra-pagination

### Run and build
Step-1 : 
create keyspace products by running createTables.cql under resources folder
export SPRING_CACHE_TYPE=NONE (don't use cache)
export CASSANDRA_KEYSPACE=products
./gradlew clean build

Step-2 :
java -jar build/libs/sample-0.0.1-SNAPSHOT.jar

