 /*
  * Copyright (c) 2010 by Guido Steinacker
  */
 
 package de.steinacker.jcg.transform.type;
 
 import de.steinacker.jcg.Context;
 import de.steinacker.jcg.model.Field;
 import de.steinacker.jcg.model.FieldBuilder;
 import de.steinacker.jcg.model.FieldModifier;
 
 import java.util.EnumSet;
 import java.util.Set;
 
 /**
 * A ModelTransformer which makes all fields 'private'.
  * <p/>
 * This Transformer optionally supports predicates to decide, whether a type or a field
 * will be transformed or not.
  *
  * @author Guido Steinacker
  * @version %version: 28 %
  */
 public final class PrivatizeFields extends AbstractFieldTransformer implements TypeTransformer {
 
 
     @Override
     public String getName() {
         return "PrivatizeFields";
     }
 
     @Override
     protected Field transformField(final Field field, final Context context) {
         final Field finalField;
         final Set<FieldModifier> modifiers = field.getModifiers().isEmpty()
                 ? EnumSet.noneOf(FieldModifier.class)
                 : EnumSet.copyOf(field.getModifiers());
         modifiers.remove(FieldModifier.PROTECTED);
         modifiers.remove(FieldModifier.PUBLIC);        
         if (!modifiers.contains(FieldModifier.PRIVATE)) {
             final Set<FieldModifier> newModifiers = modifiers.isEmpty()
                     ? EnumSet.noneOf(FieldModifier.class)
                     : EnumSet.copyOf(modifiers);
             newModifiers.add(FieldModifier.PRIVATE);
             finalField = new FieldBuilder(field).setModifiers(newModifiers).toField();
         } else {
             finalField = field;
         }
         return finalField;
     }
 
     @Override
     public String toString() {
         return getName();
     }
 }
