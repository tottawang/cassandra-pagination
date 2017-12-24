# cassandra-pagination

### Run and build
Step-1 : 
create keyspace my_test_keyspace and run createTables.cql under resources folder
export SPRING_CACHE_TYPE=NONE (don't use cache)
export CASSANDRA_KEYSPACE=my_test_keyspace
./gradlew clean build

Step-2 :
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9001,suspend=n -jar build/libs/sample-0.0.1-SNAPSHOT.jar

Step-3:
Add data by calling POST multiple http://localhost:8082/api/insert
Query first page GET http://localhost:8082/api/paged-products
Query next page sample GET http://localhost:8082/api/paged-products http://localhost:8082/api/paged-products?pagingState=001e001000120010eebba0c4f82a4c768b9ee03b55aeb479f07ffffffbf07ffffffbad84d1f600f652a610bc8466d928e0c80004