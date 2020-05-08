 package hello;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 
 
 public class FizzBuzzTest {
 	@Test
 	public void _3のときはFizz() throws Exception {
 		FizzBuzz target = new FizzBuzz();
		assertThat(target.FizzBuzz(3), is("Fizz")); 
 	}
 	
 }
