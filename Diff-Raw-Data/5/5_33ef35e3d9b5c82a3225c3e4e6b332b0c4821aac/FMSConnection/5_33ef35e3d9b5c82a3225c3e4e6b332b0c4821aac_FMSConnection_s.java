 // LATER: Rename this file. It is used for both FMS and Freetalk.
 /* Subclass the GNU inetlib NNTPClass to support FMS XGETTRUST.
  *
  *  Copyright (C) 2010, 2011 Darrell Karbott
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.0 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  *  Author: djk@isFiaD04zgAgnrEC5XJt1i4IE7AkNPqhBG5bONi6Yks
  *
  *  This file was developed as component of
  * "fniki" (a wiki implementation running over Freenet).
  */
 
 package fmsutil;
 
 import java.io.IOException;
 
 import gnu.inet.nntp.NNTPConnection;
 import gnu.inet.nntp.StatusResponse;
 
 class FMSConnection extends NNTPConnection {
     public final static int MESSAGE = 1;
     public final static int TRUSTLIST = 2;
     public final static int PEERMESSAGE = 3;
     public final static int PEERTRUSTLIST = 4;
 
     public static String trustKindToString(int constant) {
         switch (constant) {
         case MESSAGE: return "MESSAGE";
         case TRUSTLIST: return "TRUSTLIST";
         case PEERMESSAGE: return "PEERMESSAGE";
         case PEERTRUSTLIST: return "PEERTRUSTLIST";
         default:
             throw new IllegalArgumentException("Invalid trust constant: " + constant);
         }
     }
 
     public FMSConnection(String host, int port) throws IOException {
         super(host, port);
     }
 
     public boolean supportsXGETTRUST() {
         if (getWelcome() == null) {
             return false;
         }
         return getWelcome().indexOf("Freetalk") == -1;
     }
 
     // Hmmmm... would be better to raise NNTPExceptions here.
     // Returns -1 for 'null' trust.
     public int xgettrust(int kind, String fmsId) throws IOException {
         if (!supportsXGETTRUST()) {
             return -1; // Freetalk doesn't support trust extensions.
         }
         send(String.format("XGETTRUST %s %s", trustKindToString(kind), fmsId));
         String reply = read();
         StatusResponse response = parseResponse(reply, false);
         if (response.getStatus() < 200 || response.getStatus() > 299) {
             throw new IOException("XGETTRUST NNTP request failed: " + reply);
         }
         String fields[] = reply.split(" ");
         if (fields.length != 2) {
             throw new IOException("Couldn't parse reply: " + reply);
         }
         if (fields[1].equals("null")) {
             return -1;
         } else {
             try {
                 return Integer.parseInt(fields[1]);
             } catch (NumberFormatException nfe) {
                 throw new IOException("Couldn't parse reply: " + reply);
             }
         }
     }
 }
