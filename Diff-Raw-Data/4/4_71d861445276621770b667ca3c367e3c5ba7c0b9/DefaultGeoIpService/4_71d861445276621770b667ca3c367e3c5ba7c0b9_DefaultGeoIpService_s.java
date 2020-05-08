 package com.aciertoteam.geo.services.impl;
 
 import com.aciertoteam.common.service.impl.DefaultEntityService;
 import com.aciertoteam.geo.entity.Country;
 import com.aciertoteam.geo.services.GeoIpService;
 import com.aciertoteam.io.exceptions.FileException;
 import com.maxmind.geoip.LookupService;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * @author Bogdan Nechyporenko
  */
 @Service
 @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
 public class DefaultGeoIpService extends DefaultEntityService implements GeoIpService {
 
     private static final Logger LOG = Logger.getLogger(DefaultGeoIpService.class);
 
     @Value("${geo.ip.file.path}")
     private String geoIpFilePath;
 
     @Value("${geo.ip.file.local.path}")
     private String geoIpFileLocalPath;
 
     private static final String DEFAULT_COUNTRY = "netherlands";
 
     @Override
     public Country defineCountry(String ipAddress) {
         try {
             LOG.info(String.format("Ip Address: %s", ipAddress));
             String countryName = getCountryName(ipAddress);
             String countryLabel = String.format("label.country.%s", countryName).toLowerCase();
             LOG.info("Country label: " + countryLabel);
             Country country = findByField(Country.class, "name", countryLabel);
             if (country != null) {
                 country.setIpAddress(ipAddress);
             }
             return country;
         } catch (IOException e) {
             LOG.error(e.getMessage(), e);
         }
         return null;
     }
 
     private String getCountryName(String ipAddress) throws IOException {
         String countryName = getGeoLookupService().getCountry(ipAddress).getName();
        if ("n/a".equals(countryName)) {
             return  DEFAULT_COUNTRY;
         }
         return countryName;
     }
 
     private LookupService getGeoLookupService() throws IOException {
         return new LookupService(getLocalGeoFilePath(), LookupService.GEOIP_MEMORY_CACHE);
     }
 
     private String getLocalGeoFilePath() {
         File geoIpFileLocalPath = new File(getLocalGeoFolderPath(), "geoIp.dat");
         if (!geoIpFileLocalPath.exists()) {
             InputStream inputStream = getClass().getResourceAsStream(geoIpFilePath);
             FileOutputStream outputStream = null;
             try {
                 outputStream = new FileOutputStream(geoIpFileLocalPath);
                 IOUtils.copy(inputStream, new FileOutputStream(geoIpFileLocalPath));
             } catch (IOException e) {
                 throw new FileException(e.getMessage(), e);
             } finally {
                 IOUtils.closeQuietly(outputStream);
             }
         }
         return geoIpFileLocalPath.getPath();
     }
 
     private String getLocalGeoFolderPath() {
         File folder = new File(geoIpFileLocalPath);
         if (!folder.exists()) {
             try {
                 FileUtils.forceMkdir(folder);
             } catch (IOException e) {
                 throw new FileException(e.getMessage(), e);
             }
         }
         return folder.getPath();
     }
 }
 
