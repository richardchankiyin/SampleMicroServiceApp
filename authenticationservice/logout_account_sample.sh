x=$1; curl -s http://localhost:8082/api/logout -X POST -H 'application/json' --data "sessionkey=${x}"
