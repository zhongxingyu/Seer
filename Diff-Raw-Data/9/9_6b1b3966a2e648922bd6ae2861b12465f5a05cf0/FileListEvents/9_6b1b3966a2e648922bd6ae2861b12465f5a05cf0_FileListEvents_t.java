 package filelist;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragGestureEvent;
 import java.awt.dnd.DragGestureListener;
 import java.awt.dnd.DragSource;
 import java.awt.dnd.DragSourceDragEvent;
 import java.awt.dnd.DragSourceDropEvent;
 import java.awt.dnd.DragSourceEvent;
 import java.awt.dnd.DragSourceListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 import javax.swing.TransferHandler;
 
 import components.MainWnd;
 
 import service.Common;
 import service.Dropper;
 
 public class FileListEvents  implements DragSourceListener, DragGestureListener {
 	FileList filelist;
 	Dropper dropper;
 	DragSource ds;
 	List<ListItem> droped_items = new Vector<ListItem>();
	boolean drop_in = false;
 	
 	public FileListEvents(FileList flist) { 
 		filelist = flist;
 		Init();
 	}
 	
 	void Init() {
         dropper = new Dropper(filelist, new Dropper.Listener() {
         	public void filesDropped(Dropper.Event ev) {
        		if (!(drop_in = (boolean)ev.getSource()))
         			Common._drop_initer.ProceedDrop(filelist, ev.getFiles());
         	}
         });
         
         ds = new DragSource();
         ds.createDefaultDragGestureRecognizer(filelist, DnDConstants.ACTION_COPY, this);        
         
         //dragging
         filelist.setDragEnabled(true);
         filelist.setTransferHandler(new FileTransferHandler());
         
         filelist.addMouseListener(new MouseListener() {
         	@Override
             public void mouseClicked(MouseEvent mouseEvent) {
         		if (mouseEvent.getClickCount() == 2) {
         			int index = filelist.locationToIndex(mouseEvent.getPoint());
         			if (index >= 0)
         				filelist.SetPlayed((ListItem) filelist.model.getElementAt(index));
         		}
             }
     		@Override
     		public void mouseEntered(MouseEvent arg0) {}
     		@Override
     		public void mouseExited(MouseEvent arg0) {}
     		@Override
     		public void mousePressed(MouseEvent arg0) {}
     		@Override
     		public void mouseReleased(MouseEvent arg0) {}
         });
         
         filelist.addKeyListener(new KeyListener() {
     		@Override
     		public void keyTyped(KeyEvent e) {}
     		@Override
     		public void keyPressed(KeyEvent e) {
     			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
     				int last_pos = filelist.getSelectedIndex() - 1;
     				for(Object obj: filelist.getSelectedValuesList()) {
 	    				if (filelist.parent.options.delete_files)
 	    					Common._trash.AddElem(((ListItem)obj).file, filelist.parent.options.delete_empty_folders);
     					filelist.model.removeElement(obj);
     				}
     				filelist.CalcSelect(last_pos, true);
     			}
     			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
     				filelist.SetPlayed(filelist.model.elementAt(filelist.getSelectedIndex()));
     			}
     		}
     		@Override
     		public void keyReleased(KeyEvent e) {}
         });
 	}
 	
     private class FileTransferHandler extends TransferHandler {
 		private static final long serialVersionUID = 1873106934925755472L;
 		@Override
     	protected Transferable createTransferable(JComponent c) { return null; 	}
     	@Override
     	public int getSourceActions(JComponent c) {
     		return COPY; //MOVE
     	}
     }
 
     private class FileTransferable implements Transferable {
     	private List<File> files;
 
     	public FileTransferable(List<File> files) { this.files = files;	}
     	@Override
     	public DataFlavor[] getTransferDataFlavors() {
     		return new DataFlavor[]{DataFlavor.javaFileListFlavor};
     	}
 		@Override
 		public boolean isDataFlavorSupported(DataFlavor flavor) {
 			return flavor.equals(DataFlavor.javaFileListFlavor);
 		}
 		@Override
 		public Object getTransferData(DataFlavor flavor)
 				throws UnsupportedFlavorException, IOException {
 			if (!isDataFlavorSupported(flavor)) {
     			throw new UnsupportedFlavorException(flavor);
     		}
     		return files;
 		}
     }
 
 	@Override
 	public void dragGestureRecognized(DragGestureEvent dge) {
 		List<File> files = new ArrayList<File>();
 		ListItem temp;
 		for (Object obj: filelist.getSelectedValuesList()) {
 			temp = (ListItem)obj;
 			droped_items.add(temp);
 			files.add(temp.file);
 		}
 
 		ds.startDrag(dge, DragSource.DefaultCopyDrop, new FileTransferable(files), this);
 	}
 	@Override
 	public void dragEnter(DragSourceDragEvent dsde) {}
 	@Override
 	public void dragOver(DragSourceDragEvent dsde) {}
 	@Override
 	public void dropActionChanged(DragSourceDragEvent dsde) {}
 	public void dragExit(DragSourceEvent dse) {}
 	@Override
 	public void dragDropEnd(DragSourceDropEvent dsde) {
	    if (dsde.getDropSuccess() && !drop_in) {
 	        for(ListItem li : droped_items)
 	        	li.SetStatusLiked();
 	        MainWnd.wnd.repaint();
 	    }
 	    droped_items.clear();
	    drop_in = false;
 	} 	
 }
