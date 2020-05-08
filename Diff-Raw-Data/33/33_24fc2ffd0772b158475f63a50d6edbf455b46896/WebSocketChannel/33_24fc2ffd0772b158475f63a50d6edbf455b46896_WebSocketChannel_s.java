 /*
  * Copyright 2012 Future Systems
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
 package org.araqne.httpd.impl;
 
 import java.net.InetSocketAddress;
 
 import org.araqne.httpd.WebSocket;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
 
 public class WebSocketChannel implements WebSocket {

 	private Channel channel;
 
 	public WebSocketChannel(Channel channel) {
 		this.channel = channel;
 	}
 
 	@Override
 	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) channel.getLocalAddress();
 	}
 
 	@Override
 	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.getRemoteAddress();
 	}
 
 	@Override
 	public void send(String text) {
 		if (channel != null)
 			channel.write(new TextWebSocketFrame(text));
 	}
 
 	@Override
 	public void close() {
 		if (channel != null) {
 			channel.close();
 			channel = null;
 		}
 	}
 
 }
