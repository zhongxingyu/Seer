 /*
  * Created on Dec 31, 2004 at the Interface Ecology Lab.
  */
 package ecologylab.types;
 
 import java.io.File;
 
 import ecologylab.net.ParsedURL;
 
 /**
  * Type system entry for java.awt.Color. Uses a hex string as initialization.
  * 
  * @author andruid
  */
 public class ParsedURLType extends Type
 {
 /**
  * This constructor should only be called once per session, through
  * a static initializer, typically in TypeRegistry.
  * <p>
  * To get the instance of this type object for use in translations, call
  * <code>TypeRegistry.get("cm.generic.ParsedURL")</code>.
  * 
  */
 	public ParsedURLType()
 	{
		super("ecologylab.net.ParsedURL", /*TYPE_PARSED_URL, */ false);
 	}
 
 	/**
 	 * @param value is interpreted as hex-encoded RGB value, in the
 	 * same style as HTML & CSS. A # character at the start is unneccesary,
 	 * but acceptable.
 	 * 
 	 * @see ecologylab.types.Type#getInstance(java.lang.String)
 	 */
 	public Object getInstance(String value)
 	{
 	   if (value.indexOf(':') == 1)
 	   {
 		   File file	= ecologylab.generic.Files.newFile(value);
 		   return new ParsedURL(file);
 	   }
 	   return ParsedURL.getAbsolute(value, " getInstance()");
 	}
 }
