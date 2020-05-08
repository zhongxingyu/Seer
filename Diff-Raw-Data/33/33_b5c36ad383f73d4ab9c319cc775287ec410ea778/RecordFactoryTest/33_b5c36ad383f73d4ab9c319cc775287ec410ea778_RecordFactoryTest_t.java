 package parser.parser;
 
 import org.junit.Before;
 import org.junit.Test;
 import parser.configuration.Config;
 import parser.parser2.Record;
import parser.parser2.PropertiesParser2Impl;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * Created by IntelliJ IDEA.
  * User: al1
  * Date: 11.04.12
  */
 public class RecordFactoryTest {
 
     private Config mockConfig;
    private PropertiesParser2Impl factory;
 
     @Before
     public void startUp() {
         mockConfig = getMockConfig();
     }
 
     @Test
     public void testCreateField() {
        factory = new PropertiesParser2Impl(mockConfig);
         Record record = factory.getInstance("opa:opapa", 0);
         assertEquals("opa", record.getName());
         assertEquals("opapa", record.getValue().getContent());
     }
 
     @Test
     public void testCreateClass() {
        factory = new PropertiesParser2Impl(mockConfig);
         Record record = factory.getInstance("A()", 0);
         assertEquals("A", record.getName());
         assertEquals("", record.getValue().getContent());
     }
 
     @Test
     public void testCreateClassWithField() {
        factory = new PropertiesParser2Impl(mockConfig);
         Record record = factory.getInstance("A(opa:opapa)", 0);
         assertEquals("A", record.getName());
         assertEquals("opa", record.getValue().getName());
         assertEquals("opapa", record.getValue().getValue().getContent());
     }
 
     @Test
     public void testCreateClassWithClass() {
        factory = new PropertiesParser2Impl(mockConfig);
         Record record = factory.getInstance("A(opa:A())", 0);
         assertEquals("A", record.getName());
         assertEquals("opa", record.getValue().getName());
         assertEquals("A", record.getValue().getValue().getName());
     }
 
     @Test
     public void testCreateClassWithClassAndField() {
        factory = new PropertiesParser2Impl(mockConfig);
         Record record = factory.getInstance("A(opa:A(opa:opapa))", 0);
         assertEquals("A", record.getName());
         assertEquals("opa", record.getValue().getName());
         assertEquals("A", record.getValue().getValue().getName());
         assertEquals("opa", record.getValue().getValue().getValue().getName());
         assertEquals("opapa", record.getValue().getValue().getValue().getValue().getContent());
     }
 
     private Config getMockConfig() {
         Config mockedConfig = mock(Config.class);
         when(mockedConfig.getClassDelimiter()).thenReturn(',');
         when(mockedConfig.getValueDelimiter()).thenReturn(':');
         when(mockedConfig.getLeftBracket()).thenReturn('(');
         when(mockedConfig.getRightBracket()).thenReturn(')');
 
         return mockedConfig;
     }
 }
