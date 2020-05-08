 /*
  * Copyright (C) 2012 Gnapse.com
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.gnapse.common.inflector;
 
 import static com.gnapse.common.inflector.Rule.disjunction;
 import static com.gnapse.common.inflector.Rule.toBiMap;
 import static com.gnapse.common.inflector.Rule.toMap;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import com.google.common.annotations.Beta;
 import com.google.common.base.Function;
 import com.google.common.collect.BiMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 /**
  * Provides default inflectors implementing {@linkplain PluralInflector plural} and
  * {@linkplain SingularInflector singular} inflection rules in the English language.  It also
  * provides some static utility methods to work with {@link Inflector} instances.
  *
  * @author Ernesto García
  */
 @Beta
 public final class Inflectors {
 
     private static PluralInflector pluralInflector;
 
     private static SingularInflector singularInflector;
 
     /**
      * Private constructor to prevent instantiation.
      */
     private Inflectors() {
     }
 
     /**
      * The identity {@code Inflector}, one that returns input words unmodified.
      */
     public static final Inflector IDENTITY = new Inflector() {
         @Override public String apply(String input) { return input; }
     };
 
     /**
      * Returns the default {@linkplain PluralInflector English plural inflector}.
      */
     public static RuleBasedInflector getPluralInflector() {
         if (pluralInflector == null) {
             pluralInflector = new PluralInflector();
         }
         return pluralInflector;
     }
 
     /**
      * Returns the default {@linkplain SingularInflector English singular inflector}.
      */
     public static RuleBasedInflector getSingularInflector() {
         if (singularInflector == null) {
             singularInflector = new SingularInflector();
         }
         return singularInflector;
     }
 
     /**
      * Returns the plural form of the given word, using the default
      * {@linkplain #getPluralInflector() English plural inflector}.
      *
      * @param word the word to convert to its plural form
      * @return the plural form of the specified word
      */
     public static String pluralOf(String word) {
         return getPluralInflector().apply(word);
     }
 
     /**
      * Returns the singular form of the given word, using the default
      * {@linkplain #getSingularInflector() English singular inflector}.
      *
      * @param word the word to convert to its singular form
      * @return the singular form of the specified word
      */
     public static String singularOf(String word) {
         return getSingularInflector().apply(word);
     }
 
     /**
      * Returns an {@code Inflector} that inflects words using the given {@link Function}.  If the
      * function is already an {@code Inflector}, then this method simply performs a typecast and
      * returns the specified function {@code fn} unmodified.  Otherwise, it returns an inflector
      * instance that merely wraps {@code fn}'s {@link Function#apply(Object) apply} method.
      *
      * @param fn the function to return as an {@code Inflector}.
      * @return an {@code Inflector} acting as the specified function, or the function itself if it
      *     is already an {@code Inflector}
      */
     public static Inflector forFunction(final Function<String, String> fn) {
         checkNotNull(fn);
         if (fn instanceof Inflector) {
             return (Inflector)fn;
         } else {
             return new Inflector() {
                 @Override public String apply(String input) {
                     return fn.apply(input);
                 }
             };
         }
     }
 
     /**
      * Inflects all words in the given list, using the specified {@code Inflector}.
      *
      * @param inflector the inflector to use to inflect words
      * @param words the list of words to inflect
      * @return a new list containing the resulting inflected words
      */
     public static List<String> inflectAll(Inflector inflector, Iterable<String> words) {
         final List<String> result = Lists.newArrayListWithCapacity(Iterables.size(words));
         for (String word : words) {
             result.add(inflector.apply(word));
         }
         return result;
     }
 
     /**
      * Equivalent to {@link #inflectAll(Inflector, Iterable)}.
      */
     public static List<String> inflectAll(Inflector inflector, String... words) {
         return inflectAll(inflector, Arrays.asList(words));
     }
 
     /**
      * Creates and returns a new {@linkplain Map map} with all the given words as keys mapped to
      * their inflected form, using the specified {@code Inflector}.
      *
      * @param inflector the inflector used to inflect words
      * @param words the list of words to inflect
      * @return a new map containing all the given words as keys mapped to their inflected form
      */
     public static Map<String, String> inflectionMap(Inflector inflector, Iterable<String> words) {
         final Map<String, String> result = Maps.newHashMap();
         for (String word : words) {
             result.put(word, inflector.apply(word));
         }
         return result;
     }
 
     /**
      * Equivalent to {@link #inflectionMap(Inflector, Iterable)}.
      */
     public static Map<String, String> inflectionMap(Inflector inflector, String... words) {
         return inflectionMap(inflector, Arrays.asList(words));
     }
 
     //
     // Implementations of plural and singular inflections in the English language
     //
 
     /**
      * An {@link Inflector} implementing plural inflection rules in the English language.
      */
     public static class PluralInflector extends RuleBasedInflector {
 
         /**
          * Creates a new plural inflector with the default set of rules.
          */
         public PluralInflector() {
             super();
             rules.addAll(RULES);
         }
 
         /**
          * Rules based on <a href="http://www.csse.monash.edu.au/~damian/papers/HTML/Plurals.html">
          * Conway's algorithm</a>.  Step numbers below are references to the steps in that
          * algorithm.
          *
          * @see <a href="http://www.csse.monash.edu.au/~damian/papers/HTML/Plurals.html">Conway's
          * algorithm</a>
          */
         private final List<Rule> RULES = Arrays.asList(new Rule[] {
 
             /*
              * Uninflected words [Step 2]
              */
             Rule.IDENTITY.onlyForWords(InflectionData.UNINFLECTED_WORDS),
             Rule.IDENTITY.onlyForWords(InflectionData.UNINFLECTED_S_WORDS),
             Rule.IDENTITY.forWordsEndingWith(InflectionData.UNINFLECTED_SUFFIXES_REGEX),
 
             /*
              * Turns "degree Fahrenheit" to "degrees Fahrenheit"
              */
             Rule.inflectPattern("(?i)^(degree)((\\s|-).*)$", "$1s$2"),
 
             /*
              * Phrases like "kilogram of salt" => "kilograms of salt" [Step 12]
              */
             Rule.inflectPattern(InflectionData.PREPOSITIONS_REGEX, new Function<Matcher, String>() {
                 @Override public String apply(Matcher matcher) {
                     return PluralInflector.this.apply(matcher.group(1)) +
                             matcher.group(2) + matcher.group(3);
                 }
             }),
 
             /*
              * Phrases ending with a sub-phrase in parenthesis.
              */
             Rule.inflectPattern("(?i)^(.*)(\\s*\\(.*\\))\\s*$", new Function<Matcher, String>() {
                 @Override public String apply(Matcher matcher) {
                     return PluralInflector.this.apply(matcher.group(1)) + matcher.group(2);
                 }
             }),
 
             /*
              * Pronouns and irregular nouns [Steps 3 and 4]
              */
             Rule.irregulars(InflectionData.PLURAL_PRONOUNS),
             Rule.irregulars(InflectionData.IRREGULAR_NOUNS),
 
             /*
              * Families of irregular plurals for common suffixes [Step 5]
              */
             Rule.inflectPattern("(?i)^([lm])ouse$", "$1ice"),
             Rule.inflectSuffix("foot", "feet"),
             Rule.inflectSuffix("goose", "geese"),
             Rule.inflectSuffix("tooth", "teeth"),
             Rule.inflectSuffix("zoon", "zoa"),
             Rule.inflectSuffix("man", "men").exceptForWords(InflectionData.PLURAL_MAN_MANS_RULE),
             Rule.inflectSuffix("sis", "ses").onlyForWords(InflectionData.PLURAL_SIS_SES_RULE),
             Rule.inflectSuffix("is", "es").forWordsEndingWith("[cx]is"),
 
             /*
              * Assimilated irregular plurals [Step 6]
              */
             Rule.inflectSuffix("ex", "ices").onlyForWords(InflectionData.PLURAL_EX_ICES_RULE),
             Rule.inflectSuffix("ix", "ices").onlyForWords(InflectionData.PLURAL_IX_ICES_RULE),
             Rule.inflectSuffix("um", "a").onlyForWords(InflectionData.PLURAL_UM_A_RULE),
             Rule.inflectSuffix("us", "i").onlyForWords(InflectionData.PLURAL_US_I_RULE),
             Rule.inflectSuffix("on", "a").onlyForWords(InflectionData.PLURAL_ON_A_RULE),
             Rule.inflectSuffix("a", "ae").onlyForWords(InflectionData.PLURAL_A_AE_RULE),
 
             /*
              * Classical variants of modern inflections [Step 7]
              */
             Rule.inflectSuffix("trix", "trices"),
             Rule.inflectSuffix("eau", "eaux"),
             Rule.inflectSuffix("ieu", "ieux"),
             Rule.inflectSuffix("nx", "nges").forWordsEndingWith("[iay]nx"),
             // TODO: Some more categories still missing here (see algorithm webpage)
 
             /*
              * Suffixes -ch, -sh, and -ss all take -es in the plural [Step 8]
              */
             Rule.inflectSuffix("h", "hes").forWordsEndingWith("[cs]h"),
             Rule.inflectSuffix("ss", "sses"),
             Rule.inflectSuffix("x", "xes"),
             Rule.inflectSuffix("z", "zes"),
 
             /*
              * Certain words ending in -f or -fe take -ves in the plural [Step 9]
              */
             Rule.inflectSuffix("f", "ves").forWordsEndingWith("([aeo]lf|[^d]eaf|arf|loaf)"),
             Rule.inflectSuffix("fe", "ves").forWordsEndingWith("[lnw]ife"),
 
             /*
              * Nouns ending with "-y" [Step 10]
              */
             Rule.inflectSuffix("y", "ys").forWordsEndingWith("[aeiou]y"),
             Rule.inflectSuffix("y", "ies").exceptForWords(InflectionData.PLURAL_Y_YS_RULE),
 
             /*
              * Nouns ending with "-o" [Step 11]
              */
             Rule.inflectSuffix("o", "os").onlyForWords(InflectionData.PLURAL_O_OS_RULE),
             Rule.inflectSuffix("o", "os").forWordsEndingWith("[aeiou]o"),
             Rule.inflectSuffix("o", "oes"),
 
             /*
              * Default rule [Step 13]
              */
             Rule.inflectSuffix("", "s"),
         });
 
     } // PluralInflector
 
     /**
      * An {@link Inflector} implementing singular inflection rules in the English language.
      */
     public static class SingularInflector extends RuleBasedInflector {
 
         /**
          * Creates a new singular inflector with the default set of rules.
          */
         public SingularInflector() {
             super();
             rules.addAll(RULES);
         }
 
         /**
          * Rules based on <a href="http://www.csse.monash.edu.au/~damian/papers/HTML/Plurals.html">
          * Conway's algorithm</a>.  Step numbers below are references to the steps in that
          * algorithm. In addition to reversing the rules for the singular, the order of steps also
          * changes.
          *
          * @see <a href="http://www.csse.monash.edu.au/~damian/papers/HTML/Plurals.html">Conway's
          * algorithm</a>
          */
         private final List<Rule> RULES = Arrays.asList(new Rule[] {
 
             /*
              * Uninflected words [Step 2]
              */
             Rule.IDENTITY.onlyForWords(InflectionData.UNINFLECTED_WORDS),
             Rule.IDENTITY.onlyForWords(InflectionData.UNINFLECTED_S_WORDS),
             Rule.IDENTITY.forWordsEndingWith(InflectionData.UNINFLECTED_SUFFIXES_REGEX),
 
             /*
              * Turns "degrees Fahrenheit" to "degree Fahrenheit"
              */
             Rule.inflectPattern("^(?i)(degree)s((\\s|-).*)$", "$1$2"),
 
             /*
              * Phrases like "kilograms of salt" => "kilogram of salt" [Step 12]
              */
             Rule.inflectPattern(InflectionData.PREPOSITIONS_REGEX, new Function<Matcher, String>() {
                 @Override public String apply(Matcher matcher) {
                     return SingularInflector.this.apply(matcher.group(1)) +
                             matcher.group(2) + matcher.group(3);
                 }
             }),
 
             /*
              * Phrases ending with a sub-phrase in parenthesis.
              */
             Rule.inflectPattern("(?i)^(.*)(\\s*\\(.*\\))\\s*$", new Function<Matcher, String>() {
                 @Override public String apply(Matcher matcher) {
                     return SingularInflector.this.apply(matcher.group(1)) + matcher.group(2);
                 }
             }),
 
             /*
              * Pronouns and irregular nouns [Steps 3 and 4]
              */
             Rule.irregulars(InflectionData.SINGULAR_PRONOUNS),
             Rule.irregulars(InflectionData.IRREGULAR_NOUNS.inverse()),
 
             /*
              * Classical variants of modern inflections [Step 7]
              */
             Rule.inflectSuffix("trices", "trix"),
             Rule.inflectSuffix("eaux", "eau"),
             Rule.inflectSuffix("ieux", "ieu"),
             Rule.inflectSuffix("nges", "nx").forWordsEndingWith("[iay]nges"),
             // TODO: Some more categories still missing here (see algorithm webpage)
 
             /*
              * Suffixes -ch, -sh, and -ss all take -es in the plural [Step 8]
              */
             Rule.inflectSuffix("hes", "h").forWordsEndingWith("[cs]hes"),
             Rule.inflectSuffix("sses", "ss"),
             Rule.inflectSuffix("xes", "x"),
             Rule.inflectSuffix("zes", "z"),
 
             /*
              * Assimilated irregular plurals [Step 6]
              */
             Rule.inflectSuffix("ices", "ex").onlyForWords(InflectionData.SINGULAR_ICES_EX_RULE),
             Rule.inflectSuffix("ices", "ix").onlyForWords(InflectionData.SINGULAR_ICES_IX_RULE),
             Rule.inflectSuffix("a", "um").onlyForWords(InflectionData.SINGULAR_A_UM_RULE),
             Rule.inflectSuffix("i", "us").onlyForWords(InflectionData.SINGULAR_I_US_RULE),
             Rule.inflectSuffix("a", "on").onlyForWords(InflectionData.SINGULAR_A_ON_RULE),
             Rule.inflectSuffix("ae", "a").onlyForWords(InflectionData.SINGULAR_AE_A_RULE),
 
             /*
              * Certain words ending in -f or -fe take -ves in the plural [Step 9]
              */
             Rule.inflectSuffix("ves", "f").forWordsEndingWith("([aeo]lves|[^d]eaves|arves|loaves)"),
             Rule.inflectSuffix("ves", "fe").forWordsEndingWith("[lnw]ives"),
 
             /*
              * Families of irregular plurals for common suffixes [Step 5]
              */
             Rule.inflectPattern("(?i)^([ml])ice$", "$1ouse"),
             Rule.inflectSuffix("feet", "foot"),
             Rule.inflectSuffix("geese", "goose"),
             Rule.inflectSuffix("teeth", "tooth"),
             Rule.inflectSuffix("zoa", "zoon"),
             Rule.inflectSuffix("men", "man").exceptForWords(InflectionData.SINGULAR_MANS_MAN_RULE),
             Rule.inflectSuffix("ses", "sis").onlyForWords(InflectionData.SINGULAR_SES_SIS_RULE),
             Rule.inflectSuffix("es", "is").forWordsEndingWith("[cx]es"),
 
             /*
              * Nouns ending with "-y" [Step 10]
              */
             Rule.inflectSuffix("ys", "y").forWordsEndingWith("[aeiou]ys"),
             Rule.inflectSuffix("ies", "y"),
 
             /*
              * Nouns ending with "-o" [Step 11]
              */
             Rule.inflectSuffix("os", "o").onlyForWords(InflectionData.SINGULAR_OS_O_RULE),
             Rule.inflectSuffix("os", "o").forWordsEndingWith("[aeiou]os"),
             Rule.inflectSuffix("oes", "o"),
 
             /*
              * Default rule [Step 13]
              */
             Rule.inflectSuffix("s", ""),
         });
 
     } // SingularInflector
 
     /**
      * Internal class acting as a namespace or container of data used to define plural and singular
      * inflection rules in the English language.
      */
     private static final class InflectionData {
 
         private InflectionData() {
         }
 
         /**
          * Words with these suffixes will not be inflected.
          */
         private static final String UNINFLECTED_SUFFIXES_REGEX = disjunction(new String[] {
             "ceps",
             "craft",
             "deer",
             "fish",
             "itis",
             "measles",
             "ois",
             "pox",
             "sheep",
             "[nrlm]ese",
         });
 
         /**
          * Words listed here will not be inflected
          */
         private static final List<String> UNINFLECTED_WORDS = Arrays.asList(new String[] {
             // Fish and herd animals
             "bison",
             "bream",
             "carp",
             "cod",
             "flounder",
             "mackerel",
             "moose",
             "pike",
             "salmon",
             "swine",
             "tuna",
             "trout",
             "whiting",
 
             // Nationals ending in -ese
             "amoyese",
             "borghese",
             "congoese",
             "faroese",
             "foochowese",
             "genevese",
             "genoese",
             "gilbertese",
             "hottentotese",
             "kiplingese",
             "kongoese",
             "lucchese",
             "maltese",
             "nankingese",
             "niasese",
             "pekingese",
             "piedmontese",
             "pistoiese",
             "portuguese",
             "sarawakese",
             "shavese",
             "vermontese",
             "wenchowese",
             "yengeese",
 
             // Other oddities
             "djinn",
 
             // Pairs or groups subsumed to a singular
             "breeches",
             "britches",
             "clippers",
             "gallows",
             "herpes",
             "hijinks",
             "headquarters",
             "pincers",
             "pliers",
             "proceedings",
             "scissors",
             "shears",
             "trousers",
 
             // Unassimilated Latin 4th declension
             "cantus",
             "coitus",
             "nexus",
 
             // Recent imports
             "contretemps",
             "corps",
             "debris",
             "siemens",
 
             // Diseases
             "diabetes",
             "mumps",
 
             // Others
             "chassis",
             "innings",
             "jackanapes",
             "news",
             "mews",
             "rabies",
             "series",
             "species",
         });
 
         /**
          * Prepositions.  Useful to pluralize "door across the street" as "doors across the street";
          * or "pound of salt" as "pounds of salt".
          */
         private static final List<String> PREPOSITIONS = Arrays.asList(new String[] {
             "about",
             "above",
             "across",
             "after",
             "among",
             "around",
             "at",
             "athwart",
             "before",
             "behind",
             "below",
             "beneath",
             "beside",
             "besides",
             "between",
             "betwixt",
             "beyond",
             "but",
             "by",
             "during",
             "except",
             "for",
             "from",
             "in",
             "into",
             "near",
             "of",
             "off",
             "on",
             "onto",
             "out",
             "over",
             "since",
             "till",
             "to",
             "under",
             "until",
             "unto",
             "upon",
             "with",
         });
 
         /**
          * A regular expression to recognize phrases with prepositions.
          */
         private static final String PREPOSITIONS_REGEX
                 = disjunction("(?i)^(.+)((?:\\s|-)(?:%s)(?:\\s|-))(.+)$", PREPOSITIONS);
 
         /**
          * Mapping of singular to plural pronouns.
          */
         private static final Map<String, String> PLURAL_PRONOUNS = toMap(new String[][]{
             // nominative
             { "i", "we" },
             { "you", "you" },
             { "she", "they" },
             { "he", "they" },
             { "it", "they" },
             { "they", "they" },
 
             // reflexive
             { "myself", "ourselves" },
             { "yourself", "yourselves" },
             { "herself", "themselves" },
             { "himself", "themselves" },
             { "itself", "themselves" },
             { "themself", "themselves" },
 
             // possessive
             { "mine", "ours" },
             { "yours", "yours" },
             { "hers", "theirs" },
             { "his", "theirs" },
             { "its", "theirs" },
             { "theirs", "theirs" },
         });
 
         /**
          * Mapping of plural to singular pronouns.
          */
         private static final BiMap<String, String> SINGULAR_PRONOUNS = toBiMap(new String[][]{
             // nominative
             { "we", "i" },
             { "you", "you" },
             { "they", "they" },
 
             // reflexive
             { "ourselves", "myself" },
             { "yourselves", "yourself" },
             { "themselves", "themself" },
 
             // possessive
             { "ours", "mine" },
             { "yours", "yours" },
             { "theirs", "theirs" },
         });
 
         /**
          * Mapping of irregular nouns not covered by any other special rule.
          */
         private static final BiMap<String, String> IRREGULAR_NOUNS = toBiMap(new String[][]{
             { "child", "children" },
             { "person", "people" },
             { "money", "monies" },
             { "mongoose", "mongooses" },
             { "ox", "oxen" },
             { "soliloquy", "soliloquies" },
             { "graffito", "graffiti" },
             { "genie", "genies" },
             { "trilby", "trilbys" },
             { "numen", "numina" },
             { "atman", "atmas" },
             { "quiz", "quizzes" },
 
             // Words ending in -s
             { "octopus", "octopuses" },
             { "corpus", "corpuses" },
             { "opus", "opuses" },
             { "genus", "genera" },
             { "mythos", "mythoi" },
             { "penis", "penises" },
             { "testis", "testes" },
             { "atlas", "atlases" },
             { "alias", "aliases" },
            { "cloth", "clothes" },
         });
 
         /**
          * Nouns ending in -man that are NOT pluralized as -men.
          */
         private static final List<String> PLURAL_MAN_MANS_RULE = Arrays.asList(new String[] {
             "human",
 
             "alabaman",
             "bahaman",
             "burman",
             "german",
             "hiroshiman",
             "liman",
             "nakayaman",
             "oklahoman",
             "panaman",
             "selman",
             "sonaman",
             "tacoman",
             "yakiman",
             "yokohaman",
             "yuman",
         });
 
         /**
          * Nouns ending in {@code -ex} with plural ending in {@code -ices}.
          */
         private static final List<String> PLURAL_EX_ICES_RULE = Arrays.asList(new String[] {
             "codex",
             "murex",
             "silex",
         });
 
         /**
          * Nouns ending in {@code -ix} with plural ending in {@code -ices}.
          */
         private static final List<String> PLURAL_IX_ICES_RULE = Arrays.asList(new String[] {
             "helix",
             "radix",
         });
 
         /**
          * Nouns ending in {@code -um} with plural ending in {@code -a}.
          */
         private static final List<String> PLURAL_UM_A_RULE = Arrays.asList(new String[] {
             "agendum",
             "bacterium",
             "candelabrum",
             "datum",
             "desideratum",
             "erratum",
             "extremum",
             "ovum",
             "stratum",
         });
 
         /**
          * Nouns ending in {@code -us} with plural ending in {@code -i}.
          */
         private static final List<String> PLURAL_US_I_RULE = Arrays.asList(new String[] {
             "alumnus",
             "alveolus",
             "bacillus",
             "bronchus",
             "gladiolus",
             "locus",
             "meniscus",
             "nucleus",
             "stimulus",
         });
 
         /**
          * Nouns ending in {@code  -on} with plural ending in {@code -a}.
          */
         private static final List<String> PLURAL_ON_A_RULE = Arrays.asList(new String[] {
             "aphelion",
             "asyndeton",
             "criterion",
             "hyperbaton",
             "noumenon",
             "organon",
             "perihelion",
             "phenomenon",
             "prolegomenon",
         });
 
         /**
          * Nouns ending in {@code -a} with plural ending in {@code -ae}.
          */
         private static final List<String> PLURAL_A_AE_RULE = Arrays.asList(new String[] {
             "alga",
             "alumna",
             "persona",
             "vertebra",
         });
 
         /**
          * Nouns ending in {@code -sis} with plural ending in {@code -ses}.
          */
         private static final List<String> PLURAL_SIS_SES_RULE = Arrays.asList(new String[] {
             "analysis",
             "basis",
             "diagnosis",
             "parenthesis",
             "prognosis",
             "synopsis",
             "thesis",
             "synthesis",
             "crisis",
         });
 
         /**
          * Nouns ending in {@code -o} preceded with a consonant, and having a plural ending in
          * {@code -os}.
          */
         private static final List<String> PLURAL_O_OS_RULE = Arrays.asList(new String[] {
             "albino",
             "alto",
             "archipelago",
             "armadillo",
             "auto",
             "basso",
             "canto",
             "casino",
             "commando",
             "contralto",
             "crescendo",
             "ditto",
             "dynamo",
             "embryo",
             "escudo",
             "euro",
             "fiasco",
             "generalissimo",
             "ghetto",
             "guano",
             "homo",
             "inferno",
             "jumbo",
             "kimono",
             "lingo",
             "lumbago",
             "macro",
             "magneto",
             "manifesto",
             "medico",
             "octavo",
             "peso",
             "photo",
             "piano",
             "portico",
             "pro",
             "quarto",
             "rhino",
             "solo",
             "soprano",
             "stylo",
             "tempo",
             "virtuoso",
             "zero",
         });
 
         /**
          * Nouns ending in {@code -y} preceded with a consonant, and having a plural ending in
          * {@code -ys}.
          */
         private static final List<String> PLURAL_Y_YS_RULE = Arrays.asList(new String[] {
             "harry",
             "tony",
             "mary",
             "germany"
         });
 
         /**
          * Words ending in {@code -s} that do not change in their plural form.
          */
         private static final List<String> UNINFLECTED_S_WORDS = Arrays.asList(new String[] {
             "acropolis",
             "aegis",
             "asbestos",
             "bathos",
             "bias",
             "bronchitis",
             "bursitis",
             "caddis",
             "cannabis",
             "canvas",
             "chaos",
             "cosmos",
             "dais",
             "digitalis",
             "epidermis",
             "ethos",
             "eyas",
             "gas",
             "glottis",
             "hubris",
             "ibis",
             "lens",
             "mantis",
             "marquis",
             "metropolis",
             "pathos",
             "pelvis",
             "polis",
             "rhinoceros",
             "sassafras",
             "trellis",
 
             "ephemeris",
             "iris",
             "clitoris",
             "chrysalis",
             "epididymis",
         });
 
         //
         // Inverse data for singular rules
         //
 
         private static final List<String> SINGULAR_MANS_MAN_RULE
                 = Lists.transform(PLURAL_MAN_MANS_RULE, mapSuffixFn("man", "mans"));
 
         private static final List<String> SINGULAR_ICES_EX_RULE
                 = Lists.transform(PLURAL_EX_ICES_RULE, mapSuffixFn("ex", "ices"));
 
         private static final List<String> SINGULAR_ICES_IX_RULE
                 = Lists.transform(PLURAL_IX_ICES_RULE, mapSuffixFn("ix", "ices"));
 
         private static final List<String> SINGULAR_A_UM_RULE
                 = Lists.transform(PLURAL_UM_A_RULE, mapSuffixFn("um", "a"));
 
         private static final List<String> SINGULAR_I_US_RULE
                 = Lists.transform(PLURAL_US_I_RULE, mapSuffixFn("us", "i"));
 
         private static final List<String> SINGULAR_A_ON_RULE
                 = Lists.transform(PLURAL_ON_A_RULE, mapSuffixFn("on", "a"));
 
         private static final List<String> SINGULAR_AE_A_RULE
                 = Lists.transform(PLURAL_A_AE_RULE, mapSuffixFn("a", "ae"));
 
         private static final List<String> SINGULAR_SES_SIS_RULE
                 = Lists.transform(PLURAL_SIS_SES_RULE, mapSuffixFn("is", "es"));
 
         private static final List<String> SINGULAR_OS_O_RULE
                 = Lists.transform(PLURAL_O_OS_RULE, mapSuffixFn("o", "os"));
 
         private static Function<String, String> mapSuffixFn(final String _old, final String _new) {
             return new Function<String, String>() {
                 @Override public String apply(String input) {
                     return input.replaceFirst(String.format("%s$", _old), _new);
                 }
             };
         }
 
     } // InflectionData
 
 }
