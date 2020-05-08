 package net.sourceforge.eclipseccase.ui;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 
 
 public class ClearcaseImages
 {
     // base path
     public static final String ICON_PATH = "icons/full/"; //$NON-NLS-1$
 
    // images (don't forget to add to ClearcaseUI#initialize...)
     public static final String IMG_HIJACKED = "hijacked.gif"; //$NON-NLS-1$
     public static final String IMG_LINK = "link_ovr.gif"; //$NON-NLS-1$
     public static final String IMG_LINK_WARNING = "linkwarn_ovr.gif"; //$NON-NLS-1$
 	public static final String IMG_QUESTIONABLE = "question_ov.gif"; //$NON-NLS-1$
 	public static final String IMG_EDITED = "edited_ov.gif"; //$NON-NLS-1$
 	public static final String IMG_NO_REMOTEDIR = "no_remotedir_ov.gif"; //$NON-NLS-1$
     public static final String IMG_REFRESH = "refresh.gif"; //$NON-NLS-1$
     public static final String IMG_REFRESH_DISABLED = "refresh_disabled.gif"; //$NON-NLS-1$
     
     /**
      * @param string
      * @return
      */
     public static ImageDescriptor getImageDescriptor(String string)
     {
         return ClearcaseUI.getInstance().getImageRegistry().getDescriptor(string);
     }
 }
