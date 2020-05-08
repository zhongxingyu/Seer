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
  
 package net.cyklotron.cms.documents;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
import org.dom4j.Element;
 import org.objectledge.context.Context;
 import org.objectledge.coral.BackendException;
 import org.objectledge.coral.entity.EntityDoesNotExistException;
 import org.objectledge.coral.schema.AttributeDefinition;
 import org.objectledge.coral.schema.ResourceClass;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.InvalidResourceNameException;
 import org.objectledge.coral.store.ModificationNotPermitedException;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.coral.store.ValueRequiredException;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.parameters.RequestParameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.web.HttpContext;
 
 import net.cyklotron.cms.CmsDataFactory;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.NavigationNodeResourceImpl;
 import net.cyklotron.cms.structure.StructureService;
 
 /**
  * An implementation of <code>documents.document_node</code> Coral resource class.
  *
  * @author Coral Maven plugin
  */
 public class DocumentNodeResourceImpl
     extends NavigationNodeResourceImpl
     implements DocumentNodeResource
 {
     // class variables /////////////////////////////////////////////////////////
 
     /** Class variables initialization status. */
     private static boolean definitionsInitialized;
 	
     /** The AttributeDefinition object for the <code>abstract</code> attribute. */
     private static AttributeDefinition abstractDef;
 
     /** The AttributeDefinition object for the <code>content</code> attribute. */
     private static AttributeDefinition contentDef;
 
     /** The AttributeDefinition object for the <code>eventEnd</code> attribute. */
     private static AttributeDefinition eventEndDef;
 
     /** The AttributeDefinition object for the <code>eventPlace</code> attribute. */
     private static AttributeDefinition eventPlaceDef;
 
     /** The AttributeDefinition object for the <code>eventStart</code> attribute. */
     private static AttributeDefinition eventStartDef;
 
     /** The AttributeDefinition object for the <code>footer</code> attribute. */
     private static AttributeDefinition footerDef;
 
     /** The AttributeDefinition object for the <code>keywords</code> attribute. */
     private static AttributeDefinition keywordsDef;
 
     /** The AttributeDefinition object for the <code>lang</code> attribute. */
     private static AttributeDefinition langDef;
 
     /** The AttributeDefinition object for the <code>meta</code> attribute. */
     private static AttributeDefinition metaDef;
 
     /** The AttributeDefinition object for the <code>subTitle</code> attribute. */
     private static AttributeDefinition subTitleDef;
 
     /** The AttributeDefinition object for the <code>titleCalendar</code> attribute. */
     private static AttributeDefinition titleCalendarDef;
 
 	// custom injected fields /////////////////////////////////////////////////
 	
     /** The SiteService. */
     protected SiteService siteService;
 
     /** The HTMLService. */
     protected HTMLService htmlService;
 
     /** The StructureService. */
     protected StructureService structureService;
 
     /** The CmsDataFactory. */
     protected CmsDataFactory cmsDataFactory;
 
     /** The org.objectledge.web.mvc.tools.LinkToolFactory. */
     protected org.objectledge.web.mvc.tools.LinkToolFactory linkToolFactory;
 
     /** The org.objectledge.cache.CacheFactory. */
     protected org.objectledge.cache.CacheFactory cacheFactory;
 
     /** The net.cyklotron.cms.documents.DocumentService. */
     protected net.cyklotron.cms.documents.DocumentService documentService;
 
     // initialization /////////////////////////////////////////////////////////
 
     /**
      * Creates a blank <code>documents.document_node</code> resource wrapper.
      *
      * <p>This constructor should be used by the handler class only. Use 
      * <code>load()</code> and <code>create()</code> methods to create
      * instances of the wrapper in your application code.</p>
      *
      * @param siteService the SiteService.
      * @param htmlService the HTMLService.
      * @param structureService the StructureService.
      * @param cmsDataFactory the CmsDataFactory.
      * @param linkToolFactory the org.objectledge.web.mvc.tools.LinkToolFactory.
      * @param cacheFactory the org.objectledge.cache.CacheFactory.
      * @param documentService the net.cyklotron.cms.documents.DocumentService.
      */
     public DocumentNodeResourceImpl(SiteService siteService, HTMLService htmlService,
         StructureService structureService, CmsDataFactory cmsDataFactory,
         org.objectledge.web.mvc.tools.LinkToolFactory linkToolFactory,
         org.objectledge.cache.CacheFactory cacheFactory, net.cyklotron.cms.documents.DocumentService
         documentService)
     {
         this.siteService = siteService;
         this.htmlService = htmlService;
         this.structureService = structureService;
         this.cmsDataFactory = cmsDataFactory;
         this.linkToolFactory = linkToolFactory;
         this.cacheFactory = cacheFactory;
         this.documentService = documentService;
     }
 
     // static methods ////////////////////////////////////////////////////////
 
     /**
      * Retrieves a <code>documents.document_node</code> resource instance from the store.
      *
      * <p>This is a simple wrapper of StoreService.getResource() method plus
      * the typecast.</p>
      *
      * @param session the CoralSession
      * @param id the id of the object to be retrieved
      * @return a resource instance.
      * @throws EntityDoesNotExistException if the resource with the given id does not exist.
      */
     public static DocumentNodeResource getDocumentNodeResource(CoralSession session, long id)
         throws EntityDoesNotExistException
     {
         Resource res = session.getStore().getResource(id);
         if(!(res instanceof DocumentNodeResource))
         {
             throw new IllegalArgumentException("resource #"+id+" is "+
                                                res.getResourceClass().getName()+
                                                " not documents.document_node");
         }
         return (DocumentNodeResource)res;
     }
 
     /**
      * Creates a new <code>documents.document_node</code> resource instance.
      *
      * @param session the CoralSession
      * @param name the name of the new resource
      * @param parent the parent resource.
      * @param title the title attribute
      * @param site the site attribute
      * @param preferences the preferences attribute
      * @return a new DocumentNodeResource instance.
      * @throws ValueRequiredException if one of the required attribues is undefined.
      * @throws InvalidResourceNameException if the name argument contains illegal characters.
      */
     public static DocumentNodeResource createDocumentNodeResource(CoralSession session, String
         name, Resource parent, String title, SiteResource site, Parameters preferences)
         throws ValueRequiredException, InvalidResourceNameException
     {
         try
         {
             ResourceClass rc = session.getSchema().getResourceClass("documents.document_node");
             Map attrs = new HashMap();
             attrs.put(rc.getAttribute("title"), title);
             attrs.put(rc.getAttribute("site"), site);
             attrs.put(rc.getAttribute("preferences"), preferences);
             Resource res = session.getStore().createResource(name, parent, rc, attrs);
             if(!(res instanceof DocumentNodeResource))
             {
                 throw new BackendException("incosistent schema: created object is "+
                                            res.getClass().getName());
             }
             return (DocumentNodeResource)res;
         }
         catch(EntityDoesNotExistException e)
         {
             throw new BackendException("incompatible schema change", e);
         }
     }
 
     // public interface //////////////////////////////////////////////////////
  
     /**
      * Returns the value of the <code>abstract</code> attribute.
      *
      * @return the value of the <code>abstract</code> attribute.
      */
     public String getAbstract()
     {
         return (String)getInternal(abstractDef, null);
     }
     
     /**
      * Returns the value of the <code>abstract</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>abstract</code> attribute.
      */
     public String getAbstract(String defaultValue)
     {
         return (String)getInternal(abstractDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>abstract</code> attribute.
      *
      * @param value the value of the <code>abstract</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setAbstract(String value)
     {
         try
         {
             if(value != null)
             {
                 set(abstractDef, value);
             }
             else
             {
                 unset(abstractDef);
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
 	 * Checks if the value of the <code>abstract</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>abstract</code> attribute is defined.
 	 */
     public boolean isAbstractDefined()
 	{
 	    return isDefined(abstractDef);
 	}
  
     /**
      * Returns the value of the <code>content</code> attribute.
      *
      * @return the value of the <code>content</code> attribute.
      */
     public String getContent()
     {
         return (String)getInternal(contentDef, null);
     }
     
     /**
      * Returns the value of the <code>content</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>content</code> attribute.
      */
     public String getContent(String defaultValue)
     {
         return (String)getInternal(contentDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>content</code> attribute.
      *
      * @param value the value of the <code>content</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setContent(String value)
     {
         try
         {
             if(value != null)
             {
                 set(contentDef, value);
             }
             else
             {
                 unset(contentDef);
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
 	 * Checks if the value of the <code>content</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>content</code> attribute is defined.
 	 */
     public boolean isContentDefined()
 	{
 	    return isDefined(contentDef);
 	}
  
     /**
      * Returns the value of the <code>eventEnd</code> attribute.
      *
      * @return the value of the <code>eventEnd</code> attribute.
      */
     public Date getEventEnd()
     {
         return (Date)getInternal(eventEndDef, null);
     }
     
     /**
      * Returns the value of the <code>eventEnd</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>eventEnd</code> attribute.
      */
     public Date getEventEnd(Date defaultValue)
     {
         return (Date)getInternal(eventEndDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>eventEnd</code> attribute.
      *
      * @param value the value of the <code>eventEnd</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setEventEnd(Date value)
     {
         try
         {
             if(value != null)
             {
                 set(eventEndDef, value);
             }
             else
             {
                 unset(eventEndDef);
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
 	 * Checks if the value of the <code>eventEnd</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>eventEnd</code> attribute is defined.
 	 */
     public boolean isEventEndDefined()
 	{
 	    return isDefined(eventEndDef);
 	}
  
     /**
      * Returns the value of the <code>eventPlace</code> attribute.
      *
      * @return the value of the <code>eventPlace</code> attribute.
      */
     public String getEventPlace()
     {
         return (String)getInternal(eventPlaceDef, null);
     }
     
     /**
      * Returns the value of the <code>eventPlace</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>eventPlace</code> attribute.
      */
     public String getEventPlace(String defaultValue)
     {
         return (String)getInternal(eventPlaceDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>eventPlace</code> attribute.
      *
      * @param value the value of the <code>eventPlace</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setEventPlace(String value)
     {
         try
         {
             if(value != null)
             {
                 set(eventPlaceDef, value);
             }
             else
             {
                 unset(eventPlaceDef);
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
 	 * Checks if the value of the <code>eventPlace</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>eventPlace</code> attribute is defined.
 	 */
     public boolean isEventPlaceDefined()
 	{
 	    return isDefined(eventPlaceDef);
 	}
  
     /**
      * Returns the value of the <code>eventStart</code> attribute.
      *
      * @return the value of the <code>eventStart</code> attribute.
      */
     public Date getEventStart()
     {
         return (Date)getInternal(eventStartDef, null);
     }
     
     /**
      * Returns the value of the <code>eventStart</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>eventStart</code> attribute.
      */
     public Date getEventStart(Date defaultValue)
     {
         return (Date)getInternal(eventStartDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>eventStart</code> attribute.
      *
      * @param value the value of the <code>eventStart</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setEventStart(Date value)
     {
         try
         {
             if(value != null)
             {
                 set(eventStartDef, value);
             }
             else
             {
                 unset(eventStartDef);
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
 	 * Checks if the value of the <code>eventStart</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>eventStart</code> attribute is defined.
 	 */
     public boolean isEventStartDefined()
 	{
 	    return isDefined(eventStartDef);
 	}
  
     /**
      * Returns the value of the <code>footer</code> attribute.
      *
      * @return the value of the <code>footer</code> attribute.
      */
     public String getFooter()
     {
         return (String)getInternal(footerDef, null);
     }
     
     /**
      * Returns the value of the <code>footer</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>footer</code> attribute.
      */
     public String getFooter(String defaultValue)
     {
         return (String)getInternal(footerDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>footer</code> attribute.
      *
      * @param value the value of the <code>footer</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setFooter(String value)
     {
         try
         {
             if(value != null)
             {
                 set(footerDef, value);
             }
             else
             {
                 unset(footerDef);
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
 	 * Checks if the value of the <code>footer</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>footer</code> attribute is defined.
 	 */
     public boolean isFooterDefined()
 	{
 	    return isDefined(footerDef);
 	}
  
     /**
      * Returns the value of the <code>keywords</code> attribute.
      *
      * @return the value of the <code>keywords</code> attribute.
      */
     public String getKeywords()
     {
         return (String)getInternal(keywordsDef, null);
     }
     
     /**
      * Returns the value of the <code>keywords</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>keywords</code> attribute.
      */
     public String getKeywords(String defaultValue)
     {
         return (String)getInternal(keywordsDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>keywords</code> attribute.
      *
      * @param value the value of the <code>keywords</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setKeywords(String value)
     {
         try
         {
             if(value != null)
             {
                 set(keywordsDef, value);
             }
             else
             {
                 unset(keywordsDef);
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
 	 * Checks if the value of the <code>keywords</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>keywords</code> attribute is defined.
 	 */
     public boolean isKeywordsDefined()
 	{
 	    return isDefined(keywordsDef);
 	}
  
     /**
      * Returns the value of the <code>lang</code> attribute.
      *
      * @return the value of the <code>lang</code> attribute.
      */
     public String getLang()
     {
         return (String)getInternal(langDef, null);
     }
     
     /**
      * Returns the value of the <code>lang</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>lang</code> attribute.
      */
     public String getLang(String defaultValue)
     {
         return (String)getInternal(langDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>lang</code> attribute.
      *
      * @param value the value of the <code>lang</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setLang(String value)
     {
         try
         {
             if(value != null)
             {
                 set(langDef, value);
             }
             else
             {
                 unset(langDef);
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
 	 * Checks if the value of the <code>lang</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>lang</code> attribute is defined.
 	 */
     public boolean isLangDefined()
 	{
 	    return isDefined(langDef);
 	}
  
     /**
      * Returns the value of the <code>meta</code> attribute.
      *
      * @return the value of the <code>meta</code> attribute.
      */
     public String getMeta()
     {
         return (String)getInternal(metaDef, null);
     }
     
     /**
      * Returns the value of the <code>meta</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>meta</code> attribute.
      */
     public String getMeta(String defaultValue)
     {
         return (String)getInternal(metaDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>meta</code> attribute.
      *
      * @param value the value of the <code>meta</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setMeta(String value)
     {
         try
         {
             if(value != null)
             {
                 set(metaDef, value);
             }
             else
             {
                 unset(metaDef);
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
 	 * Checks if the value of the <code>meta</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>meta</code> attribute is defined.
 	 */
     public boolean isMetaDefined()
 	{
 	    return isDefined(metaDef);
 	}
  
     /**
      * Returns the value of the <code>subTitle</code> attribute.
      *
      * @return the value of the <code>subTitle</code> attribute.
      */
     public String getSubTitle()
     {
         return (String)getInternal(subTitleDef, null);
     }
     
     /**
      * Returns the value of the <code>subTitle</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>subTitle</code> attribute.
      */
     public String getSubTitle(String defaultValue)
     {
         return (String)getInternal(subTitleDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>subTitle</code> attribute.
      *
      * @param value the value of the <code>subTitle</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setSubTitle(String value)
     {
         try
         {
             if(value != null)
             {
                 set(subTitleDef, value);
             }
             else
             {
                 unset(subTitleDef);
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
 	 * Checks if the value of the <code>subTitle</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>subTitle</code> attribute is defined.
 	 */
     public boolean isSubTitleDefined()
 	{
 	    return isDefined(subTitleDef);
 	}
  
     /**
      * Returns the value of the <code>titleCalendar</code> attribute.
      *
      * @return the value of the <code>titleCalendar</code> attribute.
      */
     public String getTitleCalendar()
     {
         return (String)getInternal(titleCalendarDef, null);
     }
     
     /**
      * Returns the value of the <code>titleCalendar</code> attribute.
      *
      * @param defaultValue the value to return if the attribute is undefined.
      * @return the value of the <code>titleCalendar</code> attribute.
      */
     public String getTitleCalendar(String defaultValue)
     {
         return (String)getInternal(titleCalendarDef, defaultValue);
     }    
 
     /**
      * Sets the value of the <code>titleCalendar</code> attribute.
      *
      * @param value the value of the <code>titleCalendar</code> attribute,
      *        or <code>null</code> to remove value.
      */
     public void setTitleCalendar(String value)
     {
         try
         {
             if(value != null)
             {
                 set(titleCalendarDef, value);
             }
             else
             {
                 unset(titleCalendarDef);
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
 	 * Checks if the value of the <code>titleCalendar</code> attribute is defined.
 	 *
 	 * @return <code>true</code> if the value of the <code>titleCalendar</code> attribute is defined.
 	 */
     public boolean isTitleCalendarDefined()
 	{
 	    return isDefined(titleCalendarDef);
 	}
   
     // @custom methods ///////////////////////////////////////////////////////
 
     // @extends structure.navigation_node
     // @import java.util.List
     // @import java.util.Iterator
     // @import net.cyklotron.cms.CmsDataFactory
     // @import net.cyklotron.cms.site.SiteService
     // @import net.cyklotron.cms.structure.NavigationNodeResourceImpl
     // @import net.cyklotron.cms.structure.StructureService
     // @import org.objectledge.context.Context
     // @import org.objectledge.coral.session.CoralSession
     // @import org.objectledge.parameters.Parameters
     // @import org.objectledge.parameters.RequestParameters
     // @import org.objectledge.pipeline.ProcessingException
     // @import org.objectledge.web.HttpContext    
 	// @field SiteService siteService
     // @field HTMLService htmlService
     // @field StructureService structureService
     // @field CmsDataFactory cmsDataFactory
     // @field org.objectledge.web.mvc.tools.LinkToolFactory linkToolFactory
     // @field org.objectledge.cache.CacheFactory cacheFactory
     // @field net.cyklotron.cms.documents.DocumentService documentService
     
     // @order title, site, preferences
 
     
     // indexable resource methods //////////////////////////////////////////////////////////////////
 
     public String getIndexAbbreviation()
     {
         return htmlToText(getAbstract());
     }
 
     public String getIndexContent()
     {
         return htmlToText(getContent());
     }
 
     public String getIndexTitle()
     {
         return getTitle();
     }
 
     public Object getFieldValue(String fieldName)
     {
         if("keywords".equals(fieldName))
         {
             return getKeywords();
         }
         // WARN hack - should be in Navigation Node Resource
         else
         if(fieldName.equals("validityStart"))
         {
             return getValidityStart();
         }
         else
         if(fieldName.equals("eventStart"))
         {
             String title = getTitleCalendar();
             if(title == null || title.length()==0)
             {
                 return null;
             }
         	Date eS = getEventStart();
             if(eS == null)
             {
                 eS = new Date(0);
             }
             return eS;
         }
         else
 		if(fieldName.equals("eventEnd"))
 		{
             String title = getTitleCalendar();
             if(title == null || title.length()==0)
             {
                 return null;
             }
             Date eE = getEventEnd();
             if(eE == null)
             {
                 eE = new Date(Long.MAX_VALUE);
             }
             return eE;
 		}
 		else
 		if(fieldName.equals("titleCalendar"))
 		{
 			String title = getTitleCalendar();
 			if(title == null || title.length()==0)
 			{
 				return EMPTY_TITLE;
 			}
 			return title;
 		}
 		else
 		if(fieldName.equals("lastRedactor"))
 		{
 			return getLastRedactor();
 		}
 		else
 		if(fieldName.equals("lastEditor"))
 		{
 			return getLastEditor();
 		}
         else
         if(fieldName.equals("authors"))
         {
             return getMetaFieldText("/meta/authors");
         }
         if(fieldName.equals("sources"))
         {
             return getMetaFieldText("/meta/sources");
         }
 		return null;
     }
     
     private String getMetaFieldText(String xpath)
     {
         String meta = getMeta();
         if(meta == null || meta.length() == 0)
         {
             return null;
         }
         
         try
         {
             StringBuilder buf = new StringBuilder(256);
             org.dom4j.Document metaDom = HTMLUtil.parseXmlAttribute(meta, "meta");
             collectText((List<Element>)metaDom.selectNodes(xpath), buf);
             return buf.toString().trim();
         }
         catch (DocumentException e)
         {
             return null;
         }
     }
     
     private void collectText(List<Element> elements, StringBuilder buff)  
     {
         for(Element e : elements)
         {
             buff.append(e.getTextTrim()).append(' ');
             collectText((List<Element>)e.elements(), buff);
         }
     }
     
 	/**
 	 * Returns the store flag of the field.
 	 *
 	 * @return the store flag.
 	 */
 	public boolean isStored(String fieldName)
 	{
 		if(fieldName.equals("validityStart") ||
            fieldName.equals("eventStart") ||
 		   fieldName.equals("eventEnd") ||
 		   fieldName.equals("titleCalendar"))
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Returns the indexed flag of the field.
 	 *
 	 * @return the indexed flag.
 	 */
 	public boolean isIndexed(String fieldName)
 	{
 		return true;
 	}
 		
 	/**
 	 * Returns the tokenized flag of the field.
 	 *
 	 * @return the tokenized flag.
 	 */
 	public boolean isTokenized(String fieldName)
 	{
 		if(fieldName.equals("validityStart") ||
            fieldName.equals("eventStart") ||
 		   fieldName.equals("eventEnd"))
 		{
 			return false; 
 		}
 		return true;
 	}
 
     private String htmlToText(String html)
     {
         try
         {
             return htmlService.htmlToText(html);
         }
         catch(HTMLException e)
         {
             return null;
         }
     }
 
     // view helper methods //////////////////////////////////////////////////
    
     public DocumentTool getDocumentTool(Context context)
         throws ProcessingException
     {
         DocumentRenderingHelper docHelper = getDocumentRenderingHelper(context);
 
 		// determine current page for this document
 		int currentPage = 1; 
 		if(this == cmsDataFactory.getCmsData(context).getNode())
 		{
             Parameters parameters = RequestParameters.getRequestParameters(context);
 			currentPage = parameters.getInt("doc_pg",1);
 		}
 		// create tool
         HttpContext httpContext = HttpContext.getHttpContext(context);
         return new DocumentTool(docHelper, currentPage, httpContext.getEncoding());
     }
 
     private DocumentRenderingHelper getDocumentRenderingHelper(Context context)
         throws ProcessingException
     {
         Map helperMap = cacheFactory.getInstance("docRenderingHelpers");
         DocumentRenderingHelper docHelper = (DocumentRenderingHelper)helperMap.get(getIdObject());
         if(docHelper == null)
         {
             HttpContext httpContext = HttpContext.getHttpContext(context);
             CoralSession coralSession = context.getAttribute(CoralSession.class);
             docHelper = new DocumentRenderingHelper(coralSession, siteService, structureService,
                 htmlService, this, new RequestLinkRenderer(siteService, httpContext,
                     linkToolFactory), new PassThroughHTMLContentFilter());
             helperMap.put(getIdObject(), docHelper);
         }
         return docHelper;
     }
 
     public void clearCache()
     {
         Map helperMap = cacheFactory.getInstance("docRenderingHelpers");
         helperMap.remove(getIdObject());
     }
 
 	public DocumentTool getDocumentTool(CoralSession coralSession,
 		LinkRenderer linkRenderer, HTMLContentFilter filter, String characterEncoding)
 	throws ProcessingException
 	{
 		DocumentRenderingHelper tmpDocHelper =
 			new DocumentRenderingHelper(coralSession, siteService,
                 structureService, htmlService,this, linkRenderer, filter);
 		// create tool
 		return new DocumentTool(tmpDocHelper, 1, characterEncoding);
 	}
     
 
     
     public String getFooterContent(Context context)
         throws Exception
     {
         CoralSession coralSession = context.getAttribute(CoralSession.class);
         return documentService.getFooterContent(coralSession, getSite(), getFooter());
     }
     
 }
