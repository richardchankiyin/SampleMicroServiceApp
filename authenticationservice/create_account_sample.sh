x=$1; curl -s http://localhost:8082/api/createAccount -X POST -H 'application/json' --data-raw "name=${x}"
