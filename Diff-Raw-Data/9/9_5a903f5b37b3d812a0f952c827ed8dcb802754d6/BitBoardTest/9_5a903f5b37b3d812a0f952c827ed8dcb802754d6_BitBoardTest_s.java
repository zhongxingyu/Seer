 package com.chroneus.atarigo;
 
 import static org.junit.Assert.*;
 
 import java.util.Random;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class BitBoardTest {
     BitBoard b;
 	 Random r=new Random();
 	@Before
 	public void setUp() throws Exception {
 		b=new BitBoard(Board.SIZE,Board.SIZE);
 	}
 
 
 	@Test
 	public void testSet() {
 		for (int i=r.nextInt(128),j=0;j<100;j++){
 		b.set(i);
 		assertTrue(b.get(i));
 		b.clear(i);
 		assertTrue(!b.get(i));
 		b.flip(i);
 		assertTrue(b.get(i));
 		b.flip(i);
 		assertTrue(!b.get(i));
 		b.flipInRange(i, 127);
 		assertTrue(b.get(i));
 		b.flipInRange(i, 127);
 		assertTrue(!b.get(i));
 		}
 	}
 
 
 	@Test
 	public void testNot() {
 		b.not();
 		assertEquals(BoardConstant.ALL, b);
 	}
 	
 	@Test
 	public void testGetsize() {
 		for(int i=0;i<100;i++){
 			b.set(i);
 		}
 		assertTrue(b.getXsize()==9);
 		assertTrue(b.getYsize()==9);
 	}
 
 	@Test
 	public void testNextSetBit() {
 		b.set(0);
 		for (int i=r.nextInt(128),j=0;j<100000;j++){
 		b.set(i);
 		assertTrue(b.nextSetBit(1)==i);
 		if(i<50){
 			b.set(i+77);
 			assertTrue(b.nextSetBit(i+1)==i+77);
 			b.clear(i+77);
 		}
 		b.clear(i);;
 		}
 	}
 	
 
 	
 	@Test
 	public void testclone(){
 		b.set(0);
 		assertTrue (b.equals(b.clone()));		
 	}
 
 	@Test
 	public void testCardinality(){
 		int j;
 		for ( j=0;j<80;j++){
 			b.set(j);
 			assertTrue (b.cardinality()==j+1);	
 		}
 	
 		b.clear();
 		b.flipInRange(0, j/2);
 		assertTrue (b.cardinality()==j/2+1);
 	}
 	
 	@Test
 	public void testRotate90(){
 		for (int i = 0; i < 100; i++) {
 			BitBoard test=random();			assertEquals(test, test.rotate90().rotate90().rotate90().rotate90());
 		}
 //		
 		BitBoard test= new BitBoard(3,2);
 		test.set(1);test.set(3);test.set(4);
 		test.rotate90();
 	}
 	
 	public  BitBoard random(){
 		BitBoard result= new BitBoard(Board.SIZE,Board.SIZE);
 		result.a0=r.nextLong();
 		result.a1=(r.nextLong()&BitBoard.mask)|(result.a1&~BitBoard.mask);
 		return result;
 	}
 
 @Test	
 	public  void testNearStone(){
 	
	    BoardConstant.ALL.nearest_stones();
 		assertTrue(BoardConstant.FirstLine.nearest_stones().equals(BoardConstant.SecondLine));
 		int size=1000000;
 		BitBoard[]perf_array=new BitBoard[size];
 		for (int i = 0; i < size; i++) {
 			perf_array[i]=random();//takes a long time
 			
 		}
 		long start = System.currentTimeMillis();
 		for (int k = 0; k < perf_array.length; k++) {
 			perf_array[k]=
 					perf_array[k].nearest_stones();
 		}
 		long end = System.currentTimeMillis();
 		System.out.println("near stones: " + (end - start) + " msec for "+size);
 		
 	}
 
 }
