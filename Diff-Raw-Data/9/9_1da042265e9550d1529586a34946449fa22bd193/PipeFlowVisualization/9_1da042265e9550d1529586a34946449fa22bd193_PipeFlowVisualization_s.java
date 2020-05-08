 package pleocmd.itfc.gui;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 import pleocmd.pipe.PipePart;
 
 public final class PipeFlowVisualization extends Thread {
 
 	private final PipeConfigBoard board;
 
 	private final Deque<PipeFlow> queue;
 
 	private boolean modified;
 
 	PipeFlowVisualization(final PipeConfigBoard board) {
 		super("PipeFlow-Visualization-Thread");
 		this.board = board;
 		queue = new LinkedList<PipeFlow>();
 		setDaemon(true);
 		start();
 	}
 
 	public void addPipeFlow(final PipePart src, final PipePart dst) {
 		final PipeFlow pf = new PipeFlow(src, dst);
 		relayout(pf);
 		synchronized (queue) {
 			queue.add(pf);
 		}
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				Thread.sleep(100);
 			} catch (final InterruptedException e) {
 				break;
 			}
 			synchronized (queue) {
 				if (modified) {
 					modified = false;
 					for (final PipeFlow pf : queue)
 						relayout(pf);
 				}
 				List<PipeFlow> remove = null;
 				for (final PipeFlow pf : queue)
 					if (!pf.nextStep()) {
 						if (remove == null) remove = new ArrayList<PipeFlow>();
 						remove.add(pf);
 					}
 				if (remove != null) for (final PipeFlow pf : remove)
 					queue.remove(pf);
 				board.setPipeflow(queue);
 			}
 			board.repaint();
 		}
 		queue.clear();
 	}
 
 	private void relayout(final PipeFlow pf) {
		final double scale = board.getPainter().getScale();
 		final Point sp = new Point();
 		final Point dp = new Point();
 		BoardPainter.calcConnectorPositions(pf.getSrc().getGuiPosition(), pf
 				.getDst().getGuiPosition(), sp, dp);
		pf.getOrigin().x = sp.x * scale;
		pf.getOrigin().y = sp.y * scale;
 		pf.setTheta(Math.atan2(sp.y - dp.y, sp.x - dp.x));
 		final int xd = sp.x - dp.x;
 		final int yd = sp.y - dp.y;
		pf.setParams(Math.sqrt(xd * xd + yd * yd) * scale);
 	}
 
 	public void modified() {
 		synchronized (queue) {
 			modified = true;
 		}
 	}
 
 }
