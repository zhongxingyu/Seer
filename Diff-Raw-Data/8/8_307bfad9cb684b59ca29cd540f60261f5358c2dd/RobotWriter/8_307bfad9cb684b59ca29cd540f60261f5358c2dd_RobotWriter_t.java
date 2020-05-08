 /*
    Copyright 2011 Sebastian Kaspari
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package de.pocmo.springobot.control;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 
 /**
  * RobotWriter class for sending commands via the Open Interface.
  *
  * @author Sebastian Kaspari <s.kaspari@gmail.com>
  */
 public class RobotWriter
 {
     /**
      * The output stream to write to.
      */
     private OutputStream stream;
 
     /**
      * Create a new RobotWriter instance.
      *
      * @param stream The output stream to write to.
      */
     public RobotWriter(OutputStream stream)
     {
         this.stream = stream;
     }
 
     /**
      * This command starts the OI. You must always send the Start
      * command before sending any other commands to the OI.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Passive. Create beeps once to
      * acknowledge it is starting from “off” mode.
      */
     public void start()
     {
         this.send(Opcode.START);
     }
 
     /**
      * This command sets the baud rate in bits per second (bps)
      * at which OI commands and data are sent according to the
      * baud code sent in the data byte. The default baud rate at
      * power up is 57600 bps.
      *
      * Once the baud rate is changed, it persists until Create
      * is power cycled by pressing the power button or removing
      * the battery, or when the battery voltage falls below the
      * minimum required for processor operation.
      *
      * You must wait 100ms after sending this command before
      * sending additional commands at the new baud rate.
      *
      * @param baudRate Use one of the constants of the BaudRate class.
      */
     public void setBaudRate(int baudRate)
     {
         this.send(Opcode.BAUD, baudRate);
     }
 
     /**
      * This command puts the OI into Safe mode, enabling user
      * control of Create. It turns off all LEDs. The OI can be in
      * Passive, Safe, or Full mode to accept this command.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Safe.
      */
     public void enableSafeMode()
     {
         this.send(Opcode.SAFE);
     }
 
     /**
      * This command gives you complete control over Create
      * by putting the OI into Full mode, and turning off the cliff,
      * wheel-drop and internal charger safety features. That is, in
      * Full mode, Create executes any command that you send
      * it, even if the internal charger is plugged in, or the robot
      * senses a cliff or wheel drop.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Full.
      */
     public void enableFullMode()
     {
         this.send(Opcode.FULL);
     }
 
     /**
      * This command starts the requested built-in demo.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Passive.
      *
      * @param demo Demo number (1-9, 255 for abort)
      */
     public void startDemo(int demo)
     {
         this.send(Opcode.DEMO, demo);
     }
 
     /**
      * This command starts the Cover demo.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Passive.
      */
     public void startCover()
     {
         this.send(Opcode.COVER);
     }
 
     /**
      * This command starts the Cover and Dock demo.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Passive.
      */
     public void startCoverAndDock()
     {
         this.send(Opcode.COVER_AND_DOCK);
     }
 
     /**
      * This command starts the Spot Cover demo.
      *
      * Available in modes: Passive, Safe, or Full.
      *
      * Changes mode to: Passive.
      */
     public void startSpotCover()
     {
         this.send(Opcode.SPOT);
     }
 
     /**
      * This command controls Create’s drive wheels.
      *
      * A Drive command with a positive velocity and a
      * positive radius makes Create drive forward while turning
      * toward the left. A negative radius makes Create turn toward
      * the right. Special cases for the radius make Create turn
      * in place or drive straight, as specified below. A negative
      * velocity makes Create drive backward.
      *
      * NOTE: Internal and environmental restrictions may prevent
      * Create from accurately carrying out some drive commands.
      * For example, it may not be possible for Create to drive at
      * full speed in an arc with a large radius of curvature.
      *
      * Special cases:
      *  - Straight                        = 32768 or 32767
      *  - Turn in place clockwise         = 65535
      *  - Turn in place counter-clockwise = 1
      *
      * @param velocity   Velocity (-500 – 500 mm/s)
      *                   Average velocity of the drive wheels in
      *                   millimeters per second (mm/s).
      *
      * @param turnRadius Radius (-2000 – 2000 mm)
      *                   Radius in millimeters at which Create will
      *                   turn. The longer radii make Create drive
      *                   straighter, while the shorter radii make
      *                   Create turn more. The radius is measured
      *                   from the center of the turning circle to
      *                   the center of Create.
      */
     public void drive(int velocity, int turnRadius)
     {
         if (velocity < -500 || velocity > 500) {
             throw new IllegalArgumentException("Velocity out of range (-500 to 500)");
         }
 
         if (turnRadius < -2000 || turnRadius > 2000) {
             if (turnRadius != 32768 && turnRadius != 32767 && turnRadius != 65535)
             throw new IllegalArgumentException("Turn radius out of range (-2000 to 2000)");
         }
 
         this.send(
             Opcode.DRIVE,
             velocity & 0x0000FF00,   // Velocity high byte
             velocity & 0x000000FF,   // Velocity low byte
             turnRadius & 0x0000FF00, // Turn radius high byte
             turnRadius & 0x000000FF  // Turn radius low byte
         );
     }
 
     /**
      * This command lets you control the forward and backward
      * motion of Create’s drive wheels independently.
      *
      * @param velocityRight Velocity of the right wheel in
      *                      millimeters per second (mm/s)
      *                      (-500 – 500 mm/s).
      *
      * @param velocityLeft  Velocity of the left wheel in
      *                      millimeters per second (mm/s)
      *                      (-500 – 500 mm/s).
      */
     public void driveDirect(int velocityRight, int velocityLeft)
     {
         if (velocityLeft < -500 || velocityLeft > 500) {
             throw new IllegalArgumentException("Velocity left is out of range (-500 to 500)");
         }
 
         if (velocityRight < -500 || velocityRight > 500) {
             throw new IllegalArgumentException("Velocity right is out of range (-500 to 500)");
         }
 
         this.send(
             Opcode.DRIVE_DIRECT,
            (velocityRight & 0x0000FF00) >> 8, // Velocity right high byte
            velocityRight & 0x000000FF,        // Velocity right low byte
            (velocityLeft & 0x0000FF00) >> 8,  // Velocity left high byte
            velocityLeft & 0x000000FF          // Velocity left low byte
         );
     }
 
     /**
      * Send the given command byte and (optional) data byte(s).
      *
      * @param command Command byte
      * @param data    None, one or more data bytes
      */
     private void send(int command, int... data)
     {
         try {
             stream.write(command);
 
             for (int i = 0; i < data.length; i++) {
                 stream.write(data[i]);
             }
         } catch(IOException exception) {
         // TODO: Exception?
         }
     }
 }
