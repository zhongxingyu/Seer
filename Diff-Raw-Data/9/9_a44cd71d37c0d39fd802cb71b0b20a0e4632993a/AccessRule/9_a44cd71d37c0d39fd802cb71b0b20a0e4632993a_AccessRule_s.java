 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.util.StringTokenizer;
 import org.apache.oro.text.regex.Perl5Matcher;
 import org.apache.oro.text.regex.Perl5Pattern;
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.SimplePatternParser;
 
 /**
  * Access rule. This class can take a character string to produce an
  * {@link AccessRule} object from it.
  *
  * <h3>Descriptor format</h3>
  *
  * <p>A descriptor must comply to the following format:
  * <ul>
  *    <li>start with either <code>"allow"</code> or <code>"deny"</code>;
  *    <li>followed by any number of white space characters;
  *    <li>followed by a valid IP address;
  *    <li>followed by a slash character (<code>'/'</code>);
  *    <li>followed by a mask between 0 and 32 in decimal format, no leading zeroes;
  *    <li>followed by any number of white space characters;
  *    <li>followed by a simple pattern, see class {@link SimplePatternParser}.
  * </ul>
  *
  * <h3>Descriptor examples</h3>
  *
  * <p>Example of access rule descriptors:
  *
  * <dl>
  *    <dt><code>"allow&nbsp;194.134.168.213/32 *"</code></dt>
  *    <dd>Allows 194.134.168.213 to access any function.</dd>
  *
  *    <dt><code>"deny&nbsp;&nbsp;194.134.168.213/24\t_*"</code></dt>
  *    <dd>Denies all 194.134.168.x IP addresses to access any function
  *        starting with an underscore (<code>'_'</code>).</dd>
  * </dl>
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  * @author Chris Gilbride (<a href="mailto:chris.gilbride@nl.wanadoo.com">chris.gilbride@nl.wanadoo.com</a>)
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public final class AccessRule implements AccessRuleContainer {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Parses the specified character string to construct a new
     * <code>AccessRule</code> object.
     *
     * @param descriptor
     *    the access rule descriptor, the character string to parse, cannot be <code>null</code>.
     *    It also cannot be empty <code>(" ")</code>.
     *
     * @return
     *    an {@link AccessRule} instance, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>descriptor == null</code>.
     *
     * @throws ParseException
     *    If there was a parsing error.
     */
    public static AccessRule parseAccessRule(String descriptor)
    throws IllegalArgumentException, ParseException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("descriptor", descriptor);
 
       StringTokenizer tokenizer = new StringTokenizer(descriptor," \t\n\r");
 
       // Determine if it is an 'allow' or a 'deny' rule
       boolean allow;
       String token = nextToken(descriptor, tokenizer);
       if ("allow".equals(token)) {
          allow = true;
       } else if ("deny".equals(token)) {
          allow = false;
       } else {
          throw new ParseException("First token of descriptor is \"" + token + "\", instead of either 'allow' or 'deny'.");
       }
       FastStringBuffer asString = new FastStringBuffer(70, token);
 
       // Determine the IP address to be checked
       token = nextToken(descriptor, tokenizer);
       IPFilter filter = IPFilter.parseIPFilter(token);
       asString.append(' ');
       asString.append(filter.toString());
 
       // Determine the function the access is to be checked for
       token = nextToken(descriptor, tokenizer);
       Perl5Pattern pattern = new SimplePatternParser().parseSimplePattern(token);
       asString.append(' ');
       asString.append(token);
 
       return new AccessRule(allow, filter, pattern, asString.toString());
    }
 
    /**
    * Returns the next token in the descriptor
     *
     * @param descriptor
     *   the original descriptor, for use in the {@link ParseException}, if
     *   necessary.
     *
     * @param tokenizer
     *   the {@link StringTokenizer} to retrieve the next token from.
     *
     * @return
     *   the next token, never <code>null</code>.
     *
     * @throws ParseException
     *   if <code>tokenizer.</code>{@link StringTokenizer#hasMoreTokens() hasMoreTokens}()<code> == false</code>.
     */
    private static String nextToken(String descriptor, StringTokenizer tokenizer)
    throws ParseException {
 
       if (!tokenizer.hasMoreTokens()) {
          throw new ParseException("The string \"" + descriptor + "\" is invalid as an access rule descriptor. Too few tokens retrieved from the descriptor.");
       } else {
          return tokenizer.nextToken();
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AccessRule</code>.
     *
     * @param allow
     *    flag that indicates if this rule grants access (<code>true</code>) or
     *    denies access (<code>false</code>).
     *
     * @param ipFilter
     *    filter used for matching (or not) IP addresses, cannot be
     *    <code>null</code>.
     *
     * @param asString
     *    textual presentation of this access rule, cannot be
     *    <code>null</code>.
     *
     * @param functionNamePattern
     *    regular expression used for matching (or not) a function name; cannot
     *    be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>ipFilter            == null
     *          || functionNamePattern == null
     *          || asString            == null</code>.
     */
    private AccessRule(boolean      allow,
                       IPFilter     ipFilter,
                       Perl5Pattern functionNamePattern,
                       String       asString)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("ipFilter",            ipFilter,
                                      "functionNamePattern", functionNamePattern,
                                      "asString",            asString);
 
       // Store the data
       _allow               = allow ? Boolean.TRUE : Boolean.FALSE;
       _ipFilter            = ipFilter;
       _functionNamePattern = functionNamePattern;
       _asString            = asString;
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * If the access method is 'allow' or not.
     */
    private final Boolean _allow;
 
    /**
     * The IP address filter used to create the access rule. Cannot be
     * <code>null</code>.
     */
    private final IPFilter _ipFilter;
 
    /**
     * The function name pattern. Cannot be <code>null</code>.
     */
    private final Perl5Pattern _functionNamePattern;
 
    /**
     * String representation of this object. Cannot be <code>null</code>.
     */
    private final String _asString;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns if this rule is an <em>allow</em> or a <em>deny</em> rule.
     *
     * @return
     *    <code>true</code> if this is an <em>allow</em> rule, or
     *    <code>false</code> if this is a <em>deny</em> rule.
     */
    public boolean isAllowRule() {
       return _allow.booleanValue();
    }
 
    /**
     * Returns the IP filter.
     *
     * @return
     *    the IP filter, cannot be <code>null</code>.
     */
    public IPFilter getIPFilter() {
       return _ipFilter;
    }
 
    /**
     * Determines if the specified IP address and function match this rule.
     *
     * @param ip
     *    the IP address to match, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function to match, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if this rule matches, <code>false</code> otherwise.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null || functionName == null</code>.
     *
     * @throws ParseException
     *    if the specified IP address cannot be parsed.
     */
    public boolean match(String ip, String functionName)
    throws IllegalArgumentException, ParseException {
       return isAllowed(ip, functionName) != null;
    }
 
    /**
     * Determines if the specified IP address is allowed to access the
     * specified function, returning a <code>Boolean</code> object or
     * <code>null</code>.
     *
     * <p>This method finds the first matching rule and then returns the
     * <em>allow</em> property of that rule (see
     * {@link AccessRule#isAllowRule()}). If there is no matching rule, then
     * <code>null</code> is returned.
     *
     * @param ip
     *    the IP address, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @return
     *    {@link Boolean#TRUE} if the specified IP address is allowed to access
     *    the specified function, {@link Boolean#FALSE} if it is disallowed
     *    access or <code>null</code> if no match is found.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null || functionName == null</code>.
     *
     * @throws ParseException
     *    if the specified IP address is malformed.
     */
    public Boolean isAllowed(String ip, String functionName)
    throws IllegalArgumentException, ParseException {
 
       // TODO: If disposed, then throw a ProgrammingError
 
       // Check preconditions
       MandatoryArgumentChecker.check("ip", ip, "functionName", functionName);
 
       Perl5Matcher patternMatcher = new Perl5Matcher();
       if (!_ipFilter.match(ip) || !patternMatcher.matches(functionName, _functionNamePattern)) {
          return null;
       } else {
          return _allow;
       }
    }
 
    /**
     * Disposes this access rule. All claimed resources are freed as much as
     * possible.
     *
     * <p>Once disposed, the {@link #isAllowed} method should no longer be
     * called.
     *
     * @deprecated
     *    Deprecated since XINS 1.3.0. This method is unused and the
     *    implementation is empty.
     */
    public void dispose() {
       // empty
    }
 
    /**
     * Returns a character string representation of this object. The returned
     * string is in the form:
     *
     * <blockquote><em>type a.b.c.d/m pattern</em></blockquote>
     *
     * where <em>type</em> is either <code>"allow"</code> or
     * <code>"deny"</code>, <em>a.b.c.d</em> is the base IP address, <em>m</em>
     * is the mask, and <em>pattern</em> is the function name simple pattern.
     *
     * @return
     *    a character string representation of this access rule, never
     *    <code>null</code>.
     */
    public String toString() {
       return _asString;
    }
 }
