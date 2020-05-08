 package presentation;
 
 import java.awt.Cursor;
 import java.awt.datatransfer.Transferable;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceMotionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JPanel;
 
 import controler.CCarte;
 import controler.CSabot;
 import dndListener.MyDragGestureListener;
 import dndListener.MyDragSourceListener;
 import dndListener.MyDragSourceMotionListener;
 
 public class PSabot extends JPanel {
 
 	private static final long serialVersionUID = -7826492851733908362L;
 
 	private CSabot cSabot;
 
 	private PTasDeCartes cachees;
 	private PTasDeCartes visibles;
 	private RetournerCartesListener rcl;
 	private RetournerTasListener rtl;
 
 	private DragGestureEvent theInitialEvent;
 	
 	private MyDragSourceListener myDragSourceListener;
 	private DragSource dragSource;
 	private MyDragSourceMotionListener myDragSourceMotionListener;
 
 	/**
 	 * Le constructeur
 	 * 
 	 * @param cachees
 	 * @param visibles
 	 */
 	public PSabot(PTasDeCartes cachees, PTasDeCartes visibles) {
 		super();
 		this.cachees = cachees;
 		this.visibles = visibles;
 		rcl = new RetournerCartesListener();
 		rtl = new RetournerTasListener();
 
 		add(cachees);
 		cachees.setDxDy(0, 0);
 		add(visibles);
 		visibles.setDxDy(25, 0);
 		
 		
 		// init DnD listener, peut etre fait dans une autre class
 		
 		myDragSourceListener = new MyDragSourceListener(this);
 		
 		dragSource = new DragSource();
 		
 		dragSource.createDefaultDragGestureRecognizer(visibles,
 		DnDConstants.ACTION_MOVE, new MyDragGestureListener(this));
 		
 		myDragSourceMotionListener = new MyDragSourceMotionListener();
 		dragSource.addDragSourceMotionListener((DragSourceMotionListener) myDragSourceMotionListener);
 
 
 	}
 
 	public void setcSabot(CSabot cSabot) {
 		this.cSabot = cSabot;
 	}
 
 	public void activerRetournerTas() {
 		cachees.addMouseListener(rtl);
 		cachees.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 	}
 
 	public void desactiverRetournerTas() {
 		cachees.removeMouseListener(rtl);
 		cachees.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 	}
 
 	public void activerRetournerCarte() {
 		cachees.addMouseListener(rcl);
 		cachees.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 	}
 
 	public void desactiverRetournerCarte() {
 		cachees.removeMouseListener(rcl);
 		cachees.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
 	}
 
 	
 	public void dragGestureRecognized(DragGestureEvent e) {
 		System.out.println("dragGestureRecognized");
 		CCarte cc;
 		PCarte pc;
 		theInitialEvent = e;
 		try {
 //			pc = (PCarte) getComponentAt(e.getDragOrigin()); // version du td
 			pc = (PCarte) visibles.getComponentAt(e.getDragOrigin());
 			
 			cc = pc.getControle();
 			
 			System.out.println("PCarte : " + pc.toString());
 			System.out.println("CCarte : " + cc.toString());
 			
 			// C'est le controle qui gere lui meme si cc est null apres que
 			// l'utilisateur est pas selectionne la bonne carte
 			cSabot.p2cDebutDnD(cc);
 		} catch (Exception e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public void c2pDebutDnDOK(PCarte pc) {
 		System.out.println("c2pDebutDnDOK");
 		System.out.println("CCarte : " + pc.getControle().toString());
 		
 		myDragSourceMotionListener.setCurrentMovedCard(pc); // Pour le deplacement graphique de la carte
 		
		JPanel castPC = (JPanel) pc;
		
		dragSource.startDrag(theInitialEvent, DragSource.DefaultMoveDrop, (Transferable) castPC, myDragSourceListener);
 		
 		// startDrag -> fait a l'aide de la presentation sabot + donnee
 		// transferee (= pc) + event (= theInitialEvent)
 	}
 
 	
 	
 	public void dragDropEnd(DragSourceDropEvent e) {
 		System.out.println("dragDropEnd");
 		
 		cSabot.p2cDragDropEnd(e.getDropSuccess(),
 				((PCarte) e.getSource()).getControle());
 	}
 
 	public void c2pDebutDnDKO() {
 		System.out.println("c2pDebutDnDKO : Le drag and drop n'a pas fonctionné");
 		
 		// S'il y avait besoin de faire un traitement sur un plantage du DnD
 		// Ici, il n'y a pas besoin de traitement en utilisanat AWT
 	}
 
 	public void c2pDebutDnDNull() {
 		System.out.println("c2pDebutDnDNull");
 		
 		// S'il y avait besoin de faire un traitement sur un plantage du DnD
 		// Ici, il n'y a pas besoin de traitement en utilisanat AWT
 	}
 	
 	
 
 	public class RetournerTasListener implements MouseListener {
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			try {
 				// System.out.println("retournerTasListener");
 				cSabot.retourner();
 			} catch (Exception e) {
 				// System.err.println("PSabot MouseClicked error");
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 	public class RetournerCartesListener implements MouseListener {
 
 		@Override
 		public void mouseClicked(MouseEvent arg0) {
 			try {
 				// System.out.println("retournerCartesListener");
 				cSabot.retournerCarte();
 			} catch (Exception e) {
 				// System.err.println("PSabot MouseClicked error");
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent arg0) {
 			// TODO Auto-generated method stub
 
 		}
 
 	}
 
 }
