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
 import java.io.*;
 import java.util.List;
 
 public class ArchivedFileDownloadServlet extends BaseDownloadServlet
 {
     private transient final Logger logger = LoggerFactory.getLogger(getClass());
 
     private static class ExactCaseInsensitiveFilenameFilter implements FilenameFilter
     {
         private String filename;
 
         public ExactCaseInsensitiveFilenameFilter( String filename )
         {
             this.filename = null != filename ? filename : null;
         }
 
         public boolean accept(File file, String s)
         {
             return filename.equalsIgnoreCase(s);
         }
     }
 
     protected boolean isRandomAccessSupported()
     {
         return false;
     }
 
     protected InputStream getInputStream( File f ) throws IOException
     {
         if (f instanceof de.schlichtherle.io.File ) {
             return new de.schlichtherle.io.FileInputStream(f);
         } else {
             return new FileInputStream(f);
         }
     }
 
     protected File validateRequest( HttpServletRequest request, HttpServletResponse response, List<String> userIDs )
             throws DownloadServletException
     {
         RegexHelper PARSE_ARGUMENTS_REGEX = new RegexHelper("/([^/]+)/([^/]+)/([^/]+)$", "i");
         String[] requestArgs = PARSE_ARGUMENTS_REGEX.match(request.getRequestURL().toString());
 
         if (null == requestArgs || requestArgs.length != 3
                 || "".equals(requestArgs[0]) || "".equals(requestArgs[1])
                 || "".equals(requestArgs[2])) {
             throw new DownloadServletException(
                     "Bad arguments passed via request URL ["
                             + request.getRequestURL().toString()
                             + "]"
             );
         }
 
         File file;
 
         String accession = requestArgs[0];
         String archName = requestArgs[1];
         String fileName = requestArgs[2];
 
         logger.info("Requested download of [{}] from archive [{}],  accession [" + accession + "]", fileName, archName);
         Files files = (Files) getComponent("Files");
         Experiments experiments = (Experiments) getComponent("Experiments");
 
         try {
 
             if (!files.doesExist(accession, archName)) {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                 throw new DownloadServletException(
                         "Archive with name ["
                                 + archName
                                 + "], accession ["
                                 + accession
                                 + "] is not in files.xml");
             } else {
                 String archLocation = files.getLocation(accession, archName);
 
                 // finally if there is no accession or location determined at the stage - panic
                 if ("".equals(archLocation) || "".equals(accession)) {
                     response.sendError(HttpServletResponse.SC_NOT_FOUND);
                     throw new DownloadServletException(
                             "Either accession ["
                                     + String.valueOf(accession)
                                     + "] or location ["
                                     + String.valueOf(archLocation)
                                     + "] were not determined");
                 }
 
                if (!experiments.isAccessible(accession, userIDs)) {
                     response.sendError(HttpServletResponse.SC_FORBIDDEN);
                     throw new DownloadServletException(
                             "The experiment ["
                                     + accession
                                     + "] is not accessible for user id(s) ["
                                     + StringTools.arrayToString(userIDs.toArray(new String[userIDs.size()]), ", ")
                                     + "]"
                     );
                 }
 
                 logger.debug("Will be serving archive [{}]", archLocation);
                 de.schlichtherle.io.File archFile = new de.schlichtherle.io.File(files.getRootFolder(), archLocation);
                 File[] entries = archFile.listFiles(new ExactCaseInsensitiveFilenameFilter(fileName));
 
                 if (null == entries || entries.length != 1) {
                 response.sendError(HttpServletResponse.SC_NOT_FOUND);
                     throw new DownloadServletException(
                             "File ["
                                     + fileName
                                     + "] was not found or there are multiple files with the name inside archive ["
                                     + String.valueOf(archLocation)
                                     + "]");
 
                 } else {
                     file = entries[0];
                 }
             }
         } catch (DownloadServletException x) {
             throw x;
         } catch (Exception x) {
             throw new DownloadServletException(x);
         }
         return file;
     }
 }
