 package hello;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Test;
 
 
 public class FizzBuzzTest {
 	@Test
 	public void _3のときはFizz() throws Exception {
 		FizzBuzz target = new FizzBuzz();
 		assertThat(target.fizzbuzz(3), is("Fizz")); 
 	}
 	
	@Test
	public void _5のときはBuzz() throws Exception {
		FizzBuzz target = new FizzBuzz();
		assertThat(target.fizzbuzz(5), is("Buzz")); 
	}
	
 }
