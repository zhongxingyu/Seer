 package export;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import reporting.FunctionCallReport;
 import reporting.Scorable;
 
 public class FunctioncallHtmlExporter {
 	private static int BAR_TOTAL_WIDTH = 200;
 	private List<FunctionCallReport> reports;
 	private double[] thresholds;
 	
 	public FunctioncallHtmlExporter(List<FunctionCallReport> reports) {
 		this.reports = reports;
 		Collections.sort(reports, Collections.reverseOrder());
 		double[] percentiles = {0.05,0.35,0.65,0.95};
 		this.thresholds = Scorable.getScores(percentiles, reports);
 	}
 	
 	public void export(String filename, String projectName) {
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
 		
 			out.write("<!doctype html>");
 			out.write("<html><head><title>");
 			out.write(projectName);
 			out.write(" analyzability</title><style type=\"text/css\"></style></head><body><h1>");
 			out.write(projectName);
 			out.write(" analyzability</h1>");
 			out.write("<table border=0><tr><th>Module</th><th>Function call tracability</th><th>Stars</th></tr>");
 			
 			FunctionCallReport totals = new FunctionCallReport("total");
 			for(FunctionCallReport report : this.reports) {
 				this.exportDetails(out, report);
 				
 				totals.add(report);
 			}
 			this.exportDetails(out, totals, true);
 			out.write("</table></body></html>");
 			out.flush();
 			out.close();
 			
 		} catch (IOException iox) {
 			iox.printStackTrace();
 		}
 	}
 	
 	private void exportDetails(BufferedWriter out, FunctionCallReport report) throws IOException {
 		this.exportDetails(out, report, false);
 	}
 	
 	private void exportDetails(BufferedWriter out, FunctionCallReport report, boolean isSummary) throws IOException {
 		if(report.getId().indexOf("thirdparty") == 0) {
 			return;
 		}
 		
 		out.write("<tr><td>");
 		if(isSummary) out.write("<strong>");
 		out.write(report.getId());
 		if(isSummary) out.write("</strong>");
 		out.write("</td>");
 		
 		printBar(out, report);
 		
 		out.write("<td>");
 		if(!isSummary) {
 			out.write('*');
 			for(double threshold : this.thresholds)
 				if(report.getScore() >= threshold)
 					out.write('*');
 		}
 		out.write("</td>");
 		
 		out.write("</tr>");
 	}
 
 	private void printBar(BufferedWriter out, FunctionCallReport report) throws IOException {
 		int total = report.getTotal();
 		int good = report.numLocalFunctionCalls + report.numRequireFunctionCalls;
 		int medium = report.numRequireWrongFunctionCalls;
 		int bad = report.numUnknownFunctionCalls;
 		
 		int greenWidth = BAR_TOTAL_WIDTH;
 		int yellowWidth = 0;
 		if(total > 0) {
 			greenWidth = (BAR_TOTAL_WIDTH * good) / total;
 			yellowWidth = (BAR_TOTAL_WIDTH * medium) / total;
 		}
 		out.write("<td><img src=\"green.gif\" height=10 width=" + greenWidth + ">");
 		out.write("<img src=\"yellow.gif\" height=10 width=" + (yellowWidth) + ">");
 		out.write("<img src=\"red.gif\" height=10 width=" + ((BAR_TOTAL_WIDTH - yellowWidth) - greenWidth) + ">");
 		out.write(good + " / " + medium + " / " + bad);
 		out.write("</td>");
 	}
 	
 }
