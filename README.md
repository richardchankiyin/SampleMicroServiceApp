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
- 2a using Authentication Service /api/login to login
- 2b Multiple login with the same account is not allowed and old sessions should be kicked out
- 3a using Transaction Service /api/account with prior authentication done in Authentication Service
- 3b using Transaction Service /api/account/transfer with prior authentication done in Authentication Service

Solution
==========
## Tech Stack
- os: linux/mac (or Windows with bash)
- mava 11
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
- com.richard.authenticateservice and com.richard.transactionservice are the core packages. Application restapis are provided by AuthenticationserviceController and TransactionserviceController
- AuthenticationserviceScheduledTasks and TransactionserviceScheduledTasks are controlling the implementation of scheduled tasks. These scheduled tasks are the background tasks
- com.richard.authenticationservice.db and com.richard.transactionservice.db are controlling the implementation of database related activities
- com.richard.authenticationservice.model and com.richard.transactionservice.model are POJO holding data
- com.richard.authenticationservice.msg and com.richard.transactionservice.msg are controlling the implementation of messaging queue activities
- com.richard.authenticationservice.process and com.richard.transactionservice.process are controlling the implementation of higher level domain logic. They will have domain level validation performed and call db/mq for data persistency or messaging
- com.richard.transactionservice.api is controlling the restapi calls to external service (i.e. Authentication Service)

## API explanation
- Authentication Service
  - /api/createAccount: To create account by providing name
    - sample: x=$1; curl -s http://localhost:8082/api/createAccount -X POST -H 'application/json' --data-raw "name=${x}" (could be found at authenticationservice/create_account_sample.sh)
    - return messages:
       - M001 Account created successfully with account no generated. Example: [M001]Account created successfully[Account No:000000023796100001]
       - E001 Wrong Request Content. Example: [E001]Wrong Request Content
       - F001 System error. To be seen when the application is running wrong. Possible reasons could be database/mq/file system error

  - /api/login: To login by providing accountno and password (for simplicity password is static. Value being used: notsecurepassword)
    - sample: x=$1;y=notsecurepassword; curl -s http://localhost:8082/api/login -X POST -H 'application/json' --data "accountno=${x},password=${y}" (could be found at authenticationservice/login_account_sample.sh)
    - return messages:
       - M004 Login successfully with session key. Example: [M004]Login successfully[Session:8e0838bb-be3c-4cc1-a774-4ca7daf621f6]
       - E003 Unable to login. That could be application issues. Example: [E003]Unable to login
       - E004 Incorrect Login Info. Example: [E004]Incorrect Login Info

  - /api/logout: To logout by providing sessionkey obtained after login
    - sample: x=$1; curl -s http://localhost:8082/api/logout -X POST -H 'application/json' --data "sessionkey=${x}" (could be found at authenticationservice/logout_account_sample.sh) 
    - return messages:
       - M005 Logout successfully. Example: [M005]Logout successfully
       - W002 Attempt to logout using invalid session key. That could happen when another login session kicks the session key out. Or the session key expires. Example: [W002]Attempt to logout using invalid session key
       - E001 Wrong Request Content. Example: [E001]Wrong Request Content
       - F001 System error. To be seen when the application is running wrong. Possible reasons could be database/mq/file system error

  - /api/validateSession: To validate session by providing sessionkey after login
    - sample: x=$1; curl -s http://localhost:8082/api/validateSession -X POST -H 'application/json' --data "sessionkey=${x}" (could be found at authenticationservice/validate_session_sample.sh)
    - return messages:
       - M007 Valid session with accountno returned. Example: [M007]Valid session[accountno=000000023796100003]
       - M006 Invalid session. Example [M006]Invalid session
       - E001 Wrong Request Content. Example: [E001]Wrong Request Content
       - F001 System error. To be seen when the application is running wrong. Possible reasons could be database/mq/file system error

  - /api/admin/checkStatus: To check the status of the application by providing admin password (for simplicity static password iamadmin is being used)
    - sample: x=iamadmin; curl -s http://localhost:8082/api/admin/checkStatus -X POST -H 'application/json' --data "password=${x}" (could be found at authenticationservice/admin_check_status.sh)
    - return message:
       - [A001]Service is ready

- Transaction Service
  - /api/account: To retrieve account balance by providing sessionkey after login
    - sample: x=$1; curl -s http://localhost:8083/api/account -X POST -H 'application/json' --data "sessionkey=${x}" (could be found at transactionservice/enquire_balance.sh)
    - return messages:
       - M003 Authorized with balance. Example: [M003]Authorized[balance:250.50000]
       - M002 Unauthorized. Example: [M002]Unauthorized

  - /api/account/transfer: To transfer from/to account by providing sessionkey after login and amount (-ve as withdraw and +ve as deposit)
    - sample: x=$1; y=$2; curl -s http://localhost:8083/api/account/transfer -X POST -H 'application/json' --data "sessionkey=${x},amount=${y}" (could be found at transactionservice/transfer.sh)
    - return messages:
       - M007 Transfer complete with requestid and current balance returned. Example:[M007]Transfer complete[requestid=2914f645-5386-4dc8-94d5-5953711749de,balance=250.50000]
       - M006 Transfer rejected with requestid returned. Example: [M006]Transfer rejected[requestid=2914f645-5386-4dc8-94d5-5953711749de]
       - M002 Unauthorized. Example: [M002]Unauthorized

  - /api/admin/checkStatus: To check the status of the application by providing admin password (for simplicity static password iamadmin is being used)
    - sample: x=iamadmin; curl -s http://localhost:8083/api/admin/checkStatus -X POST -H 'application/json' --data "password=${x}" (could be found at transactionservice/admin_check_status.sh)
    - return message:
       - [A001]Service is ready

  - /api/admin/retrieveDuplicateAcctSync: To check the status of the application by providing admin password and messagekey (for simplicity static password iamadmin is being used)
i   - sample: x=iamadmin;y=$1; curl -s http://localhost:8083/api/admin/retrieveDuplicateAcctSync -X POST -H 'application/json' --data "password=${x},msgkey=${y}" (could be found at transactionservice/admin_retrieve_duplicateacctsync.sh)
    - return messages:
       - [A003]No duplicate account sync found
       - A004 Duplicate account sync found with message detail. Example: [A004]Duplicate account sync found[msgkey=0268ddb2-4453-4523-bb0a-9be27f88b0b7,accountno=000000023832080006,payload={"msgKey":"0268ddb2-4453-4523-bb0a-9be27f88b0b7","account":{"accountNo":"000000023832080006","name":"tai1"}},time=2023-01-28 14:00:28.211]
       - [A001]Service is ready
