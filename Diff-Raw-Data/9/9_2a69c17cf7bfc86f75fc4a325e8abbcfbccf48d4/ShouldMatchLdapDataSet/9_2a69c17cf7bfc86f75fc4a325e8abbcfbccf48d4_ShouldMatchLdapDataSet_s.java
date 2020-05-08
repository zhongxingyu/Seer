 package com.zimory.ldapunit.core;
 
 import java.lang.annotation.Documented;
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Inherited;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Indicates that the test should use a data set to match against the contents of the LDAP server. To be used in
  * conjunction with {@link LdapWatcher}:
  *
  * <pre>
  *     @{@link org.junit.Rule}
 *     public {@link org.junit.rules.TestRule} ldapWatcher = new LdapWatcher(new {@link javax.inject.Provider}&lt;{@link com.unboundid.ldap.listener.InMemoryDirectoryServer}&gt;() {
  *        {@literal @}Override
  *         public InMemoryDirectoryServer get() {
  *             return ldapServer;
  *         }
  *     });
  * </pre>
  * Note that it's also possible to annotate the {@link org.junit.After} methods: in this case, the matching will be done
  * after that of each {@link org.junit.Test} method.
  */
 @Inherited
 @Documented
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface ShouldMatchLdapDataSet {
 
     /**
      * The relative path to the LDIF (extension can be dropped) to use for matching (will be looked up from
      * {@link LdapWatcher#LDIF_DIR}). If not set, will be constructed from the names of the test class and method, e.g.:
      * {@code /TestClass/expected-testMethodName}.
      * @return the relative path to the LDIF file to use for matching
      */
     String value() default "";
 
 }
