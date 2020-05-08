 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 13th September 2009
  */
 
 package au.edu.uts.eng.remotelabs.rigclient.protocol.types;
 
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.apache.axiom.om.OMConstants;
 import org.apache.axiom.om.OMDataSource;
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMFactory;
 import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
 import org.apache.axis2.databinding.ADBBean;
 import org.apache.axis2.databinding.ADBDataSource;
 import org.apache.axis2.databinding.ADBException;
 import org.apache.axis2.databinding.utils.BeanUtil;
 import org.apache.axis2.databinding.utils.ConverterUtil;
 import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReader;
 import org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl;
 import org.apache.axis2.databinding.utils.writer.MTOMAwareXMLStreamWriter;
 
 /**
  * ConfigPropertyTypeEnum bean class.
  */
 public class ConfigPropertyTypeEnum implements ADBBean
 {
     private static final long serialVersionUID = 8183610784692110023L;
 
     public static final QName MY_QNAME = new QName("http://remotelabs.eng.uts.edu.au/rigclient/protocol", "type_type3",
             "ns1");
     
    private static HashMap<String, ConfigPropertyTypeEnum> _table_ = new HashMap<String, ConfigPropertyTypeEnum>();
    
     public static final String _STRING = ConverterUtil.convertToString("STRING");
     public static final String _INTEGER = ConverterUtil.convertToString("INTEGER");
     public static final String _FLOAT = ConverterUtil.convertToString("FLOAT");
     public static final String _BOOLEAN = ConverterUtil.convertToString("BOOLEAN");
     public static final String _CHAR = ConverterUtil.convertToString("CHAR");
 
     public static final ConfigPropertyTypeEnum STRING = new ConfigPropertyTypeEnum(ConfigPropertyTypeEnum._STRING, true);
     public static final ConfigPropertyTypeEnum INTEGER = new ConfigPropertyTypeEnum(ConfigPropertyTypeEnum._INTEGER,
             true);
     public static final ConfigPropertyTypeEnum FLOAT = new ConfigPropertyTypeEnum(ConfigPropertyTypeEnum._FLOAT, true);
     public static final ConfigPropertyTypeEnum BOOLEAN = new ConfigPropertyTypeEnum(ConfigPropertyTypeEnum._BOOLEAN,
             true);
     public static final ConfigPropertyTypeEnum CHAR = new ConfigPropertyTypeEnum(ConfigPropertyTypeEnum._CHAR, true);
 
     private static String generatePrefix(final String namespace)
     {
         if (namespace.equals("http://remotelabs.eng.uts.edu.au/rigclient/protocol"))
         {
             return "ns1";
         }
         return BeanUtil.getUniquePrefix();
     }
 
     protected String type;
    
 
     protected ConfigPropertyTypeEnum(final String value, final boolean isRegisterValue)
     {
         this.type = value;
         if (isRegisterValue)
         {
             ConfigPropertyTypeEnum._table_.put(this.type, this);
         }
     }
 
     @Override
     public boolean equals(final Object obj)
     {
         return (obj == this);
     }
 
     public OMElement getOMElement(final QName parentQName, final OMFactory factory) throws ADBException
     {
         final OMDataSource dataSource = new ADBDataSource(this, ConfigPropertyTypeEnum.MY_QNAME)
         {
             @Override
             public void serialize(final MTOMAwareXMLStreamWriter xmlWriter) throws XMLStreamException
             {
                 ConfigPropertyTypeEnum.this.serialize(ConfigPropertyTypeEnum.MY_QNAME, factory, xmlWriter);
             }
         };
         return new OMSourcedElementImpl(ConfigPropertyTypeEnum.MY_QNAME, factory, dataSource);
     }
 
     @Override
     public XMLStreamReader getPullParser(final QName qName) throws ADBException
     {
         return new ADBXMLStreamReaderImpl(ConfigPropertyTypeEnum.MY_QNAME, new Object[] {
                 ADBXMLStreamReader.ELEMENT_TEXT, ConverterUtil.convertToString(this.type) }, null);
     }
 
     public String getValue()
     {
         return this.type;
     }
 
     @Override
     public int hashCode()
     {
         return this.toString().hashCode();
     }
 
     private String registerPrefix(final XMLStreamWriter xmlWriter, final String namespace) throws XMLStreamException
     {
         String prefix = xmlWriter.getPrefix(namespace);
         if (prefix == null)
         {
             prefix = ConfigPropertyTypeEnum.generatePrefix(namespace);
             while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null)
             {
                 prefix = BeanUtil.getUniquePrefix();
             }
             xmlWriter.writeNamespace(prefix, namespace);
             xmlWriter.setPrefix(prefix, namespace);
         }
         return prefix;
     }
 
     @Override
     public void serialize(final QName parentQName, final OMFactory factory, final MTOMAwareXMLStreamWriter xmlWriter)
             throws XMLStreamException, ADBException
     {
         this.serialize(parentQName, factory, xmlWriter, false);
     }
 
     @Override
     public void serialize(final QName parentQName, final OMFactory factory, final MTOMAwareXMLStreamWriter xmlWriter,
             final boolean serializeType) throws XMLStreamException, ADBException
     {
         final String namespace = parentQName.getNamespaceURI();
         final String localName = parentQName.getLocalPart();
         if (!namespace.equals(""))
         {
             String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null)
             {
                 prefix = ConfigPropertyTypeEnum.generatePrefix(namespace);
                 xmlWriter.writeStartElement(prefix, localName, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             else
             {
                 xmlWriter.writeStartElement(namespace, localName);
             }
         }
         else
         {
             xmlWriter.writeStartElement(localName);
         }
 
         if (serializeType)
         {
             final String namespacePrefix = this.registerPrefix(xmlWriter,
                     "http://remotelabs.eng.uts.edu.au/rigclient/protocol");
             if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0))
             {
                 this.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", namespacePrefix
                         + ":type_type3", xmlWriter);
             }
             else
             {
                 this.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type", "type_type3", xmlWriter);
             }
         }
 
         if (this.type == null)
         {
             throw new ADBException("Value cannot be null !!");
         }
         else
         {
             xmlWriter.writeCharacters(this.type);
         }
         xmlWriter.writeEndElement();
     }
 
     @Override
     public String toString()
     {
         return this.type.toString();
     }
     
     public static boolean isReaderMTOMAware(final XMLStreamReader reader)
     {
         boolean isReaderMTOMAware = false;
         try
         {
             isReaderMTOMAware = Boolean.TRUE.equals(reader.getProperty(OMConstants.IS_DATA_HANDLERS_AWARE));
         }
         catch (final IllegalArgumentException e)
         {
             isReaderMTOMAware = false;
         }
         return isReaderMTOMAware;
     }
 
     private void writeAttribute(final String prefix, final String namespace, final String attName,
             final String attValue, final XMLStreamWriter xmlWriter) throws XMLStreamException
     {
         if (xmlWriter.getPrefix(namespace) == null)
         {
             xmlWriter.writeNamespace(prefix, namespace);
             xmlWriter.setPrefix(prefix, namespace);
         }
         xmlWriter.writeAttribute(namespace, attName, attValue);
     }
 
     public static class Factory
     {
         public static ConfigPropertyTypeEnum fromString(final String value, final String namespaceURI)
                 throws IllegalArgumentException
         {
             try
             {
                 return Factory.fromValue(ConverterUtil.convertToString(value));
             }
             catch (final Exception e)
             {
                 throw new IllegalArgumentException();
             }
         }
 
         public static ConfigPropertyTypeEnum fromString(final XMLStreamReader xmlStreamReader, final String content)
         {
             if (content.indexOf(":") > -1)
             {
                 final String prefix = content.substring(0, content.indexOf(":"));
                 final String namespaceUri = xmlStreamReader.getNamespaceContext().getNamespaceURI(prefix);
                 return ConfigPropertyTypeEnum.Factory.fromString(content, namespaceUri);
             }
             else
             {
                 return ConfigPropertyTypeEnum.Factory.fromString(content, "");
             }
         }
 
         public static ConfigPropertyTypeEnum fromValue(final String value) throws IllegalArgumentException
         {
             final ConfigPropertyTypeEnum enumeration = ConfigPropertyTypeEnum._table_.get(value);
             if (enumeration == null)
                 throw new IllegalArgumentException();
             return enumeration;
         }
 
 
         public static ConfigPropertyTypeEnum parse(final XMLStreamReader reader) throws Exception
         {
             ConfigPropertyTypeEnum object = null;
 
             String prefix = "";
             String namespaceuri = "";
             try
             {
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 while (!reader.isEndElement())
                 {
                     if (reader.isStartElement() || reader.hasText())
                     {
                         final String content = reader.getElementText();
                         if (content.indexOf(":") > 0)
                         {
                             prefix = content.substring(0, content.indexOf(":"));
                             namespaceuri = reader.getNamespaceURI(prefix);
                             object = ConfigPropertyTypeEnum.Factory.fromString(content, namespaceuri);
                         }
                         else
                         {
                             object = ConfigPropertyTypeEnum.Factory.fromString(content, "");
                         }
                     }
                     else
                     {
                         reader.next();
                     }
                 }
             }
             catch (final XMLStreamException e)
             {
                 throw new Exception(e);
             }
 
             return object;
         }
     }
 }
