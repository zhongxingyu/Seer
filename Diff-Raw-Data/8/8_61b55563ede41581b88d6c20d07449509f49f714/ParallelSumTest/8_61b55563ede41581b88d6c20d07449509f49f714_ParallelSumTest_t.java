 package br.com.batuta;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Test;
 
 import br.com.batuta.events.Joinable;
 import br.com.batuta.events.Produce;
 import br.com.batuta.events.Task;
 
 public class ParallelSumTest {
 
 	@Test
 	public void simpleSum() {
 		Integer total = new Batuta<Integer>().add(new Task() {
 			@Override
 			@SuppressWarnings("unchecked")
 			public Produce<Integer> produce() {
				System.out.println("sum processing - 1");
 				int parcel = 0;
 				for (int i = 0; i < 1000; i++) parcel += i;
 				return new Produce<Integer>(parcel);
 			}
 		}).add(new Task() {
 			@Override
 			@SuppressWarnings("unchecked")
 			public Produce<Integer> produce() {
				System.out.println("sum processing - 2");
 				int parcel = 0;
 				for (int i = 1000; i < 2000; i++) parcel += i;
 				return new Produce<Integer>(parcel);
 			}
 		}).add(new Task() {
 			@Override
 			@SuppressWarnings("unchecked")
 			public Produce<Integer> produce() {
				System.out.println("sum processing - 3");
 				int parcel = 0;
 				for (int i = 2000; i < 3000; i++) parcel += i;
 				return new Produce<Integer>(parcel);
 			}
 		}).join(new Joinable<Integer>() {
 			@Override
 			public Integer join(List<Produce<Integer>> produces) {
 				int result = 0; 
 				for (Produce<Integer> produce : produces) {
 					result += produce.getValue();
 				}
 				return result;
 			}
 		}).execute();
 		
 		assertEquals(Integer.valueOf(4498500), total);
 	}
 }
