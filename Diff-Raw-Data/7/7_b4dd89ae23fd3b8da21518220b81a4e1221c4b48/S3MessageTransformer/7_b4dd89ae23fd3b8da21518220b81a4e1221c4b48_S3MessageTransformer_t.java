 package com.opencredo.integration.s3;
 
 
 import org.jets3t.service.model.S3Object;
 import org.springframework.integration.core.Message;
import org.springframework.integration.core.MessagingException;
 
 public class S3MessageTransformer {
 
 
 	public Message<S3Object> transform(Message<S3Object> s3Object) {
 		//TODO: transform the message with metadata to message with real content
 		try{
 			
 		} 
 		catch (Exception e) {
			throw new MessagingException(s3Object, "failed to transform  Message", e);
 		}
 		return null;
 	}
 
 }
