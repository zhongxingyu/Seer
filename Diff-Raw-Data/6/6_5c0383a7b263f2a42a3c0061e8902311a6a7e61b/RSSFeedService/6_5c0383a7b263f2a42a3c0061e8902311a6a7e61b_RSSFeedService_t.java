 /*
  * (C) Copyright 2011 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Nuxeo - initial API and implementation
  */
 
 package org.nuxeo.rss.reader.service;
 
 import java.util.List;
 
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 
 /**
  * Service to provide some FeedReader features
  *
  * @author <a href="mailto:akervern@nuxeo.com">Arnaud Kervern</a>
  * @since 5.4.2
  */
 public interface RSSFeedService {
 
     void createRssFeedModelContainerIfNeeded(CoreSession session)
             throws ClientException;
 
     String getCurrentUserRssFeedModelContainerPath(CoreSession session)
             throws ClientException;
 
     String getRssFeedModelContainerPath();
 
     int getDisplayedArticleCount(CoreSession session) throws ClientException;
 
     int getMaximumFeedsCount(CoreSession session) throws ClientException;

     List<String> getUserRssFeedAddresses(CoreSession session)
             throws ClientException;
 
     /**
      * return user feeds
      *
      * @param session
      * @return
      * @throws ClientException
      */
     DocumentModelList getUserRssFeedDocumentModelList(CoreSession session)
             throws ClientException;
 
     /**
      * return feed defined by administrator
      *
      * @param session
      * @return
      * @throws ClientException
      */
     DocumentModelList getGlobalFeedsDocumentModelList(CoreSession session)
             throws ClientException;
 
 }
