 package com.pance.springIntSpike;
 
 import org.springframework.integration.annotation.MessageEndpoint;
 import org.springframework.integration.annotation.ServiceActivator;
 
 @MessageEndpoint
 public class FooMessageService {
 
 	@ServiceActivator(inputChannel="foo", outputChannel="bar")

	@Polled(period=300)
 	public String getMessage(String message) {
 		System.out.println("Foo: " + message);
 		return message;
 	}
 
 }
