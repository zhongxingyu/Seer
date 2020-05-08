 package ca.cumulonimbus.barometer;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 
 // Interface with Google's Charting API
 public class ChartData {
 	private String chartType;
 	private String chartCaption;
 	private String[] columns = {"Pressure"}; 
 	private ArrayList<RowInfo> rows = new ArrayList<RowInfo>();
	private static final int MAX = 200;
 	
 	
 	public void addRow(double reading, long time) {
 		rows.add(new RowInfo(reading, time));
 	}
 	
 	public String getChartWebPage() {
 		// Start everything off.
 		String fullWebPage = "<html><head><script type='text/javascript' src='https://www.google.com/jsapi'></script>" + 
 							 "<script type='text/javascript'>google.load('visualization', '1', {packages:['imagechart']});</script><script type='text/javascript'>" + 
 							 "function drawChart() { var data = new google.visualization.DataTable();";
 		// Add one column. More are allowed.
 		fullWebPage += "data.addColumn('number');"; // X axis
 		fullWebPage += "data.addColumn('number');"; // Y axis
 		//fullWebPage += "data.addColumn('string');"; // Y axis
 		int numRows = this.getRows().size();
 		// fullWebPage += "data.addRows(" + numRows + ");";
 		// Print out row information
 		int i = 0;
 		long minTime = 31;
 		long maxTime = 0;
 		double minReading = 1500;
 		double maxReading = 0;
 		String rowAddString = "[";
 		Calendar now = Calendar.getInstance();
 		ArrayList<RowInfo> rows = this.getRows();
 		Collections.shuffle(rows);
 		boolean limitHit = false; // keep checking for min and max but stop adding values
 		for(RowInfo ri : rows) {
 			i++;
 			Date date = new Date(ri.getTime());
 			
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(date);
 			int dateInt = cal.get(Calendar.DAY_OF_MONTH);
 			int hourInt = cal.get(Calendar.HOUR_OF_DAY);
 
 			
 			double fraction = hourInt / 24.0;
 			NumberFormat nf = new DecimalFormat("##.##");
 			double timeToCalc = (dateInt + fraction); 
 			String plot = nf.format(timeToCalc) + "";
 
 			// Chart wraparound to last month HACK
 			// reject the wrap, should instead wrap nicely
 			int limit = 7;
 			if(dateInt>now.get(Calendar.DAY_OF_MONTH)) {
 				i--;
 				continue;
 			}
 			if(dateInt<now.get(Calendar.DAY_OF_MONTH) - limit) {
 				i--;
 				continue;
 			}
 			
 			// set the edges of the chart
 			if(dateInt < minTime) {
 				minTime = dateInt;
 			}
 			if(dateInt > maxTime) {
 				maxTime = dateInt + 2; // + 2 for fractions of the next day
 			}
 			if(ri.getReading() < minReading) {
 				minReading = ri.getReading();
 			}
 			if(ri.getReading() > maxReading) {
 				maxReading = ri.getReading();
 			}
 			
 			String printReading = nf.format(ri.getReading());
 			
 			if(!limitHit) {
 				rowAddString += "[" + plot +"," + printReading + "],";
 			}
 			
 
 			// Temporary limit to test for performance improvement.
 			// If it speeds up, move this step to the sql query
 			if(i>MAX) {
 				limitHit = true;
 			}
 			
 		}
 		// remove trailing comma and end with bracket
 		if(rowAddString.endsWith(",")) {
 			rowAddString = rowAddString.substring(0, rowAddString.length() - 1);
 		}
 		rowAddString +=  "]";
 		fullWebPage += "data.addRows(" + rowAddString + ");";
 		fullWebPage += "var options = {chxs:'0,333333|1,333333', chco:'33b5e5', chdls:'ffffff,12', chf:'bg,s,ffffff', cht: 's', width: 400, height:250, chds:'" + minTime + "," + maxTime + "," + minReading + "," + maxReading + "'};";
 		
 		
 		fullWebPage += "var chart = new google.visualization.ImageChart(document.getElementById('chart_div'));" + 
 			    //"chart.draw(data, {width: 800, height: 480,title: '" + chartCaption + "', hAxis: {title: '" + columns[0] + "', minValue: " + minTime + ", maxValue: " + maxTime + "}," +
                 //"vAxis: {title: '" + columns[1] + "', minValue: " + minReading + ", maxValue: " + maxReading + "}, legend: 'none'}); } </script></head><body> <div id='chart_div'></div></body></html>";
 			    "chart.draw(data,options); }google.setOnLoadCallback(drawChart); </script></head><body bgcolor='#ffffff'> <div id='chart_div'></div><div id='other'></div><p style='color:#000000;a:#336699'>";
 		
 		return fullWebPage;
 	}
 	
 	public String getChartType() {
 		return chartType;
 	}
 
 	public void setChartType(String chartType) {
 		this.chartType = chartType;
 	}
 
 	public String getChartCaption() {
 		return chartCaption;
 	}
 
 	public void setChartCaption(String chartCaption) {
 		this.chartCaption = chartCaption;
 	}
 
 	public ArrayList<RowInfo> getRows() {
 		return rows;
 	}
 
 	public void setRows(ArrayList<RowInfo> rows) {
 		this.rows = rows;
 	}
 
 	private class RowInfo implements Comparable<RowInfo> {
 		double reading;
 		long time;
 		
 		public RowInfo(double reading, long time) {
 			this.reading = reading;
 			this.time = time;
 		}
 		
 		public double getReading() {
 			return reading;
 		}
 		public void setReading(double reading) {
 			this.reading = reading;
 		}
 		public long getTime() {
 			return time;
 		}
 		public void setTime(long time) {
 			this.time = time;
 		}
 
 		@Override
 		public int compareTo(RowInfo other) {
 			return (int)(other.getTime() - this.getTime());
 		}
 
 	}
 	
 	public ChartData(String caption) {
 		this.chartCaption = caption;
 	}
 }
