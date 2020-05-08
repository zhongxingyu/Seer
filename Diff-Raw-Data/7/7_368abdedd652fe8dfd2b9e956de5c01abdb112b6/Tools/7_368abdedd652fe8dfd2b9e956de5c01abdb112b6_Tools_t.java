 /*
 * JBoss, a division of Red Hat
 * Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 
 package org.picketlink.idm.impl.helper;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerFactory;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Enumeration;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * @author <a href="mailto:boleslaw.dawidowicz at redhat.com">Boleslaw Dawidowicz</a>
  * @version : 0.1 $
  */
 public class Tools
 {
    private static final String DN_REGEX = "([^=,\\\\]*(\\\\.)?)+";
 
    private static Logger log = Logger.getLogger(Tools.class.getName());
 
    private static MBeanServer instance = null;
 
    public static <E> List<E> toList(Enumeration<E> e)
    {
       if (e == null)
       {
          throw new IllegalArgumentException();
       }
       List<E> list = new ArrayList<E>();
       while (e.hasMoreElements())
       {
          list.add(e.nextElement());
       }
       return list;
    }
 
    public static String wildcardToRegex(String wildcard){
        StringBuffer s = new StringBuffer(wildcard.length());
        s.append('^');
        for (int i = 0, is = wildcard.length(); i < is; i++) {
            char c = wildcard.charAt(i);
            switch(c) {
                case '*':
                    s.append(".*");
                    break;
 //               case '?':
 //                   s.append(".");
 //                   break;
                    // escape special regexp-characters
                case '(': case ')': case '[': case ']': case '$':
                case '^': case '.': case '{': case '}': case '|':
                case '\\':
                    s.append("\\");
                    s.append(c);
                    break;
                default:
                    s.append(c);
                    break;
            }
        }
        s.append('$');
        return(s.toString());
    }
 
    /**
     * Process dn and retrieves a part from it:
     * uid=xxx,dc=example,dc=org - retrieves xxx
     *
     * @param dn
     * @return
     */
    public static String stripDnToName(String dn)
    {
       if (dn == null || dn.length() == 0)
       {
          throw new IllegalArgumentException("Cannot process empty dn");
       }
       String name = null;
 
       String[] parts = dn.split(",");
 
       parts = parts[0].split("=");
       if (parts.length != 2)
       {
          throw new IllegalArgumentException("Wrong dn format: " + dn);
       }
 
       return parts[1];
    }
 
    public static String getOptionSingleValue(String optionName, Map<String, List<String>> options)
    {
       if (options == null || options.size() == 0)
       {
          return null;
       }
 
       List<String> values = options.get(optionName);
 
       if (values != null && values.size() > 0)
       {
          return values.get(0);
       }
 
       return null;
    }
 
 
    /**
     * Escape string for LDAP search filter use according to RFC 2554
     *
     *       Character       ASCII value
     *       ---------------------------
     *       *               0x2a
     *       (               0x28
     *       )               0x29
     *       \               0x5c
     *       NUL             0x00
     * 
     * @param filter
     * @return
     */
    public static final String escapeLDAPSearchFilter(String filter)
    {
       StringBuilder sb = new StringBuilder();
       for (int i = 0; i < filter.length(); i++) {
          char curChar = filter.charAt(i);
          switch (curChar) {
             case '\\':
                sb.append("\\5c");
                break;
             case '*':
                sb.append("\\2a");
                break;
             case '(':
                sb.append("\\28");
                break;
             case ')':
                sb.append("\\29");
                break;
             case '\u0000':
                sb.append("\\00");
                break;
             default:
                sb.append(curChar);
          }
       }
       return sb.toString();
    }
 
    public static void logMethodIn(Logger log, Level level, String methodName, Object[] args)
    {
       try
       {
          StringBuilder sb = new StringBuilder();
          sb.append("Method '")
             .append(methodName)
             .append("' called with arguments: ");
 
          if (args != null)
          {
             for (Object arg : args)
             {
                if (arg != null && arg instanceof Object[])
                {
                   sb.append(Arrays.toString((Object[])arg))
                      .append("; ");
                }
                else
                {
                   sb.append(arg)
                      .append("; ");
                }
             }
          }
          log.log(level, sb.toString());
       }
       catch (Throwable t)
       {
          log.log(level, "Error in logging code block (not related to application code): ", t);
       }
 
    }
 
    public static void logMethodOut(Logger log, Level level, String methodName, Object result)
    {
       try
       {
          StringBuilder sb = new StringBuilder();
          sb.append("Method '")
             .append(methodName)
             .append("' returning object: ");
 
          if (result != null && result instanceof Collection)
          {
             sb.append("Collection of size: ").append(((Collection)result).size());
          }
          else
          {
             if (result != null)
             {
                sb.append("[").append(result.getClass().getCanonicalName()).append("]");
             }
             sb.append(result);
          }
 
          log.log(level, sb.toString());
 
       }
       catch (Throwable t)
       {
          log.log(level, "Error in logging code block (not related to application code): ", t);
       }
    }
 
    public static MBeanServer locateJBoss()
    {
       synchronized (Tools.class)
       {
          if (instance != null)
          {
             return instance;
          }
       }
       for (Iterator i = MBeanServerFactory.findMBeanServer(null).iterator(); i.hasNext(); )
       {
          MBeanServer server = (MBeanServer) i.next();
          if (server.getDefaultDomain().equals("jboss"))
          {
             return server;
          }
       }
 
 
       throw new IllegalStateException("No 'jboss' MBeanServer found!");
    }
 
    /**
     * @param dn1
     * @param dn2
     * @return true if first DN ends with second Ldap DN. It will ignore whitespaces in the path. See {@link #dnFormatWhitespaces}
     */
    public static boolean dnEndsWith(String dn1, String dn2)
    {
       String dn1Formatted = dnFormatWhitespaces(dn1);
       String dn2Formatted = dnFormatWhitespaces(dn2);
 
       return dn1Formatted.endsWith(dn2Formatted);
    }
 
    /**
     * @param dn1
     * @param dn2
     * @return true if first DN equals second Ldap DN. It will ignore whitespaces in the path. See {@link #dnFormatWhitespaces}
     */
    public static boolean dnEquals(String dn1, String dn2)
    {
       String dn1Formatted = dnFormatWhitespaces(dn1);
       String dn2Formatted = dnFormatWhitespaces(dn2);
 
       return dn1Formatted.equals(dn2Formatted);
    }
 
    /**
     * Format whitespaces in DN records path. It won't affect whitespaces inside some record, but it will affect
    * whitespaces at the beginning or at the end of single path argument. It also lowercase all letters.
     *
     * Examples:
    * input="uid=root, ou=Organization, o=gatein,dc=example,dc=com " , output="uid=root,ou=organization,o=gatein,dc=example,dc=com"
    * input="uid=root, ou=My Big Organization Unit,o=gatein org,dc= example ,dc=com " , output="uid=root,ou=my big organization unit,o=gatein org,dc=example,dc=com"
     *
     * @param inputDn
     * @return formatted inputDn
     */
    public static String dnFormatWhitespaces(String inputDn)
    {
       String inputlc = inputDn.toLowerCase();
 
       StringBuilder result = new StringBuilder();
       int last = 0;
 
       Pattern pattern = Pattern.compile(DN_REGEX);
       Matcher m = pattern.matcher(inputlc);
       while (m.find())
       {
          if (m.group().length() == 0)
          {
             continue;
          }
 
          last++;
          if (last > 1)
          {
             result.append(last%2 == 0 ? '=' : ',');
          }
          result.append(m.group().trim());
       }
 
       if (log.isLoggable(Level.FINER))
       {
          log.log(Level.FINER, "Input to format=\"" + inputDn + "\", Output from format=\"" + result.toString() + "\"");
       }
 
       return result.toString();
    }
 
 
 }
