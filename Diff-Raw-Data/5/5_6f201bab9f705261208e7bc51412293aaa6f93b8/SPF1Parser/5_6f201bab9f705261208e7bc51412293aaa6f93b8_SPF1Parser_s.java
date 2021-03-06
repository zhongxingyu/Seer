 /***********************************************************************
  * Copyright (c) 1999-2006 The Apache Software Foundation.             *
  * All rights reserved.                                                *
  * ------------------------------------------------------------------- *
  * Licensed under the Apache License, Version 2.0 (the "License"); you *
  * may not use this file except in compliance with the License. You    *
  * may obtain a copy of the License at:                                *
  *                                                                     *
  *     http://www.apache.org/licenses/LICENSE-2.0                      *
  *                                                                     *
  * Unless required by applicable law or agreed to in writing, software *
  * distributed under the License is distributed on an "AS IS" BASIS,   *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
  * implied.  See the License for the specific language governing       *
  * permissions and limitations under the License.                      *
  ***********************************************************************/
 
 package org.apache.spf;
 
 import org.apache.spf.mechanismn.AMechanism;
 import org.apache.spf.mechanismn.AllMechanism;
 import org.apache.spf.mechanismn.Directive;
 import org.apache.spf.mechanismn.ExistsMechanism;
 import org.apache.spf.mechanismn.IP4Mechanism;
 import org.apache.spf.mechanismn.IP6Mechanism;
 import org.apache.spf.mechanismn.IncludeMechanism;
 import org.apache.spf.mechanismn.MXMechanism;
 import org.apache.spf.mechanismn.Mechanism;
 import org.apache.spf.mechanismn.PTRMechanism;
 import org.apache.spf.modifier.ExpModifier;
 import org.apache.spf.modifier.Modifier;
 import org.apache.spf.modifier.RedirectModifier;
 import org.apache.spf.modifier.UnknownModifier;
 import org.apache.spf.util.MatchResultSubset;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class can be used to parse SPF1-Records from their textual form to an SPF1Record object that is composed by 2 collections: directives and modifiers.
  * 
  * TODO this is work in progress and to be documented.
  * The matchResultPositions field should be made simpler and easier to understand.
  * 
  * TODO doubts about the specification
  * - redirect or exp with no domain-spec are evaluated as an unknown-modifiers according to the current spec (it does not make too much sense)
  * - top-label is defined differently in various specs. We'll have to review the code.
  *   - http://data.iana.org/TLD/tlds-alpha-by-domain.txt (we should probably beeter use and alpha sequence being at least 2 chars
  *   - Somewhere is defined as "." TLD [ "." ]
  *   - Otherwise defined as ( *alphanum ALPHA *alphanum ) / ( 1*alphanum "-" *( * alphanum / "-" ) alphanum )
  * 
  * @see org.apache.spf.SPF1Record
  * 
  * @author Norman Maurer <nm@byteaction.de>
  * @author Stefano Bagnara <apache@bago.org>
  */
 public class SPF1Parser {
 
     private static final Class[] knownMechanisms = new Class[] {
             AllMechanism.class, 
             AMechanism.class, 
             ExistsMechanism.class,
             IncludeMechanism.class, 
             IP4Mechanism.class, 
             IP6Mechanism.class,
             MXMechanism.class, 
             PTRMechanism.class 
     };
 
     private static final Class[] knownModifiers = new Class[] {
             ExpModifier.class, 
             RedirectModifier.class, 
             UnknownModifier.class 
     };
 
     /**
      * Regex based on http://ftp.rfc-editor.org/in-notes/authors/rfc4408.txt.
      * This will be the next official SPF-Spec
      */
 
    // TODO: fix the Quantifier problem
    // What is the "Quantifier problem"?
     public static final String ALPHA_DIGIT_PATTERN = "[a-zA-Z0-9]";
 
     public static final String ALPHA_PATTERN = "[a-zA-Z]";
 
     private static final String MACRO_LETTER_PATTERN = "[lsoditpvhcrLSODITPVHCR]";
 
     private static final String TRANSFORMERS_REGEX = "\\d*[r]?";
 
     private static final String DELEMITER_REGEX = "[\\.\\-\\+,/_\\=]";
 
     public static final String MACRO_EXPAND_REGEX = "\\%(?:\\{"
             + MACRO_LETTER_PATTERN + TRANSFORMERS_REGEX + DELEMITER_REGEX + "*"
             + "\\}|\\%|\\_|\\-)";
 
     private static final String MACRO_LITERAL_REGEX = "[\\x21-\\x24\\x26-\\x7e]";
 
     /**
      * ABNF: macro-string = *( macro-expand / macro-literal )
      */
     public static final String MACRO_STRING_REGEX = "(?:" + MACRO_EXPAND_REGEX
             + "|" + MACRO_LITERAL_REGEX + "{1})*";
 
     /**
      * ABNF: qualifier = "+" / "-" / "?" / "~"
      */
     private static final String QUALIFIER_PATTERN = "[\\+\\-\\?\\~]";
 
 //    /**
 //     * ABNF: mechanism = ( all / include / A / MX / PTR / IP4 / IP6 / exists )
 //     * AUTOGENERATED
 //     */
 //    private String MECHANISM_NAME_STEP_REGEX = null;
 //
 //    /**
 //     * TODO check that MACRO_STRING_REGEX already include all the available
 //     * chars in mechanism parameters
 //     */
 //    private static final String MECHANISM_VALUE_STEP_REGEX = MACRO_STRING_REGEX;
 
     /**
      * ABNF: toplabel = ( *alphanum ALPHA *alphanum ) / ( 1*alphanum "-" *(
      * alphanum / "-" ) alphanum ) ; LDH rule plus additional TLD restrictions ;
      * (see [RFC3696], Section 2)
      */
     private static final String TOP_LABEL_REGEX = "(?:"
             + SPF1Parser.ALPHA_DIGIT_PATTERN + "*" + SPF1Parser.ALPHA_PATTERN
             + "{1}" + SPF1Parser.ALPHA_DIGIT_PATTERN + "*|(?:"
             + SPF1Parser.ALPHA_DIGIT_PATTERN + "+" + "\\-" + "(?:"
             + SPF1Parser.ALPHA_DIGIT_PATTERN + "|\\-)*"
             + SPF1Parser.ALPHA_DIGIT_PATTERN + "))";
 
     /**
      * ABNF: domain-end = ( "." toplabel [ "." ] ) / macro-expand
      */
     private static final String DOMAIN_END_REGEX = "(?:\\." + TOP_LABEL_REGEX
             + "\\.?" + "|" + SPF1Parser.MACRO_EXPAND_REGEX + ")";
 
     /**
      * ABNF: domain-spec = macro-string domain-end
      */
     public static final String DOMAIN_SPEC_REGEX = "("
             + SPF1Parser.MACRO_STRING_REGEX + DOMAIN_END_REGEX + ")";
 
     private Pattern termsSeparatorPattern = null;
 
     
     private Pattern termPattern = null;
 
     
     // TODO: this should be automatically calculated 
     private static final int TERM_STEP_REGEX_QUALIFIER_POS = 1;
 
     private int TERM_STEP_REGEX_MECHANISM_POS = 2;
 
     private int TERM_STEP_REGEX_MODIFIER_POS;
 
     private Collection mechanismsCollection;
 
     private Collection modifiersCollection;
 
     private ArrayList matchResultPositions;
 
     private class TermDef {
         private Pattern pattern;
         private Class termDef;
         private int matchSize = 0;
         
         public TermDef(Class tClass) throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
             String pString = (String) tClass.getField("REGEX").get(null);
             pattern = Pattern.compile(pString);
             termDef = tClass;
             calcGroups(pString);
         }
 
         /**
          * This method should be done differently.
          * We currently don't hanlde the escaping at all.
          * 
          * @param pString
          */
         private void calcGroups(String pString) {
             if (pString == null || pString.length() == 0) {
                 matchSize = 0;
             } else {
                 int i = 0;
                 int c = 0;
                 while (i < pString.length()) {
                     int p1 = pString.indexOf("(",i);
                     int p2 = pString.indexOf("(?:",i);
                     if (p1 < 0) break;
                     if (p1 != p2) c++;
                     i = p1+1;
                 }
                 matchSize = c;
             }
         }
 
         public Pattern getPattern() {
             return pattern;
         }
 
         public Class getTermDef() {
             return termDef;
         }
         
         public int matchSize() {
             return matchSize ;
         }
 
         public int getMatchSize() {
             return matchSize;
         }
     }
     
     /**
      * Constructor.
      * Creates all the values needed to run the parsing
      */
     public SPF1Parser() {
         
         mechanismsCollection = createTermCollection(knownMechanisms);
         modifiersCollection = createTermCollection(knownModifiers);
         /**
          * ABNF: mechanism = ( all / include / A / MX / PTR / IP4 / IP6 / exists )
          */
         String MECHANISM_REGEX = createRegex(mechanismsCollection);
 
         /**
          * ABNF: modifier = redirect / explanation / unknown-modifier
          */
         String MODIFIER_REGEX = "(" + createRegex(modifiersCollection) + ")";
 
         /**
          * ABNF: directive = [ qualifier ] mechanism
          */
         String DIRECTIVE_REGEX = "(" + QUALIFIER_PATTERN + "?)(" + MECHANISM_REGEX
                 + ")";
 
         /**
          * ABNF: ( directive / modifier )
          */
         String TERM_REGEX = "(?:" + DIRECTIVE_REGEX + "|" + MODIFIER_REGEX
                 + ")";
 
         /**
          * ABNF: 1*SP
          */
         String TERMS_SEPARATOR_REGEX = "[ ]+";
 
         termsSeparatorPattern = Pattern.compile(TERMS_SEPARATOR_REGEX);
         termPattern = Pattern.compile(TERM_REGEX);
         
         initializePositions();
     }
 
     /**
      * Fill in the matchResultPositions ArrayList.
      * This array simply map each regex matchgroup to the Term class that
      * originated that part of the regex.
      */
     private void initializePositions() {
         matchResultPositions = new ArrayList();
         Iterator i = mechanismsCollection.iterator();
         int posIndex = 0;
         matchResultPositions.ensureCapacity(posIndex+1);
         matchResultPositions.add(posIndex,null);
         posIndex++;
         matchResultPositions.ensureCapacity(posIndex+1);
         matchResultPositions.add(posIndex,null);
         posIndex++;
         matchResultPositions.ensureCapacity(posIndex+1);
         matchResultPositions.add(posIndex,null);
         posIndex++;
         
         while (i.hasNext()) {
             TermDef td = (TermDef) i.next();
             int size = td.getMatchSize()+1;
             for (int k = 0; k < size; k++) {
                 matchResultPositions.ensureCapacity(posIndex+1);
                 matchResultPositions.add(posIndex,td);
                 posIndex++;
             }
         }
         TERM_STEP_REGEX_MODIFIER_POS = posIndex++;
         matchResultPositions.add(TERM_STEP_REGEX_MODIFIER_POS,null);
         i = modifiersCollection.iterator();
         while (i.hasNext()) {
             TermDef td = (TermDef) i.next();
             int size = td.getMatchSize()+1;
             for (int k = 0; k < size; k++) {
                 matchResultPositions.ensureCapacity(posIndex+1);
                 matchResultPositions.add(posIndex,td);
                 posIndex++;
             }
         }
     }
 
     /**
      * Loop the classes searching for a String static field named staticFieldName
      * and create an OR regeex like this:
      * (?:FIELD1|FIELD2|FIELD3)
      * 
      * @param classes classes to analyze
      * @param staticFieldName static field to concatenate
      * @return regex
      */
     private String createRegex(Collection commandMap) {
         StringBuffer modifierRegex = new StringBuffer();
         Iterator i = commandMap.iterator();
         boolean first = true;
         while (i.hasNext()) {
             if (first) {
                 modifierRegex.append("(?:(");
                 first = false;
             } else {
                 modifierRegex.append(")|(");
             }
             Pattern pattern = ((TermDef) i.next()).getPattern();
             modifierRegex.append(pattern.pattern());
         }
         modifierRegex.append("))");
         return modifierRegex.toString();
     }
 
     /**
      * @param classes classes to analyze
      * @param staticFieldName static field to concatenate
      * @return map <Class,Pattern>
      */
     private Collection createTermCollection(Class[] classes) {
         Collection l = new ArrayList();
         for (int j = 0; j < classes.length; j++) {
             Class mechClass = classes[j];
             try {
                 l.add(new TermDef(mechClass));
             } catch (Exception e) {
                 
             }
         }
         return l;
     }
 
     public SPF1Record parse(String spfRecord) throws PermErrorException,
             NoneException {
 
         SPF1Record result = new SPF1Record();
 
         // check the version "header"
         if (!spfRecord.startsWith(SPF1Utils.SPF_VERSION + " ")) {
             throw new NoneException("No valid SPF Record: " + spfRecord);
         }
 
         // extract terms
         String[] terms = termsSeparatorPattern.split(
                 spfRecord.replaceFirst(SPF1Utils.SPF_VERSION, ""));
 
         // cycle terms
         for (int i = 0; i < terms.length; i++) {
             if (terms[i].length() > 0) {
                 Matcher termMatcher = termPattern.matcher(terms[i]);
                 if (!termMatcher.matches()) {
                     throw new PermErrorException("Term [" + terms[i]
                             + "] is not syntactically valid: "
                             + termPattern.pattern());
                 }
                 
                 // true if we matched a modifier, false if we matched a
                 // directive
                 String modifierString = termMatcher
                         .group(TERM_STEP_REGEX_MODIFIER_POS);
                 
                 if (modifierString != null) {
                     // MODIFIER
                     Modifier mod = (Modifier) lookupAndCreateTerm(termMatcher, TERM_STEP_REGEX_MODIFIER_POS);
 
                     if (mod.enforceSingleInstance()) {
                         Iterator it = result.getModifiers().iterator();
                         while (it.hasNext()) {
                             if (it.next().getClass().equals(mod.getClass())) {
                                 throw new PermErrorException("More than one "
                                         + modifierString + " found in SPF-Record");
                             }
                         }
                     }
 
                     result.getModifiers().add(mod);
 
                 } else {
                     // DIRECTIVE
                     String qualifier = termMatcher.group(TERM_STEP_REGEX_QUALIFIER_POS);
 
                     Object mech = lookupAndCreateTerm(termMatcher, TERM_STEP_REGEX_MECHANISM_POS);
 
                     result.getDirectives().add(
                             new Directive(qualifier,(Mechanism) mech));
 
                 }
 
             }
         }
 
         return result;
     }
     
     /**
      * @param mechName
      * @param mechValue
      * @param mech
      * @param termDefs
      * @return
      * @throws PermErrorException
      */
     private Object lookupAndCreateTerm(MatchResult res, int start) throws PermErrorException {
         for (int k = start+1; k < res.groupCount(); k++) {
             //System.out.println(k+"] "+(pos.get(k) != null ? ((TermDef) pos.get(k)).getPattern().pattern()+" => "+res.group(k) : null));
             if (res.group(k) != null) {
                 TermDef c = (TermDef) matchResultPositions.get(k);
                 MatchResult subres = new MatchResultSubset(res, k, c.getMatchSize());
                 try {
                     Object term = c.getTermDef().newInstance();
                     if (term instanceof Configurable) {
                         if (subres == null || subres.groupCount() == 0) {
                             ((Configurable) term).config(null);
                         } else {
                             ((Configurable) term).config(subres);
                         }
                     }
                     return term;
                 } catch (IllegalAccessException e) {
                     throw new PermErrorException("Unexpected error creating term: "+e.getMessage());
                 } catch (InstantiationException e) {
                     throw new PermErrorException("Unexpected error creating term: "+e.getMessage());
                 }
 
             }
         }
         return null;
     }
 
 }
