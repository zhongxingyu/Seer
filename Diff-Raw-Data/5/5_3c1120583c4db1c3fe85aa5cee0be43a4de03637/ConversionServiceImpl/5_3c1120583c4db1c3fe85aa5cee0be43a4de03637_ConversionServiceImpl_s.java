 package eionet.webq.service;
 
 import eionet.webq.dto.Conversion;
 import eionet.webq.dto.ListConversionResponse;
 import eionet.webq.dto.UploadedXmlFile;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.http.HttpEntity;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Component;
 import org.springframework.util.LinkedMultiValueMap;
 import org.springframework.util.MultiValueMap;
 import org.springframework.web.client.RestOperations;
 
 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Web Questionnaires 2
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Anton Dmitrijev
  */
 
 /**
  * Conversion service implementation.
  */
 @Component
 public class ConversionServiceImpl implements ConversionService {
     /**
      * Static logger for this class.
      */
     private static final Logger LOGGER = Logger.getLogger(ConversionServiceImpl.class);
     /**
      * Template for calling rest services.
      */
     @Autowired
     RestOperations restOperations;
     /**
      * Url to converters api.
      */
     @Value("#{application_properties['converters.api.url']}")
     private String converterApiUrl;
     /**
      * Template for conversions list method call.
      */
     @Value("#{application_properties['list.conversions.call.template']}")
     private String listConversions;
     /**
      * Template for convertPush method call.
      */
     @Value("#{application_properties['convert.push.call.template']}")
     private String convertPush;
     /**
      * Convert push file parameter name.
      */
     @Value("#{application_properties['convert.push.file.parameter']}")
     private String convertPushFileParameter;
     /**
      * Convert push file parameter name.
      */
     @Value("#{application_properties['convert.push.conversion.id.parameter']}")
     private String convertPushIdParameter;
 
     @Override
     public byte[] convert(UploadedXmlFile fileContent, int conversionId) {
         MultiValueMap<String, Object> request = new LinkedMultiValueMap<String, Object>();
         request.add(convertPushFileParameter, createFileHttpEntity(fileContent));
         request.add(convertPushIdParameter, new HttpEntity<String>(Integer.toString(conversionId)));
 
         String conversionResult = restOperations.postForObject(apiCallTo(convertPush), request, String.class);
         LOGGER.info("Response from conversion service for file=" + fileContent.getName() + ", conversionId=" + conversionId + "\n"
                 + conversionResult);
        return conversionResult.getBytes();
     }
 
     @Cacheable(value = "conversions")
     @Override
     public List<Conversion> conversionsFor(String schema) {
         return restOperations.getForObject(apiCallTo(listConversions), ListConversionResponse.class, schema).getConversions();
     }
 
     /**
      * Creates and sets required headers for file push.
      *
      * @param fileContent
      *            file content and name
      * @return new {@link HttpEntity} with required headers and body set
      */
     private HttpEntity<byte[]> createFileHttpEntity(UploadedXmlFile fileContent) {
         HttpHeaders fileHttpHeaders = new HttpHeaders();
         fileHttpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
         fileHttpHeaders.setContentDispositionFormData(convertPushFileParameter, fileContent.getName());
         return new HttpEntity<byte[]>(fileContent.getContent(), fileHttpHeaders);
     }
 
     /**
      * Creates api call url.
      *
      * @param path call path
      * @return api url
      */
     private String apiCallTo(String path) {
         return converterApiUrl + path;
     }
 }
