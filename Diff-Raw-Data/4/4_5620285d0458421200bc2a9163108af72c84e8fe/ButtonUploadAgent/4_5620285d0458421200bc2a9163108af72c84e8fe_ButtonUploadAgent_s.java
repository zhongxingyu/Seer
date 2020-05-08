 /* ButtonUploadAgent.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Jun 19, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.text.MessageFormat;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.jetty.util.IO;
 import org.zkoss.lang.Strings;
 import org.zkoss.zats.common.util.MultiPartOutputStream;
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.impl.ClientCtrl;
 import org.zkoss.zats.mimic.impl.EventDataManager;
 import org.zkoss.zats.mimic.impl.OperationAgentBuilder;
 import org.zkoss.zats.mimic.impl.Util;
 import org.zkoss.zats.mimic.operation.UploadAgent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.sys.DesktopCtrl;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Menuitem;
 
 /**
  * The upload agent implementation for buttons.
  * @author pao
  * @since 1.1.0
  */
 public class ButtonUploadAgent implements OperationAgentBuilder<ComponentAgent, UploadAgent> {
 	private final static Logger logger = Logger.getLogger(ButtonUploadAgent.class.getName());
 
 	public Class<UploadAgent> getOperationClass() {
 		return UploadAgent.class;
 	}
 
 	public UploadAgent getOperation(ComponentAgent target) {
 		return new UploadAgentImpl(target);
 	}
 
 	class UploadAgentImpl extends AgentDelegator<ComponentAgent> implements UploadAgent {
 
 		private MultiPartOutputStream multipartStream;
 		private HttpURLConnection conn;
 
 		public UploadAgentImpl(ComponentAgent target) {
 			super(target);
 		}
 
 		public void upload(File file, String contentType) {
 			if (file == null)
 				throw new NullPointerException("file can't be null.");
 
 			InputStream is = null;
 			try {
 				is = new BufferedInputStream(new FileInputStream(file));
 				upload(file.getName(), is, contentType);
 			} catch (IOException e) {
 				throw new AgentException(e.getMessage(), e);
 			} finally {
 				Util.close(is);
 			}
 		}
 
 		public void upload(String fileName, InputStream content, String contentType) {
 			if (fileName == null)
 				throw new NullPointerException("file name can't be null.");
 			if (content == null)
 				throw new NullPointerException("content stream can't be null.");
 
 			// fetch upload flag
 			String flag = null;
 			if (target.is(Button.class))
 				flag = target.as(Button.class).getUpload();
 			else if (target.is(Menuitem.class))
 				flag = target.as(Menuitem.class).getUpload();
 			else
 				throw new AgentException("unsupported component: " + target.getDelegatee().getClass().getName());
 			// check upload flag
 			if (flag == null || flag.length() == 0)
 				throw new AgentException("upload feature doesn't turn on.");
 			else {
 				Set<String> set = new HashSet<String>(Arrays.asList(flag.split("[\\s,]+")));
 				if (set.contains("false"))
 					throw new AgentException("upload feature doesn't turn on.");
 			}
 
 			// first time upload
 			if (multipartStream == null) {
 				try {

 					// parameters 
 					String param = "?uuid={0}&dtid={1}&sid=0&maxsize=undefined";
 					param = MessageFormat.format(param, target.getUuid(), target.getDesktop().getId());
 					// open connection
 					String boundary = MultiPartOutputStream.generateBoundary(); // boundary for multipart
 					ClientCtrl cc = (ClientCtrl) getClient();
 					conn = cc.getConnection("/zkau/upload" + param, "POST");
 					conn.addRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
 					conn.setDoInput(true);
 					conn.setDoOutput(true);
 					OutputStream os = conn.getOutputStream();
 					multipartStream = new MultiPartOutputStream(os, boundary);
 				} catch (IOException e) {
 					clean();
 					throw new AgentException(e.getMessage(), e);
 				}
 			}
 
 			try {
				// additional readers
 				String contentDisposition = "Content-Disposition: form-data; name=\"file\"; filename=\"{0}\"";
 				contentDisposition = MessageFormat.format(contentDisposition, fileName);
 				String[] headers = new String[] { contentDisposition };
 				// upload multipart data
 				multipartStream.startPart(contentType != null ? contentType : "application/octet-stream", headers); // default content type
 				int b;
 				while ((b = content.read()) >= 0)
 					multipartStream.write(b);
 			} catch (IOException e) {
 				clean();
 				throw new AgentException(e.getMessage(), e);
 			}
 		}
 
 		public void finish() {
 			if (multipartStream == null)
 				return;
 
 			// finish upload first and get the correct key
 			clean();
 			int key = ((DesktopCtrl) target.getDesktop().getDelegatee()).getNextKey() - 1;
 			String contentId = Strings.encode(new StringBuffer(12).append("z__ul_"), key).toString(); // copy from AuUploader
 			// perform AU
 			String cmd = "updateResult";
 			String desktopId = target.getDesktop().getId();
 			Event event = new Event(cmd, (Component) target.getDelegatee());
 			Map<String, Object> data = EventDataManager.getInstance().build(event);
 			data.put("wid", target.getUuid());
 			data.put("contentId", contentId);
 			data.put("sid", "0");
 			((ClientCtrl) target.getClient()).postUpdate(desktopId, cmd, target.getUuid(), data, null);
 			((ClientCtrl) target.getClient()).flush(desktopId);
 		}
 
 		private void clean() {
 			// close output
 			Util.close(multipartStream);
 			multipartStream = null;
 
 			// close input 
 			InputStream is = null;
 			try {
 				String respMsg = conn.getResponseMessage();
 				is = conn.getInputStream();
 				String resp = IO.toString(is);
 				if (logger.isLoggable(Level.FINEST)) {
 					logger.finest("response message: " + respMsg);
 					logger.finest("response content: " + resp);
 				}
 			} catch (IOException e) {
 				throw new AgentException(e.getMessage(), e);
 			} finally {
 				Util.close(is);
 				conn = null;
 			}
 		}
 	}
 }
