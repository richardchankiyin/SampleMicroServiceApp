package com.richard.transactionservice.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;

import org.javatuples.Triplet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Flow;


class AuthenticationValidatorImplTest {

	private AuthenticationValidatorImpl impl;
	private HttpClient client;
	private HttpResponse<String> response;
	
	@SuppressWarnings("unchecked")
	@BeforeEach
	void setup() {
		client = mock(HttpClient.class);
		
		response = mock(HttpResponse.class);
		impl = new AuthenticationValidatorImpl(client, "localhost", 8080);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void testHttpClientSendFailed() throws Exception{
		doThrow(RuntimeException.class).when(client).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
		Triplet<Boolean, String, String> result = impl.authenticate("c739ace0-ac56-41f3-b9c7-a225add955fb");
		assertFalse(result.getValue0());
		assertEquals("[F001]System error", result.getValue1());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void testHttpClientSendWithResponseStatusCodeNot200() throws Exception{
		ArgumentCaptor<HttpRequest> valueCapture = ArgumentCaptor.forClass(HttpRequest.class);
		when(client.send(valueCapture.capture(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
		when(response.statusCode()).thenReturn(404);
		when(response.body()).thenReturn("Not found");
		
		Triplet<Boolean, String, String> result = impl.authenticate("c739ace0-ac56-41f3-b9c7-a225add955fb");
		
		HttpRequest.BodyPublisher bodyPublisher = valueCapture.getValue().bodyPublisher().get();
		FlowSubscriber<ByteBuffer> flowSubscriber = new FlowSubscriber<>();
	    bodyPublisher.subscribe(flowSubscriber);
		
	    byte[] actual = flowSubscriber.getBodyItems().get(0).array();
	    assertArrayEquals("sessionkey=c739ace0-ac56-41f3-b9c7-a225add955fb".getBytes(), actual);
		
		assertFalse(result.getValue0());
		assertEquals("[F001]System error", result.getValue1());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void testHttpClientSendWithResponseReturningInvalidCode() throws Exception {
		ArgumentCaptor<HttpRequest> valueCapture = ArgumentCaptor.forClass(HttpRequest.class);
		when(client.send(valueCapture.capture(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
		when(response.statusCode()).thenReturn(200);
		when(response.body()).thenReturn("[M006]Invalid session");
		
		Triplet<Boolean, String, String> result = impl.authenticate("c739ace0-ac56-41f3-b9c7-a225add955fb");
		
		HttpRequest.BodyPublisher bodyPublisher = valueCapture.getValue().bodyPublisher().get();
		FlowSubscriber<ByteBuffer> flowSubscriber = new FlowSubscriber<>();
	    bodyPublisher.subscribe(flowSubscriber);
		
	    byte[] actual = flowSubscriber.getBodyItems().get(0).array();
	    assertArrayEquals("sessionkey=c739ace0-ac56-41f3-b9c7-a225add955fb".getBytes(), actual);
	    
	    assertFalse(result.getValue0());
		assertEquals("[M002]Unauthorized access", result.getValue1());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	void testHttpClientSendWithResponseReturningValidCode() throws Exception{
		ArgumentCaptor<HttpRequest> valueCapture = ArgumentCaptor.forClass(HttpRequest.class);
		when(client.send(valueCapture.capture(), any(HttpResponse.BodyHandler.class))).thenReturn(response);
		when(response.statusCode()).thenReturn(200);
		when(response.body()).thenReturn("[M007]Valid session[accountno=000000000272760001]");
		
		Triplet<Boolean, String, String> result = impl.authenticate("c739ace0-ac56-41f3-b9c7-a225add955fb");
		
		HttpRequest.BodyPublisher bodyPublisher = valueCapture.getValue().bodyPublisher().get();
		FlowSubscriber<ByteBuffer> flowSubscriber = new FlowSubscriber<>();
	    bodyPublisher.subscribe(flowSubscriber);
		
	    byte[] actual = flowSubscriber.getBodyItems().get(0).array();
	    assertArrayEquals("sessionkey=c739ace0-ac56-41f3-b9c7-a225add955fb".getBytes(), actual);
		
		assertTrue(result.getValue0());
		assertEquals("[M003]Authorized[accountno=000000000272760001]", result.getValue1());
		assertEquals("c739ace0-ac56-41f3-b9c7-a225add955fb", result.getValue2());
	}

}


/**
 * This class is to verify the bodypublisher content
 * ref: https://stackoverflow.com/questions/59342963/how-to-test-java-net-http-java-11-requests-bodypublisher
 * @author richard
 *
 * @param <T>
 */
class FlowSubscriber<T> implements Flow.Subscriber<T> {
    private final CountDownLatch latch = new CountDownLatch(1);
    private List<T> bodyItems = new ArrayList<>();

    public List<T> getBodyItems() {
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return bodyItems;
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        //Retrieve all parts
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(T item) {
        this.bodyItems.add(item);
    }

    @Override
    public void onError(Throwable throwable) {
        this.latch.countDown();
    }

    @Override
    public void onComplete() {
        this.latch.countDown();
    }
}
