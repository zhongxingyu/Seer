 /* Copyright (c) 2010 Park "segfault" Joon-Kyu
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package org.planetmono.dcuploader;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class SignOnGallog implements SignOnBase {
 	private static final String SIGNON_URL = "http://dcid.dcinside.com/join/member_check.php";
 	private static final String SIGNOFF_URL = "http://dcid.dcinside.com/join/logout.php";
	private static final String SIGNON_BASE_URL = "http://dcid.dcinside.com";
	private static final String SIGNON_PAGE_URL = "http://dcid.dcinside.com/join/login.php";
 	
 	public Runnable getMethodSignOn(final Application app, final Bundle b, final Handler resultHandler) {
 		return new Runnable() {
 			public void run() {
 				String id, password;
 				
 				id = b.getString("id");
 				password = b.getString("password");
 				
 				Message m = resultHandler.obtainMessage();
 				Bundle bm = m.getData();
 				
 				Log.d(Application.TAG, "logging in...");
 				
 				HttpPost post = new HttpPost(SIGNON_URL);
 				List<NameValuePair> vlist = new ArrayList<NameValuePair>();
 				vlist.add(new BasicNameValuePair("user_id", id));
 				vlist.add(new BasicNameValuePair("password", password));
 				vlist.add(new BasicNameValuePair("x", "0"));
 				vlist.add(new BasicNameValuePair("y", "0"));
 				vlist.add(new BasicNameValuePair("s_url", "about:blank"));
 				try {
 					post.setEntity(new UrlEncodedFormEntity(vlist));
 				} catch (UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 				
				post.setHeader("Origin", SIGNON_BASE_URL);
				post.setHeader("Referer", SIGNON_PAGE_URL);
				
 				HttpResponse response = null;
 				try {
 					response = app.sendPostRequest(post);
 				} catch (Exception e) {
 					e.printStackTrace();
 					
 					bm.putBoolean("result", false);
 					bm.putString("resultString", "서버 오류");
 					resultHandler.sendMessage(m);
 					return;
 				}
 				
 				HttpEntity entity = response.getEntity();
 				BufferedReader r;
 				try {
 					r = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"));
 					
 					while (true) {
 						String line = r.readLine();
 						if (line == null) break;
 						
 						if (line.contains("비밀번호가 틀렸습니다") || line.contains("등록된 아이디가 아닙니다")) {
 							bm.putBoolean("result", false);
 							bm.putString("resultString", "로그인 실패");
 							resultHandler.sendMessage(m);
 							entity.consumeContent();
 							return;
 						} else if (line.contains("about:blank")) {
 							/* successful */
 							bm.putBoolean("result", true);
 							bm.putInt("method", getMethodId());
 							resultHandler.sendMessage(m);
 							entity.consumeContent();
 							return;
 						}
 					}
 				} catch (Exception e) {
 					bm.putBoolean("result", false);
 					bm.putString("resultString", "파싱 실패");
 					resultHandler.sendMessage(m);
 					
 					try {
 						entity.consumeContent();
 					} catch (IOException e1) {}
 					
 					return;
 				}
 				
 				try {
 					entity.consumeContent();
 				} catch (IOException e) {}
 				
 				/* abnormal status. */
 				bm.putBoolean("result", false);
 				bm.putString("resultString", "알 수 없는 오류.");
 				resultHandler.sendMessage(m);
 			}
 		};
 	}
 	
 	public Runnable getMethodSignOff(final Application app, final Handler resultHandler) {
 		return new Runnable() {
 			public void run() {
 				HttpGet get = new HttpGet(SIGNOFF_URL + "?s_url=about:blank");
 				try {
 					app.sendGetRequest(get);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				Message m = resultHandler.obtainMessage();
 				m.getData().putBoolean("result", true);
 				resultHandler.handleMessage(m);
 			}
 		};
 	}
 	
 	public int getMethodId() {
 		return Application.AUTHENTICATION_METHOD_GALLOG;
 	}
 }
