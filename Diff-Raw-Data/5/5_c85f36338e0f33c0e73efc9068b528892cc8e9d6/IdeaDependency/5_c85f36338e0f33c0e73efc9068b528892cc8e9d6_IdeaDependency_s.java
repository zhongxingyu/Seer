 package com.isocraft.thesis;
 
 public class IdeaDependency {
 
 	private IdeaDependencyType t;
 	private String a;
 	private String b;
 	private String c;
 	
 	public enum IdeaDependencyType {
 		Achievement(),
 		Theorem();
 	}
 	
 	/**
 	 * Dependency for idea creation from theorem - achievement
 	 * @param type Achievement or Theorem
 	 * @param arg name of achievement OR thesis-theorem references
 	 */
 	public IdeaDependency(String achievement) {
 		this.t = IdeaDependencyType.Achievement;
 		this.a = achievement;
 		this.b = "*";
		this.c = "8";
 	}
 	
 	/**
 	 * Dependency for idea creation from theorem - achievement
 	 * @param type Achievement or Theorem
 	 * @param arg name of achievement OR thesis-theorem references
 	 */
 	public IdeaDependency(String thesis, String theorem) {
 		this.t = IdeaDependencyType.Achievement;
		this.a = "8";
 		this.b = thesis;
 		this.c = theorem;
 	}
 	
 	/**
 	 * @return Dependency Type
 	 */
 	public IdeaDependencyType getType(){
 		return this.t;
 	}
 	
 	/**
 	 * @return Achievement Name
 	 */
 	public String getAchievement(){
 		return this.a;
 	}
 	
 	/**
 	 * @return Thesis Reference
 	 */
 	public String getThesis(){
 		return this.b;
 	}
 	
 	/**
 	 * @return Theorem Reference
 	 */
 	public String getTheorem(){
 		return this.c;
 	}
 }
 
