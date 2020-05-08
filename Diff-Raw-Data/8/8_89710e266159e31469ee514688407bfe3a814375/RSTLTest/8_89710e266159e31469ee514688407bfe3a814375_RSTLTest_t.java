 
 package org.rstl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class RSTLTest {
 	StatementFactory fac = new StatementFactory();
 
 	@Test
 	public void testChunk() throws IOException, RecognitionException {
 		String input = "abcdedf";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		assertEquals("Failed to match input chunk",
 				StatementType.chunkstatement.name(), result.get(0).getType());
 		Chunk c = (Chunk) result.get(0);
 
 		assertEquals("Failed to match input chunk", input, c.getValue());
 
 		List<Chunk> chunks = parser.getChunks();
 		assertTrue("Failed to return chunk", !chunks.isEmpty());
 		assertEquals("Failed to match input chunk", input, chunks.get(0)
 				.getValue());
 	}
 
 	@Test
 	public void testMultipleChunks() throws IOException, RecognitionException {
 		String input = "abcdedf one or any", input2 = "beadaf any or one";
 		String template = input + "{{myvar}}" + input2;
 		RSTLParser parser = createParser(template);
 		parser.rule();
 		List<Chunk> chunks = parser.getChunks();
 		assertTrue("Failed to return chunk", !chunks.isEmpty());
 		assertEquals("Failed to match input chunk1", input, chunks.get(0)
 				.getValue());
 		assertEquals("Failed to match input chunk2", input2, chunks.get(1)
 				.getValue());
 
 	}
 
 	@Test
 	public void testVar() throws IOException, RecognitionException {
 		String input = "{{foobar}}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		assertEquals("Failed to match variable",
 				fac.createVariable("foobar"), result.get(0));
 	}
 
 	@Test
 	public void testResource() throws IOException, RecognitionException {
 		String input = "{%resource.xhtml /abc%}";
 		RSTLParser parser = createParser(input);
 
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		assertEquals("Failed to match code", new ResourceImpl("/abc", "xhtml",
 				0), result.get(0));
 	}
 
 	@Test
 	public void testResourcewSpace() throws IOException, RecognitionException {
 		String input = "{%resource.xhtml /abc.html %}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		assertEquals("Failed to match code", new ResourceImpl("/abc.html",
 				"xhtml", 0), result.get(0));
 		input = "{%resource.xhtml /abc.html?foo=bar %}";
 		parser = createParser(input);
 		parser.rule();
 		result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		assertEquals("Failed to match code", new ResourceImpl(
 				"/abc.html?foo=bar", "xhtml", 0), result.get(0));
 	}
 
 	@Test
 	public void testResourcewWidget() throws IOException, RecognitionException {
 		String input = "{%resource.xhtml /abc/{def}/part with com.xyz.Catalog %}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		assertEquals("Failed to match code", new ResourceImpl(
 				"/abc/{def}/part", "com.xyz.Catalog", "xhtml", null),
 				result.get(0));
 		assertEquals("Failed to match res ref", "/abc/{def}/part", result
 				.get(0).getId());
 		input = "{%resource.json abc/foo.html   with   com.xyz.Catalog  as r%}";
 		parser = createParser(input);
 		parser.rule();
 		result = parser.getMain();
 		assertTrue("Failed to return a string", !result.isEmpty());
 		Resource r = (Resource)result.get(0);
 		assertEquals("Failed to match id", "abc/foo.html", r.getId());
 		assertEquals("Failed to match widget name",	"com.xyz.Catalog", r.getWidgetName());
 		assertEquals("Failed to match representation type", "json", r.getRepresentationFormat());
 		assertEquals("Failed to match variable name", "r", r.getVariableName());
 	}
 
 	@Test
 	public void testResourceWithTitle() throws IOException, RecognitionException {
 		String input = "{%resource.xhtml /abc/{def}/part with com.xyz.Catalog title \"Another resource\"%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertTrue("Failed to return a statement", !result.isEmpty());
 		Resource r = (Resource)result.get(0);
 		assertEquals("Failed to match id", "/abc/{def}/part", r.getId());
 		assertEquals("Failed to match widget name",	"com.xyz.Catalog", r.getWidgetName());
 		assertEquals("Failed to match representation type", "xhtml", r.getRepresentationFormat());
 		assertEquals("Failed to match title ", "Another resource", r.getTitle());
 	}
 	
 	@Test
 	public void test2Vars() throws IOException, RecognitionException {
 		String input = "{{foobar}}{{crowbar}}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return 2 variables", 2, result.size());
 		assertTrue("Failed to match variable",
 				result.contains(fac.createVariable("foobar")));
 		assertTrue("Failed to match variable",
 				result.contains(fac.createVariable("crowbar")));
 	}
 
 	@Test
 	public void test2Codes() throws IOException, RecognitionException {
 		String input = "{%block a%}{%endblock%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return block", 1, result.size());
 		assertEquals("Failed to match block", fac.createBlock("a"), result.get(0));
 		assertEquals("Failed to return 1 block", 1, parser.getBlocks().size());
 	}
 
 	@Test
 	public void testblockendblockwspace() throws IOException,
 			RecognitionException {
 		String input = "{%block a%}{%endblock asb %}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return 1 block", 1, result.size());
 		assertEquals("Failed to match block", fac.createBlock("a"), result.get(0));
 		assertEquals("Failed to match size of block", 0,
 				((Block) result.get(0)).getStatements().size());
 	}
 
 	@Test
 	public void testblockendblockwspace2() throws IOException,
 			RecognitionException {
 		String input = "{%block a%}{%endblock asb %} {%block b%} foobar {%endblock b%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return 2 code segment", 3, result.size());
 		assertEquals("Failed to match block", fac.createBlock("a"), result.get(0));
 		assertEquals("Failed to match chunk", " ",
 				((Chunk) result.get(1)).getValue());
 		Block expBlock = fac.createBlock("b");
 		expBlock.addStatement(fac.createChunk(" foobar "));
 		assertEquals("Failed to match block", expBlock, result.get(2));
 
 	}
 
 	@Test
 	public void testfailblockendblockwspace() throws IOException,
 			RecognitionException {
 		String input = "{%block a,bc%}{%endblock asbc %}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
		parser.getMain();
 
 //		assertEquals("Failed to return exception", 1, lexer.getExceptions()
 //				.size());
 
 	}
 
 	@Test
 	public void testfor() throws IOException, RecognitionException {
 		String input = "{% for key in list %}{%endfor%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return for-endfor segment", 1, result.size());
 		ForLoop f = fac.createForLoop("key^^list^");
 		ForLoop actual = (ForLoop) result.get(0);
 		assertEquals("Failed to match for key", f.getKey(), actual.getKey());
 		assertEquals("Failed to match for collection", f.getCollection(),
 				actual.getCollection());
 	}
 
 	@Test
 	public void testfor2() throws IOException, RecognitionException {
 		String input = "{% for key , value in list %}{% endfor %}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return for-endfor segment", 1, result.size());
 		ForLoop f = fac.createForLoop("key^value^list^");
 		ForLoop actual = (ForLoop) result.get(0);
 		assertEquals("Failed to match for key", f.getKey(), actual.getKey());
 		assertEquals("Failed to match for collection", f.getCollection(),
 				actual.getCollection());
 		assertEquals("Failed to match for collection", f.getValue(),
 				actual.getValue());
 
 	}
 
 	@Test
 	public void testfor3() throws IOException, RecognitionException {
 		String input = "{% for key , value in list reversed%} andiamo {%rgroup foo %}{%resource.xhtml foo%}{%endrgroup foo%}{%endfor%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to return for-chunk-endfor segment", 1,
 				result.size());
 		ForLoop f = fac.createForLoop("key^value^list^reversed");
 		f.addStatement(fac.createChunk(" andiamo "));
 		ResourceGroup e = fac.createResourceGroup("foo");
 		e.addStatement(fac.createResourceXhtml("foo"));
 		f.addStatement(e);
 		ForLoop actual = (ForLoop) result.get(0);
 		assertEquals("Failed to match for key", f.getKey(), actual.getKey());
 		assertEquals("Failed to match for collection", f.getCollection(),
 				actual.getCollection());
 		assertEquals("Failed to match for collection", f.getValue(),
 				actual.getValue());
 
 	}
 
 	@Test
 	public void testmultwithspaces() throws IOException, RecognitionException {
 		String input = "html{% block abc %}memyself and I{{bar}}{{bond}}{%endblock 200 %}html2{foobar}%200{adnd%bar";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to find chunk", "html",
 				((Chunk) result.get(0)).getValue());
 		Block b = (Block) result.get(1);
 		assertEquals("Failed to match text in block", "memyself and I",
 				((Chunk) b.getStatements().get(0)).getValue());
 		StringBuilder sb = new StringBuilder();
 		for (int ix = 2; ix < result.size(); ix++) {
 			Chunk c = (Chunk) result.get(ix);
 			sb.append(c.getValue());
 		}
 
 		assertEquals("Failed to match chunk", "html2{foobar}%200{adnd%bar",
 				sb.toString());
 	}
 
 	@Test
 	@Ignore
 	public void testextendsvariable() throws IOException, RecognitionException {
 		String input = "html{%extends var%}\n{% block abc %}memyself and I{{bar}}{{bond}}{%endblock 200 %}html2{foobar}%200{adnd%bar";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		assertEquals("Failed to find extends", "var",
 				parser.getSuperClassName());
 	}
 
 	@Test
 	public void testmultiplecrlf() throws IOException, RecognitionException {
 		String input = "\n\n\n\n";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("failed to match lf chunk",
 				TemplateUtil.literalize(input),
 				((Chunk) result.get(0)).getValue());
 	}
 
 	@Test
 	public void testSimpleConditional() throws IOException,
 			RecognitionException {
 		String input = "{%if var%}foobar {{var}}{%endif%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to match expected conditional", 1, result.size());
 		assertEquals("Failed to match type",
 				StatementType.conditionalstatement.name(), result.get(0)
 						.getType());
 		Conditional cond = (Conditional) result.get(0);
 		assertEquals("failed to match expression", "var", cond.getExpression());
 		assertEquals("There should be no statements in the else clause", 0,
 				cond.getElseClause().getStatements().size());
 		Block block = cond.getIfClause();
 		assertEquals("Failed to match statements in if clause", 2, block
 				.getStatements().size());
 		assertEquals("Failed to match chunk",
 				StatementType.chunkstatement.name(),
 				block.getStatements().get(0).getType());
 		assertEquals("Failed to match var",
 				StatementType.variablestatement.name(), block.getStatements()
 						.get(1).getType());
 	}
 
 	@Test
 	public void testMoreConditionals() throws IOException, RecognitionException {
 		String input = "{%if var.bar%}foobar {{var}}{%else%}barfoooo{%endif%}";
 		RSTLParser parser = createParser(input);
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to match expected conditional", 1, result.size());
 		assertEquals("Failed to match type",
 				StatementType.conditionalstatement.name(), result.get(0)
 						.getType());
 		Conditional cond = (Conditional) result.get(0);
 		assertEquals("failed to match expression", "var.bar",
 				cond.getExpression());
 		Block block = cond.getIfClause();
 		assertEquals("Failed to match statements in if clause", 2, block
 				.getStatements().size());
 		assertEquals("Failed to match chunk",
 				StatementType.chunkstatement.name(),
 				block.getStatements().get(0).getType());
 		assertEquals("Failed to match var",
 				StatementType.variablestatement.name(), block.getStatements()
 						.get(1).getType());
 		block = cond.getElseClause();
 		assertEquals("Failed to match statements in else clause", 1, block
 				.getStatements().size());
 		assertEquals("Failed to match chunk",
 				StatementType.chunkstatement.name(),
 				block.getStatements().get(0).getType());
 	}
 
 	@Test
 	public void testNestedConditionals() throws IOException,
 			RecognitionException {
 		String input = "{%if var%}foobar {{var}}{%else%}barfoooo{%if bar%}twobar{%endif%}{%endif%}";
 		CharStream stream = new ANTLRStringStream(input);
 		RSTLLexer lexer = new RSTLLexer(stream);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		RSTLParser parser = new RSTLParser(tokens);
 		
 		for(Object obj: tokens.getTokens()) {
 			System.out.println("Token:" + obj);
 		}
 
 		parser.rule();
 		List<Statement> result = parser.getMain();
 		assertEquals("Failed to match expected conditional", 1, result.size());
 		assertEquals("Failed to match type",
 				StatementType.conditionalstatement.name(), result.get(0)
 						.getType());
 		Conditional cond = (Conditional) result.get(0);
 		assertEquals("failed to match expression", "var", cond.getExpression());
 		Block block = cond.getIfClause();
 		assertEquals("Failed to match statements in if clause", 2, block
 				.getStatements().size());
 		assertEquals("Failed to match chunk",
 				StatementType.chunkstatement.name(),
 				block.getStatements().get(0).getType());
 		assertEquals("Failed to match var",
 				StatementType.variablestatement.name(), block.getStatements()
 						.get(1).getType());
 		block = cond.getElseClause();
 		assertEquals("Failed to match statements in else clause", 2, block
 				.getStatements().size());
 		assertEquals("Failed to match chunk",
 				StatementType.chunkstatement.name(),
 				block.getStatements().get(0).getType());
 		assertEquals("Failed to match if",
 				StatementType.conditionalstatement.name(), block
 						.getStatements().get(1).getType());
 		cond = (Conditional) block.getStatements().get(1);
 		assertEquals("failed to match expression", "bar", cond.getExpression());
 		block = cond.getIfClause();
 		assertEquals("Failed to match statements in if clause", 1, block
 				.getStatements().size());
 		assertEquals("Failed to match chunk",
 				StatementType.chunkstatement.name(),
 				block.getStatements().get(0).getType());
 
 	}
 
 	private RSTLParser createParser(String testString) throws IOException {
 		CharStream stream = new ANTLRStringStream(testString);
 		RSTLLexer lexer = new RSTLLexer(stream);
 		CommonTokenStream tokens = new CommonTokenStream(lexer);
 		RSTLParser parser = new RSTLParser(tokens);
 		return parser;
 	}
 
 	@Test
 	public void testResourceEquality() {
 		Resource r = new ResourceImpl("/faoobar/abb", null, "json", "r");
 		Resource r2 = new ResourceImpl("/faoobar/abb", null, "json", "r");
 		assertEquals("Resource not equal", r, r2);
 		r = new ResourceImpl("/foo", "xhtml");
 		r2 = new ResourceImpl("/foo", "xhtml");
 		assertEquals("Resource not equal", r, r2);
 		r = new ResourceImpl("/faoobar/abb", "cat", "json", null);
 		r2 = new ResourceImpl("/faoobar/abb", "cat", "json", null);
 		assertEquals("Resource not equal", r, r2);
 		r = new ResourceImpl("/faoobar/abb", "cat", "json", null);
 		r2 = new ResourceImpl("/faoobar/abb", "cat", "json", "bar");
 		assertTrue("Resource  equal", !r.equals(r2));
 		r = new ResourceImpl("/faoobar/abb", null, "json", "w");
 		r2 = new ResourceImpl("/faoobar/abb", "cat", "json", "w");
 		assertTrue("Resource  equal", !r.equals(r2));
 
 	}
 }
