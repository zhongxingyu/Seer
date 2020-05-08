 package com.sff.report_performance;
 
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 @SuppressWarnings("rawtypes")
 public class GenerateReportAction extends AbstractAction{
 	private static final long serialVersionUID = 1L;
 	private List<List> customerData;
 	private List<List> projectData;
 	private List<List> statusData;
 	private JFrame frame;
 	
 	public GenerateReportAction(List<List> customerData, List<List> projectData, List<List> statusData, JFrame frame){
 		this.customerData = customerData;
 		this.projectData = projectData;
 		this.statusData = statusData;
 		this.frame = frame;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		boolean isFileUnlocked = false;
 		File output = null;
 		String directory = "C:/vendorLogistics/Logistics/vendorUser/";
 		String fileName = "report.xlsx";
 		output = new File(directory + fileName);
 		isFileUnlocked = true;
 		if(isFileUnlocked){
 			ProgressDialog.runReport(customerData, projectData, statusData, output, frame);
 		}
 		else{
 			JOptionPane.showMessageDialog(frame, "Please close file " + fileName + " before generating a new report");
 		}
 	}
 }
