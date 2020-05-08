 /**
  * @author Team O
  * @title Graphical Thesaurus
  * @date 3/12/12
  */
 
 package thesaurus.gui.canvas;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 import javafx.application.Application;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.paint.Color;
 import javafx.stage.Stage;
 import javafx.scene.input.MouseEvent;
 
 
 public class ViewGraph {
 	int windowWidth = 700;
 	int windowHeight = 316;
 	
 	//Create 5 snyonym nodes for testing.
 	SynonymNode syn[] = new SynonymNode[3];
 	AntonymNode ant[] = new AntonymNode[2];
 	MainNode main;
 	private Canvas graph;
 	
 	public ViewGraph(int width, int height){
 		windowWidth = width;
 		windowHeight = height;
 		start();
 	}
 
 	public void start() {
 		graph = new Canvas(windowWidth,windowHeight);
 		final GraphicsContext gc = graph.getGraphicsContext2D();
 		drawNodes(gc);
 	
 		final Queue<Integer> curX = new LinkedList<Integer>();
 		final Queue<Integer> curY = new LinkedList<Integer>();
 		
 		
 		graph.addEventHandler(MouseEvent.MOUSE_DRAGGED,
 				new EventHandler<MouseEvent>(){
 			public void handle(MouseEvent e){
 				curX.add((int)e.getX());
 				curY.add((int)e.getY());
 				
 				if(curX.size()>1){
 					//two positions added, move nodes.
 					gc.setFill(Color.WHITE);
 					gc.fillRect(0, 0, windowWidth, windowHeight);
 					gc.setFill(Color.BLACK);
 					
 					for(int i=0;i<3;i++){
 						syn[i].setX(syn[i].getX()-((Integer)curX.toArray()[0]-(Integer)curX.toArray()[1]));
 						syn[i].setY(syn[i].getY()-((Integer)curY.toArray()[0]-(Integer)curY.toArray()[1]));
 						redrawSyn(i,syn[i],gc);
 					}
 					
 					for(int i=0;i<2;i++){
 						ant[i].setX(ant[i].getX()-((Integer)curX.toArray()[0]-(Integer)curX.toArray()[1]));
 						ant[i].setY(ant[i].getY()-((Integer)curY.toArray()[0]-(Integer)curY.toArray()[1]));
 						redrawAnt(i,ant[i],gc);
 					}
 					
					main.setX(main.getX()-((int)curX.toArray()[0]-(int)curX.toArray()[1]));
					main.setY(main.getY()-((int)curY.toArray()[0]-(int)curY.toArray()[1]));
 					
 					curX.remove();
 					curY.remove();
 				}
 			}
 		});
 		
 	}
 	
 	private void drawNodes(GraphicsContext gc){
 		gc.setStroke(Color.BLACK);
 		
 		/** Main Node Code */
 		main = new MainNode("Hot",gc,0,0,windowWidth,windowHeight);
 		main.draw();
 		
 		syn[0] = new SynonymNode("Warm",gc,100,200);
 		syn[1] = new SynonymNode("Roasting",gc,300,100);
 		syn[2] = new SynonymNode("Boiling",gc,50,50);
 		
 		ant[0] = new AntonymNode("Cold",gc,500,150);
 		ant[1] = new AntonymNode("Freezing",gc,600,50);
 		
 		for(int i=0;i<3;i++){
 			syn[i].draw();
 			syn[i].drawConnector(main);
 		}
 		
 		for(int i=0;i<2;i++){
 			ant[i].draw();
 			ant[i].drawConnector(main);
 		}
 	}
 	
 	private void redrawSyn(int index, SynonymNode synNode, GraphicsContext gc){
 		main.redraw();
 		syn[index] = new SynonymNode(synNode.getValue(),gc,synNode.getX(),synNode.getY());
 		syn[index].draw();
 		syn[index].drawConnector(main);
 	}
 	
 	private void redrawAnt(int index, AntonymNode antNode, GraphicsContext gc){
 		ant[index] = new AntonymNode(antNode.getValue(),gc,antNode.getX(),antNode.getY());
 		ant[index].draw();
 		ant[index].drawConnector(main);
 	}
 	
 	public Canvas returnGraph(){
 		return graph;
 	}
 	
 }
