 /*
  * Copyright (C) 2008 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.xproc.port;
 
 /**
  * Common XProc ports.
  * 
  * @author Herve Quiroz
  */
 public final class XProcPorts
 {
     public static final String ALTERNATE = "alternate";
     public static final String CURRENT = "current";
    public static final String ERROR = "error";
     public static final String ERRORS = "errors";
     public static final String EXIT_STATUS = "exit-status";
     public static final String ITERATION_SOURCE = "iteration-source";
     public static final String PARAMETERS = "parameters";
     public static final String MATCHED = "matched";
    public static final String NOT_MATCHED = "not-matched";
     public static final String QUERY = "query";
     public static final String REPLACEMENT = "replacement";
     public static final String RESULT = "result";
     public static final String SECONDARY = "secondary";
     public static final String SCHEMA = "schema";
     public static final String SOURCE = "source";
     public static final String STYLESHEET = "stylesheet";
     public static final String VIEWPORT_SOURCE = "viewport-source";
     public static final String XPATH_CONTEXT = "xpath-context";
 
     private XProcPorts()
     {
         // No instantiation
     }
 }
