 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.util.HashSet;
 
 import org.apache.oro.text.regex.MalformedPatternException;
 import org.apache.oro.text.regex.Pattern;
 import org.apache.oro.text.regex.Perl5Compiler;
 import org.apache.oro.text.regex.Perl5Matcher;
 
 import org.xins.common.Utils;
 
 /**
  * Meta information about a calling convention, describing some of its
  * characteristics. Currently only contains the supported HTTP methods.
  *
  * <p>When a calling convention implementation provides a
 * <code>CallingConventionInfo</code> object to the XINS framework, then the
  * framework will make it unmodifiable.
  *
  * <p>This class is thread-safe.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  */
 public final class CallingConventionInfo extends Object {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME;
 
    /**
     * The pattern object that valid HTTP method names match and invalid ones 
     * do not.
     */
    private static final Pattern HTTP_METHOD_PATTERN;
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Initializes all class variables.
     */
    static {
 
       CLASSNAME = CallingConventionInfo.class.getName();
 
       String thisMethod = "<clinit>()";
       String pattern = "[A-Z0-9_-]+";
       Perl5Compiler compiler = new Perl5Compiler();
       try {
          HTTP_METHOD_PATTERN = compiler.compile(pattern,
                                                 Perl5Compiler.READ_ONLY_MASK);
 
       } catch (MalformedPatternException exception) {
          String subjectClass  = compiler.getClass().getName();
          String subjectMethod = "compile(java.lang.String,int)";
          String detail        = "The pattern \""
                               + pattern
                               + "\" is considered malformed.";
 
          throw Utils.logProgrammingError(CLASSNAME,    thisMethod,
                                          subjectClass, subjectMethod,
                                          detail,       exception);
       }
    }
 
    //------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CallingConventionInfo</code> object.
     */
    public CallingConventionInfo() {
       _lock             = new Object();
       _unmodifiable     = false;
       _supportedMethods = new HashSet();
       _matcher          = new Perl5Matcher();
    }
 
 
    //------------------------------------------------------------------------
    // Fields
    //------------------------------------------------------------------------
 
    /**
     * The lock object. Needs to be synchronized on before
     * {@link #_unmodifiable} can be read or written.
     */
    private final Object _lock;
 
    /**
     * Flag that indicates whether this instance has been locked to avoid 
     * further modifications. Initially <code>false</code>.
     *
     * <p>Synchronize on {@link #_lock} before accessing this field.
     */
    private boolean _unmodifiable;
 
    /**
     * The set of supported HTTP methods. Never <code>null</code>.
     */
    private final HashSet _supportedMethods;
 
    /**
     * The pattern matcher used. Never <code>null</code>.
     */
    private final Perl5Matcher _matcher;
 
 
    //------------------------------------------------------------------------
    // Methods
    //------------------------------------------------------------------------
 
    /**
     * Adds the specified HTTP method as a method supported for invoking
     * functions.
     *
     * <p>Note that the <em>OPTIONS</em> method can never be used to invoke
     * a function. Instead, it is used to determine which HTTP methods a
     * server supports for a specific resource.
     *
     * @param method
     *    the supported HTTP method, must be a valid HTTP method name,
     *    cannot be <code>null</code> or empty and cannot be
     *    <code>"OPTIONS"</code>.
     *
     * @throws IllegalStateException
     *    if this <code>CallingConventionInfo</code> instance has already
     *    been locked and cannot be modified anymore.
     *
     * @throws IllegalArgumentException
     *    if the specified method is a duplicate of a method already
     *    registered with this object, or if it is invalid in some other
     *    way.
     */
    public void addSupportedMethod(String method)
    throws IllegalStateException, IllegalArgumentException {
 
       synchronized (_lock) {
 
          // Check whether this object is modifiable
          if (_unmodifiable) {
             throw new IllegalStateException("No longer modifiable.");
 
          // Check whether the argument is null
          } else if (method == null) {
             throw new IllegalArgumentException("method == null");
          }
 
          String upper = method.toUpperCase();
 
          // Check whether the argument matches the pattern
          if (! _matcher.matches(upper, HTTP_METHOD_PATTERN)) {
             throw new IllegalArgumentException("The specified method \"" + method + "\" is considered invalid.");
 
          // Disallow the OPTIONS method
          } else if ("OPTIONS".equals(upper)) {
             throw new IllegalArgumentException("The \"OPTIONS\" method cannot be used for invoking functions.");
 
          // Check for duplicates
          } else if (_supportedMethods.contains(upper)) {
             throw new IllegalArgumentException("The \"" + method + "\" method is already registered as a supported method.");
 
          // Indeed add the method as a supported method
          } else {
             _supportedMethods.add(upper);
          }
       }
    }
 }
