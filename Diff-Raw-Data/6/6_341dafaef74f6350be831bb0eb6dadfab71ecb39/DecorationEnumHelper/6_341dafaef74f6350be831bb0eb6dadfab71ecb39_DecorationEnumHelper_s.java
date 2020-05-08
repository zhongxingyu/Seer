 package com.lanl.application.treeDecorator.enumeration;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.font.TextAttribute;
 import java.text.AttributedString;
 import java.util.Set;
 
 import org.forester.phylogeny.PhylogenyNode;
 
 import com.lanl.application.TPTD.applet.SubTreePanel;
 import com.lanl.application.treeDecorator.applet.ui.drawDecoration.DecoratorColorSet;
 import com.lanl.application.treeDecorator.applet.ui.drawDecoration.DecoratorShapes;
 import com.lanl.application.treeDecorator.dataStructures.DecoratorTable;
 
 public class DecorationEnumHelper {
 	public static DecorationStyles[] allColors = {
 		DecorationStyles.EGGPLANT,
 		DecorationStyles.DARK_GRAY,
 		DecorationStyles.TANGERINE,
 		DecorationStyles.BLUE,
 		DecorationStyles.LIME,
 		DecorationStyles.STEEL_BLUE,
 		DecorationStyles.RED,
 		DecorationStyles.BROWN,
 		DecorationStyles.FIRE_BRICK,
 		DecorationStyles.AQUA,
 		DecorationStyles.DARK_KHAKI,
 		DecorationStyles.DARK_MAGENTA,
 		DecorationStyles.BLACK,
 		DecorationStyles.NAVY,
 		DecorationStyles.PINK,
 		DecorationStyles.FOREST_GREEN
 		
 	};
 	
 	public static DecorationStyles[] allShapes = {
 		DecorationStyles.HOLLOW_TRIANGE,
 		DecorationStyles.FILLED_TRIANGE,
 		DecorationStyles.HOLLOW_CIRCLE,
 		DecorationStyles.FILLED_CIRCLE,
 		DecorationStyles.HOLLOW_INVERTED_TRIANGE,
 		DecorationStyles.FILLED_INVERTED_TRIANGE,
 		DecorationStyles.HOLLOW_DIAMOND,
 		DecorationStyles.FILLED_DIAMOND,
 		DecorationStyles.HOLLOW_SQUARE,
 		DecorationStyles.FILLED_SQUARE
 	};
 	
 	public static DecorationStyles[] allCases = {
 		DecorationStyles.AS_IS,
 		DecorationStyles.LOWER,
 		DecorationStyles.UPPER
 	};
 	
 	public static DecorationStyles[] allFonts = {
 		DecorationStyles.ARIAL,
 		DecorationStyles.CHALKBOARD,
 		DecorationStyles.VERANDA ,
 		DecorationStyles.HELVETICA,
 		DecorationStyles.TIMES_NEW_ROMAN ,
 		DecorationStyles.COURIER,
 		DecorationStyles.COMIC_SANS_MS
 	};
 	
 	public static DecorationStyles[] allStyles = {
 		DecorationStyles.REGULAR,
 		DecorationStyles.BOLD,
 		DecorationStyles.ITALIC,
 		DecorationStyles.UNDERLINE
 	};
 	
 	public static DecorationStyles[] allSizes = {
 		DecorationStyles._8,
 		DecorationStyles._9,
 		DecorationStyles._10,
 		DecorationStyles._12,
 		DecorationStyles._14
 	};
 	
 /*	public static DecoratorUIConstants syleNames[] = {
 		DecoratorUIConstants.NODE_COLOR,
 		DecoratorUIConstants.SHAPES,
 		DecoratorUIConstants.STRAIN_COLOR,
 		DecoratorUIConstants.CASE,
 		DecoratorUIConstants.FONT,
 		DecoratorUIConstants.STYLE,
 		DecoratorUIConstants.SIZE,
 	};*/
 	
 	
 	public static void drawShapesWithColor(DecorationStyles shape, Graphics g, Point location, int height,
 			int width, DecorationStyles color){
 		int adjustment = 2;
 		if(shape == DecorationStyles.HOLLOW_TRIANGE){
 			DecoratorShapes.drawTriangleHollow(g, location, height+adjustment, width+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.FILLED_TRIANGE){
 			DecoratorShapes.drawTriangleFilled(g, location, height+adjustment, width+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.HOLLOW_CIRCLE){
 			DecoratorShapes.drawCircleHollow(g, location, height+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.FILLED_CIRCLE){
 			DecoratorShapes.drawCircleFilled(g, location, height+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.HOLLOW_INVERTED_TRIANGE){
 			DecoratorShapes.drawInvertedTriangleHollow(g, location, height+adjustment, width+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.FILLED_INVERTED_TRIANGE){
 			DecoratorShapes.drawInvertedTriangleFilled(g, location, height+adjustment, width+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.HOLLOW_DIAMOND){
 			DecoratorShapes.drawDiamondHollow(g, location, height+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.FILLED_DIAMOND){
 			DecoratorShapes.drawDiamondFilled(g, location, height+adjustment, getColor(color));
 		}
 		else if(shape == DecorationStyles.HOLLOW_SQUARE){
 			DecoratorShapes.drawSquareHollow(g, location, height, getColor(color));
 		}
 		else if(shape == DecorationStyles.FILLED_SQUARE){
 			DecoratorShapes.drawSquareFilled(g, location, height, getColor(color));
 		}
 		else throw new IllegalArgumentException("The node shape selected is not permitted in Tree Decorator");
 	}
 	
 	public static DecoratorUIConstants getNodeOrStrain(DecoratorUIConstants styleName){
 		if(styleName == DecoratorUIConstants.NODE_COLOR || styleName == DecoratorUIConstants.SHAPES){
 			return DecoratorUIConstants.NODE;
 		}
 		else if(styleName == DecoratorUIConstants.STRAIN_COLOR || styleName == DecoratorUIConstants.CASE ||
 				styleName == DecoratorUIConstants.FONT || styleName == DecoratorUIConstants.STYLE ||
 				styleName == DecoratorUIConstants.SIZE){
 			return DecoratorUIConstants.STRAIN;
 		}
 		else throw new IllegalArgumentException("The style name is unknown to the Tree Decorator");
 	}
 
 	public static boolean isStyleValueDefault(DecoratorUIConstants styleName, DecorationStyles styleValue){
 		if(styleName == DecoratorUIConstants.NODE_COLOR){
 			if(styleValue == DecorationStyles.BLACK){
 				return true;
 			}
 			else {return false;}
 		}
 		else if(styleName == DecoratorUIConstants.SHAPES){
 			if(styleValue == DecorationStyles.FILLED_SQUARE){
 				return true;
 			}
 			else {return false;}
 		}
 		else if(styleName == DecoratorUIConstants.STRAIN_COLOR){
 			if(styleValue == DecorationStyles.BLACK){
 				return true;
 			}
 			else {return false;}
 		}
 		else if(styleName == DecoratorUIConstants.CASE){
 			if(styleValue == DecorationStyles.AS_IS){
 				return true;
 			}
 			else {return false;}
 		}
 		else if(styleName == DecoratorUIConstants.FONT){
 			if(styleValue == DecorationStyles.ARIAL){
 				return true;
 			}
 			else {return false;}
 		}
 		else if(styleName == DecoratorUIConstants.STYLE){
 			if(styleValue == DecorationStyles.REGULAR){
 				return true;
 			}
 			else {return false;}
 		}
 		else if(styleName == DecoratorUIConstants.SIZE){
 			if(styleValue == DecorationStyles._12){
 				return true;
 			}
 			else {return false;}
 		}
 		else throw new IllegalArgumentException("The style name is unknown to the Tree Decorator");
 	}
 	
 	public static DecorationStyles[] getAllStyleValues(DecoratorUIConstants styleName){
 		if(styleName == DecoratorUIConstants.NODE_COLOR){
 			return allColors;
 		}
 		else if(styleName == DecoratorUIConstants.SHAPES){
 			return allShapes;
 		}
 		else if(styleName == DecoratorUIConstants.STRAIN_COLOR){
 			return allColors;
 		}
 		else if(styleName == DecoratorUIConstants.CASE){
 			return allCases;
 		}
 		else if(styleName == DecoratorUIConstants.FONT){
 			return allFonts;
 		}
 		else if(styleName == DecoratorUIConstants.STYLE){
 			return allStyles;
 		}
 		else if(styleName == DecoratorUIConstants.SIZE){
 			return allSizes;
 		}
 		else throw new IllegalArgumentException("The style name is unknown to the Tree Decorator");
 	}
 	
 	public static Font getFont(DecorationStyles font,DecorationStyles style,DecorationStyles size){
 			boolean fontFine = false;
 	    	boolean sizeFine = false;
 	    	boolean styleFine = false;
 	    	for(int i=0;i<allFonts.length;i++){
 	    		if(font == allFonts[i]){
 	    			fontFine = true;
 	    		}
 	    	}
 	    	for(int i=0;i<allStyles.length;i++){
 	    		if(style == allStyles[i]){
 	    			styleFine = true;
 	    		}
 	    	}
 	    	for(int i=0;i<allSizes.length;i++){
 	    		if(size == allSizes[i]){
 	    			sizeFine = true;
 	    		}
 	    	}
 	    	if(fontFine && sizeFine && styleFine){
 	    		return (new Font(font.getName(),Integer.parseInt(style.getName())
 	    				,Integer.parseInt(size.getName())));
 	    	} else
 				throw new IllegalArgumentException("You have selected a Font,style or size " +
 						"that is not accepted by the tree decorator"+fontFine + styleFine +sizeFine);
 		}
 	
 	
 	
 	public static String getStringWithCase(String s,DecorationStyles _case){
 		if(_case == DecorationStyles.UPPER){
 			return s.toUpperCase();
 		}
 		else if(_case == DecorationStyles.LOWER){
 			return s.toLowerCase();
 		}
 		else if(_case == DecorationStyles.AS_IS){
 			return s;
 		}
 		else throw new IllegalArgumentException("The case selected is not permitted in Tree Decorator");
 	}
 	
 	public static Color getColor(DecorationStyles color){  //16
 		if(color == DecorationStyles.EGGPLANT){
 			return DecoratorColorSet.getEggplant();
 		}
 		else if(color == DecorationStyles.DARK_GRAY){
 			return DecoratorColorSet.getDarkGray();
 		}
 		else if(color == DecorationStyles.BROWN){
 			return DecoratorColorSet.getBrown();
 		}
 		else if(color == DecorationStyles.BLACK){
 			return DecoratorColorSet.getBlack();
 		}
 		else if(color == DecorationStyles.TANGERINE){
 			return DecoratorColorSet.getTangerine();
 		}
 		else if(color == DecorationStyles.RED){
 			return DecoratorColorSet.getRed();
 		}
 		else if(color == DecorationStyles.PINK){
 			return DecoratorColorSet.getPink();
 		}
 		else if(color == DecorationStyles.BLUE){
 			return DecoratorColorSet.getBlue();
 		}
 		else if(color == DecorationStyles.DARK_KHAKI){
 			return DecoratorColorSet.getDarkKhaki();
 		}
 		else if(color == DecorationStyles.FIRE_BRICK){
 			return DecoratorColorSet.getFireBrick();
 		}
 		else if(color == DecorationStyles.DARK_MAGENTA){
 			return DecoratorColorSet.getDarkMagenta();
 		}
 		else if(color == DecorationStyles.NAVY){
 			return DecoratorColorSet.getNavy();
 		}
 		else if(color == DecorationStyles.STEEL_BLUE){
 			return DecoratorColorSet.getSteelBlue();
 		}
 		else if(color == DecorationStyles.FOREST_GREEN){
 			return DecoratorColorSet.getForestGreen();
 		}
 		else if(color == DecorationStyles.AQUA){
 			return DecoratorColorSet.getAqua();
 		}
 		else if(color == DecorationStyles.LIME){
 			return DecoratorColorSet.getLime();
 		}
 		else throw new IllegalArgumentException("The color selected is not permitted in Tree Decorator");
 		
 		
 	}
 	
 	public static String doUnderline(String s, DecorationStyles style){
 		if(style.getName().equals("10")){
 			return new String("<html><u>"+s+"</u></html>");
 		}
 		else{
 			return s;
 		}
 		
     	
     }
     
 	public static void drawStrainWithColorFontCaseForPdf( Graphics g, String s, Font f,Point location, 
 			DecorationStyles style,DecorationStyles color){
 		Color strainColor = getColor(color);
 		g.setFont(f);
 		g.setColor(strainColor);
 		if(style.getName().equals("10")){
			g.drawLine(location.x, location.y,location.x+s.length()*6 ,location.y );
 			g.drawString(s,location.x, location.y);
 		}
 		else{
 			g.drawString(s,location.x, location.y);
 		}
 		
 	}
 	
     public static void drawStrainWithColorFontCase( Graphics g, String s, Font f,Point location, 
     				DecorationStyles style,DecorationStyles color){
     	Color strainColor = getColor(color);
     	if(style.getName().equals("10")){
 	    	Graphics2D g2d = (Graphics2D) g;
 	    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 	    	        RenderingHints.VALUE_ANTIALIAS_ON);
 	    	AttributedString as = new AttributedString(s);
 	    	as.addAttribute(TextAttribute.FOREGROUND,strainColor);
 	    	as.addAttribute(TextAttribute.FONT,f);
 	    	as.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0,s.length());
 	    	g2d.drawString(as.getIterator(), location.x, location.y);
     	}
     	else {
     		Graphics2D g2d = (Graphics2D) g;
 	    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 	    	        RenderingHints.VALUE_ANTIALIAS_ON);
 	    	AttributedString as = new AttributedString(s);
 	    	as.addAttribute(TextAttribute.FOREGROUND,strainColor);
 	    	as.addAttribute(TextAttribute.FONT,f);
 	    	g2d.drawString(as.getIterator(), location.x, location.y);
     	}
     }
     
     //for communication get the DecorationStyles and DecoratorUIConstants object from string
     
     public static DecorationStyles getDecorationStylesObject(String s){
     	if(s.equals(DecorationStyles.EGGPLANT.getName())){
     		return DecorationStyles.EGGPLANT;
     	}
     	else if(s.equals(DecorationStyles.DARK_GRAY.getName())){
     		return DecorationStyles.DARK_GRAY;
     	}
     	else if(s.equals(DecorationStyles.BROWN.getName())){
     		return DecorationStyles.BROWN;
     	}
     	else if(s.equals(DecorationStyles.BLACK.getName())){
     		return DecorationStyles.BLACK;
     	}
     	else if(s.equals(DecorationStyles.TANGERINE.getName())){
     		return DecorationStyles.TANGERINE;
     	}
     	else if(s.equals(DecorationStyles.RED.getName())){
     		return DecorationStyles.RED;
     	}
     	else if(s.equals(DecorationStyles.PINK.getName())){
     		return DecorationStyles.PINK;
     	}
     	else if(s.equals(DecorationStyles.BLUE.getName())){
     		return DecorationStyles.BLUE;
     	}
     	else if(s.equals(DecorationStyles.DARK_KHAKI.getName())){
     		return DecorationStyles.DARK_KHAKI;
     	}
     	else if(s.equals(DecorationStyles.FIRE_BRICK.getName())){
     		return DecorationStyles.FIRE_BRICK;
     	}
     	else if(s.equals(DecorationStyles.DARK_MAGENTA.getName())){
     		return DecorationStyles.DARK_MAGENTA;
     	}
     	else if(s.equals(DecorationStyles.NAVY.getName())){
     		return DecorationStyles.NAVY;
     	}
     	else if(s.equals(DecorationStyles.STEEL_BLUE.getName())){
     		return DecorationStyles.STEEL_BLUE;
     	}
     	else if(s.equals(DecorationStyles.FOREST_GREEN.getName())){
     		return DecorationStyles.FOREST_GREEN;
     	}
     	else if(s.equals(DecorationStyles.AQUA.getName())){
     		return DecorationStyles.AQUA;
     	}
     	else if(s.equals(DecorationStyles.LIME.getName())){
     		return DecorationStyles.LIME;
     	}
     	else if(s.equals(DecorationStyles.HOLLOW_TRIANGE.getName())){
     		return DecorationStyles.HOLLOW_TRIANGE;
     	}
     	else if(s.equals(DecorationStyles.FILLED_TRIANGE.getName())){
     		return DecorationStyles.FILLED_TRIANGE;
     	}
     	else if(s.equals(DecorationStyles.HOLLOW_CIRCLE.getName())){
     		return DecorationStyles.HOLLOW_CIRCLE;
     	}
     	else if(s.equals(DecorationStyles.FILLED_CIRCLE.getName())){
     		return DecorationStyles.FILLED_CIRCLE;
     	}
     	else if(s.equals(DecorationStyles.HOLLOW_INVERTED_TRIANGE.getName())){
     		return DecorationStyles.HOLLOW_INVERTED_TRIANGE;
     	}
     	else if(s.equals(DecorationStyles.FILLED_INVERTED_TRIANGE.getName())){
     		return DecorationStyles.FILLED_INVERTED_TRIANGE;
     	}
     	else if(s.equals(DecorationStyles.HOLLOW_DIAMOND.getName())){
     		return DecorationStyles.HOLLOW_DIAMOND;
     	}
     	else if(s.equals(DecorationStyles.FILLED_DIAMOND.getName())){
     		return DecorationStyles.FILLED_DIAMOND;
     	}
     	else if(s.equals(DecorationStyles.HOLLOW_SQUARE.getName())){
     		return DecorationStyles.HOLLOW_SQUARE;
     	}
     	else if(s.equals(DecorationStyles.FILLED_SQUARE.getName())){
     		return DecorationStyles.FILLED_SQUARE;
     	}
     	else if(s.equals(DecorationStyles.AS_IS.getName())){
     		return DecorationStyles.AS_IS;
     	}
 		else if(s.equals(DecorationStyles.LOWER.getName())){
     		return DecorationStyles.LOWER;
     	}
 		else if(s.equals(DecorationStyles.UPPER.getName())){
     		return DecorationStyles.UPPER;
     	}
 		else if(s.equals(DecorationStyles.ARIAL.getName())){
     		return DecorationStyles.ARIAL;
     	}
 		else if(s.equals(DecorationStyles.CHALKBOARD.getName())){
     		return DecorationStyles.CHALKBOARD;
     	}
 		else if(s.equals(DecorationStyles.VERANDA.getName())){
     		return DecorationStyles.VERANDA;
     	}
 		else if(s.equals(DecorationStyles.HELVETICA.getName())){
     		return DecorationStyles.HELVETICA;
     	}
 		else if(s.equals(DecorationStyles.TIMES_NEW_ROMAN.getName())){
     		return DecorationStyles.TIMES_NEW_ROMAN;
     	}
 		else if(s.equals(DecorationStyles.COURIER.getName())){
     		return DecorationStyles.COURIER;
     	}
 		else if(s.equals(DecorationStyles.COMIC_SANS_MS.getName())){
     		return DecorationStyles.COMIC_SANS_MS;
     	}
     	else if(s.equals(DecorationStyles.REGULAR.getName())){
     		return DecorationStyles.REGULAR;
     	}
     	else if(s.equals(DecorationStyles.BOLD.getName())){
     		return DecorationStyles.BOLD;
     	}
     	else if(s.equals(DecorationStyles.ITALIC.getName())){
     		return DecorationStyles.ITALIC;
     	}
     	else if(s.equals(DecorationStyles.UNDERLINE.getName())){
     		return DecorationStyles.UNDERLINE;
     	}
     	else if(s.equals(DecorationStyles._8.getName())){
     		return DecorationStyles._8;
     	}
     	else if(s.equals(DecorationStyles._9.getName())){
     		return DecorationStyles._9;
     	}
     	else if(s.equals(DecorationStyles._10.getName())){
     		return DecorationStyles._10;
     	}
     	else if(s.equals(DecorationStyles._12.getName())){
     		return DecorationStyles._12;
     	}
     	else if(s.equals(DecorationStyles._14.getName())){
     		return DecorationStyles._14;
     	}
     	else throw new IllegalArgumentException("The style selected is not permitted in Tree Decorator");
     }
     
     public static DecoratorUIConstants getDecoratorUIConstantsObject(String s){
     	if(s.equals(DecoratorUIConstants.NODE_COLOR.getName())){
     		return DecoratorUIConstants.NODE_COLOR;
     	}
     	else if(s.equals(DecoratorUIConstants.SHAPES.getName())){
     		return DecoratorUIConstants.SHAPES;
     	}
     	else if(s.equals(DecoratorUIConstants.STRAIN_COLOR.getName())){
     		return DecoratorUIConstants.STRAIN_COLOR;
     	}
     	else if(s.equals(DecoratorUIConstants.CASE.getName())){
     		return DecoratorUIConstants.CASE;
     	}
     	else if(s.equals(DecoratorUIConstants.FONT.getName())){
     		return DecoratorUIConstants.FONT;
     	}
     	else if(s.equals(DecoratorUIConstants.STYLE.getName())){
     		return DecoratorUIConstants.STYLE;
     	}
     	else if(s.equals(DecoratorUIConstants.SIZE.getName())){
     		return DecoratorUIConstants.SIZE;
     	}
     	else if(s.equals(DecoratorUIConstants.COUNTRY.getName())){
     		return DecoratorUIConstants.COUNTRY;
     	}
     	else if(s.equals(DecoratorUIConstants.YEAR.getName())){
     		return DecoratorUIConstants.YEAR;
     	}
     	else if(s.equals(DecoratorUIConstants.A_HA_SUBTYPE.getName())){
     		return DecoratorUIConstants.A_HA_SUBTYPE;
     	}
     	else if(s.equals(DecoratorUIConstants.A_NA_SYBTYPE.getName())){
     		return DecoratorUIConstants.A_NA_SYBTYPE;
     	}
     	else if(s.equals(DecoratorUIConstants.HOST_SPECIES.getName())){
     		return DecoratorUIConstants.HOST_SPECIES;
     	}
     	else return DecoratorUIConstants.NULL;
     	
     }
     
     public static String getCharValueForCharNameFromNode(DecoratorUIConstants charName,PhylogenyNode node){
 		if(charName == DecoratorUIConstants.COUNTRY){
 			return node.extraNodeInfo.getNodeCountry();
 		}
 		else if(charName == DecoratorUIConstants.YEAR){
 			return node.extraNodeInfo.getNodeYear();
 		}
 		else if(charName == DecoratorUIConstants.A_HA_SUBTYPE){
 			return node.extraNodeInfo.getNodeHA();
 		}
 		else if(charName == DecoratorUIConstants.A_NA_SYBTYPE){
 			return node.extraNodeInfo.getNodeNA();
 		}
 		else if(charName == DecoratorUIConstants.HOST_SPECIES){
 			return node.extraNodeInfo.getNodeHost();
 		}
 		else throw new IllegalArgumentException("The characteristic selected is not permitted in Tree Decorator");
 	}
     
     public static DecorationStyles getDefaultDecorationStyles(DecoratorUIConstants styleName){
     	if(styleName == DecoratorUIConstants.NODE_COLOR){
 			return DecorationStyles.BLACK;
 		}
 		else if(styleName == DecoratorUIConstants.SHAPES){
 			return DecorationStyles.FILLED_SQUARE;
 		}
 		else if(styleName == DecoratorUIConstants.STRAIN_COLOR){
 			return DecorationStyles.BLACK;
 		}	
 		else if(styleName == DecoratorUIConstants.CASE){
 			return DecorationStyles.AS_IS;
 		}	
 		else if(styleName == DecoratorUIConstants.FONT){
 			return DecorationStyles.ARIAL;
 		}
 		else if(styleName == DecoratorUIConstants.STYLE){
 				return DecorationStyles.REGULAR;
 		}
 		else if(styleName == DecoratorUIConstants.SIZE){
 				return DecorationStyles._12;
 		}
 		else throw new IllegalArgumentException("The style name is unknown to the Tree Decorator");
     }
 	
     public static void populateBranchColorNodes(){
     	
     	if(DecoratorTable.styleCharacteristicMapping.containsKey(DecoratorUIConstants.STRAIN_COLOR)){
     		DecoratorUIConstants charName = DecoratorTable.styleCharacteristicMapping.get((DecoratorUIConstants.STRAIN_COLOR));
     		Set<PhylogenyNode> leafNodes = SubTreePanel._full_phylogeny.getExternalNodes();
     		DecoratorTable.clearBranchColoring();
     		for(PhylogenyNode pn: leafNodes){
     			DecorationStyles styleValue = getDefaultDecorationStyles(DecoratorUIConstants.STRAIN_COLOR);
     			String charValue = getCharValueForCharNameFromNode(charName, pn);
     			if(charValue!=null){
     				if(!charValue.equals("null")){
     					styleValue = DecoratorTable.decoratorTable.get(charName).get(charValue).getStrainColor();
     				}
     			}
     			if(!isStyleValueDefault(DecoratorUIConstants.STRAIN_COLOR,styleValue)){
 	    			DecoratorTable.nodeIDStyleValuesForBranchColoring.put(pn.getNodeId(), styleValue);
 	    			PhylogenyNode parent = pn;                 
 	    			PhylogenyNode nextParent = null;
 	    			while( ( nextParent = parent.getParent()) != null){
 	    				if(DecoratorTable.nodeIDStyleValuesForBranchColoring.containsKey(nextParent.getChildNode1().getNodeId())&&
 	    					DecoratorTable.nodeIDStyleValuesForBranchColoring.containsKey(nextParent.getChildNode2().getNodeId())){
 	    					if(DecoratorTable.nodeIDStyleValuesForBranchColoring.get(nextParent.getChildNode1().getNodeId())==
 	    						DecoratorTable.nodeIDStyleValuesForBranchColoring.get(nextParent.getChildNode2().getNodeId())){
 	    						DecoratorTable.nodeIDStyleValuesForBranchColoring.put(nextParent.getNodeId(), styleValue);
 	    					}
 	    					else{
 	    						break;
 	    					}
 	    				}
 	    				else{
 	    					break;
 	    				}
 	    				parent=nextParent;
 	    			}
 	    		}
     			
     			
     		}
     	}
     	else{
     		DecoratorTable.clearBranchColoring();
     	}
     }	
 }
 
