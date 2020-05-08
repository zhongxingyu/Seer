 package undotree;
 
 import java.util.*;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.text.Document;
 import javax.swing.text.BadLocationException;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 
 public class NodeComponent extends JPanel
 {
     private static final long serialVersionUID = 1L;
     private UndoNode undoNode;
 
     public NodeComponent(final UndoNode undoNode, final UndoController undoController)
     {
 	this.undoNode = undoNode;
 	setBorder(BorderFactory.createLineBorder(Color.black));
 	setOpaque(true);
 	if (undoNode.getType().equals("addition")){
 	    setBackground(Color.green);
 	}
 	else if (undoNode.getType().equals("deletion")){
 	    setBackground(Color.red);
 	}
 	else{
 	    setBackground(Color.gray);//top node
 	}
 	addMouseListener(new MouseAdapter(){
 		@Override
 		    public void mouseClicked(MouseEvent e)
 		{
 		    UndoNode currentNode = undoController.getUndoTree().getCurrentNode();
 		    List<UndoNode> pathToCurrent = undoNode.pathToNode(currentNode);
 		    Document currentDocument = undoController.getDocument();
 		    Document restoredDocument = restore(currentDocument, pathToCurrent);
 		    undoController.getView().setDocument(restoredDocument);
 		}
 	    });
     }
 
     public Document restore(Document document, List<UndoNode> pathToCurrent)
     {
 	for (int i=0; i<pathToCurrent.size()-1; i++){
 	    UndoNode node = pathToCurrent.get(i);
 	    if (node.getParent() == pathToCurrent.get(i+1)){
 		node.undo();
 	    }
 	    else{
 		node.redo();
 	    }
 	}
 	RSyntaxDocument newDocument = new RSyntaxDocument(SyntaxConstants.SYNTAX_STYLE_JAVA);
 	try{
 	    newDocument.insertString(0,document.getText(0,document.getLength()),null);
 	}
 	catch(BadLocationException e){}
 	if (pathToCurrent.size() > 1){
 	    for (int i=pathToCurrent.size()-2; i>=0; i--){
 		UndoNode node = pathToCurrent.get(i);
 		if(node.getParent() == pathToCurrent.get(i+1)){
 		    node.redo();
 		}
 		else{
 		    node.undo();
 		}
 	    } 
 	}
 	return newDocument;
     }
 
     @Override
 	public Dimension getPreferredSize()
     {
	return new Dimension(20, undoNode.getEditSize() + 1);
     }
 

 }
