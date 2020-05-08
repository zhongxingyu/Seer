 package javachallenge.graphics;
 
 import java.awt.Dimension;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import javachallenge.common.Direction;
 import javachallenge.common.Point;
 import javachallenge.graphics.components.Label;
 import javachallenge.graphics.components.MapPanel;
 import javachallenge.graphics.components.Panel;
 import javachallenge.graphics.components.Screen;
 import javachallenge.graphics.components.ScrollableList;
 import javachallenge.graphics.components.ScrollablePanel;
 import javachallenge.graphics.components.Sprite;
 import javachallenge.graphics.util.AnimatedImage;
 import javachallenge.graphics.util.ColorMaker;
 import javachallenge.graphics.util.HTMLMaker;
 import javachallenge.graphics.util.ImageHolder;
 import javachallenge.graphics.util.Mover;
 import javachallenge.graphics.util.Position;
 import javachallenge.server.Map;
 
 public class GraphicClient {
 	public static int x[]={0,1,1,0,-1,-1};
 	public static int y[]={-1,-1,0,1,0,-1};
 	public static int moveSpeed = 300, moveSteps = 25;
 	public static int attackSpeed = 90, attackSteps = 10;
 
 	protected MapPanel panel;
 	protected java.util.Map<Integer,Sprite> flags=new TreeMap<Integer,Sprite>();
 	protected java.util.Map<Integer,Sprite> spawnPoints=new TreeMap<Integer,Sprite>();
 	protected java.util.Map<Integer,Sprite> units=new TreeMap<Integer,Sprite>();
 	protected PlayGround ground;
 	protected LogMonitor logMonitor;
 
 	public void setTime(int a)
 	{
 		ground.getStatus().setTime(a);
 		//ground.getStatus().getTime().setText(new Integer(a).toString());
 	}
 	public void setScore(int id, int a,double ratio)
 	{
 		ground.getStatus().updateScore(id,a,ratio);
 		//	ground.getStatus().getScore().setText(new Integer(a).toString());
 	}
 
 	public MapPanel getPanel() {
 		return panel;
 	}
 
 	public void setPanel(MapPanel panel) {
 		this.panel = panel;
 	}
 	public GraphicClient(int width,int height, final Position[] positions,int Players) throws NullPointerException,OutOfMapException{
 		this (new Map(width, height, 3, null, null) {
 			{
 				flagLocations = new ArrayList<Point>();
 				for (Position position : positions)
 					flagLocations.add(new Point(position.getX(), position.getY()));
 			}
 		});
 	}
 
 	public GraphicClient(Map map) throws OutOfMapException
 	{
 		int Players = map.getTeamCount() ; 
 		ground=new PlayGround();
 		ground.createScreenElements(panel=new MapPanel(map) {
 			@Override
 			public void onClick(int x, int y) {
 				/*try
 				{
 					spawn(units.size()+1, new Position(x, y));
 				} catch (OutOfMapException e)
 				{
 					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 				} catch (DuplicateMemberException e)
 				{
 					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 				}*/
 			}
 		});
 		ground.getStatus().addBars(Players);
 		for (int i = 0; i < map.getFlagLocations().size(); i++) {
 			Position position = new Position(map.getFlagLocations().get(i));
 			flags.put(i+1, panel.setFlag(position, i));
 		}
 		for (int i = 0; i < map.getSpawnLocations().size(); i++) {
 			Position position = new Position(map.getSpawnLocations().get(i));
 			if (panel.isOut(position)) throw new OutOfMapException();
 			Sprite spawn = new AnimatedImage(ImageHolder.Objects.mage, 250, position);
 			panel.addToContainer(spawn ,2);
 			spawnPoints.put(i+1, spawn);
 		}
 	}
 
 	public void spawn(Integer id,Position position) throws OutOfMapException, DuplicateMemberException
 	{
 		if (panel.isOut(position)) throw new OutOfMapException();
 		if (units.get(id)!=null) throw new DuplicateMemberException();
 		Sprite sprite=new Sprite(ImageHolder.Units.wesfolkOutcast, position);
 		units.put(id,sprite);
 		panel.addToContainer(sprite,3);
 	}
 
 	public void die(Integer id) throws NullPointerException{
 		Sprite sprite=units.get(id);
 		units.remove(id);
 		sprite.setVisible(false);
 		panel.remove(sprite);
 	}
 
 	public void attack(Integer id, Direction dir) {
 		int direction = dir.ordinal();
 		final Sprite sprite = units.get(id);
 		final Position position = MapPanel.getAbsolutePosition(x[direction], y[direction]);
 		position.x /= 2;
 		position.y /= 2;
 		if (direction == 1 || direction == 2)
 			units.get(id).setIcon(ImageHolder.Units.wesfolkOutcast);
 		if (direction == 4 || direction == 5)
 			units.get(id).setIcon(ImageHolder.Units.wesfolkOutcastMirror);
 		new Mover(sprite,position,attackSpeed/attackSteps,attackSteps) {
 			@Override
 			public void atTheEnd() {
 				position.x *= -1;
 				position.y *= -1;
 				new Mover(sprite,position,attackSpeed/attackSteps,attackSteps).start();
 			}
 		}.start();
 	}
 
 	public void move(Integer id,Direction dir) throws NullPointerException {
 		int direction=dir.ordinal();
 		Sprite sprite=units.get(id);
 		Position position=MapPanel.getAbsolutePosition(x[direction],y[direction]);
 		if (direction == 1 || direction == 2)
 			units.get(id).setIcon(ImageHolder.Units.wesfolkOutcast);
 		if (direction == 4 || direction == 5)
 			units.get(id).setIcon(ImageHolder.Units.wesfolkOutcastMirror);
 		new Mover(sprite,position,moveSpeed/moveSteps,moveSteps).start();
 	}
 
 	public void obtainFlag (Integer id)  throws NullPointerException{
 		Sprite flag = flags.get(id);
 		flag.setVisible(false);
 		panel.remove(flag);
 		flags.remove(id);
 		flags.put(id, new Sprite(ImageHolder.Objects.underFire, flag.getPosition()));
 		panel.addToContainer(flags.get(id), 2);
 	}
 
 	public void setFlagStatus(Integer id, int progressTeam, int progressPercent, int curTeam){
		Sprite flag = flags.get(id);
		flag.setVisible(false);
		panel.remove(flag);
		flags.remove(id);
		flags.put(id, new AnimatedImage(ImageHolder.Objects.flags[curTeam + 1], 200, flag.getPosition()));
		panel.addToContainer(flags.get(id), 2);
 	}
 	
 	public void log (String message) {
 		ground.addLog(message);
 	}
 	
 	public static class DuplicateMemberException extends Exception
 	{
 
 	}
 	public static class OutOfMapException extends Exception
 	{
 	}
 	
 	public static class LogMonitor extends Screen {
 		protected ScrollableList scrollableList;
 		
 		public LogMonitor() {
 			super("Log Monitor");
 			getContentPane().setBackground(ColorMaker.black);
 			setPreferredSize(new Dimension(500, 400));
 			scrollableList = new ScrollableList(20, 0, getWidth() - 30, getHeight() - 30, ColorMaker.black, true);
 			add (scrollableList);
 			
 			// resize sensitive dimension updater
 			addComponentListener(new ComponentListener() {
 				public void componentShown(ComponentEvent arg0) {}
 				public void componentMoved(ComponentEvent arg0) {}
 				public void componentHidden(ComponentEvent arg0) {}
 				public void componentResized(ComponentEvent arg0) {
 					updateDimensions();
 				}
 			});
 		}
 		
 		private void updateDimensions() {
 			Dimension size = getSize();
 			scrollableList.setSize(size.width - 10, size.height - 30);
 			scrollableList.getScroll().setSize(size.width - 20, size.height - 30);
 		}
 		
 		void addLog (String message) {
 			scrollableList.addComponent(new Label(new HTMLMaker(message, ColorMaker.green, 10).toString()), 20);
 		}
 	}
 	
 	public static void main(String[] args) {
 		LogMonitor logMonitor = new LogMonitor();
 		logMonitor.setVisible(true);
 		for (int i = 0; i < 20; i++)
 		logMonitor.addLog("salam!!!!");
 		logMonitor.addLog("aleyk!!!!");
 	}
 }
