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
  * A Qt widget used to embed JFreeChart charts in a Qt-based GUI
  */
 public class ChartViewer extends QWidget {
 	
 	private static final int DEFAULT_WIDTH = 500;
 	private static final int DEFAULT_HEIGHT = 300;
 	
 	private JFreeChart chart = null;
     private double scaleFactor;
 
     private QToolBar toolBar;
     private QLabel chartLabel;
     private QScrollArea scrollArea;
 
     private QAction fitToWindowAct;
     private QAction normalSizeAct;
     private QAction zoomOutAct;
     private QAction zoomInAct;
 
     public ChartViewer() {
 		this(null);
 	}
 	
 	public ChartViewer(QWidget parent) {
 		super(parent);
 
         initUI();
         initToolBar();
 	}
 
     private void initUI() {
         // Widgets
         toolBar = new QToolBar();
 
         chartLabel = new QLabel();
         chartLabel.setScaledContents(true);
         chartLabel.setBackgroundRole(QPalette.ColorRole.Base);
         chartLabel.setSizePolicy(QSizePolicy.Policy.Ignored, QSizePolicy.Policy.Ignored);
 
         scrollArea = new QScrollArea();
         scrollArea.setBackgroundRole(QPalette.ColorRole.Dark);
         scrollArea.setWidget(chartLabel);
 
         // Layout
         QVBoxLayout layout = new QVBoxLayout(this);
         layout.setMargin(0);
         layout.addWidget(toolBar);
         layout.addWidget(scrollArea, 1);
     }
 
     private void initToolBar() {
         // Actions
         QAction copyAction = new QAction(tr("&Copy"), this);
 		copyAction.triggered.connect(this, "copy()");
 
 		QAction saveAsAction = new QAction(tr("&Save As..."), this);
 		saveAsAction.setIcon(new QIcon("classpath:/icons/save.png"));
 		saveAsAction.triggered.connect(this, "saveAs()");
 
         zoomInAct = new QAction(tr("Zoom &In"), this);
         zoomInAct.setShortcut(tr("Ctrl++"));
         zoomInAct.setEnabled(false);
         zoomInAct.triggered.connect(this, "zoomIn()");
 
         zoomOutAct = new QAction(tr("Zoom &Out"), this);
         zoomOutAct.setShortcut(tr("Ctrl+-"));
         zoomOutAct.setEnabled(false);
         zoomOutAct.triggered.connect(this, "zoomOut()");
 
         normalSizeAct = new QAction(tr("&Normal Size"), this);
         normalSizeAct.setEnabled(false);
         normalSizeAct.triggered.connect(this, "normalSize()");
 
         fitToWindowAct = new QAction(tr("&Fit to Window"), this);
         fitToWindowAct.setEnabled(false);
         fitToWindowAct.setCheckable(true);
         fitToWindowAct.triggered.connect(this, "fitToWindow()");
 
         // Tool bar
 		toolBar.addAction(copyAction);
 		toolBar.addAction(saveAsAction);
         toolBar.addSeparator();
         toolBar.addAction(zoomInAct);
         toolBar.addAction(zoomOutAct);
         toolBar.addAction(normalSizeAct);
         toolBar.addAction(fitToWindowAct);
     }
 	
 	public JFreeChart getChart() {
 		return chart;
 	}
 	
 	public void setChart(JFreeChart chart) {
 		this.chart = chart;
 		
 		renderChart(DEFAULT_WIDTH, DEFAULT_HEIGHT);
 		update();
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
 						chartLabel.setPixmap(QPixmap.fromImage(img));
                         scaleFactor = 1.0;
 
                         fitToWindowAct.setEnabled(true);
                         updateActions();
 
                         if(!fitToWindowAct.isChecked()) {
                             chartLabel.adjustSize();
                         }
 					}
 				});
 			}
 		}, "ChartRenderThread").start();
 	}
 
     private void scaleChart(double factor) {
         scaleFactor *= factor;
 
         chartLabel.resize(
                 (int) (scaleFactor * chartLabel.pixmap().width()),
                 (int) (scaleFactor * chartLabel.pixmap().height()));
 
         adjustScrollBar(scrollArea.horizontalScrollBar(), factor);
         adjustScrollBar(scrollArea.verticalScrollBar(), factor);
 
         zoomInAct.setEnabled(scaleFactor < 3.0);
         zoomOutAct.setEnabled(scaleFactor > 0.333);
     }
 
     private void adjustScrollBar(QScrollBar scrollBar, double factor) {
         scrollBar.setValue((int) (factor * scrollBar.value() + ((factor - 1) * scrollBar.pageStep() / 2)));
     }
 
     private void updateActions() {
         zoomInAct.setEnabled(!fitToWindowAct.isChecked());
         zoomOutAct.setEnabled(!fitToWindowAct.isChecked());
         normalSizeAct.setEnabled(!fitToWindowAct.isChecked());
     }
 
     @SuppressWarnings("unused")
     private void zoomIn() {
         scaleChart(1.25);
     }
 
     @SuppressWarnings("unused")
     private void zoomOut() {
         scaleChart(0.75);
     }
 
     @SuppressWarnings("unused")
     private void normalSize() {
         chartLabel.adjustSize();
 
         scaleFactor = 1.0;
     }
 
     @SuppressWarnings("unused")
     private void fitToWindow() {
         boolean fitToWindow = fitToWindowAct.isChecked();
 
         scrollArea.setWidgetResizable(fitToWindow);
         if (!fitToWindow) {
             normalSize();
         }
         updateActions();
     }
 
     @SuppressWarnings("unused")
 	private void copy() {
 		QApplication.clipboard().setPixmap(chartLabel.pixmap());
 	}
 
     @SuppressWarnings("unused")
 	private void saveAs() {
 		// Show dialog
 		String filter = tr("PNG Image (*.png)");
 		String file = MessageDialog.getSaveFileName(window(), tr("Save As"), filter);
 		
 		if (file == null || file.isEmpty())
 			return;
 		
 		// Write to file
         boolean result = chartLabel.pixmap().save(file, "png");
 
 		if (!result) {
             MessageDialog.showException(window(),
                     new RuntimeException("Unable to save chart"));
         }
 	}
 }
