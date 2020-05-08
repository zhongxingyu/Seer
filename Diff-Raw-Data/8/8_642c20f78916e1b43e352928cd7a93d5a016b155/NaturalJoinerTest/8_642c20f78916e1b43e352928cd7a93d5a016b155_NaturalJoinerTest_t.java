 package eu.play_project.distributedetalis.join.tests;
 
import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import eu.play_project.dcep.distributedetalis.join.NaturalJoiner;
 
 
 
 /**
  * @author Ningyuan Pan
  *
  */
 public class NaturalJoinerTest {
 	
 	private static NaturalJoiner nj;
 	
 	@BeforeClass
 	public static void init(){
 		nj = new NaturalJoiner();
 	}
 	
 	@Test
 	public void testNaturalJoin1() throws IOException {
 		NaturalJoinerTestReader re = new NaturalJoinerTestReader("nj1.txt");
 		re.read();
 		
 		List<List> r = nj.naturalJoin(re.r1, re.v1, re.r2, re.v2);
 		boolean pass = false;
 		
 		pass = re.v1.size() == re.v.size();
 		assertTrue("Variable size not equal on natural join test 1", pass);
 		pass = r.size() == re.r.size();
 		assertTrue("Result size not equal on natural join test 1", pass);
 		
 		for(int i = 0; i < re.v1.size(); i++){
 			assertTrue("Variable error on natural join test 1", re.v1.get(i).equals(re.v.get(i)));
 		}
 		for(int i = 0; i < r.size(); i++){
 			List<String> m = r.get(i);
 			List<String> n = re.r.get(i);
 			assertTrue("Result size error on natural join test 1", m.size()==n.size());
 			for(int j = 0; j < n.size(); j++){
 				assertTrue("Result error on natural join test 1", m.get(j).equals(n.get(j)));
 			}
 		}
 	}
 	
 	@Test
 	public void testNaturalJoin2() throws IOException {
 		NaturalJoinerTestReader re = new NaturalJoinerTestReader("nj2.txt");
 		re.read();
 		
 		List<List> r = nj.naturalJoin(re.r1, re.v1, re.r2, re.v2);
 		boolean pass = false;
 		
 		pass = re.v1.size() == re.v.size();
 		assertTrue("Variable size not equal on natural join test 2", pass);
 		pass = r.size() == re.r.size();
 		assertTrue("Result size not equal on natural join test 2", pass);
 		
 		for(int i = 0; i < re.v1.size(); i++){
 			assertTrue("Variable error on natural join test 2", re.v1.get(i).equals(re.v.get(i)));
 		}
 		for(int i = 0; i < r.size(); i++){
 			List<String> m = r.get(i);
 			List<String> n = re.r.get(i);
 			assertTrue("Result size error on natural join test 2", m.size()==n.size());
 			for(int j = 0; j < n.size(); j++){
 				assertTrue("Result error on natural join test 2", m.get(j).equals(n.get(j)));
 			}
 		}
 	}
 	
 	@Test
 	public void testNaturalJoin3() throws IOException {
 		NaturalJoinerTestReader re = new NaturalJoinerTestReader("nj3.txt");
 		re.read();
 		
 		List<List> r = nj.naturalJoin(re.r1, re.v1, re.r2, re.v2);
 		boolean pass = false;
 		
 		pass = re.v1.size() == re.v.size();
 		assertTrue("Variable size not equal on natural join test 3", pass);
 		pass = r.size() == re.r.size();
 		assertTrue("Result size not equal on natural join test 3", pass);
 		
 		for(int i = 0; i < re.v1.size(); i++){
 			assertTrue("Variable error on natural join test 3", re.v1.get(i).equals(re.v.get(i)));
 		}
 		for(int i = 0; i < r.size(); i++){
 			List<String> m = r.get(i);
 			List<String> n = re.r.get(i);
 			assertTrue("Result size error on natural join test 3", m.size()==n.size());
 			for(int j = 0; j < n.size(); j++){
 				assertTrue("Result error on natural join test 3", m.get(j).equals(n.get(j)));
 			}
 		}
 	}
 	
 	@Test
 	public void testNaturalJoin4() throws IOException {
 		NaturalJoinerTestReader re = new NaturalJoinerTestReader("nj4.txt");
 		re.read();
 		
 		List<List> r = nj.naturalJoin(re.r1, re.v1, re.r2, re.v2);
 		boolean pass = false;
 		
 		pass = re.v1.size() == re.v.size();
 		assertTrue("Variable size not equal on natural join test 4", pass);
 		pass = r.size() == re.r.size();
 		assertTrue("Result size not equal on natural join test 4", pass);
 		
 		for(int i = 0; i < re.v1.size(); i++){
 			assertTrue("Variable error on natural join test 4", re.v1.get(i).equals(re.v.get(i)));
 		}
 		for(int i = 0; i < r.size(); i++){
 			List<String> m = r.get(i);
 			List<String> n = re.r.get(i);
 			assertTrue("Result size error on natural join test 4", m.size()==n.size());
 			for(int j = 0; j < n.size(); j++){
 				assertTrue("Result error on natural join test 4", m.get(j).equals(n.get(j)));
 			}
 		}
 	}
 	
 	@Test
 	public void testNaturalJoin5() throws IOException {
 		NaturalJoinerTestReader re = new NaturalJoinerTestReader("nj5.txt");
 		re.read();
 		
 		List<List> r = nj.naturalJoin(re.r1, re.v1, re.r2, re.v2);
 		boolean pass = false;
 		
 		pass = re.v1.size() == re.v.size();
 		assertTrue("Variable size not equal on natural join test 5", pass);
 		pass = r.size() == re.r.size();
 		assertTrue("Result size not equal on natural join test 5", pass);
 		
 		for(int i = 0; i < re.v1.size(); i++){
 			assertTrue("Variable error on natural join test 5", re.v1.get(i).equals(re.v.get(i)));
 		}
 		for(int i = 0; i < r.size(); i++){
 			List<String> m = r.get(i);
 			List<String> n = re.r.get(i);
 			assertTrue("Result size error on natural join test 5", m.size()==n.size());
 			for(int j = 0; j < n.size(); j++){
 				assertTrue("Result error on natural join test 5", m.get(j).equals(n.get(j)));
 			}
 		}
 	}
 }
