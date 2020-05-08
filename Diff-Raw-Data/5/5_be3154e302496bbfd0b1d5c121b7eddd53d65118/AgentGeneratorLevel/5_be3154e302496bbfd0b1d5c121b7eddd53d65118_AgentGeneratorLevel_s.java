 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package app.creator;
 
 import environment.geography.AgentLine;
 import environment.geography.Position;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  *
  * @author vaisagh
  */
 class AgentGeneratorLevel extends AbstractLevel implements MouseListener, MouseMotionListener {
 
     private DrawingPanel interactionArea;
     private ArrayList<Position> points;
     private ArrayList<AgentLine> agentLines;
     private Position point;
     private Position prevPoint;
     private Point currentPoint;
     private static final double DEFAULT_MAX_SPEED = 2.6;
     private static final double DEFAULT_MIN_SPEED = 0.0;
     private static final double DEFAULT_SDEV_SPEED = 0.0;
     private static final double DEFAULT_MEAN_SPEED = 1.3 ;
 
     public AgentGeneratorLevel(ModelDetails model, JFrame frame, JLabel statusBar, JPanel buttonArea, DrawingPanel interactionArea) {
         super(model, frame, statusBar, buttonArea);
         this.interactionArea = interactionArea;
 
     }
 
     @Override
     public void setUpLevel() {
         points = new ArrayList<Position>();
         prevPoint = new Position();
         point = new Position();
         point.setX(-1.0);
         point.setY(-1.0);
         prevPoint.setX(-1.0);
         prevPoint.setY(-1.0);
         currentPoint = new Point(0, 0);
 
         if (model.getAgentLines().isEmpty()) {
             agentLines = new ArrayList<AgentLine>();
         } else {
             agentLines = (ArrayList<AgentLine>) model.getAgentLines();
         }
 
 
         previousButton.setEnabled(true);
         clearButton.setEnabled(true);
         nextButton.setEnabled(true);
 
         frame.repaint();
 
 
         frame.add(interactionArea, BorderLayout.CENTER);
         interactionArea.setBackground(Color.white);
         interactionArea.setCurrentLevel(this);
         interactionArea.setEnabled(true);
         interactionArea.repaint();
         interactionArea.addMouseListener(this);
         interactionArea.addMouseMotionListener(this);
     }
 
     @Override
     public void clearUp() {
         model.setAgentLines(agentLines);
         interactionArea.removeMouseListener(this);
         interactionArea.removeMouseMotionListener(this);
 
         interactionArea.setEnabled(false);
         frame.remove(interactionArea);
     }
 
     @Override
     public void draw(Graphics g) {
         super.drawGridLines(g);
 
         super.drawCurrentPoint(g, currentPoint);
 
         if (!points.isEmpty()) {
             super.drawPoints(g, points);
         }
 
         if (!model.getObstacles().isEmpty()) {
             super.drawObstacles(g, model.getObstacles());
         }
 
         if (!agentLines.isEmpty()) {
             super.drawAgentLines(g, agentLines);
         }
     }
 
     @Override
     public void mouseClicked(MouseEvent me) {
     }
 
     @Override
     public void mousePressed(MouseEvent me) {
     }
 
     @Override
     public void mouseReleased(MouseEvent me) {
         boolean validityCheck = super.mouseReleaseDefaultActions(me, currentPoint, point, prevPoint);
 
         if (!validityCheck) {
             return;
         }
 
 
         if (prevPoint.getX() >= 0) {
             if (Math.abs(point.getX() - prevPoint.getX()) < 0.3) {
                 point.setX(prevPoint.getX());
             } else if (Math.abs(point.getY() - prevPoint.getY()) < 0.3) {
                 point.setY(prevPoint.getY());
             } else {
                 point.setX(prevPoint.getX());
                 point.setY(prevPoint.getY());
                 return;
             }
         }
 
         if (points.size() < 1) {
             statusBar.setText("Agent line started at " + point.getX() + "," + point.getY() + ". Please select the end point.");
             Position tempStorage = new Position();
             tempStorage.setX(point.getX());
             tempStorage.setY(point.getY());
             points.add(tempStorage);
 
 
 
         } else if (points.size() == 1) {
             statusBar.setText("Agent line end set at " + point.getX() + "," + point.getY());
             AgentLine tempAgentLine = new AgentLine();
 
 
             Position tempStorage = new Position();
             tempStorage.setX(point.getX() > points.get(0).getX() ? point.getX() : points.get(0).getX());
             tempStorage.setY(point.getY() > points.get(0).getY() ? point.getY() : points.get(0).getY());
             tempAgentLine.setEndPoint(tempStorage);
 
 
             tempStorage = new Position();
             tempStorage.setX(point.getX() < points.get(0).getX() ? point.getX() : points.get(0).getX());
             tempStorage.setY(point.getY() < points.get(0).getY() ? point.getY() : points.get(0).getY());
             tempAgentLine.setStartPoint(tempStorage);
 
 
 
 
             int size=0;
             double minSpeed= DEFAULT_MIN_SPEED, maxSpeed = DEFAULT_MAX_SPEED;
             double meanSpeed = DEFAULT_MEAN_SPEED, sDev = DEFAULT_SDEV_SPEED;
             String tempString;
             do {
                 tempString = (String) JOptionPane.showInputDialog(
                         null,
                         "Minimum Speed",
                         "Input",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                         String.valueOf(DEFAULT_MIN_SPEED));
                try {
                     minSpeed = Double.parseDouble(tempString);
                 } catch (NumberFormatException numException) {
                     continue;
                 }
             } while (minSpeed<0||minSpeed>2.6);
             
         
             do {
                 tempString = (String) JOptionPane.showInputDialog(
                         null,
                         "Maximum Speed?",
                         "Input",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                         String.valueOf(DEFAULT_MAX_SPEED));
                try {
                     maxSpeed = Double.parseDouble(tempString);
                 } catch (NumberFormatException numException) {
                     continue;
                 }
             } while (maxSpeed<minSpeed||maxSpeed>2.6);
             meanSpeed = (minSpeed + maxSpeed) /2.0;
             do {
                 tempString = (String) JOptionPane.showInputDialog(
                         null,
                         "Average Speed?",
                         "Input",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                         Double.toString(meanSpeed));
                try {
                     meanSpeed = Double.parseDouble(tempString);
                 } catch (NumberFormatException numException) {
                     continue;
                 }
             } while (meanSpeed<minSpeed||meanSpeed>maxSpeed);
             
             do {
                 tempString = (String) JOptionPane.showInputDialog(
                         null,
                         "Standard Deviation:",
                         "Input",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                         "0.0");
                try {
                     sDev = Double.parseDouble(tempString);
                 } catch (NumberFormatException numException) {
                     continue;
                 }
             } while (false);
 
             String frequency;
             do {
                 frequency = (String) JOptionPane.showInputDialog(
                         null,
                        "How many time steps between agents being generate?",
                         "Input",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                        "1");
                 try {
                     Integer.parseInt(frequency);
                 } catch (NumberFormatException numException) {
                     continue;
                 }
             } while (false);
 
 
             String number;
             do {
                 number = (String) JOptionPane.showInputDialog(
                         null,
                         "How many agents produced per generation?",
                         "Input",
                         JOptionPane.PLAIN_MESSAGE,
                         null,
                         null,
                         "1");
                 try {
                     Integer.parseInt(number);
                 } catch (NumberFormatException numException) {
                     continue;
                 }
             } while (false);
 
 
 
             tempAgentLine.setMinSpeed(minSpeed);
             tempAgentLine.setMaxSpeed(maxSpeed);
             tempAgentLine.setMeanSpeed(meanSpeed);
             tempAgentLine.setSDevSpeed(sDev);
             tempAgentLine.setFrequency(Integer.parseInt(frequency));
             tempAgentLine.setNumber(Integer.parseInt(number));
             agentLines.add(tempAgentLine);
 
             prevPoint.setX(-1.0);
             prevPoint.setY(-1.0);
             point.setX(-1.0);
             point.setY(-1.0);
             points.clear();
 
 
         }
 
         interactionArea.repaint();
     }
 
     @Override
     public void mouseEntered(MouseEvent me) {
     }
 
     @Override
     public void mouseExited(MouseEvent me) {
     }
 
     @Override
     public void mouseDragged(MouseEvent me) {
     }
 
     @Override
     public void mouseMoved(MouseEvent me) {
         super.calculateCurrentPoint(me, currentPoint,true);
         interactionArea.repaint();
     }
 
     @Override
     public void clearAllPoints() {
         points.clear();
         agentLines.clear();
         interactionArea.repaint();
     }
 
     @Override
     public String getName() {
         return "Factory Line Level";
     }
 }
