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

def test_accountsync_idempotent():
    name='tai1'
    r = createaccountandlogin(name)
    accountno = r[0]
    sessionkey = r[1]

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

    acctsyncbefore = getaccountsyncincluptimefromtransactiondb(accountno)

    msgkey=r1[0][0]
    payload=r1[0][2]
    log.info('msgkey of this account: %s', msgkey) 
    log.info('payload of this account: %s', payload) 
    r = retrieveduplicateacctsync(msgkey)
    text = r.text
    assert(r.status_code == 200)
    assert(text.startswith('[A003]'))
    log.info('before publishing msg. No duplicate found: %s', text) 

    publishmessage(payload)
    
    #sleep for 1 sec to ensure message got processed 
    time.sleep(1)

    r = retrieveduplicateacctsync(msgkey)
    text = r.text
    assert(r.status_code == 200)
    assert(text.startswith('[A004]'))
    log.info('after publishing msg. Duplicate found: %s', text) 
    assert(msgkey in text) 

    acctsyncafter = getaccountsyncincluptimefromtransactiondb(accountno)
    log.info('\naccountsync before: %s\naccountsync after:%s\n', acctsyncbefore, acctsyncafter)
    assert(acctsyncbefore == acctsyncafter)

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

def test_enquire_balance_after_logout_failed():
    name='tebalf1'
    r = createaccountandlogin(name)
    account = r[0]
    sessionkey = r[1]
    r = logout(sessionkey)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    r = enquirebalance(sessionkey) 
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M002]'))

def test_transfer_deposit_then_enquire_balance():
    name='ttdteb1'
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


def test_transfer_withdraw_then_enquire_balance():
    name='ttwteb1'
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

def test_transfer_after_logout_failed():
    name='ttalf1'
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

    r = logout(sessionkey)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)

    #deposit
    r = transfer(sessionkey,250.5)
    text = r.text
    log.info('returned text %s', text)
    assert(r.status_code == 200)
    assert(text.startswith('[M002]'))
