c=$(docker container ls | grep rabbitmq_rabbitmq | awk '{print $1}'); docker exec -it $c rabbitmqadmin declare queue name=accountsync durable=true
