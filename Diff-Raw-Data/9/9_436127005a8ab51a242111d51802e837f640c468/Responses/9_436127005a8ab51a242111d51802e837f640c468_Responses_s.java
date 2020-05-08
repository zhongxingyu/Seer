 package net.cheney.manhattan.application;
 
 import static net.cheney.snax.model.ProcessingInstruction.XML_DECLARATION;
 
 import java.nio.charset.Charset;
 
 import net.cheney.cocktail.application.Application;
 import net.cheney.cocktail.application.Environment;
 import net.cheney.cocktail.message.Response;
 import net.cheney.cocktail.message.Response.Status;
 import net.cheney.manhattan.resource.Elements.MULTISTATUS;
 import net.cheney.snax.model.Document;
 import net.cheney.snax.writer.XMLWriter;
 
 public class Responses {
 
 	private Responses() {} 
 	
 	private static final Charset UTF_8 = Charset.forName("utf-8");
 	
 	public static Application serverErrorNotImplemented() {
 		return new Application() {
 			
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.SERVER_ERROR_NOT_IMPLEMENTED).build();
 			}
 		};
 	}
 
 	public static Application clientErrorNotFound() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_NOT_FOUND).build();
 			}
 			
 		};
 	}
 
 	public static Application successMultiStatus(final MULTISTATUS multistatus) {
 		return new Application() {
 			@Override
 			public Response call(Environment env) {
 				final Document doc = new Document(XML_DECLARATION, multistatus);
 				return Response.builder(Status.SUCCESS_MULTI_STATUS).body(UTF_8.encode(XMLWriter.write(doc))).build();
 			}
 		};
 	}
 
 	public static Application clientErrorMethodNotAllowed() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED).build();
 			}
 			
 		};
 	}
 
 	public static Application successCreated() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.SUCCESS_CREATED).build();
 			}
 			
 		};
 	}
 
 	public static Application serverErrorInternal(final Throwable t) {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
				return Response.builder(Status.SERVER_ERROR_INTERNAL).body(t.toString()).build();
 			}
 			
 		};
 	}
 
 	public static Application clientErrorConflict() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_CONFLICT).build();
 			}
 			
 		};
 	}
 	
 	public static Application clientErrorBadRequest() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_BAD_REQUEST).build();
 			}
 			
 		};
 	}
 
 	public static Application clientErrorLocked() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_LOCKED).build();
 			}
 			
 		};
 	}
 
 	public static Application successNoContent() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.SUCCESS_NO_CONTENT).build();
 			}
 			
 		};
 	}
 
 	public static Application clientErrorPreconditionFailed() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_PRECONDITION_FAILED).build();
 			}
 			
 		};
 	}
 
 	public static Application clientErrorUnsupportedMediaType() {
 		return new Application() {
 
 			@Override
 			public Response call(Environment env) {
 				return Response.builder(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE).build();
 			}
 			
 		};
 	}
 }
