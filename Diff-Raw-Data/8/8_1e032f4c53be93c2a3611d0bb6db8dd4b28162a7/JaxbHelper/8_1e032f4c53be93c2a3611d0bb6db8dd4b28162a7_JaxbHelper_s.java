 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.PropertyException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
import org.zend.sdklib.repository.site.Site;
 import org.zend.sdklib.descriptor.pkg.Package;
 
 /**
  * Helps marshal and un-marshal using jaxb APIs
  * 
  * @author Roy, 2011
  */
 public class JaxbHelper {
 
 	private static final String ORG_ZEND_SDKLIB_REPOSITORY_SITE = "org.zend.sdklib.repository.site";
 	private static final String ORG_ZEND_SDKLIB_DESCRIPTOR_PKG = "org.zend.sdklib.descriptor.pkg";
 
 	/**
 	 * Takes a stream which is a site.xml and converts to Site object
 	 * 
 	 * @param siteStream
 	 * @return
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public static Site unmarshalSite(InputStream siteStream)
 			throws IOException, JAXBException {
 		Source source = new StreamSource(siteStream);
 		JAXBContext jc = JAXBContext
 				.newInstance(ORG_ZEND_SDKLIB_REPOSITORY_SITE);
 		Unmarshaller u = jc.createUnmarshaller();
 		Site site = (Site) u.unmarshal(source);
 		return site;
 	}
 
 	/**
 	 * Takes a Site object and converts to xml
 	 * 
 	 * @param printStream
 	 * @param s
 	 * @throws JAXBException
 	 * @throws PropertyException
 	 */
 	public static void marshalSite(PrintStream printStream, final Site s)
 			throws JAXBException, PropertyException {
 		JAXBContext jc = JAXBContext
 				.newInstance(ORG_ZEND_SDKLIB_REPOSITORY_SITE);
 		Marshaller m = jc.createMarshaller();
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		m.marshal(s, printStream);
 	}
 
 	/**
 	 * @param pkgStream
 	 * @return
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public static Package unmarshalPackage(InputStream pkgStream)
 			throws IOException, JAXBException {
 		Source source = new StreamSource(pkgStream);
 		JAXBContext jc = JAXBContext
 				.newInstance(ORG_ZEND_SDKLIB_DESCRIPTOR_PKG);
 		Unmarshaller u = jc.createUnmarshaller();
 		Package pkg = (Package) u.unmarshal(source);
 		return pkg;
 	}
 
 	/**
 	 * @param printStream
 	 * @param p
 	 * @throws JAXBException
 	 * @throws PropertyException
 	 */
	public static void marshalPackage(PrintStream printStream, final Package p)
 			throws JAXBException, PropertyException {
 		JAXBContext jc = JAXBContext
 				.newInstance(ORG_ZEND_SDKLIB_DESCRIPTOR_PKG);
 		Marshaller m = jc.createMarshaller();
 		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		m.marshal(p, printStream);
 	}
 }
