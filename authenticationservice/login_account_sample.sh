x=000000000325260001;y=notsecurepassword; curl -s http://localhost:8082/api/login -X POST -H 'application/json' --data "accountno=${x},password=${y}"
