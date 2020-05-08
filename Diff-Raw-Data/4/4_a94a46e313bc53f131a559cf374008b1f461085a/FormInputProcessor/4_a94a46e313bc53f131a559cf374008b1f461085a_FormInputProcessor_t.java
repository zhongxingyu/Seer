 package com.psddev.dari.db;
 
 import com.psddev.dari.util.HtmlWriter;
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.TypeReference;
 
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 /** Processes individual inputs written with {@link FormWriter}. */
 public interface FormInputProcessor {
 
     /** Returns an HTML string for displaying an input. */
     public String display(String inputId, String inputName, ObjectField field, Object value);
 
     /** Returns an updated value after processing an input. */
     public Object update(String inputName, ObjectField field, HttpServletRequest request);
 
     /**
      * Default {@link FormInputProcessor} that uses JSON to handle unknown
      * content.
      */
     public static class Default extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             writer.start("span", "class", "json");
                 writer.start("textarea", "id", inputId, "name", inputName);
                     writer.html(ObjectUtils.toJson(value, true));
                 writer.end();
             writer.end();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.fromJson(param(String.class, request, inputName));
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#BOOLEAN_TYPE}. */
     public static class ForBoolean extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             writer.tag("input",
                     "type", "checkbox",
                     "id", inputId,
                     "name", inputName,
                     "value", "true",
                     "checked", Boolean.TRUE.equals(value) ? "checked" : null);
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(boolean.class, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#DATE_TYPE}. */
     public static class ForDate extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             writer.tag("input",
                     "type", "text",
                     "class", "date",
                     "id", inputId,
                     "name", inputName,
                    "value", value);
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(Date.class, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#LIST_TYPE}. */
     public static class ForListRecord extends AbstractFormInputProcessor {
 
         private static final ForRecord FOR_RECORD = new ForRecord();
         private static final TypeReference<List<UUID>> LIST_UUID_TYPE = new TypeReference<List<UUID>>() { };
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             List<?> valueList = ObjectUtils.to(List.class, value);
 
             writer.start("ol", "class", "repeatable");
                 if (valueList != null) {
                     for (Object item : valueList) {
                         writer.start("li", "class", "repeatable-item");
                             FOR_RECORD.doDisplay(null, inputName, field, item, writer);
                         writer.end();
                     }
                 }
 
                 writer.start("li", "class", "repeatable-template");
                     FOR_RECORD.doDisplay(null, inputName, field, null, writer);
                 writer.end();
             writer.end();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(LIST_UUID_TYPE, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#LIST_TYPE}. */
     public static class ForListText extends AbstractFormInputProcessor {
 
         private static final ForText FOR_TEXT = new ForText();
         private static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<List<String>>() { };
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             List<String> valueList = ObjectUtils.to(LIST_STRING_TYPE, value);
 
             writer.start("ol", "class", "repeatable");
                 if (valueList != null) {
                     for (String item : valueList) {
                         writer.start("li", "class", "repeatable-item");
                             FOR_TEXT.doDisplay(null, inputName, field, item, writer);
                         writer.end();
                     }
                 }
 
                 writer.start("li", "class", "repeatable-template");
                     FOR_TEXT.doDisplay(null, inputName, field, null, writer);
                 writer.end();
             writer.end();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(LIST_STRING_TYPE, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#RECORD_TYPE}. */
     public static class ForRecord extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             State valueState = State.getInstance(value);
             StringBuilder typeIdsBuilder = new StringBuilder();
 
             for (ObjectType type : field.getTypes()) {
                 typeIdsBuilder.append(type.getId());
                 break;
             }
 
             writer.start("div", "class", "objectId-label");
                 if (valueState != null) {
                     writer.html(valueState.getLabel());
                 }
             writer.end();
 
             writer.tag("input",
                     "type", "text",
                     "class", "objectId",
                     "data-type-ids", typeIdsBuilder,
                     "id", inputId,
                     "name", inputName,
                     "value", valueState != null ? valueState.getId() : null);
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(UUID.class, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#SET_TYPE}. */
     public static class ForSetRecord extends AbstractFormInputProcessor {
 
         private static final ForRecord FOR_RECORD = new ForRecord();
         private static final TypeReference<Set<UUID>> SET_UUID_TYPE = new TypeReference<Set<UUID>>() { };
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             Set<?> valueSet = ObjectUtils.to(Set.class, value);
 
             writer.start("ul", "class", "repeatable");
                 if (valueSet != null) {
                     for (Object item : valueSet) {
                         writer.start("li", "class", "repeatable-item");
                             FOR_RECORD.doDisplay(null, inputName, field, item, writer);
                         writer.end();
                     }
                 }
 
                 writer.start("li", "class", "repeatable-template");
                     FOR_RECORD.doDisplay(null, inputName, field, null, writer);
                 writer.end();
             writer.end();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(SET_UUID_TYPE, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#SET_TYPE}. */
     public static class ForSetText extends AbstractFormInputProcessor {
 
         private static final ForText FOR_TEXT = new ForText();
         private static final TypeReference<Set<String>> SET_STRING_TYPE = new TypeReference<Set<String>>() { };
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             Set<String> valueSet = ObjectUtils.to(SET_STRING_TYPE, value);
 
             writer.start("ul", "class", "repeatable");
                 if (valueSet != null) {
                     for (String item : valueSet) {
                         writer.start("li", "class", "repeatable-item");
                             FOR_TEXT.doDisplay(null, inputName, field, item, writer);
                         writer.end();
                     }
                 }
 
                 writer.start("li", "class", "repeatable-template");
                     FOR_TEXT.doDisplay(null, inputName, field, null, writer);
                 writer.end();
             writer.end();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(SET_STRING_TYPE, request, inputName);
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#TEXT_TYPE}. */
     public static class ForText extends AbstractFormInputProcessor {
 
         protected String createPlaceholder(ObjectField field) {
             return field.isRequired() ? "(Required)" : null;
         }
 
         protected Map<String, String> createExtraAttributes(ObjectField field) {
             return null;
         }
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, Object value, HtmlWriter writer) throws IOException {
             String placeholder = createPlaceholder(field);
             Map<String, String> extraAttributes = createExtraAttributes(field);
             Set<ObjectField.Value> possibleValues = field.getValues();
 
             if (possibleValues == null || possibleValues.isEmpty()) {
                 writer.tag("input",
                         "type", "text",
                         "id", inputId,
                         "name", inputName,
                         "value", value,
                         "placeholder", placeholder,
                         extraAttributes);
 
             } else {
                 writer.start("select",
                         "id", inputId,
                         "name", inputName,
                         extraAttributes);
 
                     writer.start("option",
                             "value", "",
                             "class", "placeholder");
                         writer.html(placeholder);
                     writer.end();
 
                     for (ObjectField.Value v : possibleValues) {
                         String vv = v.getValue();
                         writer.start("option",
                                 "value", vv,
                                 "selected", ObjectUtils.equals(vv, value));
                             writer.html(v.getLabel());
                         writer.end();
                     }
 
                 writer.end();
             }
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return param(String.class, request, inputName);
         }
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link AbstractFormInputProcessor} instead. */
     @Deprecated
     public static abstract class Abstract extends AbstractFormInputProcessor {
     }
 }
