 /**
 * Master Time Keeper – The perfect graphical terminal schedule viewer
  * 
  * Copyright © 2012  Mattias Andrée (maandree@kth.se)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.kth.maandree.mastertimekeeper;
 
 import java.io.*;
 
 
 /**
  * Terminal utility class
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Terminal
 {
     /**
      * Hidden constructor
      */
     private Terminal()
     {
         //Nullify default constructor
     }
     
     
     
     /**
      * The stdout pipe file's canonical path.
      */
     private static String tty;
     
     /**
      * The stdout print stream.
      */
     private static PrintStream out;
     
     
     /**
      * Whether the cursor should be visible.
      */
     private static boolean cursorVisibile = true;
     
     /**
      * Whether the display should be in reverse video mode.
      */
     private static boolean reverseVideo = false;
     
     /**
      * Whether the terminal should be in X10 mouse reporting mode.
      */
     private static boolean x10Mouse = false;
     
     /**
      * Whether the terminal should be in X11 mouse reporting mode.
      */
     private static boolean x11Mouse = false;
     
     /**
      * Whether the echo flag is on
      */
     private static boolean echoFlag = true;
     
     /**
      * Whether the buffer flag is on
      */
     private static boolean bufferFlag = true;
     
     /**
      * Whether the signal flag is on
      */
     private static boolean signalFlag = true;
 
 
 
     /**
      * Class initialiser
      */
     static
     {
         String outtty = null;
         try
         {
             outtty = (new File("/dev/stdout")).getCanonicalPath();
         }
         catch (final Throwable err)
         {
             outtty = "/dev/null";
         }
         Terminal.tty = outtty;
         Terminal.out = System.out;
     }
 
 
 
     /**
      * Sets the cursor's visibility.
      *
      * @param  status  Whether the cursor should be visible. (Default = true)
      */
     public static void setCursorVisibility(final boolean status)
     {
         Terminal.out.print("\033[?25" + (status ? 'h' : 'l'));
         Terminal.cursorVisibile = status;
     }
 
     /**
      * Sets the display's reverse video status.
      *
      * @param  status  Whether the display should be in reverse video mode. (Default = false)
      */
     public static void setReverseVideoMode(final boolean status)
     {
         Terminal.out.print("\033[?5" + (status ? 'h' : 'l'));
         Terminal.reverseVideo = status;
     }
     
     /**
      * Sets the X10 mouse reporting status. (Obsolete)
      *
      * @param  status  Whether the terminal should be in X10 mouse reporting mode. (Default = false)
      */
     public static void setX10MouseReportMode(final boolean status)
     {
         Terminal.out.print("\033[?9" + (status ? 'h' : 'l'));
         Terminal.x10Mouse = status;
     }
     
     /**
      * Sets the X11 mouse reporting status.
      *
      * @param  status  Whether the terminal should be in X11 mouse reporting mode. (Default = false)
      */
     public static void setX11MouseReportMode(final boolean status)
     {
         Terminal.out.print("\033[?1000" + (status ? 'h' : 'l'));
         Terminal.x11Mouse = status;
     }
     
     
     
     /**
      * Sets the cursor's visibility.
      */
     public static void toggleCursorVisibility()
     {
         Terminal.cursorVisibile = !(Terminal.cursorVisibile);
         System.out.print("\033[?25" + (Terminal.cursorVisibile ? 'h' : 'l'));
     }
 
     /**
      * Sets the display's reverse video status.
      */
     public static void toggleReverseVideoMode()
     {
         Terminal.reverseVideo = !(Terminal.reverseVideo);
         System.out.print("\033[?5" + (Terminal.reverseVideo ? 'h' : 'l'));
     }
     
     /**
      * Sets the X10 mouse reporting status. (Obsolete)
      */
     public static void toggleX10MouseReportMode()
     {
         Terminal.x10Mouse = !(Terminal.x10Mouse);
         System.out.print("\033[?9" + (Terminal.x10Mouse ? 'h' : 'l'));
     }
     
     /**
      * Sets the X11 mouse reporting status.
      */
     public static void toggleX11MouseReportMode()
     {
         Terminal.x10Mouse = !(Terminal.x10Mouse);
         System.out.print("\033[?1000" + (Terminal.x11Mouse ? 'h' : 'l'));
     }
     
 
     /**
      * Gets the cursor's visibility.
      *
      * @return  Whether the cursor should be visible. (Default = true)
      */
     public static boolean getCursorVisibility()
     {
         return Terminal.cursorVisibile;
     }
 
     /**
      * Gets the display's reverse video status.
      *
      * @return  Whether the display should be in reverse video mode. (Default = false)
      */
     public static boolean getReverseVideoMode()
     {
         return Terminal.reverseVideo;
     }
     
     /**
      * Gets the X10 mouse reporting status. (Obsolete)
      *
      * @return  Whether the terminal should be in X10 mouse reporting mode. (Default = false)
      */
     public static boolean getX10MouseReportMode()
     {
         return Terminal.x10Mouse;
     }
     
     /**
      * Gets the X11 mouse reporting status.
      *
      * @return  Whether the terminal should be in X11 mouse reporting mode. (Default = false)
      */
     public static boolean getX11MouseReportMode()
     {
         return Terminal.x11Mouse;
     }
     
     
     /**
      * Gets the terminal ($TERM).<br/>
      * Known terminals:<br/>
      * "linux" ∋ Linux VT<br/>
      * "rxvt" ∋ Aterm<br/>
      * "xterm" ∋ Xterm, GNOME Terminal
      *
      * @return               The terminal
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static String getTerminal() throws IOException
     {
         final Process process = (new ProcessBuilder("/bin/sh", "-c", "echo $TERM < " + Terminal.tty)).start();
         String rcs = new String();
         final InputStream stream = process.getInputStream();
         int c;
         while (((c = stream.read()) != '\n') && (c != -1))
             rcs += (char)c;
 
         return rcs;
     }
 
     /**
      * Gets the terminal's width.
      *
      * @return               The terminal's width.
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static int getTerminalWidth() throws IOException
     {
         final Process process = (new ProcessBuilder("/bin/sh", "-c", "tput cols 2> " + Terminal.tty)).start();
         String rcs = new String();
         final InputStream stream = process.getInputStream();
         int c;
         while (((c = stream.read()) != '\n') && (c != -1))
             rcs += (char)c;
 
         return Integer.valueOf(rcs);
     }
 
     /**
      * Gets the terminal's height.
      *
      * @return               The terminal's height.
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static int getTerminalHeight() throws IOException
     {
         final Process process = (new ProcessBuilder("/bin/sh", "-c", "tput lines 2> " + Terminal.tty)).start();
         String rcs = new String();
         final InputStream stream = process.getInputStream();
         int c;
         while (((c = stream.read()) != '\n') && (c != -1))
             rcs += (char)c;
 
         return Integer.valueOf(rcs);
     }
 
     /**
      * Sets the value of the ECHO flag; iff on the input to the terminal will be echoed back while typing.
      * This can be used to hide what the user is type, for exempel when a password is requested.
      *
      * @param   on           Whether the flag should be on.
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static void setEchoFlag(final boolean on) throws IOException
     {
         Terminal.echoFlag = on;
         (new ProcessBuilder("/bin/sh", "-c", "stty " + (on ? "echo" : "-echo") + " < " + Terminal.tty + " > /dev/null")).start();
     }
 
     /**
      * Sets the value of the ICANON flag; iff on the input will wait to be sent onto a line feed is given.
      * This can be used to read single characters instead of lines.
      *
      * @param   on           Whether the flag should be on.
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static void setBufferFlag(final boolean on) throws IOException
     {
         Terminal.bufferFlag = on;
         (new ProcessBuilder("/bin/sh", "-c", "stty " + (on ? "icanon" : "-icanon") + " < " + Terminal.tty + " > /dev/null")).start();
     }
 
     /**
      * Sets the value of the ISIG flag; iff on the input can be overriden by signals.
      * This can be used to allow keystokes such as ^C and ^\.
      *
      * @param   on           Whether the flag should be on.
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static void setSignalFlag(final boolean on) throws IOException
     {
         Terminal.signalFlag = on;
         (new ProcessBuilder("/bin/sh", "-c", "stty " + (on ? "isig" : "-isig") + " < " + Terminal.tty + " > /dev/null")).start();
     }
 
     /**
      * Toggles the value of the ECHO flag; iff on the input to the terminal will be echoed back while typing.
      * This can be used to hide what the user is type, for exempel when a password is requested.
      *
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static void toggleEchoFlag() throws IOException
     {
         Terminal.echoFlag = Terminal.echoFlag == false;
         (new ProcessBuilder("/bin/sh", "-c", "stty " + (Terminal.echoFlag ? "echo" : "-echo") + " < " + Terminal.tty + " > /dev/null")).start();
     }
 
     /**
      * Toggles the value of the ICANON flag; iff on the input will wait to be sent onto a line feed is given.
      * This can be used to read single characters instead of lines.
      *
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static void toggleBufferFlag() throws IOException
     {
         Terminal.bufferFlag = Terminal.bufferFlag == false;
         (new ProcessBuilder("/bin/sh", "-c", "stty " + (Terminal.bufferFlag ? "icanon" : "-icanon") + " < " + Terminal.tty + " > /dev/null")).start();
     }
 
     /**
      * Toggles the value of the ISIG flag; iff on the input can be overriden by signals.
      * This can be used to allow keystokes such as ^C and ^\.
      *
      * @throws  IOException  Should not be thrown in GNU.
      */
     public static void toggleSignalFlag() throws IOException
     {
         Terminal.signalFlag = Terminal.signalFlag == false;
         (new ProcessBuilder("/bin/sh", "-c", "stty " + (Terminal.signalFlag ? "isig" : "-isig") + " < " + Terminal.tty + " > /dev/null")).start();
     }
 
     /**
      * Gets the value of the ECHO flag; iff on the input to the terminal will be echoed back while typing.
      * This can be used to hide what the user is type, for exempel when a password is requested.
      *
      * @return  Whether the flag should be on.
      */
     public static boolean getEchoFlag()
     {
         return Terminal.echoFlag;
     }
 
     /**
      * Gets the value of the ICANON flag; iff on the input will wait to be sent onto a line feed is given.
      * This can be used to read single characters instead of lines.
      *
      * @return  Whether the flag is on.
      */
     public static boolean getBufferFlag()
     {
         return Terminal.bufferFlag;
     }
 
     /**
      * Gets the value of the ISIG flag; iff on the input can be overriden by signals.
      * This can be used to allow keystokes such as ^C and ^\.
      *
      * @return  Whether the flag is on.
      */
     public static boolean getSignalFlag()
     {
         return Terminal.signalFlag;
     }
     
 }
