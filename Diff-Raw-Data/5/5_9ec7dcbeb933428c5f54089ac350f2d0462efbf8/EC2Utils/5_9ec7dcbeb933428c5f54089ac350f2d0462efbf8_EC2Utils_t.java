 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2008 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.ec2;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * This class provides helper methods to interact with the Amazon EC2 service.
  *
  * @author D. Kavanagh
  * @author developer@dotech.com
  */
 public class EC2Utils {
     private static Log logger = LogFactory.getLog(EC2Utils.class);
 
 	/**
 	 * This method makes a best effort to fetch all instance metadata.
 	 *
 	 * @return map of metadata
 	 */
 	public static Map<String, String> getInstanceMetadata() {
 		HashMap<String, String> result = new HashMap<String, String>();
 		int retries = 0;
 		while (true) {
 			try {
 				URL url = new URL("http://169.254.169.254/latest/meta-data/");
 				BufferedReader rdr = new BufferedReader(new InputStreamReader(url.openStream()));
 				String line = rdr.readLine();
 				while (line != null) {
 					try {
 						String val = getInstanceMetadata(line);
 						result.put(line, val);
 					} catch (IOException ex) {
 						logger.error("Problem fetching piece of instance metadata!", ex);
 					}
 					line = rdr.readLine();
 				}
 				return result;
 			} catch (IOException ex) {
 				if (retries == 5) {
 					logger.debug("Problem getting instance data, retries exhausted...");
 					return result;
 				}
 				else {
 					logger.debug("Problem getting instance data, retrying...");
 					try { Thread.sleep((int)Math.pow(2.0, retries)*1000); } catch (InterruptedException e) {}
					retries++;
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method makes a best effort to fetch a piece of instance metadata.
 	 *
 	 * @param key the name of the metadata to fetch
 	 * @return value of the metadata item
 	 */
 	public static String getInstanceMetadata(String key) throws IOException {
 		int retries = 0;
 		String value = null;
 		while (true) {
 			try {
 				URL url = new URL("http://169.254.169.254/latest/meta-data/"+key);
 				value = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
 				return value;
 			} catch (IOException ex) {
 				if (retries == 5) {
 					logger.debug("Problem getting instance data, retries exhausted...");
 					logger.debug("value = "+value);
 					return null;
 				}
 				else {
 					logger.debug("Problem getting instance data, retrying...");
 					try { Thread.sleep((int)Math.pow(2.0, retries)*1000); } catch (InterruptedException e) {}
					retries++;
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method makes a best effort to fetch a piece of instance metadata.
 	 *
 	 * @param key the name of the metadata to fetch
 	 * @return value of the metadata item
 	 */
 	public static String getInstanceUserdata() throws IOException {
 		int retries = 0;
 		while (true) {
 			try {
 				URL url = new URL("http://169.254.169.254/latest/user-data/");
 				InputStreamReader rdr = new InputStreamReader(url.openStream());
 				StringWriter wtr = new StringWriter();
 				char [] buf = new char[1024];
 				int bytes;
 				while ((bytes = rdr.read(buf)) > -1) {
 					if (bytes > 0) {
 						wtr.write(buf, 0, bytes);
 					}
 				}
 				rdr.close();
 				return wtr.toString();
 			} catch (IOException ex) {
 				if (retries == 5) {
 					logger.debug("Problem getting user data, retries exhausted...");
 					return null;
 				}
 				else {
 					logger.debug("Problem getting user data, retrying...");
 					try { Thread.sleep((int)Math.pow(2.0, retries)*1000); } catch (InterruptedException e) {}
					retries++;
 				}
 			}
 		}
 	}
 
 	/**
 	 * This method encodes key/value pairs into a user-data string
 	 *
 	 */
 	public static String encodeUserParams(Map<String, String> params) {
 		StringBuilder ret = new StringBuilder();
 		for (String key : params.keySet()) {
 			ret.append(key);
 			ret.append("=");
 			ret.append(params.get(key));
 			ret.append(";");
 		}
 		return ret.toString();
 	}
 
 	/**
 	 * This method decodes key/value pairs from a user-data string
 	 *
 	 */
 	public static Map<String, String> decodeUserParams(String params) {
 		HashMap<String, String> result = new HashMap<String, String>();
 		String param = params.substring(0, params.indexOf(';'));
 		while (param != null) {
 			int idx = param.indexOf('=');
 			String key = param.substring(0, idx);
 			String value = param.substring(idx+1);
 			result.put(key, value);
 			if (param.length() == params.length()-1) {
 				param = null; // done
 			}
 			else {
 				params = params.substring(params.indexOf(';')+1);
 				param = params.substring(0, params.indexOf(';'));
 			}
 		}
 		return result;
 	}
 }
