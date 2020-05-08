 /**
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 package kramer;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.*;
 
 
 public class PropertyFileReader {
    private static final String BAD_KEY = "Property ({0}) is not correct format. Example formats: form1.class, form2.fieldname";
     private FieldSizeResolver fieldSizeResolver;
 
     public List<FormFields> read(List<File> files) {
         Properties properties = new Properties();
         try {
             for (File file : files) {
                 properties.load(new FileInputStream(file));
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return read(properties);
     }
 
     public List<FormFields> read(File file) {
         return read(Arrays.asList(file));
     }
 
     public List<FormFields> read(Properties properties) {
         Map<String, FormFields> readForms = new HashMap<String, FormFields>();
         List<FormFields> forms = new ArrayList<FormFields>();
         if (!properties.keySet().isEmpty()) {
             for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                 validateKey(entry.getKey());
                 FormFields form = getFormFields(readForms, getFormId((String) entry.getKey()));
                 if (isField((String) entry.getKey())) {
                     form.field(getField((String) entry.getKey()), fieldSizeResolver.resolveLength((String) entry.getValue()));
                 } else {
                     form.setFormClass(loadClass((String) entry.getValue()));
                 }
             }
         }
         forms.addAll(readForms.values());
         return forms;
     }
 
     private void validateKey(Object key) {
         if (key.toString().split("\\.").length != 2) {
             throw new IllegalArgumentException(MessageFormat.format(BAD_KEY, key.toString()));
         }
     }
 
     private String getField(String property) {
         return property.split("\\.")[1];
     }
 
     private boolean isField(String property) {
         return !property.endsWith(".class");
     }
 
     private FormFields getFormFields(Map<String, FormFields> readForms, String formId) {
         if (readForms.containsKey(formId)) {
             return readForms.get(formId);
         }
         FormFields fields = new FormFields();
         readForms.put(formId, fields);
         return fields;
     }
 
     private String getFormId(String property) {
         return property.split("\\.")[0];
     }
 
     private Class loadClass(String className) {
         try {
             return Thread.currentThread().getContextClassLoader().loadClass(className);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     public void setFieldSizeResolver(FieldSizeResolver fieldSizeResolver) {
         this.fieldSizeResolver = fieldSizeResolver;
     }
 }
