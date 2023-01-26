x=$1; y=$2; curl -s http://localhost:8083/api/account/transfer -X POST -H 'application/json' --data "sessionkey=${x},amount=${y}"
