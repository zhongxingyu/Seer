 package hu.sztaki.ilab.longneck.weblog.parser.postprocessor;
 
 import hu.sztaki.ilab.longneck.Field;
 import hu.sztaki.ilab.longneck.Record;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 /**
  * Postprocessor that splits URL parameters to key/value pairs.
  *
  * Note: It is possible to add more than one character encoding set;
  * Parameter and Parameter2 are decoded according to the two given encodings.
  *
  * This class is thread-safe.
  *
  * @author Bendig Loránd <lbendig@ilab.sztaki.hu>
  *
  */
 public class UrlParamsPostProcessor extends AbstractCachingPostProcessor implements PostProcessor, CharcodingInterface {
 
     private static final Logger LOG = Logger.getLogger(UrlParamsPostProcessor.class);
 
     /** All url parameters result in a separate field with the given name and value, if enabled. */
     public Boolean userCreateParameterFiled = null;
     /** The default is that the filed will created. */
     public static final Boolean DEFAULT_CREATE_PARAMETER_FIELDS = true ;
 
     /** Url parameter name prefix */
     public static final String ATTR_PARAM_POSTFIX = "Parameter";
     public static final String ATTR_PARAM_POSTFIX_2 = "Parameter2";
 
     /** Base url extension */
     public static final String ATTR_URL_EXT = "Extension";
 
     /** Normalized url */
     public static final String ATTR_URL_NORMALIZED = "Full" ;
 
     /** Normalized url2 if secondary charset is given. */
     public static final String ATTR_URL_NORMALIZED2 = "Full2" ;
 
     public static final String PARAM_NAME_VALUE_DELIMITER = "=";
 
     public static final String PARAMS_DELIMITER = "&";
 
     /** Default characterset. */
     public static final String URL_DECODING_CHARSET = "UTF-8";
     /** The supposed character encoding of the logline. User can set this value in the config.xml.
         This variable override the default URL_DECODING_CHARSET. */
     String userdefined_charset = null;
     /** The second supposed character encoding of the logline in the case when mixing encoding occur.
      * User can set this value in the config.xml.*/
     String userdefined_charset2 = null;
 
     public void setCharset(String userdefined_charset) {
         this.userdefined_charset = userdefined_charset;
     }
 
     public void setCharset2(String userdefined_charset2) {
         this.userdefined_charset2 = userdefined_charset2;
     }
 
     @Override
     public String getCharset() {
         return userdefined_charset;
     }
 
     @Override
     public String getCharset2() {
         return userdefined_charset2;
     }
 
     public Boolean getUserCreateParameterFiled() {
         return userCreateParameterFiled;
     }
 
     public void setUserCreateParameterFiled(Boolean userCreateParameterFiled) {
         this.userCreateParameterFiled = userCreateParameterFiled;
     }
 
     /** Splits URL parameters to key/value pairs.
      * @see hu.sztaki.ilab.longneck.weblog.parser.postprocessor.PostProcessor#doPostProcess(
      * java.lang.String, hu.sztaki.ilab.longneck.weblog.parser.processor.LogAttribute, hu.sztaki.ilab.longneck.Record)
      */
     @Override
     public void doPostProcess(String elementName, String elementValue, List<String> relatedFields,
             Record record, boolean caching) {
 
         if (elementValue == null) {
             LOG.warn(String.format("Couldn't postprocess field %1$s . No value present.", elementName));
             return;
         }
 
         List<Field> urlFragmentFields;
         if (caching) {
             // false if the cash is not initialiyed yet.
             if (!this.isCaching())
               initCache();
             else{
                 urlFragmentFields = getCacheElement(elementValue);
                 // cache hit
                 if (urlFragmentFields != null) {
                     for (Field f : urlFragmentFields) {
                         record.add(f);
                     }
                     return;
                 }
             }
         }
 
         urlFragmentFields = new ArrayList<Field>();
 
         // separate URL: base and query parts
         String[] split = elementValue.split("\\?");
 
         //extract url extension and create a corresponding field
         createUrlExtensionField(split[0], elementName, urlFragmentFields);
 
         if (split.length >= 2) {
             //Decode percent-encoded octets of unreserved characters in query part
             // and create normalized url field
             createNormalizedUrlField(elementName, split[0], decodePercentEncodedQuery(split[1]),
                     urlFragmentFields, ATTR_URL_NORMALIZED);
             // if user defined secoundery character set use it
             // and create secoundery normalized url  field according to the given encoding.
             if (userdefined_charset2 != null) {
                 createNormalizedUrlField(elementName, split[0], decodePercentEncodedQuery2(split[1]),
                         urlFragmentFields, ATTR_URL_NORMALIZED2);
             }
             //create fields from Url parameter key/value pairs
             createUrlParamFields(elementName, split[1], urlFragmentFields);
         } else {
             createNormalizedUrlField(elementName, split[0], null,
                     urlFragmentFields, ATTR_URL_NORMALIZED);
             // if user defined secoundery character set use it
             // and create secoundery normalized url  field according to the given encoding.
             if (userdefined_charset2 != null) {
                 createNormalizedUrlField(elementName, split[0], null,
                         urlFragmentFields, ATTR_URL_NORMALIZED2);
             }
         }
 
         if (urlFragmentFields.isEmpty()) {
             return;
         }
 
         for (Field field : urlFragmentFields) {
             record.add(field);
         }
 
        if (caching) {
             putCacheElement(elementValue, urlFragmentFields);
         }
     }
 
     private void createUrlParamFields(String elementName, String query, List<Field> urlFragmentFields) {
         Map<String, List<String>> urlParams = extractUrlParameters(query);
 
         StringBuffer sb = new StringBuffer();
         StringBuffer sb2 = new StringBuffer();
 
         for (Map.Entry<String, List<String>> entry : urlParams.entrySet()) {
 
             String paramName = entry.getKey();
             String paramValue = StringUtils.join(entry.getValue().iterator(), ", ");
 
             // encoding 1:
             String paramValue1 = decodePercentEncodedQueryParameters(paramValue,
                     userdefined_charset != null? userdefined_charset : URL_DECODING_CHARSET);
             appendURLParams(paramName, paramValue1, sb);
 
             // set each URL parameter to a distinct prefixed field
             if (userCreateParameterFiled == null ? DEFAULT_CREATE_PARAMETER_FIELDS : userCreateParameterFiled) {
                 Field f = new Field((elementName + ATTR_PARAM_POSTFIX + "-" + paramName),
                         paramValue1 == null ? paramValue : paramValue1);
                 urlFragmentFields.add(f);
             }
 
             // encoding 2:
             if (userdefined_charset2 != null) {
                 String paramValue2 = decodePercentEncodedQueryParameters(paramValue, userdefined_charset2);
                 appendURLParams(paramName, paramValue2, sb2);
 
                 // set each URL parameter to a distinct prefixed field
                 if (userCreateParameterFiled == null ? DEFAULT_CREATE_PARAMETER_FIELDS : userCreateParameterFiled) {
                     Field f = new Field((elementName + ATTR_PARAM_POSTFIX_2 + "-" + paramName),
                             paramValue2 == null ? paramValue : paramValue2);
                     urlFragmentFields.add(f);
                 }
             }
         }
 
         // encoding 1:
        if (sb.length() > 0) {
            sb.deleteCharAt(0);
        }
         Field f = new Field(elementName + ATTR_PARAM_POSTFIX, sb.toString());
         urlFragmentFields.add(f);
 
         // encoding 2:
         if (userdefined_charset2 != null) {
            if (sb.length() > 0)
                sb2.deleteCharAt(0);
             f = new Field(elementName + ATTR_PARAM_POSTFIX_2, sb2.toString());
             urlFragmentFields.add(f);
         }
 
     }
 
     private void createNormalizedUrlField(String elementName, String queryBasePart, String decodedQuery,
              List<Field> urlFragmentFields, final String postfix) {
 
         //url base part is set to lowercase
         Field f = new Field((elementName + postfix), queryBasePart.toLowerCase() + (decodedQuery == null?"":"?" + decodedQuery ));
         urlFragmentFields.add(f);
     }
 
     /**
      * Extracts URL extension
      *
      * @param queryBasePart URL without query part
      * @param elementName
      * @param urlFragmentFields
      */
     private void createUrlExtensionField(String queryBasePart, String elementName,
             List<Field> urlFragmentFields) {
 
         String urlExtension = getUrlExtension(queryBasePart);
         if (urlExtension != null) {
             Field f = new Field((elementName + ATTR_URL_EXT), urlExtension);
             urlFragmentFields.add(f);
         }
     }
 
     /**
      * Decodes percent-encoded octets of unreserved characters in url query part
      * @param querySplit - URL base part and query part
      * @return
      */
     private String decodePercentEncodedQuery(String queryparameterpart) {
         String result = null;
         try {
             result = URLDecoder.decode(queryparameterpart,
                     userdefined_charset != null ? userdefined_charset : URL_DECODING_CHARSET);
 
         } catch (Exception e) {
             LOG.error("Couldn't decode and postprocess URL query part: " + queryparameterpart);
             return null;
         }
         return result.isEmpty()?null:result;
     }
 
     /**
      * Decodes percent-encoded octets of unreserved characters in url query part
      * @param querySplit - URL base part and query part
      * @return
      */
     private String decodePercentEncodedQuery2(String queryparameterpart) {
         String result = null;
         try {
             result = URLDecoder.decode(queryparameterpart,
                     userdefined_charset2);
 
         } catch (Exception e) {
             LOG.error("Couldn't decode and postprocess URL query part: " + queryparameterpart);
         }
         return result;
     }
 
     /**
      * Decodes percent-encoded octets of unreserved characters in url query part
      * @param querySplit - URL base part and query part
      * @return
      */
     private String decodePercentEncodedQueryParameters(String parameter, String charset) {
         String result = null;
         try {
             result = URLDecoder.decode(parameter, charset);
 
         } catch (Exception e) {
             LOG.error("Couldn't decode URL-parameter: " + parameter);
         }
         return result;
     }
 
     /**
      * Extracts URL parameter name/value pairs from a given URL
      *
      * @param queryPart - Query part of the URL
      * @return - Map of URL parameters name/value
      */
     private Map<String, List<String>> extractUrlParameters(String queryPart) {
         Map<String, List<String>> result = new LinkedHashMap<String, List<String>>();
         try {
             String[] query = queryPart.split("&");
             for (String param : query) {
                 String pair[] = param.split("=");
                 String key = (pair.length == 0) ? param : pair[0];  // because when "&=" occour then lead to pair.length == 0
                 List<String> values = result.get(key);
                 if (values == null) {
                     values = new ArrayList<String>();
                     result.put(key, values);
                 }
                 if (pair.length > 1) {
                     values.add(pair[1]);
                 }
             }
         } catch (Exception e) {
             LOG.error("Couldn't decode and postprocess URL parameters: " + queryPart);
         }
 
         return result;
     }
 
     /**
      * Extracts url extension from baseUrl
      *
      * @param baseUrl
      * @return
      */
     private String getUrlExtension(String baseUrl) {
         String result = null;
         int lastSlashIdx = baseUrl.lastIndexOf('/');
 
         if (lastSlashIdx < 1 || baseUrl.charAt(lastSlashIdx-1) == '/') {
             return result;
         }
 
         int lastDotIdx = StringUtils.indexOf(baseUrl, '.', lastSlashIdx);
         if (lastDotIdx == -1) {
             return result;
         }
 
         result = baseUrl.substring(lastDotIdx + 1, baseUrl.length());
         //strip off unnecessary leftovers if there are any
         if (result.contains("&")) {
             return result.split("\\&")[0];
         }
         if(result.contains("\\;")) {
             return result.split("\\;")[0]; //attached sessionids
         }
 
         return result;
 
     }
 
     private void appendURLParams(String paramName, String paramValue, StringBuffer sb) {
         sb.append(PARAMS_DELIMITER);
         sb.append(paramName);
         sb.append(PARAM_NAME_VALUE_DELIMITER);
         sb.append(paramValue);
     }
 
 }
