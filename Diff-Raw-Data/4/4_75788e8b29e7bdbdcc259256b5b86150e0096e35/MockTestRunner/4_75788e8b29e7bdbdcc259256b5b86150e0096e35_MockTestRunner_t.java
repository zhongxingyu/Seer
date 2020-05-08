 /*
  * $Id: CdkTestRunner.java 16902 2010-05-05 23:50:39Z alexsmirnov $
  *
  * License Agreement.
  *
  * Rich Faces - Natural Ajax for Java Server Faces (JSF)
  *
  * Copyright (C) 2007 Exadel, Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License version 2.1 as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
  */
 
 package org.jboss.test.faces.mock;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.junit.runner.notification.RunNotifier;
 import org.junit.runners.BlockJUnit4ClassRunner;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.InitializationError;
 
 /**
  * <p class="changed_added_4_0">
  * </p>
  * 
  * @author asmirnov@exadel.com
  * 
  */
 public class MockTestRunner extends BlockJUnit4ClassRunner {
 
     /**
      * <p class="changed_added_4_0">
      * </p>
      * 
      * @param klass
      * @throws InitializationError
      * @throws InitializationError
      */
     public MockTestRunner(Class<?> klass) throws InitializationError {
         super(klass);
     }
 
     /**
      * Gets all declared fields and all inherited fields.
      */
     protected Set<Field> getFields(Class<?> c) {
         Set<Field> fields = new HashSet<Field>(Arrays.asList(c.getDeclaredFields()));
         while ((c = c.getSuperclass()) != null) {
             for (Field f : c.getDeclaredFields()) {
                 if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isPrivate(f.getModifiers())) {
                     fields.add(f);
                 }
             }
         }
         return fields;
     }
 
     @Override
     protected void runChild(FrameworkMethod method, RunNotifier notifier) {
         super.runChild(method, notifier);
     }
 
     @Override
     protected Object createTest() throws Exception {
         Class<?> c = getTestClass().getJavaClass();
         Set<Field> testFields = getFields(c);
 
         // make sure we have one (and only one) @Unit field
         // Field unitField = getUnitField(testFields);
         // if ( unitField.getAnnotation(Mock.class) != null ) {
         // throw new IncompatibleAnnotationException(Unit.class, Mock.class);
         // }
         //        
         final Map<Field, Binding> fieldValues = getMockValues(testFields);
         // if ( fieldValues.containsKey(unitField)) {
         // throw new IncompatibleAnnotationException(Unit.class, unitField.getType());
         // }
 
         Object test = super.createTest();
 
         // any field values created by AtUnit but not injected by the container are injected here.
         for (Field field : fieldValues.keySet()) {
             Binding binding = fieldValues.get(field);
             field.setAccessible(true);
             if (null != binding.getValue() && field.get(test) == null) {
                 field.set(test, binding.getValue());
             } 
         }
 
         return test;
     }
 
  
     protected static final class FieldModule implements MockController {
         
         interface Invoker{
             void perform(Object...objects);
             void perform(MockFacesEnvironment environment);
         }
         
         final Collection<Binding> fields;
 
         public FieldModule(Map<Field, Binding> fields) {
            this.fields = new ArrayList<MockTestRunner.Binding>(fields.values());
         }
 
         private void perform(Invoker invoker, Object ...objects) {
             for (Binding field : fields) {
                 if(field.isMock()){
                     if(field.getValue() instanceof MockFacesEnvironment){
                         invoker.perform((MockFacesEnvironment) field.getValue());
                     } else {
                         invoker.perform(field.getValue());
                     }
                 }
             }
             invoker.perform(objects);
         }
 
         public void reset(Object ...objects) {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.reset();
                 }
                 
                 public void perform(Object... objects) {
                     FacesMock.reset(objects);
                 }
             },objects);
         }
 
         public void resetToNice(Object ...objects) {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.resetToNice();
                 }
                 
                 public void perform(Object... objects) {
                     FacesMock.resetToNice(objects);
                 }
             },objects);
         }
 
         public void resetToStrict(Object ...objects) {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.resetToStrict();
                 }
                 
                 public void perform(Object... objects) {
                     FacesMock.resetToStrict(objects);
                 }
             },objects);
         }
 
         public void resetToDefault(Object ...objects) {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.resetToDefault();
                 }
                 
                 public void perform(Object... objects) {
                     FacesMock.resetToDefault(objects);
                 }
             },objects);
         }
 
         public void verify(Object ...objects) {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.verify();
                 }
                 
                 public void perform(Object... objects) {
                     FacesMock.verify(objects);
                 }
             },objects);
         }
 
         public void replay(Object... objects) {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.replay();
                 }
                 
                 public void perform(Object... objects) {
                     FacesMock.replay(objects);
                 }
             },objects);
         }
 
         public void release() {
             perform(new Invoker() {
                 
                 public void perform(MockFacesEnvironment environment) {
                     environment.release();
                 }
                 
                 public void perform(Object... objects) {
                     // do nothing
                 }
             });
         }
 
         public <T> T createMock(Class<T> clazz) {
             T mock = FacesMock.createMock(clazz);
             fields.add(createMockBinding(mock));
             return mock;
         }
 
         public <T> T createMock(String name, Class<T> clazz) {
             T mock = FacesMock.createMock(name,clazz);
             fields.add(createMockBinding(mock));
             return mock;
         }
 
         public <T> T createNiceMock(Class<T> clazz) {
             T mock = FacesMock.createNiceMock(clazz);
             fields.add(createMockBinding(mock));
             return mock;
         }
 
         public <T> T createNiceMock(String name, Class<T> clazz) {
             T mock = FacesMock.createNiceMock(name,clazz);
             fields.add(createMockBinding(mock));
             return mock;
         }
 
         public <T> T createStrictMock(Class<T> clazz) {
             T mock = FacesMock.createStrictMock(clazz);
             fields.add(createMockBinding(mock));
             return mock;
         }
 
         public <T> T createStrictMock(String name, Class<T> clazz) {
             T mock = FacesMock.createStrictMock(name,clazz);
             fields.add(createMockBinding(mock));
             return mock;
         }
     }
 
     /**
      * <p class="changed_added_4_0">Binding definition storage</p>
      * @author asmirnov@exadel.com
      *
      */
     protected static final class Binding {
         private Object value;
         private boolean mock;
         protected Binding() {
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @param value the value to set
          */
         void setValue(Object value) {
             this.value = value;
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @return the value
          */
         Object getValue() {
             return value;
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @param mock the mock to set
          */
         public void setMock(boolean mock) {
             this.mock = mock;
         }
         /**
          * <p class="changed_added_4_0"></p>
          * @return the mock
          */
         public boolean isMock() {
             return mock;
         }
     }
 
     private static Map<Field, Binding> getMockValues(Set<Field> testFields) {
         Map<Field, Binding> mocksAndStubs = new HashMap<Field, Binding>();
         // TODO - create annotation attribute that tells runner to use the scme Mock Controller to create related mocks.
         for (Field field : testFields) {
             if (field.isAnnotationPresent(Strict.class)) {
                 mocksAndStubs.put(field, createMockBinding(field, FacesMock.createStrictMock(notEmpty(field.getAnnotation(Strict.class).value()),field.getType())));
             } if (field.isAnnotationPresent(Mock.class)) {
                     mocksAndStubs.put(field, createMockBinding(field, FacesMock.createMock(notEmpty(field.getAnnotation(Mock.class).value()),field.getType())));
             } else if (field.isAnnotationPresent(Stub.class)) {
                 mocksAndStubs.put(field, createMockBinding(field, FacesMock.createNiceMock(notEmpty(field.getAnnotation(Stub.class).value()),field.getType())));
             } else if(field.getType().isAssignableFrom(MockController.class)){
                 FieldModule module = new FieldModule(mocksAndStubs);
                 mocksAndStubs.put(field, createBinding(module));
             }
         }
 
         return mocksAndStubs;
     }
     
     private static Binding createMockBinding(Field field,Object value) {
         Binding bind = createMockBinding(value);
         if(field.isAnnotationPresent(Environment.class)){
             MockFacesEnvironment environment = (MockFacesEnvironment) value;
             for (Environment.Feature feature : field.getAnnotation(Environment.class).value()) {
                 switch(feature){
                     case EXTERNAL_CONTEXT:
                         environment.withExternalContext();
                         break;
                     case EL_CONTEXT:
                         environment.withELContext();
                         break;
                     case APPLICATION:
                         environment.withApplication();
                         break;
                     case FACTORIES:
                         environment.withFactories();
                         break;
                     case RENDER_KIT:
                         environment.withRenderKit();
                         break;
                     case SERVLET_REQUEST:
                         environment.withServletRequest();
                         break;
                     case RESPONSE_WRITER:
                         environment.withReSponseWriter();
                         break;
                 }
             }
         }
         return bind;
     }
 
     private static Binding createMockBinding(Object value) {
         Binding bind = createBinding(value);
         bind.setMock(true);
         return bind;
     }
 
     private static Binding createBinding(Object value) {
         Binding bind = new Binding();
         bind.setValue(value);
         return bind;
     }
 
     private static String notEmpty(String value) {
         return "".equals(value)?null:value;
     }
 }
