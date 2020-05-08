 package com.celements.photo.image;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.api.Document;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 
 @Component
 public class CropImage implements ICropImage {
   
   private static final Log LOGGER = LogFactory.getFactory().getInstance(CropImage.class);
 
   @Requirement
   Execution execution;
 
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
   
   public void crop(BufferedImage img, int x, int y, int w, int h, String type, 
       OutputStream out)  {
    BufferedImage buffCropped = img;
    w = Math.min(w, (img.getWidth()-x));
    h = Math.min(h, (img.getHeight()-y));
    if((x < img.getWidth()) && (y < img.getHeight())) {
      buffCropped = img.getSubimage(x, y, w, h);
    }
     (new GenerateThumbnail()).encodeImage(out, buffCropped, img, type);
   }
 
   public OutputStream crop(XWikiAttachment xAtt, int x, int y, int w, int h) {
     OutputStream out = new ByteArrayOutputStream();
     crop(xAtt, x, y, w, h, out);
     return out;
   }
 
   public OutputStream crop(Document doc, String filename, int x, int y, int w, int h,
       OutputStream out) {
     XWikiAttachment xAtt = getAttachment(doc, filename);
     if(xAtt != null){
       return crop((XWikiAttachment)xAtt.clone(), x, y, w, h, out);
     }
     return null;
   }
 
     public OutputStream crop(XWikiAttachment xAtt, int x, int y, int w, int h,
         OutputStream out) {
     try {
       InputStream in = xAtt.getContentInputStream(getContext());
       BufferedImage img = new GenerateThumbnail().decodeImage(in);
       in.close();
       crop(img, x, y, w, h, xAtt.getMimeType(getContext()), out);
     } catch (XWikiException e) {
       LOGGER.error("Error getting attachment content and decoding it into an " +
           "BufferedImage", e);
     } catch (IOException e) {
       LOGGER.error("Error in input our output stream.", e);
     }
     return out;
   }
   
   XWikiAttachment getAttachment(Document srcDoc, String filename) {
     XWikiAttachment xAtt = null;
     if(srcDoc != null) {
       try {
         XWikiDocument xDoc = getContext().getWiki().getDocument(
             srcDoc.getDocumentReference(),getContext());
         xAtt = xDoc.getAttachment(filename);
       } catch (XWikiException e) {
         LOGGER.error("Exception getting XWikiDocument with crop Attachment", e);
       }
     }
     return xAtt;
   }
 
   public void outputCroppedImage(Document srcDoc, String srcFilename, int x, int y,
       int w, int h) {
     XWikiAttachment xAtt = getAttachment(srcDoc, srcFilename);
     if(xAtt != null){
       try {
         getContext().getResponse().setContentType("image/png");//output is always png
         crop((XWikiAttachment)xAtt.clone(), x, y, w, h, getContext().getResponse(
             ).getOutputStream());
       } catch (IOException e) {
         LOGGER.error("Error writing cropped image to response stream.", e);
       }
     }
   }
 }
