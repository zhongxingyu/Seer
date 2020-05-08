 package org.mapster.ast;
 
 import java.io.*;
 import java.net.URI;
 import java.util.*;
 
 import javax.tools.*;
 import javax.tools.JavaCompiler.CompilationTask;
 import javax.ws.rs.*;
 import javax.ws.rs.core.*;
 import javax.ws.rs.core.Response.Status;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.TransformerException;
 
 import org.mapster.myast.*;
 
 import com.google.gson.*;
 import com.sun.source.tree.CompilationUnitTree;
 import com.sun.source.util.*;
 
 @Path("/tojson")
 public class CompileResource {
 	@GET
 	@Produces(MediaType.APPLICATION_JSON)
 	public String test(){
 		return "[{\"NODE_TYPE\":\"class\",\"startPos\":31,\"endPos\":1301,\"name\":\"Mains\",\"members\":[{\"NODE_TYPE\":\"method\",\"startPos\":46,\"endPos\":89,\"name\":\"funksjon\",\"type\":{\"NODE_TYPE\":\"primitive\",\"startPos\":46,\"endPos\":50,\"value\":\"VOID\"},\"parameters\":[{\"NODE_TYPE\":\"variable\",\"startPos\":60,\"endPos\":65,\"name\":\"x\",\"type\":{\"NODE_TYPE\":\"primitive\",\"startPos\":60,\"endPos\":63,\"value\":\"INT\"}},{\"NODE_TYPE\":\"variable\",\"startPos\":67,\"endPos\":75,\"name\":\"b\",\"type\":{\"NODE_TYPE\":\"identifier\",\"startPos\":67,\"endPos\":73,\"value\":\"String\"}}],\"body\":{\"NODE_TYPE\":\"block\",\"startPos\":76,\"endPos\":89,\"statements\":[{\"NODE_TYPE\":\"assignment\",\"startPos\":80,\"endPos\":85,\"variable\":{\"NODE_TYPE\":\"identifier\",\"startPos\":80,\"endPos\":81,\"value\":\"x\"},\"expr\":{\"NODE_TYPE\":\"literal\",\"startPos\":84,\"endPos\":85,\"type\":\"INT_LITERAL\",\"value\":\"5\"}}]}},{\"NODE_TYPE\":\"method\",\"startPos\":93,\"endPos\":210,\"modifiers\":{\"NODE_TYPE\":\"modifiers\",\"startPos\":93,\"endPos\":106,\"modifiers\":[\"public\",\"static\"]},\"name\":\"main\",\"type\":{\"NODE_TYPE\":\"primitive\",\"startPos\":107,\"endPos\":111,\"value\":\"VOID\"},\"parameters\":[{\"NODE_TYPE\":\"variable\",\"startPos\":117,\"endPos\":128,\"name\":\"args\",\"type\":{\"NODE_TYPE\":\"identifier\",\"startPos\":117,\"endPos\":123,\"value\":\"String\"}}],\"body\":{\"NODE_TYPE\":\"block\",\"startPos\":129,\"endPos\":210,\"statements\":[{\"NODE_TYPE\":\"variable\",\"startPos\":133,\"endPos\":143,\"name\":\"x\",\"type\":{\"NODE_TYPE\":\"primitive\",\"startPos\":133,\"endPos\":136,\"value\":\"INT\"},\"initializer\":{\"NODE_TYPE\":\"literal\",\"startPos\":141,\"endPos\":142,\"type\":\"INT_LITERAL\",\"value\":\"3\"}},{\"NODE_TYPE\":\"assignment\",\"startPos\":146,\"endPos\":151,\"variable\":{\"NODE_TYPE\":\"identifier\",\"startPos\":146,\"endPos\":147,\"value\":\"x\"},\"expr\":{\"NODE_TYPE\":\"literal\",\"startPos\":150,\"endPos\":151,\"type\":\"INT_LITERAL\",\"value\":\"5\"}},{\"NODE_TYPE\":\"assignment\",\"startPos\":155,\"endPos\":161,\"variable\":{\"NODE_TYPE\":\"identifier\",\"startPos\":155,\"endPos\":156,\"value\":\"x\"},\"expr\":{\"NODE_TYPE\":\"literal\",\"startPos\":159,\"endPos\":161,\"type\":\"INT_LITERAL\",\"value\":\"10\"}},{\"NODE_TYPE\":\"assignment\",\"startPos\":165,\"endPos\":171,\"variable\":{\"NODE_TYPE\":\"identifier\",\"startPos\":165,\"endPos\":166,\"value\":\"x\"},\"expr\":{\"NODE_TYPE\":\"literal\",\"startPos\":169,\"endPos\":171,\"type\":\"INT_LITERAL\",\"value\":\"21\"}},{\"NODE_TYPE\":\"if\",\"startPos\":175,\"endPos\":197,\"condition\":{\"NODE_TYPE\":\"parenthesized\",\"startPos\":177,\"endPos\":186,\"body\":{\"NODE_TYPE\":\"binary\",\"startPos\":178,\"endPos\":185,\"type\":\"EQUAL_TO\",\"left_op\":{\"NODE_TYPE\":\"identifier\",\"startPos\":178,\"endPos\":179,\"value\":\"x\"},\"right_op\":{\"NODE_TYPE\":\"literal\",\"startPos\":183,\"endPos\":185,\"type\":\"INT_LITERAL\",\"value\":\"21\"}}},\"then\":{\"NODE_TYPE\":\"assignment\",\"startPos\":190,\"endPos\":196,\"variable\":{\"NODE_TYPE\":\"identifier\",\"startPos\":190,\"endPos\":191,\"value\":\"x\"},\"expr\":{\"NODE_TYPE\":\"literal\",\"startPos\":194,\"endPos\":196,\"type\":\"INT_LITERAL\",\"value\":\"13\"}}},{\"NODE_TYPE\":\"assignment\",\"startPos\":200,\"endPos\":206,\"variable\":{\"NODE_TYPE\":\"identifier\",\"startPos\":200,\"endPos\":201,\"value\":\"x\"},\"expr\":{\"NODE_TYPE\":\"literal\",\"startPos\":204,\"endPos\":206,\"type\":\"INT_LITERAL\",\"value\":\"22\"}}]}}]},{\"NODE_TYPE\":\"class\",\"startPos\":1303,\"endPos\":1317,\"name\":\"Bla\",\"members\":[]}]";
 	}
 	
 	@Path("{filename}")
 	@POST
 	@Consumes("text/x-java")
 	@Produces(MediaType.APPLICATION_JSON)
 	public Response getJson(@PathParam("filename") String filename, String sourcecode) {
 		CompileErrorListener diag = new CompileErrorListener();
 		String json = ""; 
 		try {
 			json = compile(filename, sourcecode, diag);
 		} catch (Exception | Error e) {
 			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("{error: \"unable to build AST\", details: \""+e+"\"}").build();
 		}
 		if(diag.isError()){
 			Gson gson = new Gson();
 			return Response.status(Status.BAD_REQUEST).entity(gson.toJson(diag.diags)).build();
 		}
 		
 		return Response.ok(json).build();
 	}
 	
 	private String compile(String filename, String sourcecode, CompileErrorListener diag) throws IOException, ParserConfigurationException, TransformerException, WriteToStreamFailure {
 		Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(new JavaSourceString(filename, sourcecode));
 		
 		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
 		CompilationTask task = compiler.getTask(null, null, diag, null, null, compilationUnits);		
		JavacTask javacTask = (JavacTask) task;

 		Trees trees = Trees.instance(task);
 		JsonDocument jsonDoc = new JsonDocument();
 		
 		MyTreePathScanner<JsonElement> jsonScanner = new MyTreePathScanner<>(jsonDoc);
 		for(CompilationUnitTree tree: javacTask.parse()){
 			jsonScanner.addUnitToDocument(tree, trees);
 		}
 
 		javacTask.analyze();
 		ByteArrayOutputStream os = new ByteArrayOutputStream();
 		jsonDoc.writeToStream(os);
 		return os.toString();
 	}
 	
 	static class JavaSourceString extends SimpleJavaFileObject {
 		private String sourcecode;
 
 		protected JavaSourceString(String filename, String sourcecode) {
 			super(URI.create("string://java/source/"+filename), Kind.SOURCE);
 			this.sourcecode = sourcecode;
 		}
 		
 		@Override
 		public CharSequence getCharContent(boolean ignoreEncodingErrors){
 			return sourcecode;
 		}
 	}
 	
 	static class CompileErrorListener implements DiagnosticListener<JavaFileObject> {
 		List<ErrorDiag> diags = new LinkedList<>();
 		
 		public boolean isError(){
 			return !diags.isEmpty();
 		}
 
 		@Override
 		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
 			diags.add(new ErrorDiag(diagnostic.getKind(), diagnostic.getMessage(null)));
 		}
 		
 	}
 	
 	static class ErrorDiag {
 		final String kind;
 		final String message;
 
 		public ErrorDiag(Diagnostic.Kind kind, String message) {
 			this.kind = kind.toString();
 			this.message = message;
 		}
 		
 	}
 }
