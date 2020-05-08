 package org.iugonet.www;
 
 import org.jfree.data.time.TimeSeries;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ImagNiprTest {
 	ImagNipr imagNipr;
 	String url;
 	String filepath;
 
 	@Before
 	public void setUp() throws Exception {
 		imagNipr = new ImagNipr();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void test() {
 		url = "http://iugonet0.nipr.ac.jp";
 		filepath = "/data/imag/syo/20hz/2003/nipr_20hz_imag_syo_20030204_v01.cdf";
		/*
 		imagNipr.file_http_copy(url + filepath);
 		imagNipr.readData(filepath);
		*/
 		TimeSeries[] timeSeries = imagNipr.getTimeSeries();
 		for (int i = 0; i < timeSeries[0].getItemCount(); i++) {
 			System.out.print(timeSeries[0].getTimePeriod(i).getStart() + " ");
 			System.out.println(timeSeries[0].getDataItem(i).getValue());
 		}
 	}
 
 }
