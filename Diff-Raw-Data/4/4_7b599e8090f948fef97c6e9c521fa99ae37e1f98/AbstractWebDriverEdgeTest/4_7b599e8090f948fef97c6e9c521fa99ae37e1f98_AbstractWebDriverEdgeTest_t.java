 package com.operativus.senacrs.audit.graph.edges.webdriver;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.Mockito;
 import org.openqa.selenium.WebDriver;
 
 import com.operativus.senacrs.audit.graph.nodes.Node;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNode;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeFactory;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeType;
 import com.operativus.senacrs.audit.graph.nodes.webdriver.WebDriverNodeTypeEnum;
 
 public class AbstractWebDriverEdgeTest {
 
 	private WebDriver driver = null;
 	private AbstractWebDriverEdge mock = null;
 
 	private class MyAbstractWebDriverEdge
 			extends AbstractWebDriverEdge {
 
 		protected MyAbstractWebDriverEdge(final WebDriverNodeType... acceptedSourceNodes) {
 
 			super(AbstractWebDriverEdgeTest.this.driver, acceptedSourceNodes);
 		}
 
 		@Override
 		protected void internTraverse(final WebDriverNode source) {
 
 			AbstractWebDriverEdgeTest.this.mock.internTraverse(source);
 		}
 
 	}
 
 	@Rule
 	public ExpectedException thrown = ExpectedException.none();
 
 	@Before
 	public void setUp() throws Exception {
 
 		this.driver = Mockito.mock(WebDriver.class);
 		this.mock = Mockito.mock(AbstractWebDriverEdge.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 
 		this.mock = null;
 		this.driver = null;
 	}
 
 	@Test
 	public void testAbstractWebDriverEdgeNull() {
 
 		this.thrown.expect(IllegalArgumentException.class);
 		new AbstractWebDriverEdge(null) {
 
 			@Override
 			protected void internTraverse(final WebDriverNode source) {
 
 				// do nothing
 			}
 		};
 	}
 
 	@Test
 	public void testTraverseNull() {
 
 		AbstractWebDriverEdge obj = null;
 
 		obj = new MyAbstractWebDriverEdge();
 		this.thrown.expect(IllegalArgumentException.class);
 		obj.traverse(null);
 	}
 
 	@Test
 	public void testTraverseNonWebDriverNode() {
 
 		AbstractWebDriverEdge obj = null;
 
 		obj = new MyAbstractWebDriverEdge();
 		this.thrown.expect(IllegalSourceNodeClassException.class);
 		obj.traverse(Node.START);
 	}
 
 	@Test
 	public void testTraverseNonAcceptableSourceNodes() {
 
 		AbstractWebDriverEdge obj = null;
 		WebDriverNode node = null;
 
 		obj = new MyAbstractWebDriverEdge();
 		node = WebDriverNodeFactory.createNode(WebDriverNodeTypeEnum.START);
		this.thrown.expect(IllegalSourceNodeTypeException.class);
 		obj.traverse(node);
 	}
 
 	@Test
 	public void testTraverseAcceptableSourceNodes() {
 
 		AbstractWebDriverEdge obj = null;
 		WebDriverNode node = null;
 		WebDriverNodeTypeEnum type = null;
 
 		type = WebDriverNodeTypeEnum.START;
 		obj = new MyAbstractWebDriverEdge(type);
 		node = WebDriverNodeFactory.createNode(type);
 		obj.traverse(node);
 		Mockito.verify(this.mock).internTraverse(node);
 	}
 }
