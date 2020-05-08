 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.client.http.impl;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.SocketException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.araqne.logdb.client.Message;
 import org.json.JSONArray;
 import org.json.JSONTokener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class TrapReceiver extends Thread {
 	private final Logger logger = LoggerFactory.getLogger(TrapReceiver.class);
 	private boolean doStop;
 	private String host;
 	private int port;
 	private String cookie;
 	private CopyOnWriteArraySet<TrapListener> listeners;
 	private HttpURLConnection con;
 
 	public TrapReceiver(String host, String cookie) {
 		this(host, 80, cookie);
 	}
 
 	public TrapReceiver(String host, int port, String cookie) {
 		this.host = host;
 		this.port = port;
 		this.cookie = cookie;
 		this.listeners = new CopyOnWriteArraySet<TrapListener>();
 	}
 
 	@Override
 	public void run() {
 		try {
 			logger.trace("logdb client: trap thread started");
 			while (!doStop) {
 				receiveTrap();
 			}
 		} catch (SocketException e) {
			if (e.getMessage().equalsIgnoreCase("socket closed"))
 				return;
 		} catch (Throwable t) {
 			logger.error("logdb client: trap receiver error", t);
 		} finally {
 			logger.trace("logdb client: trap thread stopped");
 		}
 	}
 
 	private void receiveTrap() throws SocketException {
 		InputStream is = null;
 		try {
 			con = (HttpURLConnection) new URL("http://" + host + ":" + port + "/msgbus/trap").openConnection();
 			con.setRequestProperty("Content-Type", "text/json");
 			con.setRequestProperty("Cookie", cookie);
 			con.setConnectTimeout(5000);
 			con.setReadTimeout(5000);
 
 			is = con.getInputStream();
 
 			ByteArrayOutputStream bos = null;
 			if (con.getContentLength() > 0)
 				bos = new ByteArrayOutputStream(con.getContentLength());
 			else
 				bos = new ByteArrayOutputStream();
 
 			byte[] b = new byte[8096];
 
 			while (true) {
 				int read = is.read(b);
 				if (read < 0)
 					break;
 
 				bos.write(b, 0, read);
 			}
 
 			String text = new String(bos.toByteArray(), "utf-8");
 			if (text.isEmpty())
 				return;
 
 			JSONTokener tokenizer = new JSONTokener(new StringReader(text));
 			JSONArray container = (JSONArray) tokenizer.nextValue();
 			for (int i = 0; i < container.length(); i++) {
 				JSONArray obj = container.getJSONArray(i);
 				Message msg = MessageCodec.decode(obj.toString());
 				invokeTrapCallbacks(msg);
 			}
 		} catch (SocketTimeoutException e) {
 			logger.debug("logdb client: socket timeout");
 		} catch (ConnectException e) {
 			throw e;
 		} catch (SocketException e) {
			if (e.getMessage().equalsIgnoreCase("socket closed"))
 				throw e;
 			logger.error("logdb client: cannot fetch trap", e);
 		} catch (Throwable t) {
 			logger.error("logdb client: cannot fetch trap", t);
 		} finally {
 			if (is != null) {
 				try {
 					is.close();
 				} catch (IOException e) {
 				}
 			}
 
 			if (con != null) {
 				con.disconnect();
 				con = null;
 			}
 		}
 
 	}
 
 	private void invokeTrapCallbacks(Message msg) {
 		for (TrapListener listener : listeners) {
 			try {
 				listener.onTrap(msg);
 			} catch (Throwable t) {
 				logger.error("logdb client: listener should not throw any exception", t);
 			}
 		}
 	}
 
 	public void addListener(TrapListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(TrapListener listener) {
 		listeners.remove(listener);
 	}
 
 	public void close() {
 		try {
 			doStop = true;
 			if (con != null)
 				con.disconnect();
 			interrupt();
 			join();
 		} catch (InterruptedException e) {
 		}
 	}
 }
