x=$1; curl -s http://localhost:8083/api/account -X POST -H 'application/json' --data "sessionkey=${x}"
