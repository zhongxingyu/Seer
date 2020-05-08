 package pt.ist.expenditureTrackingSystem.presentationTier.actions.statistics;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.AxisLocation;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.CategoryDataset;
 import org.jfree.data.DefaultCategoryDataset;
 
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.AcquisitionProcessStateType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.AllocateFundsPermanently;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.AllocateProjectFundsPermanently;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ApproveAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.CreateAcquisitionPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.FixInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.FundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.PayAcquisition;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ProjectFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.ReceiveInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SendPurchaseOrderToSupplier;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SkipPurchaseOrderDocument;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForApproval;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForConfirmInvoice;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.SubmitForFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.simplified.activities.UnsetSkipSupplierFundAllocation;
 import pt.ist.expenditureTrackingSystem.domain.processes.AbstractActivity;
 import pt.ist.expenditureTrackingSystem.domain.statistics.SimplifiedProcessActivityLogStatistics;
 import pt.ist.expenditureTrackingSystem.domain.statistics.SimplifiedProcessStatistics;
 import pt.ist.expenditureTrackingSystem.domain.statistics.SimplifiedProcessActivityLogStatistics.LogEntry;
 import sun.awt.image.codec.JPEGImageEncoderImpl;
 
 import com.sun.image.codec.jpeg.JPEGImageEncoder;
 
 public class ChartGenerator {
 
     protected static JFreeChart createBarChart(final CategoryDataset categoryDataset, final String title) {
 	final JFreeChart chart = ChartFactory.createBarChart3D(title, "", "",
 		categoryDataset, PlotOrientation.VERTICAL, true, true, false);
 	chart.setBackgroundPaint(new Color(0xF5, 0xF5, 0xF5));
 
 	final CategoryPlot plot = chart.getCategoryPlot();
 	plot.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
 
 	final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
 	rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
 	return chart;
     }
 
     protected static byte[] createBarChartImage(final CategoryDataset categoryDataset, final String title)
     		throws RuntimeException, IOException {
 	final JFreeChart chart = createBarChart(categoryDataset, title);
 	final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 	final JPEGImageEncoder imageEncoder = new JPEGImageEncoderImpl(outputStream);
 	final BufferedImage bufferedImage = chart.createBufferedImage(625, 475);
 	imageEncoder.encode(bufferedImage);
 	bufferedImage.flush();
 	outputStream.close();
 	return outputStream.toByteArray();	
     }
 
     public static CategoryDataset simplifiedProcessStatisticsChart(final SimplifiedProcessStatistics simplifiedProcessStatistics) {
 	final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
 
 	final Map<AcquisitionProcessStateType, Integer>	map = simplifiedProcessStatistics.getNumberOfProcessesByAcquisitionProcessStateType();
 	char c = 'A';
 	for (final Entry<AcquisitionProcessStateType, Integer> entry : map.entrySet()) {
 	    final AcquisitionProcessStateType acquisitionProcessStateType = entry.getKey();
 	    final Integer numberOfProcesses = entry.getValue();
 
 	    if (numberOfProcesses.intValue() > 0) {
 		dataset.addValue(numberOfProcesses, "" + c + " - " + acquisitionProcessStateType.getLocalizedName(), Character.valueOf(c++));
 	    }
 	}
 
 	return dataset;
     }
 
     private static CategoryDataset simplifiedProcessStatisticsActivityTimeChart(final SimplifiedProcessActivityLogStatistics simplifiedProcessActivityLogStatistics) {
 	final DefaultCategoryDataset dataset = new DefaultCategoryDataset();
 
 	char c = 'A';
 	for (final LogEntry logEntry : simplifiedProcessActivityLogStatistics.getLogEntries()) {
 	    final AbstractActivity abstractActivity = logEntry.getAbstractActivity();
 	    if (isRelevanteActivity(abstractActivity)) {
 		final String name = abstractActivity.getLocalizedName();
 		int indexOfSpan = name.indexOf('<');
 		final String label = indexOfSpan > 0 ? name.substring(0, indexOfSpan) : name;
 		dataset.addValue(logEntry.getDays(), "" + c + " - " + label, Character.valueOf(c++));
 	    }
 	}
 
 	return dataset;
     }
 
     private static boolean isRelevanteActivity(final AbstractActivity abstractActivity) {
 	if (abstractActivity == null) {
 	    return false;
 	}
 	final Class clazz = abstractActivity.getClass();
 	return clazz == CreateAcquisitionPurchaseOrderDocument.class
 		|| clazz == SendPurchaseOrderToSupplier.class
 		|| clazz == SkipPurchaseOrderDocument.class
 		|| clazz == SubmitForApproval.class
 		|| clazz == SubmitForFundAllocation.class
		|| clazz == ApproveAcquisitionProcess.class
 		|| clazz == AllocateProjectFundsPermanently.class
 		|| clazz == AllocateFundsPermanently.class
 		|| clazz == ProjectFundAllocation.class
 		|| clazz == FundAllocation.class
 		|| clazz == PayAcquisition.class
 		|| clazz == ReceiveInvoice.class
 		|| clazz == FixInvoice.class
 		|| clazz == SubmitForConfirmInvoice.class
 		|| clazz == ConfirmInvoice.class
 		|| clazz == SetSkipSupplierFundAllocation.class
 		|| clazz == UnsetSkipSupplierFundAllocation.class;
     }
 
     public static byte[] simplifiedProcessStatisticsImage(final SimplifiedProcessStatistics simplifiedProcessStatistics)
     		throws RuntimeException, IOException {
 	final CategoryDataset dataset = simplifiedProcessStatisticsChart(simplifiedProcessStatistics);
 	return createBarChartImage(dataset, "Número de Processos por Estado");
     }
 
     public static byte[] simplifiedProcessStatisticsActivityTimeImage(final SimplifiedProcessActivityLogStatistics simplifiedProcessActivityLogStatistics)
 	throws RuntimeException, IOException {
 	final CategoryDataset dataset = simplifiedProcessStatisticsActivityTimeChart(simplifiedProcessActivityLogStatistics);
 	return createBarChartImage(dataset, "Dias por Actividade");
     }
 
 }
