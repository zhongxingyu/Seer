 /*
  *
  *  * Copyright - TrianaCloud
  *  * Copyright (C) 2012. Kieran Evans. All Rights Reserved.
  *  *
  *  * This program is free software; you can redistribute it and/or
  *  * modify it under the terms of the GNU General Public License
  *  * as published by the Free Software Foundation; either version 2
  *  * of the License, or (at your option) any later version.
  *  *
  *  * This program is distributed in the hope that it will be useful,
  *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  * GNU General Public License for more details.
  *  *
  *  * You should have received a copy of the GNU General Public License
  *  * along with this program; if not, write to the Free Software
  *  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  *
  */
 
 package org.trianacode.TrianaCloud.Broker;
 
 import com.rabbitmq.client.AMQP;
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.commons.io.FilenameUtils;
 import org.trianacode.TrianaCloud.Utils.Task;
 import org.trianacode.TrianaCloud.Utils.TaskOps;
 import org.trianacode.TrianaCloud.Utils.TrianaCloudServlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 /**
  * Created by IntelliJ IDEA.
  * User: keyz
  * Date: 25/02/12
  * Time: 22:12
  * To change this template use File | Settings | File Templates.
  */
 public class Broker extends TrianaCloudServlet {
 
     public static final String RABBIT_QUEUE = "rabbitmq.queue";
     public static final String EXCHANGE = "rabbitmq.exchange";
     public static Map<String, Task> taskMap;
 
     private String r_replyQueue;
     private String r_exchange;
 
     private Connection connection;
     private Channel channel;
     private ConnectionFactory factory;
 
     public void init() throws ServletException {
         try {
             factory = (ConnectionFactory) getServletContext().getAttribute("RabbitMQConnectionFactory");
             if (factory == null) {
                 throw new ServletException("No RabbitMQ factory retrieved from Servlet Context. Cannot go on.");
             }
             r_exchange = getInitParameter(EXCHANGE);
             if (r_exchange == null) {
                 throw new ServletException("No RabbitMQ exchange defined in init parameter. Cannot go on.");
             }
             r_replyQueue = (String) getServletContext().getAttribute("replyQueue");
             if (r_replyQueue == null) {
                throw new ServletException("No RabbitMQ reply queue defined in init parameter. Cannot go on.");
             }
 
             taskMap = (Map<String, Task>) getServletContext().getAttribute("taskmap");
             if (taskMap == null) {
                 throw new ServletException("Couldn't get Taskmap");
             }
 
             connection = factory.newConnection();
             channel = connection.createChannel();
             channel.exchangeDeclare(r_exchange, "topic");
         } catch (NumberFormatException e) {
             throw new ServletException("No RabbitMQ password defined in init parameter. Cannot go on.");
         } catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     public void dispatchTask(Task t) throws IOException {
         ///TODO: Read task metadata to determine which queue to send to (e.g. #.triana)
         String corrId = UUID.randomUUID().toString();
 
         AMQP.BasicProperties props = new AMQP.BasicProperties
                 .Builder()
                 .correlationId(corrId)
                 .replyTo(r_replyQueue)
                 .build();
 
        String routingKey = t.routingKey;
 
         taskMap.put(corrId, t);
         channel.basicPublish(r_exchange, routingKey, props, TaskOps.encodeTask(t));
         System.out.println("Sent job " + corrId + " with payload " + t.getName());
     }
 
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         String pathInfo = isolatePath(request);
         String content = "";
         if (!pathInfo.equalsIgnoreCase("")) {
             write404Error(response, "Unknonw endpoint");
             return;
         }
 
         try {
             byte[] data = null;
             String r_key = "";
             String fname = "";
             int numTasks = 0;
             StringBuilder s = new StringBuilder();
 
             try {
                 List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                 for (FileItem item : items) {
                     if (item.isFormField()) {
                         // Process regular form field (input type="text|radio|checkbox|etc", select, etc).
                         String fieldname = item.getFieldName();
                         String fieldvalue = item.getString();
                         if (fieldname.equalsIgnoreCase("task")) {
                             s.append(fieldvalue);
                         }
                         if (fieldname.equalsIgnoreCase("routingkey")) {
                             r_key = fieldvalue;
                         }
                         if (fieldname.equalsIgnoreCase("numtasks")) {
                             numTasks = Integer.parseInt(fieldvalue);
                         }
                     } else {
                         // Process form file field (input type="file").
                         String fieldname = item.getFieldName();
                         String filename = FilenameUtils.getName(item.getName());
                         // ... (do your job here)
 
                         fname = filename;
 
                         InputStream is = item.getInputStream();
 
                         long length = item.getSize();
 
                         if (length > Integer.MAX_VALUE) {
                             // File is too large
                             throw new Exception("File too large");
                         }
 
                         // Create the byte array to hold the data
                         byte[] bytes = new byte[(int) length];
 
                         // Read in the bytes
                         int offset = 0;
                         int numRead = 0;
                         while (offset < bytes.length
                                 && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                             offset += numRead;
                         }
 
                         // Ensure all the bytes have been read in
                         if (offset < bytes.length) {
                             throw new IOException("Could not completely read file " + length);
                         }
                         data = bytes;
                     }
                 }
             } catch (FileUploadException e) {
                 throw new ServletException("Cannot parse multipart request.", e);
             }
 
             log.debug(content);
             for (int i = 0; i < numTasks; i++) {
                 Task t = new Task();
                 t.setData(data);
                 t.setName(fname);
                 t.setOrigin("Broker");
                 t.setDispatchTime(System.currentTimeMillis());
                 t.setRoutingKey(r_key);
                 dispatchTask(t);
             }
             //Task t = new Task("call", content.getBytes(),"dart.triana");
             //dispatchTask(t);
             String ret = "Ok"; ///TODO:do some stuff here
             writeResponse(response, 200, "Success", ret);
 
         } catch (Exception e) {
             e.printStackTrace();
             log.error(e);
             StringBuffer stack = new StringBuffer("Error: " + e.getMessage() + "<br/>");
             StackTraceElement[] trace = e.getStackTrace();
             for (StackTraceElement element : trace) {
                 stack.append(element.toString()).append("<br/>");
             }
             writeError(response, 500, stack.toString());
         } catch (Throwable t) {
             writeThrowable(response, t);
         }
     }
 }
