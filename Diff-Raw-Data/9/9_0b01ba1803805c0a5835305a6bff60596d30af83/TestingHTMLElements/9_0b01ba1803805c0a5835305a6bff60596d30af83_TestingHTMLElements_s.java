 package br.org.mosaic.tests;
 import junit.framework.TestCase;
 import br.org.mosaic.HTMLComponentCollection;
 import br.org.mosaic.HTMLElement;
 import br.org.mosaic.properties.StyleName;
 import br.org.mosaic.properties.StyleProperty;
 import br.org.mosaic.tags.CodeElement;
 import br.org.mosaic.tags.TextElement;
 import br.org.mosaic.tags.list.LI;
 import br.org.mosaic.tags.list.UL;
 import br.org.mosaic.tags.page.Br;
 import br.org.mosaic.tags.page.Div;
 import br.org.mosaic.tags.text.P;
 import br.org.mosaic.tags.text.Span;
 
 public class TestingHTMLElements extends TestCase 
 {
 	public void testElement(){
 		HTMLElement e = new Div().text("TESTE");
 
 		assertEquals("<div>TESTE</div>", e.toString());
 	}
 
 	public void testElementWithId(){
 		HTMLElement e = new Div().id("divtest").text("TESTE");
 
 		assertEquals("<div id='divtest' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithOneClasses(){
 		HTMLElement e = new Div().classes("display").text("TESTE");
 
 		assertEquals("<div class='display' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithTwoClasses(){
 		HTMLElement e = new Div().classes("display", "borded").text("TESTE");
 
 		assertEquals("<div class='display borded' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithThreeClasses(){
 		HTMLElement e = new Div().classes("display", "borded", "marked").text("TESTE");
 
 		assertEquals("<div class='display borded marked' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithOneStyle(){
 		String st1 = StyleProperty.style(StyleName.BORDER, "1px dotted #333");
 
 		HTMLElement e = new Div().styles(st1).text("TESTE");
 
 		assertEquals("<div style='border:1px dotted #333;' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithTwoStyle(){
 		String st1 = StyleProperty.style(StyleName.BORDER, "1px dotted #333");
 		String st2 = StyleProperty.style(StyleName.BACKGROUND_COLOR, "#EDEDED");
 
 		HTMLElement e = new Div().styles(st1, st2).text("TESTE");
 
 		assertEquals("<div style='border:1px dotted #333;background-color:#EDEDED;' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithThreeStyle(){
 		String st1 = StyleProperty.style(StyleName.BORDER, "1px dotted #333");
 		String st2 = StyleProperty.style(StyleName.BACKGROUND_COLOR, "#EDEDED");
 		String st3 = StyleProperty.style(StyleName.FONT_FAMILY, "Verdana");
 
 		HTMLElement e = new Div().styles(st1, st2, st3).text("TESTE");
 
 		assertEquals("<div style='border:1px dotted #333;background-color:#EDEDED;font-family:Verdana;' >TESTE</div>", e.toString());
 	}
 
 	public void testElementWithEvent(){
 		HTMLElement e = new Div().onclick("function(){alert('Hello')}").text("TESTE");
 
 		assertEquals("<div onclick='function(){alert(\\'Hello\\')}' >TESTE</div>", e.toString());
 	}
 
	public void testElementWithAllEvents(){
		String func = "void();";
		Div d = new Div();
		d.onchange(func);
		d.text("TESTE");

		assertEquals("<div onclick='function(){alert(\\'Hello\\')}' >TESTE</div>", d.toString());
	}
	
 	public void testElementNested(){
 		HTMLElement e = new Div().add(
 				new Br(),
 				new TextElement("TESTE: "),
 				new P().text("TESTE"),
 				new UL().add(
 						new LI().text("Maça"),
 						new LI().text("Uva"),
 						new LI().text("Pera"),
 						new LI().text("Carambola")
 				)
 		);
 
 		assertEquals("<div><br/>TESTE: <p>TESTE</p><ul><li>Maça</li><li>Uva</li><li>Pera</li><li>Carambola</li></ul></div>", e.toString());
 	}
 
 	public void testElementNestedWithCollection(){
 		HTMLComponentCollection collection = HTMLComponentCollection.create(
 				new Br(),
 				new TextElement("TESTE: "),
 				new P().text("TESTE"),
 				new UL().add(
 						new LI().text("Maça"),
 						new LI().text("Uva"),
 						new LI().text("Pera"),
 						new LI().text("Carambola")
 				)		
 		);
 
 		HTMLElement e = new Div().add(
 				collection
 		);
 
 		assertEquals("<div><br/>TESTE: <p>TESTE</p><ul><li>Maça</li><li>Uva</li><li>Pera</li><li>Carambola</li></ul></div>", e.toString());
 	}
 
 	public void testElementNestedWithCollectionAnotherWay(){
 		HTMLComponentCollection collection = HTMLComponentCollection.create();
 		collection.add( new Br() );
 		collection.add( new TextElement("TESTE: ") );
 		collection.add( new P().text("TESTE"));
 		collection.add( new UL().add(
 				new LI().text("Maça"),
 				new LI().text("Uva"),
 				new LI().text("Pera"),
 				new LI().text("Carambola") )
 		);
 
 		HTMLElement e = new Div().add(
 				collection
 		);
 
 		assertEquals("<div><br/>TESTE: <p>TESTE</p><ul><li>Maça</li><li>Uva</li><li>Pera</li><li>Carambola</li></ul></div>", e.toString());
 	}
 
 	public void testElementNestedWithCollectionAnotherAnotherWay(){
 		HTMLComponentCollection collection = HTMLComponentCollection.create();
 		collection.add( 
 				new Br(),
 				new TextElement("TESTE: "),
 				new P().text("TESTE"),
 				new UL().add(
 						new LI().text("Maça"),
 						new LI().text("Uva"),
 						new LI().text("Pera"),
 						new LI().text("Carambola") 
 				)
 		);
 
 		HTMLElement e = new Div().add(
 				collection
 		);
 
 		assertEquals("<div><br/>TESTE: <p>TESTE</p><ul><li>Maça</li><li>Uva</li><li>Pera</li><li>Carambola</li></ul></div>", e.toString());
 	}
 	
 	public void testElementNestedWithCollectionNestedInAnotherCollection(){
 		HTMLComponentCollection collection = HTMLComponentCollection.create();
 		collection.add( 
 				new Br(),
 				new TextElement("TESTE: "),
 				new P().text("TESTE"),
 				new UL().add(
 						new LI().text("Maça"),
 						new LI().text("Uva"),
 						new LI().text("Pera"),
 						new LI().text("Carambola") 
 				)
 		);
 
 		HTMLComponentCollection godMaster = HTMLComponentCollection.create(new Span().text("span"), collection);
 		
 		HTMLElement e = new Div().add(
 				godMaster
 		);
 
 		assertEquals("<div><span>span</span><br/>TESTE: <p>TESTE</p><ul><li>Maça</li><li>Uva</li><li>Pera</li><li>Carambola</li></ul></div>", e.toString());
 	}
 	
 	public void testCollectionShouldntPrintAnythingforItSelf(){
 		HTMLComponentCollection collection = HTMLComponentCollection.create();
 
 		assertEquals("", collection.toString());
 	}
 
 	public void testCollectionShouldPrintAText(){
 		HTMLComponentCollection collection = HTMLComponentCollection.create(new TextElement("nested"));
 
 		assertEquals("nested", collection.toString());
 	}
 	
 	public void testCodeElement(){
 		CodeElement c = new CodeElement(
 		"var a = '1';",
 		"var b = '2';",
 		"if( a == b ){",
 		"   alert('a is NOT equals b');",
 		"}else{   ",
 		    "return;",
 		"}"
 		);
 		assertEquals("var a = '1';var b = '2';if( a == b ){alert('a is NOT equals b');}else{return;}", c.toString());
 	}
 }
