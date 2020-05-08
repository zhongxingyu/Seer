 /*
  * Part of Fresco software under GPL licence
  * http://www.gnu.org/licenses/gpl-3.0.txt
  */
 package support.regmarks;
 
 import fresco.CData;
 import java.awt.image.BufferedImage;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.LinkedList;
 import javax.swing.JDialog;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import support.CSupportWorker;
 import utils.metrics.CAreaSimilarityMetric;
 import utils.metrics.CCrossCorrelationMetric;
 import workers.registration.CPointPairs;
 
 /**
  *
  * @author gimli
  */
 public class CPointPairsOverview extends CSupportWorker<JDialog, Void> {
 
 	private final LinkedList<CAreaSimilarityMetric> metrics = new LinkedList<CAreaSimilarityMetric>();
 	private CPointPairs pairs;
 	private double[][] values;
 	private final String[] columnNames;
 	private Object[][] rows;
 
 	public CPointPairsOverview(CPointPairs pairs, BufferedImage imageA, BufferedImage imageB) {
 		this.pairs = pairs;
 		metrics.add(new CCrossCorrelationMetric(imageA, imageB, 25, CAreaSimilarityMetric.Shape.CIRCULAR));
 		columnNames = new String[]{"No.", "cross corelation"};
 
 		values = new double[pairs.size()][metrics.size()];
 	}
 
 	private void countValues() {
 		for(int i=0; i< pairs.size(); i++) {
 			int j=0;
 			for(CAreaSimilarityMetric metric: metrics) {
 				values[i][j] = metric.getDistance(pairs.getOrigin(i), pairs.getProjected(i));
 				j++;
 				setProgress((i*metrics.size()+j)*100/pairs.size()/metrics.size());
 			}
 		}
 	}
 
 	public JTable createGui() {
 		rows = new Object[pairs.size()][metrics.size() + 1];
 
 		for(int i=0; i< pairs.size(); i++) {
 			int j = 0;
			rows[i][j++] = i+1;
 			for(int k=0; k< metrics.size(); k++) {
 				rows[i][j++] = values[i][k];
 			}
 		}
 
 		sortRows(1);
 
 		JTable gui = new JTable(rows, columnNames);
 
 		return gui;
 	}
 
 	private void sortRows(int field) {
 		Arrays.sort(rows, new CRowComparator(field));
 	}
 
 	@Override
 	public String getWorkerName() {
 		return "Registration marks metrics overview";
 	}
 
 	@Override
 	protected JDialog doInBackground() throws Exception {
 		JDialog dialog = new JDialog(CData.mainFrame, "Registration marks quality.", false);
 
 		countValues();
 		JScrollPane scroll = new JScrollPane(createGui());
 		dialog.add(scroll);
 		dialog.pack();
 
 		return dialog;
 	}
 
 	private class CRowComparator implements Comparator<Object[]> {
 
 		int field = 0;
 
 		public CRowComparator(int fieldToComapre) {
 			field = fieldToComapre;
 		}
 
 		@Override
 		public int compare(Object[] o1, Object[] o2) {
 			if ((Double)o1[field] > (Double)o2[field])
				return -1;
 			else if ((Double)o1[field] == (Double)o2[field])
 				return 0;
 			else
				return 1;
 		}
 
 	}
 
 }
