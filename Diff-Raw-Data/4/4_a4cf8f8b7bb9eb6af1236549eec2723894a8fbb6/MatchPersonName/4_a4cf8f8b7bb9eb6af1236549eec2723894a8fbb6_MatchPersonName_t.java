 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
  * Java(TM), hosted at https://github.com/gunterze/dcm4che.
  *
  * The Initial Developer of the Original Code is
  * Agfa Healthcare.
  * Portions created by the Initial Developer are Copyright (C) 2011
  * the Initial Developer. All Rights Reserved.
  *
  * Contributor(s):
  * See @authors listed below
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  *
  * ***** END LICENSE BLOCK ***** */
 
 package org.dcm4chee.archive.ejb.query;
 
 import org.dcm4che.data.PersonName;
 import org.dcm4che.data.PersonName.Group;
 import org.dcm4chee.archive.persistence.AttributeFilter;
 
 import com.mysema.query.BooleanBuilder;
 import com.mysema.query.types.ExpressionUtils;
 import com.mysema.query.types.Predicate;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.mysema.query.types.path.StringPath;
 
 /**
  * @author Michael Backhaus <michael.backhaus@agfa.com>
  */
 class MatchPersonName {
 
     static Predicate personName(StringPath alphabethicName,
             StringPath ideographicName, 
             StringPath phoneticName,
             StringPath familyNameSoundex,
             StringPath givenNameSoundex, 
             String value,
             AttributeFilter filter, boolean isFuzzy, boolean matchUnknown) {
         if (value.equals("*"))
             return null;
 
         return isFuzzy
             ? fuzzyPersonName(alphabethicName, ideographicName, phoneticName,
                     familyNameSoundex, givenNameSoundex, value, filter, matchUnknown)
             : literalPersonName(alphabethicName, ideographicName, phoneticName,
                     familyNameSoundex, givenNameSoundex, value, filter, matchUnknown);
     }
 
     private static Predicate literalPersonName(StringPath alphabethicName,
             StringPath ideographicName, StringPath phoneticName,
             StringPath familyNameSoundex, StringPath givenNameSoundex,
             String value, AttributeFilter filter, boolean matchUnknown) {
         PersonName pn = new PersonName(value);
         BooleanBuilder builder = new BooleanBuilder();
         if (value.indexOf('=') == -1) {
             String queryString = toQueryString(pn, PersonName.Group.Alphabetic);
             builder.or(Builder.wildCard(alphabethicName, queryString, false));
             builder.or(Builder.wildCard(ideographicName, queryString, false));
             builder.or(Builder.wildCard(phoneticName, queryString, false));
             if (matchUnknown) {
                 Predicate emptyName = ExpressionUtils.and(alphabethicName.eq("*"),
                                       ExpressionUtils.and(ideographicName.eq("*"),
                                                           phoneticName.eq("*")));
                 builder.or(emptyName);
             }
         } else {
             builder.and(wildCard(alphabethicName, pn, PersonName.Group.Alphabetic, matchUnknown));
             builder.and(wildCard(ideographicName, pn, PersonName.Group.Ideographic, matchUnknown));
             builder.and(wildCard(phoneticName, pn, PersonName.Group.Phonetic, matchUnknown));
         }
         return builder;
     }
 
     private static Predicate wildCard(StringPath path,
             PersonName pn, Group group, boolean matchUnknown) {
         return pn.contains(group)
             ? Builder.wildCard(path, toQueryString(pn, group), matchUnknown)
             : null;
     }
 
     private static String toQueryString(PersonName pn, PersonName.Group g) {
         String s = pn.toString(g, true);
         return (s.endsWith("*") || pn.contains(g, PersonName.Component.NameSuffix)) ? s : s + "^*";
     }
 
     private static Predicate fuzzyPersonName(StringPath name,
             StringPath ideographicName, 
             StringPath phoneticName,
             StringPath familyNameSoundex,
             StringPath givenNameSoundex, 
             String value,
             AttributeFilter filter, boolean matchUnknown) {
         PersonName pn = new PersonName(value);
        boolean containsFamilyName = pn.contains(PersonName.Component.FamilyName);
        boolean containsGivenName = pn.contains(PersonName.Component.GivenName);
         if (containsFamilyName && containsGivenName)
             return fuzzyNames(name, familyNameSoundex, givenNameSoundex, 
                     pn.get(PersonName.Component.FamilyName),
                     pn.get(PersonName.Component.GivenName), 
                     filter, matchUnknown);
         if (containsGivenName)
             return fuzzyName(familyNameSoundex, givenNameSoundex, 
                     pn.get(PersonName.Component.GivenName), filter, matchUnknown);
         if (containsFamilyName)
             return fuzzyName(familyNameSoundex, givenNameSoundex, 
                     pn.get(PersonName.Component.FamilyName), filter, matchUnknown);
         return null;
     }
 
     private static Predicate fuzzyName(StringPath familyNameSoundex, StringPath givenNameSoundex,
             String value,  AttributeFilter filter, boolean matchUnknown) {
         String fuzzyName = filter.toFuzzy(value);
         BooleanBuilder builder = new BooleanBuilder()
             .or(fuzzyNameWildCard(familyNameSoundex, value, fuzzyName))
             .or(fuzzyNameWildCard(givenNameSoundex, value, fuzzyName));
         if (matchUnknown)
             builder.or(ExpressionUtils.and(
                     givenNameSoundex.eq("*"),
                     familyNameSoundex.eq("*")));
         return builder;
     }
 
     private static Predicate fuzzyNames(StringPath name,
             StringPath familyNameSoundex, StringPath givenNameSoundex,
             String familyName, String givenName, 
             AttributeFilter filter, boolean matchUnknown) {
         String fuzzyFamilyName = filter.toFuzzy(familyName);
         String fuzzyGivenName = filter.toFuzzy(givenName);
         Predicate names = ExpressionUtils.and(
                 fuzzyNameWildCard(givenNameSoundex, givenName, fuzzyGivenName),
                 fuzzyNameWildCard(familyNameSoundex, familyName, fuzzyFamilyName));
         Predicate namesSwap = ExpressionUtils.and(
                 fuzzyNameWildCard(givenNameSoundex, familyName, fuzzyFamilyName),
                 fuzzyNameWildCard(familyNameSoundex, givenName, fuzzyGivenName));
         BooleanBuilder builder = new BooleanBuilder().or(names).or(namesSwap);
         if (matchUnknown) {
             BooleanExpression noFamilyNameSoundex = familyNameSoundex.eq("*");
             BooleanExpression noGivenNameSoundex = givenNameSoundex.eq("*");
             builder
                 .or(ExpressionUtils.and(
                         fuzzyNameWildCard(givenNameSoundex, givenName, fuzzyGivenName),
                         noFamilyNameSoundex))
                 .or(ExpressionUtils.and(
                         fuzzyNameWildCard(familyNameSoundex, familyName, fuzzyFamilyName),
                         noGivenNameSoundex))
                 .or(ExpressionUtils.and(
                         fuzzyNameWildCard(givenNameSoundex, familyName, fuzzyFamilyName),
                         noFamilyNameSoundex))
                 .or(ExpressionUtils.and(
                         fuzzyNameWildCard(familyNameSoundex, givenName, fuzzyGivenName),
                         noGivenNameSoundex))
                 .or(ExpressionUtils.and(noFamilyNameSoundex, noGivenNameSoundex));
         }
 
         return builder;
     }
 
     private static Predicate fuzzyNameWildCard(StringPath field, String name, String fuzzy) {
         if (name.endsWith("*")) {
             String pattern = fuzzy.concat("%");
             return field.like(pattern);
         }
         return field.eq(fuzzy);
     }
 }
