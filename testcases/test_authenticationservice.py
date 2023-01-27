from utils import *

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
    #sleep for 1 sec to ensure synchronization complete 
    time.sleep(1)

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
