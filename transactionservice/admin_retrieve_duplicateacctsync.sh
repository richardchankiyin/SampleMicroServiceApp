x=iamadmin;y=$1; curl -s http://localhost:8083/api/admin/retrieveDuplicateAcctSync -X POST -H 'application/json' --data "password=${x},msgkey=${y}"
