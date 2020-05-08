 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.medic.impl;
 
 import org.eclipse.virgo.medic.eventlog.Level;
 import org.eclipse.virgo.medic.eventlog.LogEvent;
 
 public enum MedicLogEvents implements LogEvent {
 
    MISSING_MESSAGE(1, Level.WARNING), //
    DIRECTORY_CREATION_FAILED(2, Level.WARNING), //
    CONTRIBUTION_FAILED(3, Level.WARNING), //
    DUMP_GENERATED(4, Level.INFO);
 
     private static final String PREFIX = "ME";
 
     private final int code;
 
     private final Level level;
 
     private MedicLogEvents(int code, Level level) {
         this.code = code;
         this.level = level;
     }
 
     public String getEventCode() {
         return String.format("%s%04d%1.1s", PREFIX, this.code, this.level);
     }
 
     public Level getLevel() {
         return level;
     }
 }
