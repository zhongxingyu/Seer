 /*
  * Copyright (c) 2012, Keeley Hoek
  * All rights reserved.
  * 
  * Redistribution and use of this software in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  * 
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
  * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.github.escortkeel.circle;
 
 import com.github.escortkeel.circle.exception.IRCNameReservedException;
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Objects;
 import com.github.escortkeel.circle.event.IRCAdapter;
 import com.github.escortkeel.circle.event.IRCRawMessageEvent;
 import com.github.escortkeel.circle.event.IRCMotdEvent;
 
 /**
  * This class implements an IRC client connection to an IRC server.
  *
  * @author Keeley Hoek (escortkeel@live.com)
  */
 public class IRCClient implements Closeable {
 
     private final Socket socket;
     private final BufferedReader in;
     private final PrintStream out;
     private final String nickname;
     private final String password;
     private final String username;
     private final String realname;
     private final boolean invisible;
     private final StringBuilder motd = new StringBuilder();
     private final ArrayList<IRCAdapter> adapters = new ArrayList<>();
 
     /**
      * Creates a new
      * <code>IRCClient</code> and connects it to port 6667 (the default IRC
      * port) on the named host, using the specified nickname.
      * <p>
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param nickname the nickname.
      *
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public IRCClient(String address, String nickname) throws IOException, IRCNameReservedException {
         this(address, 6667, nickname);
     }
 
     /**
      * Creates a new
      * <code>IRCClient</code> and connects it to the specified port number on
      * the named host, using the specified nickname.
      * <p>
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param port the port number.
      * @param nickname the nickname.
      *
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public IRCClient(String address, int port, String nickname) throws IOException {
         this(address, 6667, nickname, nickname, nickname, false);
     }
 
     /**
      * Creates a new
      * <code>IRCClient</code> and connects it to the specified port number on
      * the named host, using the specified nickname, username, real name, and
      * invisibility flag.
      * <p>
      * If the specified host is <tt>null</tt> it is the equivalent of specifying
      * the address as
      * <tt>{@link java.net.InetAddress#getByName InetAddress.getByName}(null)</tt>.
      * In other words, it is equivalent to specifying an address of the loopback
      * interface.
      *
      * @param address the host name, or <code>null</code> for the loopback
      * address.
      * @param port the port number.
      * @param nickname the nickname.
      * @param username the username.
      * @param realname the real name.
      * @param invisible whether the client should be invisible to other clients.
      *
      * @exception IOException if an I/O error occurs when creating the
      * connection.
      */
     public IRCClient(String address, int port, String nickname, String username, String realname, boolean invisible) throws IOException {
         Objects.requireNonNull(nickname);
         Objects.requireNonNull(username);
         Objects.requireNonNull(realname);
 
         if (nickname.contains(" ")) {
             throw new IllegalArgumentException("Nickname must not contain spaces");
         }
 
         if (username.contains(" ")) {
             throw new IllegalArgumentException("Username must not contain spaces");
         }
 
         this.socket = new Socket(address, port);
         this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         this.out = new PrintStream(socket.getOutputStream());
 
         this.nickname = nickname;
         this.password = Long.toString(new SecureRandom().nextLong(), 36);
         this.username = username;
         this.realname = realname;
 
         this.invisible = invisible;
 
         handshake();
     }
 
     /**
      * Attaches a new
      * <code>IRCAdapter</code> instance to this
      * <code>IRCClient</code>.
      */
     public void addClient(IRCAdapter client) {
         adapters.add(client);
     }
 
     /**
      * Returns the closed state of the socket.
      *
      * @return true if the socket has been closed
      * @see #close
      */
     public boolean isClosed() {
         return socket.isClosed();
     }
 
     /**
      * Closes this
      * <code>IRCClient</code> gracefully with the specified reason. This method
      * is synonymous with the
      * <code>close</code> method.
      *
      * @param reason the reason for closing the connection
      * @throws IOException if an I/O error occurs
      */
     public void quit(String reason) throws IOException {
         close(reason);
     }
 
     /**
      * Closes this IRCClient gracefully with the specified reason. If the
      * connection is already closed then invoking this method has no effect.
      *
      * @param reason the reason for closing the connection
      * @throws IOException if an I/O error occurs
      */
     public void close(String reason) throws IOException {
         out.println("QUIT :" + reason);
         socket.close();
     }
 
     /**
      * Closes this IRCClient gracefully. If the connection is already closed
      * then invoking this method has no effect.
      *
      * @throws IOException if an I/O error occurs
      */
     @Override
     public void close() throws IOException {
         out.println("QUIT");
         socket.close();
     }
 
     private void handshake() throws IOException {
         out.println("PASS " + password);
         out.println("USER " + username + " " + (invisible ? "8" : "0") + " * :" + realname);
         out.println("NICK " + nickname);
 
         Thread worker = new Thread("cIRCle Thread") {
             @Override
             public void run() {
                 try {
                     String s;
                     while ((s = in.readLine()) != null) {
                         handleMessage(s);
                     }
                 } catch (IOException ex) {
                     try {
                         close();
                     } catch (IOException ex2) {
                     }
                 }
             }
         };
 
         worker.setDaemon(true);
         worker.start();
     }
 
     private void handleMessage(String raw) {
         String source = null;
         int split = raw.indexOf(' ');
         if (raw.startsWith(":")) {
             source = raw.substring(1, split);
             raw = raw.substring(split + 1);
             split = raw.indexOf(' ');
         }
 
         String keyword = raw.substring(0, split);
         String args = raw.substring(split + 1);
 
         int reply = -1;
         try {
             if (keyword.length() == 3) {
                 reply = Integer.parseInt(keyword);
             }
         } catch (NumberFormatException nfe) {
         }
 
         if (reply == -1) {
             switch (keyword) {
                 case "PING":
                     out.println("PONG " + args);
                     break;
             }
         } else {
             switch (IRCReply.toEnum(reply)) {
                 case MOTDSTART: {
                     int motdStart = args.indexOf(':');
                     String sub = args.substring(motdStart);
                     if (motdStart != -1 && sub.length() != 1 && sub.length() > 3) {
                         motdStart = sub.indexOf(' ');
                         sub = sub.substring(motdStart + 1, sub.length() - 3);
 
                         if (motdStart != -1 && sub.length() != 1) {
                             motd.append(sub).append("\n");
                         }
                     }
                     break;
                 }
                 case MOTD: {
                     int motdStart = args.indexOf(':');
                     String sub = args.substring(motdStart);
                     if (motdStart != -1 && sub.length() != 1) {
                         motdStart = sub.indexOf(' ');
                         sub = sub.substring(motdStart + 1);
 
                         if (motdStart != -1 && sub.length() != 1) {
                             motd.append(sub).append("\n");
                         }
                     }
                     break;
                 }
                 case ENDOFMOTD: {
                     int motdStart = args.indexOf(':');
                     if (motdStart != -1 && args.substring(motdStart).length() != 1) {
                         motd.append(args.substring(motdStart + 1)).append("\n");
                     }
                     fireMotdEvent(new IRCMotdEvent(motd.toString()));
                     break;
                 }
             }
         }
 
         fireRawMessageEvent(new IRCRawMessageEvent(source, raw));
     }
 
     private void fireMotdEvent(IRCMotdEvent e) {
         for (IRCAdapter l : adapters) {
             l.onMotd(e);
         }
     }
 
     private void fireRawMessageEvent(IRCRawMessageEvent e) {
         for (IRCAdapter l : adapters) {
             l.onRawMessage(e);
         }
     }
 }
