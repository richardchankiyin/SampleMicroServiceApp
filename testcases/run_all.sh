python3 setup.py
./run_test_authenticationservice.sh
echo "run_test_authenticationservice.sh $?" > _result
./run_test_transactionservice.sh
echo "run_test_transactionservice.sh $?" >> _result
./run_test_recovery.sh
echo "run_test_recovery.sh $?" >> _result
python3 teardown.py
awk '{print $2}' _result
c=$(awk '{print $2}' _result | sort -u); if [ "0" = "$c" ]; then echo "successful"; exit(0); else echo "no"; exit(1); fi
