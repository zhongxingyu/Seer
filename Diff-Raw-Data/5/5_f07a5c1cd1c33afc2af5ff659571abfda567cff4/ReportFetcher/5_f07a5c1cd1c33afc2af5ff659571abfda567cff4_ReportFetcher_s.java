 package nve;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.*;
 import java.util.ArrayList;
 
 import core.Core;
 
 import models.NVERegion;
 import models.Repository;
 
 public class ReportFetcher {
 
 	private static final String reportUrl = "http://api01.nve.no/hydrology/forecast/avalanche/v1.0.0/api/AvalancheWarningByRegion/Simple/%d/1/";
 											//http://api01.nve.no/hydrology/forecast/avalanche/v1.0.0/api/AvalancheWarningByRegion/Simple/1/1/
 	public void fetchRiskReports(Core core, ArrayList<NVERegion> regions) {
 		// TODO Auto-generated method stub
 		for (NVERegion region : regions) {
 			fetchAndUpdateRiskReport(region);
 		}
 		Repository repo = core.getRepository();
 		repo.updateRegions(regions);
 		core.RiskLevelsChanged();
 	}
 	
 	private void fetchAndUpdateRiskReport(NVERegion region) {
 		String xmlString = fetchReportXML(String.format(ReportFetcher.reportUrl, region.getId()));
 		NVERegion.parseXmlAndUpdateRegion(xmlString, region);
 	}
 	
 	private String fetchReportXML(String urlString){
 		System.out.println(String.format("Attempting to connect to: %s", urlString));
 		HttpURLConnection conn = null;
		// for debugging reasons - keep getting Internal server error on full path
		urlString = "http://api01.nve.no/hydrology/forecast/avalanche/v1.0.0/api/AvalancheWarningByRegion/";
					//http://api01.nve.no/hydrology/forecast/avalanche/v1.0.0/api/AvalancheWarningByRegion/Simple/1/1/
 		String xmlString = null;
 		try {
 			URL url = new URL(urlString);
 			
 			conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			conn.connect();
 			if (conn.getResponseCode() != 200) {
 				throw new IOException(String.format("%d - %s",conn.getResponseCode(),conn.getResponseMessage()));
 			}
 		
 			// Buffer the result into a string
 			BufferedReader rd = new BufferedReader(
 				new InputStreamReader(conn.getInputStream()));
 			StringBuilder sb = new StringBuilder();
 			String line;
 			while ((line = rd.readLine()) != null) {
 				sb.append(line);
 			}
 			rd.close();
 		
 			xmlString = sb.toString();
 		} catch (MalformedURLException e) {
 			System.out.println(String.format("Could not connect to host: %s", e.getMessage()));
 		} catch (IOException e) {
 			System.out.println(String.format("Could not connect to host: %s", e.getMessage()));
 		} finally {
 			if(conn != null) {
 				conn.disconnect();
 			}
 		}
 		return xmlString;
 		
 	}
 
 
 
 }
