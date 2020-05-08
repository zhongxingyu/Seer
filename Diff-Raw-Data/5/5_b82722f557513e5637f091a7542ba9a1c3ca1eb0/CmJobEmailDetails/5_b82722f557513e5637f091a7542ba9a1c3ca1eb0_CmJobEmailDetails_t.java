 package org.sgs.controlm;
 
 import java.util.Collection;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class CmJobEmailDetails implements Comparable<CmJobEmailDetails>{
 	
 	private String cmName;
 	private String rhubarbName;
 	private Set<String> jobSuccessEmails;
 	private Set<String> jobFailureEmails;
 	
 	public CmJobEmailDetails(String cmName) {
 		this.cmName = cmName;
 		this.jobSuccessEmails = new TreeSet<String>();
 		this.jobFailureEmails = new TreeSet<String>();
 	}
 
 	public String getCmName() {
 		return cmName;
 	}
 
 	public String getRhubarbName() {
 		return rhubarbName;
 	}
 
 	public void setRhubarbName(String rhubarbName) {
 		this.rhubarbName = rhubarbName;
 	}
 
 	public void addToSuccessEmails(String s) {
 		jobSuccessEmails.add(s);
 	}
 	
 	public void addAllToSuccessEmails(Collection<String> c) {
 		jobSuccessEmails.addAll(c);
 	}
 	
 	public void addToFailureEmails(String s) {
		jobFailureEmails.add(s);
 	}
 	
 	public void addAllToFailureEmails(Collection<String> c) {
		jobFailureEmails.addAll(c);
 	}
 	
 	public Set<String> getJobSuccessEmails() {
 		return jobSuccessEmails;
 	}
 
 	public Set<String> getJobFailureEmails() {
 		return jobFailureEmails;
 	}
 	
 	@Override
 	public int compareTo(CmJobEmailDetails otherDetail) {
 		return getCmName().compareTo(otherDetail.getCmName());
 	}
 
 }
