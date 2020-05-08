 package org.jackie.test.utils;
 
 import org.jackie.utils.Assert;
 import org.jackie.utils.ChainImpl;
 import org.testng.annotations.Test;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * @author Patrik Beno
  */
 public class ChainTest {
 
 	static class IntChain extends ChainImpl<IntChain> {
 		int id;
 		IntChain(int id) {
 			this.id = id;
 		}
 		List<Integer> dump() {
 			List<Integer> ids = new ArrayList<Integer>();
 			IntChain chain = head();
 			while (chain != null) {
 				ids.add(chain.id);
 				chain = chain.next();
 			}
 			return ids;
 		}
 	}
 
 	static IntChain create(int id) {
 		return new IntChain(id);
 	}
 
 	static IntChain createTestChain() {
 		return create(1).append(create(2)).append(create(3)).append(create(4)).head();
 	}
 
 	List<Integer> expected = Arrays.asList(1,2,3,4);
 
 	@Test
 	public void headtail() {
 		IntChain chain = create(1).append(create(2)).append(create(3)).append(create(4));
 		Assert.expected(1, chain.head().id, "head?");
 		Assert.expected(4, chain.tail().id, "tail?");
 		Assert.doAssert(chain.head().isHead(), "isHead?");
 		Assert.doAssert(chain.tail().isTail(), "isTail?");
 	}
 
 	@Test(dependsOnMethods="headtail")
 	public void append() {
 		IntChain chain = create(1).append(create(2)).append(create(3)).append(create(4)).head();
 		Assert.expected(expected, chain.dump(), "Chain.append()");
 
 		IntChain segment = create(2).append(create(3)).head();
 		chain = create(1).append(segment).append(create(4));
 		Assert.expected(expected, chain.dump(), "Chain.append() with segment");
 
 	}
 
 	@Test(dependsOnMethods="headtail")
 	public void insert() {
 		IntChain chain = create(4).insert(create(1)).insert(create(2)).insert(create(3)).head();
 		Assert.expected(expected, chain.dump(), "Chain.insert()");
 	}
 
 	@Test(dependsOnMethods="append")
 	public void delete() {
 		IntChain unwanted = create(1001).append(create(1002)).append(create(1003)).head();
 		IntChain chain = create(1).append(create(2)).append(create(3))
 				.append(unwanted)
 				.append(create(4))
 				.head();
 
 		unwanted.delete().delete().delete();
 
 		Assert.expected(expected, chain.dump(), "Chain.delete()");
 	}
 
 	@Test
 	public void index() {
 		IntChain chain = createTestChain();
 		Assert.expected(4, chain.length(), "length?");
		Assert.expected(0, chain.index(), "index?");
		Assert.expected(1, chain.next().index(), "index?");
		Assert.expected(3, chain.tail().index(), "index?");
 	}
 
 	@Test
 	public void iterator() {
 		IntChain chain = createTestChain();
 		int expect = 0;
 		for (IntChain element : chain) {
 			expect++;
 			Assert.expected(expect, element.id, "iterator?");
 		}
 		Assert.expected(expect, 4, "iterator max?");
 	}
 }
