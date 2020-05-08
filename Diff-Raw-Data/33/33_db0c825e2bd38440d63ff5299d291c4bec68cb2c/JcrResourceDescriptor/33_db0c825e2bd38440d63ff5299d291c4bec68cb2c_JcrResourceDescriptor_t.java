 /**
 Copyright (c) 2014 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.resourcemanager.types;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Level;
 
 import javax.jcr.Binary;
 import javax.jcr.Node;
 import javax.jcr.NodeIterator;
 import javax.jcr.Property;
 import javax.jcr.PropertyIterator;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 import javax.jcr.version.Version;
 import javax.jcr.version.VersionHistory;
 import javax.jcr.version.VersionIterator;
 import javax.jcr.version.VersionManager;
 
 import org.apache.jackrabbit.core.ItemImpl;
 import org.apache.jackrabbit.core.ItemValidator;
 import org.apache.jackrabbit.core.NodeImpl;
 import org.apache.jackrabbit.core.SessionImpl;
 import org.apache.jackrabbit.core.session.SessionContext;
 
 import com.delcyon.capo.CapoApplication;
 import com.delcyon.capo.ContextThread;
 import com.delcyon.capo.datastream.stream_attribute_filter.ContentFormatTypeFilterInputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.MD5FilterInputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.MimeTypeFilterInputStream;
 import com.delcyon.capo.datastream.stream_attribute_filter.SizeFilterInputStream;
 import com.delcyon.capo.resourcemanager.ContentFormatType;
 import com.delcyon.capo.resourcemanager.ResourceParameter;
 import com.delcyon.capo.resourcemanager.ResourceParameterBuilder;
 import com.delcyon.capo.resourcemanager.ResourceURI;
 import com.delcyon.capo.resourcemanager.types.FileResourceType.Parameters;
 import com.delcyon.capo.server.jackrabbit.CapoJcrServer;
 import com.delcyon.capo.xml.cdom.VariableContainer;
 import com.delcyon.capo.xml.dom.ResourceDeclarationElement;
 
 /**
  * @author jeremiah
  *
  */
 public class JcrResourceDescriptor extends AbstractResourceDescriptor implements Versionable
 {
 
 	
 	
 	//private Session session;
 	//private Node node;
 	private String absPath = null;
 	private JcrContentMetaData jcrResourceMetatData = null;
 	//private boolean isLocalSession = false;
 	private volatile Boolean isWriting = false;
 	private Boolean hasPipeThreadStarted = false;
     private PipedOutputStream pipedOutputStream;
     private ItemValidator originalItemValidator;
 
 	 
 	
 	@Override
 	public void init(ResourceDeclarationElement declaringResourceElement,VariableContainer variableContainer, LifeCycle lifeCycle,boolean iterate, ResourceParameter... resourceParameters) throws Exception
 	{
 	    String parentDirParameter = getVarValue(variableContainer, Parameters.PARENT_PROVIDED_DIRECTORY);  
         //String rootDirParameter = getVarValue(variableContainer, Parameters.ROOT_DIR);
         
         if (parentDirParameter != null && parentDirParameter.isEmpty() == false)
         {
             //append parent path to child
             this.absPath = ((JcrResourceDescriptor)CapoApplication.getDataManager().getResourceDirectory(parentDirParameter)).getResourceURI().getPath()+"/"+getResourceURI().getPath();
             setResourceURI(new ResourceURI(getResourceType().getName()+":"+this.absPath));
         }
         else
         {
             this.absPath = getResourceURI().getPath();
         }
 		super.init(declaringResourceElement, variableContainer, lifeCycle, iterate, resourceParameters);
 	}
 	
 	
 	@Override
 	public void open(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{
 		advanceState(State.INITIALIZED, variableContainer, resourceParameters);
 	    if (getResourceState().ordinal() < State.OPEN.ordinal())
 	    {
 	    	
 //	    	this.isLocalSession = false;
 //			
 //			if(Thread.currentThread() instanceof ContextThread)
 //			{
 //			    this.session = ((ContextThread)Thread.currentThread()).getSession();
 //			}
 //			else if(WApplication.getInstance() != null)
 //			{
 //			    this.session = ((CapoWebApplication)WApplication.getInstance()).getJcrSession();
 //			}
 //			else if(CapoJcrServer.getRepository() != null && getLifeCycle() == LifeCycle.EXPLICIT)
 //	        {
 //	            this.session = CapoJcrServer.getRepository().login(new SimpleCredentials("admin","admin".toCharArray()));
 //	            this.isLocalSession = true;
 //	        }
 //			if(this.session == null)
 //			{
 //			    if(CapoApplication.isServer()) //one last ditch effort
 //			    {
 //			        this.session = CapoJcrServer.getApplicationSession();			        
 //			    }
 //			    if(this.session == null)
 //	            {
 //			        throw new Exception("Can't use JCR resources without a Session");
 //	            }
 //			}
 //			
 			CapoApplication.logger.log(Level.FINE, "JCRAssoc T="+Thread.currentThread()+" S="+CapoJcrServer.getSession()+" N="+absPath);			
 //			if(session.nodeExists(absPath) == true)
 //			{
 //				this.node = session.getNode(absPath);
 //			}
 	        super.open(variableContainer,resourceParameters);
 	       // ((JcrContentMetaData) getResourceMetaData(variableContainer, resourceParameters)).setNode(this.node);
 	    }
 	}
 	
 	@Override
 	public StreamType[] getSupportedStreamTypes() throws Exception
 	{
 		return  EnumSet.of(StreamType.INPUT, StreamType.OUTPUT).toArray(new StreamType[]{});
 	}
 
 	@Override
 	public StreamFormat[] getSupportedStreamFormats(StreamType streamType) throws Exception
 	{
 	    if(streamType == StreamType.INPUT || streamType == StreamType.OUTPUT)
 	    {
 	        return EnumSet.of(StreamFormat.XML_BLOCK,StreamFormat.STREAM).toArray(new StreamFormat[]{});
 	    }
 	    else
 	    {
 	        return null;
 	    }
 	}
 
 	@Override
 	public boolean next(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{		
 	    advanceState(State.OPEN, variableContainer, resourceParameters);
         if(getResourceState() == State.OPEN)
         {
             
            // contentMetaData = new SimpleContentMetaData(getResourceURI());
 //            URL url = new URL(getResourceURI().getBaseURI());   
 //            InputStream inputStream = contentMetaData.wrapInputStream(url.openConnection().getInputStream());
 //            content = contentMetaData.readInputStream(inputStream,true);
             setResourceState(State.STEPPING);
             return true;
         }
         else if (getResourceState() == State.STEPPING)
         {
             setResourceState(State.OPEN);
             return false;
         }
         return false;
 	}
 
 	@Override
 	public ContentMetaData getContentMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{		
 	    return buildResourceMetaData(variableContainer, resourceParameters);
 	}
 
 	@Override
 	public ContentMetaData getOutputMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{
 	    return buildResourceMetaData(variableContainer, resourceParameters);
 	}
 
 	@Override
 	protected void clearContent() throws Exception
 	{
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected ContentMetaData buildResourceMetaData(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{
 		if(jcrResourceMetatData == null)
 		{
 		    this.jcrResourceMetatData = new JcrContentMetaData(getResourceURI()); 
 		}
 		return this.jcrResourceMetatData;
 	}
 
 	@Override
 	public OutputStream getOutputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{
 	    //return the output stream we have, if it's already open
 		if(this.pipedOutputStream != null)
 		{
 		    return this.pipedOutputStream;
 		}
 		
 	    synchronized (isWriting)
 		{
 	    	isWriting = true;
 	    	//If we're going to write. make sure we have a node to write to.
 	    	if(getNode() == null)
 	    	{
 	    	    performAction(variableContainer, Action.CREATE, resourceParameters);
 	    	    performAction(variableContainer, Action.COMMIT, resourceParameters);
 	    	}
 	    	
 	    	final PipedInputStream pipedInputStream = new PipedInputStream(){
 	    		@Override
 	    		public void close() throws IOException
 	    		{
 	    			//System.out.println("piped input close attempt: "+System.currentTimeMillis());
 	    			// TODO Auto-generated method stub
 	    			super.close();
 	    			//System.out.println("piped input closed: "+System.currentTimeMillis());
 
 	    		}
 	    	};
 
 	    	PipedOutputStream pipedOutputStream = new PipedOutputStream(pipedInputStream){
 	    		@Override
 	    		public void close() throws IOException
 	    		{	
 	    			//System.out.println("piped output close attempt: "+System.currentTimeMillis());
 	    			
 	    			
 	    			super.close();
 	    			//System.out.println("isWaiting = "+isWriting);
 	    			Boolean _isWaiting = null;
 	    			synchronized (isWriting)
 	    			{
 	    				_isWaiting = isWriting;
 	    			}
 	    				//don't let this close while we're still dealing with threaded output
 	    				//System.out.println("isWaiting = "+_isWaiting);
 	    				while(_isWaiting == true )
 	    				{
 	    					try
 	    					{
 	    					//	System.out.println("waiting for pipe thread to finish");
 	    						Thread.sleep(100);
 	    						synchronized (isWriting)
 								{
 	    							_isWaiting = isWriting;
 								}
 	    					} catch (InterruptedException e)
 	    					{					
 	    						e.printStackTrace();
 	    					}
 	    				}
 	    			
 	    			//System.out.println("piped output closed: "+System.currentTimeMillis());
 	    			JcrResourceDescriptor.this.pipedOutputStream = null;
 	    			
 	    		}
 	    	};
 
 
 	    	//MimeTable mt = MimeTable.getDefaultTable();
 	    	//String mimeType = mt.getContentTypeFor(file.getName());
 	    	//if (mimeType == null) mimeType = "application/octet-stream";
 
 	    	// Node fileNode = node.addNode("<name>", "nt:file");
 
 	    	//System.out.println( fileNode.getName() );
 
 	    	//final Node resNode = fileNode.addNode("jcr:content", "nt:resource");
 	    	//resNode.setProperty("jcr:mimeType", "<mimeType>");
 	    	//resNode.setProperty("jcr:encoding", "");
 
 	    	Runnable pipe = new Runnable()
 	    	{
 
 	    		@Override
 	    		public void run()
 	    		{
 	    			
 
 	    				try
 	    				{
 	    					
 
 	    					MD5FilterInputStream md5FilterInputStream = new MD5FilterInputStream(pipedInputStream);
 	    					ContentFormatTypeFilterInputStream contentFormatTypeFilterInputStream = new ContentFormatTypeFilterInputStream(md5FilterInputStream);
 	    					MimeTypeFilterInputStream mimeTypeFilterInputStream = new MimeTypeFilterInputStream(contentFormatTypeFilterInputStream);
 	    					SizeFilterInputStream sizeFilterInputStream = new SizeFilterInputStream(mimeTypeFilterInputStream);
 	    					//System.out.println("pipe thread start read: "+System.currentTimeMillis());
 	    					synchronized (hasPipeThreadStarted)
 							{
 	    						hasPipeThreadStarted.notify();
 							}	    					
 	    					Binary binary = getNode().getSession().getValueFactory().createBinary( sizeFilterInputStream );
 	    					if(binary.getSize() != 0)
 	    					{
 	    					    getNode().setProperty("jcr:data",binary);
 
 	    					    getNode().setProperty(contentFormatTypeFilterInputStream.getName(),contentFormatTypeFilterInputStream.getValue());
 	    					    if(contentFormatTypeFilterInputStream.getContentFormatType() != ContentFormatType.BINARY)
 	    					    {
 	    					        byte[] buffer = new byte[(int) binary.getSize()];
 	    					        binary.read(buffer, 0);
 	    					        getNode().setProperty("content",new String(buffer));
 	    					    }
 	    					    else if(getNode().hasProperty("content"))
 	    					    {
 	    					        getNode().getProperty("content").remove();
 	    					    }
 	    					    getNode().setProperty(sizeFilterInputStream.getName(),sizeFilterInputStream.getValue());                    
 	    					    getNode().setProperty(mimeTypeFilterInputStream.getName(),mimeTypeFilterInputStream.getValue());
 	    					    //getNode().setProperty("jcr:mimeType",mimeTypeFilterInputStream.getValue());
 	    					    getNode().setProperty(md5FilterInputStream.getName(),md5FilterInputStream.getValue());
 	    					}
 	    					else //there is no data
 	    					{
 	    					    PropertyIterator propertyIterator = getNode().getProperties();
 	    					    while(propertyIterator.hasNext())
 	    					    {
 	    					        Property property = propertyIterator.nextProperty();
 	    					        if(property.getName().startsWith("jcr:") == false)
 	    					        {
 	    					            property.remove();
 	    					        }
 	    					    }
 	    					    if(getNode().hasProperty("jcr:data"))
                                 {
                                     getNode().getProperty("jcr:data").remove();
                                 }
 	    					}
 	    					binary.dispose();
 	    					//System.out.println("pipe thread done: "+System.currentTimeMillis());
 	    					if(getNode().getSession() != null && getNode().getSession().isLive() && getNode().getSession().hasPendingChanges())
                             {//TODO check for autocommit setting
 	    					    getNode().getSession().save();
                             }
 	    				}
 	    				catch (Exception e)
 	    				{
 	    					e.printStackTrace();            		
 	    				}
 	    				finally
 	    				{
 	    					//isWriting.notify();
 	    					synchronized (isWriting)
 	    	    			{
 	    					    
 	    						isWriting = false;
 	    	    			}
 	    					//cleanup session out of thread
 	    					if(Thread.currentThread() instanceof ContextThread)
 	    					{
 	    					    ((ContextThread) Thread.currentThread()).setSession(null);
 	    					}
 	    				}
 	    			}
 	    		
 	    	};
 
 	    	synchronized (hasPipeThreadStarted)
 			{
 	    	    //System.err.println( "PipeWrite T="+Thread.currentThread()+" S="+session+" N="+absPath);
 	    		ContextThread contextThread = new ContextThread(pipe, getNode().getName()+" pipeThread-"+System.currentTimeMillis());
 	    		contextThread.setSession(getNode().getSession());
 	    		contextThread.start();
 	    		//Calendar lastModified = Calendar.getInstance();
 	    		//lastModified.setTimeInMillis(file.lastModified());
 	    		//resNode.setProperty("jcr:lastModified", lastModified);
 	    		//Uti
 
 
 				//System.out.println("waiting for pipe thread to start");
 				hasPipeThreadStarted.wait(1500);
 			}
 			
 			this.pipedOutputStream = pipedOutputStream;
 	    	return pipedOutputStream;
 		}
 	}
 	
 	@Override
 	public InputStream getInputStream(VariableContainer variableContainer,ResourceParameter... resourceParameters) throws Exception
 	{	
 	    advanceState(State.STEPPING, variableContainer, resourceParameters);
 	    if(getNode().hasProperty("jcr:data"))
 	    {
 	        return getNode().getProperty("jcr:data").getBinary().getStream();
 	    }
 	    else
 	    {
 	        return new ByteArrayInputStream(new byte[]{});
 	    }
 	}
 	
 //	@Override
 //	public void writeXML(VariableContainer variableContainer, CElement element, ResourceParameter... resourceParameters) throws Exception
 //	{
 //	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 //	    XPath.dumpNode(element, outputStream);
 //	    session.importXML(getResourceURI().getResourceURIString().replaceFirst("^repo:", ""), new ByteArrayInputStream(outputStream.toByteArray()), 0);
 //	    dump(session.getRootNode());
 //	    session.save();
 //	    String[] langs = session.getWorkspace().getQueryManager().getSupportedQueryLanguages();
 //	    String[] prefixes = session.getNamespacePrefixes();
 //	    
 //	    Query query = session.getWorkspace().getQueryManager().createQuery("SELECT * FROM [nt:unstructured] where NAME([nt:unstructured]) = 'server:log' order by message", "JCR-SQL2");
 //	    QueryResult result = query.execute();
 //
 //
 //	    // Iterate over the nodes in the results ...
 //
 //	    NodeIterator nodeIter = result.getNodes();
 //	    System.out.println("=============================");
 //	    while ( nodeIter.hasNext() ) {
 //
 //	        Node _node = nodeIter.nextNode();
 //	        System.out.println("===>"+_node.getName()+" type:"+_node.getPrimaryNodeType().getName());
 //	        dump(_node);
 //
 //	    }
 //	    System.out.println("=============================");
 //	    session.logout();
 //	}
 	
 	
 	@Override
 	public boolean performAction(VariableContainer variableContainer,Action action,ResourceParameter... resourceParameters) throws Exception
 	{
 	    super.addResourceParameters(variableContainer, resourceParameters);
 	    advanceState(State.OPEN, variableContainer, resourceParameters);
         
         boolean success = false;
 
         if (action == Action.CREATE)
         {
         	if(CapoJcrServer.getSession().nodeExists(absPath) == false)
     		{
     		    String[] relPath = absPath.split("/");
     		    Node currentNode = CapoJcrServer.getSession().getRootNode();
     		    for (String nodeName : relPath)
                 {
     		        if(nodeName.isEmpty())
     		        {
     		            currentNode = CapoJcrServer.getSession().getRootNode();
     		            continue;
     		        }
                     if(currentNode.hasNode(nodeName) == false)
                     {
                         currentNode.addNode(nodeName);//,"nt:folder");
                         if(currentNode.isNew())
                         {
                             currentNode.setProperty("container", "true");
                         }
                     }
                     currentNode = currentNode.getNode(nodeName);
                 }
     		    //session.getRootNode().addNode(getResourceURI().getResourceURIString().replaceFirst("^repo:/", ""));
     		    
     		    
     		    String containerFlag = getVarValue(variableContainer, DefaultParameters.CONTAINER); 
                 if (containerFlag != null && containerFlag.equalsIgnoreCase("true"))
                 {
                     getNode().setProperty("container", containerFlag);
                 }    		    
     		    refreshResourceMetaData(variableContainer, resourceParameters);
     		    success = true;    		    
     		}
         }
         else if (action == Action.DELETE)
         {
             if (getNode() != null)
             {
                 Node node = getNode();
                 node.remove();
                 node.getSession().save();
     		    refreshResourceMetaData(variableContainer, resourceParameters);
     		    success = true;
             }
         }
         else if (action == Action.COMMIT)
         {
             if(pipedOutputStream != null)
             {
                 try {
                     pipedOutputStream.close();
                     pipedOutputStream = null;
                 }
                 catch (Exception exception){}
             }
             if (getNode() != null)
             {
                 getNode().getSession().save();
     		    success = true;
             }
         }
         else if (action == Action.CHECKIN)
         {
             checkin();
             refreshResourceMetaData(variableContainer, resourceParameters);
 		    success = true;
         }
         else if (action == Action.CHECKOUT)
         {
             checkout();
             refreshResourceMetaData(variableContainer, resourceParameters);
 		    success = true;
         }
         else if (action == Action.RESTORE)
         {
             restore(ResourceParameterBuilder.getValue(Versionable.ResourceParameters.VERSION_UUID, resourceParameters));
             refreshResourceMetaData(variableContainer, resourceParameters);
 		    success = true;
         }
 	    return success;
 	}
 	
 	@Override
 	protected Action[] getSupportedActions()
 	{
 		return (Action[]) EnumSet.allOf(Action.class).toArray(new Action[]{});
 	}
 
 	/** Recursively outputs the contents of the given node. */ 
 	public static void dump(Node node) throws RepositoryException { 
 	    // First output the node path 
 	    if(node == null)
 	    {
 	        return;
 	    }
 	    System.out.println(node.getPath()); 
 	    // Skip the virtual (and large!) jcr:system subtree 
 //	    if (node.getPath().startsWith("/jcr:system")) { 
 //	        return; 
 //	    } 
 	    
 	    // Then output the properties 
 	    PropertyIterator properties = node.getProperties(); 
 	    while (properties.hasNext()) { 
 	        Property property = properties.nextProperty();
 	        if(property.getName().equals("jcr:data") || property.getName().equals("content"))
 	        {
 	        	continue;
 	        }
 	        if (property.getDefinition().isMultiple()) { 
 	            // A multi-valued property, print all values 
 	            Value[] values = property.getValues(); 
 	            for (int i = 0; i < values.length; i++) { 
 	                System.out.println( 
 	                        property.getPath() + " += " + values[i].getString()); 
 	            } 
 	        } else { 
 	            // A single-valued property 
 	            System.out.println( 
 	                    property.getPath() + " == " + property.getString()); 
 	        } 
 	    } 
 
 	    // Finally output all the child nodes recursively 
 	    NodeIterator nodes = node.getNodes(); 
 	    while (nodes.hasNext()) { 
 	        dump(nodes.nextNode()); 
 	    } 
 	} 
 
 	@Override
 	public void close(VariableContainer variableContainer, ResourceParameter... resourceParameters) throws Exception
 	{	 
 		super.close(variableContainer, resourceParameters);
 		if(pipedOutputStream != null)
 		{
 		    try {
 		        pipedOutputStream.close();
 		        pipedOutputStream = null;
 		    }
 		    catch (Exception exception){}
 		}
 		if(CapoJcrServer.getSession() != null && CapoJcrServer.getSession().isLive() && CapoJcrServer.getSession().hasPendingChanges())
 		{//TODO check for autocommit setting
 		    CapoJcrServer.getSession().save();
 		}
 //	    if(isLocalSession == true && session != null && session.isLive())
 //	    {
 //	        session.logout();
 //	        ((JcrContentMetaData) getResourceMetaData(variableContainer, resourceParameters)).setNode(null);
 //		    
 //	    }
 	 //   this.session = null;
 	    
 	}
 	
 //	@Override
 //	public ResourceDescriptor getChildResourceDescriptor(ControlElement callingControlElement, String relativeURI) throws Exception
 //	{
 //	    ResourceDescriptor childResourceDescriptor = super.getChildResourceDescriptor(callingControlElement, relativeURI);
 //	    if(childResourceDescriptor instanceof JcrResourceDescriptor)
 //	    {
 //	        ((JcrResourceDescriptor) childResourceDescriptor).session = this.session;
 //	    }
 //	    return childResourceDescriptor;
 //	}
 	
 	public void checkin() throws Exception {
 		if (getNode() != null)
         {		    
         	VersionManager versionManager = getNode().getSession().getWorkspace().getVersionManager();            	
             Node node = getNode();
             
             if(node.isCheckedOut() == false) //this should return true on anything that isn't versionable
             {
                 //make sure that the node is versionable
                 if(node.isNodeType("mix:versionable") == false)
                 {
                     try
                     {
                         //if this is not checked out AND ist' not versionable something is wrong, so try and check it out and see if that throws an exception
                         versionManager.checkout(node.getPath());
                     } catch (Exception exception)
                     {                    
                         //if so we managed to get a node checked in without a versionable mixin
                         //so let the addMixin call succeed no matter the cost by removing some validation from jack rabbit for the time being 
                         overrideJackrabbitValidation(node,ItemValidator.CHECK_CHECKED_OUT);                   
                     }
                 }
             }
             
             if(node.isNodeType("mix:versionable") == false)
             {
                 node.addMixin("mix:versionable");
                 node.getSession().save();
 
                 if(originalItemValidator != null)
                 {
                     resetJackRabitValidation(node);            	    
                 }
             }
             
             versionManager.checkin(node.getPath());
     
         }
 	}
 
 	
 
 	/**
 	 * This should never really be used, but in testing a node was marked as checked in w/o the required mixin's for versioning.
 	 * So this is basically a method to override jackrabbit's validation methods so that we can still write to a checked in version.
 	 * @param n
 	 * @param optionsOverride
 	 * @throws Exception
 	 */
 	private void overrideJackrabbitValidation(Node n, int optionsOverride) throws Exception
 	{
 	    try
 	    {
 
 
 	        NodeImpl node = (NodeImpl) n;
 	        SessionImpl session =  (SessionImpl) node.getSession();
 	        Field sessionContextField = SessionImpl.class.getDeclaredField("context");
 	        sessionContextField.setAccessible(true);
 	        SessionContext sessionContext = (SessionContext) sessionContextField.get(session);
 
 	        Field modifiersField = Field.class.getDeclaredField("modifiers");
 	        modifiersField.setAccessible(true);
 
 	        Field itemValidatorField =  SessionContext.class.getDeclaredField("itemValidator");
 	        itemValidatorField.setAccessible(true);
 	        originalItemValidator = (ItemValidator) itemValidatorField.get(sessionContext); 
 	        modifiersField.setInt(itemValidatorField, itemValidatorField.getModifiers() & ~Modifier.FINAL);
 
 	        ItemValidator itemValidator = new ItemValidator(sessionContext){
 	            @Override
 	            public synchronized boolean canModify(ItemImpl item, int options, int permissions) throws RepositoryException
 	            {
 	                options = options & ~optionsOverride;
 	                return super.canModify(item, options, permissions);
 	            }
 	            @Override
 	            public synchronized void checkModify(ItemImpl item, int options, int permissions) throws RepositoryException
 	            {
 	                options = options & ~optionsOverride;
 	                super.checkModify(item, options, permissions);
 	            }
 
 	            @Override
 	            public synchronized void checkRemove(ItemImpl item, int options, int permissions) throws RepositoryException
 	            {
 	                options = options & ~optionsOverride;
 	                super.checkRemove(item, options, permissions);
 	            }
 
 	        };
 
 	        itemValidatorField.set(sessionContext, itemValidator);
 
 	    }
 	    catch (Exception e)
 	    {
 	        throw e;
 	    }
 
 	}
 
 	/**
 	 * this should restore any jackrabbit validation that was turned off. 
 	 * @param n
 	 * @throws Exception
 	 */
 	private void resetJackRabitValidation(Node n) throws Exception
 	{
 	    try
 	    {
 
 
 	        NodeImpl node = (NodeImpl) n;
 	        SessionImpl session =  (SessionImpl) node.getSession();
 	        Field sessionContextField = SessionImpl.class.getDeclaredField("context");
 	        sessionContextField.setAccessible(true);
 	        SessionContext sessionContext = (SessionContext) sessionContextField.get(session);
 
 	        Field itemValidatorField =  SessionContext.class.getDeclaredField("itemValidator");
 	        itemValidatorField.setAccessible(true);
 	        itemValidatorField.set(sessionContext, originalItemValidator);
 
 	        Field modifiersField = Field.class.getDeclaredField("modifiers");
 	        modifiersField.setAccessible(true); 
 	        modifiersField.setInt(itemValidatorField, itemValidatorField.getModifiers() | Modifier.FINAL);
 	        modifiersField.setAccessible(false); 
 	        itemValidatorField.setAccessible(false);
 	        sessionContextField.setAccessible(false);
 	        originalItemValidator = null;
 	    }
 	    catch (Exception e)
 	    {
 	        throw e;
 	    }
 
 	}
 
     @Override
 	public void checkout() throws Exception
 	{
 		if (getNode() != null)
         {
         	VersionManager versionManager = getNode().getSession().getWorkspace().getVersionManager();            	
             Node node = getNode();            
             getVersionHistory();
             if(node.hasProperty("jcr:isCheckedOut") == true)
             {
             	versionManager.checkout(node.getPath());	
             }                		 
         }
 		
 	}
 	
 	@Override
 	public boolean isVersioned() throws Exception
 	{
 		if (getNode() != null && getNode().hasProperty("jcr:isCheckedOut"))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	public List<ContentMetaData> getVersionHistory() throws Exception
 	{
 		Vector<ContentMetaData> versionHistory = new Vector<>();
 		if (getNode() != null && getNode().hasProperty("jcr:isCheckedOut"))
 		{			 
 			VersionManager versionManager = getNode().getSession().getWorkspace().getVersionManager();  
 			VersionHistory history = versionManager.getVersionHistory(getNode().getPath());
 			boolean isRootVersion = true;
 			for (VersionIterator it = history.getAllVersions(); it.hasNext();) 
 			{
 				
 				Version version = (Version) it.next();
 				version.getPath();				
 				JcrContentMetaData contentMetaData = new JcrVersionContentMetaData(new ResourceURI("repo:"+getNode().getPath()+"?version="+version.getName()));
 				//skip first version
 				if(isRootVersion)
 				{
 					isRootVersion = false;
 					continue;
 				}
 				versionHistory.add(contentMetaData);
 				//dump(version.getFrozenNode());
 				//System.out.println(version.getName());				
 			}
 		}
 		return versionHistory;
 	}
 	
 	@Override
 	public String getVersion(String versionUUID) throws Exception
 	{
 	    VersionManager versionManager = getNode().getSession().getWorkspace().getVersionManager();  
         ResourceURI versionURI = new ResourceURI(versionUUID);
         VersionHistory history = versionManager.getVersionHistory(getNode().getPath());
         Version version = history.getVersion(versionURI.getParameterMap().get("version"));
         if (version != null)
         {
             return "repo:"+version.getFrozenNode().getPath();
         }
         return null;
 	}
 	
 	@Override
 	public void restore(String versionUUID) throws Exception
 	{
 		
 		if (getNode() != null)
 		{
 			VersionManager versionManager = getNode().getSession().getWorkspace().getVersionManager();  
 			ResourceURI versionURI = new ResourceURI(versionUUID);
 			VersionHistory history = versionManager.getVersionHistory(getNode().getPath());
 			Version version = history.getVersion(versionURI.getParameterMap().get("version"));
 			if (version != null)
 			{
 				versionManager.restore( version, true);
 			}
 			
         }
 	}
 	
 	@Override
 	public void remove(String versionUUID) throws Exception
 	{
 		
 		if (getNode() != null)
 		{
			if (getNode() != null)
			{
				VersionManager versionManager = getNode().getSession().getWorkspace().getVersionManager();  
				ResourceURI versionURI = new ResourceURI(versionUUID);
				VersionHistory history = versionManager.getVersionHistory(getNode().getPath());
				Version version = history.getVersion(versionURI.getParameterMap().get("version"));
				if (version != null)
 				{
					history.removeVersion(version.getName());
 				}
				
	        }
         }
 	}
 	
 	private Node getNode() throws Exception
 	{
 	    if(CapoJcrServer.getSession().nodeExists(absPath))
 	    {
 	        return CapoJcrServer.getSession().getNode(absPath);
 	    }
 	    else
 	    {
 	        return null;
 	    }
 	}
 	
 }
