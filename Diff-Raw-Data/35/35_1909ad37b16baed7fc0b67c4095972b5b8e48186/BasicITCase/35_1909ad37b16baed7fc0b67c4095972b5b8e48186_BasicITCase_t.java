 /*
  * JBoss, Home of Professional Open Source.
  * Copyright (c) 2011, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
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
 package org.jboss.libra.basic;
 
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

 import org.jboss.libra.Libra;
 import org.jboss.libra.LibraException;
 import org.junit.Test;
 
 /**
  * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
  */
 public class BasicITCase {
     static class TwoArrays {
         byte a[];
         byte b[];
     }
 
     @Test
     public void testBasic() {
         byte a[] = new byte[1024];
         long size = Libra.getObjectSize(a);
         assertTrue(size >= 1024);
     }
 
     @Test
     public void testDeep() throws LibraException {
         TwoArrays two = new TwoArrays();
         two.a = new byte[1024];
         two.b = new byte[1024];
         long size = Libra.getDeepObjectSize(two);
         assertTrue(size >= 2048);
     }

    @Test
    public void testHashMap() throws LibraException {
        // if it does give a weird exception, we're good
        long size = Libra.getDeepObjectSize(new HashMap());
        assertTrue(size > 0);
    }
 }
