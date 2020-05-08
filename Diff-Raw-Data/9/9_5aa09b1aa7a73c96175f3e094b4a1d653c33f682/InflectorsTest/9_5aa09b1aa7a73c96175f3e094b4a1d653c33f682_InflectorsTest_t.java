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
 
 import static org.junit.Assert.*;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * Tests for the {@link Inflectors} class.
  *
  * @author Ernesto García
  */
 public class InflectorsTest {
 
     public InflectorsTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     /**
      * Tests {@link Inflectors#pluralOf(String)} against all test cases.
      */
     @Test
     public void test_pluralOf() {
         for (String[] testCase : TEST_CASES) {
             String word = testCase[0];
             String expected = testCase[1];
             String actual = Inflectors.pluralOf(word);
             assertInflection(word, expected, actual, "plural");
         }
     }
 
     /**
      * Tests {@link Inflectors#singularOf(String)} against all test cases.
      */
     @Test
     public void test_singularOf() {
         for (String[] testCase : TEST_CASES) {
             String word = testCase[1];
             String expected = testCase[0];
             String actual = Inflectors.singularOf(word);
             assertInflection(word, expected, actual, "singular");
         }
     }
 
     /**
      * Tests that {@link RuleBasedInflector#apply(String)} returns its result matching the letter
      * casing of the word originally provided.  That is, if the user provides an ALL-CAPS word, the
      * result should be given with all letters in UPPER-CASE form.  If the user provides a
      * Capitalized word, the result should be capitalized as well.
      */
     @Test
     public void test_matchCase() {
         for (String[] testCase : TEST_CASES) {
             String singular = capitalize(testCase[0]);
             String plural = capitalize(testCase[1]);
             testMatchCase(singular, plural);
             testMatchCase(singular.toUpperCase(), plural.toUpperCase());
         }
     }
 
     /**
      * Tests that {@link Inflectors#forFunction(Function)} does not create a new {@link Inflector}
      * if the provided argument is already an {@code Inflector}.
      */
     @Test
     public void test_forFunction() {
         final Inflector[] INFLECTOR_LIST = {
             Inflectors.getPluralInflector(),
             Inflectors.getSingularInflector(),
             Inflectors.IDENTITY,
         };
 
         for (Inflector i : INFLECTOR_LIST) {
             assertSame(i, Inflectors.forFunction(i));
         }
     }
 
     /**
      * Tests {@link Inflectors#inflectAll(Inflector, Iterable)}.
      */
     @Test
     public void test_inflectAll() {
         final List<String> inflected = Inflectors.inflectAll(
                 Inflectors.getPluralInflector(), TEST_KEYS);
         assertEquals(Arrays.asList(TEST_VALUES), inflected);
     }
 
     /**
      * Tests {@link Inflectors#inflectionMap(Inflector, Iterable)}.
      */
     @Test
     public void test_inflectionMap() {
         final Map<String, String> inflectionMap
                 = Inflectors.inflectionMap(Inflectors.getPluralInflector(), TEST_KEYS);
         assertEquals(
                 Sets.newHashSet(TEST_VALUES),
                 Sets.newHashSet(inflectionMap.values()));
         assertEquals(TEST_MAP, inflectionMap);
     }
 
     //
     // Internal helper methods
     //
 
     private static void assertInflection(String word, String expected, String actual, String type) {
         final String errorMessage = String.format(
                 "%s of \"%s\" should be \"%s\" instead of \"%s\"",
                 type, word, expected, actual);
         assertEquals(errorMessage, expected, actual);
     }
 
     private static String capitalize(String word) {
         return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
     }
 
     private static void testMatchCase(String singular, String plural) {
         final String computedSingular = Inflectors.singularOf(plural);
         final String computedPlural = Inflectors.pluralOf(singular);
         assertEquals(singular, computedSingular);
         assertEquals(plural, computedPlural);
     }
 
     //
     // Test cases used in the tests above
     //
 
     private static final String[][] TEST_CASES = {
         // irregulars
         { "child", "children" },
         { "ox", "oxen" },
         { "barefoot", "barefeet" },
         { "tooth", "teeth" },
         { "goose", "geese" },
         { "mongoose", "mongooses" },
         { "numen", "numina" },
         { "atman", "atmas" },
         { "quiz", "quizzes" },
 
         // phrases
         { "bit of salt", "bits of salt" },
         { "life-after-death", "lives-after-death" },
         { "degree Fahrenheit", "degrees Fahrenheit" },
         { "degree-Celcius", "degrees-Celcius" },
         { "Zimbabwean Dollar (1980-2008)", "Zimbabwean Dollars (1980-2008)" },
 
         // singulars ending with -s
         { "penis", "penises" },
         { "testis", "testes" },
         { "atlas", "atlases" },
         { "mythos", "mythoi" },
 
         // singulars ending with -us
         { "octopus", "octopuses" },
         { "corpus", "corpuses" },
         { "opus", "opuses" },
         { "genus", "genera" },
         { "gladiolus", "gladioli" },
         { "stimulus", "stimuli" },
 
         // uninflected nouns
         { "series", "series" },
         { "canvas", "canvas" },
         { "biceps", "biceps" },
         { "sheep", "sheep" },
         { "deer", "deer" },
         { "spacecraft", "spacecraft" },
 
         // standard plural rule
         { "pound", "pounds" },
         { "inflection", "inflections" },
         { "connector", "connectors" },
         { "genie", "genies" },
         { "ganglion", "ganglions" },
         { "occiput", "occiputs" },
         { "brother", "brothers" },
         { "cow", "cows" },
         { "prima donna", "prima donnas" },
         { "eye", "eyes" },
         { "judge", "judges" },
 
         // -y endings
         { "story", "stories" },
         { "boy", "boys" },
         { "trilby", "trilbys" },
         { "money", "monies" },
         { "soliloquy", "soliloquies" },
         { "harry", "harrys" },
         { "germany", "germanys" },
 
         // -an endings
         { "human", "humans" },
         { "German", "Germans" },
         { "woman", "women" },
 
         // -ix / -ex endings
         { "matrix", "matrices" },
         { "codex", "codices" },
         { "radix", "radices" },
         { "index", "indexes" },
         { "suffix", "suffixes" },
         { "prefix", "prefixes" },
         { "annex", "annexes" },
 
         // -x and -z endings
         { "ax", "axes" },
         { "box", "boxes" },
         { "buzz", "buzzes" },
 
         // -f and -fe endings
         { "hoof", "hoofs" },
         { "beef", "beefs" },
         { "cliff", "cliffs" },
         { "turf", "turfs" },
         { "dwarf", "dwarves" },
         { "knife", "knives" },
         { "wife", "wives" },
         { "wolf", "wolves" },
         { "half", "halves" },
         { "elf", "elves" },
         { "leaf", "leaves" },
         { "meetloaf", "meetloaves" },
 
         // -o endings
         { "hero", "heroes" },
         { "studio", "studios" },
         { "soprano", "sopranos" },
         { "flamingo", "flamingoes" },
         { "graffito", "graffiti" },
         { "photo", "photos" },
         { "zero", "zeros" },
         { "euro", "euros" },
         { "piano", "pianos" },
 
         // -sh, -ch and -ss endings
         { "mesh", "meshes" },
         { "glass", "glasses" },
         { "church", "churches" },
 
         // -th endings
        { "cloth", "cloths" },
         { "death", "deaths" },
         { "bath", "baths" },
         { "mouth", "mouths" },
 
         // minor irregular suffixes rules
         { "mouse", "mice" },
         { "louse", "lice" },
         { "stratum", "strata" },
         { "forum", "forums" },
         { "criterion", "criteria" },
         { "vertebra", "vertebrae" },
         { "sphinx", "sphinges" },
 
         // The strange case of person/people
         { "person", "people" },
         { "people", "peoples" },
 
         // words with the plural ending in  -ses
         { "house", "houses" },
         { "blouse", "blouses" },
         { "bruise", "bruises" },
         { "crisis", "crises" },
     };
 
     private static final String[] TEST_KEYS = extractColumn(TEST_CASES, 0);
 
     private static final String[] TEST_VALUES = extractColumn(TEST_CASES, 1);
 
     private static final Map<String, String> TEST_MAP = tableToMap(TEST_CASES);
 
     private static Map<String, String> tableToMap(String[][] table) {
         final Map<String, String> result = Maps.newHashMapWithExpectedSize(table.length);
         for (int i = 0; i < table.length; ++i) {
             result.put(table[i][0], table[i][1]);
         }
         return Collections.unmodifiableMap(result);
     }
 
     private static String[] extractColumn(String[][] table, int col) {
         final String[] result = new String[table.length];
         for (int i = 0; i < table.length; ++i) {
             result[i] = table[i][col];
         }
         return result;
     }
 
 }
