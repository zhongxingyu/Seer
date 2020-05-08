 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package coq;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.DefaultHighlighter;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import org.netbeans.api.editor.settings.AttributesUtilities;
 import org.netbeans.core.spi.multiview.MultiViewElement;
 import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
 import org.netbeans.spi.editor.highlighting.support.OffsetsBag;
 import org.openide.awt.ActionID;
 import org.openide.awt.ActionReference;
 import org.openide.awt.ActionReferences;
 import org.openide.cookies.EditorCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.MIMEResolver;
 import org.openide.loaders.DataObject;
 import org.openide.loaders.DataObjectExistsException;
 import org.openide.loaders.MultiDataObject;
 import org.openide.loaders.MultiFileLoader;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.NbBundle.Messages;
 import org.openide.util.RequestProcessor;
 import org.openide.windows.TopComponent;
 
 @Messages({
     "LBL_cq_LOADER=Files of cq"
 })
 @MIMEResolver.ExtensionRegistration(
         displayName = "#LBL_cq_LOADER",
         mimeType = "text/coq",
         extension = {"v"})
 @DataObject.Registration(
         mimeType = "text/coq",
         iconBase = "coq/1372847281_ok_16x16.gif",
         displayName = "#LBL_cq_LOADER",
         position = 300)
 @ActionReferences({
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
             position = 100,
             separatorAfter = 200),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
             position = 300),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
             position = 400,
             separatorAfter = 500),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
             position = 600),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
             position = 700,
             separatorAfter = 800),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
             position = 900,
             separatorAfter = 1000),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
             position = 1100,
             separatorAfter = 1200),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
             position = 1300),
     @ActionReference(
             path = "Loaders/text/coq/Actions",
             id =
             @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
             position = 1400)
 })
 public class cqDataObject extends MultiDataObject implements KeyListener, UndoableEditListener, DocumentListener{
 
     /**
      * @return the compiledOffset
      */
     public int getCompiledOffset() {
         return compiledOffset.intValue();
     }
 
     /**
      * @return the uiWindow
      */
     public ProofError getUiWindow() {
         return uiWindow;
     }
 
     /**
      * @param uiWindow the uiWindow to set
      */
     public void setUiWindow(ProofError uiWindow) {
         this.uiWindow = uiWindow;
     }
 
     /**
      * @return the dbugcontents
      */
     public String getDbugcontents() {
         return dbugcontents;
     }
 
     /**
      * @param dbugcontents the dbugcontents to set
      */
     public void setDbugcontents(String dbugcontents) {
         this.dbugcontents = dbugcontents;
     }
 
     /**
      * @return the editor
      */
     public EditorCookie getEditor() {
         if(!initialized)
             initialize();
         return editor;
     }
 
     /**
      * @return the goal
      */
     public nu.xom.Document getGoal() {
         return goal;
     }
 
     /**
      * @param goal the goal to set
      */
     public void setGoal(nu.xom.Document goal) {
         this.goal = goal;
     }
 
     @Override
     public void keyTyped(KeyEvent ke) {
         //JOptionPane.showMessageDialog(null, "NSA you edited!");
               //  highlighter.setHighlight(0, getCompiledOffset());
 
     }
 
     boolean isCompileToCursorShortcut(KeyEvent ke)
     {
         return (ke.isControlDown()&&ke.getKeyCode()==KeyEvent.VK_RIGHT);
     }
     
     boolean isUpShortcut(KeyEvent ke)
     {
         return (ke.isControlDown()&&ke.getKeyCode()==KeyEvent.VK_UP);
     }
 
     boolean isDownShortcut(KeyEvent ke)
     {
         return (ke.isControlDown()&&ke.getKeyCode()==KeyEvent.VK_DOWN);
     }
 
     boolean isSearchAboutShortcut(KeyEvent ke)
     {
         return (ke.isControlDown()&&ke.isAltDown()&&ke.getKeyCode()==KeyEvent.VK_L);
     }
 
     boolean isPrintShortcut(KeyEvent ke)
     {
         return (ke.isControlDown()&&ke.isAltDown()&&ke.getKeyCode()==KeyEvent.VK_P);
     }
     
     boolean isPrintSelectedShortcut(KeyEvent ke)
     {
        return (ke.isControlDown()&&ke.isAltDown()&&ke.getKeyCode()==KeyEvent.VK_O);
     }
     
     String getSelectedWord()
     {
         try
         {
             return getEditor().getOpenedPanes()[0].getSelectedText();
         }
         catch(NullPointerException ex)
         {
             return "";
         }
     }
     
     static boolean partOFId(char ch)
     {
         return ('a'<=ch && ch<='z')||('A'<=ch && ch<='Z') || ('0'<=ch && ch<='9') || (ch=='_');
     }
 
     static String getSelectedWord(Object src)
     {
         
         try
         {
             JTextComponent comp=(JTextComponent) src;
            return comp.getSelectedText();
         }
         catch(Exception ex) // cast exception
         {
             return "";
         }
     }
     
     static String getWordAtCursor(Object src)
     {
         
         try
         {
             JTextComponent comp=(JTextComponent) src;
             //return comp.getSelectedText()+":"+comp;
             int start=comp.getCaret().getDot();
             int pos=start;
             String ret="";
             // march forward
             try
             {
                 char ch;
                 while( partOFId(ch= comp.getText(pos, 1).charAt(0)))
                 {
                     ret=ret+ch;
                     pos++;
                 }
             }
             catch (Exception ex) // illegal position exception
             {
             }
             
             //march backward
             pos=start-1;
             try
             {
                 char ch;
                 while( partOFId(ch= comp.getText(pos, 1).charAt(0)))
                 {
                     ret=ch+ret;
                     pos--;
                 }
             }
             catch (Exception ex) 
             {
             }
             
             return ret;
         }
         catch(Exception ex) // cast exception
         {
             return "";
         }
     }
     
     @Override
     public void keyPressed(KeyEvent ke) {
         //keyboard shortcuts?
         if(isCompileToCursorShortcut(ke))
             handleDownToCursor();
         else if(isUpShortcut(ke))
             handleUpButton();
         else if(isDownShortcut(ke))
             handleDownButton();
         else if(isSearchAboutShortcut(ke))
         {
             if(uiWindow!=null) 
             {
                 String word=getWordAtCursor(ke.getSource());
                 if(!word.isEmpty())
                 {
                     uiWindow.setQuery("SearchAbout "+word+".");
                     handleQuery();
                 }
             }
         }            
         else if(isPrintShortcut(ke) ||isPrintSelectedShortcut(ke))
         {
             if(uiWindow!=null) 
             {
                 
                 String word;
                 if(isPrintShortcut(ke))
                     word= getWordAtCursor(ke.getSource());
                 else
                     word= getSelectedWord(ke.getSource());
                     
                 if(!word.isEmpty())
                 {
                     uiWindow.setQuery("Print "+word+".");
                     handleQuery();
                 }
             }
         }            
     }
 
     @Override
     public void keyReleased(KeyEvent ke) {
     }
 
     /**
      * @return the retb
      */
     public OffsetsBag getRetb() {
         return retb;
     }
 
     /**
      * @param retb the retb to set
      */
     public void setRetb(OffsetsBag retb) {
         this.retb = retb;
     }
 
     public synchronized void  setHighlightHelper(int start, int end)
     {
         try {
             //System.out.println(getDocument());
               this.getEditor().getOpenedPanes()[0].getHighlighter().removeAllHighlights();
               this.getEditor().getOpenedPanes()[0].getHighlighter().addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Color.red));
               
     //        retb.clear();
     //        if(ProofError.DARK)
     //            retb.addHighlight(start, end, compiledCodeAttrDark);
     //            retb.addHighlight(start, end, compiledCodeAttr);
     //            retb.addHighlight(start, end, compiledCodeAttr);
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
         }        
     }
     public synchronized void setHighlight(int start, int end)
     {
         retb.clear();
         if(ProofError.DARK)
             retb.addHighlight(start, end, compiledCodeAttrDark);
         else    
             retb.addHighlight(start, end, compiledCodeAttr);
     }        
     
     
     public void setHighlightBad(final int start, final int end)
     {
             SwingUtilities.invokeLater(new Runnable () {
 
                 @Override
                 public void run() {
                     setHighlightHelper(start, end);
                 }
             });
         
     }
 
     public synchronized void addErrorHighlight(int start, int end)
     {
         if(start<end)
             retb.addHighlight(start, end, errorCodeAttr);
     }
 
     @Override
     public void undoableEditHappened(UndoableEditEvent uee) {
        // setHighlight(0, getCompiledOffset());
     }
 
     @Override
     public void insertUpdate(DocumentEvent de) {
        // JOptionPane.showMessageDialog(null, "you inserted text");
         int offset=de.getOffset();
         if(offset<getCompiledOffset())
         {
             handleCompileToOffset(offset);
             lastCharIsDot=false;
         }
         else
         {
             try {
                 String insert=getDocument().getText(offset, de.getLength());
                 if(lastCharIsDot&&insert.equals(" "))
                 {
                     handleCompileToOffset(offset);                    
                 }
                 else
                 {
                     setHighlight(0, getCompiledOffset());                    
                 }
                 if(insert.equals("."))
                     lastCharIsDot=true;
                 else
                     lastCharIsDot=false;
             } catch (BadLocationException ex) {
                 setHighlight(0, getCompiledOffset());
                 lastCharIsDot=false;
                 Exceptions.printStackTrace(ex);
             }
         }
         
         
     }
 
     @Override
     public void removeUpdate(DocumentEvent de) {
         int offset=de.getOffset();
         lastCharIsDot=false;
         if(offset<getCompiledOffset())
         {
             handleCompileToOffset(offset);
         }
     }
 
     @Override
     public void changedUpdate(DocumentEvent de) {
         de.getType(); // just to find out when it gets triggered
     }
 
     class BatchCompile implements Runnable{
         private AtomicInteger targetOffset;
         private AtomicInteger pendingSteps;
         private AtomicBoolean stopRequest; // flag to request it to stop
         private AtomicInteger lastActionRequest;
         
         public static final int NOP_ACTION=0; 
         public static final int MOVE_TO_CURSOR_ACTION=1; 
         public static final int UP_DOWN_ACTION=2; 
         public static final int QUERY_ACTION=3; 
         
         
         public void resetPendingSteps()
         {
           //  pendingSteps=0;
             pendingSteps.set(0);
         }
 
         void decrementPendingSteps(int n)
         {
             pendingSteps.addAndGet(0-n);
             lastActionRequest.set(UP_DOWN_ACTION);
         }
         
         public void incrementPendingSteps()
         {
             pendingSteps.incrementAndGet();
             lastActionRequest.set(UP_DOWN_ACTION);
         }
                 
         public void requestStopping()
         {
             stopRequest.set(true);
         }
 
         public BatchCompile(int targetOffset) {
             pendingSteps=new AtomicInteger(0);
             this.targetOffset=new AtomicInteger(targetOffset);
             stopRequest=new AtomicBoolean(false);
             lastActionRequest=new AtomicInteger(NOP_ACTION);
         }
         
         @Override
         public void run() {
             int lastAc=lastActionRequest.intValue();
             lastActionRequest.set(NOP_ACTION); // actions take time to execute
             
             
             boolean change=false;
             if(lastAc==UP_DOWN_ACTION)
                 change=handleSteps();
             else if(lastAc==MOVE_TO_CURSOR_ACTION)
                 change=handleCompileToTargetPos();
             else if(lastAc==QUERY_ACTION)
                 handleQuery();
             getUiWindow().enableCompileButtonsAndShowDbug();
             if(change)
             {
                 if(uiWindow.isShowGoalChecked())
                 {
                     updateGoal();
                     uiWindow.showGoal();                    
                 }
             }
         }
 
         boolean handleSteps()
         {
             if(pendingSteps.intValue()<0)
                 return handleRewind(0-pendingSteps.intValue());
             
                 boolean change=false;
             while(pendingSteps.intValue()>0)
             {
                 if(compileStep())
                 {
                    pendingSteps.decrementAndGet();
                    change=true;
                 }
                 else
                 {
                     pendingSteps.set(0);
                     break;
                 }
             }
             if(lastError!=null)
             {
                 lastError.highlight();
             }
             return change;
         }
         
         boolean handleRewind(int nofSteps)
         {
             
              int rewSteps=(rewindCoqtop(nofSteps));
              resetPendingSteps();
              return rewSteps>0;
               
         }
         
         boolean handleUpCursor()
         {
             int curOffset=compiledOffset.intValue();
             int countPops=0;
             int lastElem=offsets.size()-1;
             while(targetOffset.intValue()<curOffset)
             {
                 //offsets.
                 curOffset=offsets.get(lastElem-countPops); // binary search can be done here
                 // do not pop here. handleRewind does that.
                 countPops=countPops+1;
             }
             if(countPops>0)
                 return handleRewind(countPops);
             else 
                 return false;
         }
         
         boolean handleCompileToTargetPos()
         {
             boolean change = false;
             if (getCompiledOffset() > targetOffset.intValue()) {
                 change = handleUpCursor();
             } 
             else {
                 while (getCompiledOffset() < targetOffset.intValue() && (!stopRequest.get())) {
                     if (compileStep()) {
                         change = true;
                     } else {
                         break;
                     }
                 }
                 if(lastError!=null)
                 {
                     lastError.highlight();
                 }
 
             }
             stopRequest.set(false);
             targetOffset.set(0);
             return change;
         }
         
         void handleQuery() {
             String sendtocoq = uiWindow.getQuery();
             CoqTopXMLIO.CoqRecMesg rec = coqtop.interpret(sendtocoq);
             if (rec.success) {
                 String reply=rec.nuDoc.getRootElement().getFirstChildElement("string").getValue();
                 String warnMesg="Warning: query commands should not be inserted in scripts";
                 if(!reply.startsWith(warnMesg))
                     JOptionPane.showMessageDialog(null, "you probably executed a non-query command as a query");
                             //+ "this might make IDE's estimation of coqtop's state inconsistent."
                             //+ "you might want to save the file and restart the IDE");
                 setDbugcontents(reply.substring(warnMesg.length()));
                 rewindCoqtopForQuery();
             } else {
                 String error= "probably too large output from Coq. If so, please ask developer to increase "
                         + "CoqRecMesg.BUF_SIZE and/or CoqRecMesg.NUM_TRIALS";
                 if(rec.nuDoc!=null)
                 {
                     error=rec.nuDoc.toXML();
                 }
                 setDbugcontents("sent: " + sendtocoq + " received " + error);
             }
         }
        
         public void requestQuery()
         {
             lastActionRequest.set(QUERY_ACTION);
         }
         
         public synchronized void setTargetOffset(int targetOffset) {
             this.targetOffset.set(targetOffset);
             lastActionRequest.set(MOVE_TO_CURSOR_ACTION);
             
         }
     }
     
      private RequestProcessor.Task batchCompileTask;
     private CoqTopXMLIO coqtop;
     private String dbugcontents;
     private AtomicInteger compiledOffset;
     private EditorCookie editor;
     private boolean initialized;   
     //private CoqHighlighter highlighter;
     private final RequestProcessor rp;
     private static final Pattern coqCommandEnd=Pattern.compile("([^.]\\.[\\s])");
     private static final Pattern coqComment=Pattern.compile("(\\(\\*)|(\\*\\))");
     private BatchCompile batchCompile;
     private ProofError uiWindow;
     private nu.xom.Document goal;
     private Stack<Integer> offsets;
     private OffsetsBag retb;
     private static final AttributeSet compiledCodeAttr =
             AttributesUtilities.createImmutable(StyleConstants.Background,
             new Color(200, 255, 200));
     private static final AttributeSet compiledCodeAttrDark =
             AttributesUtilities.createImmutable(StyleConstants.Background,
             new Color(0, 0, 150));
     private static final AttributeSet errorCodeAttr =
             AttributesUtilities.createImmutable(StyleConstants.Background,
             new Color(255, 100, 100));
     boolean lastCharIsDot;
     private CoqError lastError;
     private static String indentStrs = "-+*";
 
     class CoqError{
         public int startLoc;
         public int endLoc;
         public String errorMesg;
         
         public void highlight()
         {
             addErrorHighlight(startLoc, endLoc);
         }
     }
     
     public cqDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
         super(pf, loader); 
         initialized=false;
         registerEditor("text/coq", true);
         coqtop=new CoqTopXMLIO(pf.getParent());
         compiledOffset=new AtomicInteger(0);
         rp = new RequestProcessor(cqDataObject.class);
         batchCompile=new BatchCompile(0);
         batchCompileTask=rp.create(batchCompile, true);
         offsets=new Stack<Integer>();
         retb=null;
         lastCharIsDot=false;
         keyListenerAssigned=false;
     //    initialize();
     }
 
     void setKeyboardListener()
     {
         if(!keyListenerAssigned)
             getEditor().getOpenedPanes()[0].addKeyListener(this);
         keyListenerAssigned=true;
     }
     void scheduleCompilation()
     {
         batchCompileTask.schedule(10);
     }
 
     void handleCompileOffsetChange()
     {
         setHighlight(0, getCompiledOffset());        
     }
     /**
      * this should only be called after coq compilation/rewind.
      * else it can cause inconsistency between coqtop's and editor's state
      * @param change 
      */
     synchronized void addToCompiledOffset(int change)
     {
         assert(change>0);
         offsets.push(compiledOffset.intValue());
         compiledOffset.addAndGet(change);
         handleCompileOffsetChange();
     }
     
     synchronized void unwindOffsets(int n)
     {
         int newOffset=compiledOffset.intValue();
         for(int i=0;i<n;i++)
         {
             newOffset=offsets.pop();
         }
         compiledOffset.set(newOffset);
         handleCompileOffsetChange();
     }
     
     void jumpToCompileOffest()
     {        
          getEditor().getOpenedPanes()[0].getCaret().setDot(compiledOffset.intValue());
     }    
     
     final void initialize()
     {
         initialized=true;
         assignCookie();
 //        assert(highlighter!=null);
         
      }
     
     private StyledDocument getDocument()
     {
         return getEditor().getDocument();
     }
     synchronized void updateGoal()
     {
         setGoal(coqtop.getGoal());
     }
     
     int getOffsetToSend() {
 
         int offset = 0;
         int endPos = getDocument().getEndPosition().getOffset();
         String code="";
         try {
             code = getDocument().getText(getCompiledOffset(), endPos - getCompiledOffset());
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
             assert(false);
         }
         int unmatchedComLeft = 0;
         //int unmatchedStrLift = 0;
         Matcher commandEndMatcher = coqCommandEnd.matcher(code);
         int start=0;
         while (commandEndMatcher.find()) {
             if ((start==0))
             {
                 String segment=code.substring(0, commandEndMatcher.end());
                 String seg_trim=segment.trim();
                 // if the segment to next dot begins with - / * / + , send only to that part
                 for(int i=0;i<indentStrs.length();i++)
                 {
                     String ich=indentStrs.substring(i, i+1)+" ";
                     if(seg_trim.startsWith(ich))
                     {
                         offset= segment.indexOf(ich)+1;
                         return offset;
                     }
                 }
             }
             
             Matcher comments = coqComment.matcher(code.substring(start, commandEndMatcher.end()));
             start=commandEndMatcher.end();
             while (comments.find()) {
                 if (comments.group().equals("*)")) {
                     unmatchedComLeft = unmatchedComLeft - 1;
                 }
 
                 if (comments.group().equals("(*")) {
                     unmatchedComLeft = unmatchedComLeft + 1;
                 }
             }
             if (unmatchedComLeft == 0) {
                 offset = commandEndMatcher.end() - 1;
                 // code[offset]='.'
                 break;
             }
         }
 
 
 
 
         return offset;
 
 
     }
     
     synchronized boolean  compileStep() {
         if(!initialized)
             initialize();
         
         int dotOffset=getOffsetToSend();
         String sendtocoq="";
         try {
             sendtocoq = getDocument().getText(getCompiledOffset(), dotOffset);
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
             assert(false);
         }
         
         CoqTopXMLIO.CoqRecMesg rec=coqtop.interpret(sendtocoq);
         if(rec.success)
         {
       //          setDbugcontents(""+rec.nuDoc.toXML());
             setDbugcontents("received "+rec.nuDoc.toXML()+"sent: "+sendtocoq);
             addToCompiledOffset (dotOffset+1);
             lastError=null;
         }
         else
         {
             if(rec.nuDoc!=null)
             {
 //                setDbugcontents("received "+rec.nuDoc.toXML()+"sent: "+sendtocoq);
                 setDbugcontents(rec.nuDoc.getRootElement().getValue());
                 nu.xom.Element root=rec.nuDoc.getRootElement();
                 try
                 {
                     int startOffset=Integer.parseInt(root.getAttributeValue("loc_s"));
                     int endOffset=Integer.parseInt(root.getAttributeValue("loc_e"));
                     lastError=new CoqError();
                     lastError.startLoc= compiledOffset.intValue() + startOffset ;
                     lastError.endLoc= compiledOffset.intValue() + endOffset;
                 }catch(NumberFormatException ex)
                 {
                     lastError=new CoqError();
                     lastError.startLoc= compiledOffset.intValue() ;
                     lastError.endLoc= compiledOffset.intValue() + dotOffset;
                     
                 }
                 
             }
             else
             {
                 setDbugcontents("received null, sent: "+sendtocoq);
             }
         }
         return rec.success;
     }
 
     void handleDownButton()
     {
         setKeyboardListener();
         batchCompile.incrementPendingSteps();
         scheduleCompilation();
     }
     
     void handleQuery()
     {
         batchCompile.requestQuery();
         scheduleCompilation();
     }
     
     void handleCompileToOffset(int offset)
     {
         setKeyboardListener();
         batchCompile.setTargetOffset(offset);
         scheduleCompilation();
     }
     
     void handleDownToCursor()
     {
         int curPos=getEditor().getOpenedPanes()[0].getCaretPosition();
         handleCompileToOffset(curPos);
     }
     
     void handleUpButton()
     {
         batchCompile.decrementPendingSteps(1);
         scheduleCompilation();
     }
     void handleUppButton()
     {
         batchCompile.decrementPendingSteps(offsets.size());
         scheduleCompilation();
     }
 
     void handleBottomButton()
     {
         handleCompileToOffset(getDocument().getEndPosition().getOffset());
     }
     /**
      * 
      * @param nofSteps
      * @return the number of steps actually rewound
      *  INCLUDING the ones asked for(and extra steps)
      */
     int rewindCoqtop (int nofSteps)
     {
         CoqTopXMLIO.CoqRecMesg rec=coqtop.rewind(nofSteps);
         if(rec.success)
         {
             int actualSteps= rec.getExtraRewoudSteps()+nofSteps;
             unwindOffsets(actualSteps);
             return actualSteps;
                     
         }
         else
             return 0;
     }
     /**
      * same as rewindCoqtop, but since it is a query
      * entered externally(w.r.t the file editor), there
      * us no need to change editor state(highlighting)
      * @param nofSteps
      * @return the number of steps actually rewound
      *  INCLUDING the ones asked for(and extra steps)
      */
     void rewindCoqtopForQuery ()
     {
         CoqTopXMLIO.CoqRecMesg rec=coqtop.rewind(1);
         assert(rec.success);
         assert(rec.getExtraRewoudSteps()==0);
     }
     /**
      * final because it is called in the constructor
      */
     boolean keyListenerAssigned;
     final void assignCookie()
     {
         editor=getLookup().lookup(EditorCookie.class);
         assert(getEditor()!=null);
         assert(getEditor().getDocument()!=null);
         getEditor().getDocument().addDocumentListener(this);
   //      getEditor().getDocument().add
         //getEditor().getDocument().addUndoableEditListener(this);
         //getEditor().getDocument().add
     }
     
     @Override
     protected int associateLookup() {
         return 1;
     }
 
     void getContents() {
         
             setDbugcontents("successfully started CoqTop version: \n" +coqtop.getVersion());
             
         
     }
     @MultiViewElement.Registration(
             displayName = "#LBL_cq_EDITOR",
             iconBase = "coq/1372847281_ok_16x16.gif",
             mimeType = "text/coq",
             persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
             preferredID = "cq",
             position = 1000)
     @Messages("LBL_cq_EDITOR=Source")
     public static MultiViewEditorElement createEditor(Lookup lkp) {
         return new MultiViewEditorElement(lkp);
     }
 
     /**
      * @param highlighter the highlighter to set
      */
 //    public void setHighlighter(CoqHighlighter highlighter) {
 //        this.highlighter = highlighter;
 //    }
 
 }
