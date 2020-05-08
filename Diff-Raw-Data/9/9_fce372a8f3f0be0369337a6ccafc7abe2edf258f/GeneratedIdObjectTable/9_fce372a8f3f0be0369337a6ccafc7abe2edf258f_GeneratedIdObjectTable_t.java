 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables;
 
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectEntitySettings;
 import com.jtbdevelopment.e_eye_o.entities.annotations.IdObjectFieldSettings;
 import com.jtbdevelopment.e_eye_o.entities.reflection.IdObjectReflectionHelper;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.converters.AppUserOwnedObjectStringConverter;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.converters.DateTimeStringConverter;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.converters.LocalDateTimeStringConverter;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Table;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Date: 6/4/13
  * Time: 10:31 PM
  */
 public abstract class GeneratedIdObjectTable<T extends AppUserOwnedObject> extends IdObjectTable<T> {
    private static final Logger logger = LoggerFactory.getLogger(GeneratedIdObjectTable.class);
 
     protected DateTimeStringConverter dateTimeStringConverter;
 
     @Autowired
     private IdObjectReflectionHelper idObjectReflectionHelper;
 
     @Autowired
     AppUserOwnedObjectStringConverter appUserOwnedObjectStringConverter;
 
     @Autowired
     private LocalDateTimeStringConverter localDateTimeStringConverter;
 
     public GeneratedIdObjectTable(Class<T> entityType) {
         super(entityType);
     }
 
     @Override
     protected void addColumnConverters() {
         applyFunctionToFields(new Callback() {
             @Override
             public void apply(String fieldName, Method readMethod, IdObjectFieldSettings field) {
                 Class<?> returnType = readMethod.getReturnType();
                 if (DateTime.class.equals(returnType)) {
                     entityTable.setConverter(fieldName, dateTimeStringConverter);
                 } else if (LocalDateTime.class.equals(returnType)) {
                     entityTable.setConverter(fieldName, localDateTimeStringConverter);
                 } else if (AppUserOwnedObject.class.isAssignableFrom(returnType)) {
                     entityTable.setConverter(fieldName, appUserOwnedObjectStringConverter);
                     entities.addAdditionalSortableProperty(fieldName);
                 }
             }
         });
     }
 
     @Override
     protected void addGeneratedColumns() {
         super.addGeneratedColumns();
         applyFunctionToFields(new Callback() {
             @Override
             public void apply(final String fieldName, final Method readMethod, final IdObjectFieldSettings field) {
                 if (boolean.class.equals(readMethod.getReturnType())) {
                     switch (field.fieldType()) {
                         case CHECKBOX:
                         case REVERSE_CHECKBOX:
                             entityTable.addGeneratedColumn(fieldName, new Table.ColumnGenerator() {
                                 @Override
                                 public Object generateCell(Table source, Object itemId, Object columnId) {
                                     T entity = entities.getItem(itemId).getBean();
                                     try {
                                         if (IdObjectFieldSettings.DisplayFieldType.REVERSE_CHECKBOX.equals(field.fieldType())) {
                                             return new Embedded(null, (boolean) readMethod.invoke(entity) ? NOT_X : null);
                                         } else {
                                             return new Embedded(null, (boolean) readMethod.invoke(entity) ? IS_X : null);
 
                                         }
                                     } catch (IllegalAccessException | InvocationTargetException e) {
                                        logger.warn("Failure to create embedded checkbox image for " + fieldName, e);
                                         return null;
                                     }
                                 }
                             });
                             break;
                     }
                 }
             }
         });
     }
 
     protected List<String> getTableFields() {
         return Arrays.asList(entityType.getAnnotation(IdObjectEntitySettings.class).viewFieldOrder());
     }
 
     @Override
     protected String[] getVisibleColumns() {
         final List<String> fieldNames = new LinkedList<>();
         fieldNames.add("edit");
         applyFunctionToFields(new Callback() {
             @Override
             public void apply(String fieldName, Method readMethod, IdObjectFieldSettings field) {
                 fieldNames.add(fieldName);
             }
         });
         fieldNames.add("actions");
         return fieldNames.toArray(new String[fieldNames.size()]);
     }
 
     @Override
     protected String[] getColumnHeaders() {
         final List<String> fieldLabels = new LinkedList<>();
         fieldLabels.add("");
         applyFunctionToFields(new Callback() {
             @Override
             public void apply(String fieldName, Method readMethod, IdObjectFieldSettings field) {
                 fieldLabels.add(field.label());
             }
         });
         fieldLabels.add("Actions");
         return fieldLabels.toArray(new String[fieldLabels.size()]);
     }
 
     @Override
     protected Table.Align[] getColumnAlignments() {
         final List<Table.Align> alignments = new LinkedList<>();
         alignments.add(Table.Align.CENTER);
         applyFunctionToFields(new Callback() {
             @Override
             public void apply(String fieldName, Method readMethod, IdObjectFieldSettings field) {
                 alignments.add(getAlignment(field));
             }
         });
         alignments.add(Table.Align.RIGHT);
         return alignments.toArray(new Table.Align[alignments.size()]);
     }
 
     protected Table.Align getAlignment(IdObjectFieldSettings field) {
         Table.Align align = Table.Align.LEFT;
         switch (field.alignment()) {
             case CENTER:
                 align = Table.Align.CENTER;
                 break;
             case RIGHT:
                 align = Table.Align.RIGHT;
                 break;
         }
         return align;
     }
 
     private static interface Callback {
         public void apply(final String fieldName, final Method readMethod, final IdObjectFieldSettings field);
     }
 
     private void applyFunctionToFields(final Callback callback) {
         Map<String, Method> fields = idObjectReflectionHelper.getAllGetMethods(entityType);
         for (String fieldName : getTableFields()) {
             Method readMethod = fields.get(fieldName);
             if (readMethod == null) {
                 continue;
             }
             IdObjectFieldSettings field = readMethod.getAnnotation(IdObjectFieldSettings.class);
             if (field == null) {
                 continue;
             }
             callback.apply(fieldName, readMethod, field);
         }
     }
 
 }
