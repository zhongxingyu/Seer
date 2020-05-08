 package backgammon.listener;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionAdapter;
 import java.util.Vector;
 
 import backgammon.event.CheckerMoveEvent;
 import backgammon.model.player.Move;
 import backgammon.view.helpers.BChecker;
 import backgammon.view.helpers.BDice;
 import backgammon.view.helpers.ImageBoard;
 import backgammon.view.helpers.PHitBox;
 
 public class ImageBoardMouseListener extends MouseMotionAdapter implements
 		MouseListener {
 
 	private ImageBoard parent;
 	private static BChecker checker = null;
 	private static boolean dragging = false;
 
 	public ImageBoardMouseListener(ImageBoard parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent f) {
 
 		//Anzeigen der mglichen Zge
 		
 		Boolean found = false;
 
 		for (BDice dice : this.parent.getDices()) {
 			if (dice.getX() - 24 <= f.getX() && dice.getX() + 24 >= f.getX()
 					&& dice.getY() - 24 <= f.getY()
 					&& dice.getY() + 24 >= f.getY()) {
 				// Wrfel gefunden
 				found = true;
 			}
 		}
 		if (found) {
 
 			this.parent.repaint();
 
 			this.parent.getView().getController().initNextPlayerMove();
 		}
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	
 		
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 
 		// System.out.println("Press");
 		
 		//Falls noch animiert wird, Checker sperren
 		if(this.parent.getCheckerAnimation().isAnimating() ||
 		   this.parent.getDiceAnimation().isAnimating())
 			{
 				ImageBoardMouseListener.checker = null;
 				return;
 			}
 		
 
 		this.parent.clearInfo();
 		
 		if (!this.setCheckerFromEvent(e))
 			return;
 
 		if (this.parent.getView().getController()
 				.startMove(ImageBoardMouseListener.checker.getPlayer())) {
 			// System.out.println("Start dragging");
 			ImageBoardMouseListener.dragging = true;
 			this.parent.setFocus(ImageBoardMouseListener.checker);
 			//Set position
 			ImageBoardMouseListener.checker.setCoords(e.getX(), e.getY());
 		} else {
 			ImageBoardMouseListener.checker = null;
 		}
 
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent f) {
 		
 		//Falls noch animiert wird, Checker sperren
 		if(this.parent.getCheckerAnimation().isAnimating() ||
 		   this.parent.getDiceAnimation().isAnimating())
 			{
 				ImageBoardMouseListener.checker = null;
 				return;
 			}
 		
 		if(ImageBoardMouseListener.dragging == false)
 			return;
 		
 		if(ImageBoardMouseListener.checker == null)
 			return;
 		
 		//Event zusammenbauen
 		ImageBoardMouseListener.dragging = false;
 		
 		int oldX = ImageBoardMouseListener.checker.getPoint();
 	    int oldY = ImageBoardMouseListener.checker.getIndex();
 		
 		if(this.setPointFromEvent(f))
 		{	
 			System.out.println(Integer.toString(oldX) + " - " + Integer.toString(ImageBoardMouseListener.checker.getPoint()));
 			System.out.println(Integer.toString(oldY) + " - " + Integer.toString(ImageBoardMouseListener.checker.getIndex()));
 			
 			Move move = new Move(ImageBoardMouseListener.checker.getPlayer(), oldX, oldY, ImageBoardMouseListener.checker.getPoint(), ImageBoardMouseListener.checker.getIndex());	
 			CheckerMoveEvent moveEvent = new CheckerMoveEvent(move);			
 			ImageBoardMouseListener.checker = null;
 			this.parent.getView().getController().endMove(moveEvent);
 		}
 	}
 
 	public void mouseDragged(MouseEvent e) {
 
 		//Falls noch animiert wird, Checker sperren
 		if(this.parent.getCheckerAnimation().isAnimating() ||
 		   this.parent.getDiceAnimation().isAnimating())
 			{
 				ImageBoardMouseListener.checker = null;
 				return;
 			}
 		
 		if (!ImageBoardMouseListener.dragging) {
 			return;
 		}
 
 		// System.out.println("Drag");
 		ImageBoardMouseListener.checker.setCoords(e.getX(), e.getY());
 
 		this.parent.repaint();
 	}
 
 	private Boolean setCheckerFromEvent(MouseEvent f) {
 
 		if (ImageBoardMouseListener.checker != null)
 			return true;
 
 		Integer Point = 50;
 
 		Integer x = f.getPoint().x;// - e.getLocationOnScreen().x;
 		Integer y = f.getPoint().y;// - e.getLocationOnScreen().y;
 
 		Integer i = 1;
 		for (BChecker c : this.parent.getChecker()) {
 			if (c.getCoords() != null) {
 				// System.out.println(Integer.toString(i) + ". Checker: " +
 				// Integer.toString(c.getCoords().getX()) + " - " +
 				// Integer.toString(c.getCoords().getY()));
 				if (c.getCoords().getX() - 25 <= x
 						&& c.getCoords().getX() + 25 >= x
 						&& c.getCoords().getY() - 25 <= y
 						&& c.getCoords().getY() + 25 >= y) {
 					// innerhalb, also Point ausgeben
 					// System.out.println("Point: " +
 					// Integer.toString(c.getPoint()));
 					Point = c.getPoint();
 				}
 			}
 			i++;
 		}
 		// System.out.println("Mouse: " + Integer.toString(x) + " - " +
 		// Integer.toString(y));
 
 		if (Point != 50) {
 			// Kontroller Bescheid sagen.
 			// System.out.println("Point: " + Integer.toString(Point));
 			ImageBoardMouseListener.checker = this.parent
 					.getHighestChecker(Point);
 
 			if (ImageBoardMouseListener.checker != null)
 				return true;
 			else
 				return false;
 		} else {
 			// System.out.println("Test1");
 			ImageBoardMouseListener.checker = null;
 			return false;
 		}
 	}
 
 	private Boolean setPointFromEvent(MouseEvent f) {
 		Vector<PHitBox> tmp = ImageBoard.getPointHitBox();
 
 		// gucken ob wir auf einem Point landen, falls ja, dann holen wir uns
 		// den hchsten Index
 		// und platzieren den Checker dort.
 		//START POINT POSITION
 		boolean found = false;
 		int i = 0;
 		for (PHitBox h : tmp) {
 			if (f.getX() >= h.getX1() && f.getX() <= h.getX2()
 					&& f.getY() >= h.getY1() && f.getY() <= h.getY2()) 
 			{
 				// Hitbox gefunden, also point und Index setzen.
 				found = true;
 				// System.out.println("Hitbox gefunden");
 				int index = this.parent.getHighestIndex(i);
 				if (ImageBoardMouseListener.checker.getPoint() != i)
 				{	
 					this.parent.getCheckerAnimation().addMoveAnimation(
 							ImageBoardMouseListener.checker, i, index + 1);
					ImageBoardMouseListener.checker.setPointIndex(i, index+1);
 				}
 			}
 			i++;
 		}
 		//END POINT POSITION
 		//START OUT POSITION
 		tmp = ImageBoard.getOutHitBox();
 		i = 0;
 		for (PHitBox h : tmp) {
 			if (f.getX() >= h.getX1() && f.getX() <= h.getX2()
 					&& f.getY() >= h.getY1() && f.getY() <= h.getY2()) 
 			{
 				// Hitbox gefunden, also point und Index setzen.
 				found = true;
 				// System.out.println("Hitbox gefunden");
 				int point = 24 + ImageBoardMouseListener.checker.getPlayer();
 				
 				int index = this.parent.getHighestIndex(point);
 				if (ImageBoardMouseListener.checker.getPoint() != point)
 				{	
 					this.parent.getCheckerAnimation().addMoveAnimation(
 							ImageBoardMouseListener.checker, point, index + 1);
					ImageBoardMouseListener.checker.setPointIndex(i, index+1);
 				}
 			}
 			i++;
 		}
 		//ende OUT POSITION
 		// keine Hitbox getroffen, also zurcksetzen.
 		if (!found)
 		{
 			this.parent.getCheckerAnimation().addMoveAnimation(
 					ImageBoardMouseListener.checker,
 					ImageBoardMouseListener.checker.getPoint(),
 					ImageBoardMouseListener.checker.getIndex());
 				// ImageBoardMouseListener.checker.setCoordsFromPointAndIndex();
 			return false;
 		}
 		this.parent.repaint();
 		return true;
 	}
 
 }
