 /*
  * Copyright 2010 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fast.examples.bats.europe;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 
 import silvertip.PartialMessageException;
import silvertip.GarbledMessageException;
 import fast.bats.europe.FastPitchMessageParser;
 
 public class DecodeFile {
   private static FastPitchMessageParser parser = new FastPitchMessageParser();
   private static ByteBuffer buffer = ByteBuffer.allocate(4096);
 
   public static void main(String[] args) throws Exception {
     if (args.length == 0) {
       System.err.println("Usage: DecodeFile [FILE]");
       return;
     }
     FileInputStream in = new FileInputStream(args[0]);
     FileChannel fc = in.getChannel();
     while (fc.isOpen()) {
       int len = readChannel(fc);
       if (len > 0) {
         parse();
       } else if (len < 0) {
         fc.close();
       }
     }
     System.out.println("OK");
   }
 
   private static int readChannel(FileChannel fc) {
     try {
       return fc.read(buffer);
     } catch (IOException e) {
       return -1;
     }
   }
 
   private static void parse() {
     buffer.flip();
     while (buffer.hasRemaining()) {
       buffer.mark();
       try {
         System.out.println(parser.parse(buffer));
       } catch (PartialMessageException e) {
         buffer.reset();
         break;
      } catch (GarbledMessageException e) {
        e.printStackTrace();
        break;
       }
     }
     buffer.compact();
   }
 }
 
