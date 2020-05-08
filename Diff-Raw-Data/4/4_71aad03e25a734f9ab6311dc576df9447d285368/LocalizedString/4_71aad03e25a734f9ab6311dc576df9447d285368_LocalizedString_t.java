 /**
  * Copyright (c) 2013, Instituto Superior Técnico. All rights reserved.
  *
  * This file is part of fenixedu-commons.
  *
  * fenixedu-commons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * fenixedu-commons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with fenixedu-commons. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.fenixedu.commons.i18n;
 
 import java.io.Serializable;
import java.text.Collator;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Locale;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Strings;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 /**
  * <p>
  * Localized text, conceptually is a map between {@link Locale}s and the corresponding translation of a given text. This class is
  * immutable to work as a value type in the domain. Content is fetched passing the {@link Locale} on which we intend to view the
  * text, the text returned is the closest possible translation.
  * </p>
  * 
  * <h3>Creating/Modifying Content</h3>
  * 
  * <p>
  * Construction and modifications are done using the {@link Builder} class.
  * </p>
  * 
  * <p>
  * Example:
  * 
  * <pre>
  * <code>
  * new LocalizedString.Builder().with(Locale.ENGLISH, "hello")
  *     .with(Locale.forLanguageTag("pt-PT"), "bom dia").build();
  * </code>
  * </pre>
  * 
  * </p>
  * 
  * <h3>Accessing Content</h3>
  * 
  * <p>
  * Access is done invoking {@link #getContent()} or {@link #getContent(Locale)}
  * </p>
  * 
  */
 public final class LocalizedString implements Serializable, Comparable<LocalizedString> {
     private static final long serialVersionUID = -31967064113338779L;
 
     protected static final Logger logger = LoggerFactory.getLogger(LocalizedString.class);
 
     protected static final class InternalMap extends HashMap<Locale, String> {
         private static final long serialVersionUID = -2722162618911116061L;
 
         public String getContent(Locale locale) {
             if (containsKey(locale)) {
                 return get(locale);
             }
             // Best effort strategy inspired on ResourceBundle behaviour
             Locale generic = generifyLocale(locale);
             while (generic != null) {
                 if (containsKey(generic)) {
                     return get(generic);
                 }
                 generic = generifyLocale(generic);
             }
             Set<Locale> sameLanguage = new HashSet<>();
             for (Locale candidate : keySet()) {
                 if (candidate.getLanguage().equals(locale.getLanguage())) {
                     sameLanguage.add(candidate);
                 }
             }
             if (!sameLanguage.isEmpty()) {
                 if (sameLanguage.size() == 1) {
                     return get(sameLanguage.iterator().next());
                 }
                 Set<Locale> sameCountry = new HashSet<>();
                 for (Locale candidate : sameLanguage) {
                     if (candidate.getCountry().equals(locale.getCountry())) {
                         sameCountry.add(locale);
                     }
                 }
                 if (!sameCountry.isEmpty()) {
                     if (sameCountry.size() == 1) {
                         return get(sameCountry.iterator().next());
                     }
                     logger.debug("Retrieving a content with ambiguous locale fallback strategy");
                     return get(sameLanguage.iterator().next());
                 }
                 logger.debug("Retrieving a content with ambiguous locale fallback strategy");
                 return get(sameLanguage.iterator().next());
             }
             if (!locale.equals(I18N.getLocale())) {
                 return getContent(I18N.getLocale());
             }
             return null;
         }
 
         private Locale generifyLocale(Locale locale) {
             if (Strings.isNullOrEmpty(locale.getVariant())) {
                 if (Strings.isNullOrEmpty(locale.getCountry())) {
                     return null;
                 }
                 return new Locale(locale.getLanguage());
             }
             return new Locale(locale.getLanguage(), locale.getCountry());
         }
 
         public JsonElement json() {
             JsonObject json = new JsonObject();
             for (Locale locale : keySet()) {
                 json.addProperty(locale.toLanguageTag(), getContent(locale));
             }
             return json;
         }
 
         public static LocalizedString fromJson(JsonElement json) {
             Builder builder = new Builder();
             for (Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                 builder = builder.with(Locale.forLanguageTag(entry.getKey()), entry.getValue().getAsString());
             }
             return builder.build();
         }
     }
 
     /**
      * <p>
      * Builders are created empty with {@link #Builder()} or created from an existing {@link LocalizedString} with
      * {@link LocalizedString#builder()}.
      * </p>
      * 
      * <p>
      * Several flow language modifiers are available to work with the builder:
      * <ul>
      * <li>{@link Builder#with(Locale, String)}</li>
      * <li>{@link Builder#without(Locale)}</li>
      * <li>{@link Builder#append(LocalizedString)}</li>
      * <li>{@link Builder#append(LocalizedString, String)}</li>
      * <li>{@link Builder#append(String)}</li>
      * <li>{@link Builder#append(String, String)}</li>
      * </ul>
      * </p>
      * 
      * <p>
      * At the end, invoke {@link Builder#build()} to obtain the corresponding {@link LocalizedString}
      * </p>
      */
     public static final class Builder {
         protected InternalMap map;
 
         /**
          * Create an empty builder.
          */
         public Builder() {
             this.map = new InternalMap();
         }
 
         protected Builder(InternalMap map) {
             this.map = map;
         }
 
         /**
          * Sets/changes the translation of the given locale to the given text.
          * 
          * @param locale {@link Locale} on which to set the translated text.
          * @param content The translated text.
          * @return The builder instance with the content changed.
          * 
          * @see {@link #without(Locale)}
          */
         public Builder with(Locale locale, String content) {
             if (locale == null) {
                 throw new RuntimeException("Attempted adding null locale to LocalizedString");
             }
             if (Strings.isNullOrEmpty(content)) {
                 logger.debug("adding null content for locale: {} to {}", locale.toLanguageTag(), this.toString());
                 return this;
             }
             map.put(locale, content);
             return this;
         }
 
         /**
          * Removes the translation of the given locale.
          * 
          * @param locale {@link Locale} from which to remove a translation.
          * @return The builder instance with the content changed.
          * 
          * @see {@link #with(Locale, String)}
          */
         public Builder without(Locale locale) {
             map.remove(locale);
             return this;
         }
 
         /**
          * Appends the given Localized string at the end.
          * 
          * @param string the {@link LocalizedString} to append
          * @return The builder instance with the content changed.
          * 
          * @see {@link #append(LocalizedString, String)}, {@link #append(String)}
          */
         public Builder append(LocalizedString string) {
             return append(string, "");
         }
 
         /**
          * Appends the given Localized string at the end, including a separator string between.
          * 
          * @param string the {@link LocalizedString} to append
          * @param separator string to be placed between the contents
          * @return The builder instance with the content changed.
          */
         public Builder append(LocalizedString string, String separator) {
             Set<Locale> locales = new HashSet<>(map.keySet());
             locales.addAll(string.map.keySet());
             for (Locale locale : locales) {
                 map.put(locale,
                         Strings.nullToEmpty(map.getContent(locale)) + separator
                                 + Strings.nullToEmpty(string.map.getContent(locale)));
             }
             return this;
         }
 
         /**
          * Appends the given string at the end.
          * 
          * @param string the string to append
          * @return The builder instance with the content changed.
          */
         public Builder append(String string) {
             return append(string, "");
         }
 
         /**
          * Appends the given string at the end, including a separator string between.
          * 
          * @param string the string to append
          * @param separator string to be placed between the contents
          * @return The builder instance with the content changed.
          */
         public Builder append(String string, String separator) {
             for (Locale locale : map.keySet()) {
                 map.put(locale, Strings.nullToEmpty(map.getContent(locale)) + separator + Strings.nullToEmpty(string));
             }
             return this;
         }
 
         /**
          * Builds an {@link LocalizedString} from the builder state.
          * 
          * @return the corresponding {@link LocalizedString}.
          */
         public LocalizedString build() {
             return new LocalizedString(map);
         }
     }
 
     protected final InternalMap map;
 
     /**
      * Creates an empty {@link LocalizedString}.
      */
     public LocalizedString() {
         this.map = new InternalMap();
     }
 
     /**
      * Creates an {@link LocalizedString} initialised with the given translation.
      * 
      * @param locale {@link Locale} of the translation.
      * @param content translated text.
      */
     public LocalizedString(Locale locale, final String content) {
         this.map = new Builder().with(locale, content).map;
     }
 
     protected LocalizedString(InternalMap map) {
         this.map = map;
     }
 
     /**
      * Create a {@link Builder} initialised with the translation of this instance.
      * 
      * @return the {@link Builder} instance.
      */
     public Builder builder() {
         return new Builder((InternalMap) map.clone());
     }
 
     /**
      * Same as <code>builder().with(locale, content).build()</code>. Does not change current instance.
      * 
      * @param locale {@link Locale} of the translation.
      * @param content translated text.
      * @return {@link LocalizedString} instance with the added translation.
      * @see {@link Builder#with(Locale, String)}
      */
     public LocalizedString with(Locale locale, final String content) {
         return builder().with(locale, content).build();
     }
 
     /**
      * Same as <code>builder().without(locale).build()</code>. Does not change current instance.
      * 
      * @param locale {@link Locale} to remove.
      * @return {@link LocalizedString} instance with the translation removed.
      * @see {@link Builder#without(Locale)}
      */
     public LocalizedString without(Locale locale) {
         return builder().without(locale).build();
     }
 
     /**
      * Same as <code>builder().append(string).build()</code>. Does not change current instance.
      * 
      * @param string the {@link LocalizedString} to append
      * @return {@link LocalizedString} instance with the appended content.
      * @see {@link Builder#append(LocalizedString)}
      */
     public LocalizedString append(LocalizedString string) {
         return builder().append(string).build();
     }
 
     /**
      * Same as <code>builder().append(string, separator).build()</code>. Does not change current instance.
      * 
      * @param string the {@link LocalizedString} to append
      * @param separator string to be placed between the contents
      * @return {@link LocalizedString} instance with the appended content.
      * @see {@link Builder#append(LocalizedString, String)}
      */
     public LocalizedString append(LocalizedString string, String separator) {
         return builder().append(string, separator).build();
     }
 
     /**
      * Same as <code>builder().append(string).build()</code>. Does not change current instance.
      * 
      * @param string the string to append
      * @return {@link LocalizedString} instance with the appended content.
      * @see {@link Builder#append(String)}
      */
     public LocalizedString append(String string) {
         return builder().append(string).build();
     }
 
     /**
      * Same as <code>builder().append(string, separator).build()</code>. Does not change current instance.
      * 
      * @param string the string to append
      * @param separator string to be placed between the contents
      * @return {@link LocalizedString} instance with the appended content.
      * @see {@link Builder#append(String, String)}
      */
     public LocalizedString append(String string, String separator) {
         return builder().append(string, separator).build();
     }
 
     /**
      * Locales of the tanslations present in this {@link LocalizedString}
      * 
      * @return {@link Set} of {@link Locale}s.
      */
     public Set<Locale> getLocales() {
         return Collections.unmodifiableSet(map.keySet());
     }
 
     /**
      * Accesses the translation corresponding to the given {@link Locale}. The result is the best possible approximation, as
      * follows:
      * <ul>
      * <li>If an exact match is found return it;</li>
      * <li>If a more generic version of the requested {@link Locale} is found, return it. For example, requested: en-GB, existing:
      * en;</li>
      * <li>If a translation is found sharing the language or the country parts of the {@link Locale}, return it, For example,
      * requested: en-US, existing: en-GB;</li>
      * <li>Returns the translation for the thread's {@link Locale} if exists, otherwise null is returned.</li>
      * </ul>
      * 
      * @param locale the {@link Locale} to fetch.
      * @return the best possible translation, can be null.
      */
     public String getContent(Locale locale) {
         return map.getContent(locale);
     }
 
     /**
      * Accesses the translation corresponding to the thread's {@link Locale}, or the system's default {@link Locale} if the first
      * is not found.
      * 
      * @return the best possible translation, can be null.
      * @see {@link #getContent(Locale)}
      */
     public String getContent() {
         String text = getContent(I18N.getLocale());
         if (text == null) {
             return getContent(Locale.getDefault());
         }
         return text;
     }
 
     /**
      * @return true if contains no translations
      */
     public boolean isEmpty() {
         return map.isEmpty();
     }
 
     /**
      * Serialise to json.
      * 
      * @return the corresponding {@link JsonElement} instance.
      * @see {@link #fromJson(JsonElement)}
      */
     public JsonElement json() {
         return map.json();
     }
 
     /**
      * Import from json.
      * 
      * @param json the {@link JsonElement} in the same format returned by {@link #json()}.
      * @return the {@link LocalizedString} parsed from json.
      * @see {@link #json()}
      */
     public static LocalizedString fromJson(JsonElement json) {
         return InternalMap.fromJson(json);
     }
 
     @Override
     public String toString() {
         return map.toString();
     }
 
     @Override
     public int compareTo(LocalizedString other) {
        return Collator.getInstance(I18N.getLocale()).compare(getContent(), other.getContent());
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof LocalizedString) {
             LocalizedString i18NString = (LocalizedString) obj;
             return map.equals(i18NString.map);
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return map.hashCode();
     }
 }
