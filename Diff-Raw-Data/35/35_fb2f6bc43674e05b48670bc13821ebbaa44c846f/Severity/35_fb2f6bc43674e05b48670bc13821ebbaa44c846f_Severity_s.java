 /*
  * $Id: Severity.java 1336 2009-03-28 22:04:07Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.phoenix.report;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Enumerated type of a severity.
  * 
  * <p>The severity of a finding shall reflect the urgency of a 
  * fix needed. Higher severity levels require more urgent actions
  * where lower levels might denote findings that are for information
  * only.</p> 
  *
  * <p>This class also defines the weight of a finding 
  * ({@link #getPenalty()} and thus 
  * how much the quality is affected by the finding of
  * this severity.</p>
  *    
  * <p>Instances of this class are immutable.</p>
  *
  * <p>The following severities are defined:
  * <ol>
  *    <li>{@link #CPD}</li>
  *    <li>{@link #WARNING}</li>
  *    <li>{@link #DESIGN}</li>
  *    <li>{@link #CODE_STYLE}</li>
  *    <li>{@link #INFO}</li>
  *    <li>{@link #COVERAGE}</li>
  *    <li>{@link #FILTERED}</li>
  * </ol>
  * </p>
  * 
  * @author Andreas Mandel
  */
 public final class Severity
        implements Serializable, Comparable
 {
     /**
      * Scale of the penalty points. One penalty point marks  
      * <code>1 / PENALTY_SCALE</code> lines as bad.  
      */
     public static final int PENALTY_SCALE = 10;
 
 
     private static final long serialVersionUID = 2L;
 
     /** Ordinal of next severity to be created. */
     private static int sNextOrdinal = 0;
 
     /** Assign a ordinal to this severity. */
     private final int mOrdinal = sNextOrdinal++;
 
     /** 
      * The penalty for violations of this severity level.
      */
     private final transient int mPenalty;
 
     /** The name of the severity */
     private final transient String mName;
 
     /** Maps a string representation to an enumerated value. */
    private static final Map FROM_STRING = new HashMap();
 
     /** 
      * Severity for filtered findings.
      * <p>Findings that are not appropriate for whatever reason 
      * (ex. being a false positive) should get this level assigned by 
      * the filtering style sheet.</p>  
      * <p>A finding of this severity marks no lines of code as bad.</p>
      */
     public static final Severity FILTERED = new Severity("filtered", 0);
 
     /** 
      * Severity level used to denote no finding.
      */
     public static final Severity OK = new Severity("ok", 0);
 
     /** 
      * Severity for informational finders like to-do markers or code
      * that uses outdated API which should be updated but has no side
      * effect. 
      * <p>A finding of this severity marks no lines of code as bad.</p>
      */
     public static final Severity INFO = new Severity("info", 0);
 
     /** 
      * Severity for code-style type of findings.
      * <p>All finders that report indentation, position of braces or this
      * kind of violation should use this level. There is no deeper 
      * differentiation for this type of findings.</p>  
      * <p>A finding of this severity marks 0.5 lines of code as bad.</p>
      */
     public static final Severity CODE_STYLE = new Severity("code-style", 5);
 
     /** 
      * Severity for code that is not covered by test cases.
      * <p>If code coverage is enabled each line with a coverage of 0 
      * gets marked with this violation.</p> 
      * <p>A finding of this severity marks 0.8 lines of code as bad.</p>
      */
     public static final Severity COVERAGE = new Severity("coverage", 8);
 
     /** 
      * This severity level is for design related findings.
      * <p>Inheritance problems or broken implementation of standard methods
      * should get this severity level, unless {@link #ERROR} or 
      * {@link #WARNING} fits better.</p>  
      * <p>A finding of this severity marks three lines of code as bad.</p>
      */
     public static final Severity DESIGN = new Severity("design", 30);
 
     /** 
      * Warning level severities.
      * <p>A finding of this severity marks 5 lines of code as bad.</p>
      */
     public static final Severity WARNING = new Severity("warning", 50);
 
     /** 
      * Detected copied &amp; pasted code.
      * <p>The copy and paste detector detected a number of similar lines 
      * at a different position. This should be refactored immediately.</p>
      * <p>A finding of this severity marks 10 lines of code as bad.</p>
      */
     public static final Severity CPD = new Severity("cpd", 100);
 
     /**
      * Severe findings requiring immediate action.
      * <p>Findings of this severity are serious errors. Finders that detect 
      * style or design violations or finders that produce a certain number 
      * of false positives must not use this severity.</p>
      * <p>Findings of this severity should prevent a project from being 
      * released. A severity of this level marks 10 lines as bad.</p>
      */
     public static final Severity ERROR = new Severity("error", 100);
 
     /** 
      * The maximum possible severity. 
      * Is {@link #ERROR}.
      */
     public static final Severity MAX_SEVERITY = ERROR;
 
     /** The maximum possible severity as int. */
     public static final int MAX_SEVERITY_INT = MAX_SEVERITY.toInt();
 
     /** Internal list of all available severities. */
     private static final Severity[] PRIVATE_VALUES =
     {
         Severity.FILTERED,
         Severity.OK,
         Severity.COVERAGE,
         Severity.INFO,
         Severity.CODE_STYLE,
         Severity.DESIGN,
         Severity.WARNING,
         Severity.CPD,
         Severity.ERROR
     };
 
     /** Immutable list of the severities. */
    public static final List VALUES =
         Collections.unmodifiableList(Arrays.asList(PRIVATE_VALUES));
 
 
     /** Private Constructor. */
     private Severity (String name, int penalty)
     {
         mName = name;
         mPenalty = penalty;
         FROM_STRING.put(mName, this);
     }
 
     /**
      * Creates a Severity object from its int representation.
      *
      * @param i the int representation of the severity to be returned.
      * @return the Severity object represented by this int.
      * @throws IllegalArgumentException If the assigned int value isn't 
      *      listed in the internal severity table
      */
     public static Severity fromInt (int i)
             throws IllegalArgumentException
     {
         try
         {
             return PRIVATE_VALUES[i];
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
            final IllegalArgumentException ex = new IllegalArgumentException(
                 "Illegal int representation " + i + " of Severity");
            ex.initCause(e);
            throw ex;
         }
     }
 
     /**
      * Creates a Severity object from its String representation.
      *
      * @param str the string representation of the severity to be 
      *   returned.
      * @return the Severity object represented by this string.
      * @throws IllegalArgumentException If the given string value isn't 
      *      listed in the internal severity table
      */
     public static Severity fromString (String str)
             throws IllegalArgumentException
     {
         final Severity result = (Severity) FROM_STRING.get(str);
         if (result == null)
         {
             throw new IllegalArgumentException(
                 "Illegal string representation " + str + " of Severity");
         }
         return result;
     }
 
     /**
      * Returns the int representation of this severity.
      * @return the int representation of this severity.
      */
     public int toInt ()
     {
         return mOrdinal;
     }
 
     /**
      * Returns the String representation of this severity.
      * @return the String representation of this severity.
      */
     public String toString ()
     {
         return mName;
     }
 
     /**
      * Returns the penalty assigned to findings of this severity.
      * The number is the ten times the number of lines a finding of this
      * severity should mark as bad.
      * @return the penalty assigned to this severity.
      */
     public int getPenalty ()
     {
         return mPenalty;
     }
 
     /** {@inheritDoc} */
    public int compareTo (Object o)
     {
         return mOrdinal - ((Severity) o).mOrdinal;
     }
 
     /** {@inheritDoc} */
     public boolean equals (Object o)
     {
         return (o instanceof Severity) 
         && (mOrdinal == ((Severity) o).mOrdinal);
     }
 
     /** {@inheritDoc} */
     public int hashCode ()
     {
         return mOrdinal;
     }
 
     /**
      * Returns the maximum severe code of this and the given severity.
      * @param other the severity to compare with.
      * @return the maximum severe code of this and the given severity.
      */
     public Severity max (Severity other)
     {
         final Severity result;
 
         if (compareTo(other) > 0)
         {
             result = this;
         }
         else
         {
             result = other;
         }
         return result;
     }
 
     /**
      * Resolves instances being de-serialized to a single instance
      * per severity.
      */
     private Object readResolve ()
     {
         return PRIVATE_VALUES[mOrdinal];
     }
 }
