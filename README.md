# SampleMicroServiceApp
This sample microservice app will have different microservices bridging by rabbitmq. Each microservice has its own database (mysql)

Task goal
=========
A bank is trying to develop a payment app, which can help users to manage all the transactions, create an account and should initiate deposit, withdrawal and balance enquiry. The bank has a few requirements which are below mentioned. Create two services (authentication and transactions) that synchronize user data using rabbitmq and the database should be separated.  

- Whenever a user creates an account in service authentication service it should synchronize into transactions using message queue  
- success or fail task queue should recorded in db  
- The backend should not allow multiple login with the same account. As soon as the account login with the new client the old sessions should be kicked-out.  
- Provide four APIs  
  - /api/createAccount for creating an account with a few user’s details in Service A  
  - /api/login in Service A  
  - /api/account/ for retrieving balance restricted with auth in Service B  
  - /api/account/transfer for transferring balance restricted with auth in Service B  

- The bank is looking for microservices architecture and should be able to communicate with another microservice in future if required.  
- Try to fail the task, the system should be able to pick up the fail task and run again  
- A success task that is repeated should be idempotent. meaning if there is the same task with the same job(duplicate) should not run twice. But the test needs to make sure this happens and provide a system that can handle that.  

The objective  
============
The worker to synchronize the user data should be idempotent. We record the worker task and if the task fails it should be repeatable. All data synchronization should be in the background. Test for failing tasks and test if the system repeats the task over time (duplicated) the system should acknowledge.  

Analysis
========
## Architectural Requirements
- Microservice architecture with message queue about background synchronization
- Authentication and Transaction are two different services (should have their own runtime and database)

## Functional and Non-functional Requirements 
- 1a using Authentication Service /api/createAccount to create account 
- 1b using rabbitmq to synchronize accounts between Authentication Service and Transaction Service in the background 
- 1c Authentication database and Transaction Service database are separated
- 1d Account Synchronization success and fail tasks are being recorded in database
- 1e Account Synchronization should be idempotent which means same task should not run twice.
- 1f Account Synchronization should run again if it fails before
- 2a using Authentication Service /api/login to login
- 2b Multiple login with the same account is not allowed and old sessions should be kicked out
- 3a using Transaction Service /api/account with prior authentication done in Authentication Service
- 3b using Transaction Service /api/account/transfer with prior authentication done in Authentication Service

Solution
==========
## Tech Stack
- os: linux/mac (or Windows with bash)
- java 11
- maven
- docker and docker-compose (as containers of rabbitmq and mysql instances)
- python3 (for integration test purpose, optional)

## Major open-source libraries
- Spring boot 2.7.7
- mysql-connector
- org.apache.commons common-lang3
- javatuples
- com.googlecode.json-simple json-simple
- junit 5
- mockito (for junit mocking purpose)

## Architectural Design
- Authentication Service and Transaction Service are two individually spring boot application. Each of them is connecting to its own mysql instance
- Rabbitmq is bridging two services. Authentication service will send message to a predefined queue and Transaction Service will listen to the queue
for account synchronization
- Authentication Service will provide validateSession api for Transaction Service to verify authentication done before
- Authentication Service will provide account creation and login apis. Transaction Service will provide account balance enquiry and transfer apis
- Authentication Service and Transaction Service will provide admin apis for status checking and account sync duplication checking protected by password
- Authentication Service db will have account, accountsync and accountloginsession tables 
- Transaction Service db will have account, accountsync, accountbalance and accounttransfer tables

## TCP Ports To be used
- 8082 (Authentication Service Spring Boot Server Port)
- 8083 (Transaction Service Spring Boot Server Port)
- 23306 (Authentication Service Mysql listening port mapped to docker mysql instance 3306)
- 33306 (Transaction Service Mysql listening port mapped to docker mysql instance 3306)
- 25672 (Rabbitmq connector listening port mapped to docker rabbitmq 5672)
- 35672 (Rabbitmq admin port mapped to docker rabbitmq 15672) 

## Source Code explanation
- authenticationservice and transactionservice
  - com.richard.authenticateservice and com.richard.transactionservice are the core packages. Application restapis are provided by AuthenticationserviceController and TransactionserviceController
  - AuthenticationserviceScheduledTasks and TransactionserviceScheduledTasks are controlling the implementation of scheduled tasks. These scheduled tasks are the background tasks
  - com.richard.authenticationservice.db and com.richard.transactionservice.db are controlling the implementation of database related activities
  - com.richard.authenticationservice.model and com.richard.transactionservice.model are POJO holding data
  - com.richard.authenticationservice.msg and com.richard.transactionservice.msg are controlling the implementation of messaging queue activities
  - com.richard.authenticationservice.process and com.richard.transactionservice.process are controlling the implementation of higher level domain logic. They will have domain level validation performed and call db/mq for data persistency or messaging
  - com.richard.transactionservice.api is controlling the restapi calls to external service (i.e. Authentication Service)

- dockerimages
  - authenticationservicedb and transactionservicedb are docker-compose files for mysql docker containers
  - rabbitmq is docker-compose file for rabbitmq docker container

- testcases
  - accountable for preparing python venv (requirements.txt), setup, run cases and teardown relying on python3

## API explanation
- Authentication Service
  - /api/createAccount: To create account by providing name
    - return messages:
       - M001 Account created successfully with account no generated. Example: [M001]Account created successfully[Account No:000000023796100001]
       - E001 Wrong Request Content. Example: [E001]Wrong Request Content
       - F001 System error. To be seen when the application is running wrong. Possible reasons could be database/mq/file system error
    - sample: (could be found at authenticationservice/create_account_sample.sh)
```
x=$1; curl -s http://localhost:8082/api/createAccount -X POST -H 'application/json' --data-raw "name=${x}" 
```

  - /api/login: To login by providing accountno and password (for simplicity password is static. Value being used: notsecurepassword)
    - return messages:
       - M004 Login successfully with session key. Example: [M004]Login successfully[Session:8e0838bb-be3c-4cc1-a774-4ca7daf621f6]
       - E003 Unable to login. That could be application issues. Example: [E003]Unable to login
       - E004 Incorrect Login Info. Example: [E004]Incorrect Login Info
    - sample: (could be found at authenticationservice/login_account_sample.sh)
```
x=$1;y=notsecurepassword; curl -s http://localhost:8082/api/login -X POST -H 'application/json' --data "accountno=${x},password=${y}" 
```

  - /api/logout: To logout by providing sessionkey obtained after login
    - return messages:
       - M005 Logout successfully. Example: [M005]Logout successfully
       - W002 Attempt to logout using invalid session key. That could happen when another login session kicks the session key out. Or the session key expires. Example: [W002]Attempt to logout using invalid session key
       - E001 Wrong Request Content. Example: [E001]Wrong Request Content
       - F001 System error. To be seen when the application is running wrong. Possible reasons could be database/mq/file system error
    - sample: (could be found at authenticationservice/logout_account_sample.sh)
```
x=$1; curl -s http://localhost:8082/api/logout -X POST -H 'application/json' --data "sessionkey=${x}"  
```

  - /api/validateSession: To validate session by providing sessionkey after login
    - return messages:
       - M007 Valid session with accountno returned. Example: [M007]Valid session[accountno=000000023796100003]
       - M006 Invalid session. Example [M006]Invalid session
       - E001 Wrong Request Content. Example: [E001]Wrong Request Content
       - F001 System error. To be seen when the application is running wrong. Possible reasons could be database/mq/file system error
    - sample: (could be found at authenticationservice/validate_session_sample.sh)
```
x=$1; curl -s http://localhost:8082/api/validateSession -X POST -H 'application/json' --data "sessionkey=${x}" 
```

  - /api/admin/checkStatus: To check the status of the application by providing admin password (for simplicity static password iamadmin is being used)
    - return message:
       - [A001]Service is ready
    - sample: (could be found at authenticationservice/admin_check_status.sh)
```
x=iamadmin; curl -s http://localhost:8082/api/admin/checkStatus -X POST -H 'application/json' --data "password=${x}" 
```

- Transaction Service
  - /api/account: To retrieve account balance by providing sessionkey after login
    - return messages:
       - M003 Authorized with balance. Example: [M003]Authorized[balance:250.50000]
       - M002 Unauthorized. Example: [M002]Unauthorized
    - sample: (could be found at transactionservice/enquire_balance.sh)
```
x=$1; curl -s http://localhost:8083/api/account -X POST -H 'application/json' --data "sessionkey=${x}" 
```

  - /api/account/transfer: To transfer from/to account by providing sessionkey after login and amount (-ve as withdraw and +ve as deposit)
    - return messages:
       - M007 Transfer complete with requestid and current balance returned. Example:[M007]Transfer complete[requestid=2914f645-5386-4dc8-94d5-5953711749de,balance=250.50000]
       - M006 Transfer rejected with requestid returned. Example: [M006]Transfer rejected[requestid=2914f645-5386-4dc8-94d5-5953711749de]
       - M002 Unauthorized. Example: [M002]Unauthorized
    - sample: (could be found at transactionservice/transfer.sh)
```
x=$1; y=$2; curl -s http://localhost:8083/api/account/transfer -X POST -H 'application/json' --data "sessionkey=${x},amount=${y}"
```

  - /api/admin/checkStatus: To check the status of the application by providing admin password (for simplicity static password iamadmin is being used)
    - return message:
       - [A001]Service is ready
    - sample: (could be found at transactionservice/admin_check_status.sh)
```
x=iamadmin; curl -s http://localhost:8083/api/admin/checkStatus -X POST -H 'application/json' --data "password=${x}" 
```

  - /api/admin/retrieveDuplicateAcctSync: To check the status of the application by providing admin password and messagekey (for simplicity static password iamadmin is being used)
    - return messages:
       - [A003]No duplicate account sync found
       - A004 Duplicate account sync found with message detail. Example: [A004]Duplicate account sync found[msgkey=0268ddb2-4453-4523-bb0a-9be27f88b0b7,accountno=000000023832080006,payload={"msgKey":"0268ddb2-4453-4523-bb0a-9be27f88b0b7","account":{"accountNo":"000000023832080006","name":"tai1"}},time=2023-01-28 14:00:28.211]
       - [A001]Service is ready
    - sample: (could be found at transactionservice/admin_retrieve_duplicateacctsync.sh)
```
x=iamadmin;y=$1; curl -s http://localhost:8083/api/admin/retrieveDuplicateAcctSync -X POST -H 'application/json' --data "password=${x},msgkey=${y}" 
```

Build
=====
## Setup docker
- please add running user to docker group. Otherwise sudo will be required.

## Setup Rabbitmq
- go to directory dockerimages/rabbitmq and type command:
```
docker-compose up -d
```
- run command: 
```
c=$(docker container ls | grep rabbitmq_rabbitmq | awk '{print $1}'); docker exec -it $c rabbitmqadmin declare queue name=accountsync durable=true 
```
(can be found at authenticationservice/setup_mq.sh)
- post check command and expected output:
```
c=$(docker container ls | grep rabbitmq_rabbitmq | awk '{print $1}'); docker exec -it $c rabbitmqadmin list queues
```
```
+-------------+----------+  
|    name     | messages |  
+-------------+----------+  
| accountsync | 0        |  
+-------------+----------+  
```

## Setup Authentication Service mysql
- go to directory dockerimages/authenticationservicedb and type command "docker-compose up -d"
- run command: c=$(docker container ls | grep authenticationservicedb | awk '{print $1}'); docker exec -it $c mysql -uroot -proot test -A 
- above command will be able to access the mysql interactive mode
- copy content of create_grant_user.sql, create_table_account.sql, create_table_accountsync.sql and create_table_accountloginsession.sql under authenticationservice folder 
- after running we can post check by running below commands
```
c=$(docker container ls | grep authenticationservicedb | awk '{print $1}'); docker exec -it $c mysql -uroot -proot test -A -e "select User,Host from mysql.user where User='app' and Host='%'"
+------+------+
| User | Host |
+------+------+
| app  | %    |
+------+------+
```
```
c=$(docker container ls | grep authenticationservicedb | awk '{print $1}'); docker exec -it $c mysql -uroot -proot test -A -e "show tables"
+---------------------+
| Tables_in_test      |
+---------------------+
| account             |
| accountloginsession |
| accountsync         |
+---------------------+
```

## Setup Transaction Service mysql
- go to directory dockerimages/transactionservicedb and type command "docker-compose up -d"
- run command: c=$(docker container ls | grep transactionservicedb | awk '{print $1}'); docker exec -it $c mysql -uroot -proot test -A 
- above command will be able to access the mysql interactive mode
- copy content of create_grant_user.sql, create_table_account.sql, create_table_accountsync.sql, create_table_accountbalance.sql and create_table_accounttransfer.sql under transactionservice folder 
- after running we can post check by running below commands
```
c=$(docker container ls | grep transactionservicedb | awk '{print $1}'); docker exec -it $c mysql -uroot -proot test -A -e "select User,Host from mysql.user where User='app' and Host='%'"
+------+------+
| User | Host |
+------+------+
| app  | %    |
+------+------+
```

```
c=$(docker container ls | grep transactionservicedb | awk '{print $1}'); docker exec -it $c mysql -uroot -proot test -A -e "show tables;"
+-----------------+
| Tables_in_test  |
+-----------------+
| account         |
| accountbalance  |
| accountsync     |
| accounttransfer |
+-----------------+
```

## Setup Authentication Service Spring Boot
- cd authenticationservice folder and type "mvn clean install" and see below to confirm build successful.
```
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ authenticationservice ---
[INFO] Installing /newhome/richard/asklora/repo/SampleMicroServiceApp/authenticationservice/target/authenticationservice-0.0.1-SNAPSHOT.jar to /newhome/richard/.m2/repository/com/richard/authenticationservice/0.0.1-SNAPSHOT/authenticationservice-0.0.1-SNAPSHOT.jar
[INFO] Installing /newhome/richard/asklora/repo/SampleMicroServiceApp/authenticationservice/pom.xml to /newhome/richard/.m2/repository/com/richard/authenticationservice/0.0.1-SNAPSHOT/authenticationservice-0.0.1-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  6.621 s
[INFO] Finished at: 2023-01-29T00:04:24+08:00
[INFO] ------------------------------------------------------------------------

``` 

## Setup Transaction Service Spring Boot
- cd transactionservice folder and type "mvn clean install" and see below to confirm build successful.
```
[INFO] --- maven-install-plugin:2.5.2:install (default-install) @ transactionservice ---
[INFO] Installing /newhome/richard/asklora/repo/SampleMicroServiceApp/transactionservice/target/transactionservice-0.0.1-SNAPSHOT.jar to /newhome/richard/.m2/repository/com/richard/transactionservice/0.0.1-SNAPSHOT/transactionservice-0.0.1-SNAPSHOT.jar
[INFO] Installing /newhome/richard/asklora/repo/SampleMicroServiceApp/transactionservice/pom.xml to /newhome/richard/.m2/repository/com/richard/transactionservice/0.0.1-SNAPSHOT/transactionservice-0.0.1-SNAPSHOT.pom
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  7.120 s
[INFO] Finished at: 2023-01-29T00:07:05+08:00
[INFO] ------------------------------------------------------------------------

```

Run
=====
- please make sure docker images are all up. 
```
docker container ls
CONTAINER ID   IMAGE                   COMMAND                  CREATED      STATUS        PORTS                                                                                                                                                   NAMES
f4a32dcb8363   mysql:8.0               "docker-entrypoint.s…"   2 days ago   Up 2 days     33060/tcp, 0.0.0.0:33306->3306/tcp, :::33306->3306/tcp                                                                                                  transactionservicedb_transactionservicedb_1
6c0017cbcae5   mysql:8.0               "docker-entrypoint.s…"   2 days ago   Up 25 hours   33060/tcp, 0.0.0.0:23306->3306/tcp, :::23306->3306/tcp                                                                                                  authenticationservicedb_authenticationservicedb_1
22d5c054634c   rabbitmq:3-management   "docker-entrypoint.s…"   9 days ago   Up 2 days     4369/tcp, 5671/tcp, 15671/tcp, 15691-15692/tcp, 25672/tcp, 0.0.0.0:25672->5672/tcp, :::25672->5672/tcp, 0.0.0.0:35672->15672/tcp, :::35672->15672/tcp   rabbitmq_rabbitmq_1
```
- run_app.sh can be found under authenticationservice and transactionservice. Running them can bring the applications up. If not, please check any ports are being used (e.g. 8082 and 8083) 
```
richard@richard-linux-mint:~/asklora/repo/SampleMicroServiceApp/transactionservice$ ./run_app.sh 

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.7)

2023-01-28 16:15:19.388  INFO 46373 --- [           main] c.r.t.TransactionserviceApplication      : Starting TransactionserviceApplication v0.0.1-SNAPSHOT using Java 11.0.17 on richard-linux-mint with PID 46373 (/newhome/richard/asklora/repo/SampleMicroServiceApp/transactionservice/target/transactionservice-0.0.1-SNAPSHOT.jar started by richard in /newhome/richard/asklora/repo/SampleMicroServiceApp/transactionservice)
2023-01-28 16:15:19.390 DEBUG 46373 --- [           main] c.r.t.TransactionserviceApplication      : Running with Spring Boot v2.7.7, Spring v5.3.24
2023-01-28 16:15:19.391  INFO 46373 --- [           main] c.r.t.TransactionserviceApplication      : No active profile set, falling back to 1 default profile: "default"
2023-01-28 16:15:20.368  INFO 46373 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8083 (http)
2023-01-28 16:15:20.383  INFO 46373 --- [           main] o.apache.catalina.core.StandardService   : Starting service [Tomcat]
2023-01-28 16:15:20.383  INFO 46373 --- [           main] org.apache.catalina.core.StandardEngine  : Starting Servlet engine: [Apache Tomcat/9.0.70]
2023-01-28 16:15:20.447  INFO 46373 --- [           main] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring embedded WebApplicationContext
2023-01-28 16:15:20.447  INFO 46373 --- [           main] w.s.c.ServletWebServerApplicationContext : Root WebApplicationContext: initialization completed in 982 ms
2023-01-28 16:15:20.626  INFO 46373 --- [           main] c.r.t.TransactionserviceAppResourceImpl  : authenticationservice.connect.timeout.millisecond->5000
2023-01-28 16:15:20.627  INFO 46373 --- [           main] c.r.t.TransactionserviceAppResourceImpl  : valid authentication service connect timeout milliseconds: 5000
2023-01-28 16:15:20.716  INFO 46373 --- [           main] c.r.t.TransactionserviceAppResourceImpl  : authenticationservice.connect.host->localhost
2023-01-28 16:15:20.720  INFO 46373 --- [           main] c.r.t.TransactionserviceAppResourceImpl  : authenticationservice.connect.port->8082
2023-01-28 16:15:20.722  INFO 46373 --- [           main] c.r.t.TransactionserviceAppResourceImpl  : valid authentication host and port: [localhost, 8082]
2023-01-28 16:15:21.144  INFO 46373 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port(s): 8083 (http) with context path ''
2023-01-28 16:15:21.146  INFO 46373 --- [           main] o.s.a.r.c.CachingConnectionFactory       : Attempting to connect to: [localhost:25672]
2023-01-28 16:15:21.187  INFO 46373 --- [           main] o.s.a.r.c.CachingConnectionFactory       : Created new connection: rabbitConnectionFactory#511816c0:0/SimpleConnection@3f36b447 [delegate=amqp://guest@127.0.0.1:25672/, localPort= 32968]
2023-01-28 16:15:21.228  INFO 46373 --- [   scheduling-1] c.r.t.TransactionserviceScheduledTasks   : This is a heart beat activity
2023-01-28 16:15:21.229  INFO 46373 --- [           main] c.r.t.TransactionserviceApplication      : Started TransactionserviceApplication in 2.274 seconds (JVM running for 2.632)
```

Test
====
## Unit Test
- using maven install feature to run unit tests of authenticationservice and transactionservice

## Integration Test
- setup python venv (https://packaging.python.org/en/latest/guides/installing-using-pip-and-virtual-environments/)
- source venv
- go to testcases folder and run below to install required packages
```
pip install -r requirements.txt
```
- ./run_all.sh will bring up applications, run cases and tear down (***Remarks: all data in db and mq will be purged, if you want to keep data please backup or you can choose separate db instances/db for running applications other than this test) 
- sample output:
```
2023-01-28 16:31:02.273 DEBUG 61836 --- [nio-8083-exec-1] c.r.t.TransactionserviceController       : checkStatus result: [true, [A001]Service is ready]
--------------------------------------------------------------------------------------------------- Captured log call ----------------------------------------------------------------------------------------------------
INFO     root:test_recovery.py:60 killing transaction service
INFO     root:test_recovery.py:62 bringing up fault transaction service
INFO     root:utils.py:38 return text [A001]Service is ready
INFO     root:test_recovery.py:68 returned text [M001]Account created successfully[Account No:000000023922330001]
INFO     root:test_recovery.py:72 accountno: 000000023922330001
INFO     root:test_recovery.py:75 returned text [M004]Login successfully[Session:8252e340-a192-4332-af25-cc9067218e17]
INFO     root:test_recovery.py:80 sessionkey 8252e340-a192-4332-af25-cc9067218e17
INFO     root:test_recovery.py:83 returned text [M007]Valid session[accountno=000000023922330001]
INFO     root:test_recovery.py:91 
authenticationdb account r1: [('000000023922330001', 'tcasatr1')]
transactiondb account r2:[]

INFO     root:test_recovery.py:96 
authenticationdb accountsync r1: [('22c4bc11-f19c-438f-9bbf-090de6373832', '000000023922330001', '{"msgKey":"22c4bc11-f19c-438f-9bbf-090de6373832","account":{"accountNo":"000000023922330001","name":"tcasatr1"}}', 'S')]
transactiondb accountsync r2:[]

INFO     root:test_recovery.py:99 killing fault transaction service
INFO     root:test_recovery.py:101 bringing up transaction service
INFO     root:utils.py:23 return text [A001]Service is ready
INFO     root:test_recovery.py:106 
authenticationdb account r1: [('000000023922330001', 'tcasatr1')]
transactiondb account r2:[('000000023922330001', 'tcasatr1')]

INFO     root:test_recovery.py:111 
authenticationdb accountsync r1: [('22c4bc11-f19c-438f-9bbf-090de6373832', '000000023922330001', '{"msgKey":"22c4bc11-f19c-438f-9bbf-090de6373832","account":{"accountNo":"000000023922330001","name":"tcasatr1"}}', 'S')]
transactiondb accountsync r2:[('22c4bc11-f19c-438f-9bbf-090de6373832', '000000023922330001', '{"msgKey":"22c4bc11-f19c-438f-9bbf-090de6373832","account":{"accountNo":"000000023922330001","name":"tcasatr1"}}', 'S')]
-------------------------------------------------- generated html file: file:///newhome/richard/asklora/repo/SampleMicroServiceApp/testcases/test_recovery_report.html ---------------------------------------------------
================================================================================================ short test summary info =================================================================================================
PASSED test_recovery.py::test_create_account_sync_after_authentication_recovery
PASSED test_recovery.py::test_create_account_sync_after_transaction_recovery
=================================================================================================== 2 passed in 41.74s ===================================================================================================
0
0
0
successful
```

- test_authenticationservice_report.html, test_recovery_report.html and test_transactionservice_report.html are report files output. 

### Requirement Tracebility by test cases
Requirement  | Test Cases
------------- | -------------
1a using Authentication Service /api/createAccount to create account  | test_authenticationservice.py::test_create_account_successfully, test_authenticationservice.py::test_create_account_failed
1b using rabbitmq to synchronize accounts between Authentication Service and Transaction Service in the background | test_authenticationservice.py::test_create_account_sync_withdb_successfully
1c Authentication database and Transaction Service database are separated | test_authenticationservice.py::test_create_account_successfully, test_authenticationservice.py::test_create_account_failed
1d Account Synchronization success and fail tasks are being recorded in database | test_recovery.py::test_create_account_sync_after_authentication_recovery
1e Account Synchronization should be idempotent which means same task should not run twice | test_transactionservice.py::test_accountsync_idempotent
1f Account Synchronization should run again if it fails before | test_recovery.py::test_create_account_sync_after_authentication_recovery, test_recovery.py::test_create_account_sync_after_transaction_recovery
2a using Authentication Service /api/login to login | test_authenticationservice.py::test_login_successfully, test_authenticationservice.py::test_login_failed_accountnotexist, test_authenticationservice.py::test_login_failed_wrong_password
2b Multiple login with the same account is not allowed and old sessions should be kicked out | test_authenticationservice.py::test_login_kick_out_previous_session
3a using Transaction Service /api/account with prior authentication done in Authentication Service | test_transactionservice.py::test_enquire_init_balance, test_transactionservice.py::test_enquire_balance_after_logout_failed  
3b using Transaction Service /api/account/transfer with prior authentication done in Authentication Service | test_transactionservice.py::test_transfer_deposit_then_enquire_balance, test_transactionservice.py::test_transfer_withdraw_then_enquire_balance

Continuous Integration
==============
This repo is ready for continuous integration. A jenkins instance has been setup with information:
- http://3.0.174.58/
- login: guest/welcome
- SampleMicroServiceApp shows maven build and unit test results
- SampleMicroServiceApp_integration shows integration test results and reports
