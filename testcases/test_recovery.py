from utils import *

def test_create_account_sync_after_authentication_recovery():
    log.info('killing authentication service')
    killauthenticationservice()
    log.info('bringing up fault authentication service');
    bringupfaultauthenticationservice()

    name='tcasaar1' 
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

    r1 = getaccountfromauthenticationdb(accountno)
    r2 = getaccountfromtransactiondb(accountno)
    log.info('\nauthenticationdb account r1: %s\ntransactiondb account r2:%s\n', r1, r2)
    assert(r1 != r2)

    r1 = getaccountsyncfromauthenticationdb(accountno)
    r2 = getaccountsyncfromtransactiondb(accountno)
    log.info('\nauthenticationdb accountsync r1: %s\ntransactiondb accountsync r2:%s\n', r1, r2)
    assert(r1 != r2)

    log.info('killing fault authentication service')
    killauthenticationservice()
    log.info('bringing up authentication service')
    bringupauthenticationservice()

    r1 = getaccountfromauthenticationdb(accountno)
    r2 = getaccountfromtransactiondb(accountno)
    log.info('\nauthenticationdb account r1: %s\ntransactiondb account r2:%s\n', r1, r2)
    assert(r1 == r2)

    r1 = getaccountsyncfromauthenticationdb(accountno)
    r2 = getaccountsyncfromtransactiondb(accountno)
    log.info('\nauthenticationdb accountsync r1: %s\ntransactiondb accountsync r2:%s\n', r1, r2)
    assert(r1 == r2)


def test_create_account_sync_after_transaction_recovery():
    log.info('killing transaction service')
    killtransactionservice()
    log.info('bringing up fault transaction service');
    bringupfaulttransactionservice()

    name='tcasatr1' 
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

    r1 = getaccountfromauthenticationdb(accountno)
    r2 = getaccountfromtransactiondb(accountno)
    log.info('\nauthenticationdb account r1: %s\ntransactiondb account r2:%s\n', r1, r2)
    assert(r1 != r2)

    r1 = getaccountsyncfromauthenticationdb(accountno)
    r2 = getaccountsyncfromtransactiondb(accountno)
    log.info('\nauthenticationdb accountsync r1: %s\ntransactiondb accountsync r2:%s\n', r1, r2)
    assert(r1 != r2)

    log.info('killing fault transaction service')
    killtransactionservice()
    log.info('bringing up transaction service')
    bringuptransactionservice()

    r1 = getaccountfromauthenticationdb(accountno)
    r2 = getaccountfromtransactiondb(accountno)
    log.info('\nauthenticationdb account r1: %s\ntransactiondb account r2:%s\n', r1, r2)
    assert(r1 == r2)

    r1 = getaccountsyncfromauthenticationdb(accountno)
    r2 = getaccountsyncfromtransactiondb(accountno)
    log.info('\nauthenticationdb accountsync r1: %s\ntransactiondb accountsync r2:%s\n', r1, r2)
    assert(r1 == r2)
