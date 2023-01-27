from utils import *

def createaccountandlogin(name):
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
    return (accountno, sessionkey)

def test_enquire_init_balance():
    name='teib1'
    r = createaccountandlogin(name)
    account = r[0]
    sessionkey = r[1]
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M003]'))
    balance = parsereturnstringfromenquirebalance(text)
    assert(0.0==balance)

def test_transfer_deposit():
    name='ttd'
    r = createaccountandlogin(name)
    account = r[0]
    sessionkey = r[1]
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M003]'))
    balance = parsereturnstringfromenquirebalance(text)
    assert(0.0==balance)
    r = transfer(sessionkey,250.5)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M007]'))
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M003]'))
    balance = parsereturnstringfromenquirebalance(text)
    assert(250.5==balance)


def test_transfer_withdraw():
    name='ttd'
    r = createaccountandlogin(name)
    account = r[0]
    sessionkey = r[1]
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M003]'))
    balance = parsereturnstringfromenquirebalance(text)
    assert(0.0==balance)

    #deposit
    r = transfer(sessionkey,250.5)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M007]'))
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M003]'))
    balance = parsereturnstringfromenquirebalance(text)
    assert(250.5==balance)

    #withdraw
    r = transfer(sessionkey,-100.3)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M007]'))
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M003]'))
    balance = parsereturnstringfromenquirebalance(text)
    assert(150.2==balance)
