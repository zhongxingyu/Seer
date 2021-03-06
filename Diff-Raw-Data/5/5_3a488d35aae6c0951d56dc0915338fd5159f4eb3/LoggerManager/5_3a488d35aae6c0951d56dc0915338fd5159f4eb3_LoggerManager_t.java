 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
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
 package org.xwiki.logging;
 
 import org.xwiki.component.annotation.ComponentRole;
 import org.xwiki.observation.EventListener;
 
 /**
  * Provide some logging management APIs such as the ability to redirect logs to an {@link EventListener} or to a
  * {@link LogQueue}.
  * 
  * @version $Id$
  * @since 3.2M3
  */
 @ComponentRole
 public interface LoggerManager
 {
     /**
      * Grab subsequent logs produced by the current thread and send them to the provided listener.
      * <p>
     * Note that the logs will not be sent anymore to SLF4J. In addition, it also overrides any previous call to
     * {@link #pushLogListener(EventListener)} (which will get active again after a call to {@link #popLogListener()}).
      * 
      * @param listener the listener that will receive all future logging events
      */
     void pushLogListener(EventListener listener);
 
     /**
      * Remove the current listener from the current thread stack.
      * <p>
      * If several listeners have been pushed it makes the previous one active again.
      * 
      * @return the previous log events listener for the current thread
      */
     EventListener popLogListener();
 }
