 package org.jtalks.common.model.permissions;
 
 import com.google.common.collect.Lists;
 import ru.javatalks.utils.general.Assert;
 
 import javax.annotation.Nonnull;
 import java.util.List;
 
 /**
  * General purpose permissions like in {@link org.springframework.security.acls.domain.BasePermission}.
  *
  * @author stanislav bashkirtsev
  */
 public enum GeneralPermission implements JtalksPermission {
     /**
      * The ability of Sids to have all the rights, to perform any action on the object.
      */
    CREATE_TOPICS("1000", "ADMIN"),
     /**
      * The ability of the Sids to change the object identity.
      */
    VIEW_TOPICS("10", "WRITE");
 
     private final String name;
     private final int mask;
 
     /**
      * Constructs the whole object without symbol.
      *
      * @param mask a bit mask that represents the permission, can be negative only for restrictions (look at the class
      *             description). The integer representation of it is saved to the ACL tables of Spring Security.
      * @param name a textual representation of the permission (usually the same as the constant name), though the
      *             restriction usually starts with the 'RESTRICTION_' word
      */
     GeneralPermission(int mask, @Nonnull String name) {
         this.mask = mask;
         throwIfNameNotValid(name);
         this.name = name;
     }
 
     /**
      * Takes a string bit mask.
      *
      * @param mask a bit mask that represents the permission. It's parsed into integer and saved into the ACL tables of
      *             Spring Security.
      * @param name a textual representation of the permission (usually the same as the constant name)
      * @throws NumberFormatException look at {@link Integer#parseInt(String, int)} for details on this as this method is
      *                               used underneath
      * @see GeneralPermission#GeneralPermission(int, String)
      * @see org.springframework.security.acls.domain.BasePermission
      */
     GeneralPermission(@Nonnull String mask, @Nonnull String name) {
         throwIfNameNotValid(name);
         this.mask = Integer.parseInt(mask, 2);
         this.name = name;
     }
 
     /**
      * Gets the human readable textual representation of the restriction (usually the same as the constant name).
      *
      * @return the human readable textual representation of the restriction (usually the same as the constant name)
      */
     @Override
     public String getName() {
         return name;
     }
 
     private void throwIfNameNotValid(String name) {
         Assert.throwIfNull(name, "The name can't be null");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int getMask() {
         return mask;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String getPattern() {
         return null;
     }
 
     public static GeneralPermission findByMask(int mask) {
         for (GeneralPermission nextPermission : values()) {
             if (mask == nextPermission.getMask()) {
                 return nextPermission;
             }
         }
         return null;
     }
 
     public static List<GeneralPermission> getAllAsList() {
         return Lists.newArrayList(values());
     }
 
 }
