 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.forge.validation.scaffold.configurator;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.jboss.forge.project.Project;
 import org.jboss.forge.project.facets.WebResourceFacet;
 import org.jboss.forge.shell.ShellPrompt;
 import org.jboss.forge.validation.api.scaffold.ScaffoldConfigurator;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @author Kevin Pollet
  */
 public class MetawidgetConfigurator implements ScaffoldConfigurator
 {
     @Override
     public void addValidationConfiguration(Project project, ShellPrompt prompt)
     {
         if (isMetawidgetScaffold(project))
         {
             if (prompt.promptBoolean("MetaWidget scaffold detected would you like to add validation configuration?"))
             {
                 try
                 {
                     final File metawidgetConfigFile = getMetawidgetConfigurationFile(project);
                     final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                     final DocumentBuilder builder = factory.newDocumentBuilder();
                     final Document document = builder.parse(metawidgetConfigFile);
 
                     if (document.getElementsByTagName("beanValidationInspector").getLength() == 0)
                     {
                         final Element beanValidationInspector = document.createElement("beanValidationInspector");
                         beanValidationInspector.setAttribute("xmlns", "java:org.metawidget.inspector.beanvalidation");
 
                         final Element propertyStyle = document.createElement("propertyStyle");
                         beanValidationInspector.appendChild(propertyStyle);
 
                         final Element javaBeanPropertyStyle = document.createElement("javaBeanPropertyStyle");
                         javaBeanPropertyStyle.setAttribute("xmlns", "java:org.metawidget.inspector.impl.propertystyle.javabean");
                        javaBeanPropertyStyle.setAttribute("config", "java:org.metawidget.inspector.impl.propertystyle BeanPropertyStyleConfig");
                         propertyStyle.appendChild(javaBeanPropertyStyle);
 
                         final Element privateFieldConvention = document.createElement("privateFieldConvention");
                         javaBeanPropertyStyle.appendChild(privateFieldConvention);
 
                         final Element format = document.createElement("format");
                         format.setTextContent("{0}");
                         privateFieldConvention.appendChild(format);
 
                         final NodeList nodeList = document.getElementsByTagName("array");
                         nodeList.item(0).appendChild(beanValidationInspector);
 
                         final Transformer transformer = TransformerFactory.newInstance().newTransformer();
                         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
                         final Result result = new StreamResult(metawidgetConfigFile);
                         final Source source = new DOMSource(document);
                         transformer.transform(source, result);
                     }
 
                 } catch (ParserConfigurationException e)
                 {
                     throw new RuntimeException(e);
                 } catch (SAXException e)
                 {
                     throw new RuntimeException(e);
                 } catch (IOException e)
                 {
                     throw new RuntimeException(e);
                 } catch (TransformerConfigurationException e)
                 {
                     throw new RuntimeException(e);
                 } catch (TransformerException e)
                 {
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
     private boolean isMetawidgetScaffold(Project project)
     {
         final WebResourceFacet facet = project.getFacet(WebResourceFacet.class);
         return facet.getWebResource("WEB-INF" + File.separator + "metawidget.xml").exists();
     }
 
     private File getMetawidgetConfigurationFile(Project project)
     {
         final WebResourceFacet facet = project.getFacet(WebResourceFacet.class);
         return facet.getWebResource("WEB-INF" + File.separator + "metawidget.xml").getUnderlyingResourceObject();
     }
 }
