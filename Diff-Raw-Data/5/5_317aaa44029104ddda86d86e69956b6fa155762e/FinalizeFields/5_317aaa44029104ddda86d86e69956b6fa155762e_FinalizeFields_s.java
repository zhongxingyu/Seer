 /*
  * Copyright (c) 2010 by Guido Steinacker
  */
 
 package de.steinacker.jcg.transform.type;
 
 import de.steinacker.jcg.Context;
 import de.steinacker.jcg.model.Field;
 import de.steinacker.jcg.model.FieldBuilder;
 import de.steinacker.jcg.model.FieldModifier;
 import org.apache.log4j.Logger;
 
 import java.util.EnumSet;
 import java.util.Set;
 
 /**
 * A ModelTransformer which makes all fields 'final'.
  * <p/>
 * This Transformer optionally supports predicates to decide, whether a type or a field
 * will be transformed or not.
  *
  * @author Guido Steinacker
  * @version %version: 28 %
  */
 public final class FinalizeFields extends AbstractFieldTransformer {
 
     private final static Logger LOG = Logger.getLogger(FinalizeFields.class);
 
     @Override
     public String getName() {
         return "FinalizeFields";
     }
 
     @Override
     protected Field transformField(final Field field, final Context context) {
         final Field finalField;
         final Set<FieldModifier> modifiers = field.getModifiers();
         if (!modifiers.contains(FieldModifier.FINAL)) {
             final Set<FieldModifier> newModifiers = modifiers.isEmpty()
                     ? EnumSet.noneOf(FieldModifier.class)
                     : EnumSet.copyOf(modifiers);
             newModifiers.add(FieldModifier.FINAL);
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
