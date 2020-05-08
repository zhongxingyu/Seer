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
 
 import java.io.ByteArrayOutputStream;
 import java.io.FilterInputStream;
 import java.io.FilterOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.datastream.NullOutputStream;
 import com.delcyon.capo.datastream.OutputStreamAttributeFilterProvider;
 import com.delcyon.capo.datastream.StreamUtil;
 import com.delcyon.capo.datastream.stream_attribute_filter.InputStreamAttributeFilterProvider;
 import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.StreamAttributeFilter;
 import com.delcyon.capo.resourcemanager.ContentFormatType;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 import com.delcyon.capo.util.CloneControl;
 import com.delcyon.capo.util.CloneControl.Clone;
 import com.delcyon.capo.util.ReflectionUtility;
 import com.delcyon.capo.util.ToStringControl;
 import com.delcyon.capo.util.ToStringControl.Control;
 
 /**
  * @author jeremiah
  *
  */
 @SuppressWarnings("unchecked")
 @ToStringControl(control=Control.exclude,modifiers=Modifier.STATIC)
 public abstract class AbstractContentMetaData implements ContentMetaData
 {
 
 	private static Vector<Class> inputStreamAttributeFilterVector = null;
 	static
 	{
 		inputStreamAttributeFilterVector = new Vector<Class>();
 		Set<String> inputStreamAttributeFilterProviderSet = CapoApplication.getAnnotationMap().get(InputStreamAttributeFilterProvider.class.getCanonicalName());
 		if (inputStreamAttributeFilterProviderSet != null) //this is only null during testing
 		{
 			for (String className : inputStreamAttributeFilterProviderSet)
 			{
 				try
 				{
 					Class inputStreamAttributeFilterProviderClass = Class.forName(className);
 					InputStreamAttributeFilterProvider inputStreamAttributeFilterProvider = (InputStreamAttributeFilterProvider) inputStreamAttributeFilterProviderClass.getAnnotation(InputStreamAttributeFilterProvider.class);
 					if (FilterInputStream.class.isAssignableFrom(inputStreamAttributeFilterProviderClass) == false)
 					{
 						CapoApplication.logger.log(Level.WARNING, inputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a filterInputStream. SKIPPING");
 					}
 					else if (StreamAttributeFilter.class.isAssignableFrom(StreamAttributeFilter.class) == false)
 					{
 						CapoApplication.logger.log(Level.WARNING, inputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a StreamAttributeFilter. SKIPPING");
 					}
 					else
 					{
 						inputStreamAttributeFilterVector.add(inputStreamAttributeFilterProviderClass);
 						CapoApplication.logger.log(Level.CONFIG, "Loaded InputStreamAttributeFilterProvider "+inputStreamAttributeFilterProvider.name()+" from "+className);
 					}
 				} catch (ClassNotFoundException classNotFoundException)
 				{
 					CapoApplication.logger.log(Level.WARNING, "Error getting StreamAttributeFilter providers",classNotFoundException);
 				}
 			}
 		}
 	}
 	
 	private static Vector<Class> outputStreamAttributeFilterVector = null;
 	static
 	{
 		outputStreamAttributeFilterVector = new Vector<Class>();
 		Set<String> outputStreamAttributeFilterProviderSet = CapoApplication.getAnnotationMap().get(OutputStreamAttributeFilterProvider.class.getCanonicalName());
 		if (outputStreamAttributeFilterProviderSet != null) //this is only null during testing, since we have built-in providers
 		{
 			for (String className : outputStreamAttributeFilterProviderSet)
 			{
 				try
 				{
 					Class outputStreamAttributeFilterProviderClass = Class.forName(className);
 					OutputStreamAttributeFilterProvider outputStreamAttributeFilterProvider = (OutputStreamAttributeFilterProvider) outputStreamAttributeFilterProviderClass.getAnnotation(OutputStreamAttributeFilterProvider.class);
 					if (FilterOutputStream.class.isAssignableFrom(outputStreamAttributeFilterProviderClass) == false)
 					{
 						CapoApplication.logger.log(Level.WARNING, outputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a FilterOutputStream. SKIPPING");
 					}
 					else if (StreamAttributeFilter.class.isAssignableFrom(outputStreamAttributeFilterProviderClass) == false)
 					{
 						CapoApplication.logger.log(Level.WARNING, outputStreamAttributeFilterProviderClass.getCanonicalName()+" is not a StreamAttributeFilter. SKIPPING");
 					}
 					else
 					{
 						outputStreamAttributeFilterVector.add(outputStreamAttributeFilterProviderClass);
 						CapoApplication.logger.log(Level.CONFIG, "Loaded OutputStreamAttributeFilterProvider "+outputStreamAttributeFilterProvider.name()+" from "+className);
 					}
 				} catch (ClassNotFoundException classNotFoundException)
 				{
 					CapoApplication.logger.log(Level.WARNING, "Error getting OutputStreamAttributeFilter providers",classNotFoundException);
 				}
 			}
 		}
 	}
 	
 	private boolean includeStreamAttributes = false;
 	private boolean isInitialized = false;
 	private ResourceURI resourceURI = null;
 	private HashMap<String, String> attributeHashMap = new HashMap<String, String>();
 	
 	@CloneControl(filter=Clone.exclude)
 	private transient Vector<StreamAttributeFilter> streamAttributeFilterVector = new Vector<StreamAttributeFilter>();
 	private Vector<ContentMetaData> childContentMetaDataVector = new Vector<ContentMetaData>();
 	
 	/**
 	 * this will try to initialize md5 and length fields by reading the input stream
 	 * If you want to use a byte[] wrap it in a ByteArrayInputStream
 	 * This does WILL close the stream
 	 * @param inputStream
 	 * @throws Exception 
 	 */
 	protected byte[] readInputStream(InputStream inputStream, boolean returnContent) throws Exception
 	{
 	    this.includeStreamAttributes = true;
 	    OutputStream outputStream = null;
 	    if(returnContent == true)
 	    {
 	        outputStream = new ByteArrayOutputStream();
 	    }
 	    else
 	    {
 	        outputStream = new NullOutputStream();
 	    }
 		inputStream =  wrapInputStream(inputStream);
 		StreamUtil.readInputStreamIntoOutputStream(inputStream, outputStream);
 		getAttributeMap(); //once we've read the file, load up he attribute map with any stream attributes.
 		this.isInitialized = true;
 		if (returnContent == true)
 		{
 		    return ((ByteArrayOutputStream) outputStream).toByteArray();
 		}
 		else
 		{
 		    return null;
 		}
 	}
 	
 	/**
 	 * this will try to initialize md5 and length fields by reading the input stream
 	 * If you want to use a byte[] wrap it in a ByteArrayInputStream
 	 * This does NOT close the stream
 	 * @param inputStream
 	 * @throws Exception 
 	 */
 	protected InputStream wrapInputStream(InputStream inputStream) throws Exception
 	{
 	    this.includeStreamAttributes = true;
 		streamAttributeFilterVector.clear();
 		for (Class inputStreamAttributeFilterProviderClass : inputStreamAttributeFilterVector)
 		{			
 			inputStream = (FilterInputStream) inputStreamAttributeFilterProviderClass.getConstructor(InputStream.class).newInstance(inputStream);
 			streamAttributeFilterVector.add((StreamAttributeFilter) inputStream);
 		}		
 		return inputStream;
 	}
 	
 	protected OutputStream wrapOutputStream(OutputStream outputStream) throws Exception
 	{
 	    this.includeStreamAttributes = true;
 		streamAttributeFilterVector.clear();
		attributeHashMap.remove(MD5FilterInputStream.ATTRIBUTE_NAME);
		attributeHashMap.remove(SizeFilterInputStream.ATTRIBUTE_NAME);
		attributeHashMap.remove(ContentFormatType.ATTRIBUTE_NAME);
 		for (Class outputStreamAttributeFilterProviderClass : outputStreamAttributeFilterVector)
 		{			
 			outputStream = (FilterOutputStream) outputStreamAttributeFilterProviderClass.getConstructor(OutputStream.class).newInstance(outputStream);
 			streamAttributeFilterVector.add((StreamAttributeFilter) outputStream);
 		}		
 		return outputStream;
 	}
 	
 	@Override
     public boolean isDynamic()
     {       
         if(includeStreamAttributes == true)
         {
             return false;
         }
         else
         {
             return true;
         }
     }
     
 	
 	@Override
 	public boolean isInitialized()
 	{
 		return this.isInitialized;
 	}
 	
 	@Override
 	public void setInitialized(boolean isInitialized)
 	{
 		this.isInitialized = isInitialized;		
 	}
 	
 	@Override
 	public HashMap<String, String> getAttributeMap()
 	{
 		for (StreamAttributeFilter streamAttributeFilter : streamAttributeFilterVector)
 		{
 			if (attributeHashMap.containsKey(streamAttributeFilter.getName()) == false)
 			{
 				attributeHashMap.put(streamAttributeFilter.getName(), streamAttributeFilter.getValue());
 			}
 		}
 		
 		attributeHashMap.put(Attributes.path.toString(), getResourceURI().getPath());
 		attributeHashMap.put(Attributes.uri.toString(), getResourceURI().getBaseURI());
 		return attributeHashMap;
 	}
 	
 	public ContentFormatType getContentFormatType()
 	{
 		attributeHashMap = getAttributeMap();
 		if (attributeHashMap.containsKey(ContentFormatType.ATTRIBUTE_NAME))
 		{
 			String contentFormatTypeString = attributeHashMap.get(ContentFormatType.ATTRIBUTE_NAME);
 			return ContentFormatType.valueOf(contentFormatTypeString);
 		}
 		else
 		{
 			return null;
 		}
 	}
 
 	public void setContentFormatType(ContentFormatType contentFormatType)
 	{
 		attributeHashMap.put(ContentFormatType.ATTRIBUTE_NAME, contentFormatType.toString());
 	}
 	
 	public String getMD5()
 	{
 		attributeHashMap = getAttributeMap();
 		return attributeHashMap.get(MD5FilterInputStream.ATTRIBUTE_NAME);		
 	}
 	
 	public void setMD5(String md5)
 	{		
 		attributeHashMap.put(MD5FilterInputStream.ATTRIBUTE_NAME, md5);
 	}
 	
 	public Long getLength()
 	{
 		attributeHashMap = getAttributeMap();
 		String length = attributeHashMap.get(SizeFilterInputStream.ATTRIBUTE_NAME);
 		if (length.matches("\\d+"))
 		{
 			return Long.parseLong(length);
 		}
 		return null;
 	}
 	public void setLength(Long length)
 	{
 		attributeHashMap.put(SizeFilterInputStream.ATTRIBUTE_NAME, length+"");
 	}
 	
 	@Override
 	public String getValue(String name)
 	{
 		return getAttributeMap().get(name);
 		
 	}
 	
 	@Override
	public String getValue(Enum name)
	{
	    return getValue(name.toString());
	}
	
	@Override
 	public void setValue(String name, String value)
 	{
 		attributeHashMap.put(name, value);
 		
 	}
 	
 	/**
 	 * Convince Method
 	 * @param enumclass
 	 * @param object
 	 */
 	public void setValue(Enum enumclass, Object object)
 	{
 		if (object != null)
 		{
 			setValue(enumclass.toString(), object.toString());
 		}
 		else
 		{
 			setValue(enumclass.toString(), null);
 		}
 		
 	}
 	
 	public void setValue(String name, Object object)
 	{
 		if (object != null)
 		{
 			setValue(name, object.toString());
 		}
 		else
 		{
 			setValue(name, null);
 		}
 		
 	}
 	
 	@Override
 	public String toString()
 	{
 		return ReflectionUtility.processToString(this);
 	}
 
 	@Override
 	public boolean isSupported(String attributeName)
 	{
 		List<String> supportedAttributeList = getSupportedAttributes();
 		if(includeStreamAttributes == true)
 		{
 		    for (Class inputStreamAttributeFilterProviderClass : inputStreamAttributeFilterVector)
 		    {			
 		        supportedAttributeList.add(((InputStreamAttributeFilterProvider) inputStreamAttributeFilterProviderClass.getAnnotation(InputStreamAttributeFilterProvider.class)).name());
 		    }	
 		}
 		return supportedAttributeList.contains(attributeName);
 	}
 
 	@Override
 	public List<String> getSupportedAttributes()
 	{
 		Vector<String>  supportedAttributeVector = new Vector<String>();
 		Enum[] supportedAttributeList = getAdditionalSupportedAttributes();
 		for (Enum attributeEnum : supportedAttributeList)
 		{
 			supportedAttributeVector.add(attributeEnum.toString());
 		}
 		
 		if(includeStreamAttributes == true)
 		{
 		    for (Class inputStreamAttributeFilterProviderClass : inputStreamAttributeFilterVector)
 		    {			
 		        supportedAttributeVector.add(((InputStreamAttributeFilterProvider) inputStreamAttributeFilterProviderClass.getAnnotation(InputStreamAttributeFilterProvider.class)).name());
 		    }
 		}
 		supportedAttributeVector.add(Attributes.path.toString());
 		return supportedAttributeVector;
 	}
 	
 	@Override
 	public List<ContentMetaData> getContainedResources()
 	{
 		return childContentMetaDataVector;
 	}
 	
 	@Override
 	public void addContainedResource(ContentMetaData contentMetaData)
 	{
 		childContentMetaDataVector.add(contentMetaData);
 	}
 	
 	public abstract Enum[] getAdditionalSupportedAttributes();
 
 	public int getIntValue(Enum name, int defaultValue, ResourceParameter... resourceParameters)
 	{
 		for (ResourceParameter resourceParameter : resourceParameters)
 		{
 			if (resourceParameter.getName().equals(name.toString()))
 			{
 				if(resourceParameter.getValue().equalsIgnoreCase("max"))
 				{
 					return Integer.MAX_VALUE;
 				}
 				else if (resourceParameter.getValue().matches("\\d+"))
 				{
 					return Integer.parseInt(resourceParameter.getValue());
 				}
 				else
 				{
 					throw new RuntimeException(resourceParameter.getName()+": '"+resourceParameter.getValue()+"' is not a number");
 				}
 			}
 		}
 		return defaultValue;	
 	}
 	
 	
 	public boolean getBoolean(Enum name, boolean defaultValue, ResourceParameter... resourceParameters)
 	{
 		for (ResourceParameter resourceParameter : resourceParameters)
 		{
 			if (resourceParameter.getName().equals(name.toString()))
 			{
 				if(resourceParameter.getValue().equalsIgnoreCase("true"))
 				{
 					return true;
 				}				
 			}
 		}
 		return defaultValue;
 		
 	}
 	
 	public String getString(Enum name, String defaultValue, ResourceParameter... resourceParameters)
 	{
 		for (ResourceParameter resourceParameter : resourceParameters)
 		{
 			if (resourceParameter.getName().equals(name.toString()))
 			{
 				return resourceParameter.getValue();				
 			}
 		}
 		return defaultValue;
 	}
 	
 	public void setResourceURI(ResourceURI resourceURI)
 	{
 		this.resourceURI = resourceURI;
 	}
 	
 	@Override
 	public ResourceURI getResourceURI()
 	{
 		return resourceURI;
 	}
 }
