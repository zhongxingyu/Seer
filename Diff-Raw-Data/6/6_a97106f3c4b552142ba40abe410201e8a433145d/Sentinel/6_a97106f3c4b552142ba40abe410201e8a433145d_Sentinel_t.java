 
 package com.zanoccio.axirassa.webapp.pages;
 
 import java.math.BigDecimal;
 import java.sql.Timestamp;
 import java.util.List;
 
 import org.apache.tapestry5.annotations.Import;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.Request;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 
 import com.zanoccio.axirassa.util.HibernateTools;
 import com.zanoccio.axirassa.webapp.annotations.PublicPage;
 import com.zanoccio.axirassa.webapp.utilities.AxPlotDataPackage;
 import com.zanoccio.axirassa.webapp.utilities.AxPlotDataPackage.AxPlotAxisLabelingFunction;
 import com.zanoccio.axirassa.webapp.utilities.AxPlotDataSet;
 import com.zanoccio.axirassa.webapp.utilities.AxPlotRange;
 import com.zanoccio.javakit.ListUtilities;
 
 @PublicPage
 @Import(library = "${tapestry.scriptaculous}/prototype.js")
 public class Sentinel {
 
	private final static String cpusql = "SELECT CPU, Date, User, System FROM SentinelCPUStats WHERE Machine_ID = 1 ORDER BY CPU ASC, Date ASC";
	private final static String memsql = "SELECT Date, Used, Total FROM SentinelMemoryStats WHERE Machine_ID = 1 ORDER BY Date ASC";
	private final static String disksql = "SELECT Disk, Date, Used, Total FROM SentinelDiskUsageStats WHERE Machine_ID = 1 ORDER BY Disk ASC, Date ASC";
 
 	@Inject
 	private Request request;
 
 
 	public Object onActionFromCpuupdate() {
 		// this codes makes certain assumptions about the ordering from the
 		// database query; in particular that data is grouped by CPU and then by
 		// date.
 
 		if (request != null && !request.isXHR())
 			// cleanly handle non-JS
 			return "Sentinel";
 
 		Session session = HibernateTools.getSession();
 
 		// execute the search query
 		SQLQuery query = session.createSQLQuery(cpusql);
 		List<Object[]> data = query.list();
 		session.close();
 
 		List<List<Object[]>> cpudata = ListUtilities.partition(data, new ListUtilities.ListPartitioner<Object[]>() {
 			@Override
 			public boolean partition(Object[] previous, Object[] current) {
 				return (!previous[0].equals(current[0]));
 			}
 		});
 
 		// build the individual datasets
 		AxPlotDataPackage datapackage = new AxPlotDataPackage();
 
 		AxPlotRange yrange = new AxPlotRange(0, 100);
 		int cpuindex = 0;
 		int index = 0;
 		for (List<Object[]> dataset : cpudata) {
 			index = 0;
 
 			double[] times = new double[dataset.size()];
 
 			// for storing <user,system> tuples:
 			double[][] rawdata = new double[dataset.size()][2];
 
 			for (Object[] row : dataset) {
 				Timestamp time = (Timestamp) row[1];
 				long realtime = time.getTime();
 				double user = 100 * ((BigDecimal) row[2]).doubleValue();
 				double system = 100 * ((BigDecimal) row[3]).doubleValue();
 
 				times[index] = realtime;
 				rawdata[index][0] = user;
 				rawdata[index][1] = system;
 
 				index++;
 			}
 
 			AxPlotDataSet currentdataset = new AxPlotDataSet();
 			currentdataset.setLabel("CPU " + cpuindex);
 			currentdataset.setData(times, rawdata);
 			currentdataset.setYRange(yrange);
 
 			datapackage.addDataSet(currentdataset);
 
 			cpuindex++;
 		}
 
 		datapackage.setAggregatedYAxisRange(new AxPlotRange(0, cpuindex * 100));
 		datapackage.setYAxisLabelingFunction(AxPlotAxisLabelingFunction.PERCENT);
 		datapackage.setLabelDataSets(false);
 
 		datapackage.setDataFormat("%2.1f");
 
 		return datapackage.toJSON();
 	}
 
 
 	public Object onActionFromMemupdate() {
 		if (!request.isXHR())
 			return "Sentinel";
 
 		Session session = HibernateTools.getSession();
 
 		// session.beginTransaction();
 		SQLQuery query = session.createSQLQuery(memsql);
 		List<Object[]> result = query.list();
 		session.close();
 
 		double[] timestamps = new double[result.size()];
 		double[] dataset = new double[result.size()];
 
 		long maxmemory = 0;
 		int i = 0;
 		for (Object[] row : result) {
 			Timestamp time = (Timestamp) row[0];
 			long used = Long.parseLong((String) row[1]);
 			long total = Long.parseLong((String) row[2]);
 
 			if (total > maxmemory)
 				maxmemory = total;
 
 			timestamps[i] = time.getTime();
 			dataset[i] = used;
 
 			i++;
 		}
 
 		AxPlotDataSet memdata = new AxPlotDataSet();
 		memdata.setData(timestamps, dataset);
 		memdata.setLabel("Memory");
 
 		AxPlotDataPackage plotdata = new AxPlotDataPackage();
 		plotdata.addDataSet(memdata);
 		plotdata.setAggregatedYAxisRange(new AxPlotRange(0, maxmemory));
 		plotdata.setYAxisLabelingFunction(AxPlotAxisLabelingFunction.DATA);
 
 		return plotdata.toJSON();
 	}
 
 
 	public Object onActionFromDiskSpaceUpdate() {
 		Session session = HibernateTools.getSession();
 
 		// execute the search query
 		// session.beginTransaction();
 		SQLQuery query = session.createSQLQuery(disksql);
 		List<Object[]> data = query.list();
 		session.close();
 
 		List<List<Object[]>> diskdata = ListUtilities.partition(data, new ListUtilities.ListPartitioner<Object[]>() {
 			@Override
 			public boolean partition(Object[] previous, Object[] current) {
 				return (!previous[0].equals(current[0]));
 			}
 		});
 
 		// build per-disk datasets
 		AxPlotDataPackage datapackage = new AxPlotDataPackage();
 		int index = 0;
 		double totalmemory = 0;
 		for (List<Object[]> dataset : diskdata) {
 			index = 0;
 			double maxsize = 0;
 			double[] times = new double[dataset.size()];
 			double[] rawdata = new double[dataset.size()];
 
 			AxPlotDataSet currentdataset = new AxPlotDataSet();
 
 			for (Object[] row : dataset) {
 				// pull data
 				String disk = (String) row[0];
 				Timestamp time = (Timestamp) row[1];
 				long realtime = time.getTime();
 				long used = Long.parseLong((String) row[2]);
 				long total = Long.parseLong((String) row[3]);
 
 				if (currentdataset.getLabel() == null)
 					currentdataset.setLabel("Disk - " + disk);
 
 				times[index] = realtime;
 				rawdata[index] = used;
 
 				if (maxsize < total)
 					maxsize = total;
 
 				index++;
 			}
 
 			totalmemory += maxsize;
 
 			currentdataset.setData(times, rawdata);
 			currentdataset.setYRange(0.0, maxsize);
 
 			datapackage.addDataSet(currentdataset);
 		}
 
 		datapackage.setDataFormat("%3.01f");
 
 		datapackage.setLabelDataSets(true);
 		datapackage.setAggregatedYAxisRange(new AxPlotRange(0.0, totalmemory));
 		datapackage.setYAxisLabelingFunction(AxPlotAxisLabelingFunction.DATA);
 		return datapackage.toJSON();
 	}
 }
