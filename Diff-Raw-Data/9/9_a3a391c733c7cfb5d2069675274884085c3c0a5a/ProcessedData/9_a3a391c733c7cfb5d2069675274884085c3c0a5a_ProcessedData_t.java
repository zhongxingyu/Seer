 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller.terminal.controller;
 
 import controller.terminal.controller.data.RentSummary;
 import controller.terminal.controller.data.ReturnSummary;
 import java.util.ArrayList;
 import model.database.TerminalMapper;
 import model.object.Payment;
 import model.object.Terminal;
 
 /**
  *
  * @author Valentin SEITZ
  */
 class ProcessedData {
 
 	private static Terminal terminal;
 	private static int anonymousUserId;
 	private static RentSummary rentSummary = new RentSummary();
 	private static ReturnSummary returnSummary;
 	private static ArrayList<Integer> idBikeUsagesToResetEndDate = new ArrayList<>();
 	private static ArrayList<Integer> idBikeUsagesToDelete = new ArrayList<>();
 	private static ArrayList<Integer> idReturnAmountToDelete = new ArrayList<>();
 	private static ArrayList<Payment> paymentToProcess = new ArrayList<>();
 
 	public static ArrayList<Payment> getPaymentToProcess() {
 		return paymentToProcess;
 	}
 
 	public static void setPaymentToProcess(ArrayList<Payment> paymentToProcess) {
 		ProcessedData.paymentToProcess = paymentToProcess;
 	}
 
 	public static ArrayList<Integer> getIdReturnAmountToDelete() {
 		return idReturnAmountToDelete;
 	}
 
 	public static void setIdReturnAmountToDelete(ArrayList<Integer> idReturnAmountToDelete) {
 		ProcessedData.idReturnAmountToDelete = idReturnAmountToDelete;
 	}
 
 	public static ArrayList<Integer> getIdBikeUsagesToResetEndDate() {
 		return idBikeUsagesToResetEndDate;
 	}
 
 	public static void setIdBikeUsagesToResetEndDate(ArrayList<Integer> BikeUsagesToResetEndDate) {
 		ProcessedData.idBikeUsagesToResetEndDate = BikeUsagesToResetEndDate;
 	}
 
 	public static ArrayList<Integer> getIdBikeUsagesToDelete() {
 		return idBikeUsagesToDelete;
 	}
 
 	public static void setIdBikeUsagesToDelete(ArrayList<Integer> BikeUsagesToDelete) {
 		ProcessedData.idBikeUsagesToDelete = BikeUsagesToDelete;
 	}
 
 	public static int getAnonymousUserId() {
 		return anonymousUserId;
 	}
 
 	public static void setAnonymousUserId(int anonymousUserId) {
 		ProcessedData.anonymousUserId = anonymousUserId;
 	}
 
 	public static RentSummary getRentSummary() {
 		return rentSummary;
 	}
 
 	public static Terminal getTerminal() {
 		if (ProcessedData.terminal == null) {
 			TerminalMapper terminalMapper = new TerminalMapper();
			terminal = terminalMapper.getTerminal(1);
 		}
 		return ProcessedData.terminal;
 	}
 
 	public static ReturnSummary getReturnSummary() {
 		return returnSummary;
 	}
 
 	public static void setReturnSummary(ReturnSummary rentSummary) {
 		ProcessedData.returnSummary = rentSummary;
 	}
 
 	public static void doResetData() {
 		ProcessedData.idBikeUsagesToResetEndDate = new ArrayList<>();
 		ProcessedData.idBikeUsagesToDelete = new ArrayList<>();
 		ProcessedData.rentSummary = new RentSummary();
 		ProcessedData.returnSummary = null;
 		ProcessedData.idReturnAmountToDelete = new ArrayList<>();
 		ProcessedData.paymentToProcess = new ArrayList<>();
 	}
 }
