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
 package com.flexive.war.javascript;
 
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.search.Briefcase;
 import com.flexive.war.JsonWriter;
 
 import java.io.Serializable;
 import java.io.StringWriter;
 
 /**
  * JSON/RPC beans for the briefcase navigation page.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class BriefcaseEditor implements Serializable {
     private static final long serialVersionUID = 7897740955297252921L;
     private static final String EMPTY = "[]";
 
     /**
      * Renders all briefcases of the calling user.
      *
      * @return all briefcases of the calling user (in JSON format).
      * @throws Exception on server-side errors
      */
     public String renderBriefcases() throws Exception {
         final StringWriter out = new StringWriter();
         final JsonWriter writer = new JsonWriter(out);
         writer.startArray();
        for (Briefcase briefcase : EJBLookup.getBriefcaseEngine().getList(true)) {
             writer.startMap()
                     .writeAttribute("id", briefcase.getId())
                     .writeAttribute("name", briefcase.getName())
                     .writeAttribute("aclId", briefcase.getAcl())
                     .writeAttribute("size", briefcase.getSize())
                     .closeMap();
         }
         writer.closeArray().finishResponse();
         return out.toString();
     }
 
     /**
      * Creates a new private briefcase with the given name.
      *
      * @param name the briefcase name
      * @return nothing
      * @throws Exception on server-side errors
      */
     public String create(String name) throws Exception {
         EJBLookup.getBriefcaseEngine().create(name, null, null);
         return EMPTY;
     }
 
     /**
      * Delete the briefcase with the given ID.
      *
      * @param id the briefcase ID
      * @return nothing
      * @throws Exception if the briefcase could not be deleted
      */
     public String remove(long id) throws Exception {
         EJBLookup.getBriefcaseEngine().remove(id);
         return EMPTY;
     }
 
     /**
      * Remove the given items from the briefcase with the given ID.
      *
      * @param id      the briefcase ID
      * @param itemIds the item ids
      * @return nothing
      * @throws Exception if the briefcase could not be deleted
      */
     public String removeItems(long id, long[] itemIds) throws Exception {
         EJBLookup.getBriefcaseEngine().removeItems(id, itemIds);
         return EMPTY;
     }
 
     /**
      * Add the given items to the given briefcase ID.
      *
      * @param id      the briefcase id
      * @param itemIds the item id(s)
      * @return nothing
      * @throws Exception if the briefcase could not be deleted
      */
     public String add(long id, long[] itemIds) throws Exception {
         EJBLookup.getBriefcaseEngine().addItems(id, itemIds);
         return EMPTY;
     }
 
     /**
      * Rename a briefcase.
      *
      * @param id   the briefcase ID
      * @param name the new briefcase name
      * @return nothing
      * @throws Exception if the briefcase could not be renamed
      */
     public String rename(long id, String name) throws Exception {
         EJBLookup.getBriefcaseEngine().modify(id, name, null, null);
         return EMPTY;
     }
 
     /**
      * Share a briefcase.
      *
      * @param id    the briefcase ID
      * @param aclId the ACL id. Set to -1 to "un-share" the briefcase.
      * @return nothing
      * @throws Exception if the briefcase could not be shared
      */
     public String share(long id, long aclId) throws Exception {
         EJBLookup.getBriefcaseEngine().modify(id, null, null, aclId);
         return EMPTY;
     }
 }
