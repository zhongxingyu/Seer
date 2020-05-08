 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2008, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * $Revision$
  * $Date$
  * $State$
  */
 package org.jdesktop.wonderland.modules.simplewhiteboard.client;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.util.ArrayList;
 import java.util.logging.Logger;
 import org.jdesktop.wonderland.modules.appbase.client.DrawingSurface;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardActionType;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardActionType.ActionType;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardCommand.Command;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardPenColors;
 import org.jdesktop.wonderland.modules.simplewhiteboard.common.WhiteboardTool.Tool;
 
 /**
  * @author paulby,deronj
  */
 
 @ExperimentalAPI
 public class WhiteboardDrawingSurface extends DrawingSurface {
 
     private static final Logger logger = Logger.getLogger(WhiteboardDrawingSurface.class.getName());
     
     private Color selectedColor = Color.BLACK;
     private Button selectedButton;
     private Tool selectedTool = Tool.STROKE;
     private Command command;
     private ActionType actionType = WhiteboardActionType.TOOL;
     
     private final int buttonWidth = 40;
     private final int button_x_space = 3;
     private final int button_y_space = 3;
     
     private ArrayList<Button> buttons = new ArrayList();
     
     private Rectangle buttonArea = null;
     
     public WhiteboardDrawingSurface (int width, int height) {
 	this();
 	setSize(width, height);
     }
 
     public WhiteboardDrawingSurface() {
         super();
         int x = button_x_space;
         int y = buttonWidth;
         
         for (Color c : WhiteboardPenColors.PenColors){
             logger.info("adding color: " + c);
             buttons.add(new Button(c, x, y, buttonWidth, buttonWidth));
             y += buttonWidth + button_y_space;
         }
         
         y += buttonWidth + button_y_space;
         
         for (Tool t : Tool.values()) {
             logger.info("adding tool: " + t);
             buttons.add(new Button(t, Color.LIGHT_GRAY, x, y, buttonWidth, buttonWidth));
             y += buttonWidth + button_y_space;
         }
         
         y += buttonWidth+button_y_space;
         
         for (Command c : Command.values()) {
             logger.info("adding command: " + c);
             buttons.add(new Button(c, Color.DARK_GRAY, x, y, buttonWidth, buttonWidth));
             y += buttonWidth + button_y_space;
         }
     }
     
     @Override
     public void setSize(int width, int height) {
         buttonArea = new Rectangle(0, 0, buttonWidth+button_y_space*2, height);
         super.setSize(width, height);
     }
     
     public void penMove(Point loc) {
         renderCursor();
         penX = loc.x;
         penY = loc.y;
         renderCursor();
     }
     
     public void penDrag(Point loc) {
         Graphics2D g = getGraphics();
         renderCursor();
         setClip(g, true);
         g.setColor(selectedColor);
         g.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
         g.drawLine(penX, penY, loc.x, loc.y);
         penX = loc.x;
         penY = loc.y;
         setClip(g, false);
         renderCursor();
     }
     
     public void penSelect(Point loc) {
         if (buttonArea.contains(loc)) {
             for(Button b : buttons) {
                 if (b.contains(loc)) {
                     selectedButton = b;
                     if (selectedButton.isColorButton()) {
                         setPenColor(selectedButton.getColor());
                     } else if (selectedButton.isToolButton()) {
                         setTool(selectedButton.getTool());
                     } else if (selectedButton.isCommandButton()) {
                         setCommand(selectedButton.getCommand());
                     } else {
                         
                     }
                     break;
                 }
             }
         }
     }
     
     public ActionType getActionType() {
         return actionType;
     }
     
     public void setCommand(Command command) {
         actionType = WhiteboardActionType.COMMAND;
         this.command = command;
         switch (command) {
             case ERASE:
                 erase();
                 break;
             default:
                 break;
         }
     }
     
     public Command getCommand() {
         return command;
     }
     
     public void setTool(Tool tool) {
         actionType = WhiteboardActionType.TOOL;
         selectedTool = tool;
     }
     
     public Tool getTool() {
         return selectedTool;
     }
     
     public void setPenColor(Color color) {
         actionType = WhiteboardActionType.COLOR;
         selectedColor = color;
     }
     
     public Color getPenColor() {
         return selectedColor;
     }
     
     @Override
     protected void setClip(Graphics2D g, boolean clipEnabled) {
         if (clipEnabled)
             g.setClip(buttonArea.width,0,getWidth()-buttonArea.width, getHeight());
         else
             g.setClip(0,0,getWidth(), getHeight());
     }
     
     public void penMove(float x, float y) {
         penMove(computeImagePoint(x,y));
     }
     
     public void penSelect(float x, float y) {
         penSelect(computeImagePoint(x,y));
     }
     
     public void erase() {
         Graphics2D g = getGraphics();
         setClip(g, true);
	/* TODO: bug: this should fill the image with white, but it doesn't
         g.setBackground(Color.WHITE);
         g.clearRect(0, 0, getWidth(), getHeight());
	*/
	/* Bug workaround */
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
	/**/

         setClip(g, false);
     }
     
     public void penDrag(float x, float y) {
         penDrag(computeImagePoint(x,y));
     }
     
     protected void initSurface (Graphics2D g) {
	erase();

 	// Note: this works only when we always draw
 	// the buttons on the left side of the window.
         for (Button b : buttons) {
             b.paint(g);
         }
     }
     
     class Button {
         private ActionType type;
         private Color color;
         private Tool tool;
         private Command command;
         private int x;
         private int y;
         private int width;
         private int height;
         
         Shape rect = null;
         
         public Button(int x, int y, int width, int height) {
             this.x = x;
             this.y = y;
             this.width = width;
             this.height = height;
             rect = new Rectangle(x,y,width,height);
         }
         
         public Button(Color color, int x, int y, int width, int height) {
             this(x, y, width, height);
             type = ActionType.COLOR;
             this.color = color;
         }
         
         public Button(Tool tool, Color color, int x, int y, int width, int height) {
             this(color, x, y, width, height);
             type = ActionType.TOOL;
             this.tool = tool;
         }
         
         public Button(Command command, Color color, int x, int y, int width, int height) {
             this(color, x, y, width, height);
             type = ActionType.COMMAND;
             this.command = command;
         }
         
         public void paint(Graphics2D g) {
             g.setColor(color);
             g.fill(rect);
             if (command != null) {
                 switch (command) {
                     case ERASE:
                         g.setColor(Color.RED);
                         g.drawLine(x+1, y+1, x+width-2, y+height-2);
                         g.drawLine(x+1, y+height-2, x+width-2, y+1);
                         break;
                 }
             } else if (tool != null) {
                 switch (tool) {
                     case STROKE:
                         g.setColor(Color.BLUE);
                         int sixth = width/6;
                         g.drawLine(x+sixth, y+sixth, x+3*sixth, y+5*sixth);
                         g.drawLine(x+3*sixth, y+5*sixth, x+4*sixth, y+2*sixth);
                         g.drawLine(x+4*sixth, y+2*sixth, x+5*sixth, y+5*sixth);
                         break;
                 }
             }
         }
         
         /**
          *  Returns true if this button contains this point
          */
         public boolean contains(Point p) {
             return rect.contains(p);
         }
         
         public ActionType getType() {
             return type;
         }
         
         public Tool getTool() {
             return tool;
         }
         
         public Color getColor() {
             return color;
         }
         
         public Command getCommand() {
             return command;
         }
         
         public boolean isToolButton() {
             return (type == WhiteboardActionType.TOOL);
         }
         
         public boolean isColorButton() {
             return (type == WhiteboardActionType.COLOR);
         }
         
         public boolean isCommandButton() {
             return (type == WhiteboardActionType.COMMAND);
         }
     }
 }
