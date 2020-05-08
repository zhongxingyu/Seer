 package com.testdroid.api.sample.util;
 
 import com.testdroid.api.APIClient;
 import com.testdroid.api.APIEntity;
 import com.testdroid.api.APIException;
 import com.testdroid.api.APIExceptionMessage;
 import com.testdroid.api.DefaultAPIClient;
 import java.io.StringReader;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 /**
  *
  * @author Sławomir
  */
 public class Common {
 
    public static final String SERVER_URL = "Fill with server URL";
    public static final String USERNAME = "Fill with username";
    public static final String PASSWORD = "Fill with user password"; 
     
     public static final String ANDROID_FILE_MIME_TYPE = "application/vnd.android.package-archive";
     public static final String ZIP_FILE_MIME_TYPE = "application/zip";
     
     public static APIClient createApiClient() {
         return new DefaultAPIClient(SERVER_URL, USERNAME, PASSWORD);
     }
     
 }
