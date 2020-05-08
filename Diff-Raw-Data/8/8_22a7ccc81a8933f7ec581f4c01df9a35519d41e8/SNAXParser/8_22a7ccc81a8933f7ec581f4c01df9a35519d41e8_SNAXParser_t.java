 package net.sundell.snax;
 
 import java.io.Reader;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.stream.*;
 import javax.xml.stream.events.*;
 
 /**
  * SNAX parser that operates on top of a STaX XMLEventReader.
  * 
  * @param <T> Data object type that will be passed to parse calls
  */
 public class SNAXParser<T> {
 
     private static Logger logger = Logger.getLogger(SNAXParser.class.getName());
     
     private XMLInputFactory factory;
     private NodeModel<T> model;
 
     /**
      * Return a new SNAXParser using the specified model.
      * @param factory XMLInputFactory to use when creating input streams
      * @param model NodeModel that defines the state machine to use when parsing
      * @return a new parser
      */
     public static <T> SNAXParser<T> createParser(XMLInputFactory factory, NodeModel<T> model) {
         return new SNAXParser<T>(factory, model);
     }
     
     private SNAXParser(XMLInputFactory factory, NodeModel<T> model) {
         this.factory = factory;
         this.model = model;
     }
     
     /**
      * Get the NodeModel used by this parser.
      * @return model
      */
     public NodeModel<T> getModel() {
         return model;
     }
     
     /**
      * Set the NodeModel used by this parser.  This method may not be called 
      * once parsing has been started with a call to <code>startParsing()</code> or 
      * <code>parse()</code>.
      * 
      * @param model the model to use
      * @throws IllegalStateException if this method is called while parsing is underway
      */
     public void setModel(NodeModel<T> model) {
         checkState(xmlReader == null, "Model was changed while parsing is underway");
         this.model = model;
     }
     
     private XMLEventReader xmlReader;
     private T data;
     private boolean done;
     private boolean isIncremental = false;
     private Stack<ParseState> stack;
     private ParseState currentState;
     private Location currentLocation;
 
     /**
      * Begin incremental parsing of a data stream, represented by a Reader.  This will initialize
      * the parser but will not perform any parsing.  Callers should then use hasMoreEvents() and 
      * processEvent() to consume XML events until the document has been completely parsed.
      * 
      * @param reader XML content to process
      * @param data optional, user-defined object to be passed as an argument to ElementHandlers
      * @throws XMLStreamException if an error occurs during initialization
      */
     public void startParsing(Reader reader, T data) throws XMLStreamException {
         checkState(model != null, "No model was set");
         init(reader, data);
         isIncremental = true;
     }
     
     /**
      * Check to see if more XML remains to be processed.  This method may only be
      * called following a call to <code>startParsing()</code>.
      * @return true if more XML remains to be processed
      */
     public boolean hasMoreEvents() {
         checkState(xmlReader != null, "startParsing() was never called");
         checkState(isIncremental, "startParsing() was never called");
         return xmlReader.hasNext(); 
     }
     
     /**
      * Consumes the next XML event.  This method may only be
      * called following a call to <code>startParsing()</code>.
      * @return the XMLEvent that was processed
      * @throws XMLStreamException if there is an error with the underlying XML
      * @throws SNAXUserException if there is an error in an attached <code>ElementHandler</code>   
      * @throws NoSuchElementException if no XML remains to be processed
      */
     public XMLEvent processEvent() throws XMLStreamException, SNAXUserException {
         checkState(xmlReader != null, "startParsing() was never called");
         checkState(isIncremental, "startParsing() was never called");
         return processEvent(xmlReader.nextEvent());
     }
     
     /**
      * Parse a data stream, represented by a Reader, to completion.  This will process the entire
      * document and trigger any ElementHandler calls that result from applying the selectors defined
      * in the NodeModel.  The data parameter will be passed back as an argument to all ElementHandler
      * calls.
      *  
      * @param reader XML content to process
      * @param data optional, user-defined object to be passed as an argument to ElementHandlers
      * @throws XMLStreamException if there is an error with the underlying XML
      * @throws SNAXUserException if there is an error in an attached <code>ElementHandler</code>   
      */
     public void parse(Reader reader, T data) throws XMLStreamException, SNAXUserException {
         // TODO: this needs to catch concurrent parse attempts
         init(reader, data);
         for (XMLEvent event = xmlReader.nextEvent(); xmlReader.hasNext(); event = xmlReader.nextEvent()) {
             processEvent(event);
         }
     }
     
     private void init(Reader reader, T data) throws XMLStreamException {
         this.xmlReader = factory.createXMLEventReader(reader);
         this.data = data;
         stack = new Stack<ParseState>();
         currentState = new ParseState(model.getRoot(), model.getRoot().getDescendantRules(), null);
         currentLocation = null;
         done = false;
     }
 
    private XMLEvent processEvent(XMLEvent event) throws SNAXUserException {       
         try {
             int type = event.getEventType();
             currentLocation = event.getLocation();
             switch (type) {
             case XMLEvent.START_ELEMENT:
                 StartElement startEl = event.asStartElement();
                 if (logger.isLoggable(Level.FINE)) {
                     logger.fine("START: " + startEl.getName().getLocalPart());
                 }
                 checkState(!done, "Element started after end of document");
                 
                 NodeState<T> nextState = currentState.nodeState.follow(startEl);
                 if (nextState.equals(NodeState.EMPTY_STATE)) {
                     // Look for a match among the inherited descendant rules
                     for (NodeTransition<T> rule : currentState.deferredRules) {
                         if (rule.getTest().matches(startEl)) {
                             nextState = rule.getTarget();
                             break;
                         }
                     }
                 }
                 ParseState newState = new ParseState(nextState, 
                         CompoundIterable.prepend(nextState.getDescendantRules(), 
                                                  currentState.deferredRules), 
                         startEl);
                 stack.push(newState);
                 newState.nodeState.handleElementStart(startEl, data);
                 currentState = newState;
                 break;
             case XMLEvent.END_ELEMENT:
                 ParseState ended = stack.pop();
                 EndElement endEl = event.asEndElement();
                 if (logger.isLoggable(Level.FINE)) {
                     logger.fine("END: " + endEl.getName().getLocalPart());
                 }
                 ended.nodeState.handleElementEnd(endEl, data);
                 if (stack.empty()) {
                     // End of document!
                     this.done = true;
                 }
                 else {
                     currentState = stack.peek();
                 }
                 break;
             case XMLEvent.CHARACTERS:
                 currentState.nodeState.handleContents(currentState.element, 
                                             event.asCharacters(), data);
                 break;
             case XMLEvent.DTD:
                 model.handleDTD((DTD)event, data);
                 break;
             case XMLEvent.ENTITY_DECLARATION:
                 model.handleEntityDeclaration((EntityDeclaration)event, data);
                 break;
             case XMLEvent.ENTITY_REFERENCE:
                 model.handleEntityReference((EntityReference)event, data);
                 break;
             case XMLEvent.NOTATION_DECLARATION:
                 model.handleNotationDeclaration((NotationDeclaration)event, data);
                 break;
             }
             return event;
         }
         catch (SNAXUserException e) {
             e.setLocation(currentLocation);
             throw e;
         }
        catch (Exception e) {
            SNAXUserException se = new SNAXUserException(e);
            se.setLocation(currentLocation);
            throw se;
        }
     }
 
     private void checkState(boolean test, String message) {
         if (!test) {
             throw new IllegalStateException(message);
         }
     }
 
     /**
      * Holder for runtime state.
      */
     class ParseState {
         NodeState<T> nodeState;
         Iterable<NodeTransition<T>> deferredRules;
         StartElement element;
 
         ParseState(NodeState<T> nodeState, Iterable<NodeTransition<T>> deferredRules,
                    StartElement element) { 
             this.nodeState = nodeState;
             this.deferredRules = deferredRules;
             this.element = element;
         }
     }
 }
