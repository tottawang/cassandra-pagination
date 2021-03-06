# cassandra-pagination

##  Run and build

### Step 1
- create keyspace test_pagination_keyspace and run createTables.cql under resources folder
- export SPRING_CACHE_TYPE=NONE (don't use cache)
- export CASSANDRA_KEYSPACE=test_pagination_keyspace
- /gradlew clean build

### Step 2
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=9001,suspend=n -jar build/libs/sample-0.0.1-SNAPSHOT.jar

### Step 3
- Add data by calling POST multiple times http://localhost:8082/api/insert
- Add data by calling GET http://localhost:8082/api/paged-products

##  Sample pagination payload

_{
    "links": {
        "next": "/api/paged-products?pagingState=001e0010001200106ed8163b30fb46459f4120d048cd6461f07ffffffdf07ffffffd349186dd7a2841bcae33b5eda8dacc910004"
    },
    "data": [
        {
            "itemid": "new item id",
            "version": 1,
            "productid": "3842908b-519c-4090-9bbe-6e803168cb8f",
            "scopes": {
                "name": "value"
            },
            "type": "Rocket",
            "attributes": {
                "level": "1",
                "category": "test"
            },
            "text": null,
            "productKey": {
                "itemid": "new item id",
                "version": 1,
                "productid": "3842908b-519c-4090-9bbe-6e803168cb8f"
            }
        },
        {
            "itemid": "new item id",
            "version": 1,
            "productid": "6ed8163b-30fb-4645-9f41-20d048cd6461",
            "scopes": {
                "name": "value"
            },
            "type": "Rocket",
            "attributes": {
                "level": "1",
                "category": "test"
            },
            "text": null,
            "productKey": {
                "itemid": "new item id",
                "version": 1,
                "productid": "6ed8163b-30fb-4645-9f41-20d048cd6461"
            }
        }
    ]
}_
