 /*
  * jets3t : Java Extra-Tasty S3 Toolkit (for Amazon S3 online storage service)
  * This is a java.net project, see https://jets3t.dev.java.net/
  * 
  * Copyright 2006 James Murty
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package org.jets3t.service;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Exception for use by <code>S3Service</code>s and related utilities.
  * This exception can hold useful additional information about errors that occur
  * when communicating with S3.
  *  
  * @author James Murty
  */
 public class S3ServiceException extends Exception {
     private static final long serialVersionUID = 4788682643946844474L;
 
     private String xmlMessage = null;
     
     // Fields from S3 error messages.
     private String s3ErrorCode = null;
     private String s3ErrorMessage = null;
     private String s3ErrorRequestId = null;
     private String s3ErrorHostId = null;
 
     /**
      * Constructor that includes the XML error document returned by S3.      
      * @param message
      * @param xmlMessage
      */
 	public S3ServiceException(String message, String xmlMessage) {
 		super(message);
         parseS3XmlMessage(xmlMessage);
 	}
 
 	public S3ServiceException() {
 		super();
 	}
 	
 	public S3ServiceException(String message, Throwable cause) {
 		super(message, cause);
 	}
 
 	public S3ServiceException(String message) {
 		super(message);
 	}
 
 	public S3ServiceException(Throwable cause) {
 		super(cause);
 	}
 	
 	public String toString() {
 		return super.toString() + (xmlMessage != null? " XML Error Message: " + xmlMessage: "");
 	}
     
     private String findXmlElementText(String xmlMessage, String elementName) {
         Pattern pattern = Pattern.compile(".*<" + elementName + ">(.*)</" + elementName + ">.*");
         Matcher matcher = pattern.matcher(xmlMessage);
         if (matcher.matches() && matcher.groupCount() == 1) {
             return matcher.group(1);
         } else {
             return null;
         }
     }
     
     private void parseS3XmlMessage(String xmlMessage) {
         xmlMessage = xmlMessage.replaceAll("\n", "");
         this.xmlMessage = xmlMessage;
  
         this.s3ErrorCode = findXmlElementText(xmlMessage, "Code");
         this.s3ErrorMessage = findXmlElementText(xmlMessage, "Message");
         this.s3ErrorRequestId = findXmlElementText(xmlMessage, "RequestId");
         this.s3ErrorHostId = findXmlElementText(xmlMessage, "HostId");
     }
 	
     /**
      * @return The Error Code returned by S3, if this exception was created with the 
      * XML Message constructor.
      */
 	public String getS3ErrorCode() {
         return this.s3ErrorCode;
 	}
     
     /**
      * @return The Error Message returned by S3, if this exception was created with the 
      * XML Message constructor.
      */
     public String getS3ErrorMessage() {
         return this.s3ErrorMessage;
     }
 
     /**
      * @return The Error Host ID returned by S3, if this exception was created with the 
      * XML Message constructor.
      */
     public String getS3ErrorHostId() {
         return s3ErrorHostId;
     }
 
     /**
      * @return The Error Request ID returned by S3, if this exception was created with the 
      * XML Message constructor.
      */
     public String getS3ErrorRequestId() {
         return s3ErrorRequestId;
     }
         
 }
