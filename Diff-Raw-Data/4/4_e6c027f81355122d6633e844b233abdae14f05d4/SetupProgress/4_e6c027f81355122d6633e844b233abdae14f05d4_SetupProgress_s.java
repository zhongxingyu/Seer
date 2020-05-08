 
 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: SetupProgress.java,v 1.4 2008-04-01 15:49:22 rajeevangal Exp $
  *
  * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.setup;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.URLEncoder;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 public class SetupProgress
 {
     static private Writer writer = null;
     static private String bundleName = "amConfigurator";
 
     static private OutputStream out = null;
     static private String encoding = System.getProperty("file.encoding");
 
     /**
       * @return writer associated with reporting progress to enduser.
       */
     public static Writer getWriter()
     {
         return writer;
     }
 
     /**
       * @return OutputStream associated with reporting progress to enduser.
       */
     public static OutputStream getOutputStream()
     {
         return out;
     }
 
     /**
       * Sets setup writer.
       * @param w Setup writer to be used.
       */
     public static void setWriter(Writer w)
     {
         writer = w;
         if (writer == null) {
             out = null;
             return;
         }
         out = new OutputStream() {
             public void write(int b) throws IOException {
                 writer.write("<script>addProgressText('");
                 writer.write(URLEncoder.encode(String.valueOf((char) b), encoding));
                 writer.write("<br>');</script>");
                 writer.flush();
             }
             public void write(byte[] b) throws IOException {
                 writer.write("<script>addProgressText('");
                 writer.write(URLEncoder.encode(new String(b, encoding), encoding));
                 writer.write("<br>');</script>");
                 writer.flush();
             }
             public void write(byte[] b, int off, int len) throws IOException {
                 writer.write("<script>addProgressText('");
                 writer.write(URLEncoder.encode(new String(b, off, len, encoding), encoding));
                 writer.write("<br>');</script>");
                 writer.flush();
             }
             public void flush() throws IOException {
                 writer.flush();
             }
         };
     }
     /**
       * Sets setup OututStream.
       * @param ostr OutputStream  to be used.
       */
     public static void setOutputStream(OutputStream  ostr)
     {
         out = ostr;
     }
 
     /**
       * Report beginning of an operation.
       * @param  str i18n key to be printed
       * @param param Object to be printed.
       */
     public static void reportStart(String str, Object param)
     {
         
         ResourceBundle bundle = ResourceBundle.getBundle(bundleName, 
                                                    Locale.getDefault());
         String istr = bundle.getString (str);
         
         if (istr == null)
             istr = str;
         if (param != null)
             istr +=param;
         
        System.out.println(istr);
         try {
             if (writer != null) {
                 
                 writer.write("<script>addProgressText('"+istr+"');</script>");
                 writer.flush();
             }
         } catch (Exception ex) {}
     }
 
     /**
       * Report end of an operation.
       * @param  str i18n key to be printed
       * @param param Object to be printed.
       */
     public static void reportEnd(String str, Object param)
     {
         
         ResourceBundle bundle = ResourceBundle.getBundle(bundleName, 
                                                    Locale.getDefault());
         String istr = bundle.getString (str);
                 
         if (istr == null)
             istr = str;
         if (param != null)
             istr +=param;
         
         System.out.println(istr);
         try {
             if (writer != null) {
                 
                 writer.write("<script>addProgressText('"+istr+"<br>');</script>");
                 writer.flush();
             }
         } catch (Exception ex) {}
     }
 }
