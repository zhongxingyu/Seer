 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.protocol;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 import org.apache.activemq.util.ByteArrayInputStream;
 import org.bitrepository.common.JaxbHelper;
 
 /** Used to create message objects based on the example xml found in the message-xml module. */
 public class ExampleMessageFactory {
     public static final String XML_MESSAGE_DIR = "target/";
     
     public static <T> T createMessage(Class<T> messageType) throws Exception {
         String xmlMessage = loadXMLExample(messageType.getSimpleName());
        String schemaLocation = "xsd/BitRepositoryMessages.xsd";
         JaxbHelper jaxbHelper = new JaxbHelper(schemaLocation);
         jaxbHelper.validate(new ByteArrayInputStream(xmlMessage.getBytes()));
         return jaxbHelper.loadXml(messageType,
                 new ByteArrayInputStream(xmlMessage.getBytes()));
     }
 
     /**
      * Loads the example XML for the indicated message. Assumes the XML examples are found under the
      * XML_MESSAGE_DIR/examples directory, and the naming convention for the example files are '${messagename}.xml'
      *
      * @param messageName
      * @return
      */
     private static String loadXMLExample(String messageName) throws Exception {
         String filePath = XML_MESSAGE_DIR + "examples/messages/" + messageName + ".xml";
         byte[] buffer = new byte[(int) new File(filePath).length()];
         FileInputStream f = new FileInputStream(filePath);
         f.read(buffer);
         return new String(buffer);
     }
 }
