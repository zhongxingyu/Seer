 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package coq;
 
 import edu.uci.ics.jung.algorithms.filters.VertexPredicateFilter;
 import edu.uci.ics.jung.algorithms.layout.FRLayout;
 import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
 import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.SparseMultigraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import edu.uci.ics.jung.visualization.renderers.Renderer;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Paint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import org.apache.commons.collections15.Predicate;
 import org.apache.commons.collections15.Transformer;
 import org.jgrapht.alg.StrongConnectivityInspector;
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.SimpleDirectedGraph;
 import org.netbeans.api.actions.Openable;
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
 import org.openide.loaders.DataObjectNotFoundException;
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
         iconBase = "coq/coq.gif",
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
 public class cqDataObject extends MultiDataObject implements KeyListener, UndoableEditListener, DocumentListener, MouseListener{
 
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
 
     void stopRequest()
     {
         batchCompile.requestStopping();
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
     
     boolean isJumpDefnShortcut(KeyEvent ke)
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
 
     static String getFocussedWord(Object src)
     {
         String str=getSelectedWord(src);
         // if nothins is selected, return the word at cursor
         if(str.isEmpty())
             str=getWordAtCursor(src);
         
         return str;
     }
     
     String getTextSelectedInEditor()
     {
        return getEditor().getOpenedPanes()[0].getSelectedText();
     }
     void fixSelectedCode()
     {
       String prefix="((Definition)|(Fixpoint)|(Lemma)|(Theorem)|(Inductive))";
       Pattern pat=Pattern.compile(prefix+" ([\\w]*)");
       Matcher mat=pat.matcher(getEntireText());
       dbugcontents="";
       while(mat.find())
       {
           String [] frags=mat.group().trim().split(" ");
           String mesg="Print "+frags[1]+".";
           CoqTopXMLIO.CoqRecMesg query = getCoqtop().query(mesg);
           if(query.success)
               dbugcontents=dbugcontents+query.conciseReply;
           
       }
           uiWindow.enableCompileButtonsAndShowDbug();
 /*      String sellection = getTextSelectedInEditor();
       String [] sp=sellection.split("[ :]", 3);
       String lemmaname = "";
       if(sp[0].equals("Lemma") ||sp[0].equals("Theorem"))
          lemmaname= sp[1].replace("lsubst","lsubst_aux");
                  
       insertStringAtCursor("Proof.\n"
               + "  intros. change_to_lsubst_aux2.\n"
               + "  apply "+lemmaname+";try(sp;fail);\n" +
 "  try(apply disjoint_sub_as_flat_map;disjoint_reasoning).\n"
               + "Qed.\n"
               + "");
       
       String change=sellection.replace("lsubst","lsubst_aux");
       insertStringAtCursor(change);*/
       
       
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
         else if(isPrintShortcut(ke))
         {
             if(uiWindow!=null) 
             {
                 
                 String word;
                 String query;
                 word=getSelectedWord(ke.getSource());
                 /**
                  * the only reason when one 
                  * would take the pain to select
                  * and not just click at the word,
                  * is when the selection is more
                  * complicated than a single word.
                  * In this case, Print might not
                  * make sens
                  */
                 if(word!=null && (!word.isEmpty()))
                 {
                     query="Check ("+word+").";
                 }
                 else
                 {
                     word= getWordAtCursor(ke.getSource());
                     query="Print "+word+".";
                     /**
                      * print already shows the info that Check shown
                      */
                 }
                     
                 if(!word.isEmpty())
                 {
                     uiWindow.setQuery(query);
                     handleQuery();
                 }
             }
         }
         else if(isJumpDefnShortcut(ke))
         {
             String word;
             String query;
             word= getWordAtCursor(ke.getSource());
             query="Locate "+word+".";
             uiWindow.setQuery(query);
             handleQuery();
             
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
 
     int  getDocLength()
     {
         return getDocument().getEndPosition().getOffset();
     }
     public /*synchronized*/ void setHighlight(int start, int end)
     {
         uiWindow.setProgressText(((float) getCompiledOffset())/ getDocLength() ); 
         retb.clear();
         if(ProofError.DARK)
             retb.addHighlight(start, end, compiledCodeAttrDark);
         else    
             retb.addHighlight(start, end, compiledCodeAttr);
     }        
 
 /*    public synchronized void  setHighlightHelper(int start, int end)
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
     
         public void setHighlightBad(final int start, final int end)
     {
             SwingUtilities.invokeLater(new Runnable () {
 
                 @Override
                 public void run() {
                     setHighlightHelper(start, end);
                 }
             });
         
     }
 */
     /**
      * retb already seems to have a lock; as it seems from debug
      * @param start
      * @param end 
      */
     public /*synchronized*/ void addErrorHighlight(int start, int end)
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
             if(offset+1<de.getDocument().getEndPosition().getOffset())
                 offset=offset+1; // to handle backspace before a dot
             handleCompileToOffset(offset);
             lastCharIsDot=false;
         }
         else
         {
             try {
                 String insert=getDocument().getText(offset, de.getLength());
                 if(lastCharIsDot&&Character.isWhitespace(insert.charAt(0)))
                 {
                     if(offset+1<de.getDocument().getEndPosition().getOffset())
                         offset=offset+1; // to handle backspace before a dot
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
         if(offset+1<de.getDocument().getEndPosition().getOffset())
             offset=offset+1; // to handle backspace before a dot
         if(offset<getCompiledOffset())
         {
             handleCompileToOffset(offset);
         }
     }
 
     @Override
     public void changedUpdate(DocumentEvent de) {
         de.getType(); // just to find out when it gets triggered
     }
 
 
 
     class OffsetTime implements Comparable<OffsetTime>
     {
         int offset;
         int duration;
 
         @Override
         public int compareTo(OffsetTime t) {
             return (int) (t.duration-duration);
         }
         
         boolean compileStepAndMeasureTime()
         {
             long startTime=System.nanoTime();
             boolean success=compileStep();
             offset=getCompiledOffset();
            duration=(int) (System.nanoTime()-startTime);
             return success;
         }
     }
     
     ArrayList<OffsetTime> profInfo=null;
 
     void showProfilingInfo()
     {
         Collections.sort(profInfo);
         int size= 20;
         if(profInfo.size()<size)
             size=profInfo.size();
         Integer [] times=new Integer [size];        
         for(int i=0;i<size;i++)
         {
            times[i]=(profInfo.get(i).duration/1000000); // nano to milliseconds
         }
         final JList list = new JList(times); //data has type Object[]
         list.setVisibleRowCount(-1);
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.addListSelectionListener(new ListSelectionListener() {
 
           @Override
           public void valueChanged(ListSelectionEvent lse) {
               jumpToOffset(profInfo.get(list.getSelectedIndex()).offset);
           }
         });
         JDialog jd=new JDialog();
         jd.add(list);
         jd.pack();
         jd.setVisible(true);
     }
     
     /**
      * @return the coqtop
      */
     public CoqTopXMLIO getCoqtop() {
         if(coqtop==null)
         {
             try {
                 coqtop=new CoqTopXMLIO(fileObj.getParent());
             } catch (Exception ex) {
                 Exceptions.printStackTrace(ex);
              //   uiWindow.disableCompileButtons();
                 JOptionPane.showMessageDialog(null, "could not start [h|c]oqtop. Check path in Tools | Options| Misc| CoqOptions");
                 uiWindow.setDebugMesg("could not start [h|c]oqtop. Check path in Tools | Options| Misc| CoqOptions");
                 uiWindow.enableCompileButtonsAndShowDbug(); // down buttoon might have disabled it. this will allow user to fix problem
             }
         }
         
         return coqtop;
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
         if(me.getButton()==MouseEvent.BUTTON2)
         {
             String selection=getFocussedWord(me.getSource());
             if(selection!=null && !selection.isEmpty())
             {
                 insertStringAtCursor(selection);
             }
         }
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
     }
 
   /**
    * @return the fontDelta
    */
   public int getFontDelta() {
     return fontDelta;
   }
 
   public void incementFont() {
     fontDelta = fontDelta +1;
   }
 
   public void decrementFont() {
     fontDelta = fontDelta -1;
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
             getUiWindow().disableCompileButtons();
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
                 boolean profile= (uiWindow.isProfilingEnabled());
                 if(profile)
                 {
                     if(profInfo==null)
                         profInfo=new ArrayList<OffsetTime>();
                     else
                       profInfo.clear();
                 }
                 while (getCompiledOffset() < targetOffset.intValue() && (!stopRequest.get())) {
                     boolean success;
                     if(!profile)
                     {
                         success=compileStep();
                     }
                     else
                     {
                         OffsetTime ot = new OffsetTime();
                         success=ot.compileStepAndMeasureTime();
                         profInfo.add(ot);
                     }
                     if (success) {
                         change = true;
                     } else {
                         break;
                     }
                 }
                 
                 if(profile)
                 {
                     showProfilingInfo();
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
         
         /**
          * 
          * @return Top.v if it is in current file.
          * full source filename otherwise
          */
         public String getFileName(String name)
         {
             String [] parts=name.split(".");
             if(parts.length==2) //TODO: handle other cases
             {
                 return parts[0]+".v";
             }
             else
                 return null;
         }
         
         cqDataObject getDataObject(String filename)
         {
             FileObject fob=fileObj.getParent().getFileObject(filename,"v");
             //FileObject fob = FileUtil.toFileObject(FileUtil.normalizeFile(gifFile)); 
             if (fob != null) { 
                 try {
                     cqDataObject dob = (cqDataObject) DataObject.find (fob);
                     return dob;
                 } catch (DataObjectNotFoundException ex) {
                     Exceptions.printStackTrace(ex);
                 }
             }
             return null;
 //            OpenCookie oc = (OpenCookie) dob.getCookie (OpenCookie.class); 
 //            if (oc != null) 
 //            oc.open(); 
 //            }             
         }
         void handleQuery() {
             String sendtocoq = uiWindow.getQuery();
             boolean setCommand=sendtocoq.trim().startsWith("Set") || sendtocoq.trim().startsWith("Unset");
             
             CoqTopXMLIO.CoqRecMesg rec;
             if(setCommand)
               rec=getCoqtop().setOption(sendtocoq);
             else
               rec= getCoqtop().interpret(sendtocoq);
             
             if (rec.success) {
                 String reply=rec.nuDoc.getRootElement().getFirstChildElement("string").getValue();
                 String creply=reply;
                 String warnMesg="Warning: query commands should not be inserted in scripts";
                 if(reply.startsWith(warnMesg))
                     creply=reply.substring(warnMesg.length());
                             //+ "this might make IDE's estimation of coqtop's state inconsistent."
                             //+ "you might want to save the file and restart the IDE");
                 setDbugcontents(creply);
                 
                 if(!setCommand)
                 {
                   rewindCoqtopForQuery();                
                   uiWindow.saveQuery(sendtocoq);
                 }
                 
                 if(!sendtocoq.trim().startsWith("Locate"))
                   return;
                 
                 String ctreply=creply.trim();
                // ctreply.sp
                 String [] frags=ctreply.split("[\\s]");
                 String prefix="";
                if(frags.length>1)
                {
                    if(frags[0].equals("Inductive"))
                    {
                        prefix=frags[0];
                    }
                    else if(frags[0].equals("Constant"))
                    {
                        prefix="((Definition)|(Fixpoint)|(Lemma)|(Theorem))";
                    }
                    else if(sendtocoq.startsWith("Locate"))
                     {
                       // this part is useful if the definition is the current file and
                       // is a tactic or outside the compiled region
                       prefix = "((Definition)|(Fixpoint)|(Lemma)|(Theorem)|(Ltac)|(Tactic[\\s]*Notation[\\s]*\"))";
                       frags=new String[2];
                       frags[0]="";
                       String[] parts = sendtocoq.split(" ");
                       String obname = parts[1];
                       frags[1]="Top."+obname;
                     }
                    
                    if(!prefix.isEmpty())
                    {
                         String [] parts=frags[1].split("\\.");
                         final cqDataObject target;
                         if(parts.length==2) //TODO: handle other cases
                         {
                             String suffix=parts[1];
                             String query=prefix+"[\\s]*"+suffix+"[\\s:]";
                             if(parts[0].equals("Top"))
                             {
                                 target=getThisDataObject();
                             }
                             else
                             {
                                 String filename=parts[0];
                                 target=getDataObject(filename);
                             }
                             
                             if(target!=null)
                             {
                                 target.getLookup().lookup(Openable.class).open();
                                 //target.getDocument().gette
                                 final Matcher mjump=Pattern.compile(query).matcher(target.getEntireText());
                                 if(mjump.find())
                                 {
                                     SwingUtilities.invokeLater(new Runnable () {
 
                                         @Override
                                         public void run() {
                                         target.jumpToOffset(mjump.start());
                                         }
                                     });
                                     
                                     //target.get
                                 }
                                 
                             }
                             
                         }
                        
                    }
                }
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
         
         public /*synchronized*/ void setTargetOffset(int targetOffset) {
             if(targetOffset==0)
             {
                 System.out.println("received 0 as targetOffset");
                 this.targetOffset.set(0);
                 lastActionRequest.set(MOVE_TO_CURSOR_ACTION);
             }
             else
             {
                 this.targetOffset.set(targetOffset);
                 lastActionRequest.set(MOVE_TO_CURSOR_ACTION);
             }
             
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
     private int fontDelta=0;
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
     /**
      * file object denoting this file
      */
     private FileObject fileObj;
     class CoqError{
         public int startLoc;
         public int endLoc;
         public String errorMesg;
         
         public void highlight()
         {
             addErrorHighlight(startLoc, endLoc);
         }
     }
     
     String getEntireText()
     {
         try {
             return getDocument().getText(0, getDocument().getLength());
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
             return "";
         }
     }
     
     public cqDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
         super(pf, loader); 
         initialized=false;
         registerEditor("text/coq", true);
         fileObj=pf;
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
         {
             getEditor().getOpenedPanes()[0].addKeyListener(this);
           //  getEditor().getOpenedPanes()[0].addMouseListener(this);
         }
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
     
     void jumpToOffset(int offset)
     {
         getEditor().getOpenedPanes()[0].getCaret().setDot(offset);
     }
     void jumpToCompileOffest()
     {        
          jumpToOffset(compiledOffset.intValue());
     }    
     
     final void initialize()
     {
         initialized=true;
         assignCookie();
      }
     
     public class InsertStringActionListener implements ActionListener
     {
         String str;
 
         public InsertStringActionListener(String str) {
             this.str=str;
         }
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             insertStringAtCursor(str);
         }
         
     }
         
     public class InsertAndCompileActionListener implements ActionListener
     {
         String str;
 
         public InsertAndCompileActionListener(String str) {
             this.str=str;
         }
 
         @Override
         public void actionPerformed(ActionEvent ae) {
             insertStringAtCompiledOffsetAndCompile(str);
             
         }
         
     }
      
     public StyledDocument getDocument()
     {
         return getEditor().getDocument();
     }
     /*synchronized*/ void updateGoal()
     {
         setGoal(getCoqtop().getGoal());
     }
     
     void insertStringAtCursor(String str)
     {
         try {        
             getDocument().insertString(getEditor().getOpenedPanes()[0].getCaret().getDot(), str, errorCodeAttr);
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
         }
     }
     void insertStringAndCompile(int startIndex, String str)
     {
         try {        
             getDocument().insertString(startIndex, str, errorCodeAttr);
             getEditor().getOpenedPanes()[0].getCaret().setDot(startIndex+str.length());
             handleDownToCursor();
         } catch (BadLocationException ex) {
             Exceptions.printStackTrace(ex);
         }
     }
 
     void insertStringAtCursorAndCompile(String str)
     {
         insertStringAndCompile(getEditor().getOpenedPanes()[0].getCaret().getDot(), str);
     }
 
     void insertStringAtCompiledOffsetAndCompile(String str)
     {
         insertStringAndCompile(getCompiledOffset(), str);
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
     
     /* synchronized */ boolean  compileStep() {
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
         
         CoqTopXMLIO.CoqRecMesg rec=getCoqtop().interpret(sendtocoq);
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
         CoqTopXMLIO.CoqRecMesg rec=getCoqtop().rewind(nofSteps);
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
         CoqTopXMLIO.CoqRecMesg rec=getCoqtop().rewind(1);
         assert(rec.success);
         assert(rec.getExtraRewoudSteps()==0);
     }
     
    class EdgeColor implements Transformer<String, Paint>
     {
 
       @Override
       public Paint transform(String edgeLabel) {
           if (edgeLabel.startsWith("s")) // <=
               return Color.RED;
           else if(edgeLabel.startsWith("n")) // <
               return Color.GREEN;
           else
               return Color.BLUE; // =
       }
     }
     
     static class FilterNodesOut implements Predicate<String>
     {
       Set<String> filterOut;
 
       public FilterNodesOut(Set<String> filterOut) {
         this.filterOut = filterOut;
       }
       
       @Override
       public boolean evaluate(String t) {
         return !(filterOut.contains(t));
       }
     }
 
     static class FilterKeepNodes implements Predicate<String>
     {
       Set<String> keepNodes;
 
       public FilterKeepNodes(Set<String> keepNodes) {
         this.keepNodes = keepNodes;
       }
       
       @Override
       public boolean evaluate(String t) {
         return (keepNodes.contains(t));
       }
     }
 
     static class FilterLeavesOut implements Predicate<String>
     {
       Graph<String,Object> graph;
 
       public FilterLeavesOut(Graph graph) {
         this.graph = graph;
       }
       
       @Override
       public boolean evaluate(String t) {
         return (graph.getNeighborCount(t)>1);
          // if a variable(vertex) has only 1 constraint, it can be trivially solved
       }
     }
 
     class VertexColor implements Transformer<String, Paint>
     {
       String lhs, rhs;
 
       public VertexColor(String lhs, String rhs) {
         this.lhs = lhs;
         this.rhs = rhs;
       }
       
       @Override
       public Paint transform(String i) {
         if(i.equals(lhs))
           return Color.CYAN;
         else if(i.equals(rhs))
           return Color.MAGENTA;
         else
           return Color.GREEN;
       }
       
     }
     JFrame topUnivs=null;
     void showGraph(Graph g, String lhs, String rhs, boolean newWindow)
     {
             FRLayout<String, String> layout=new FRLayout<String, String>(g);
             int numV=g.getVertexCount();
             layout.setSize(new Dimension(Math.max(600, numV), Math.max(600, numV)));
             VisualizationViewer<String, String> vv=
                         new VisualizationViewer<String, String>(layout);
             DefaultModalGraphMouse gm = new DefaultModalGraphMouse();
             gm.setMode(ModalGraphMouse.Mode.PICKING);
             vv.setGraphMouse(gm);
             vv.getRenderContext().setVertexDrawPaintTransformer(new VertexColor(lhs, rhs));
             vv.getRenderContext().setVertexFillPaintTransformer(new VertexColor(lhs, rhs));
             vv.getRenderContext().setArrowDrawPaintTransformer(new EdgeColor());
             vv.getRenderContext().setArrowFillPaintTransformer(new EdgeColor());
             vv.getRenderContext().setEdgeDrawPaintTransformer(new EdgeColor());
             vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
             vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
             
             JFrame jd;
             if(newWindow || topUnivs==null)
             {
                 jd=new JFrame();
                 jd.setTitle("Inconsistency");
                 jd.setSize(new Dimension(1000, 800));
                // jd.pack();
             }
             else
             {
                 jd=topUnivs;                
             }
             
             if(topUnivs==null&&!newWindow)
                 topUnivs=jd;
             
             jd.getContentPane().removeAll();
             jd.getContentPane().add(vv);
             jd.setVisible(true);
       
     }
     static class Constraint
     {
          public String lhs, rhs, edgetype;
 
         public Constraint(String lhs, String rhs, String line) {
           this.lhs = lhs;
           this.rhs = rhs;
           if(line.contains("<="))
             edgetype="n";
           else if (line.contains("<"))
             edgetype="s";
           else
             edgetype="e";
         }
 
         boolean involvesTop()
         {
             return lhs.startsWith("Top.") || rhs.startsWith("Top.");
         }
         
         public Constraint(String line) {
                 String frags[]=line.split("[<=]");
                 if(frags.length==3)
                 {
                     edgetype="n"; // not strict inequality (<=)
                     lhs=frags[0].trim();
                     assert(frags[1].isEmpty());
                     rhs=frags[2].trim();
                 } 
                 else
                 {
                     assert(frags.length==2);
                 
                     lhs=frags[0].trim();
                     rhs=frags[1].trim();                    
                     if (line.contains("<"))
                         edgetype = "s"; // strict inequality (<)
                     else
                     {
                         assert(line.contains("="));
                         edgetype = "e"; // equality (=)
                     }
                 }
         }
         
         public void addHelpfulNames(HashMap<String, String> help)
         {
             if(help==null|| help.isEmpty())
                 return;
             
             if(help.containsKey(lhs))
                 lhs=help.get(lhs);
             
             if(help.containsKey(rhs))
                 rhs=help.get(rhs);
             
         }
         public void addToGraph(Graph<String,String> g, int i)
         {
               String edgelabel=edgetype+i;
               assert(!lhs.isEmpty());
               assert(!rhs.isEmpty());
               g.addVertex(lhs);
               g.addVertex(rhs);
               //EdgeType edt=EdgeType.DIRECTED;
               
               //if(edgetype.equals("e"))
                 //edt=EdgeType.UNDIRECTED;
               
               assert(g.addEdge(edgelabel, rhs, lhs, EdgeType.DIRECTED ));// arrow can be read as <,i.e for a<b, arrow is at a.                
 
         }
        
     }
     
     
     Set<String> getVerticesToDiscard(Graph<String,String> g, String lhs, String rhs)
     {
       // wrong because equalities need to be bidirectional
         BFSDistanceLabeler<String,String> bfs=new BFSDistanceLabeler<String, String>();
         HashSet<String> roots=new HashSet<String>(2);
         roots.add(lhs);
         roots.add(rhs);
         bfs.labelDistances(g, roots);
         return bfs.getUnvisitedVertices();      
     }
     
     Set<String> getVerticesToKeepJgraph(DirectedSparseMultigraph<String,String> g,String vlhs, String vrhs)
     {
         SimpleDirectedGraph<String,String> jg =new SimpleDirectedGraph(DefaultEdge.class);
         Collection<String> edges = g.getEdges();
         for (String ed : edges)
         {
             String srcv = g.getSource(ed);
             String destv = g.getDest(ed);
             jg.addVertex(srcv);
             jg.addVertex(destv);
             jg.addEdge(srcv, destv,ed);
             if(ed.startsWith("e"))
                jg.addEdge(destv, srcv,ed);
         }
         
         StrongConnectivityInspector<String,String> insp=new StrongConnectivityInspector(jg);
         List<Set<String>> stronglyConnectedSets = insp.stronglyConnectedSets();
         dbugcontents=dbugcontents+"\n\n\n found #SCCs"+stronglyConnectedSets+"\n\n";
 
         for (Set<String> scc :stronglyConnectedSets)
         {
             if (scc.contains(vlhs) || scc.contains(vrhs))
                return scc;
         }
         
         return null;
         
     }
     SparseMultigraph<String, String> makeEqualitiesUndirected(DirectedSparseMultigraph<String,String> g)
     { 
         SparseMultigraph<String, String> ret=new SparseMultigraph<String, String>();
         Collection<String> edges = g.getEdges();
         for (String ed : edges)
         {
             String srcv = g.getSource(ed);
             String destv = g.getDest(ed);
             ret.addVertex(srcv);
             ret.addVertex(destv);
             EdgeType et= EdgeType.DIRECTED;
             if(ed.startsWith("e"))
                et=EdgeType.UNDIRECTED;
             ret.addEdge(ed,srcv, destv,et);
         }
         
         return ret;
     }
     
     DirectedSparseMultigraph<String,String> filterKeepAndVisualize(DirectedSparseMultigraph<String, String> g,Set<String> keepV,Constraint violatedConstr)
     {
         dbugcontents=dbugcontents+"\n init # nodes:"+g.getVertexCount()+" # edges:"+ g.getEdgeCount();
         dbugcontents=dbugcontents+"nodes left in SCC(#nodes):"+keepV.size();
         //init # nodes:1060 # edges:2911filtering removed(#nodes):943
         VertexPredicateFilter<String,String> vf =
                 //new VertexPredicateFilter<String, String>(new FilterNodesOut(discardv));
         new VertexPredicateFilter<String, String>(new FilterKeepNodes(keepV));
         DirectedSparseMultigraph<String,String> filtered=(DirectedSparseMultigraph<String,String>) vf.transform(g);
         uiWindow.enableCompileButtonsAndShowDbug();
 
         showGraph(makeEqualitiesUndirected(filtered),violatedConstr.lhs,violatedConstr.rhs,true);
         return filtered;
     }
     HashMap<String, String> helpfulConstrNames=null;
     
     void parseNames(String input)
     {
         if(helpfulConstrNames==null)
             helpfulConstrNames=new HashMap<String, String>();
         helpfulConstrNames.clear();
         
         String[] lines=input.split("\n");
         for(String line: lines)
         {
             String [] frags=line.split(":");
             helpfulConstrNames.put(frags[0],frags[1]);
             System.out.println("mapping " + frags[0] +" to "+frags[1]);
         }
     }
     
     void showTopUnivs()
     {
         CoqTopXMLIO.CoqRecMesg rec= getCoqtop().query("Print Universes.");        
         if(rec.success)
         {
             String constraints= rec.conciseReply;
             setDbugcontents(constraints);
             DirectedSparseMultigraph<String,String> g= new DirectedSparseMultigraph<String, String>();
             
             String[] lines=constraints.split("\n");
             String curLHS="";
             for(int i=0;i<lines.length;i++)
             {
                 String line=lines[i];
                 if(line.trim().isEmpty())
                     continue;
 
                 Constraint constr=new Constraint(line);
                 
                 if(constr.lhs.isEmpty())
                     constr.lhs=curLHS;
                 else
                     curLHS=constr.lhs;
                 
                 constr.addHelpfulNames(helpfulConstrNames);
                 if(constr.involvesTop())
                     constr.addToGraph(g, i+1);
             }
             showGraph(makeEqualitiesUndirected(g),"","",false);
         }
     }
         
     void debugUnivInconsistency()
     {
         Pattern pat = Pattern.compile("\\(cannot enforce ([\\w.]*) <= ([\\w.]*)\\)");
         Matcher mat = pat.matcher(dbugcontents);
         Constraint violatedConstr;
         
         if(mat.find())
         {
             String toParse=mat.group().substring("(cannot enforce".length());
             violatedConstr=new Constraint(mat.group(1),mat.group(2),mat.group());
             violatedConstr.addHelpfulNames(helpfulConstrNames);
 
         }
         else
           return;
         
         CoqTopXMLIO.CoqRecMesg rec= getCoqtop().query("Print Universes.");        
         if(rec.success)
         {
             String constraints= rec.conciseReply;
             setDbugcontents(constraints);
             // strict equality of edge is true
             DirectedSparseMultigraph<String,String> g= new DirectedSparseMultigraph<String, String>();
             violatedConstr.addToGraph(g, 0);
         //    g.addVertex(start);
         //    g.addVertex(end);
         //    g.addEdge(strict,start, end, EdgeType.DIRECTED);
             
             String[] lines=constraints.split("\n");
             String curLHS="";
             for(int i=0;i<lines.length;i++)
             {
                 String line=lines[i];
                 if(line.trim().isEmpty())
                     continue;
 
                 Constraint constr=new Constraint(line);
                 
                 if(constr.lhs.isEmpty())
                     constr.lhs=curLHS;
                 else
                     curLHS=constr.lhs;
                 
                 constr.addHelpfulNames(helpfulConstrNames);
                 constr.addToGraph(g, i+1);
             }
             showGraph(g,violatedConstr.lhs,violatedConstr.rhs,true);
           Set<String> keepV = getVerticesToKeepJgraph(g,violatedConstr.lhs,violatedConstr.rhs);
           filterKeepAndVisualize(g, keepV, violatedConstr);
           
           
             // filtering begins
             //Set<String> discardv=getVerticesToDiscard(g, violatedConstr.lhs, violatedConstr.rhs);
             
         }
         else
         {
             setDbugcontents(rec.toString());
         }
         
         
     }
     /**
      * final because it is called in the constructor
      */
     boolean keyListenerAssigned;
     final void assignCookie()
     {
         editor=getLookup().lookup(EditorCookie.class);
         assert(getEditor()!=null);
         //assert(getEditor().openDocument()!=null);
         StyledDocument doc;
         try {
             doc = getEditor().openDocument();
             assert(doc!=null);
             doc.addDocumentListener(this);
         } catch (IOException ex) {
             Exceptions.printStackTrace(ex);
         }
   //      getEditor().getDocument().add
         //getEditor().getDocument().addUndoableEditListener(this);
         //getEditor().getDocument().add
     }
     
     @Override
     protected int associateLookup() {
         return 1;
     }
 
     cqDataObject getThisDataObject()
     {
         return this;
     }
     void getContents() {
         
             setDbugcontents("successfully started CoqTop version: \n" +getCoqtop().getVersion());
             
         
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
