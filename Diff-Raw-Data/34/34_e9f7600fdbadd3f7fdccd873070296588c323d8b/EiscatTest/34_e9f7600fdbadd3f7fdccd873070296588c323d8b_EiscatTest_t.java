 package org.iugonet.www;
 
 import org.jfree.data.time.TimeSeries;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class EiscatTest {
 
 	Eiscat eiscat;
 	String url;
 	String filepath;
 
 	@Before
 	public void setUp() throws Exception {
 		eiscat = new Eiscat();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void test() {
 		url = "http://www.stelab.nagoya-u.ac.jp";
 		filepath = "/~eiscat/data/ESR_anadata/BasicParameters/ant/20110104_32ma_taro_0000.dat";
 
		/*
 		eiscat.file_http_copy(url + filepath);
 		eiscat.readData(filepath);
 		TimeSeries[] timeSeries = eiscat.getTimeSeries();
 
 		for (int i = 0; i < timeSeries[7].getItemCount(); i++) {
 			System.out.print(timeSeries[7].getTimePeriod(i).getStart() + " ");
 			System.out.println(timeSeries[7].getDataItem(i).getValue());
 		}
		*/
 	}
 
 }
