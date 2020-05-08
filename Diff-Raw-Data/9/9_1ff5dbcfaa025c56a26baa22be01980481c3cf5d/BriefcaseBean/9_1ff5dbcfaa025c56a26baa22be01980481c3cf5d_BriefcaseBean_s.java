 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.briefcase;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.beans.ResultSessionData;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.BriefcaseEngine;
 import com.flexive.shared.interfaces.SearchEngine;
 import com.flexive.shared.search.AdminResultLocations;
 import com.flexive.shared.search.Briefcase;
 import com.flexive.shared.search.BriefcaseEdit;
 import com.flexive.shared.search.FxResultSet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Management of Briefcases.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class BriefcaseBean {
 
     private BriefcaseEngine briefcaseEng;
     private List<Briefcase> list;
     private FxResultSet queryResult;
     private SearchEngine sqlSearchInterface;
     //    private static final String ID_CACHE_KEY = BriefcaseBean.class+"_id";
     //    private long id;
     private BriefcaseEdit briefcase;
     private long aclId;
     private ResultSessionData sessionData;
 
     public BriefcaseBean() {
         briefcaseEng = EJBLookup.getBriefcaseEngine();
         briefcase = new BriefcaseEdit();
     }
 
     /**
      * Retrives all briefcases for the user.
      *
      * @return all briefcases.
      */
     public List<Briefcase> getList() {
         try {
             if (list == null) {
                 // Load only once per request
                list = briefcaseEng.getList(true);
             }
             return list;
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
             return new ArrayList<Briefcase>(0);
         }
     }
 
     public FxResultSet getQueryResult() {
         return queryResult;
     }
 
     public Briefcase getBriefcase() throws FxApplicationException {
         if (briefcase != null && briefcase.getId() != getId() && getId() != -1) {
             // autodetect new ID
             load();
         }
         return briefcase;
     }
 
 
     public long getAclId() {
         return aclId;
     }
 
     public void setAclId(long aclId) {
         this.aclId = aclId;
         if (briefcase != null) briefcase.setAcl(aclId);
     }
 
     public String save() {
         try {
             briefcaseEng.modify(getId(), briefcase.getName(), briefcase.getDescription(), briefcase.getAcl());
             new FxFacesMsgInfo("Briefcase.nfo.updateSuccessfull", briefcase.getName()).addToContext();
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         return load();
     }
 
     public String load() {
         try {
             briefcase = briefcaseEng.load(getId()).asEditable();
             aclId = briefcase.getAcl();
             if (sqlSearchInterface == null) {
                 sqlSearchInterface = EJBLookup.getSearchEngine();
             }
             String sSql = "select m.id,m.version from content m filter briefcase=" + getId();
             queryResult = sqlSearchInterface.search(sSql, 1, null, null);
             return "briefcaseDetail";
         } catch (Exception exc) {
             queryResult = null;
             new FxFacesMsgErr(exc).addToContext();
             return "briefcaseNotLoaded";
         }
     }
 
     public long getId() {
         return getSessionData().getBriefcaseId();
         /*if (this.id<=0) {
             Object value = FxJsfUtils.getSessionAttribute(ID_CACHE_KEY);
             this.id = value==null?-1:(Long)value;
         }
         return id;*/
     }
 
     public ResultSessionData getSessionData() {
         if (sessionData == null) {
             // TODO: use own briefcase location
             sessionData = ResultSessionData.getSessionData(FxJsfUtils.getSession(), AdminResultLocations.ADMIN);
         }
         return sessionData;
     }
 
     public void setId(long briefcaseId) {
         getSessionData().setBriefcaseId(briefcaseId);
         /*this.id= briefcaseId;
         FxJsfUtils.setSessionAttribute(ID_CACHE_KEY,id);*/
     }
 
     /**
      * Returns true if there are no briefcases to display.
      *
      * @return true if there are no briefcases to display
      */
     public boolean getListIsEmpty() {
         return getList().size() == 0;
     }
 
     /**
      * Returns the briefcase specified by the property id.
      *
      * @return the next page to render
      */
     public String delete() {
         try {
             long _id = getId();
             String name = briefcaseEng.load(_id).getName();
             briefcaseEng.remove(_id);
             new FxFacesMsgInfo("Briefcase.nfo.deleteSuccessfull", name).addToContext();
         } catch (Throwable t) {
             new FxFacesMsgErr(t).addToContext();
         }
         return "briefcaseNotLoaded";
     }
 
 }
