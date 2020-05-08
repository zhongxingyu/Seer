 /**
  * Copyright (C) 2009 BonitaSoft S.A.
  * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2.0 of the License, or
  * (at your option) any later version.
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.bonitasoft.console.server.listener;
 
 import java.io.IOException;
import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.nio.channels.SocketChannel;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 /**
  * Studio Watchdog Servlet.
  * 
  * @author Julien Mege
  */
 public class StudioWatchdogListener implements ServletContextListener {
 
     private static final Logger LOGGER = Logger.getLogger(StudioWatchdogListener.class.getName());
 
     private static final int PORT = 6969;
 
     private static final long TIMER = 5000;
 
     private static final String WATCHDOG_PORT = "org.bonitasoft.studio.watchdog.port";
 
     private static final String WATCHDOG_TIMER = "org.bonitasoft.studio.watchdog.timer";
 
     @Override
     public void contextInitialized(final ServletContextEvent sce) {
        final Thread watchdog = new Thread(new Runnable() {
 
             @Override
             public void run() {
                 final int port = getPort();
                 final long timer = getTimer();
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(Level.WARNING, "Bonita Studio watchdog process has started on "+port+" with a delay of "+timer+"ms");
                 }
                 try {
                     while (isAlive(port, timer)) {
                     }
                 } catch (final IOException e) {
                     if (LOGGER.isLoggable(Level.WARNING)) {
                         LOGGER.log(Level.WARNING, "Bonita Studio process has been killed, terminating tomcat process properly");
                     }
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(Level.FINE,"Studio is considered killed due to:", e);
                     }
                     System.exit(-1);
                 }
             }
         });
        watchdog.setDaemon(true);
        watchdog.start();
     }
 
     @Override
     public void contextDestroyed(final ServletContextEvent arg0) {
     }
 
     private static long getTimer() {
         final String timerValue = System.getProperty(WATCHDOG_TIMER, String.valueOf(TIMER));
         long timer = TIMER;
         try {
             timer = Long.valueOf(timerValue);
             if (timer < 0) {
                 throw new Exception("Invalid timer value : " + timer);
             }
         } catch (final Exception e) {
             if (LOGGER.isLoggable(Level.INFO)) {
                 LOGGER.log(Level.INFO, "org.bonitasoft.studio.watchdog.timer property must be a valid timer value (> 0)");
             }
         }
         return timer;
     }
 
     protected static int getPort() {
         final String portValue = System.getProperty(WATCHDOG_PORT, String.valueOf(PORT));
         int port = PORT;
         try {
             port = Integer.valueOf(portValue);
             if (port < 1024 || port > 65535) {
                 throw new Exception("Invalid port range : " + port);
             }
         } catch (final Exception e) {
             if (LOGGER.isLoggable(Level.INFO)) {
                 LOGGER.log(Level.INFO, "org.bonitasoft.studio.watchdog.port property must be a valid port number [1024->65535]");
             }
         }
         return port;
     }
 
     private static boolean isAlive(final int port, final long timer) throws IOException {
         final SocketChannel sChannel = SocketChannel.open();
         final Socket socket = sChannel.socket();
        socket.connect(new InetSocketAddress(InetAddress.getByName("localhost"),port));
         while (!sChannel.finishConnect()) {
             try {
                 Thread.sleep(100);
             } catch (final InterruptedException e) {
 
             }
         }
         try {
             Thread.sleep(timer);
         } catch (final InterruptedException e) {
 
         }
         socket.close();
         while (socket.isConnected()) {
             try {
                 Thread.sleep(100);
             } catch (final InterruptedException e) {
 
             }
         }
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.log(Level.FINE, "Bonita Studio JVM is alive");
         }
         return true;
     }
 
 }
