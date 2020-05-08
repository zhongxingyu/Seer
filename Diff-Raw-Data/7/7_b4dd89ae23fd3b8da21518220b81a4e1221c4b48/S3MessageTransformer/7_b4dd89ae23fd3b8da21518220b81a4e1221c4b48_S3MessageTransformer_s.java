 package com.opencredo.integration.s3;
 
import java.io.File;
 
 import org.jets3t.service.model.S3Object;
 import org.springframework.integration.core.Message;
import org.springframework.integration.file.transformer.AbstractFilePayloadTransformer;
 
 public class S3MessageTransformer {
 
 
 	public Message<S3Object> transform(Message<S3Object> s3Object) {
 		//TODO: transform the message with metadata to message with real content
 		try{
 			
 		} 
 		catch (Exception e) {
			throw new MessagingException(message, "failed to transform  Message", e);
 		}
 		return null;
 	}
 
 }
