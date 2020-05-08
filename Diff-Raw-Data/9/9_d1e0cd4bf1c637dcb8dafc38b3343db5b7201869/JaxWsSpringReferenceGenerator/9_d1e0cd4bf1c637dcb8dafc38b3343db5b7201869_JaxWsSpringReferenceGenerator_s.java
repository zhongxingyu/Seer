 /*******************************************************************************
  * Copyright (c) 2009 SOPERA GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SOPERA GmbH - initial API and implementation
  *******************************************************************************/
 package org.eclipse.swordfish.tooling.ui.wizards.generators;
 
 import java.text.MessageFormat;
 import java.util.List;
 
 import javax.xml.transform.TransformerException;
 
 import org.eclipse.swordfish.tooling.ui.helper.ConsumerProjectInformationUtil;
 import org.eclipse.swordfish.tooling.ui.wizards.generators.data.ConsumerProjectInformation;
 import org.w3c.dom.Element;
 
 public class JaxWsSpringReferenceGenerator {
 	public static final String XML_CLOSE_COMMENT = " -->";
 	public static final String XML_OPEN_COMMENT = "<!-- ";
 
 	private static final String LINE = "\t\t<spring:property name=\"{1}\" ref=\"{0}\"/>\n";
 	
 	public String generate(List<ConsumerProjectInformation> infos) {
 		StringBuffer sb = new StringBuffer();
 		
 		if (infos != null) {
 			for (ConsumerProjectInformation info : infos) {
 				sb.append(generate(info));
 			}
 			
 			if (sb.length() > 0) {
 				sb.insert(0, XML_OPEN_COMMENT);
 				sb.append(XML_CLOSE_COMMENT);
 			}
 		}
 		
 		return sb.toString();
 	}
 	
 	
 	public String generate(ConsumerProjectInformation info) {
 		String result = "";
 
 		if (hasGenerationContent(info)) {
 			try {
 				result = generate(info.getJaxWsClientElement());
 				
 			} catch (TransformerException e) {
 				throw new IllegalArgumentException("JAXWS client info invalid " + info, e);
 			}
 		}
 
 		return result;
 	}
 
 
 	private String generate(Element jaxWsClientElement) throws TransformerException {
 		String id =	getClientID(jaxWsClientElement);
		return MessageFormat.format(LINE, deCaptitalize(id), id);
 	}
 	
 	
 	private String deCaptitalize(String str) {
 		return str.substring(0, 1).toLowerCase() + str.substring(1);
 	}
 
 	
 	String getClientID(Element jaxWsClientElement) {
 		return (jaxWsClientElement != null) ? jaxWsClientElement.getAttribute("id") : null;
 	}
 
 	
 	boolean hasGenerationContent(ConsumerProjectInformation info) {
 		return (info != null)
 			&& (info.getJaxWsClientElement() != null) 
 			&& (ConsumerProjectInformationUtil.JAXWS_CLIENT_TAG_NAME.equals(info.getJaxWsClientElement().getNodeName())
 			&& (getClientID(info.getJaxWsClientElement()) != null)
 			&& (!"".equals(getClientID(info.getJaxWsClientElement()))));
 	}
 
 }
