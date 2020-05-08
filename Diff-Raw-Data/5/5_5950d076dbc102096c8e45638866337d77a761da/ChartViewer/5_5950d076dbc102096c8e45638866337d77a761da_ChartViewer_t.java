 /*
  * Copyright (c) 2008-2009 The Negev Project
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  * - Redistributions of source code must retain the above copyright notice, 
  *   this list of conditions and the following disclaimer.
  *
  * - Redistributions in binary form must reproduce the above copyright notice, 
  *   this list of conditions and the following disclaimer in the documentation 
  *   and/or other materials provided with the distribution.
  *
  * - Neither the name of The Negev Project nor the names of its contributors 
  *   may be used to endorse or promote products derived from this software 
  *   without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package negev.giyusit.widgets;
 
 import com.trolltech.qt.core.*;
 import com.trolltech.qt.gui.*;
 
 import java.io.ByteArrayOutputStream;
 
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 
 import negev.giyusit.util.MessageDialog;
 
 /**
  * A Qt widget used to embbed JFreeChart charts in a Qt-based GUI
  */
 public class ChartViewer extends QWidget {
 	
 	private static final int DEFAULT_WIDTH = 500;
 	private static final int DEFAULT_HEIGHT = 300;
 	
 	private JFreeChart chart = null;
 	private QImage chartImage = null;
 	
 	private QMenu popupMenu;
 	
 	public ChartViewer() {
 		this(null);
 	}
 	
 	public ChartViewer(QWidget parent) {
 		super(parent);
 		
 		// Actions and pop-up menu
 		QAction copyAction = new QAction(tr("&Copy"), this);
 		copyAction.triggered.connect(this, "copy()");
 		
 		QAction saveAsAction = new QAction(tr("&Save As..."), this);
 		saveAsAction.setIcon(new QIcon("classpath:/icons/save.png"));
 		saveAsAction.triggered.connect(this, "saveAs()");
 		
 		popupMenu = new QMenu();
 		popupMenu.addAction(copyAction);
 		popupMenu.addAction(saveAsAction);
 	}
 	
 	public JFreeChart getChart() {
 		return chart;
 	}
 	
 	public void setChart(JFreeChart chart) {
 		this.chart = chart;
 		this.chartImage = null;
 		
 		renderChart(width(), height());
 		update();
 	}
 	
 	@Override
 	public QSize sizeHint() {
 		return new QSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 	}
 	
 	@Override
 	protected void contextMenuEvent(QContextMenuEvent e) {
 		if (chartImage != null)
 			popupMenu.exec(e.globalPos());
 	}
 	
 	@Override
 	protected void resizeEvent(QResizeEvent e) {
 		super.resizeEvent(e);
 		
 		renderChart(width(), height());
 	}
 	
 	@Override
 	protected void paintEvent(QPaintEvent e) {
 		super.paintEvent(e);
 		
 		QPainter painter = new QPainter(this);
 		
 		if (chart == null) {
 			// Show a message if there is no chart
 			String msg = tr("Chart is not available");
 			
 			int x = (width() - painter.fontMetrics().width(msg)) / 2;
 			int y = height() / 2;
 			
 			painter.drawText(x, y, msg);
 		}
 		else if (chartImage == null) {
 			// Didn't finish rendering the chart yet
 			String msg = tr("Generating chart...");
 			
 			int x = (width() - painter.fontMetrics().width(msg)) / 2;
 			int y = height() / 2;
 			
 			painter.drawText(x, y, msg);
 		}
 		else {
 			// Paint the image buffer to the widget surface
 			painter.drawImage(0, 0, chartImage);
 		}
 		
 		painter.end();
 	}
 	
 	private void renderChart(final int width, final int height) {
 		if (chart == null)
 			return;
 		
 		// Do the rendering in a new thread
 		new Thread(new Runnable() {
 			public void run() {
 				// Render the chart into a byte array, and then load it into the 
 				// image buffer
 				ByteArrayOutputStream stream = new ByteArrayOutputStream();
 				
 				try {
					// Clone the chart object (to avoid threading errors)
					JFreeChart clonedChart = (JFreeChart) chart.clone();
					
					ChartUtilities.writeChartAsPNG(stream, clonedChart, width, height);
 				}
 				catch (Exception ex) {
 					ex.printStackTrace();
 				}
 				
 				final QImage img = QImage.fromData(new QByteArray(stream.toByteArray()));
 				
 				// Signal the main thread
 				QApplication.invokeAndWait(new Runnable() {
 					public void run() {
 						chartImage = img;
 						
 						// Ask for a repaint of the widget
 						update();
 					}
 				});
 			}
 		}, "ChartRenderThread").start();
 	}
 	
 	private void copy() {
 		QApplication.clipboard().setImage(chartImage);
 	}
 	
 	private void saveAs() {
 		// Show dialog
 		String filter = tr("PNG Image (*.png)");
 		String file = MessageDialog.getSaveFileName(window(), tr("Save As"), filter.toString());
 		
 		if (file == null || file.isEmpty())
 			return;
 		
 		// Write to file
 		QImageWriter writer = new QImageWriter(file, new QByteArray("png"));
 		
 		writer.write(chartImage);
 		
 		if (writer.error() == QImageWriter.ImageWriterError.DeviceError) {
 			QIODevice dev = writer.device();
 			
 			if (dev != null) {
 				String errMsg = "Unable to save chart: " + dev.errorString();
 				Exception ex = new RuntimeException(errMsg);
 				
 				MessageDialog.showException(window(), ex);
 			}
 		}
 	}
 }
