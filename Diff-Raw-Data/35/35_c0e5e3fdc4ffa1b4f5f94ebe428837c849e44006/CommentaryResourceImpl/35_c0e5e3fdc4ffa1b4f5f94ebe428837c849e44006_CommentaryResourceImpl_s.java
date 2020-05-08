 // 
 // Copyright (c) 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 // 
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 // 
 // * Redistributions of source code must retain the above copyright notice,  
 //       this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 //       this list of conditions and the following disclaimer in the documentation  
 //       and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 //       nor the names of its contributors may be used to endorse or promote products  
 //       derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 // 
  
 package net.cyklotron.cms.forum;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.objectledge.coral.BackendException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.schema.AttributeDefinition;
 import org.objectledge.coral.schema.CoralSchema;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.ModificationNotPermitedException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.database.Database;
 
 import org.jcontainer.dna.Logger;
 
 /**
  * An implementation of <code>cms.forum.commentary</code> Coral resource class.
  *
  * @author Coral Maven plugin
  */
 public class CommentaryResourceImpl
     extends DiscussionResourceImpl
     implements CommentaryResource
 {
     // instance variables ////////////////////////////////////////////////////
 
     /** The AttributeDefinition object for the <code>documentTitle</code> attribute. */
     private AttributeDefinition documentTitleDef;
 
     /** The AttributeDefinition object for the <code>resourceId</code> attribute. */
     private AttributeDefinition resourceIdDef;
 
     // initialization /////////////////////////////////////////////////////////
 
     /**
      * Creates a blank <code>cms.forum.commentary</code> resource wrapper.
      *
      * <p>This constructor should be used by the handler class only. Use 
      * <code>load()</code> and <code>create()</code> methods to create
      * instances of the wrapper in your application code.</p>
      *
      * @param schema the CoralSchema.
      * @param database the Database.
      * @param logger the Logger.
      */
    public CommentaryResourceImpl(CoralSchema schema, Database database, Logger logger)
     {
         super(schema, database, logger);
         try
         {
             ResourceClass rc = schema.getResourceClass("cms.forum.commentary");
             documentTitleDef = rc.getAttribute("documentTitle");
             resourceIdDef = rc.getAttribute("resourceId");
         }
         catch(EntityDoesNotExistException e)
         {
             throw new BackendException("incompatible schema change", e);
         }
     }
 
     // static methods ////////////////////////////////////////////////////////
 
     /**
      * Retrieves a <code>cms.forum.commentary</code> resource instance from the store.
      *
      * <p>This is a simple wrapper of StoreService.getResource() method plus
      * the typecast.</p>
      *
      * @param session the CoralSession
      * @param id the id of the object to be retrieved
      * @return a resource instance.
      * @throws EntityDoesNotExistException if the resource with the given id does not exist.
      */
     public static CommentaryResource getCommentaryResource(CoralSession session, long id)
         throws EntityDoesNotExistException
     {
         Resource res = session.getStore().getResource(id);
         if(!(res instanceof CommentaryResource))
         {
             throw new IllegalArgumentException("resource #"+id+" is "+
                                                res.getResourceClass().getName()+
                                                " not cms.forum.commentary");
         }
         return (CommentaryResource)res;
     }
 
     /**
      * Creates a new <code>cms.forum.commentary</code> resource instance.
      *
      * @param session the CoralSession
      * @param name the name of the new resource
      * @param parent the parent resource.
      * @param forum the forum attribute
      * @return a new CommentaryResource instance.
      * @throws ValueRequiredException if one of the required attribues is undefined.
      */
     public static CommentaryResource createCommentaryResource(CoralSession session, String name,
         Resource parent, ForumResource forum)
         throws ValueRequiredException
     {
         try
         {
             ResourceClass rc = session.getSchema().getResourceClass("cms.forum.commentary");
             Map attrs = new HashMap();
             attrs.put(rc.getAttribute("forum"), forum);
             Resource res = session.getStore().createResource(name, parent, rc, attrs);
             if(!(res instanceof CommentaryResource))
             {
                 throw new BackendException("incosistent schema: created object is "+
                                            res.getClass().getName());
             }
             return (CommentaryResource)res;
         }
         catch(EntityDoesNotExistException e)
         {
             throw new BackendException("incompatible schema change", e);
         }
     }
 
     // public interface //////////////////////////////////////////////////////
  
     /**
      * Returns the value of the <code>documentTitle</code> attribute.
      *
      * @return the value of the <code>documentTitle</code> attribute.
      */
     public String getDocumentTitle()
     {
         return (String)get(documentTitleDef);
     }
     
     /**
      * Returns the value of the <code>documentTitle</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>documentTitle</code> attribute.
      */
     public String getDocumentTitle(String defaultValue)
     {
         if(isDefined(documentTitleDef))
         {
             return (String)get(documentTitleDef);
         }
         else
         {
             return defaultValue;
         }
     }    
 
     /**
      * Sets the value of the <code>documentTitle</code> attribute.
      *
      * @param value the value of the <code>documentTitle</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setDocumentTitle(String value)
     {
         try
         {
             if(value != null)
             {
                 set(documentTitleDef, value);
             }
             else
             {
                 unset(documentTitleDef);
             }
         }
         catch(ModificationNotPermitedException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
         catch(ValueRequiredException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
     }
    
 	/**
 	 * Checks if the value of the <code>documentTitle</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>documentTitle</code> attribute is defined.
 	 */
     public boolean isDocumentTitleDefined()
 	{
 	    return isDefined(documentTitleDef);
 	}
 
     /**
      * Returns the value of the <code>resourceId</code> attribute.
      *
      * @return the value of the <code>resourceId</code> attribute.
      * @throws IllegalStateException if the value of the attribute is 
      *         undefined.
      */
     public long getResourceId()
         throws IllegalStateException
     {
         if(isDefined(resourceIdDef))
         {
             return ((Long)get(resourceIdDef)).longValue();
         }
         else
         {
             throw new IllegalStateException("value of attribute resourceId is undefined"+
 			    " for resource #"+getId());
         }
     }
 
     /**
      * Returns the value of the <code>resourceId</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>resourceId</code> attribute.
      */
     public long getResourceId(long defaultValue)
     {
         if(isDefined(resourceIdDef))
         {
             return ((Long)get(resourceIdDef)).longValue();
         }
         else
         {
             return defaultValue;
         }
     }
 
     /**
      * Sets the value of the <code>resourceId</code> attribute.
      *
      * @param value the value of the <code>resourceId</code> attribute.
      */
     public void setResourceId(long value)
     {
         try
         {
             set(resourceIdDef, new Long(value));
         }
         catch(ModificationNotPermitedException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
         catch(ValueRequiredException e)
         {
             throw new BackendException("incompatible schema change",e);
         }
     }
 	
 	/**
      * Removes the value of the <code>resourceId</code> attribute.
      */
     public void unsetResourceId()
     {
         try
         {
             unset(resourceIdDef);
         }
         catch(ValueRequiredException e)
         {
             throw new BackendException("incompatible schema change",e);
         }     
     } 
    
 	/**
 	 * Checks if the value of the <code>resourceId</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>resourceId</code> attribute is defined.
 	 */
     public boolean isResourceIdDefined()
 	{
 	    return isDefined(resourceIdDef);
 	}
   
     // @custom methods ///////////////////////////////////////////////////////
     // @import org.objectledge.coral.session.CoralSession

     /**
      * Returns the commented resource, or null if deleted.
      */
     public Resource getResource(CoralSession coralSession)
     {
         if(!isDefined(resourceIdDef))
         {
             return null;
         }
         try
         {
             return coralSession.getStore().getResource(getResourceId());
         }
         catch(EntityDoesNotExistException e)
         {
             return null;
         }
         catch(BackendException e)
         {
             if(e.getCause() instanceof EntityDoesNotExistException)
             {
                 return null;
             }
             else
             {
                 throw e;
             }
         }
     }
 }
