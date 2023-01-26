package com.richard.transactionservice.api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.javatuples.Triplet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.richard.transactionservice.TransactionserviceMessageCode;

public class AuthenticationValidatorImpl implements AuthenticationValidator {
	private Logger logger = LoggerFactory.getLogger(AuthenticationValidatorImpl.class);
	private String serviceHost;
	private int servicePort;
	private HttpClient httpClient;
	private static final String VALID_SESSION_CODE = "[M007]";
	
	public AuthenticationValidatorImpl(HttpClient httpClient, String serviceHost, int servicePort) {
		this.httpClient = httpClient;
		this.serviceHost = serviceHost;
		this.servicePort = servicePort;
	}

	private HttpRequest prepareHttpRequest(String host, int port, String sessionkey) {
		String uritemplate = "http://%s:%d/api/validateSession";
		String uri = String.format(uritemplate, host, port);
		logger.debug("authentication service uri: {}", uri);
		HttpRequest request = HttpRequest.newBuilder()
				.POST(HttpRequest.BodyPublishers.ofString("sessionkey=" + sessionkey))
				.uri(URI.create(uri))
				.header("Content-Type", "application/json")
				.build();
		;
		logger.debug("http request: {}", request);
		return request;
	}
	
	@Override
	public Triplet<Boolean, String, String> authenticate(String sessionkey) {
		try {
			HttpResponse<String> response = httpClient.send(prepareHttpRequest(serviceHost, servicePort, sessionkey), HttpResponse.BodyHandlers.ofString());
			int statusCode = response.statusCode();
			String body = response.body();
			if (statusCode != 200) {
				throw new IllegalStateException("httpresponse status code not 200. [" + statusCode + "|" + body + "]");
			}
			logger.debug("response body: {}", body);
			if (body.startsWith(VALID_SESSION_CODE)) {
				return Triplet.with(true, TransactionserviceMessageCode.getInstance().getMessage("M003"), sessionkey);
			} else {
				return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("M002"), sessionkey);
			}			
		} catch (Exception e) {
			logger.error("error when calling authentication service", e);
			return Triplet.with(false, TransactionserviceMessageCode.getInstance().getMessage("F001"), sessionkey);
		}
	}

}
