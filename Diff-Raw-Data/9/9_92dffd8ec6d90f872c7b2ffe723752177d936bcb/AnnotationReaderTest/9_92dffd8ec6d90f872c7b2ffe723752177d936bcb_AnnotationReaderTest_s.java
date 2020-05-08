 package microejb.tests;
 
 
 import microejb.AnnotationReader;
 import microejb.DoubleInject;
 import microejb.SingleInject;
 import microejb.WrappedField;
 import org.junit.Test;
 
 import javax.inject.Inject;
 import java.lang.reflect.Field;
 import java.util.Arrays;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class AnnotationReaderTest {
     @Test
     public void classWithSingleInjectAnnotation() throws Exception {
        AnnotationReader reader = new AnnotationReader();
       WrappedField expected = new WrappedField("fieldToInject",String.class, "@Inject");
        assertThat(reader.readClass(SingleInject.class), is(Arrays.asList(expected)));
     }
     @Test
     public void classWithTwoSingleInjectAnnotations() throws Exception {
         AnnotationReader reader = new AnnotationReader();
         WrappedField expected[] = { new WrappedField("firstField", String.class, "@javax.inject.Inject()"),
                                     new WrappedField("secondField", String.class, "@javax.inject.Inject()")
                                    };
         assertThat(reader.readClass(DoubleInject.class), is(Arrays.asList(expected)));
     }
 }
