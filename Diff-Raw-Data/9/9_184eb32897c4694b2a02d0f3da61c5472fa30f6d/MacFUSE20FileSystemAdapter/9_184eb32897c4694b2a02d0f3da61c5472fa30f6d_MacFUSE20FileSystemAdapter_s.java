 /*-
  * jFUSE - FUSE bindings for Java
  * Copyright (C) 2008-2009  Erik Larsson <erik82@kth.se>
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package org.catacombae.jfuse;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.nio.ByteBuffer;
 import org.catacombae.jfuse.types.fuse26.FUSEFileInfo;
 import org.catacombae.jfuse.types.macfuse20.Setattr_x;
 import org.catacombae.jfuse.types.system.Timespec;
 
 /**
  *
  * @author erik
  */
public class MacFUSE20FileSystemAdapter extends FUSEFileSystemAdapter
         implements MacFUSE20FileSystem {
 
     public MacFUSE20Capabilities getMacFUSECapabilities() {
         MacFUSE20Capabilities c = new MacFUSE20Capabilities();
 
         // Find out our capabilities through reflection.
         Class baseClass = MacFUSE20Operations.class;
         Class<?> subClass = this.getClass();
 
         while(!subClass.equals(MacFUSE20FileSystemAdapter.class)) {
             for(Method m : baseClass.getDeclaredMethods()) {
                 try {
                     Field f = c.getClass().getField(m.getName());
 
                     try {
                         try {
                             subClass.getDeclaredMethod(m.getName(), m.getParameterTypes());
                             f.setBoolean(c, true);
                         } catch(NoSuchMethodException e) {
                             //f.setBoolean(c, false);
                         }
                     } catch(IllegalAccessException iae) {
                         throw new RuntimeException(iae);
                     }
                 } catch(NoSuchFieldException e) {
                     throw new RuntimeException("No field \"" + m.getName() +
                             "\" in MacFUSE20Capabilities.", e);
                 }
             }
 
             subClass = subClass.getSuperclass();
         }
 
         return c;
     }
 
     public int exchange(ByteBuffer path1, ByteBuffer path2, long options) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     public int getxtimes(ByteBuffer path, Timespec bkuptime, Timespec crtime) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     public int setbkuptime(ByteBuffer path, Timespec tv) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     public int setcrtime(ByteBuffer path, Timespec tv) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     public int chflags(ByteBuffer path, int flags) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     public int setattr_x(ByteBuffer path, Setattr_x attr) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 
     public int fsetattr_x(ByteBuffer path, Setattr_x attr, FUSEFileInfo fi) {
         throw new UnsupportedOperationException("Not supposed to call this method.");
     }
 }
