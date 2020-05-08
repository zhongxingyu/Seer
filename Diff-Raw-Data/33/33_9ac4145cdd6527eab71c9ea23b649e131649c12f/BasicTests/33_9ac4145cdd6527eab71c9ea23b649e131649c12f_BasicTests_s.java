 package dfh.treepath.test;
 
 import static dfh.treepath.test.XMLToy.parse;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.junit.Test;
 
 import dfh.treepath.Attribute;
 import dfh.treepath.Forester;
 import dfh.treepath.Index;
 import dfh.treepath.NodeTest;
 import dfh.treepath.Path;
 import dfh.treepath.test.XMLToy.Element;
 import dfh.treepath.test.XMLToy.XMLToyForester;
 
 /**
  * Tests to ensure basic functionality of tree path classes.
  * <p>
  * 
  * @author David F. Houghton - Apr 27, 2012
  * 
  */
 @SuppressWarnings("unchecked")
 public class BasicTests {
 
 	@Test
 	public void anywhereTag1() {
 		Element root = parse("<a><b/><c><b/><d><b/><b/></d></c></a>");
 		Path<Element> p = new XMLToyForester().path("//b");
 		Collection<Element> bs = p.select(root);
 		assertEquals(4, bs.size());
 	}
 
 	@Test
 	public void anywhereTag2() {
 		Element root = parse("<a><b><b/></b><b/></a>");
 		Path<Element> p = new XMLToyForester().path("//b//b");
 		List<Element> bs = p.select(root);
 		assertEquals(1, bs.size());
 	}
 
 	@Test
 	public void anywhereTag3() {
 		Element root = parse("<a><a/></a>");
 		Path<Element> p = new XMLToyForester().path("//a");
 		List<Element> bs = p.select(root);
 		assertEquals(2, bs.size());
 	}
 
 	@Test
 	public void rootTag() {
 		Element root = parse("<a><b/><c><b/><d><b/><b/></d></c></a>");
 		Forester<Element> f = new XMLToyForester();
 		Path<Element> p = f.path("/b");
 		Collection<Element> bs = p.select(root);
 		assertEquals(0, bs.size());
 	}
 
 	@Test
 	public void rootSelf() {
 		Element root = parse("<a><b/></a>");
 		Forester<Element> f = new XMLToyForester();
 		Path<Element> p = f.path("/.");
 		List<Element> bs = p.select(root);
 		assertEquals(1, bs.size());
 		assertEquals("a", bs.get(0).tag);
 	}
 
 	@Test
 	public void closestTag() {
 		Element root = parse("<a><b/><c><b><d><b/></d></b></c></a>");
 		Path<Element> p = new XMLToyForester().path("/>b");
 		Collection<Element> bs = p.select(root);
 		assertEquals(2, bs.size());
 	}
 
 	@Test
 	public void anywhereTest1() {
 		Element root = parse("<a><c><d><b/></d></c><b/></a>");
 		Path<Element> p = new XMLToyForester().path("//c//b");
 		Collection<Element> bs = p.select(root);
 		assertEquals(1, bs.size());
 	}
 
 	@Test
 	public void indexTest1() {
 		Element root = parse("<a><b foo='1'/><b foo='2'/><b foo='3'/></a>");
 		Path<Element> p = new XMLToyForester().path("//b[1]");
 		List<Element> bs = p.select(root);
 		assertEquals(1, bs.size());
 		assertEquals("2", bs.get(0).attributes.get("foo"));
 	}
 
 	@Test
 	public void indexTest2() {
 		Element root = parse("<root><a><b foo='1'/><b foo='2'/><b foo='3'/></a><a><b foo='2'/><b foo='3'/></a></root>");
 		Path<Element> p = new XMLToyForester().path("//a/b[1]");
 		List<Element> bs = p.select(root);
 		assertEquals(2, bs.size());
 		assertEquals("2", bs.get(0).attributes.get("foo"));
 		assertEquals("3", bs.get(1).attributes.get("foo"));
 	}
 
 	@Test
 	public void colonTest() {
 		Element root = parse("<a:b><b:b/><b:b fo:o='1'/><b:b fo:o='2'/></a:b>");
 		Path<Element> p = new XMLToyForester()
 				.path("//b:b[@attr('fo:o') != '1']");
 		Collection<Element> bs = p.select(root);
 		assertEquals(1, bs.size());
 	}
 
 	@Test
 	public void ignoreTest1() {
 		Element root = parse("<a><b/><c/></a>");
 		@SuppressWarnings("serial")
 		Forester<Element> f = new XMLToyForester(new NodeTest<Element>() {
 			@Override
 			public boolean passes(Element n, Index<Element> i) {
 				return n.tag.equals("c");
 			}
 		});
 		Path<Element> p = f.path("/*");
 		Collection<Element> bs = p.select(root);
 		assertEquals(1, bs.size());
 	}
 
 	@Test
 	public void reuseTest() {
 		Element root = parse("<root><a><b/><c><a/></c></a><b><b><a><c/></a></b></b></root>");
 		Forester<Element> f = new XMLToyForester();
 		Path<Element> p = f.path("//root");
 		Collection<Element> c = p.select(root);
 		assertEquals(1, c.size());
 		p = f.path("//a");
 		c = p.select(root);
 		assertEquals(3, c.size());
 		p = f.path("//b");
 		c = p.select(root);
 		assertEquals(3, c.size());
 		p = f.path("//c");
 		c = p.select(root);
 		assertEquals(2, c.size());
 		root = parse("<root><c><b><a/></b></c></root>");
 		p = f.path("//root");
 		c = p.select(root);
 		assertEquals(1, c.size());
 		p = f.path("//a");
 		c = p.select(root);
 		assertEquals(1, c.size());
 		p = f.path("//b");
 		c = p.select(root);
 		assertEquals(1, c.size());
 		p = f.path("//c");
 		c = p.select(root);
 		assertEquals(1, c.size());
 	}
 
 	@Test
 	public void idTest() {
 		Element root = parse("<a><b id='foo'><c/><c/><c/></b><b id='bar'><c/></b><b id='(foo)'><c/><c/></b></a>");
 		Forester<Element> f = new XMLToyForester();
 		Path<Element> p = f.path("id(foo)/*");
 		List<Element> l = p.select(root);
 		assertEquals(3, l.size());
 		p = f.path("id(bar)/*");
 		l = p.select(root);
 		assertEquals(1, l.size());
 		p = f.path("id(\\(foo\\))/*");
 		l = p.select(root);
 		assertEquals(2, l.size());
 	}
 
 	@SuppressWarnings("serial")
 	@Test
 	public void extensionTest() {
 		Element root = parse("<a><b foo='bar' bar='foo'/><b foo='foo'/></a>");
 		Forester<Element> f = new XMLToyForester() {
 			@SuppressWarnings("unused")
 			@Attribute("foobar")
 			public Boolean fooBar(Element n, Collection<Element> c,
 					Index<Element> i) {
 				return n.attributes.containsKey("foo")
 						&& n.attributes.containsKey("bar");
 			}
 		};
 		Path<Element> p = f.path("//*[@foobar]");
 		List<Element> l = p.select(root);
 		assertEquals(1, l.size());
 	}
 
 	@Test
 	public void attributeValueTest() {
 		Element root = parse("<a><b foo='bar' bar='foo'/><b foo='foo'/></a>");
 		Forester<Element> f = new XMLToyForester();
 		Path<Element> p = f.path("//*[@attr('foo')]");
 		List<Element> l = p.select(root);
 		assertEquals(2, l.size());
 		Object o = f.attribute(l.get(0), "attr", "foo");
 		assertTrue(o instanceof String);
 		assertEquals("bar", o.toString());
 	}
 
 	@Test
 	public void rootClosest() {
 		Element root = parse("<a><b><c/></b><foo><d/><e><foo/></e></foo></a>");
 		Path<Element> p = new XMLToyForester().path("/>foo/preceding::*");
 		Collection<Element> bs = p.select(root);
 		assertEquals(2, bs.size());
 		Set<String> set = new TreeSet<String>();
 		for (Element e : bs)
 			set.add(e.toString());
 		assertTrue(set.contains("<c />"));
 		assertTrue(set.contains("<b><c /></b>"));
 	}
 
 	@Test
 	public void rootLeaves() {
 		Element root = parse("<a><b><c/></b><foo><d/><e><foo/></e></foo></a>");
 		Path<Element> p = new XMLToyForester().path("/leaf::*");
 		Collection<Element> bs = p.select(root);
 		assertEquals(3, bs.size());
 		Set<String> set = new TreeSet<String>();
 		for (Element e : bs)
 			set.add(e.toString());
 		assertTrue(set.contains("<c />"));
 		assertTrue(set.contains("<d />"));
 		assertTrue(set.contains("<foo />"));
 	}
 }
