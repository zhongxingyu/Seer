 /*-
  * Copyright (c) 2006, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.conf;
 
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 
 /**
  * Store account configuration for LogicMail
  */
 public class AccountConfig implements Serializable {
     public static int TYPE_POP = 0;
     public static int TYPE_IMAP = 1;
     
     private String _acctName;
     private String _serverName;
     private int _serverType;
     private boolean _serverSSL;
     private String _serverUser;
     private String _serverPass;
     private int _serverPort;
 
     public AccountConfig() {
         _acctName = "";
         _serverName = "";
         _serverType = TYPE_POP;
         _serverSSL = false;
         _serverUser = "";
         _serverPass = "";
        _serverPort = 110;
     }
     
     public AccountConfig(byte[] byteArray) {
         deserialize(byteArray);
     }
 
     public String getAcctName() {
         return _acctName;
     }
     
     public void setAcctName(String acctName) {
         _acctName = acctName;
     }
     
     public String getServerName() {
         return _serverName;
     }
     
     public void setServerName(String serverName) {
         _serverName = serverName;
     }
     
     public int getServerType() {
         return _serverType;
     }
     
     public void setServerType(int serverType) {
         _serverType = serverType;
     }
     
     public boolean getServerSSL() {
         return _serverSSL;
     }
     
     public void setServerSSL(boolean serverSSL) {
         _serverSSL = serverSSL;
     }
 
     public int getServerPort() {
         return _serverPort;
     }
 
     public void setServerPort(int serverPort) {
         _serverPort = serverPort;
     }
     
     public String getServerUser() {
         return _serverUser;
     }
     
     public void setServerUser(String serverUser) {
         _serverUser = serverUser;
     }
     
     public String getServerPass() {
         return _serverPass;
     }
     
     public void setServerPass(String serverPass) {
         _serverPass = serverPass;
     }
 
     public byte[] serialize() {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         DataOutputStream output = new DataOutputStream(buffer);
         
         try {
             output.writeUTF(_acctName);
             output.writeUTF(_serverName);
             output.writeInt(_serverType);
             output.writeBoolean(_serverSSL);
             output.writeUTF(_serverUser);
             output.writeUTF(_serverPass);
             output.writeInt(_serverPort);
             return buffer.toByteArray();
         } catch (IOException exp) {
             return null;
         }
     }
 
     public void deserialize(byte[] byteArray) {
         ByteArrayInputStream buffer = new ByteArrayInputStream(byteArray);
         DataInputStream input = new DataInputStream(buffer);
         
         try {
             _acctName = input.readUTF();
             _serverName = input.readUTF();
             _serverType = input.readInt();
             _serverSSL = input.readBoolean();
             _serverUser = input.readUTF();
             _serverPass = input.readUTF();
             _serverPort = input.readInt();
         } catch (IOException exp) {
             _acctName = "";
             _serverName = "";
             _serverType = TYPE_POP;
             _serverSSL = false;
             _serverUser = "";
             _serverPass = "";
            _serverPort = 110;
         }
     }
 }
 
