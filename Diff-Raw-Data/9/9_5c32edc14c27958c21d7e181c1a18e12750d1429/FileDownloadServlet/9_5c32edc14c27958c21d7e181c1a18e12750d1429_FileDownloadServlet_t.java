 package uk.ac.ebi.arrayexpress.servlets;
 
 /*
  * Copyright 2009-2011 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.arrayexpress.components.Experiments;
 import uk.ac.ebi.arrayexpress.components.Files;
 import uk.ac.ebi.arrayexpress.utils.RegexHelper;
 import uk.ac.ebi.arrayexpress.utils.StringTools;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 public class FileDownloadServlet extends BaseDownloadServlet
 {
     private transient final Logger logger = LoggerFactory.getLogger(getClass());
 
     protected boolean isRandomAccessSupported()
     {
         return true;
     }
 
     protected InputStream getInputStream( File f ) throws IOException
     {
         return new FileInputStream(f);
     }
 
     protected File validateRequest( HttpServletRequest request, HttpServletResponse response, List<String> userIDs )
             throws DownloadServletException
     {
         String accession = "";
         String name = "";
         File file;
 
         try {
             String[] requestArgs = new RegexHelper("/download/([^/]+)/?([^/]*)", "i")
                     .match(request.getRequestURL().toString());
             if (null != requestArgs) {
                 if (requestArgs[1].equals("")) {
                     name = requestArgs[0]; // old-style
                 } else {
                     accession = requestArgs[0];
                     name = requestArgs[1];
                 }
             }
             logger.info("Requested download of [{}], accession [{}]", name, accession);
             Files files = (Files) getComponent("Files");
             Experiments experiments = (Experiments) getComponent("Experiments");
 
 
 
             if (!files.doesExist(accession, name)) {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 throw new DownloadServletException(
                         "File with name ["
                                 + name
                                 + "], accession ["
                                 + accession
                                 + "] is not in files.xml");
             } else {
                 String fileLocation = files.getLocation(accession, name);
 
                 if (!"".equals(fileLocation) && "".equals(accession)) {
                     // attempt to resolve accession for file by its location
                     accession = files.getAccession(fileLocation);
                 }
 
                 // finally if there is no accession or location determined at the stage - panic
                 if ("".equals(fileLocation) || "".equals(accession)) {
                     response.sendError(HttpServletResponse.SC_NOT_FOUND);
                     throw new DownloadServletException(
                             "Either accession ["
                                     + String.valueOf(accession)
                                     + "] or location ["
                                     + String.valueOf(fileLocation)
                                     + "] were not determined");
                 }
 
                if (!(null != userIDs && (0 == userIDs.size() || experiments.isAccessible(accession, userIDs)))) {
                     response.sendError(HttpServletResponse.SC_FORBIDDEN);
                     throw new DownloadServletException(
                             "The experiment ["
                                     + accession
                                     + "] is not accessible for user id(s) ["
                                     + StringTools.arrayToString(userIDs.toArray(new String[userIDs.size()]), ", ")
                                     + "]"
                     );
                 }
 
                 logger.debug("Will be serving file [{}]", fileLocation);
                 file = new File(files.getRootFolder(), fileLocation);
             }
         } catch (DownloadServletException x) {
             throw x;
         } catch (Exception x) {
             throw new DownloadServletException(x);
         }
         return file;
     }
 }
