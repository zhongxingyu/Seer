 /*
  *  Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  *  SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 package org.netbeans.modules.javafx.fxd.composer.model;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import org.netbeans.modules.javafx.fxd.dataloader.fxz.FXZDataObject;
 
 import com.sun.javafx.tools.fxd.loader.Profile;
 import com.sun.javafx.tools.fxd.container.FXDContainer;
 import org.netbeans.modules.javafx.fxd.composer.misc.FXDComposerUtils;
 
 /**
  *
  * @author Pavel Benes
  */
 public class FXDComposerModel {
     //private transient final FXDDataObject         m_dObj;    
     private transient FXZArchive  m_fxzArchive;
     private transient Exception   m_fxzArchiveException = null;
     
     /** persistent properties */
     private String        m_selectedEntry  = null;
     private boolean       m_isHighlightOn  = true;
     private boolean       m_showTooltip    = true;
     private float         m_zoomRatio      = 1.0f;
     private Profile m_previewProfile = Profile.desktop;
             
     public FXDComposerModel( FXZDataObject dObj, String cachedEntry) throws FileNotFoundException, IOException {
         assert dObj != null;
         m_selectedEntry = cachedEntry != null ? cachedEntry : FXDContainer.MAIN_CONTENT;
        //dObj.updateEditorCookie();
         try {
             m_fxzArchive = new FXZArchive(dObj);
         } catch( Exception e) {
             System.err.println(e.getLocalizedMessage());
             e.printStackTrace();
             m_fxzArchive = null;
             m_fxzArchiveException = e;
         }
     }
                 
     public FXZArchive getFXDContainer() {
         return m_fxzArchive;
     }
          
     public Exception getFXDContainerLoadError() {
         return m_fxzArchiveException;
     }
     
     public boolean getIsHighlightOn() {
         return m_isHighlightOn;
     }
 
     boolean setIsHighlightOn(boolean isHighlightOn) {
         if ( isHighlightOn != m_isHighlightOn) {
             m_isHighlightOn = isHighlightOn;
             return true;
         } else {
             return false;
         }
     }
 
     public boolean getIsTooltipOn() {
         return m_showTooltip;
     }
 
     boolean setIsTooltipOn(boolean showTooltip) {
         if ( m_showTooltip != showTooltip) {
             m_showTooltip = showTooltip;
             return true;
         } else {
             return false;
         }
     }
     
     public float getZoomRatio() {
         return m_zoomRatio;
     }
     
     boolean setZoomRatio(float zoomRatio) {
         if ( Math.abs(zoomRatio - m_zoomRatio) > 0.001) {
             m_zoomRatio = zoomRatio;
             return true;
         } else {
             return false;
         }
     }
 
     public Profile getPreviewProfile() {
         return m_previewProfile;
     }
 
     boolean setPreviewProfile(Profile profile) {
         if ( profile != m_previewProfile) {
             m_previewProfile = profile;
             return true;
         } else {
             return false;
         }
     }
 
     public String getSelectedEntry() {
         return m_selectedEntry;
     }
 
     boolean setSelectedEntry( String selectedEntry) {
         if ( !FXDComposerUtils.safeEquals( selectedEntry, m_selectedEntry)) {
             m_selectedEntry = selectedEntry;
             return true;
         } else {
             return false;
         }
     }
 }
