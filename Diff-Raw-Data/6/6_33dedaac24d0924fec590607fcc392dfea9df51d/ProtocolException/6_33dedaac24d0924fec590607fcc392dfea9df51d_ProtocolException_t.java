 /*
  * Copyright 2011 Gregory P. Moyer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.syphr.mythtv.protocol;
 
 import java.io.IOException;
 
 /**
  * This exception indicates that an unexpected response was received from the backend or
  * an attempt was made to send data to a backend that does not understand it. The likely
  * cause of this is that the backend is implementing a different version of the protocol
  * than what the client declared during the initial connection or an invalid parameter was
  * passed to a command.<br>
  * <br>
  * This exception will not be thrown for command errors, such as requesting data for
  * something that does not exist. See {@link CommandException} instead.
  *
  * @author Gregory P. Moyer
  */
 public class ProtocolException extends IOException
 {
     /**
      * Serialization ID
      */
     private static final long serialVersionUID = 1L;
 
     private final Direction direction;
 
     public ProtocolException(String message, Direction direction)
     {
         this(message, direction, null);
     }
 
     public ProtocolException(String message, Direction direction, Throwable cause)
     {
         super(message, cause);
         this.direction = direction;
     }
 
     /**
      * Get the direction of the communication when the exception occurred.
      *
      * @return {@link Direction#RECEIVE} if the problem was in a message received from the
      *         backend; {@link Direction#SEND} if the problem was in a message sent from
      *         the client
      */
     public Direction getDirection()
     {
         return direction;
     }
 
    @Override
    public String toString()
    {
        return super.toString() + " " + getDirection();
    }

     /**
      * This enum represents the network communication direction.
      *
      * @author Gregory P. Moyer
      */
     public static enum Direction
     {
         /**
          * Client => Server
          */
         SEND,
 
 
         /**
          * Server => Client
          */
         RECEIVE;
     }
 }
