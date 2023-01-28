c=$(docker container ls | grep transactionservicedb | awk '{print $1}'); for x in $(ls create*sql); do echo "processing... $x"; docker exec $c mysql -uroot -proot test -A < $x; done
