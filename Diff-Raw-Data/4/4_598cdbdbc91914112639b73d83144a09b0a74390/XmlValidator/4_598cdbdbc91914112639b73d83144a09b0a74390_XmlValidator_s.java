 package au.com.sensis.mobile.crf.util;
 
 import java.net.URL;
 
 /**
  * Simple interface for validating XML against a schema.
  *
  * @author Adrian.Koh2@sensis.com.au
  */
 public interface XmlValidator {
 
     /**
      * Validate the given XML using the given schema.
      *
     * @param xmlToValidate URL to the XML to validate.
     * @param schema URL to the schema to use for the validation.
      */
     void validate(URL xmlToValidateUrl, URL schemaUrl);
 }
