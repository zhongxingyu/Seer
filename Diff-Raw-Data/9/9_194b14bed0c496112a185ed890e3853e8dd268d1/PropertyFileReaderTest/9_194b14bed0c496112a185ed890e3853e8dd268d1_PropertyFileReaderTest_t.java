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
 
 import junit.framework.TestCase;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.util.List;
 import java.util.Properties;
 
 
 public class PropertyFileReaderTest extends TestCase {
     private PropertyFileReader reader;
     private Properties props;
     private FieldSizeResolver resolver;
 
     protected void setUp() throws Exception {
         super.setUp();
         resolver = mock(FieldSizeResolver.class);
 
         props = new Properties();
         reader = new PropertyFileReader();
         reader.setFieldSizeResolver(resolver);
     }
 
     public void test_propretyIsMalformed() {
         props.setProperty("form", "2");
 
         try {
             reader.read(props);
             fail();
         } catch (IllegalArgumentException err) {
            assertEquals("Property (form) is not a correct property format. Example formats: form1.class, form2.fieldname", err.getMessage());
         }
     }
 
     public void test_read_multipleForms() {
         props.setProperty("form.class", "java.lang.String");
         props.setProperty("form2.class", "java.util.Map");
 
         List<FormFields> forms = reader.read(props);
 
         assertEquals(2, forms.size());
     }
 
     public void test_read_singleForm() {
         props.setProperty("form.class", "java.lang.String");
         props.setProperty("form.field1", "3");
 
         when(resolver.resolveLength("3")).thenReturn(3);
 
         List<FormFields> forms = reader.read(props);
 
         assertEquals(1, forms.size());
         FormFields form = forms.get(0);
         assertEquals(String.class, form.getFormClass());
         assertEquals(3, form.lengthFor("field1"));
     }
 
     public void test_read_NoProperties() {
         List<FormFields> forms = reader.read(props);
 
         assertNotNull(forms);
         assertEquals(0, forms.size());
     }
 
 }
