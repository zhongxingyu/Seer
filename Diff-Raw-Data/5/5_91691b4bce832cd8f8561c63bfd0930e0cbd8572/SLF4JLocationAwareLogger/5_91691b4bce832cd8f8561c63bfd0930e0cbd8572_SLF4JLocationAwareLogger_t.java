 /**
  * Copyright (C) 2009 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.gatein.common.logging;
 
 import org.slf4j.spi.LocationAwareLogger;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.UndeclaredThrowableException;
 
 /**
  * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
  * @version $Revision$
  */
 class SLF4JLocationAwareLogger extends Logger
 {
 
   /** . */
    private static final String FQCN = SLF4JLocationAwareLogger.class.getName();
 
    /** . */
    private final LocationAwareLogger delegate;
 
    public SLF4JLocationAwareLogger(LocationAwareLogger delegate)
    {
       this.delegate = delegate;
    }
 
    @Override
    protected void doLog(LogLevel level, Object msg, Object[] argArray, Throwable throwable)
    {
       try
       {
          switch (LOGGER)
          {
             case SLF_1_5:
                // 1.5 : log(Marker marker, String fqcn, int level, String message, Throwable t);
               log.invoke(delegate, null, FQCN, level.getSLF4Jlevel(), String.valueOf(msg), throwable);
                break;
             case SLF_1_6:
                // 1.6 : log(Marker marker, String fqcn, int level, String message, Object[] argArray, Throwable t);
               log.invoke(delegate, null, FQCN, level.getSLF4Jlevel(), String.valueOf(msg), argArray, throwable);
                break;
          }
       }
       catch (IllegalAccessException e)
       {
          AssertionError ae = new AssertionError("Unexpected issue when using SLF4J location aware logger");
          ae.initCause(e);
          throw ae;
       }
       catch (InvocationTargetException e)
       {
          Throwable cause = e.getCause();
          if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else if (cause instanceof Error)
          {
             throw (Error)cause;
          }
          else
          {
             throw new UndeclaredThrowableException(cause);
          }
       }
    }
 
    @Override
    protected org.slf4j.Logger getDelegate()
    {
       return delegate;
    }
 }
