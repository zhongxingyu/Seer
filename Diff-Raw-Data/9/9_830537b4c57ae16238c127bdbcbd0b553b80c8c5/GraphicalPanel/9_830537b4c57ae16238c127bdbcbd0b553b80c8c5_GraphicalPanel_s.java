 import javax.swing.*;
 import javax.swing.border.Border;
 
 import java.awt.*;
 import java.util.*;
 
 import model.*;
 
 public class GraphicalPanel extends JLayeredPane{
 	private Design design = new Design();
 	private Collection<Classes> classes = design.getAllClasses();
 	private Classes mouseSelectedClass = null;
 	
 
 
 	GraphicalPanel()
 	{
 		this.setSize(900, 600);
 		this.setPreferredSize(new Dimension(900, 600));
 		this.setOpaque(true);
 		this.setLayout(null);
 		Border loweredBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK);
 		this.setBorder(loweredBorder);
 		addTestItems();
 		drawClasses();
 	}
 
 	public void setMouseSelectedClass(Classes mouseSelectedClass) {
 		this.mouseSelectedClass = mouseSelectedClass;
 	}
 	
 	public Classes getMouseSelectedClass() {
 		return mouseSelectedClass;
 	}
 	
 	public Design getDesign()
 	{
 		return design;
 	}
 	  
 	public void paintComponent(Graphics g) {
 		this.removeAll(); 
 		super.paintComponent(g);
 		drawClasses();
 		drawLinks(g);
 	}
 	
 	public Classes findNearestClass(int x, int y) {
 		Classes c;
 		double minDist = Double.MAX_VALUE;
 		Classes minDistClass = null;
 		
 		for(Classes theClass: classes){
 			double distanceTo = theClass.distanceTo(x,y);
 			if(distanceTo < minDist) {
 				minDist = distanceTo;
 				minDistClass = theClass;
 			}
 		}
 		return minDistClass;
 	}
 	
 	private void addTestItems()
 	{
 		Classes testingClass = design.addClass(new Classes("Testing"));
 		testingClass.setPosition(new Point(400,100)); //Move it to a different posiiton
 		Method newMethod = new Method("addUser");
 		newMethod.setType("void");
 		newMethod.setAccessModifier(AccessModifier.PUBLIC);
 		testingClass.addField(newMethod);
 		
 		
 		Classes betterClass = design.addClass(new Classes("Better"));
 		Method betterMethod = new Method("addUser");
 		betterMethod.setType("void");
 		betterMethod.setAccessModifier(AccessModifier.PRIVATE);
 		betterClass.addField(betterMethod);
 		betterClass.addField(new Field("username"), "String", AccessModifier.PUBLIC);
 		
 		
 		Link testLink = new Link("testLink", testingClass, betterClass, Link.CARDINALITY_ONE, Link.CARDINALITY_MANY);
 		testingClass.addLink(testLink);
 		betterClass.addLink(testLink);
 		
 	}
 	
 	public void drawGraphicalPane()
 	{
 		drawClasses();
 	}
 	
 	public String getSymbolFromAccessModifier(int accessModifer)
 	{
 		switch (accessModifer)
 		{
 			case 0:
 				return "+";
 			case 1:
 				return "#";
 			case 2:
 				return "-";	
 			default:
 				return "";
 		}
 		
 	}
 	
 	private void drawClasses()
 	{
 	   	for(Classes theClass: classes)
 	   	{
 			JLabel drawnClass = drawClass(theClass);
 			this.add(drawnClass, theClass.getLayer());
 	   	}
 	}
 	
 	private Point getLinkLinePosition(Classes classA, Classes classB)
 	{
 		int xValue = (classA.getPosition().x)-(classB.getPosition().x);
 		int yValue = (classA.getPosition().y)-(classB.getPosition().y);
 		if(Math.abs(xValue)>Math.abs(yValue)){
 			int y = classA.getPosition().y;
 			if(xValue<0)
 				return new Point(classA.getPosition().x+classA.getDimension().width/2, y);
 			else
 				return new Point(classA.getPosition().x-classA.getDimension().width/2, y);
 		}else{
 			int x = classA.getPosition().x;
 			if(yValue<0)
 				return new Point(x, classA.getPosition().y+classA.getDimension().height/2);
 			else
 				return new Point(x, classA.getPosition().y-classA.getDimension().height/2);
 		}
 	}
 	
 	private void drawLink(Link theLink, Graphics g)
 	{
 		Point p1 = getLinkLinePosition(theLink.getClassA(), theLink.getClassB());
 		Point p2 = getLinkLinePosition(theLink.getClassB(), theLink.getClassA());
 		Graphics2D ig = (Graphics2D) g;
 		ig.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		ig.drawLine(p1.x, p1.y, p2.x, p2.y);
 
 	}
 	
 	private void drawLinks(Graphics g)
 	{
 	   	for(Classes theClass: classes)
 	   	{
 			for(Link theLink: theClass.getAllLinks())
 			{
 				if(theLink.getClassA()==theClass)
 				{
 					if(design.getAllClasses().contains(theLink.getClassB()))
 						drawLink(theLink, g);
 					else
 						theClass.removeLink(theLink.getLabel());
 				}
 			}
 	   	}
 	}
 	
 	private String getMethodsFieldsHTML(Field method)
 	{
 		String returnHTML = "";
 		Method theMethod = (Method) method;
 		for(Field theField: theMethod.getParameters())
 		{
 			returnHTML = returnHTML + theField.getLabel() + " : " + theField.getType() + ", ";
 		}
 		if(returnHTML.length() > 0)
 			return returnHTML.substring(0, returnHTML.length()-2);
 		else
 			return "";
 	}
 	
 	private String getClassesFeildsHTML(Classes theClass)
 	{
 		String returnMethods = "";
 		String returnParameters = "";
 		for(Field field: theClass.getFields())
 		{
 			if(field instanceof Method)
 			{
 				returnMethods += "<p style=\"margin: 0 10px; white-space: nowrap;\">" + getSymbolFromAccessModifier(field.getAccessModifier()) 
 						+ " " + field.getLabel() + "(" + getMethodsFieldsHTML(field) + ") : " + field.getType() + "</p>";
 			}
 			else
 			{
 				returnParameters += "<p style=\"margin: 0 10px; white-space: nowrap;\">" + getSymbolFromAccessModifier(field.getAccessModifier())
 						+ " " + field.getLabel() + " : " + field.getType() + "</p>";
 			}
 		}
 		return returnParameters + "<hr>" + returnMethods;
 	}
 	
     private JLabel drawClass(Classes theClass) {
     	String labelHtml = "<html><div style=\"text-align: center; margin: 0 35px; width:100%; display: inline;\">"
     						+ theClass.getLabel() + "</div><hr width=\"100%\">";
     	labelHtml += getClassesFeildsHTML(theClass);
 		JLabel label = new JLabel(labelHtml + "<p></html>");
 		
 		label.setVerticalAlignment(JLabel.TOP);
 		label.setOpaque(true);
 		label.setBackground(Color.white);
 		label.setForeground(Color.black);
 		if (theClass == mouseSelectedClass)
 			label.setBorder(BorderFactory.createLineBorder(Color.black, 3));
 		else 
 			label.setBorder(BorderFactory.createLineBorder(Color.black));
 		int xPosition = theClass.getPosition().x-Math.round(label.getPreferredSize().width/2);
 		int yPosition = theClass.getPosition().y-Math.round(label.getPreferredSize().height/2);
 		//Width and height are divided by 2, rounded, then multiplied by 2, to ensure the number is even.
 		int width = Math.round(label.getPreferredSize().width/2)*2;
 		int height = Math.round((label.getPreferredSize().height+5)/2)*2;
 		theClass.setDimension(new Dimension(width, height));
 		label.setBounds(new Rectangle(new Point(xPosition, yPosition), theClass.getDimension()));
 		return label;
     }
 }
