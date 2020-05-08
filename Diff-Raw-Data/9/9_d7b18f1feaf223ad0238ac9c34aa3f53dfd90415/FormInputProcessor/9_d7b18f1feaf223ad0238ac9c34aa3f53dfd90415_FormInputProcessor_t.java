 package com.psddev.dari.db;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.psddev.dari.util.HtmlWriter;
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.TypeReference;
 
 /**
  * Processes individual inputs written with {@link FormWriter}.
  */
 public interface FormInputProcessor {
 
     /** Returns an HTML string for displaying an input. */
     public String display(String inputId, String inputName, ObjectField field, State state);
 
     /** Returns an updated value after processing an input. */
     public Object update(String inputName, ObjectField field, HttpServletRequest request);
 
     /**
      * Default {@link FormInputProcessor} that uses JSON to handle unknown
      * content.
      */
     public static class Default extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             writer.writeStart("span", "class", "json");
                 writer.writeStart("textarea", "id", inputId, "name", inputName);
                     writer.writeHtml(ObjectUtils.toJson(value, true));
                 writer.writeEnd();
             writer.writeEnd();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.fromJson(ObjectUtils.to(String.class, request.getParameter(inputName)));
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#BOOLEAN_TYPE}. */
     public static class ForBoolean extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             writer.writeTag("input",
                     "type", "checkbox",
                     "id", inputId,
                     "name", inputName,
                     "value", "true",
                     "checked", Boolean.TRUE.equals(value) ? "checked" : null);
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.to(boolean.class, request.getParameter(inputName));
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#DATE_TYPE}. */
     public static class ForDate extends AbstractFormInputProcessor {
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             writer.writeTag("input",
                     "type", "text",
                     "class", "date",
                     "id", inputId,
                     "name", inputName,
                     "value", value);
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.to(Date.class, request.getParameter(inputName));
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#LIST_TYPE}. */
     public static class ForListRecord extends AbstractFormInputProcessor {
 
         private FormInputProcessor.ForRecord forRecord;
 
         public ForListRecord(FormWriter delegateWriter) {
             this.forRecord = new FormInputProcessor.ForRecord(delegateWriter);
         }
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             List<?> valueList = ObjectUtils.to(List.class, value);
 
             writer.writeStart("ol", "class", "repeatable");
                 if (valueList != null) {
                     for (Object item : valueList) {
                         writer.writeStart("li", "class", "repeatable-item");
                             forRecord.doDisplay(null, inputName, field, state, item, writer);
                         writer.writeEnd();
                     }
                 }
 
                 writer.writeStart("li", "class", "repeatable-template");
                     forRecord.doDisplay(null, inputName, field, null, null, writer);
                 writer.writeEnd();
             writer.writeEnd();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             if (field.isEmbedded()) {
 
                 ObjectType type = null;
                 for (ObjectType ot : field.getTypes()) {
                     type = ot;
                     break;
                 }
 
                 List<Object> objects = new ArrayList<Object>();
                 if (type != null) {
 
                     List<UUID> objectIds = ObjectUtils.to(new TypeReference<List<UUID>>() {}, request.getParameterValues(inputName));
                     for (UUID objectId : objectIds) {
                         if (objectId != null) {
                             Object object = forRecord.update(objectId.toString(), field, request);
 
                             if (object != null) {
                                 objects.add(object);
                             }
                         }
                     }
                 }
 
                 return objects;
 
             } else {
                 return ObjectUtils.to(new TypeReference<List<UUID>>() {}, request.getParameterValues(inputName));
             }
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#LIST_TYPE}. */
     public static class ForListText extends AbstractFormInputProcessor {
 
         private static final FormInputProcessor.ForText FOR_TEXT = new FormInputProcessor.ForText();
         private static final TypeReference<List<String>> LIST_STRING_TYPE = new TypeReference<List<String>>() { };
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             List<String> valueList = ObjectUtils.to(LIST_STRING_TYPE, value);
 
             writer.writeStart("ol", "class", "repeatable");
                 if (valueList != null) {
                     for (String item : valueList) {
                         writer.writeStart("li", "class", "repeatable-item");
                             FOR_TEXT.doDisplay(null, inputName, field, state, item, writer);
                         writer.writeEnd();
                     }
                 }
 
                 writer.writeStart("li", "class", "repeatable-template");
                     FOR_TEXT.doDisplay(null, inputName, field, null, null, writer);
                 writer.writeEnd();
             writer.writeEnd();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.to(new TypeReference<List<String>>() {}, request.getParameterValues(inputName));
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#RECORD_TYPE}. */
     public static class ForRecord extends AbstractFormInputProcessor {
 
         private FormWriter delegateWriter;
 
        /** Use {@link #ForRecord(FormWriter)} instead */
        @Deprecated
         public ForRecord() {
         }
 
         public ForRecord(FormWriter delegateWriter) {
             this.delegateWriter = delegateWriter;
         }
 
         public FormWriter getDelegateWriter() {
             return delegateWriter;
         }
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
 
             State valueState = State.getInstance(value);
 
             if (field.isEmbedded()) {
                 ObjectType type = null;
                 for (ObjectType ot : field.getTypes()) {
                     type = ot;
                     break;
                 }
 
                 if (type != null) {
                     FormWriter formWriter = getDelegateWriter();
                     Writer existingDelegate = formWriter.getDelegate();
 
                     try {
                         formWriter.setDelegate(writer);
 
                         List<String> fields = new ArrayList<String>();
                         for (ObjectField typeField : type.getFields()) {
                             fields.add(typeField.getInternalName());
                         }
 
                         writer.writeTag("input",
                                 "type", "hidden",
                                 "name", inputName,
                                 "value", valueState != null ? valueState.getId() : null);
 
                         if (valueState == null) {
                             Object object = Database.Static.getDefault().getEnvironment().createObject(type.getId(), null);
                             valueState = State.getInstance(object);
                         }
                         formWriter.inputs(valueState, fields.toArray(new String[fields.size()]));
 
                     } finally {
                         formWriter.setDelegate(existingDelegate);
                     }
                 }
 
             } else {
                 StringBuilder typeIdsBuilder = new StringBuilder();
 
                 for (ObjectType type : field.getTypes()) {
                     typeIdsBuilder.append(type.getId());
                     break;
                 }
 
                 writer.writeStart("div", "class", "objectId-label");
                     if (valueState != null) {
                         writer.writeHtml(valueState.getLabel());
                     }
                 writer.writeEnd();
 
                 writer.writeTag("input",
                         "type", "text",
                         "class", "objectId",
                         "data-type-ids", typeIdsBuilder,
                         "id", inputId,
                         "name", inputName,
                         "value", valueState != null ? valueState.getId() : null);
             }
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             if (field.isEmbedded()) {
 
                 ObjectType type = null;
                 for (ObjectType ot : field.getTypes()) {
                     type = ot;
                     break;
                 }
 
                 if (type != null) {
 
                     FormWriter formWriter = getDelegateWriter();
 
                     UUID objectId;
                     if (inputName.indexOf('/') < 0) {
                         objectId = ObjectUtils.to(UUID.class, inputName);
                     } else {
                         objectId = ObjectUtils.to(UUID.class, request.getParameter(inputName));
                     }
 
                     if (objectId != null) {
                         Object object = Database.Static.getDefault().getEnvironment().createObject(type.getId(), objectId);
                         State objectState = State.getInstance(object);
 
                         for (ObjectField typeField : type.getFields()) {
                             formWriter.update(objectState, request, typeField.getInternalName());
                         }
 
                         return object;
                     }
                 }
 
                 return null;
 
             } else {
                 return ObjectUtils.to(UUID.class, request.getParameter(inputName));
             }
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#SET_TYPE}. */
     public static class ForSetRecord extends AbstractFormInputProcessor {
 
         private FormInputProcessor.ForRecord forRecord;
 
         public ForSetRecord(FormWriter delegateWriter) {
             this.forRecord = new FormInputProcessor.ForRecord(delegateWriter);
         }
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             Set<?> valueSet = ObjectUtils.to(Set.class, value);
 
             writer.writeStart("ul", "class", "repeatable");
                 if (valueSet != null) {
                     for (Object item : valueSet) {
                         writer.writeStart("li", "class", "repeatable-item");
                         forRecord.doDisplay(null, inputName, field, state, item, writer);
                         writer.writeEnd();
                     }
                 }
 
                 writer.writeStart("li", "class", "repeatable-template");
                 forRecord.doDisplay(null, inputName, field, null, null, writer);
                 writer.writeEnd();
             writer.writeEnd();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.to(new TypeReference<Set<UUID>>() {}, request.getParameterValues(inputName));
         }
     }
 
     /** {@link FormInputProcessor} for {@link ObjectField#SET_TYPE}. */
     public static class ForSetText extends AbstractFormInputProcessor {
 
         private static final FormInputProcessor.ForText FOR_TEXT = new FormInputProcessor.ForText();
         private static final TypeReference<Set<String>> SET_STRING_TYPE = new TypeReference<Set<String>>() { };
 
         @Override
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             Set<String> valueSet = ObjectUtils.to(SET_STRING_TYPE, value);
 
             writer.writeStart("ul", "class", "repeatable");
                 if (valueSet != null) {
                     for (String item : valueSet) {
                         writer.writeStart("li", "class", "repeatable-item");
                             FOR_TEXT.doDisplay(null, inputName, field, state, item, writer);
                         writer.writeEnd();
                     }
                 }
 
                 writer.writeStart("li", "class", "repeatable-template");
                     FOR_TEXT.doDisplay(null, inputName, field, null, null, writer);
                 writer.writeEnd();
             writer.writeEnd();
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.to(SET_STRING_TYPE, request.getParameterValues(inputName));
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
         protected void doDisplay(String inputId, String inputName, ObjectField field, State state, Object value, HtmlWriter writer) throws IOException {
             String placeholder = createPlaceholder(field);
             Map<String, String> extraAttributes = createExtraAttributes(field);
             Set<ObjectField.Value> possibleValues = field.getValues();
 
             if (possibleValues == null || possibleValues.isEmpty()) {
 
                 boolean containsNewLine = value != null && value.toString().contains(System.getProperty("line.separator"));
 
                 if (containsNewLine) {
                     writer.writeStart("textarea",
                             "id", inputId,
                             "name", inputName,
                             "placeholder", placeholder,
                             extraAttributes);
                         writer.writeHtml(value);
                     writer.writeEnd();
 
                 } else {
                     writer.writeTag("input",
                             "type", "text",
                             "id", inputId,
                             "name", inputName,
                             "value", value,
                             "placeholder", placeholder,
                             extraAttributes);
                 }
 
             } else {
                 writer.writeStart("select",
                         "id", inputId,
                         "name", inputName,
                         extraAttributes);
 
                     writer.writeStart("option",
                             "value", "",
                             "class", "placeholder");
                         writer.writeHtml(placeholder);
                     writer.writeEnd();
 
                     for (ObjectField.Value v : possibleValues) {
                         String vv = v.getValue();
                         writer.writeStart("option",
                                 "value", vv,
                                 "selected", ObjectUtils.equals(vv, value) ? "selected" : null);
                             writer.writeHtml(v.getLabel());
                         writer.writeEnd();
                     }
 
                 writer.writeEnd();
             }
         }
 
         @Override
         public Object update(String inputName, ObjectField field, HttpServletRequest request) {
             return ObjectUtils.to(String.class, request.getParameter(inputName));
         }
     }
 }
