 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.wms;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.font.FontRenderContext;
 import java.awt.font.LineBreakMeasurer;
 import java.awt.font.TextLayout;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.text.AttributedString;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 
 import javax.imageio.ImageIO;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.geoserver.ows.LegacyServiceExceptionHandler;
 import org.geoserver.ows.OWS;
 import org.geoserver.ows.Request;
 import org.geoserver.ows.util.ResponseUtils;
 import org.geoserver.platform.Service;
 import org.geoserver.platform.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 
 public class WmsExceptionHandler extends LegacyServiceExceptionHandler {
 
     static final Set<String> FORMATS = new HashSet<String>(Arrays.asList("image/png", "image/png8",
             "image/gif", "image/jpeg"));
 
     static final Map<String, String> IMAGEIO_FORMATS = new HashMap<String, String>() {
         {
             put("image/png", "png");
             put("image/png8", "png");
             put("image/gif", "gif");
             put("image/jpeg", "jpeg");
         }
     };
 
     public WmsExceptionHandler(Service service, OWS ows, GeoServer geoServer) {
         super(service, ows, geoServer);
     }
 
     @Override
     public void handleServiceException(ServiceException exception, Request request) {
         // first of all check what kind of exception handling we must perform
         String exceptions;
         int width;
         int height;
         String format;
         try {
             exceptions = (String) request.getKvp().get("EXCEPTIONS");
             width = (Integer) request.getKvp().get("WIDTH");
             height = (Integer) request.getKvp().get("HEIGHT");
             format = (String) request.getKvp().get("FORMAT");
         } catch (NullPointerException e) {
             // width and height might be missing
             super.handleServiceException(exception, request);
             return;
         }
         if (exceptions == null || !"application/vnd.ogc.se_inimage".equals(exceptions)
                || width <= 0 || height <= 0 || !FORMATS.contains(format))
             super.handleServiceException(exception, request);
 
         // ok, it's image, then we have to build a text representing the
         // exception and lay it out in the image
         BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g = (Graphics2D) img.getGraphics();
         g.setColor(Color.WHITE);
         g.fillRect(0, 0, img.getWidth(), img.getHeight());
         g.setColor(Color.BLACK);
 
         // draw the exception text (give it a good offset so that it can be read
         // properly in the OL preview as well)
         paintLines(g, buildExceptionText(exception), width - 2, 35, 5);
 
         // encode
         g.dispose();
         try {
             final HttpServletResponse response = request.getHttpResponse();
             response.setContentType(format);
             final ServletOutputStream os = response.getOutputStream();
             ImageIO.write(img, IMAGEIO_FORMATS.get(format), os);
             os.flush();
         } catch (IOException e) {
             LOGGER.log(Level.INFO, "Problem writing exception information back to calling client:",
                     e);
         }
     }
 
     String buildExceptionText(ServiceException exception) {
         StringBuffer sb = new StringBuffer();
         // exception code and locator
         if ((exception.getCode() != null) && !exception.getCode().equals("")) {
             sb.append("code=\"" + exception.getCode() + "\"");
         }
 
         // exception locator
         if ((exception.getLocator() != null) && !exception.getLocator().equals("")) {
             sb.append(" locator=\"" + exception.getLocator() + "\"");
         }
 
         // message
         if ((exception.getMessage() != null)) {
             dumpExceptionMessages(exception, sb, false);
 
             if (geoServer.isVerboseExceptions()) {
                 ByteArrayOutputStream stackTrace = new ByteArrayOutputStream();
                 exception.printStackTrace(new PrintStream(stackTrace));
 
                 sb.append("\nDetails:\n");
                 sb.append(new String(stackTrace.toByteArray()));
             }
         }
 
         return sb.toString();
     }
 
     /**
      * Paint the provided text onto the graphics wrapping words at the specified
      * lineWidth
      * 
      * @param lineWidth
      * @param g
      * @param text
      */
     void paintLines(Graphics2D g, String text, int lineWidth, int startX, int startY) {
         // split the text into lines, LineBreakMeasurer only lays out the single
         // line
         String[] lines = text.split("\\n");
 
         // setup the cursor
         Point cursor = new Point(startX, startY);
         
         // grab the line height to skip empty lines
         final FontMetrics metrics = g.getFontMetrics();
         int lineHeight = metrics.getAscent() + metrics.getDescent() + metrics.getLeading();
         
         FontRenderContext frc = g.getFontRenderContext();
 
         // scan over the
         for (int i = 0; i < lines.length; i++) {
             final String line = lines[i];
 
             if ("".equals(line)) {
                 cursor.y += lineHeight;
             } else {
                 AttributedString styledText = new AttributedString(line);
                 LineBreakMeasurer measurer = new LineBreakMeasurer(styledText.getIterator(), frc);
 
                 while (measurer.getPosition() < line.length()) {
 
                     TextLayout layout = measurer.nextLayout(lineWidth);
 
                     cursor.y += (layout.getAscent());
                     float dx = layout.isLeftToRight() ? 0 : (lineWidth - layout.getAdvance());
 
                     layout.draw(g, cursor.x + dx, cursor.y);
                     cursor.y += layout.getDescent() + layout.getLeading();
                 }
             }
         }
 
     }
 
 }
