 package net.codjo.test.release.task.web;
 import com.gargoylesoftware.htmlunit.ElementNotFoundException;
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import java.io.IOException;
 import java.util.Map;
 import net.codjo.test.release.task.web.finder.ComponentFinder;
 import net.codjo.test.release.task.web.finder.ResultHandler;
 /**
  *
  */
 public class DragAndDrop implements WebStep {
     private DragAndDropArg origin;
     private DragAndDropArg destination;
 
 
     public DragAndDrop() {
     }
 
 
     public void proceed(WebContext context) throws IOException {
         validateParameters();
         HtmlElement originElement = findElement(context, origin);
         HtmlElement destinationElement = findElement(context, destination);
 
         context.setPage((HtmlPage)originElement.mouseDown());
         context.setPage((HtmlPage)destinationElement.mouseMove());
         context.setPage((HtmlPage)destinationElement.mouseUp());
     }
 
 
     void validateParameters() throws WebException {
     }
 
 
     public void addOrigin(DragAndDropArg arg) {
         this.origin = arg;
     }
 
 
     public void addDestination(DragAndDropArg arg) {
         this.destination = arg;
     }
 
 
     private HtmlElement findElement(WebContext context, DragAndDropArg element) {
         Map<String, String> parameters = DragAndDropArg.toArgumentMap(element);
 
         ComponentFinder<HtmlElement> finder = new ComponentFinder<HtmlElement>(parameters);
         final ResultHandler resultHandler = buildResultHandler();
         try {
             return finder.find(context, resultHandler);
         }
         catch (ElementNotFoundException e) {
             throw new WebException(resultHandler.getErrorMessage(e.getAttributeValue()));
         }
     }
 
 
     private ResultHandler buildResultHandler() {
         return new ResultHandler() {
             public void handleElementFound(HtmlElement element, String key) throws WebException {
             }
 
 
             public String getErrorMessage(final String key) {
                 return "Aucun lment trouv avec la cl: " + key;
             }
 
 
             public void handleElementNotFound(ElementNotFoundException e, String id) throws WebException {
                 throw new WebException(getErrorMessage(id));
             }
         };
     }
 }
