 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.
  * All rights reserved.  Use is subject to license terms.
  */
 
 package de.escidoc.core.test.sb;
 
 import java.util.HashMap;
 
 /**
  * Constants for Search-Tests.
  * 
  * @author MIH
  */
 public final class Constants {
     public static final int NUM_ITEMS = 10;
 
     public static final int NUM_CONTAINERS = 1;
 
     public static final int NUM_OAIPMH_ITEMS = 2;
 
     public static final int NUM_OAIPMH_CONTAINERS = 1;
 
     public static final int NUM_ORG_UNITS = 7;
 
     public static final int INDEX_FIELD_COUNT = 85;
 
     public static final int SORT_FIELD_COUNT = 83;
 
     public static final int ITEM_CONTAINER_ADMIN_INDEX_FIELD_COUNT = 179;
 
     public static final int ITEM_CONTAINER_ADMIN_SORT_FIELD_COUNT = 175;
 
     public static final int CONTENT_MODEL_ADMIN_INDEX_FIELD_COUNT = 82;
 
     public static final int CONTENT_MODEL_ADMIN_SORT_FIELD_COUNT = 83;
 
     public static final int CONTENT_RELATION_ADMIN_INDEX_FIELD_COUNT = 15;
 
     public static final int CONTENT_RELATION_ADMIN_SORT_FIELD_COUNT = 13;
 
     public static final int CONTEXT_ADMIN_INDEX_FIELD_COUNT = 82;
 
     public static final int CONTEXT_ADMIN_SORT_FIELD_COUNT = 83;
 
    public static final int OU_INDEX_FIELD_COUNT = 93;
 
    public static final int OU_SORT_FIELD_COUNT = 92;
 
     public static final int OAIPMH_INDEX_FIELD_COUNT = 66;
 
     public static final int OAIPMH_SORT_FIELD_COUNT = 1;
 
     public static final int CONTEXT_INDEX_FIELD_COUNT = 21;
 
     public static final int CONTEXT_SORT_FIELD_COUNT = 20;
 
     public static final int CONTENT_MODEL_INDEX_FIELD_COUNT = 22;
 
     public static final int CONTENT_MODEL_SORT_FIELD_COUNT = 21;
 
     public static final int CONTENT_RELATION_INDEX_FIELD_COUNT = 20;
 
     public static final int CONTENT_RELATION_SORT_FIELD_COUNT = 19;
 
     /**
      * Private Constructor.
      * 
      */
     private Constants() {
 
     }
 
     public static final HashMap<String, HashMap<String, String>> 
                                     ITEM_INDEX_USERDEFINED_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("metadata", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=hdl*");
                     put("expectedHits", "10");
                 }
             });
             put("metadata1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=ISSN");
                     put("expectedHits", "10");
                 }
             });
             put("metadata2", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=fire");
                     put("expectedHits", "1");
                 }
             });
             put("context.objid", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.context.objid=escidoc*");
                     put("expectedHits", "10");
                 }
             });
             put("content-model.objid", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.content-model.objid=escidoc*");
                     put("expectedHits", "10");
                 }
             });
             put("creator.role", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creator.role=author");
                     put("expectedHits", "10");
                 }
             });
             put("creator.role1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creator.role=artist");
                     put("expectedHits", "10");
                 }
             });
             put("most-recent-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", 
                             "escidoc.most-recent-date=\"1980-01-27*\"");
                     put("expectedHits", "3");
                 }
             });
             put("most-recent-date.status", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.most-recent-date.status=escidoc.created");
                     put("expectedHits", "2");
                 }
             });
             put("most-recent-date.status1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.most-recent-date.status=escidoc.modified");
                     put("expectedHits", "2");
                 }
             });
             put("most-recent-date.status2", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.most-recent-date.status="
                             + "escidoc.dateSubmitted");
                     put("expectedHits", "2");
                 }
             });
             put("most-recent-date.status3", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.most-recent-date.status="
                             + "escidoc.dateAccepted");
                     put("expectedHits", "2");
                 }
             });
             put("most-recent-date.status4", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.most-recent-date.status=escidoc.issued");
                     put("expectedHits", "1");
                 }
             });
             put("most-recent-date.status5", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.most-recent-date.status="
                             + "escidoc.published-online");
                     put("expectedHits", "1");
                 }
             });
             put("context.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.context.name=\"Test Collection\"");
                     put("expectedHits", "10");
                 }
             });
             put("content-model.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", 
                       "escidoc.content-model.name=\"Persistent Content Model\"");
                     put("expectedHits", "10");
                 }
             });
             put("created-by.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.created-by.name="
                             + "\"Test System Administrator User\"");
                     put("expectedHits", "10");
                 }
             });
             put("component.created-by.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.component.created-by.name="
                             + "\"Test System Administrator User\"");
                     put("expectedHits", "10");
                 }
             });
             put("publication.type", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.publication.type=article");
                     put("expectedHits", "10");
                 }
             });
             put("last-modification-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.last-modification-date=\"20*\"");
                     put("expectedHits", "10");
                 }
             });
             put("type", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.type=article");
                     put("expectedHits", "10");
                 }
             });
             put("source.type", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.source.type=articlesource");
                     put("expectedHits", "10");
                 }
             });
             put("source.type1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.source.type=articlesource1");
                     put("expectedHits", "10");
                 }
             });
             put("any-title", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-title=t*");
                     put("expectedHits", "10");
                 }
             });
             put("any-topic", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-topic=t*");
                     put("expectedHits", "10");
                 }
             });
             put("any-persons", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-persons=t*");
                     put("expectedHits", "5");
                 }
             });
             put("any-organizations", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-organizations=t*");
                     put("expectedHits", "10");
                 }
             });
             put("any-organization-pids", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put(
                             "searchString",
                             "escidoc.any-organization-pids=escidoc:persistent25");
                     put("expectedHits", "10");
                 }
             });
             put("any-organization-pids1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put(
                             "searchString",
                             "escidoc.any-organization-pids=escidoc:persistent29");
                     put("expectedHits", "10");
                 }
             });
             put("any-genre", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-genre=article");
                     put("expectedHits", "10");
                 }
             });
             put("any-dates", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-dates<1980");
                     put("expectedHits", "0");
                 }
             });
             put("any-dates1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-dates>1980");
                     put("expectedHits", "10");
                 }
             });
             put("any-event", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-event=t*");
                     put("expectedHits", "10");
                 }
             });
             put("any-source", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-source=t*");
                     put("expectedHits", "10");
                 }
             });
             put("any-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-identifier=escidoc:*");
                     put("expectedHits", "10");
                 }
             });
             put("any-identifier1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-identifier="
                             + "\"hdl:somehandle/test/${ITEM_ID}\"");
                     put("expectedHits", "1");
                 }
             });
             put("nichtindexiert1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert3", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creator.role=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert4", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-title=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert5", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-topic=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert6", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-persons=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert7", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.any-organizations=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert8", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-event=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert9", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-source=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 CONTAINER_INDEX_USERDEFINED_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("metadata", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=hdl*");
                     put("expectedHits", "1");
                 }
             });
             put("metadata1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=ISSN");
                     put("expectedHits", "1");
                 }
             });
             put("context.objid", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.context.objid=escidoc*");
                     put("expectedHits", "1");
                 }
             });
             put("content-model.objid", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.content-model.objid=escidoc*");
                     put("expectedHits", "1");
                 }
             });
             put("creator.role", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creator.role=author");
                     put("expectedHits", "1");
                 }
             });
             put("creator.role1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creator.role=artist");
                     put("expectedHits", "1");
                 }
             });
             put("most-recent-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", 
                             "escidoc.most-recent-date=\"1980-01-27*\"");
                     put("expectedHits", "1");
                 }
             });
             put("most-recent-date.status", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.most-recent-date.status=escidoc.created");
                     put("expectedHits", "0");
                 }
             });
             put("most-recent-date.status1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.most-recent-date.status=escidoc.modified");
                     put("expectedHits", "1");
                 }
             });
             put("most-recent-date.status2", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.most-recent-date.status="
                             + "escidoc.dateSubmitted");
                     put("expectedHits", "0");
                 }
             });
             put("most-recent-date.status3", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.most-recent-date.status="
                             + "escidoc.dateAccepted");
                     put("expectedHits", "0");
                 }
             });
             put("most-recent-date.status4", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.most-recent-date.status=escidoc.issued");
                     put("expectedHits", "0");
                 }
             });
             put("most-recent-date.status5", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.most-recent-date.status="
                             + "escidoc.published-online");
                     put("expectedHits", "0");
                 }
             });
             put("context.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.context.name=\"Test Collection\"");
                     put("expectedHits", "1");
                 }
             });
             put("content-model.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", 
                        "escidoc.content-model.name=\"Persistent Content Model\"");
                     put("expectedHits", "1");
                 }
             });
             put("created-by.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.created-by.name="
                             + "\"Test System Administrator User\"");
                     put("expectedHits", "1");
                 }
             });
             put("last-modification-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.last-modification-date=\"20*\"");
                     put("expectedHits", "1");
                 }
             });
             put("member-count", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.member-count=10");
                     put("expectedHits", "1");
                 }
             });
             put("any-title", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-title=t*");
                     put("expectedHits", "1");
                 }
             });
             put("any-topic", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-topic=t*");
                     put("expectedHits", "1");
                 }
             });
             put("any-persons", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-persons=t*");
                     put("expectedHits", "1");
                 }
             });
             put("any-organizations", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-organizations=collection");
                     put("expectedHits", "1");
                 }
             });
             put("any-organization-pids", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put(
                             "searchString",
                             "escidoc.any-organization-pids=escidoc:persistent13");
                     put("expectedHits", "1");
                 }
             });
             put("any-dates", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-dates>1980");
                     put("expectedHits", "1");
                 }
             });
             put("any-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-identifier=escidoc:*");
                     put("expectedHits", "1");
                 }
             });
             put("nichtindexiert1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert3", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creator.role=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert4", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-title=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert5", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-topic=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert6", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-persons=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert7", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.any-organizations=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert8", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-event=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
             put("nichtindexiert9", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-source=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 ORG_UNIT_INDEX_USERDEFINED_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("metadata", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=ISSN");
                     put("expectedHits", "7");
                 }
             });
             put("ancestor-organization-pid", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.ancestor-organization-pid=${ORGUNIT1}");
                     put("expectedHits", "4");
                 }
             });
             put("ancestor-organization-pid1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.ancestor-organization-pid=${ORGUNIT0}");
                     put("expectedHits", "7");
                 }
             });
             put("parent.objid", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.parent.objid=${ORGUNIT0}");
                     put("expectedHits", "2");
                 }
             });
             put("parent.objid1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.parent.objid=${ORGUNIT1}");
                     put("expectedHits", "3");
                 }
             });
             put("any-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.any-identifier=\"1-01-01\"");
                     put("expectedHits", "1");
                 }
             });
             put("created-by.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.created-by.name="
                             + "\"Test System Administrator User\"");
                     put("expectedHits", "7");
                 }
             });
             put("last-modification-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.last-modification-date=\"20*\"");
                     put("expectedHits", "7");
                 }
             });
             put("nichtindexiert1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.metadata=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 OAIPMH_ITEM_INDEX_USERDEFINED_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("md-record-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.md-record-identifier="
                             + "\"escidoc@http://escidoc.mpg.de/"
                             + "metadataprofile/schema/0.1/\"");
                     put("expectedHits", "2");
                 }
             });
             put("md-record-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.md-record-identifier="
                             + "\"technical-md@"
                             + "http://jhove.com/metadata/schema/0.5\"");
                     put("expectedHits", "2");
                 }
             });
             put("last-modification-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.last-modification-date<2050");
                     put("expectedHits", "2");
                 }
             });
             put("family-name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.publication.creator.person"
                             + ".family-name=Gollmer");
                     put("expectedHits", "1");
                 }
             });
             put("family-name1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.publication.creator.person"
                             + ".family-name=Gollmer1");
                     put("expectedHits", "0");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                         OAIPMH_CONTAINER_INDEX_USERDEFINED_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("md-record-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.md-record-identifier="
                             + "\"escidoc@http://escidoc.mpg.de/"
                             + "metadataprofile/schema/0.1/\"");
                     put("expectedHits", "1");
                 }
             });
             put("md-record-identifier", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.md-record-identifier="
                             + "\"technical-md@"
                             + "http://jhove.com/metadata/schema/0.5\"");
                     put("expectedHits", "0");
                 }
             });
             put("last-modification-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.last-modification-date<2050");
                     put("expectedHits", "1");
                 }
             });
             put("family-name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put(
                             "searchString",
                             "escidoc.collection.creator.person.family-name=Hoppe");
                     put("expectedHits", "1");
                 }
             });
             put("family-name1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.collection.creator.person"
                             + ".family-name=Hoppe1");
                     put("expectedHits", "0");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                     ITEM_INDEX_PROPERTIES_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("creation-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creation-date=\"20*\"");
                     put("expectedHits", "10");
                 }
             });
             put("public-status", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.public-status=released");
                     put("expectedHits", "10");
                 }
             });
             put("public-status-comment", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.public-status-comment=released");
                     put("expectedHits", "10");
                 }
             });
             put("content-model-specific.element1",
                     new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.content-model-specific.element1="
                             + "cms-test1");
                     put("expectedHits", "10");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 CONTAINER_INDEX_PROPERTIES_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("creation-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creation-date=\"20*\"");
                     put("expectedHits", "1");
                 }
             });
             put("public-status", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.public-status=released");
                     put("expectedHits", "1");
                 }
             });
             put("public-status-comment", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.public-status-comment=containerhandler*");
                     put("expectedHits", "1");
                 }
             });
             put("content-model-specific.element1",
                     new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.content-model-specific.element1="
                             + "cms-test1");
                     put("expectedHits", "1");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 ORG_UNIT_INDEX_PROPERTIES_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("creation-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.creation-date=\"20*\"");
                     put("expectedHits", "7");
                 }
             });
             put("created-by.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.created-by.name="
                             + "\"Test System Administrator User\"");
                     put("expectedHits", "7");
                 }
             });
             put("public-status", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.public-status=opened");
                     put("expectedHits", "7");
                 }
             });
             put("has-children", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.has-children=true");
                     put("expectedHits", "3");
                 }
             });
             put("has-children1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.has-children=false");
                     put("expectedHits", "4");
                 }
             });
             put("parent", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.parent.objid=${ORGUNIT1}");
                     put("expectedHits", "3");
                 }
             });
             put("parent1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.parent.objid=${ORGUNIT0}");
                     put("expectedHits", "2");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 OAIPMH_ITEM_INDEX_PROPERTIES_SEARCHES =
             new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("latest-release-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.latest-release.date=\"20*\"");
                     put("expectedHits", "2");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                         OAIPMH_CONTAINER_INDEX_PROPERTIES_SEARCHES = 
         new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("latest-release-date", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.latest-release.date=\"20*\"");
                     put("expectedHits", "1");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                 OAIPMH_ITEM_INDEX_METADATA_SEARCHES = 
         new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("creator.role", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.publication.creator.role=author");
                     put("expectedHits", "2");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                             OAIPMH_CONTAINER_INDEX_METADATA_SEARCHES = 
         new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("creator.role", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.collection.creator.role=author");
                     put("expectedHits", "1");
                 }
             });
         }
     };
 
     public static final HashMap<String, HashMap<String, String>> 
                                     ITEM_INDEX_COMPONENTS_SEARCHES = 
         new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("component.created-by.name", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.component.created-by.name="
                             + "\"Test System Administrator User\"");
                     put("expectedHits", "10");
                 }
             });
             put("nichtindexiert2", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.component.metadata=nichtindexiert");
                     put("expectedHits", "0");
                 }
             });
         }
     };
 
     // + - & | ! ( ) { } [ ] ~ : * ? ^ \ " ' /
     // * ? ^ \ " have to be masked in cql
     // + - & | ! ( ) { } [ ] ~ : have not to be masked in cql but in Lucene
     // ' / dont have to be masked at all
     // " , ; ' < > = / . : ) ( ? ! are removed at the end of string by analyzer
     public static final HashMap<String, HashMap<String, String>> 
                                     ITEM_INDEX_SPECIAL_CHAR_SEARCHES = 
         new HashMap<String, HashMap<String, String>>() {
         private static final long serialVersionUID = 1L;
         {
             put("title1", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi+x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title2", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\+x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title3", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi-x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title4", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\-x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title5", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi&x\"");
                     put("expectedHits", "3");
                 }
             });
             put("title6", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\&x\"");
                     put("expectedHits", "3");
                 }
             });
             put("title7", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi|x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title8", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\|x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title9", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi|x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title10", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\|x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title11", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi!x\"");
                     put("expectedHits", "3");
                 }
             });
             put("title12", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\!x\"");
                     put("expectedHits", "3");
                 }
             });
             put("title13", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi(x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title14", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\(x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title15", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi)x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title16", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\)x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title17", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi{x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title18", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\{x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title19", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi}x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title20", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\}x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title21", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi[x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title22", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\[x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title23", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi]x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title24", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\]x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title25", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi~x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title26", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\~x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title27", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi:x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title28", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\:x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title29", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\*x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title30", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\?x\"");
                     put("expectedHits", "3");
                 }
             });
             put("title31", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\^x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title32", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\\\x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title33", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi\\\"x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title34", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi'x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title35", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"titleappendi/x\"");
                     put("expectedHits", "1");
                 }
             });
             put("title36", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix+\"");
                     put("expectedHits", "1");
                 }
             });
             put("title37", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix-\"");
                     put("expectedHits", "1");
                 }
             });
             put("title38", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix&\"");
                     put("expectedHits", "7");
                 }
             });
             put("title39", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix|\"");
                     put("expectedHits", "1");
                 }
             });
             put("title40", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix!\"");
                     put("expectedHits", "7");
                 }
             });
             put("title41", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix(\"");
                     put("expectedHits", "7");
                 }
             });
             put("title42", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix)\"");
                     put("expectedHits", "7");
                 }
             });
             put("title43", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix{\"");
                     put("expectedHits", "1");
                 }
             });
             put("title44", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix}\"");
                     put("expectedHits", "1");
                 }
             });
             put("title45", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix[\"");
                     put("expectedHits", "1");
                 }
             });
             put("title46", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix]\"");
                     put("expectedHits", "1");
                 }
             });
             put("title47", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix~\"");
                     put("expectedHits", "1");
                 }
             });
             put("title48", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix:\"");
                     put("expectedHits", "7");
                 }
             });
             put("title49", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix\\*\"");
                     put("expectedHits", "1");
                 }
             });
             put("title50", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix\\?\"");
                     put("expectedHits", "7");
                 }
             });
             put("title51", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix\\^\"");
                     put("expectedHits", "1");
                 }
             });
             put("title52", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix\\\\\"");
                     put("expectedHits", "1");
                 }
             });
             put("title53", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix\\\"\"");
                     put("expectedHits", "7");
                 }
             });
             put("title54", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix'\"");
                     put("expectedHits", "7");
                 }
             });
             put("title55", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix/\"");
                     put("expectedHits", "7");
                 }
             });
             put("title56", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix,\"");
                     put("expectedHits", "7");
                 }
             });
             put("title57", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix;\"");
                     put("expectedHits", "7");
                 }
             });
             put("title58", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix>\"");
                     put("expectedHits", "7");
                 }
             });
             put("title59", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendix<\"");
                     put("expectedHits", "7");
                 }
             });
             put("title60", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString",
                             "escidoc.alternative=\"alternativeappendi\\x\"");
                     put("expectedHits", "7");
                 }
             });
             put("title61", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"\\\\\"");
                     put("expectedHits", "1");
                 }
             });
             put("title62", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"\\(\"");
                     put("expectedHits", "5");
                 }
             });
             put("title63", new HashMap<String, String>() {
                 private static final long serialVersionUID = 1L;
                 {
                     put("searchString", "escidoc.title=\"(\"");
                     put("expectedHits", "5");
                 }
             });
         }
     };
 
 }
