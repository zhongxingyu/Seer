 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: TimerFactory.java,v 1.2 2006-08-29 21:55:07 veiming Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 package com.sun.identity.common;
 
 import com.sun.identity.shared.debug.Debug;
 import java.util.Timer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 
 public class TimerFactory {
     private static int concRate = 16;
     private static final String TIMER_CLASS = "java.util.Timer";
     private static final Debug debug = Debug.getInstance("amLog");
     private static Timer timer = null;
 
     static {
         timer = constructTimer("AMTimer");
     }
 
     private static Timer constructTimer(String timerName) {
         Timer retVal = null;
         if (timerName != null) {
             Object argList[] = {timerName, Boolean.TRUE};
             try {
                 Class cls = Class.forName(TIMER_CLASS);
                 Class parameters[] = { String.class, Boolean.TYPE };
                 Constructor strConstructor =
                     cls.getDeclaredConstructor(parameters);
                 retVal = (Timer)strConstructor.newInstance(argList);
             } catch(Exception ex) {
                 debug.warning("Error while creating timer with String " +
                     "parameter.  Using Timer without the parameter.", ex);
                 retVal = new Timer(true);
             }
         } else {
             retVal = new Timer(true);
         }
         return retVal;
     }
 
     public static Timer getTimer() {
         return timer;
     }
 }
