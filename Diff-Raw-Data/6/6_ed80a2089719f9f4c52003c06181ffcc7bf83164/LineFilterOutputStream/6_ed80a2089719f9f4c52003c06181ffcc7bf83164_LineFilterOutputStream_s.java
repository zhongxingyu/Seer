 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
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
 package builder.smartfrog.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FilterOutputStream;
 import java.io.OutputStream;
 
 /**
  *
  * @author Dominik Pospisil
  */
 public abstract class LineFilterOutputStream extends FilterOutputStream {
 
    private boolean wasCR;
    private static final byte CR = 13;
    private static final byte LF = 10;
    
    private ByteArrayOutputStream bos = new ByteArrayOutputStream(256);
 
    public LineFilterOutputStream(OutputStream out) {
       super(out);
    }
 
    public void write(byte[] bytes) {
       for (byte b : bytes) byteAcquired(b);
    }
 
    public void write(byte[] bytes, int off, int len) {
       for (int i = off; i < (off + len); i++) byteAcquired(bytes[i]);
    }
 
    public void write(int b) {
       byteAcquired((byte) b);
    }
    
    private void byteAcquired(byte b) {
       if ((b == LF) && (wasCR)) {
          wasCR = false;
          return;
       }
       
       wasCR = (b == CR);
 
      if ((b == CR) || (b == LF)) {
          String line = bos.toString();
          writeLine(line);
          bos.reset();
       } else {
         //TODO potential memory leak, some bound should be setup and bos write and reset
          bos.write(b);
       }
       
    }
    
    protected abstract void writeLine(String line);
 }
