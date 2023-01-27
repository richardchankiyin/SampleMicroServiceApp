import requests
import logging
import mysql.connector

log = logging.getLogger()

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

def test_create_account_successfully():
    name='tcas1'
    r = create_account(name)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M001]'))

def test_create_account_failed():
    name = 'tcaf1'
    r = requests.post('http://' + getauthenticationservice() + '/api/createAccount', data='n='+name)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[E001]'))

def test_create_account_sync_withdb_successfully():
    name = 'tcasws1'
    r = create_account(name)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M001]'))
    accountno = parsereturnstringfromcreateaccount(text) 
    log.info('accountno: %s', accountno)
    r1 = getaccountfromauthenticationdb(accountno)
    r2 = getaccountfromtransactiondb(accountno)
    log.info('\nauthenticationdb account r1: %s\ntransactiondb account r2:%s\n', r1, r2)
    assert(r1 == r2)

    r1 = getaccountsyncfromauthenticationdb(accountno)
    r2 = getaccountsyncfromtransactiondb(accountno)
    log.info('\nauthenticationdb accountsync r1: %s\ntransactiondb accountsync r2:%s\n', r1, r2)
    assert(r1 == r2)

def test_login_successfully():
    name='tls1'
    r = create_account(name)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M001]'))
    accountno = parsereturnstringfromcreateaccount(text) 
    log.info('accountno: %s', accountno)
    r = login(accountno)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M004]'))

    sessionkey = parserturnstringfromlogin(text)
    log.info('sessionkey %s', sessionkey)
    r = validatesession(sessionkey)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M007]'))
    accountno2 = parsereturnstringfromvalidatesession(text)
    assert(accountno == accountno2)


def test_login_failed_accountnotexist(): 
    accountno="notexistaccount" 
    r = login(accountno)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[E004]'))


def test_login_failed_wrong_password(): 
    name='tlfwp1'
    r = create_account(name)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M001]'))
    accountno = parsereturnstringfromcreateaccount(text) 
    log.info('accountno: %s', accountno)
    r = loginwithpasswd(accountno,'wrongpassword')
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[E004]'))

def test_login_kick_out_previous_session():
    name='tlkops1'
    r = create_account(name)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M001]'))
    accountno = parsereturnstringfromcreateaccount(text) 
    log.info('accountno: %s', accountno)
    r = login(accountno)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M004]'))

    sessionkey = parserturnstringfromlogin(text)
    log.info('sessionkey %s', sessionkey)
    r = validatesession(sessionkey)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M007]'))
    accountno2 = parsereturnstringfromvalidatesession(text)
    assert(accountno == accountno2)

    # relogin
    r = login(accountno)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M004]'))

    
    sessionkey2 = parserturnstringfromlogin(text)
    assert(sessionkey != sessionkey2)

    # old session invalid
    r = validatesession(sessionkey)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M006]'))

    # new session valid
    r = validatesession(sessionkey2)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M007]'))
