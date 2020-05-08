 /*
  * Copyright (C) 2011  Francois Forster
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.tcpdelay;
 
 import java.nio.ByteBuffer;
 import java.nio.channels.SocketChannel;
 import java.util.concurrent.Delayed;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Data to be written to target.
  */
 public class TCPData implements Delayed {
     private ByteBuffer buffer;
     private int len;
     private SocketChannel channel;
     private long delayedUntil;
 
     public TCPData(ByteBuffer buffer, int len, SocketChannel channel, long delayedUntil) {
         this.buffer = buffer;
         this.len = len;
         this.channel = channel;
         this.delayedUntil = delayedUntil;
     }
 
     public ByteBuffer getBuffer() {
         return buffer;
     }
 
     public int getLen() {
         return len;
     }
 
     public SocketChannel getChannel() {
         return channel;
     }
 
     public long getDelay(TimeUnit unit) {
         return unit.convert(delayedUntil - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
     }
 
     public int compareTo(Delayed o) {
         TCPData data = (TCPData) o;
        return (delayedUntil == data.delayedUntil && channel.equals(data.channel) && len == data.len) ? 0 : 1;
     }
 }
