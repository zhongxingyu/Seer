 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.connection.xmlrpc.util;
 
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 
 import org.apache.ws.commons.util.Base64;
 import org.apache.ws.commons.util.Base64.Encoder;
 import org.apache.ws.commons.util.Base64.EncoderOutputStream;
 import org.apache.xmlrpc.serializer.TypeSerializerImpl;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.common.model.util.SerializationException;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 /**
  * Serializer for EObjects.
  * 
  * @author emueller
  */
 public class EObjectSerializer extends TypeSerializerImpl {
 
 	/**
 	 * EObject Tag for parsing.
 	 */
 	public static final String EOBJECT_TAG = "EObject";
 	private static final String EX_EOBJECT_TAG = "ex:" + EOBJECT_TAG;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void write(ContentHandler pHandler, Object pObject) throws SAXException {
 		pHandler.startElement("", VALUE_TAG, VALUE_TAG, ZERO_ATTRIBUTES);
 		pHandler.startElement("", EOBJECT_TAG, EX_EOBJECT_TAG, ZERO_ATTRIBUTES);
 		char[] buffer = new char[1024];
 		Encoder encoder = new Base64.SAXEncoder(buffer, 0, null, pHandler);
 		try {
 			OutputStream ostream = new EncoderOutputStream(encoder);
 			BufferedOutputStream bos = new BufferedOutputStream(ostream);
 			OutputStreamWriter writer = new OutputStreamWriter(bos);
 			try {
				if (pObject instanceof ChangePackage) {
 					ModelUtil.eobjectToString(writer, (EObject) pObject, true, true, true);
 				} else {
 					bos.write(ModelUtil.eObjectToString((EObject) pObject).getBytes());
 				}
 			} catch (SerializationException e) {
 				throw new SAXException("Couldn't serialize EObject", e);
 			} finally {
 				bos.close();
 			}
 		} catch (Base64.SAXIOException e) {
 			throw e.getSAXException();
 		} catch (IOException e) {
 			throw new SAXException(e);
 		}
 		pHandler.endElement("", EOBJECT_TAG, EX_EOBJECT_TAG);
 		pHandler.endElement("", VALUE_TAG, VALUE_TAG);
 	}
 }
