 /* Copyright (C) <2004> Wim Taymans <wim@fluendo.com>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
  * Boston, MA 02111-1307, USA.
  */
 
 package com.fluendo.plugin;
 
 import java.awt.*;
 import java.awt.image.*;
 import com.fluendo.utils.*;
 import com.fluendo.jst.*;
 
 public class VideoSink extends Sink
 {
   private Component component;
   private boolean keepAspect;
   private boolean ignoreAspect;
   private boolean scale;
   private Frame frame;
 
   private int width, height;
   private int aspectX, aspectY;
   private Rectangle bounds;
 
   public VideoSink ()
   {
     keepAspect = true;
     scale = true;
     bounds = null;
   }
 
   protected boolean setCapsFunc (Caps caps)
   {
     String mime = caps.getMime();
     if (!mime.equals ("video/raw"))
       return false;
 
     width = caps.getFieldInt("width", -1);
     height = caps.getFieldInt("height", -1);
 
     if (width == -1 || height == -1)
       return false;
 
     aspectX = caps.getFieldInt("aspect_x", 1);
     aspectY = caps.getFieldInt("aspect_y", 1);
 
     if(!ignoreAspect) {
       Debug.log(Debug.DEBUG, this+" dimension: "+width+"x"+height+", aspect: "+aspectX+"/"+aspectY);
 
      if (aspectY > aspectX) {
         height = height * aspectY / aspectX;
       } else {
         width = width * aspectX / aspectY;
       }
       Debug.log(Debug.DEBUG, this+" scaled source: "+width+"x"+height);
     }
 
     component.setVisible(true);
 
     return true;
   }
 
   protected int preroll (Buffer buf)
   {
     return render (buf);
   }
 
   protected int render (Buffer buf)
   {
     Image image;
     int x, y, w, h;
 
     if (!buf.duplicate) {
       Debug.log( Debug.DEBUG, this.getName() + " starting buffer " + buf );
 
       if (buf.object instanceof ImageProducer) {
         image = component.createImage((ImageProducer)buf.object);
       }
       else if (buf.object instanceof Image) {
         image = (Image)buf.object;
       }
       else {
         System.out.println(this+": unknown buffer received "+buf);
         return Pad.ERROR;
       }
 
       if (!component.isVisible())
         return Pad.NOT_NEGOTIATED;
 
       Graphics graphics = component.getGraphics();
 
       if (keepAspect) {
         double src_ratio, dst_ratio;
 
         if (bounds == null) {
   	  bounds = new Rectangle(component.getSize());
         }
         src_ratio = (double) width / height;
         dst_ratio = (double) bounds.width / bounds.height;
 
         if (src_ratio > dst_ratio) {
           w = bounds.width;
           h = (int) (bounds.width / src_ratio);
           x = bounds.x;
           y = bounds.y + (bounds.height - h) / 2;
         } else if (src_ratio < dst_ratio) {
           w = (int) (bounds.height * src_ratio);
           h = bounds.height;
           x = bounds.x + (bounds.width - w) / 2;
           y = bounds.y;
         } else {
           x = bounds.x;
           y = bounds.y;
           w = bounds.width;
           h = bounds.height;
         }
       } else if (!scale) {
         w = Math.min (width, bounds.width);
         h = Math.min (height, bounds.height);
         x = bounds.x + (bounds.width - w) / 2;
         y = bounds.y + (bounds.height - h) / 2;
       } else {
         /* draw in available area */
         w = bounds.width;
         h = bounds.height;
         x = 0;
         y = 0;
       }
       graphics.drawImage (image, x, y, w, h, null);
       Debug.log( Debug.DEBUG, this.getName() + " done with buffer " + buf );
     }
     return Pad.OK;
   };
 
   public String getFactoryName ()
   {
     return "videosink";
   }
 
   /*
    * component:    A java.awt.Component where the video frames will be sent
    * keep-aspect:  String, if "true", the aspect ratio of the input video will be maintained
    * scale:        String, if "true", and keep-aspect is not true, the video 
    *               will be scaled to fit the bounding rectangle
    * 
    * bounds:       A java.awt.Rectangle giving the output bounding rectangle. 
    *               This must always be set after component, because setting 
    *               component resets the bounding rectangle to the full extent
    *               of the component.
    */
   public boolean setProperty (String name, java.lang.Object value) {
     if (name.equals("component")) {
       component = (Component) value;
     }
     else if (name.equals("keep-aspect")) {
       keepAspect = String.valueOf(value).equals("true");
     } else if(name.equals("ignore-aspect")) {
       ignoreAspect = value.toString().equals("true");
     } else if (name.equals("scale")) {
       scale = String.valueOf(value).equals("true");
     } else if (name.equals("bounds")) {
       bounds = (Rectangle) value;
       Debug.info("Video bounding rectangle: x=" + 
 	bounds.x + ", y=" +
 	bounds.y + ", w=" +
 	bounds.width + ", h=" +
 	bounds.height );
     }
     else {
       return super.setProperty(name, value);
     }
 
     return true;
   }
 
   public java.lang.Object getProperty (String name) {
     if (name.equals("component")) {
       return component;
     }
     else if (name.equals("keep-aspect")) {
       return (keepAspect ? "true": "false");
     } else if (name.equals("bounds")) {
       return bounds;
     } else {
       return super.getProperty(name);
     }
   }
 
   protected int changeState (int transition) {
     if (currentState == STOP && pendingState == PAUSE && component == null) {
       frame = new Frame();
       component = (Component) frame;
     }
     return super.changeState(transition);
   }
 }
