 package org.jgum.category.type;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import org.apache.log4j.or.ObjectRenderer;
 import org.jgum.JGum;
 import org.junit.Test;
 
 public class TypeCategoryTutorialTest {
 
 	public class Fruit  {}
 	public class Orange extends Fruit{}
 	
 	public class FruitRenderer implements ObjectRenderer {
 		@Override
 		public String doRender(Object fruit) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 	
 
 	@Test
 	public void testTypeCategoryInheritance() {
 		JGum jgum = new JGum();
 		TypeCategory<?> parent = jgum.forClass(Fruit.class); //type category for Fruit.class
 		TypeCategory<?> child = jgum.forClass(Orange.class); //type category for Orange.class
 		FruitRenderer fruitRenderer = new FruitRenderer();
		parent.setProperty(ObjectRenderer.class, fruitRenderer); //ObjectRenderer.class property set to fruitRenderer for Fruit.class
		assertEquals(fruitRenderer, parent.getProperty(ObjectRenderer.class).get()); //ObjectRenderer.class property is fruitRenderer for Fruit.class
		assertEquals(fruitRenderer, child.getProperty(ObjectRenderer.class).get()); //ObjectRenderer.class property is also fruitRenderer for Orange.class
		assertFalse(jgum.forClass(Object.class).getProperty(ObjectRenderer.class).isPresent()); //ObjectRenderer.class property has not been set for Object.class
 	}
 	
 }
