 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;
 
import gda.analysis.io.ADSCImageLoader;
 import gda.util.TestUtils;
 
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.PlotServer;
 import uk.ac.diamond.scisoft.analysis.PlotServerProvider;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.plotserver.AxisMapBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.DataSetWithAxisInformation;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
 import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
 import uk.ac.diamond.scisoft.analysis.rcp.views.PlotView;
 import uk.ac.diamond.scisoft.analysis.utils.PluginTestHelpers;
 
 /**
  *
  */
 public class DiffractionViewerPluginTest {
 
 	@Test
 	public final void testShowView() throws Exception {
 		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 
 		String TestFileFolder = TestUtils.getGDALargeTestFilesLocation();
 		AbstractDataset data = new ADSCImageLoader(TestFileFolder + "ADSCImageTest/F6_1_001.img").loadFile()
 				.getDataset(0);
 		
 		
 		PlotView plotView = (PlotView) window.getActivePage().showView("uk.ac.diamond.scisoft.analysis.rcp.plotView1");
 		PlotServer plotServer = PlotServerProvider.getPlotServer();
 		GuiBean guiState = plotServer.getGuiState("Plot 1");
 		if (guiState == null) {
 			guiState = new GuiBean();
 		}
 		
 		DataBean datab = new DataBean();
 		
 		DataSetWithAxisInformation dswai = new DataSetWithAxisInformation();
 		AxisMapBean amb = new AxisMapBean(AxisMapBean.DIRECT);
 		dswai.setAxisMap(amb);
 		dswai.setData(data);
 		datab.addData(dswai);
 		guiState.put(GuiParameters.PLOTMODE, GuiPlotMode.TWOD);
 		plotView.processGUIUpdate(guiState);
     	plotView.processPlotUpdate(datab);
 		PluginTestHelpers.delay(300000); // time to 'play with the graph if wanted
 	}
 
 }
