 /**
  * Copyright 2011 Jason Ferguson.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.jason.mapmaker.server.service;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.ftp.FTPReply;
 import org.jason.mapmaker.server.repository.ShapefileMetadataRepository;
 import org.jason.mapmaker.shared.exceptions.ServiceException;
 import org.jason.mapmaker.shared.model.MTFCC;
 import org.jason.mapmaker.shared.model.ShapefileMetadata;
 import org.jason.mapmaker.shared.util.GeographyUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Implementation of ShapefileMetadataService interface
  *
  * @author Jason Ferguson
  * @since 0.4
  */
@SuppressWarnings({"FieldCanBeLocal"})
 @Service("shapefileMetadataService")
 public class ShapefileMetadataServiceImpl implements ShapefileMetadataService {
 
     private ShapefileMetadataRepository shapefileMetadataRepository;
     private MtfccService mtfccService;
 
     @Autowired
     public void setShapefileMetadataRepository(ShapefileMetadataRepository shapefileMetadataRepository) {
         this.shapefileMetadataRepository = shapefileMetadataRepository;
     }
 
     @Autowired
     public void setMtfccService(MtfccService mtfccService) {
         this.mtfccService = mtfccService;
     }
 
     private String workingDirectoryFormat = "/geo/pvs/tiger%sst/%s_%s/%s/";
     private String urlPostfix = "tl_%s_%s_%s%s.zip";
     private String cdUrlPostfix = "tl_%s_%s_cd111.zip";
 
     public void generateMetadata(String year) throws ServiceException {
 
         String host = "ftp2.census.gov";
 
         for (String geoId : GeographyUtils.stateGeoIdMap.keySet()) {
             String stateName = GeographyUtils.getStateForGeoId(geoId);
 
             // generate the working directory. String.format() used because it's cleaner than the old String
             // concats
             String workingDirectory = String.format(workingDirectoryFormat, year, geoId, stateName, geoId);
 
             List<String> fileNames = getRemoteFilenames(host, workingDirectory);
             List<ShapefileMetadata> shapefileMetadataList = new ArrayList<ShapefileMetadata>(fileNames.size());
 
             for (String mangledName : GeographyUtils.mangledNamesMap.inverse().keySet()) {
                 String remoteFilename;
                 String shortYear = StringUtils.right(year, 2);  // need the last two digits of the year
 
                 if (mangledName.equals("CD111")) {
                     remoteFilename = String.format(cdUrlPostfix, year, geoId);
                 } else {
                     remoteFilename = String.format(urlPostfix, year, geoId, mangledName.toLowerCase(), shortYear);
                 }
 
                 String demangledName = GeographyUtils.demangleTypeName(mangledName);   // i.e. COUSUB to COUNTY_SUBDIVISION
                 String mtfcc = GeographyUtils.getMtfccForName(demangledName);
 
                 ShapefileMetadata sm = new ShapefileMetadata();
                 sm.setGeoId(geoId);
                 sm.setMtfccCode(mtfcc);
                 sm.setUrl("ftp://" + host + workingDirectory + remoteFilename);
                 sm.setVersion(year);
 
                 if (fileNames.contains(remoteFilename)) {
                     sm.setCurrentStatus(GeographyUtils.Status.NOT_IMPORTED);
                 } else {
                     sm.setCurrentStatus(GeographyUtils.Status.NOT_AVAILABLE);
                 }
 
                 shapefileMetadataList.add(sm);
             }
 
             try {
                 shapefileMetadataRepository.saveList(shapefileMetadataList);
             } catch (Exception e) {
                 throw new ServiceException(e);
             }
         }
     }
 
     /**
      * Method to populate the ShapefileMetadata repository, but based on the state-based FTP site directories instead
      * of the feature-based sites.
      */
     public void generateMetadata2() throws ServiceException {
 
         String host = "ftp2.census.gov";
         for (String geoId : GeographyUtils.stateGeoIdMap.keySet()) {
 
             String stateName = GeographyUtils.getStateForGeoId(geoId);
             String workingDirectory = "/geo/pvs/tiger2010st/" + geoId + "_" + stateName + "/" + geoId + "/";
             List<String> fileNames = getRemoteFilenames(host, workingDirectory);
             List<ShapefileMetadata> shapefileMetadataList = new ArrayList<ShapefileMetadata>(fileNames.size());
             for (String mangledName : GeographyUtils.mangledNamesMap.inverse().keySet()) {
 
                 String remoteFilename;
                 if (!mangledName.equals("CD111")) {
                     remoteFilename = "tl_2010_" + geoId + "_" + mangledName.toLowerCase() + "10.zip";
                 } else {
                     remoteFilename = "tl_2010_" + geoId + "_cd111.zip";
                 }
                 String demangledName = GeographyUtils.demangleTypeName(mangledName);   // i.e. COUSUB to COUNTY_SUBDIVISION
                 String mtfcc = GeographyUtils.getMtfccForName(demangledName);
 
                 ShapefileMetadata sm = new ShapefileMetadata();
                 sm.setGeoId(geoId);
                 sm.setMtfccCode(mtfcc);
                 sm.setUrl("ftp://" + host + workingDirectory + remoteFilename);
                 sm.setVersion("2010");
 
                 if (fileNames.contains(remoteFilename)) {
                     sm.setCurrentStatus(GeographyUtils.Status.NOT_IMPORTED);
                 } else {
                     sm.setCurrentStatus(GeographyUtils.Status.NOT_AVAILABLE);
                 }
 
                 shapefileMetadataList.add(sm);
             }
 
             try {
                 shapefileMetadataRepository.saveList(shapefileMetadataList);
             } catch (Exception e) {
                 throw new ServiceException(e);
             }
         }
     }
 
     public long getCount() {
         return shapefileMetadataRepository.getCount();
     }
 
 
     public List<ShapefileMetadata> getByGeoId(String geoId) {
 
         return shapefileMetadataRepository.getByGeoId(geoId);
 
     }
 
     private List<String> getRemoteFilenames(String url, String directory) {
 
         FTPClient ftp = new FTPClient();
         List<String> filenameList = new ArrayList<String>();
 
         try {
             int reply;
             ftp.connect(url);
             reply = ftp.getReplyCode();
             if (!FTPReply.isPositiveCompletion(reply)) {
                 ftp.disconnect();
                 System.out.println("FTP connection failed for " + url);
             }
             ftp.enterLocalPassiveMode();
             ftp.login("anonymous", "");
             FTPFile[] files = ftp.listFiles(directory);
             for (FTPFile f : files) {
                 filenameList.add(f.getName());
             }
 
             ftp.logout();
 
         } catch (IOException ex) {
 
             ex.printStackTrace();
         }
 
         return filenameList;
     }
 
     public ShapefileMetadata save(ShapefileMetadata sm) throws ServiceException {
         return shapefileMetadataRepository.save(sm);
     }
 
     @Override
     public void update(ShapefileMetadata sm) throws ServiceException {
         shapefileMetadataRepository.update(sm);
     }
 
     @Override
     public void deleteAll() throws ServiceException {
         shapefileMetadataRepository.deleteAll();
     }
 
     @Override
     public ShapefileMetadata getStateShapefileMetadata(String geoId) {
 
         String stateGeoId = StringUtils.left(geoId, 2);     // no matter the feature type, the first two digits are always the state
         MTFCC m = mtfccService.get(GeographyUtils.MTFCC.STATE);
 
         ShapefileMetadata example = new ShapefileMetadata();
         example.setGeoId(stateGeoId);
         example.setMtfccCode(m.getMtfccCode());
 
         List<ShapefileMetadata> resultList = shapefileMetadataRepository.queryByExample(example);
 
        return resultList.get(0);
     }
 }
