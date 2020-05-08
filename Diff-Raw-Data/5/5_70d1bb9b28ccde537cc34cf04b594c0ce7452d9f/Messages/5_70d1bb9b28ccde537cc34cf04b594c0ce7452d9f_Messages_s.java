 package net.sf.anathema.character.attributes;
 
 import org.eclipse.osgi.util.NLS;
 
 public class Messages extends NLS {
   private static final String BUNDLE_NAME = "net.sf.anathema.character.attributes.messages"; //$NON-NLS-1$
  public static final String AttributeMessages_I18nFailed = null;
   static {
     NLS.initializeMessages(BUNDLE_NAME, Messages.class);
   }
 
   private Messages() {
     // nothing to do
   }
 
  public static String Attributes_NotFound_Message;
 }
