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
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.net.URI;
 import java.nio.file.Files;
 import java.nio.file.LinkOption;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.logging.Level;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterOutputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 import com.delcyon.capo.util.CloneControl;
 import com.delcyon.capo.util.CloneControl.Clone;
 
 /**
  * @author jeremiah
  *
  */
 @CloneControl(filter=Clone.exclude, modifiers=Modifier.TRANSIENT)
 public class FileResourceContentMetaData extends AbstractContentMetaData
 {
     
     private String uri = null; 
     private int currentDepth = 0; 
     private ResourceParameter[] resourceParameters = new ResourceParameter[0];
     private transient File file;
     
     
 	public enum FileAttributes
 	{
         absolutePath, canonicalPath, symlink, regular
 	    
 	}
     
 	@SuppressWarnings("unused")
 	private FileResourceContentMetaData() //serialization only
 	{
 		
 	}
 	
 	public FileResourceContentMetaData(String uri, ResourceParameter... resourceParameters) throws Exception
 	{
 	    this.uri = uri;
 	    this.resourceParameters = resourceParameters;	    
 		init(uri,0,resourceParameters);
 	}
 	
 	public FileResourceContentMetaData(String uri, int currentDepth, ResourceParameter... resourceParameters) throws Exception
 	{
 	    this.uri = uri;
         this.resourceParameters = resourceParameters;
 		init(uri,currentDepth,resourceParameters);
 	}
 	
 	@SuppressWarnings("rawtypes")
     @Override
 	public Enum[] getAdditionalSupportedAttributes()
 	{
 		return new Enum[]{Attributes.exists,Attributes.executable,Attributes.readable,Attributes.writeable,Attributes.container,Attributes.lastModified,Attributes.MD5,FileAttributes.absolutePath,FileAttributes.canonicalPath,FileAttributes.symlink,FileAttributes.regular};
 	}
 
 	
 	
 	public void refresh(ResourceParameter... resourceParameters) throws Exception
 	{	    
 	    clearAttributes();
 	    setInitialized(false);
 	    if (resourceParameters != null)
 	    {
 	        init(uri,currentDepth,resourceParameters);    
 	    }
 	    else
 	    {
 	        init(uri,currentDepth,this.resourceParameters);
 	    }
 	}
 	
 	//just initialize anything about ourselves, BUT NOTHING ABOUT OUR CHILDREN
 	private void init(String uri,int currentDepth, ResourceParameter... resourceParameters) throws Exception
 	{
 	   
 		if (getBoolean(Parameters.USE_RELATIVE_PATHS,false,resourceParameters))
 		{
 			if (getString(Attributes.path,null,resourceParameters) == null)
 			{
 				ResourceParameterBuilder resourceParameterBuilder = new ResourceParameterBuilder();
 				resourceParameterBuilder.addAll(resourceParameters);
 				resourceParameterBuilder.addParameter(Attributes.path, uri.toString());
 				this.resourceParameters = resourceParameterBuilder.getParameters();
 			}
 			else
 			{
 				String uriString = uri.toString();
 				uriString = uriString.replaceFirst(getString(Attributes.path,null,resourceParameters),"");
 				setResourceURI(new ResourceURI(ResourceURI.removeURN(uriString)));
 			}
 		}
 		else
 		{
 			setResourceURI(new ResourceURI(uri));
 		}
 		
 		file = new File(new URI(uri));		
 		if(getResourceURI() == null)
 		{
 			setResourceURI(new ResourceURI(file.toURI().toString()));
 		}
 		
 	}
 	
 	@Override
 	public void init() throws RuntimeException
 	{
 	    if(isInitialized() == false)
 	    {
 	        try
 	        {
 	            load();
 	        }
 	        catch (Exception e)
 	        {	        	
 	            throw new RuntimeException(e);
 	        }
 	    }
 
 	}
 
 	protected void load() throws Exception
     {
 	    if(file == null)
 	    {
 	    	refresh();
 	    }
 
 	    boolean exists = false;
 	    Map fileAttributes = null;
 	    try //an exists() check on a file is more expensive than catching the IO exception 
 	    {
 	    	fileAttributes = Files.readAttributes(file.toPath(), "*", LinkOption.NOFOLLOW_LINKS);
 	    } catch(IOException ioe)
 	    {
 	    	setValue(Attributes.exists, exists+"");
 
 	    	setValue(Attributes.executable, file.canExecute());
 
 	    	setValue(Attributes.readable, file.canRead());
 
 	    	setValue(Attributes.writeable, file.canWrite());
 
 	    	setValue(Attributes.container, "false");
 
 	    	setValue(Attributes.lastModified,"");
 
 	    	setValue(SizeFilterInputStream.ATTRIBUTE_NAME, 0);
 
 	    	setValue(FileAttributes.absolutePath, file.getAbsolutePath());
 
 	    	setValue(FileAttributes.canonicalPath, file.getCanonicalPath());
 
 
 
 	    	setValue(FileAttributes.symlink, "false");
 	    	setInitialized(true);
 	    	return;
 	    }
 	    exists = true;
 	    
 	    
 	    setValue(Attributes.exists, exists);
 
         setValue(Attributes.executable, file.canExecute());
 
         setValue(Attributes.readable, file.canRead());
 
         setValue(Attributes.writeable, file.canWrite());
 
         setValue(Attributes.container, fileAttributes.get("isDirectory"));
 
         setValue(Attributes.lastModified,file.lastModified());
         
         setValue(SizeFilterInputStream.ATTRIBUTE_NAME, fileAttributes.get("size"));
         
         setValue(FileAttributes.absolutePath, file.getAbsolutePath());
         
         setValue(FileAttributes.canonicalPath, file.getCanonicalPath());
         
         boolean isSymlink = Boolean.parseBoolean(fileAttributes.get("isSymbolicLink").toString());
        // System.out.println(file+"attr="+Files.readAttributes(file.toPath(), "*",LinkOption.NOFOLLOW_LINKS));
         setValue(FileAttributes.symlink, isSymlink+"");
          
     
 		if (exists == true && file.canRead() == true && (file.isDirectory() == false || isSymlink))
 		{	
 			if(Files.isRegularFile(file.toPath()))
 			{
 				//System.out.println(Files.probeContentType(file.toPath()));
 				setValue(FileAttributes.regular, "true");
 				FileInputStream fileInputStream = new FileInputStream(file);
 				try //some files are not actually readable when we get here even though java says yes. 
 				{				
 				    readInputStream(fileInputStream,false);
 				} catch (IOException ioException)
 				{
 				    setValue(Attributes.readable, false);
 				}
 				finally
 				{
 				    fileInputStream.close();    
 				}
 				
 			}
 			else
 			{
 				setValue(FileAttributes.regular, "false");
 			}
 		}
		else if (file.isDirectory() == true && getIntValue(ContentMetaData.Parameters.DEPTH,1,resourceParameters) > currentDepth && isSymlink == false)
 		{				
 			String[] fileList = file.list();
 			
 			//check for permissions, cause if we can't read, well get a null list back
 			if(fileList == null && file.canRead() == false)
 			{
 			    CapoApplication.logger.log(Level.WARNING, "Can't read directory contents of "+file);
 			    fileList = new String[]{};
 			}
 			
 			if(isSymlink)
 			{
 			    CapoApplication.logger.log(Level.WARNING, "Symlink skipping directory contents of "+file);
                 fileList = new String[]{};
 			}
 			
 			
 			MD5FilterOutputStream md5FilterOutputStream = new MD5FilterOutputStream(new ByteArrayOutputStream());
 			if(file != null && exists)
             {
                 md5FilterOutputStream.write(file.getName());
                 md5FilterOutputStream.write(file.length()+"");
 			    md5FilterOutputStream.write(file.lastModified()+"");
             }
 			else
 			{
 			    md5FilterOutputStream.write("");
 			}
 			Arrays.sort(fileList);
 
 			int currentPreviousChildIndex = 0;
 			for (int currentChildURIIndex =0; currentChildURIIndex <  fileList.length; currentChildURIIndex++)
 			{
 				
 				String childURI = fileList[currentChildURIIndex];
 				File childFile = new File(file,childURI);
 				
 				//we don't care if this is a directory, since we always just strip off the ending slash
 				String tempChildURI = toURI(childFile, false);//.toString();
 				if (tempChildURI.endsWith(File.separator))
 	            {
 				    tempChildURI = tempChildURI.substring(0, tempChildURI.length()-File.separator.length());
 	            }
 				FileResourceContentMetaData contentMetaData = new FileResourceContentMetaData(tempChildURI, currentDepth+1,resourceParameters);
 				if(contentMetaData.file != null) //XXX we just got this from out parent, and checks are expensive && contentMetaData.file.exists())
 				{
 					
 					contentMetaData.file = childFile; //always reuse our original file object since the uri encoding of names can be problematic esp with regards to non breaking spaces in file names. 
 					BasicFileAttributes contentAttributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
 					
 				    md5FilterOutputStream.write(contentMetaData.file.getName());
 				    md5FilterOutputStream.write(contentAttributes.size()+"");
 				    md5FilterOutputStream.write(contentAttributes.lastModifiedTime().toMillis()+"");
 					
 				}
 				boolean addedChild = false;
 				for( ; currentPreviousChildIndex < childContentMetaDataLinkedList.size(); currentPreviousChildIndex++)
 				{	
 				    File currentPreviousChildFile = ((FileResourceContentMetaData)childContentMetaDataLinkedList.get(currentPreviousChildIndex)).file;
 				    int compare = currentPreviousChildFile.getName().compareTo(childFile.getName()); 
                     if(compare > 0) //previous after newChild
                     {
                         if((currentPreviousChildIndex +1) == childContentMetaDataLinkedList.size())
                         {
                             addContainedResource(contentMetaData);
                             addedChild = true;
                         }
                         else
                         {
                             childContentMetaDataLinkedList.add(currentPreviousChildIndex, contentMetaData);
                             addedChild = true;
                         }
                      
                         break;
                     }
                     else if (compare == 0) //previous before newChild
                     {
                         addedChild = true;
                         break;
                     }                    
                 }
 				if(addedChild == false)
 				{
 				    addContainedResource(contentMetaData);
 				}
 				
 				while(childFile.getName().equals(((FileResourceContentMetaData)childContentMetaDataLinkedList.get(currentChildURIIndex)).file.getName()) == false)
 				{
 					childContentMetaDataLinkedList.remove(currentChildURIIndex);
 				}
 			}
 			
 			//This may not be needed as it should effectively does again, what the removale of any non matches against the entire list  
 //			for(int currentNewFileIndex =0 ; currentNewFileIndex < fileList.length; currentNewFileIndex++)
 //			{
 //			    while(new File(file,fileList[currentNewFileIndex]).getName().equals(((FileResourceContentMetaData)childContentMetaDataLinkedList.get(currentNewFileIndex)).file.getName()) == false)
 //			    {
 //			        childContentMetaDataLinkedList.remove(currentNewFileIndex);
 //			    }
 //			        
 //			}
 			
 			//this truncates any files that were not in the new list, but are sorted to the end of the list
 			while(childContentMetaDataLinkedList.size() != fileList.length)
             {               
                 childContentMetaDataLinkedList.removeLast();
             }
 						
 			setValue(MD5FilterInputStream.ATTRIBUTE_NAME, md5FilterOutputStream.getMD5());
 			md5FilterOutputStream.close();
 			
 		}		
 		
 		setInitialized(true);
 	}
 	
 	
 
 	@Override
 	public Boolean exists()
 	{
 		return Boolean.parseBoolean(getValue(Attributes.exists));
 	}
 
 	@Override
 	public Long getLastModified()
 	{		
 		return Long.parseLong(getValue(Attributes.lastModified));
 	}
 
 	@Override
 	public Boolean isContainer()
 	{
 		return Boolean.parseBoolean(getValue(Attributes.container));
 	}
 
 	@Override
 	public Boolean isReadable()
 	{
 		return Boolean.parseBoolean(getValue(Attributes.readable));
 	}
 
 	@Override
 	public Boolean isWriteable()
 	{
 		return Boolean.parseBoolean(getValue(Attributes.writeable));
 	}
 	
 	private static String slashify(String path, boolean isDirectory) {
         String p = path;
         if (File.separatorChar != '/')
             p = p.replace(File.separatorChar, '/'); //replace separator chars with '/'
         if (!p.startsWith("/")) //add slash to front of path
             p = "/" + p;
         if (!p.endsWith("/") && isDirectory) //add / to end of path if directory
             p = p + "/";
         return p;
     }
 	
 	public String toURI(File file,boolean isDirectory) {
         try {
             File f = file.getAbsoluteFile();
             String sp = slashify(f.getPath(), isDirectory);
             if (sp.startsWith("//"))
                 sp = "//" + sp;
             
             String mine = "file:"+encode(sp);
             return mine; 
         } catch (Exception x) {
             throw new Error(x);         // Can't happen
         }
     }
 
 	public static String encode(String input) {
         StringBuilder resultStr = new StringBuilder();
         for (char ch : input.toCharArray()) {
             if (isUnsafe(ch)) {
                 resultStr.append('%');
                 resultStr.append(toHex(ch / 16));
                 resultStr.append(toHex(ch % 16));
             } else {
                 resultStr.append(ch);
             }
         }
         return resultStr.toString();
     }
 
     private static char toHex(int ch) {
         return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
     }
 
     private static boolean isUnsafe(char ch) {
     	if((int)ch == 160) //nbsp
     	{
             return true;
     	}
         return " %;?<>#|\\[]{}\"\n\r\t".indexOf(ch) >= 0;
     }
     
     @Override
     public ResourceParameter[] getResourceParameters()
     {
         return resourceParameters;
     }
 }
