package com.richard.transactionservice;

import java.net.http.HttpClient;
import java.time.Duration;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.transactionservice.api.AuthenticationValidator;
import com.richard.transactionservice.api.AuthenticationValidatorImpl;
import com.richard.transactionservice.db.AccountBalanceDao;
import com.richard.transactionservice.db.AccountBalanceJDBCTemplate;
import com.richard.transactionservice.db.AccountDao;
import com.richard.transactionservice.db.AccountJDBCTemplate;
import com.richard.transactionservice.db.AccountSyncDao;
import com.richard.transactionservice.db.AccountSyncJDBCTemplate;
import com.richard.transactionservice.db.AccountTransferDao;
import com.richard.transactionservice.db.AccountTransferJDBCTemplate;
import com.richard.transactionservice.db.JDBCResourceMgr;
import com.richard.transactionservice.db.JDBCResourceMgrImpl;
import com.richard.transactionservice.process.AccountBalanceMaintenance;
import com.richard.transactionservice.process.AccountBalanceMaintenanceImpl;
import com.richard.transactionservice.process.AccountSyncMessagePayloadParser;
import com.richard.transactionservice.process.AccountSyncMessagePayloadParserImpl;
import com.richard.transactionservice.process.AccountSynchronizer;
import com.richard.transactionservice.process.AccountSynchronizerImpl;
import com.richard.transactionservice.process.AccountTransferRequestIdGenerator;
import com.richard.transactionservice.process.AccountTransferRequestIdGeneratorImpl;
import com.richard.transactionservice.process.AdminMonitor;
import com.richard.transactionservice.process.AdminMonitorImpl;

public class TransactionserviceAppResourceImpl implements TransactionserviceAppResource {
	private Logger logger = LoggerFactory.getLogger(TransactionserviceAppResourceImpl.class);
	private Clock clock;
	private AccountTransferRequestIdGenerator accountTransferRequestIdGenerator;
	private JDBCResourceMgr jdbcResourceMgr;
	private AccountDao accountDao;
	private AccountSyncDao accountSyncDao;
	private AccountBalanceDao accountBalanceDao;
	private AccountTransferDao accountTransferDao;
	private AccountSynchronizer accountSynchronizer;
	private AccountSyncMessagePayloadParser accountSyncMessagePayloadParser;
	private AuthenticationValidator authenticationValidator;
	private AccountBalanceMaintenance accountBalanceMaintenance;
	private HttpClient httpclient;
	private AdminMonitor adminMonitor;
	
	private static TransactionserviceAppResourceImpl instance;
	static {
		instance = new TransactionserviceAppResourceImpl();
	}
	public static TransactionserviceAppResourceImpl getInstance() { return instance; }
	
	public TransactionserviceAppResourceImpl() {
		this.clock = new ClockImpl();
		this.adminMonitor = new AdminMonitorImpl(clock);
		this.accountTransferRequestIdGenerator = new AccountTransferRequestIdGeneratorImpl();
		this.jdbcResourceMgr = JDBCResourceMgrImpl.getInstance();
		this.accountDao = new AccountJDBCTemplate(jdbcResourceMgr);
		this.accountSyncDao = new AccountSyncJDBCTemplate(jdbcResourceMgr);
		this.accountBalanceDao = new AccountBalanceJDBCTemplate(jdbcResourceMgr);
		this.accountTransferDao = new AccountTransferJDBCTemplate(jdbcResourceMgr);
		this.accountSynchronizer = new AccountSynchronizerImpl(jdbcResourceMgr
				, accountDao, accountSyncDao, accountBalanceDao, adminMonitor);
		this.accountSyncMessagePayloadParser = new AccountSyncMessagePayloadParserImpl();
		this.httpclient = prepareHttpClient();
		Pair<String,Integer> authenticationHostAndPort = getAuthenticationHostAndPort();
		this.authenticationValidator = new AuthenticationValidatorImpl(httpclient
				, authenticationHostAndPort.getValue0(), authenticationHostAndPort.getValue1());
		this.accountBalanceMaintenance = new AccountBalanceMaintenanceImpl(
				clock, jdbcResourceMgr, accountBalanceDao, accountTransferDao);		
	}

	private Pair<String, Integer> getAuthenticationHostAndPort() {
		String configStrHost = System.getProperty("authenticationservice.connect.host");
		String resultHost = "localhost";
		logger.info("authenticationservice.connect.host->{}", configStrHost);
		if (!StringUtils.isBlank(configStrHost)) {
			resultHost = configStrHost;
		}
		
		String configStrPort = System.getProperty("authenticationservice.connect.port");
		int resultPort = 8080;
		logger.info("authenticationservice.connect.port->{}", configStrPort);
		try {
			resultPort = Integer.parseInt(configStrPort);
		} catch (Exception e) {
			logger.error("Integer.parseInt", e);
			logger.warn("failed to obtain AuthenticationServiceConnectionPort");			
		}
		Pair<String, Integer> result = Pair.with(resultHost, Integer.valueOf(resultPort));
		logger.info("valid authentication host and port: {}", result);
		return result;
	}
	
	private long getAuthenticationServiceConnectionTimeout() {
		long builtindefault = 10000L;
		long result = builtindefault;
		String configStr = System.getProperty("authenticationservice.connect.timeout.millisecond");
		
		logger.info("authenticationservice.connect.timeout.millisecond->{}", configStr);
		try {
			result = Long.parseLong(configStr);
		} catch (Exception e) {
			logger.error("Long.parseLong", e);
			logger.warn("failed to obtain AuthenticationServiceConnectionTimeout. Using built-in default: {}", builtindefault);
		}
		logger.info("valid authentication service connect timeout milliseconds: {}", result);
		return result;
	}
	
	private HttpClient prepareHttpClient() {
		long timeout = getAuthenticationServiceConnectionTimeout();
		return HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofMillis(timeout))
        .build();
	}
	
	@Override
	public AccountDao getAccountDao() {
		return accountDao;
	}

	@Override
	public AccountSyncDao getAccountSyncDao() {
		return accountSyncDao;
	}

	@Override
	public AccountBalanceDao getAccountBalanceDao() {
		return accountBalanceDao;
	}

	@Override
	public JDBCResourceMgr getJDBCResourceMgr() {
		return jdbcResourceMgr;
	}

	@Override
	public AccountSynchronizer getAccountSynchronizer() {
		return accountSynchronizer;
	}

	@Override
	public AccountSyncMessagePayloadParser getAccountSyncMessagePayloadParser() {
		return accountSyncMessagePayloadParser;
	}

	@Override
	public AuthenticationValidator getAuthenticationValidator() {
		return authenticationValidator;
	}

	@Override
	public AccountBalanceMaintenance getAccountBalanceMaintenance() {
		return accountBalanceMaintenance;
	}

	@Override
	public AccountTransferDao getAccountTransferDao() {
		return accountTransferDao;
	}

	@Override
	public Clock getClock() {
		return clock;
	}

	@Override
	public AccountTransferRequestIdGenerator getAccountTransferRequestIdGenerator() {
		return accountTransferRequestIdGenerator;
	}

	@Override
	public AdminMonitor getAdminMonitor() {
		return adminMonitor;
	}

}
