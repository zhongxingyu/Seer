 /**
  * Unisay — cowsay+ponysay rewritten in Java, with added features and full Unicode(!) support
  *
  * Copyright © 2012  Mattias Andrée (maandree@kth.se)
  *
 * This library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
 * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.kth.maandree.unisay;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * Utility class
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class Util
 {
     /**
      * Non-constructor
      */
     private Util()
     {
 	assert false : "This class [Util] is not meant to be instansiated.";
     }
     
     
     
     /**
      * Checks whether the a string matches, exactly, any other string
      *
      * @param  matchee  The string to try to match with any other string
      * @param  matches  The allowed matches for <code>matchee</code>
      */
     public static boolean equalsAny(final String matchee, final String... matches)
     {
 	for (final String match : matches)
 	    if (matchee.equals(match))
 		return true;
 	
 	return false;
     }
     
     /**
      * Gets a system property
      *
      * @param   property  The property name
      * @return            The property value
      */
     public static String getProperty(final String property)
     {
 	try
 	{
 	    final Process process = (new ProcessBuilder("/bin/sh", "-c", "echo $" + property)).start();
 	    String rcs = new String();
 	    final InputStream stream = process.getInputStream();
 	    int c;
 	    while (((c = stream.read()) != '\n') && (c != -1))
 		rcs += (char)c;
 	    try
 	    {
 		stream.close();
 	    }
 	    catch (final Throwable err)
 	    {
 		//Ignore
 	    }
 	    return rcs;
 	}
 	catch (final Throwable err)
 	{
 	    return new String();
 	}
     }
     
     /**
      * Gets the width of the terminal
      *
      * @return  The width of the terminal
      */
     public static int getWidth()
     {
 	try
 	{
 	    final Process process = (new ProcessBuilder("/bin/sh", "-c", "tput cols 2> " + (new File("/dev/stderr")).getCanonicalPath())).start();
 	    String rcs = new String();
 	    final InputStream stream = process.getInputStream();
 	    int c;
 	    while (((c = stream.read()) != '\n') && (c != -1))
 		rcs += (char)c;
 	    try
 	    {
 		stream.close();
 	    }
 	    catch (final Throwable err)
 	    {
 		//Ignore
 	    }
 	    return Integer.parseInt(rcs);
 	}
 	catch (final Throwable err)
 	{
 	    return 0;
 	}
     }
     
     /**
      * Converts an <code>int</code> representation of a character to an UTF-8 <code>byte[]</code> representation
      *
      * @param   character  The <code>int</code> representation of the character
      * @return             The <code>byte[]</code> representation of the character
      */
     public static byte[] toBytes(final int character)
     {
 	return toBytes(new int[] { character });
     }
     
     /**
      * Converts an <code>int[]</code> representation of a string to an UTF-8 <code>byte[]</code> representation
      *
      * @param   string  The <code>int[]</code> representation of the string
      * @return          The <code>byte[]</code> representation of the string
      */
     public static byte[] toBytes(final int[] string)
     {
 	//7:  0xxxyyyy
 	//11: 110xyyyy 10xxyyyy
 	//16: 1110xxxx 10xxyyyy 10xxyyyy
 	//21: 11110xxx 10xxyyyy 10xxyyyy 10xxyyyy
 	//26: 111110xx 10xxyyyy 10xxyyyy 10xxyyyy 10xxyyyy
 	//31: 1111110x 10xxyyyy 10xxyyyy 10xxyyyy 10xxyyyy 10xxyyyy
 	
 	final ArrayList<byte[]> chars = new ArrayList<byte[]>();
 	int length = 0;
 	
 	final byte[] buf = new byte[6];
 	for (final int c : string)
 	    if (c < 0x80)
 	    {
 		chars.add(new byte[] { (byte)c });
 		length++;
 	    }
 	    else
 	    {
 		int i = 0;
 		int b = c;
 		while (b >= 0x40)
 		{
 		    buf[i++] = (byte)((b & 0x3F) | 0x80);
 		    b >>>= 6;
 		}
 		buf[i++] = (byte)b;
 		byte[] arr = new byte[i];
 		for (int j = 0; j < i; j++)
 		    arr[j] = buf[i - j - 1];
 		arr[0] |= (byte)((0xFF << (8 - i)) & 0xFF);
 		chars.add(arr);
 		length += i;
 	    }
 	
 	final byte[] rc = new byte[length];
 	int ptr = 0;
 	for (final byte[] c : chars)
 	{
 	    System.arraycopy(c, 0, rc, ptr, c.length);
 	    ptr += c.length;
 	}
 	
 	return rc;
     }
     
     
     
     /**
      * Tests 'toBytes()'
      * 
      * @param  args  Lines to pritns
      * 
      * @throws  IOException  On I/O exception
      */
     public static void main(final String... args) throws IOException
     {
 	for (final String arg : args)
         {
 	    final int[] ints = new int[arg.length()];
 	    for (int i = 0, n = ints.length; i < n; i++)
 		ints[i] = arg.charAt(i);
 	    System.out.write(toBytes(ints));
 	    System.out.println();
 	}
     }
     
 }
 
