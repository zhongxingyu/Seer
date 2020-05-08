 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.esbhive.demoapp;
 
 import java.io.File;
 
 /**
  *
  * @author pubudu
  */
 public class ResponseDataCalculator {
 
   private int numESBs, numClients, totalRequests,requestsPerClient;
   private long totalTime, responseTimeSum, throughput, responseTime;
 
   public ResponseDataCalculator(File file) throws InvalidDataException {
     if (!file.isDirectory()) {
       throw new InvalidDataException("The " + file.getAbsolutePath() + " is not a directory.");
     } else {
       String fileName = file.getName();
       String[] parts = fileName.split("\\.");
       numESBs = Integer.parseInt(parts[0].split("-")[1]);
       numClients = Integer.parseInt(parts[1].split("-")[1]);
       requestsPerClient = Integer.parseInt(parts[2].split("-")[1]);
       File[] files = file.listFiles();
       if (files.length != numClients) {
         throw new InvalidDataException("The number of clients do not match the number of files");
       }
       for (int i = 0; i < files.length; i++) {
         ResponseData responseData = new ResponseData(files[i]);
         responseTimeSum += responseData.getResponseTimeSum();
         totalRequests += responseData.getTotalRequests();
	throughput += responseData.getTotalRequests()/(responseData.getTotalTime()/1000000000);
         if (responseData.getRequestsServed() != responseData.getTotalRequests()) {
           throw new InvalidDataException("Some requests have not been served.");
         }
       }
       responseTime = (responseTimeSum / 1000000) / totalRequests;
 
     }
 
   }
 
   public void print() {
     System.out.println("Number of clients : " +this.getNumClients());
     System.out.println("Number of ESBs : " +this.getNumESBs());
     System.out.println("Response time (ms) : " +this.getResponseTime());
     System.out.println("Throughput(per second) : " +this.getThroughput());
     System.out.println("Total Requests : " +this.getTotalRequests());
 
   }
 
   public int getNumClients() {
     return numClients;
   }
 
   public int getNumESBs() {
     return numESBs;
   }
 
   public long getResponseTime() {
     return responseTime;
   }
 
   public int getRequestsPerClient() {
     return requestsPerClient;
   }
 
   public long getThroughput() {
     return throughput;
   }
 
   public int getTotalRequests() {
     return totalRequests;
   }
 }
