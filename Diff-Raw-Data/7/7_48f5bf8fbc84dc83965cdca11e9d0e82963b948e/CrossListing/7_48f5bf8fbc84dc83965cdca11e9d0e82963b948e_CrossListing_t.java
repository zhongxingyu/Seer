 package edu.rpi.rocs.client.objectmodel;
 
 import java.util.ArrayList;
 
 /**
  * CrossListing class which stores information about multiple classes if they 
  * are crosslisted in SIS.
  * 
  * @author ewpatton
  *
  */
 public class CrossListing extends MajorMinorRevisionObject {
 	ArrayList<Integer> crns;
 	int numberOfSeats;
 	int uid;
 	
 	/**
 	 * Default constructor
 	 */
 	public CrossListing() {
 		crns = new ArrayList<Integer>();
 	}
 	
 	/**
 	 * Adds a CRN to a cross listing.
 	 * 
 	 * @param crn The CRN to be added.
 	 */
 	public void addCRNToCrossListing(int crn) {
 		crns.add(new Integer(crn));
 		updateMajorRevision();
 	}
 	
 	/**
 	 * Gets the list of CRNs for this cross listing.
 	 * 
 	 * @return An array of CRNs
 	 */
 	public ArrayList<Integer> getCRNs() {
 		return crns;
 	}
 	
 	/**
 	 * Remove a CRN from a cross listing.
 	 * 
 	 * @param crn The CRN to be removed.
 	 */
 	public void removeCRNFromCrossListing(int crn) {
 		crns.remove(new Integer(crn));
 		updateMajorRevision();
 	}
 	
 	/**
 	 * Sets the number of seats in the crosslisting
 	 * 
 	 * @param seats Total number of seats
 	 */
 	public void setNumberOfSeats(int seats) {
 		numberOfSeats = seats;
 		updateMinorRevision();
 	}
 	
 	/**
 	 * Sets the number of seats in the crosslisting
 	 * 
 	 * @param seats Total number of seats
 	 */
 	public void setNumberOfSeats(Integer seats) {
 		numberOfSeats = seats;
 		updateMinorRevision();
 	}
 	
 	/**
 	 * Gets the number of seats in this crosslisting
 	 * 
 	 * @return Total number of seats
 	 */
 	public int getNumberOfSeats() {
 		return numberOfSeats;
 	}
 	
 	public int getUID() {
 		return uid;
 	}
 	
 	public void setUID(int id) {
 		uid = id;
 	}
 }
