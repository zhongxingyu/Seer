 /*
  * $Id$
  * (c) Copyright 2004 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import org.wings.externalizer.ExternalizeManager;
 import org.wings.externalizer.ResourceExternalizer;
 import org.wings.io.Device;
 import org.wings.util.ImageInfo;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * actual this is a static resource, but buffering is not neccessary, so to save resources implement it as resource
 * 
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SByteArrayIcon extends Resource implements SIcon {
 
     protected ImageInfo imageInfo;
 
     protected byte[] iconData;
 
     protected int width = -1;
     protected int height = -1;
 
     public SByteArrayIcon(byte[] pIconData, String pExtension, String pMimeType) {
         setIconData(pIconData, pExtension, pMimeType);
 
     }
 
     public SByteArrayIcon() {
     }
 
     protected void externalize() {
         ExternalizeManager ext = getSession().getExternalizeManager();
         ext.removeExternalizedResource(ext.getId(id));
         id = ext.externalize(this, ResourceExternalizer.SHARED_INSTANCE,
                              null, ExternalizeManager.SESSION | ExternalizeManager.FINAL);
     }
 
     protected void removeExternalizedResource() {
         if (id != null) {
             ExternalizeManager ext = getSession().getExternalizeManager();
             ext.removeExternalizedResource(ext.getId(id));
             id = null;
         }
     }
 
     public void setIconData(byte[] pIconData, String pExtension, String pMimeType) {
         if (imageInfo == null) {
             imageInfo = new ImageInfo();
             imageInfo.setCollectComments(false);
             imageInfo.setDetermineImageNumber(false);
         }
 
         iconData = pIconData;
         mimeType = pMimeType;
         extension = pExtension;
 
         if ((pExtension == null || pMimeType == null) && iconData != null) {
             ByteArrayInputStream tImageInput = new ByteArrayInputStream(iconData);
             imageInfo.setInput(tImageInput);
 
             if (imageInfo.check()) {
                 if (extension == null) {
                     extension = imageInfo.getFormatName();
                 }
                 if (mimeType == null) {
                     mimeType = imageInfo.getMimeType();
                 }
                 width = imageInfo.getWidth();
                 height = imageInfo.getHeight();
             }
 
             try {
                 // inputstream isn't needed any longer
                 imageInfo.setInput((InputStream) null);
                 tImageInput.close();
             } catch (IOException ex) {
                 // ignore it, we don't need it anymore...
             }
         }
 
         // force new externalizing
         removeExternalizedResource();
     }
 
     public void setIconData(byte[] pIconData) {
         setIconData(pIconData, null, null);
     }
 
     public byte[] getIconData() {
         return iconData;
     }
 
     public int getEffectiveIconHeight() {
         return imageInfo != null ? imageInfo.getHeight() : -1;
     }
 
     public int getEffectiveIconWidth() {
         return imageInfo != null ? imageInfo.getWidth() : -1;
     }
 
     public String getId() {
         return id;
     }
 
     public SimpleURL getURL() {
         if (id == null) {
             externalize();
         }
 
         RequestURL requestURL = (RequestURL) getSession().getProperty("request.url");
         requestURL = (RequestURL) requestURL.clone();
         requestURL.setResource(id);
         return requestURL;
     }
 
     public void write(Device d) throws IOException {
         d.write(iconData);
     }
 
     public int getIconWidth() {
         return width;
     }
 
     public int getIconHeight() {
         return height;
     }
 
     public void setIconWidth(int pWidth) {
         width = pWidth;
     }
 
     public void setIconHeight(int pHeight) {
         height = pHeight;
     }
 
     public void finalize() {
         removeExternalizedResource();
     }
 }
