 /* OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2010, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 package org.openremote.android.console.model;
 
 /**
  * It contains controller exception codes, and translate code to message.
  */
 public class ControllerException {
    public static final int CONTROLLER_UNAVAILABLE = 0;
 
    public static final int UNAUTHORIZED = 401;
    public static final int REQUEST_ERROR = 404;
 
    public static final int SERVER_ERROR = 500;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int REFRESH_CONTROLLER = 506;
 
    private static final int CMD_BUILDER_ERROR = 418;
    private static final int NO_SUCH_COMPONENT = 419;
    private static final int NO_SUCH_CMD_BUILDER = 420;
    private static final int INVALID_COMMAND_TYPE = 421;
    private static final int CONTROLLER_XML_NOT_FOUND = 422;
    private static final int NO_SUCH_CMD = 423;
    private static final int INVALID_CONTROLLER_XML = 424;
    private static final int INVALID_POLLING_URL = 425;
    private static final int PANEL_XML_NOT_FOUND = 426;
    private static final int INVALID_PANEL_XML = 427;
    private static final int NO_SUCH_PANEL = 428;
    private static final int INVALID_ELEMENT = 429;
    private static final int INVALID_GROUP = 431;
    
    /**
     * Get exception message by code.
     * 
     * @param erroCode the erro code
     * 
     * @return the string
     */
    public static String exceptionMessageOfCode(int erroCode) {
       String errorMessage = null;
       if (erroCode != 200) {
          switch (erroCode) {
             case REQUEST_ERROR://404
                errorMessage = "The command was sent to an invalid URL.";
                break;
             case CMD_BUILDER_ERROR://418
                errorMessage = "Controller failed to construct an event for this command.";
                break;
             case NO_SUCH_COMPONENT://419
                errorMessage = "Controller did not recognize the sent command id.";
                break;
             case NO_SUCH_CMD_BUILDER://420
                errorMessage = "Command builder not found.";
                break;
             case INVALID_COMMAND_TYPE://421
                errorMessage = "Invalid command type.";
                break;
             case CONTROLLER_XML_NOT_FOUND://422
                errorMessage = "Error in controller - controller.xml is not correctly deployed.";
                break;
             case NO_SUCH_CMD://423
                errorMessage = "Command not found.";
                break;
             case INVALID_CONTROLLER_XML://424
                errorMessage = "Invalid controller.xml.";
                break;
             case INVALID_POLLING_URL://425
                errorMessage = "Invalid polling url.";
                break;
             case PANEL_XML_NOT_FOUND://426
                errorMessage = "panel.xml not found.";
                break;
             case INVALID_PANEL_XML://427
                errorMessage = "Invalid panel.xml.";
                break;
             case NO_SUCH_PANEL://428
                errorMessage = "Current panel identity isn't available. Please rechoose in Settings.";
                break;
             case INVALID_ELEMENT://429
                errorMessage = "Invalid XML element.";
                break;
             case SERVER_ERROR://500
                errorMessage = "Error in controller. Please check controller log.";
                break;
             case UNAUTHORIZED://401
                errorMessage = "You can't execute a protected command without authentication.";
                break;
             case CONTROLLER_UNAVAILABLE://0
             	errorMessage = "Current controller isn't available.";
             	break;
             case INVALID_GROUP: // 431
 				errorMessage = "You are in placed in the wrong group (or you don't have a group).\n\nYou can't access this command.";
 				break;
          }
          if (errorMessage == null) {
             errorMessage = "Occured unknown error, satus code is " + erroCode;
          }
       }
       return errorMessage;
    }
 }
