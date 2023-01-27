import requests
import logging
import mysql.connector
import time
import os

log = logging.getLogger()

def bringupauthenticationservice():
    c = 'java -Dsession.valid.duration.millisecond=300000 -Duser.timezone=UTC -Dlogging.level.com.richard.authenticationservice=DEBUG -Dserver.port=8082 -jar ../authenticationservice/target/authenticationservice-0.0.1-SNAPSHOT.jar &'
    os.system(c)
    time.sleep(5)
    r = checkstatus(getauthenticationservice())
    log.info('return text %s', r.text) 

def bringuptransactionservice():
    c = 'java -Duser.timezone=UTC -Dlogging.level.com.richard.transactionservice=DEBUG -Dauthenticationservice.connect.host=localhost -Dauthenticationservice.connect.port=8082 -Dauthenticationservice.connect.timeout.millisecond=5000 -Dserver.port=8083 -jar ../transactionservice/target/transactionservice-0.0.1-SNAPSHOT.jar &'
    os.system(c)
    time.sleep(5)
    r = checkstatus(gettransactionservice())
    log.info('return text %s', r.text) 

def killalljava():
    c = "jcmd | awk '{print $1}' | xargs kill -9"
    os.system(c)

def getauthenticationservice():
    return 'localhost:8082'

def gettransactionservice():
    return 'localhost:8083'

def getaccountsyncfromdb(accountno, h, p, u, pw, dbname):
    conn = mysql.connector.connect(host=h, port=p, user=u, password=pw, database=dbname)
    cursor = conn.cursor()
    try:
        cursor.execute('select msgkey, accountno, payload, status from accountsync where accountno=' + accountno)
        r = cursor.fetchall()
        log.debug('getaccountsyncfrom db %s', r)
        return r 
    finally:
        cursor.close()
        conn.close()


def getaccountsyncfromauthenticationdb(accountno):
    return getaccountsyncfromdb(accountno, 'localhost', 23306, 'app', 'apppass', 'test')

def getaccountsyncfromtransactiondb(accountno):
    return getaccountsyncfromdb(accountno, 'localhost', 33306, 'app', 'apppass', 'test')

def getaccountfromdb(accountno, h, p, u, pw, dbname):
    conn = mysql.connector.connect(host=h, port=p, user=u, password=pw, database=dbname)
    cursor = conn.cursor()
    try:
        cursor.execute('select accountno, name from account where accountno=' + accountno)
        r = cursor.fetchall()
        log.debug('getaccountfrom db %s', r)
        return r 
    finally:
        cursor.close()
        conn.close()

def getaccountfromauthenticationdb(accountno):
    return getaccountfromdb(accountno, 'localhost', 23306, 'app', 'apppass', 'test')

def getaccountfromtransactiondb(accountno):
    return getaccountfromdb(accountno, 'localhost', 33306, 'app', 'apppass', 'test')

def parsereturnstringfromcreateaccount(text):
    #sample return [M001]Account created successfully[Account No:000000000368690004]
    accountno = text[46:len(text)-1]
    return accountno 

def parserturnstringfromlogin(text):
    #sample return [M004]Login successfully[Session:c343abd0-963a-401b-b085-f91069a468b6]
    sessionkey = text[33:len(text)-1]
    return sessionkey

def parsereturnstringfromvalidatesession(text):
    #[M007]Valid session[accountno=000000000368690032]
    accountno = text[30:len(text)-1]
    return accountno

def parsereturnstringfromenquirebalance(text):
    #[M003]Authorized[balance:0.00000]
    balance = text[25:len(text)-1]
    return float(balance)

def create_account(x):
    r = requests.post('http://' + getauthenticationservice() + '/api/createAccount', data='name='+x)
    return r

def loginwithpasswd(x, pw):
    r = requests.post('http://' + getauthenticationservice() + '/api/login', data='accountno='+x+',password='+pw)
    return r

def login(x):
    r = loginwithpasswd(x,'notsecurepassword')
    return r

def validatesession(x):
    r = requests.post('http://' + getauthenticationservice() + '/api/validateSession', data='sessionkey='+x)
    return r

def enquirebalance(x):
    r = requests.post('http://' + gettransactionservice() + '/api/account', data='sessionkey='+x)
    return r

def transfer(x,y):
    r = requests.post('http://' + gettransactionservice() + '/api/account/transfer', data='sessionkey='+x+",amount="+str(y))
    return r

def checkstatus(service):
    x = 'iamadmin'
    r = requests.post('http://' + service + '/api/admin/checkStatus', data='password='+x)
    return r
