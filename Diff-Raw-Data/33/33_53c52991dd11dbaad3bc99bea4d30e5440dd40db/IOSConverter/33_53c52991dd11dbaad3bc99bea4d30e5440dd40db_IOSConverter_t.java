 package org.jabylon.ios;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ContentHandler.ByteOrderMark;
 import org.jabylon.properties.Property;
 import org.jabylon.properties.PropertyFile;
 import org.jabylon.properties.types.impl.PropertiesHelper;
 import org.jabylon.properties.util.NativeToAsciiConverter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class IOSConverter extends PropertiesHelper{
     
     private boolean isInComment = false;
 
     private static final Pattern SPLIT_PATTERN = Pattern.compile("\"(.*?)\"\\s*=(?:\\s*\"(.*?)\");?",Pattern.DOTALL);
     
     final Logger logger = LoggerFactory.getLogger(IOSConverter.class);
 
 
     public IOSConverter(boolean unicodeEscaping) {
         this(unicodeEscaping,null);
     }
 
     /**
      *
      * @param unicodeEscaping
      * @param uri can be used to log the path of invalid files if supplied
      */
     public IOSConverter(boolean unicodeEscaping, URI uri) {
     	super(unicodeEscaping,uri);
     }
 
     @Override
     protected String[] split(String propertyValue) {
     	Matcher matcher = SPLIT_PATTERN.matcher(propertyValue);
     	String[] result = new String[2];
     	if(matcher.matches())
     	{
     		result[0] = matcher.group(1);
     		if(matcher.groupCount()>1)
     			result[1] = matcher.group(2);    		
     	}
     	return result;
     	
     }
 
     @Override
     protected boolean isComment(String line) {
     	if(line.startsWith("/*"))
     	{
     		isInComment = true;
     	}
     	if(line.endsWith("*/")) {
     		if(isInComment) {
     			isInComment = false;
     			return true;    			
     		}
     	}
     	return isInComment;
     }
    
    @Override
    protected String parseComment(String line) {
    	if(line.startsWith("/*"))
    		line = line.substring(2);
    	if(line.endsWith("*/"))
    		line = line.substring(0,line.length()-2);
    	return line.trim();
    }
 
     @Override
 	public void writeProperty(Writer writer, Property property) throws IOException
     {
 		writeCommentAndAnnotations(writer, property);
         String key = property.getKey();
         //
         key = quoteCharacters(key);
         writer.write('"');
         if(isUnicodeEscaping())
             writer.write(NativeToAsciiConverter.convertUnicodeToEncoded(key,true));
         else
             writer.write(key);
         writer.write('"');
         writer.write(" = ");
         String value = property.getValue();
         if(value!=null)
         {
         	value = quoteCharacters(value);
             if(isUnicodeEscaping())
                 value = NativeToAsciiConverter.convertUnicodeToEncoded(value, true);
             writer.write('"');
             writer.write(value);
             writer.write('"');
         }
         writer.write(";\n");
 
     }
 
     
     /**
      * we need to replace \r, \n and ". See https://developer.apple.com/library/mac/documentation/cocoa/conceptual/loadingresources/Strings/Strings.html
      * @param value
      * @return
      */
     private String quoteCharacters(String value){
     	if(value==null)
     		return null;
     	value = value.replace("\n", "\\n");
     	value = value.replace("\r", "\\r");
     	value = value.replace("\"", "\\\"");
     	return value;
     }
     
     @Override
     protected void writeComment(Writer writer, String comment) throws IOException {
         writer.write("/*");
         writer.write(comment);
         writer.write("*/\n");
     }
 
 
     @Override
     public int write(OutputStream out, PropertyFile file, String encoding) throws IOException {
         int savedProperties = 0;
         BufferedWriter writer;
         if("UTF-8".equals(encoding)) {
         	//see https://github.com/jutzig/jabylon/issues/5
             //write BOMs in unicode mode
             out.write(ByteOrderMark.UTF_8.bytes());
         }	
         
         writer = new BufferedWriter(new OutputStreamWriter(out, encoding));
         try {
                         
             writeLicenseHeader(writer, file.getLicenseHeader());
             Iterator<Property> it = file.getProperties().iterator();
             while (it.hasNext()) {
                 Property property = (Property) it.next();
                 //eliminate all empty property entries
                 if(isFilled(property))
                 {
                 	writeProperty(writer, property);
                     savedProperties++;
                 }
 
             }
 
         }
         finally{
             writer.close();
         }
     	return savedProperties;
     }
 
 }
