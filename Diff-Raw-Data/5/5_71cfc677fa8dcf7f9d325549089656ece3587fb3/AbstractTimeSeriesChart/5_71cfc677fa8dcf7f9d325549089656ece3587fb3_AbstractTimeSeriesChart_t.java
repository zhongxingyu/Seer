 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package de.berlios.statcvs.xml.chart;
 
 import java.awt.Color;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 
 import net.sf.statcvs.model.CvsRevision;
 import net.sf.statcvs.model.SymbolicName;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.XYStepRenderer;
 import org.jfree.data.time.Millisecond;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 
 import de.berlios.statcvs.xml.I18n;
 import de.berlios.statcvs.xml.model.Grouper;
 import de.berlios.statcvs.xml.output.ReportSettings;
 
 /**
  * TimeLineChart
  * 
  * @author Tammo van Lessen
  */
 public class AbstractTimeSeriesChart extends AbstractChart {
 
 	private TimeSeriesCollection tsc;
 	
 	/**
 	 * @param filename
 	 * @param title
 	 */
 	public AbstractTimeSeriesChart(ReportSettings settings, String filename, String title, 
 						String rangeLabel) 
 	{
 		super(settings, filename, title);
 
 		tsc = new TimeSeriesCollection();
 
 		setChart(ChartFactory.createTimeSeriesChart(
 			settings.getProjectName(),
 			I18n.tr("Date"), rangeLabel,
 			tsc, 
 			true,
 			true,
 			false));
 		
 		//Paint[] colors = new Paint[1];
 		//colors[0] = Color.blue;
 		//getChart().getPlot().setSeriesPaint(colors);
 		
 		// setup axis
 		XYPlot plot = getChart().getXYPlot();
 		ValueAxis axis = plot.getDomainAxis();
 		axis.setVerticalTickLabels(true);
 		plot.setRenderer(new XYStepRenderer());
 		
 		// the 4th color is yellow which has almost no contrast to the white 
 		// background color, therefore we use a different color
 		plot.getRenderer().setSeriesPaint(0, Color.red);
 		plot.getRenderer().setSeriesPaint(1, Color.blue);
 		plot.getRenderer().setSeriesPaint(2, Color.green);
 		plot.getRenderer().setSeriesPaint(3, Color.magenta);
 		plot.getRenderer().setSeriesPaint(4, Color.orange);
 		plot.getRenderer().setSeriesPaint(5, Color.cyan);
 		plot.getRenderer().setSeriesPaint(6, Color.pink);	
 	}
 
 	protected void addSymbolicNames(Iterator it) 
 	{
 		XYPlot xyplot = getChart().getXYPlot();
 		while (it.hasNext()) {
 			SymbolicName sn = (SymbolicName)it.next();
 			xyplot.addAnnotation(new SymbolicNameAnnotation(sn));
 		}
 	}
 
 	protected void addTimeSeries(TimeSeries series, Date firstDate, int firstValue)
 	{
 		series.add(new Millisecond(new Date(firstDate.getTime() - 1)), firstValue);
 		tsc.addSeries(series);
 	}
 	
 	protected void addTimeSeries(TimeSeries series)
 	{
 		tsc.addSeries(series);
 	}
 
 	protected int getSeriesCount()
 	{
 		return tsc.getSeriesCount();
 	}
 	
 	protected TimeSeries createTimeSeries(String title, Iterator it, RevisionVisitor visitor) 
 	{
 		DateTimeSeries result = new DateTimeSeries(title);
 
 		while (it.hasNext()) {
 			CvsRevision rev = (CvsRevision)it.next();
 			result.add(rev.getDate(), visitor.visit(rev));
 		}
 		result.addLast();
 
 		return result;
 	}
 
 	protected Map createTimeSeries(Grouper grouper, Iterator it, RevisionVisitorFactory factory) 
 	{
 		Hashtable timeSeriesByGroup = new Hashtable();
 		Hashtable visitorByGroup = new Hashtable();
 
 		ReportSettings.Predicate predicate = getSettings().getOutputPredicate();
 		while (it.hasNext()) {
 			CvsRevision rev = (CvsRevision)it.next();
 			Object group = grouper.getGroup(rev);
 			DateTimeSeries series = (DateTimeSeries)timeSeriesByGroup.get(group);
 			RevisionVisitor visitor = (RevisionVisitor)visitorByGroup.get(group);
 			if (series == null) {
 				series = new DateTimeSeries(grouper.getName(group));
 				timeSeriesByGroup.put(group, series);
 				visitor = factory.create(group);
 				visitorByGroup.put(group, visitor);
 			}
 			
 			int value = visitor.visit(rev);
 			if (predicate == null || predicate.matches(rev)) {
				// dont add binary files
				if (rev.getFile().getCurrentLinesOfCode() != 0) {
					series.add(rev.getDate(), value);	
				}
 			}
 		}
 		
 		for (Iterator it2 = timeSeriesByGroup.values().iterator(); it2.hasNext();) {
 			((DateTimeSeries)it2.next()).addLast();
 		}
 		
 		return timeSeriesByGroup;
 	}
 
 	public boolean hasData()
 	{
 		for (Iterator it = tsc.getSeries().iterator(); it.hasNext();) {
 			TimeSeries series = (TimeSeries)it.next();
 			if (series.getItemCount() <= 1) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public static class DateTimeSeries extends TimeSeries {
 
 		private int currentValue;
 		private Date currentDate = null;
 				
 		public DateTimeSeries(String name)
 		{
 			super(name, Millisecond.class);
 		}
 		
 		public void add(Date date, int value)
 		{
 			if (currentDate == null) {
 				currentDate = date;
 			}
 			else if (!date.equals(currentDate)) {
 				super.add(new Millisecond(currentDate), currentValue);
 				currentDate = date;
 			}
 			currentValue = value;
 		}
 		
 		public void addLast()
 		{
 			if (currentDate != null) {
 				super.add(new Millisecond(currentDate), currentValue);
 			}
 		}
 		
 	}
 
 }
