 /*
  * Copyright 2013 Roland Gisler, GISLER iNFORMATiK, Switzerland.
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
 package ch.gitik.bpms.ant;
 
 import org.apache.tools.ant.BuildException;
 
 import ch.gitik.bpms.common.ConfigException;
 import ch.gitik.bpms.common.Message;
 import ch.gitik.bpms.xmpp.XMPPConfig;
 import ch.gitik.bpms.xmpp.XMPPSender;
 import ch.gitik.bpms.xmpp.XMPPSenderFactory;
 
 /**
  * ANT-Task fuer BPMS (Build Process Messaging System). Dieser ANT-Task kann in
  * ein build.xml eingebunden werden und versendet dann Messages an interessierte
  * Benutzer ueber einen XMPP (Jabber) Server.
  * @author Roland Gisler
  */
 public class BpmsXmppTask extends AbstractBpmsTask {
 
    /** Servername. */
    private String server = null;
 
    /** Serverport */
    private int port = 0;
 
    /** Username. */
    private String user = null;
 
    /** Passwort. */
    private String password = null;
 
    /** Receiver. */
    private String receiver = null;
 
    /** ConferenceFlag. */
    private boolean conference = false;
 
    /** XMPP Configuration. */
    private XMPPConfig config = null;
 
    /** JabberBean. */
    private XMPPSender xmpp = null;
 
    /**
     * Check the input and throw a BuildException when it is null.
     * @throws BuildException
     *            Buildfehler
     * @throws IllegalArgumentException
     *            Fehlendes Attribut
     */
    public void checkAttributes() throws BuildException {
 
       // Server und User sind MUST-Attribute
       if (this.server == null) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + "Server not defined");
       }
 
       if (this.user == null) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + "User not defined");
       }
 
       if (this.receiver == null) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + "Receiver or conference not defined");
       }
 
       // Port ist optional, Default ist 5222
       if (this.port == 0) {
          this.port = 5222;
       }
 
       // Passwort ist optional, Default ist ""
       if (this.password == null) {
          this.password = "";
       }
 
       // Konfig erzeugen, und erneut pruefen
       this.config = new XMPPConfig(this.server, this.port, this.user, this.password, this.receiver,
             this.conference);
       try {
          this.config.validate();
       } catch (ConfigException e) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + e);
       }
    }
 
    /**
     * Liefert den Servernamen zurueck.
     * @return Name des Servers
     */
    public final String getServer() {
       return server;
    }
 
    /**
     * Setzt den Namen des Servers.
     * @param string
     *           Servername
     */
    public final void setServer(final String string) {
       server = string;
    }
 
    /**
     * Liefert den Namen des Benutzers zurueck.
     * @return Benutzername
     */
    public final String getUser() {
       return user;
    }
 
    /**
     * Setzt das Passwort.
     * @param string
     *           Passwort
     */
    public final void setPassword(final String string) {
       password = string;
    }
 
    /**
     * Setzt den Namen des Servers.
     * @param string
     */
    public final void setUser(final String string) {
       user = string;
    }
 
    /**
     * Liefert den Receiver zurueck.
     * @return Receiver.
     */
    public final String getReceiver() {
       if (this.conference) {
          return null;
       } else {
          return this.receiver;
       }
    }
 
    /**
     * Liefert die Conference zurueck.
     * @return Conference, oder Null wenn nicht gesetzt.
     */
    public final String getConference() {
       if (this.conference) {
          return this.receiver;
       } else {
          return null;
       }
    }
 
    /**
     * Setzt den Receiver.
    * @param string
     *           Receiver.
     */
    public final void setReceiver(final String name) {
       this.conference = false;
       this.receiver = name;
    }
 
    /**
     * Setzt die Conference.
    * @param string
     *           Conference.
     */
    public final void setConference(final String name) {
       this.setReceiver(name);
       this.conference = true;
    }
 
    /**
     * @see ch.gitik.bpms.ant.AbstractBpmsTask#sendMessage(ch.gitik.bpms.common.Message)
     */
    @Override
    public void sendMessage(Message msg) {
       this.xmpp.sendMessage(this.receiver, msg);
    }
 
    /**
     * @see ch.gitik.bpms.ant.AbstractBpmsTask#initialize()
     */
    @Override
    public void initialize() {
       this.xmpp = XMPPSenderFactory.getXMPPSender(config);
    }
 }
