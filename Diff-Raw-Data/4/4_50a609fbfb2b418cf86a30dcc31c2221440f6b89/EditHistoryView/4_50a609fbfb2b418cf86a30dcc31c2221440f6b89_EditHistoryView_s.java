 package edithistory;
 
 import java.util.List;
 import java.util.ArrayList;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.text.Document;
 import java.awt.LayoutManager;
 import java.awt.CardLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.event.*;
 import java.awt.Component;
 
 import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
 import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
 import org.fife.ui.rtextarea.RTextScrollPane;
 
 public class EditHistoryView extends JPanel
 {
     private static final long serialVersionUID = 1L;
     private JSplitPane splitPane;
     private RSyntaxTextArea textArea;
     private LayoutManager layout;
     private Box nodesView;
     private List<NodeComponent> nodes;
     private UndoController undoController;
     private NodeComponent clickedNode;
     private JTextArea comment;
 
     public EditHistoryView(UndoController undoController)
     {
 	this.undoController = undoController;
 	layout = new CardLayout();
 	setLayout(layout);
 
 	textArea = new RSyntaxTextArea();
 	textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
 	textArea.setCodeFoldingEnabled(true);
 	textArea.setAntiAliasingEnabled(true);
 	textArea.setEditable(false);
 	RTextScrollPane sp = new RTextScrollPane(textArea);
 	sp.setFoldIndicatorEnabled(true);
 	sp.setLineNumbersEnabled(true);
 
 	nodesView = new Box(BoxLayout.Y_AXIS);
 	nodes = new ArrayList<>();
 
 	comment = new JTextArea(3, 15);
 	comment.setEditable(false);
 	comment.getDocument().addDocumentListener(new MyDocumentListener());
 
 	JSplitPane rightBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, comment, sp);
        
 	JPanel rightSide = new JPanel(new GridBagLayout());
 	GridBagConstraints c = new GridBagConstraints();
 	c.fill = GridBagConstraints.HORIZONTAL;
 	JButton revert = new JButton("Revert");
 	c.weightx = 0.5;
 	c.gridx = 0;
 	c.gridy = 0;
 	rightSide.add(revert, c);
 	revert.addActionListener(new ActionListener(){
 		@Override
 		    public void actionPerformed(ActionEvent e)
 		{
 		    if (clickedNode != null)
 			clickedNode.revert();
 		}
 	    });
 
 	JButton revertAll = new JButton("Revert all");
 	c.gridx = 1;
 	rightSide.add(revertAll,c );
 	
 	c.anchor = GridBagConstraints.PAGE_END;
 	c.fill = GridBagConstraints.BOTH;
 	c.weightx = 0.0;
 	c.weighty = 1.0;
 	c.gridwidth = 2;
 	c.gridx = 0;
 	c.gridy = 1;
 	rightSide.add(rightBottom, c);
 
 	splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(nodesView), rightSide);
 	//left side should have the nodes
 	//right side should have the text pane with a little box above it
 	//where you can write a tag/comment/name
 	add(splitPane);
 	splitPane.setDividerLocation(70);
 	rightBottom.setDividerLocation(20);
     }
 
     private class MyDocumentListener implements DocumentListener
     {
 	@Override
 	    public void changedUpdate(DocumentEvent e){}
 	@Override
 	    public void insertUpdate(DocumentEvent e){update();}
 	@Override
 	    public void removeUpdate(DocumentEvent e){update();}
 	public void update()
 	{
 	    if (clickedNode != null){
 		clickedNode.setComment(comment.getText());
 	    }
 	}
     }
 
     public void addNode(Edit edit)
     {
 	NodeComponent newNode = new NodeComponent(edit, undoController);
 	nodesView.add(newNode);
 	nodes.add(newNode);
 	revalidate();
     }
 
     public void setDocument(Document doc)
     {
 	textArea.setDocument(doc);
 	//	textArea.setCaretPosition(textArea.getDocument().getLength());
     }
 
     public void setCaretPosition(int position)
     {
 	int length = textArea.getDocument().getLength();
 	if (0 <= position && position < length)
 	    textArea.setCaretPosition(position);
     }
 
     public void setClickedNode(NodeComponent n)
     {
 	clickedNode = n;
 	comment.setText(n.getComment());
 	comment.setEditable(true);
     }
 
     public NodeComponent getClickedNode()
     {
 	return clickedNode;
     }
 
     public void addCompoundNode(NodeComponent n1, NodeComponent n2)
     {
 	Component[] nodeComponents = nodesView.getComponents();
 	int index1 = -1;
 	int index2 = -1;
 	for (int i=0; i<nodeComponents.length; i++){
 	    if (nodeComponents[i] == n1){
 		index1 = i;
 	    }
 	    if (nodeComponents[i] == n2){
 		index2 = i;
 	    }
 	    if (index1 >=0 && index2 >=0)
 		break;
 	}
 	int lower = (index1 <= index2) ? index1 : index2;
 	int higher = (index1 <= index2) ? index2 : index1;
 	List<NodeComponent> nodes = new ArrayList<NodeComponent>();
	for (int i=lower; i<=higher; i++){
 	    nodes.add((NodeComponent) nodeComponents[i]);
 	    nodesView.remove(lower);
 	}
 	NodeComponent compound = new CompoundNodeComponent(undoController, nodes);
 	nodesView.add(compound, lower);
 	revalidate();
     }
 
     /*    public void addCompoundNode(CompoundEdit compound)
     {
 	Edit top = compound.getTop();
 	Edit bottom = compound.getBottom();
 	int index = 0;
 	NodeComponent topNode = nodes.get(index);
 	while (topNode.getEdit() != top){
 	    index++;
 	    topNode = nodes.get(index);
 	}
 	NodeComponent newNode = new NodeComponent(compound, undoController);
 	nodesView.add(newNode, index);
 	nodes.add(index, newNode);
 	revalidate();
 	index++;
 	NodeComponent toRemove = topNode;
 	while (toRemove.getEdit() != bottom){
 	    nodesView.remove(index);
 	    revalidate();
 	    nodes.remove(index);
 	    toRemove = nodes.get(index);
 	}
 	nodesView.remove(toRemove);
 	revalidate();
 	}*/
 
 }
