 package fr.esiea.sd.greenrobot.website;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 
 import javax.servlet.AsyncEvent;
 import javax.servlet.AsyncListener;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.pdfbox.pdmodel.PDDocument;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.Maps;
 import com.google.common.util.concurrent.AsyncFunction;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.ListeningExecutorService;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.gson.Gson;
 
 import fr.esiea.sd.greenrobot.pdf_analysis.concurrent.AsynchronousPDDocumentLoader;
 import fr.esiea.sd.greenrobot.pdf_analysis.graph.KeywordsGraphBuilder;
 import fr.esiea.sd.greenrobot.website.handler.PDFRetrievalHandler;
 import fr.esiea.sd.greenrobot.website.json.Response;
 
 /**
  * Servlet implementation class PDFReceiver
  */
 @WebServlet("/PDFReceiver")
 public class PDFReceiver extends HttpServlet {
 	
 	private enum RequestType {
 		
 		PDF_RETRIEVAL_REQUEST,
 		JSON_REQUEST
 	}
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final ListeningExecutorService threadExecutor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(20));
     private static final Map<HttpSession, PDFAnalysisTask> tasks = Maps.newHashMap();
     
 	private final PDFRetrievalHandler retrievalHandler;
 	private final AsynchronousPDDocumentLoader pdfLoader;
 	
     /**
      * @see HttpServlet#HttpServlet()
      */
     public PDFReceiver() {
         super();
         
         this.retrievalHandler = new PDFRetrievalHandler();
         this.pdfLoader = new AsynchronousPDDocumentLoader(threadExecutor);
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.getWriter().printf("Hi, you !");
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		final HttpSession userSession = request.getSession();
 		
 		switch(getRequestType(request)) {
 		case JSON_REQUEST:
 			
 			PDFAnalysisTask runningTask = tasks.get(userSession);
 			Response client = new Response();
 			
 			if(runningTask.isAnalyzing()) {
 				client.
 					execute("updateProgress").
 					addArg("text", "Analyse en cours...").
 					addArg("progress", runningTask.getAnalysisProgression());
 			}
 			else {
 				
 				if(runningTask.didRetrievalFail()) {
 					Throwable failure = runningTask.getFailure();
 					client.
 					execute("updateProgress").
 					addArg("text", "La récupération du document a échoué ! ").
 					addArg("exception", "" + failure + " > " + failure.getMessage()).
 					addArg("stacktrace", failure.getStackTrace()).
 					breakUpdateLoop();
 				}
 					
 				else 
 					if(runningTask.isAnalysisDone())
 						client.execute("updateProgress").
 						addArg("text", "Analyse terminée").
 						addArg("progress", 100).
 						breakUpdateLoop().
 						launchTransition();
 					
 			}
 			
 			client.writeTo(response.getWriter());
 			
 			break;
 			
 		case PDF_RETRIEVAL_REQUEST: {
 
			
			
 			//We register a new PDFAnalysisTask to the user session
 			tasks.put(userSession, new PDFAnalysisTask(threadExecutor, true));
			userSession.setAttribute("task", tasks.get(userSession));
 			//Beware : Asynchronous black magic in use !
 			ListenableFuture<PDDocument> pdfDocRetrieval = Futures.transform(
 					this.retrievalHandler.retrieveFile(request, threadExecutor), //First we retrieve the File
 
 					new AsyncFunction<File, PDDocument>() { //Then we load the PDDocument
 
 						@Override
 						public ListenableFuture<PDDocument> apply(File pdfFile) throws Exception { 
 
 							return Futures.immediateFuture(PDDocument.load(pdfFile)); //This Future will be executed in the executor of our choice !
 							//return pdfLoader.loadAsync(pdfFile);
 						}
 					}, threadExecutor); //We choose here the executor we want.
 			
 			Futures.addCallback(pdfDocRetrieval, new FutureCallback<PDDocument>() {
 				
 				private PDFAnalysisTask getTask(HttpSession userSession) {
 					return tasks.get(userSession);
 				}
 				
 				@Override
 				public void onFailure(Throwable t) {
 					getTask(userSession).setFailure(t);
 				}
 
 				@Override
 				public void onSuccess(PDDocument result) {
 					getTask(userSession).setDocument(result);
 				}
 			}, threadExecutor);
 			
 			
 			//JSON style
 			new Response().
 				execute("updateProgress").
 				addArg("state", "OPENING_PDF_DOC").
 				addArg("text", "Ouverture du fichier...").
 				writeTo(response.getWriter());
 			
 			//Script style
 			/*response.getWriter().
 				append("function update() { updateProgress({text:'omg noob !'}); }\n").
 				append("setTimeout(update, 700);\n");
 				*/
 			
 			break;
 		}
 
 		default:
 			/* DAFUQ §? */
 			//throw new DafuqException(new BugInTheMatrixException(new DividedByZeroException("Impossibruuuuuu")));
 			break;
 
 		}
 	}
 
 	private RequestType getRequestType(HttpServletRequest request) {
 	
 		if(request.getContentType() == null)
 			return RequestType.JSON_REQUEST;
 		
 		if(request.getContentType().startsWith("multipart/form-data"))
 			return RequestType.PDF_RETRIEVAL_REQUEST;
 		
 		return RequestType.JSON_REQUEST;
 	}
 
 }
