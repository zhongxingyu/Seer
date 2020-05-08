 package nl.tudelft.cloud_computing_project.experimental;
 
 import java.util.Scanner;
 
 import org.sql2o.Connection;
 
 import nl.tudelft.cloud_computing_project.model.Database;
 
 public class EmptyDatabase {
 
 	private static final String truncate_assignments = "TRUNCATE Table Assignment;";
 	private static final String truncate_jobs = "DELETE FROM Job;";
 	
	public static void main(String[] args) {
 		System.out.println("THIS WILL DELETE EVERY JOB AND ASSIGNMENT, ARE YOU SURE (y/N):");
 		Scanner s = new Scanner(System.in);
 		String input = s.next();
 		if(!input.equals("y") && !input.equals("Y")) {
 			System.out.println("Not doing a thing");
 			System.exit(0);
 		}
 		
 		System.out.println("Bye bye data");
 		
 		Connection c = Database.getConnection().beginTransaction();
 		try {
 			c.createQuery(truncate_assignments).executeUpdate();
 			c.createQuery(truncate_jobs).executeUpdate();
 			
 			c.commit();
 		} catch(Exception e) {
 			c.rollback();
 			throw e;
 		}
 		
 		System.out.println("Deleted everything");
 	}
 
 }
