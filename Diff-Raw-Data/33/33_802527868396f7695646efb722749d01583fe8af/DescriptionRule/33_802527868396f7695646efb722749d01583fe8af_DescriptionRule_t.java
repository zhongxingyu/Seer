 package net.praqma.util.test.junit;
 
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 /**
  * @author cwolfgang
  */
 public class DescriptionRule implements TestRule {
 
    @Override
     public Statement apply( Statement base, Description description ) {
 
         if( description.getAnnotation( TestDescription.class ) != null ) {
             TestDescription d = description.getAnnotation( TestDescription.class );
 
             System.out.println( "=============================================" );
 
             /* Print the title */
             System.out.println( d.title() );
 
 
             /* Print the description */
             if( d.text() != null ) {
                 System.out.println( d.text() );
             }
 
             /* Print the outcome */
             if( d.outcome() != null ) {
                 int i = 1;
                 System.out.println( "== Test outcome ==" );
                 for( String o : d.outcome() ) {
                     System.out.println( "[" + i + "] " + o );
                     i++;
                 }
             }
 
             /* Print the configurations */
             if( d.configurations() != null ) {
                 int i = 1;
                 System.out.println( "== Test configurations ==" );
                 for( String o : d.configurations() ) {
                     System.out.println( "[" + i + "] " + o );
                     i++;
                 }
             }
 
             System.out.println( "=============================================" );
         }
 
         return base;
     }
 
 
 }
