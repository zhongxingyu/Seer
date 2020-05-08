 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard;
 
 import json.RequestDeserializer;
 import json.ResponseSerializer;
 import mecard.exception.MetroSecurityException;
 import mecard.config.ConfigFileTypes;
 import mecard.config.LibraryPropertyTypes;
 import mecard.security.SecurityManager;
 import mecard.config.PropertyReader;
 
 /**
  * Responsible for interpreting incoming MeCard requests.
  *
  * Requests are sent as JSON strings. All requests must include the API key or a 
  * MetroSecurityException is thrown.
  *
  * @author Andrew Nisbet <anisbet@epl.ca>
  */
 public final class Protocol
 {
     public final static String DEFAULT_FIELD_VALUE = "X";
     public final static String TRUE        = "Y";
     public final static String FALSE       = "N";
     public final static String TERMINATE   = "XX0";
     public final static String ACKNOWLEDGE = "XK0";
     public final static String ERROR       = "XE0";
     
    private boolean debugMode;
     
     
     public Protocol()
     { 
         String debug = PropertyReader.getProperties(ConfigFileTypes.ENVIRONMENT)
                 .getProperty(LibraryPropertyTypes.DEBUG.toString());
        if (debug.equalsIgnoreCase("false"))
        {
            debugMode = false;
        }
        else
        {
            debugMode = true;
        }
     }
 
     /**
      * Checks incoming commands to the server, parses what the command, its 
      * security token and if valid then runs the required activity.
      *
      * @param cmd command string request.
      * @return formatted response string.
      */
     public String processInput(String cmd)
     {
         Response response = new Response();
         try
         {
             String jsonCommand = SecurityManager.unEncrypt(cmd);
             RequestDeserializer deserializer = new RequestDeserializer();
             Request request = deserializer.getDeserializedRequest(jsonCommand);
             if (! SecurityManager.isAuthorized(request.getTransactionId()))
             {
                 throw new MetroSecurityException("Unrecognized security token");
             }
             Responder responder = new Responder(request, this.debugMode);
             response = responder.getResponse();
         }
         catch (RuntimeException ex)
         {
             response = Responder.getExceptionResponse(ex);
             ex.printStackTrace(System.out);
         }
         finally
         {
             ResponseSerializer responseSerializer = new ResponseSerializer();
             String serializedResponse = responseSerializer.getSerializedResponse(response);
             return SecurityManager.encrypt(serializedResponse);
         }
     }
 }
