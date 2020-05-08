 package com.amee.base.validation;
 
 import com.amee.base.utils.UidGen;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.validation.Errors;
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.regex.Pattern;
 
 /**
  * A bean to specify validation rules and perform validation for a single field submitted via a web form (or other).
  * Allows for various types of fields and rules to be checked against. See below for the available checks.
  */
 public class ValidationSpecification implements Serializable {
 
     public final static int STOP = 0;
     public final static int CONTINUE = 1;
 
     // The name of the value, such as 'password'.
     private String name = null;
 
     // Can the value be empty?
     private boolean allowEmpty = false;
 
     // The minimum size of the value, or -1 to ignore.
     private int minSize = -1;
 
     // The maximum size of the value, or -1 to ignore.
     private int maxSize = -1;
 
     // The precise size of the value, or -1 to ignore.
     private int size = -1;
 
     // A RegEx Pattern to validate with, or null to ignore.
     private Pattern format = null;
 
     // Is the value a URL?
     private boolean url = false;
 
     // Is the value a UID?
     private boolean uid = false;
 
     // Is the value a comma separated list of UIDs?
     private boolean uidList = false;
 
     // Is the value a number (integer)?
     private boolean integerNumber = false;
 
     // Is the value a number (double)?
     private boolean doubleNumber = false;
 
     // Can a number be negative?
     private boolean numberNegative = true;
 
     public ValidationSpecification() {
         super();
     }
 
     /**
      * Performs validation of the supplied value against the specification held in the
      * ValidationSpecification instance. Will update the supplied Errors instance with the
      * validation results.
      *
      * @param value to validate
      * @param e     the Errors instance for storing validation results
      * @return STOP or CONTINUE, indicating whether the caller should continue validating other values
      */
     public int validate(Object value, Errors e) {
         // Don't validate if this field already has errors.
         if (e.hasFieldErrors(name)) {
             return STOP;
         }
         // Validate Strings or Objects separately.
         if (value instanceof String) {
             return validateString((String) value, e);
         } else {
             return validateObject(value, e);
         }
     }
 
     private int validateString(String value, Errors e) {
         // Handle empty.
         if (allowEmpty && StringUtils.isBlank(value)) {
             // Allow empty.
             return CONTINUE;
         } else {
             // Don't allow empty.
             if (StringUtils.isBlank(value)) {
                 e.rejectValue(name, "empty");
                 return STOP;
             }
         }
         // Validate length.
         if (minSize != -1) {
             if (value.length() < minSize) {
                 e.rejectValue(name, "short");
                 return STOP;
             }
         }
        if (minSize != -1) {
             if (value.length() > maxSize) {
                 e.rejectValue(name, "long");
                 return STOP;
             }
         }
        if (getSize() != -1) {
            if (value.length() != getSize()) {
                 e.rejectValue(name, "length");
                 return STOP;
             }
         }
         // Validate format.
         if (format != null) {
             if (!format.matcher(value).matches()) {
                 e.rejectValue(name, "format");
                 return STOP;
             }
         }
         // Validate url format.
         if (url) {
             try {
                 new URL(value);
             } catch (MalformedURLException e1) {
                 e.rejectValue(name, "format");
                 return STOP;
             }
         }
         // Validate uid format.
         if (uid) {
             if (!UidGen.INSTANCE_12.isValid(value)) {
                 e.rejectValue(name, "format");
                 return STOP;
             }
         }
         // Validate uid list format.
         if (uidList) {
             for (String uid : value.split(",")) {
                 if (!UidGen.INSTANCE_12.isValid(uid)) {
                     e.rejectValue(name, "format");
                     return STOP;
                 }
             }
         }
         // Validate integer.
         if (integerNumber) {
             Integer i;
             try {
                 i = Integer.valueOf(value);
             } catch (NumberFormatException e1) {
                 e.rejectValue(name, "number");
                 return STOP;
             }
             if (!numberNegative && i < 0) {
                 e.rejectValue(name, "negative");
                 return STOP;
             }
         }
         // Validate double.
         if (doubleNumber) {
             Double d;
             try {
                 d = Double.valueOf(value);
             } catch (NumberFormatException e1) {
                 e.rejectValue(name, "number");
                 return STOP;
             }
             if (!numberNegative && d < 0) {
                 e.rejectValue(name, "negative");
                 return STOP;
             }
         }
         // Everything passed.
         return CONTINUE;
     }
 
     private int validateObject(Object value, Errors e) {
         // Handle empty.
         if (allowEmpty && (value == null)) {
             // Allow empty.
             return CONTINUE;
         } else {
             // Don't allow empty.
             if (value == null) {
                 e.rejectValue(name, "empty");
                 return STOP;
             }
         }
         // Everything passed.
         return CONTINUE;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isAllowEmpty() {
         return allowEmpty;
     }
 
     public void setAllowEmpty(boolean allowEmpty) {
         this.allowEmpty = allowEmpty;
     }
 
     public int getMinSize() {
         return minSize;
     }
 
     public void setMinSize(int minSize) {
         this.minSize = minSize;
     }
 
     public int getMaxSize() {
         return maxSize;
     }
 
     public void setMaxSize(int maxSize) {
         this.maxSize = maxSize;
     }
 
     public int getSize() {
         return size;
     }
 
     public void setSize(int size) {
         this.size = size;
     }
 
     public String getFormat() {
         if (this.format != null) {
             return this.format.pattern();
         } else {
             return null;
         }
     }
 
     public void setFormat(String format) {
         this.format = Pattern.compile(format);
     }
 
     public boolean isUrl() {
         return url;
     }
 
     public void setUrl(boolean url) {
         this.url = url;
     }
 
     public boolean isUid() {
         return uid;
     }
 
     public void setUid(boolean uid) {
         this.uid = uid;
     }
 
     public boolean isUidList() {
         return uidList;
     }
 
     public void setUidList(boolean uidList) {
         this.uidList = uidList;
     }
 
     public boolean isIntegerNumber() {
         return integerNumber;
     }
 
     public void setIntegerNumber(boolean integerNumber) {
         this.integerNumber = integerNumber;
     }
 
     public boolean isDoubleNumber() {
         return doubleNumber;
     }
 
     public void setDoubleNumber(boolean doubleNumber) {
         this.doubleNumber = doubleNumber;
     }
 
     public boolean isNumberNegative() {
         return numberNegative;
     }
 
     public void setNumberNegative(boolean numberNegative) {
         this.numberNegative = numberNegative;
     }
 }
 
