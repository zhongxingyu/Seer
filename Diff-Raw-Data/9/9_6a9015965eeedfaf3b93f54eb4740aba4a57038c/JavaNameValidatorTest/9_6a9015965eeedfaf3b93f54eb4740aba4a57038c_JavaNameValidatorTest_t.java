 package com.seitenbau.testing.util;
 
 import static com.seitenbau.testing.util.JavaNameValidator.isValidIdentifier;
 import static com.seitenbau.testing.util.JavaNameValidator.isValidPackageName;
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 public final class JavaNameValidatorTest
 {
 
   @Test
   public void testIsValidPackageName()
   {
     assertThat(isValidPackageName("t")).isTrue();
     assertThat(isValidPackageName("Test.TesT")).isTrue();
    assertThat(isValidPackageName("Täst")).isTrue();
    assertThat(isValidPackageName("ÜTäst")).isTrue();
     assertThat(isValidPackageName("$.TesT")).isTrue();
     assertThat(isValidPackageName("")).isTrue();
 
     assertThat(isValidPackageName(null)).isFalse();
     assertThat(isValidPackageName(".")).isFalse();
     assertThat(isValidPackageName("TesT.")).isFalse();
     assertThat(isValidPackageName(".TesT")).isFalse();
     assertThat(isValidPackageName("0.TesT")).isFalse();
     assertThat(isValidPackageName("-.TesT")).isFalse();
     assertThat(isValidPackageName("@.TesT")).isFalse();
   }
 
   @Test
   public void testIsValidIdentifier()
   {
     assertThat(isValidIdentifier("t")).isTrue();
    assertThat(isValidIdentifier("Täst")).isTrue();
    assertThat(isValidIdentifier("ÜTäst")).isTrue();
     assertThat(isValidIdentifier("$TesT")).isTrue();
     assertThat(isValidIdentifier("_TesT_")).isTrue();
 
     assertThat(isValidIdentifier(null)).isFalse();
     assertThat(isValidIdentifier("")).isFalse();
     assertThat(isValidIdentifier(".")).isFalse();
     assertThat(isValidIdentifier("TesT.")).isFalse();
     assertThat(isValidIdentifier(".TesT")).isFalse();
     assertThat(isValidIdentifier("0.TesT")).isFalse();
     assertThat(isValidIdentifier("-.TesT")).isFalse();
     assertThat(isValidIdentifier("@.TesT")).isFalse();
     assertThat(isValidIdentifier("Test.TesT")).isFalse();
   }
 }
