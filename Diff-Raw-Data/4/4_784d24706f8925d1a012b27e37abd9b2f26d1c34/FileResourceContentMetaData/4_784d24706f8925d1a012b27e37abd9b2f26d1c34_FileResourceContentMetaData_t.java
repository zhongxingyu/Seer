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
 package com.delcyon.capo.resourcemanager.types;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.math.BigInteger;
 import java.net.URI;
 
 import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
 import com.delcyon.capo.resourcemanager.ResourceManager;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
 
 /**
  * @author jeremiah
  *
  */
 public class FileResourceContentMetaData extends AbstractContentMetaData
 {
 	
 	@SuppressWarnings("unused")
 	private FileResourceContentMetaData() //serialization only
 	{
 		
 	}
 	
 	public FileResourceContentMetaData(String uri, ResourceParameter... resourceParameters) throws Exception
 	{
 		init(uri,0,resourceParameters);
 	}
 	
 	public FileResourceContentMetaData(String uri, int currentDepth, ResourceParameter... resourceParameters) throws Exception
 	{
 		init(uri,currentDepth,resourceParameters);
 	}
 	
 	@Override
 	public Attributes[] getAdditionalSupportedAttributes()
 	{
 		return new Attributes[]{Attributes.exists,Attributes.executable,Attributes.readable,Attributes.writeable,Attributes.container,Attributes.lastModified};
 	}
 
 	
 	
 	public void refresh(String uri) throws Exception
 	{
 		init(uri,0);
 	}
 	
 	private void init(String uri,int currentDepth, ResourceParameter... resourceParameters) throws Exception
 	{
 		if (getBoolean(Parameters.USE_RELATIVE_PATHS,false,resourceParameters))
 		{
 			if (getString(Attributes.path,null,resourceParameters) == null)
 			{
 				ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
 				resourceParameterBuilder.addAll(resourceParameters);
 				resourceParameterBuilder.addParameter(Attributes.path, uri.toString());
 				resourceParameters = resourceParameterBuilder.getParameters();
 			}
 			else
 			{
 				String uriString = uri.toString();
 				uriString = uriString.replaceFirst(getString(Attributes.path,null,resourceParameters),"");
 				setResourceURI(ResourceManager.removeURN(uriString));
 			}
 		}
 		else
 		{
 			setResourceURI(uri);
 		}
 		File file = new File(new URI(uri));
 		getAttributeMap().put(Attributes.exists.toString(), file.exists()+"");
 
 		getAttributeMap().put(Attributes.executable.toString(), file.canExecute()+"");
 
 		getAttributeMap().put(Attributes.readable.toString(), file.canRead()+"");
 
 		getAttributeMap().put(Attributes.writeable.toString(), file.canWrite()+"");
 
 		getAttributeMap().put(Attributes.container.toString(), file.isDirectory()+"");
 
 		getAttributeMap().put(Attributes.lastModified.toString(), file.lastModified()+"");
 		
 		
 		
 		if (file.exists() == true && file.canRead() == true && file.isDirectory() == false)
 		{
		    FileInputStream fileInputStream = new FileInputStream(file);
			readInputStream(fileInputStream);
			fileInputStream.close();
 		}
 		else if (file.isDirectory() == true && getIntValue(com.delcyon.capo.controller.elements.ResourceMetaDataElement.Attributes.depth,1,resourceParameters) > currentDepth)
 		{	
 			BigInteger contentMD5 = new BigInteger(new byte[]{0});
 			for (String childURI : file.list())
 			{
 				File childFile = new File(file,childURI);				
 				String tempChildURI = childFile.toURI().toString();
 				if (tempChildURI.endsWith(File.separator))
 	            {
 				    tempChildURI = tempChildURI.substring(0, tempChildURI.length()-File.separator.length());
 	            }
 				FileResourceContentMetaData contentMetaData = new FileResourceContentMetaData(tempChildURI, currentDepth+1,resourceParameters);
 				if (contentMetaData.getMD5() != null)
 				{
 					contentMD5 = contentMD5.add(new BigInteger(contentMetaData.getMD5(), 16));
 				}
 				addContainedResource(contentMetaData);
 			}
 			getAttributeMap().put(MD5FilterInputStream.ATTRIBUTE_NAME, contentMD5.abs().toString(16));
 		}		
 		setInitialized(true);
 		
 	}
 	
 
 	
 
 	
 
 	@Override
 	public Boolean exists()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.exists.toString()));
 	}
 
 	@Override
 	public Long getLastModified()
 	{		
 		return Long.parseLong(getAttributeMap().get(Attributes.lastModified.toString()));
 	}
 
 	@Override
 	public Boolean isContainer()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.container.toString()));
 	}
 
 	@Override
 	public Boolean isReadable()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.readable.toString()));
 	}
 
 	@Override
 	public Boolean isWriteable()
 	{
 		return Boolean.parseBoolean(getAttributeMap().get(Attributes.writeable.toString()));
 	}
 
 	
 
 }
