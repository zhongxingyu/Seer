 package hello;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class FizzBuzzTest {
 	FizzBuzz target;
 	
 	@Before
 	public void before() {
 		this.target = new FizzBuzz();
 	}
 	
 	@Test
 	public void _3のときはFizz() throws Exception {
 		assertThat(target.fizzbuzz(3), is("Fizz")); 
 	}
 	
 	@Test
 	public void _5のときはBuzz() throws Exception {
 		assertThat(target.fizzbuzz(5), is("Buzz")); 
 	}
 	
 	@Test
 	public void _15のときはFizzBuzz() throws Exception {
 		assertThat(target.fizzbuzz(15), is("FizzBuzz")); 
 	}
 	
 }
