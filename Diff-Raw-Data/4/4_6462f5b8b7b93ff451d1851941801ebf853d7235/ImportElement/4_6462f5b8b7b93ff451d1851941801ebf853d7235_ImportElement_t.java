 /**
 Copyright (C) 2012  Delcyon, Inc.
 
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.delcyon.capo.controller.elements;
 
 import java.io.ByteArrayOutputStream;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.Configuration.PREFERENCE;
 import com.delcyon.capo.controller.AbstractControl;
 import com.delcyon.capo.controller.ControlElementProvider;
 import com.delcyon.capo.datastream.StreamUtil;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamFormat;
 import com.delcyon.capo.resourcemanager.ResourceDescriptor.StreamType;
 import com.delcyon.capo.resourcemanager.ResourceParameter.Source;
 import com.delcyon.capo.resourcemanager.types.FileResourceType;
 import com.delcyon.capo.xml.XPath;
 import com.delcyon.capo.xml.XPathFunctionProcessor;
 import com.delcyon.capo.xml.XPathFunctionProvider;
 import com.delcyon.capo.xml.XPathFunctionUtility;
 
 /**
  * @author jeremiah
  *
  */
 @ControlElementProvider(name="import")
 @XPathFunctionProvider
 public class ImportElement extends AbstractControl implements XPathFunctionProcessor
 {
 
 	
 	
 	private enum Attributes
 	{
 		name,src,type,ref
 	}
 	
 	
 	
 	private static final String[] supportedNamespaces = {CapoApplication.SERVER_NAMESPACE_URI};
 
 	private static final String[] functionNames = {"import"};
 	
 	
 	@Override
 	public Attributes[] getAttributes()
 	{
 		return Attributes.values();
 	}
 	
 	@Override
 	public Attributes[] getRequiredAttributes()
 	{
 		return new Attributes[]{Attributes.name};
 	}
 	
 	@Override
 	public String[] getSupportedNamespaces()
 	{
 		return supportedNamespaces;
 	}
 
 	@Override
 	public String[] getXPathFunctionNames()
 	{
 		return functionNames;
 	}
 	
 	@Override
 	public Object processFunction(String functionName, Object... arguments) throws Exception
 	{
 		Node contextNode = getContextNode();
 		String prefix = XPathFunctionUtility.getPrefix(contextNode, arguments, 1);
 		if (functionName.equals("import"))
 		{
 			return XPath.selectSingleNode(contextNode, "//"+prefix+"import[@name = '"+arguments[0]+"']");
 		}
 		else
 		{
 			return null;
 		}
 	}
 	
 
 	@Override
 	public Object processServerSideElement() throws Exception
 	{
 		ResourceParameter[] resourceParameters = ResourceParameterBuilder.getResourceParameters(getControlElementDeclaration());
 		String src = getAttributeValue(Attributes.src);		
 		String type = getAttributeValue(Attributes.type);		
 		String ref = getAttributeValue(Attributes.ref);		
 		if (type == null || type.isEmpty())
 		{
 			type = "xml";
 		}
 		
 		ResourceDescriptor resourceDescriptor = getParentGroup().getResourceDescriptor(this, src);
		if (resourceDescriptor == null)
		{
			throw new Exception("src="+src+" not found");
		}
 		resourceDescriptor.addResourceParameters(getParentGroup(), new ResourceParameter(FileResourceType.Parameters.PARENT_PROVIDED_DIRECTORY,PREFERENCE.RESOURCE_DIR,Source.CALL));
 		
 		Document importedDocument = null;
 
 
 		if (type.equalsIgnoreCase("xml"))
 		{
 		    if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.XML_BLOCK))
 		    {
 		        importedDocument = CapoApplication.getDocumentBuilder().newDocument();
 		        Element readElement = resourceDescriptor.readXML(getParentGroup(),resourceParameters);
 		        importedDocument.adoptNode(readElement);
 		        importedDocument.appendChild(readElement);
 		    }
 		    else if (resourceDescriptor.isSupportedStreamFormat(StreamType.INPUT, StreamFormat.BLOCK))
 		    {
 		        importedDocument = CapoApplication.getDocumentBuilder().newDocument();
 		        importedDocument.appendChild(importedDocument.createElement("text"));
 		        ByteArrayOutputStream bufferByteArrayOutputStream = new ByteArrayOutputStream();
 		        StreamUtil.readInputStreamIntoOutputStream(resourceDescriptor.getInputStream(getParentGroup(),resourceParameters), bufferByteArrayOutputStream);
 		        importedDocument.getDocumentElement().setTextContent(bufferByteArrayOutputStream.toString());
 		    }
 		    else
 		    {
 		        importedDocument = CapoApplication.getDocumentBuilder().parse(resourceDescriptor.getInputStream(getParentGroup(),resourceParameters));
 		    }
 		}
 		else if (type.equalsIgnoreCase("text"))
 		{
 		    importedDocument = CapoApplication.getDocumentBuilder().newDocument();
 		    importedDocument.appendChild(importedDocument.createElement("text"));
 		    ByteArrayOutputStream bufferByteArrayOutputStream = new ByteArrayOutputStream(resourceDescriptor.getContentMetaData(getParentGroup(),resourceParameters).getLength().intValue());
 		    StreamUtil.readInputStreamIntoOutputStream(resourceDescriptor.getInputStream(getParentGroup(),resourceParameters), bufferByteArrayOutputStream);
 		    importedDocument.getDocumentElement().setTextContent(bufferByteArrayOutputStream.toString());
 		}
 		//cleanup and parameters we added when calling this.
 		//resourceDescriptor.close(this);
 
 		
 		if (importedDocument != null)
 		{
 			Element parentElement = getControlElementDeclaration();
 			if (ref != null && ref.isEmpty() == false)
 			{
 				parentElement = (Element) XPath.selectSingleNode(getControlElementDeclaration(), ref);
 			}			
 			parentElement.appendChild(getControlElementDeclaration().getOwnerDocument().importNode(importedDocument.getDocumentElement(),true));			
 		}
 		
 		
 		return null;
 	}
 
 	
 }
