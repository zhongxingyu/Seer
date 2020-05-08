 package de.ptb.epics.eve.viewer.views.engineview;
 
 import org.apache.log4j.Logger;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.ui.PlatformUI;
 
 import de.ptb.epics.eve.ecp1.client.interfaces.IChainStatusListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IConnectionStateListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IEngineStatusListener;
 import de.ptb.epics.eve.ecp1.client.interfaces.IErrorListener;
 import de.ptb.epics.eve.ecp1.client.model.Error;
 import de.ptb.epics.eve.ecp1.commands.ChainStatusCommand;
 import de.ptb.epics.eve.ecp1.types.EngineStatus;
 import de.ptb.epics.eve.ecp1.types.ErrorType;
 
 /**
  * @author Marcus Michalsky
  * @since 1.10
  */
 public class ProgressBarComposite extends Composite implements
 		IConnectionStateListener, IEngineStatusListener, IErrorListener,
 		IChainStatusListener {
 	private static final Logger LOGGER = Logger
 			.getLogger(ProgressBarComposite.class.getName());
 	
 	private ProgressBar progressBar;
 	private ProgressBarPaintListener progressBarPaintListener;
 	private int maxPositions = 100;
 	private int currentPosition = 0;
 	
 	private boolean connected;
 	private EngineStatus engineStatus;
 	
 	private Font font;
 	
 	/**
 	 * @param parent the parent
 	 * @param style the style
 	 * @param connected initial connection state
 	 */
 	public ProgressBarComposite(Composite parent, int style, boolean connected) {
 		super(parent, style);
 		FillLayout fillLayout = new FillLayout();
 		fillLayout.marginHeight = 3;
 		fillLayout.marginWidth = 3;
 		this.setLayout(fillLayout);
 		this.progressBar = new ProgressBar(this, SWT.HORIZONTAL);
 		this.progressBar.setState(SWT.NORMAL);
 		this.progressBar.setMinimum(0);
 		this.progressBar.setMaximum(this.maxPositions);
 		this.progressBar.setSelection(this.currentPosition);
 		this.progressBar.setEnabled(false);
 		this.progressBarPaintListener = new ProgressBarPaintListener(
 				this.progressBar);
 		this.progressBar.addPaintListener(this.progressBarPaintListener);
 		
 		this.connected = connected;
 		this.engineStatus = null;
 		
 		FontData fdata = Display.getCurrent().getSystemFont().getFontData()[0];
 		fdata.setStyle(SWT.BOLD);
 		this.font = new Font(Display.getCurrent(), fdata);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void chainStatusChanged(ChainStatusCommand chainStatusCommand) {
 		final ChainStatusCommand finalCommand = chainStatusCommand;
 		this.progressBar.getDisplay().syncExec(new Runnable() {
 			@Override public void run() {
				if (finalCommand.getPositionCounter() >= 0) {
					currentPosition = finalCommand.getPositionCounter();
 					progressBar.setSelection(currentPosition);
 					LOGGER.debug("Current Position: " + currentPosition);
 				}
 				progressBar.redraw();
 			}
 		});
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void errorOccured(Error error) {
 		if (error.getErrorType() == null) {
 			return;
 		}
 		if (error.getErrorType().equals(ErrorType.MAX_POS_COUNT)) {
 			final Error finalError = error;
 			LOGGER.debug("Max Poscount for new Scan: "
 					+ Integer.parseInt(finalError.getText()));
 			this.progressBar.getDisplay().syncExec(new Runnable() {
 				@Override public void run() {
 					maxPositions = Integer.parseInt(finalError.getText());
 					progressBar.setMaximum(maxPositions);
 					progressBar.redraw();
 				}
 			});
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void engineStatusChanged(EngineStatus engineStatus, String xmlName,
 			int repeatCount) {
 		if (engineStatus.equals(EngineStatus.IDLE_XML_LOADED)) {
 			this.engineStatus = engineStatus;
 			this.refreshStatus();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stackConnected() {
 		LOGGER.debug("Engine connected");
 		this.connected = true;
 		this.refreshStatus();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stackDisconnected() {
 		LOGGER.debug("Engine disconnected");
 		this.connected = false;
 		this.refreshStatus();
 	}
 	
 	private void refreshStatus() {
 		this.progressBar.getDisplay().syncExec(new Runnable() {
 			@Override public void run() {
 				if (connected) {
 					if (EngineStatus.IDLE_XML_LOADED.equals(engineStatus)) {
 						progressBar.setEnabled(true);
 						currentPosition = 0;
 						LOGGER.debug("new Scan -> enable ProgressBar");
 					} else if (EngineStatus.IDLE_NO_XML_LOADED.equals(engineStatus)) {
 						progressBar.setEnabled(false);
 						LOGGER.debug("no Scan -> disable ProgressBar");
 					}
 				} else {
 					progressBar.setEnabled(false);
 				}
 			}
 		});
 	}
 	
 	/* ********************************************************************* */
 	
 	/**
 	 * @author Marcus Michalsky
 	 * @since 1.10
 	 */
 	private class ProgressBarPaintListener implements PaintListener {
 		private ProgressBar progressBar;
 		
 		/**
 		 * @param bar the progress bar
 		 */
 		public ProgressBarPaintListener(ProgressBar bar) {
 			this.progressBar = bar;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void paintControl(PaintEvent e) {
 			if (!progressBar.getEnabled()) {
 				return;
 			}
 
 			/*
 			 * calculates the length of the progress bar. 
 			 * the ratio of the label text that overlaps with it 
 			 * gets a different color.
 			 */
 			Point point = progressBar.getSize();
 
 			StringBuffer percentage = new StringBuffer();
 			
 			if (maxPositions != 0) {
 				percentage.append(" (");
 				percentage.append((int)Math.ceil((float) currentPosition
 						/ (float) maxPositions * 100));
 				percentage.append(" %)");
 			}
 			String position = Integer.toString(currentPosition) + " / " + 
 					Integer.toString(maxPositions) + " positions" + 
 					percentage.toString();
 			
 			FontMetrics fontMetrics = e.gc.getFontMetrics();
 			int width = fontMetrics.getAverageCharWidth() * position.length();
 			int height = fontMetrics.getHeight();
 			e.gc.setClipping(e.gc.getClipping());
 			e.gc.setAntialias(SWT.ON);
 			e.gc.setTextAntialias(SWT.ON);
 			e.gc.setFont(font);
 			e.gc.setForeground(Display.getCurrent().getSystemColor(
 					SWT.COLOR_BLACK));
 			e.gc.drawString(position, (point.x - width) / 2,
 					(point.y - height) / 2, true); // transparency boolean
 
 			Rectangle all = e.gc.getClipping();//progressBar.getBounds();
 			Rectangle clip = new Rectangle(all.x, all.y, all.width
 					* progressBar.getSelection() / maxPositions, all.height);
 			e.gc.setClipping(clip);
 			e.gc.setForeground(Display.getCurrent().getSystemColor(
 					SWT.COLOR_WHITE));
 			e.gc.drawString(position, (point.x - width) / 2,
 					(point.y - height) / 2, true); // transparency boolean
 		}
 	}
 }
