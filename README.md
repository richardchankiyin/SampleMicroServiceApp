# SampleMicroServiceApp
This sample microservice app will have different microservices bridging by rabbitmq. Each microservice has its own database (mysql)

Task goal
=========
A bank is trying to develop a payment app, which can help users to manage all the transactions, create an account and should initiate deposit, withdrawal and balance enquiry. The bank has a few requirements which are below mentioned. Create two services (authentication and transactions) that synchronize user data using rabbitmq and the database should be separated.  

Whenever a user creates an account in service authentication service it should synchronize into transactions using message queue  
success or fail task queue should recorded in db  
The backend should not allow multiple login with the same account. As soon as the account login with the new client the old sessions should be kicked-out.  
Provide four APIs  
/api/createAccount for creating an account with a few user’s details in Service A  
/api/login in Service A  
/api/account/ for retrieving balance restricted with auth in Service B  
/api/account/transfer for transferring balance restricted with auth in Service B  
The bank is looking for microservices architecture and should be able to communicate with another microservice in future if required.  
Try to fail the task, the system should be able to pick up the fail task and run again  
A success task that is repeated should be idempotent. meaning if there is the same task with the same job(duplicate) should not run twice. But the test needs to make sure this happens and provide a system that can handle that.  

The objective  
============
The worker to synchronize the user data should be idempotent. We record the worker task and if the task fails it should be repeatable. All data synchronization should be in the background. Test for failing tasks and test if the system repeats the task over time (duplicated) the system should acknowledge.  

Analysis
========
# Architectural Requirements
- Microservice architecture with message queue about background synchronization
- Authentication and Transaction are two different services (should have their own runtime and database)

# Functional and Non-functional Requirements 
- 1a using Authentication Service /api/createAccount to create account 
- 1b using rabbitmq to synchronize accounts between Authentication Service and Transaction Service in the background 
- 1c Authentication database and Transaction Service database are separated
- 1d Account Synchronization success and fail tasks are being recorded in database
- 1e Account Synchronization should be idempotent which means same task should not run twice.
- 2a using Authentication Service /api/login to login
- 2b Multiple login with the same account is not allowed and old sessions should be kicked out
- 3a using Transaction Service /api/account with prior authentication done in Authentication Service
- 3b using Transaction Service /api/account/transfer with prior authentication done in Authentication Service
