 /*
  * Copyright 2012-2013 James Geboski <jgeboski@gmail.com>
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
 
 package org.jgeboski.vindicator.util;
 
 import java.net.InetAddress;
 import java.net.Inet4Address;
 import java.net.Inet6Address;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 
 public class StrUtils
 {
     public static boolean isAddress(String str)
     {
         return (getAddress(str) != null);
     }
 
     public static boolean isAddress4(String str)
     {
         return (getAddress(str) instanceof Inet4Address);
     }
 
     public static boolean isAddress6(String str)
     {
         return (getAddress(str) instanceof Inet6Address);
     }
 
     public static boolean isMinecraftName(String str)
     {
         return str.matches("\\w{2,16}");
     }
 
     public static String join(String[] strs, String glue, int start, int end)
     {
         String ret;
         int    i;
 
         if (start >= strs.length)
             return null;
 
         if (end > strs.length)
             end = strs.length;
 
         ret = strs[start];
 
         for (i = (start + 1); i < end; i++)
             ret += glue + strs[i];
 
         return ret;
     }
 
     public static String join(String[] strs, String glue, int start)
     {
         return join(strs, glue, start, strs.length);
     }
 
     public static String join(String[] strs, String glue)
     {
         return join(strs, glue, 0, strs.length);
     }
 
     public static long toSeconds(String str)
     {
         String sstr;
         char[] cstr;
 
         long time;
         long mod;
 
         int i;
         int s;
         int e;
 
         cstr = str.toLowerCase().toCharArray();
         time = 0;
 
         for (i = s = e = 0; i < cstr.length; i++) {
             if (Character.isDigit(cstr[i])) {
                 if (s == 0)
                     s = i;
 
                 e = i;
                 continue;
             }
 
             e++;
 
             if ((i != e) || (e > cstr.length))
                 continue;
 
             switch (cstr[e]) {
             case 'm':
                 if (((e + 1) < cstr.length) && (cstr[e + 1] == 'o'))
                     mod = 2592000;
                 else
                     mod = 60;
                 break;
 
             case 's': mod = 1;        break;
             case 'h': mod = 3600;     break;
             case 'd': mod = 86400;    break;
             case 'w': mod = 604800;   break;
             case 'y': mod = 31536000; break;
             default:  mod = 1;
             }
 
             sstr  = new String(Arrays.copyOfRange(cstr, s, e));
             s = e = 0;
 
             try {
                 time += Integer.parseInt(sstr) * mod;
             } catch (NumberFormatException ex) {
                 continue;
             }
         }
 
         return time;
     }
 
     private static InetAddress getAddress(String str)
     {
        InetAddress ia;

         try {
            ia = InetAddress.getByName(str);
         } catch (UnknownHostException e) {
             return null;
         }

        return str.equals(ia.getHostAddress()) ? ia : null;
     }
 }
