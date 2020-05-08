 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: EntityPage.java,v 1.2 2004-08-09 22:15:14 shahid.shah Exp $
  */
 
 package com.netspective.sparx.navigate.entity;
 
 import java.sql.SQLException;
 
 import org.apache.commons.lang.exception.NestableRuntimeException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.axiom.schema.Table;
 import com.netspective.axiom.schema.table.type.EnumerationTable;
 import com.netspective.axiom.schema.table.type.EnumerationTableRow;
 import com.netspective.axiom.schema.table.type.EnumerationTableRows;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.GenericValue;
 import com.netspective.commons.value.PresentationValue;
 import com.netspective.commons.value.Value;
 import com.netspective.commons.value.ValueContext;
 import com.netspective.commons.value.source.AbstractValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 import com.netspective.sparx.Project;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.sparx.navigate.NavigationPath;
 import com.netspective.sparx.navigate.NavigationTree;
 import com.netspective.sparx.navigate.entity.EntityRedirectorPage.EntitySubtypeRedirectInfo;
 
 public class EntityPage extends NavigationPage implements EntitySubtypePage
 {
     private static Log log = LogFactory.getLog(EntityPage.class);
 
     public class EntityPageState extends State
     {
         private ActiveEntity activeEntity;
 
         public ActiveEntity getActiveEntity()
         {
             return activeEntity;
         }
 
         public void setActiveEntity(ActiveEntity activeEntity)
         {
             this.activeEntity = activeEntity;
         }
     }
 
     public class EntityPageHeadingValueSource extends AbstractValueSource
     {
         public EntityPageHeadingValueSource()
         {
         }
 
         public boolean hasValue(ValueContext vc)
         {
             return true;
         }
 
         public Value getValue(ValueContext vc)
         {
            return new GenericValue(((EntityPageState) ((NavigationContext) vc).getActiveState()).getActiveEntity().getEntityName());
         }
 
         public PresentationValue getPresentationValue(ValueContext vc)
         {
             return new PresentationValue(getValue(vc));
         }
     }
 
     private EntityRedirectorPage redirectorPage;
     private int entitySubtypeId;
     private String entitySubtypeName;
 
     public EntityPage(NavigationTree owner)
     {
         super(owner);
         setHeading(new EntityPageHeadingValueSource());
         getFlags().setFlag(Flags.HIDDEN_UNLESS_ACTIVE);
     }
 
     public NavigationPath.State constructState()
     {
         return new EntityPageState();
     }
 
     public int getEntitySubTypeId()
     {
         return entitySubtypeId;
     }
 
     public String getEntitySubTypeName()
     {
         return entitySubtypeName;
     }
 
     public void setEntitySubtypeId(int entitySubtypeId)
     {
         this.entitySubtypeId = entitySubtypeId;
     }
 
     public void setEntitySubtypeSchemaEnum(String schemaEnumId)
     {
         String[] params = TextUtils.getInstance().split(schemaEnumId, ",", true);
         if(params.length != 2)
             log.error("the entity-subtype-schema-enum attribute in the entity page requires 2 params: schema.enum-table,enum-id-or-caption");
         else
         {
             Project project = getOwner().getProject();
             Table table = project.getSchemas().getTable(params[0]);
             if(table == null || !(table instanceof EnumerationTable))
                 log.error("the entity-subtype-schema-enum attribute in the entity page has an invalid schema.enum-table: " + params[0]);
             else
             {
                 EnumerationTable enumTable = (EnumerationTable) table;
                 EnumerationTableRows enumRows = (EnumerationTableRows) enumTable.getData();
                 EnumerationTableRow enumRow = enumRows.getByIdOrCaptionOrAbbrev(params[1]);
                 if(enumRow == null)
                     log.error("the entity-subtype-schema-enum attribute in the entity page has an invalid enum value for "+ params[0] +": " + params[1]);
                 else
                 {
                     setEntitySubtypeId(enumRow.getId());
                     setEntitySubtypeName(enumRow.getCaption());
                 }
             }
         }
     }
 
     public void setEntitySubtypeName(String entitySubtypeName)
     {
         this.entitySubtypeName = entitySubtypeName;
     }
 
     public EntityRedirectorPage getRedirectorPage()
     {
         return redirectorPage;
     }
 
     public void setEntityRedirectorPageId(String redirectPageId)
     {
         redirectorPage = (EntityRedirectorPage) getOwner().findPath(redirectPageId).getMatchedPath();
         if(redirectorPage == null)
             throw new RuntimeException("Redirector page with id '"+ redirectPageId +"' not found.");
 
         setRequireRequestParam(redirectorPage.getEntityIdRequestParamName());
         setRetainParams(new StaticValueSource(redirectorPage.getEntityIdRequestParamName()));
     }
 
     public boolean isValid(NavigationContext nc)
     {
         if(! super.isValid(nc))
             return false;
 
         ActiveEntity activeEntity;
 
         // if we're coming from a redirector then it means that we may not need to rerun our queries
         EntitySubtypeRedirectInfo esri = EntityRedirectorPage.getEntitySubtypeRedirectInfo(nc, getRedirectorPage().getEntityIdRequestParamValue(nc));
         if(esri != null)
             activeEntity = (ActiveEntity) esri.getData();
         else
         {
             // if we get here it means we need to run our queries to get the active person
             ConnectionContext cc;
             try
             {
                 cc = nc.getConnection(null, false);
             }
             catch (Exception e)
             {
                 log.error(e);
                 throw new NestableRuntimeException(e);
             }
 
             try
             {
                 activeEntity = redirectorPage.getEntityProfile(nc, cc);
             }
             catch (Exception e)
             {
                 log.error(e);
                 throw new NestableRuntimeException(e);
             }
             finally
             {
                 try
                 {
                     cc.close();
                 }
                 catch (SQLException e)
                 {
                     log.error(e);
                     throw new NestableRuntimeException(e);
                 }
             }
         }
 
         ((EntityPageState) nc.getActiveState()).setActiveEntity(activeEntity);
         return true;
     }
 }
