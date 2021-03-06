 /*
  * Created on Nov 4, 2004
  */
 package net.sourceforge.fullsync.impl;
 
 import net.sourceforge.fullsync.DataParseException;
 import net.sourceforge.fullsync.Location;
 import net.sourceforge.fullsync.RuleSet;
 import net.sourceforge.fullsync.State;
 import net.sourceforge.fullsync.fs.File;
 import net.sourceforge.fullsync.fs.FileAttributes;
 import net.sourceforge.fullsync.rules.PatternRule;
 
 /**
  * @author Michele Aiello
  */
 public class SimplyfiedSyncRules implements RuleSet {
 	
 	private String name;
 		
 	private boolean isUsingRecursion = true;
 	
 	private int applyingDeletion = Location.None;
 	
 	private String ignorePattern;
 	private PatternRule ignoreRule;
 	
 	private String takePattern;
 	private PatternRule takeRule;
 	
 	/**
 	 * Default Constructor
 	 */
 	public SimplyfiedSyncRules() {
 	}
 
 	/**
 	 * Constructor
 	 */
 	public SimplyfiedSyncRules(String name) {
 		this.name = name;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * @param name The name to set.
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#isUsingRecursion()
 	 */
 	public boolean isUsingRecursion() {
 		return isUsingRecursion;
 	}
 	
 	public void setUsingRecursion(boolean usingRecursion) {
 		this.isUsingRecursion = usingRecursion;
 	}
 	
 	public void setIgnorePattern(String pattern) {
 		this.ignorePattern = pattern;
 		
 		if ((ignorePattern == null) || (ignorePattern.equals(""))) {
 			this.ignoreRule = null;
 		}
 		else {
 			ignoreRule = new PatternRule(ignorePattern);
 		}
 	}
 	
 	public void setTakePattern(String pattern) {
 		this.takePattern = pattern;
 		
 		if ((takePattern == null) || (takePattern.equals(""))) {
 			this.takeRule = null;
 		}
 		else {
 			takeRule = new PatternRule(takePattern);
 		}		
 	}
 	
 	/**
 	 * @return Returns the ignorePattern.
 	 */
 	public String getIgnorePattern() {
 		return ignorePattern;
 	}
 
 	/**
 	 * @return Returns the takePattern.
 	 */
 	public String getTakePattern() {
 		return takePattern;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#isUsingRecursionOnIgnore()
 	 */
 	public boolean isUsingRecursionOnIgnore() {
 		return false;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#isJustLogging()
 	 */
 	public boolean isJustLogging() {
 		return false;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.IgnoreDecider#isNodeIgnored(net.sourceforge.fullsync.fs.File)
 	 */
 	public boolean isNodeIgnored(File node) {
 		boolean take = true;
 		
 		if (take) {
 			if (ignoreRule != null) {
 				take = !ignoreRule.accepts(node);
 			}
 		}
 		
 		if (!take) {
 			if (takeRule != null) {
 				take = takeRule.accepts(node);
 			}
 		}
 		
 		return !take;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.FileComparer#compareFiles(net.sourceforge.fullsync.fs.FileAttributes, net.sourceforge.fullsync.fs.FileAttributes)
 	 */
 	public State compareFiles(FileAttributes src, FileAttributes dst)
 			throws DataParseException 
 	{
 		if (src.getLength() != dst.getLength()) {
 			return new State(State.FileChange, Location.None);
 		}
 
		if (Math.round(src.getLastModified()/1000.0) > Math.round(dst.getLastModified()/1000.0)) {
 			return new State(State.FileChange, Location.Source);
		} else if (src.getLastModified() < dst.getLastModified()){
 		    return new State(State.FileChange, Location.Destination);
 		}
 
 		return new State(State.NodeInSync, Location.Both);
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#createChild(net.sourceforge.fullsync.fs.File, net.sourceforge.fullsync.fs.File)
 	 */
 	public RuleSet createChild(File src, File dst) 
 	{
 	    // TODO even simple sync rules should allow override rules
 		return this;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#isApplyingDeletion(int)
 	 */
 	public boolean isApplyingDeletion(int location) {
 		return (applyingDeletion & location) > 0;
 	}
 	
 	/**
 	 * @param applyingDeletion The applyingDeletion to set.
 	 */
 	public void setApplyingDeletion(int applyingDeletion) {
 		this.applyingDeletion = applyingDeletion;
 	}
 	
 	/**
 	 * @return Returns the applyingDeletion.
 	 */
 	public int getApplyingDeletion() {
 		return applyingDeletion;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#isCheckingBufferAlways(int)
 	 */
 	public boolean isCheckingBufferAlways(int location) {
 		return false;
 	}
 	
 	/**
 	 * @see net.sourceforge.fullsync.RuleSet#isCheckingBufferOnReplace(int)
 	 */
 	public boolean isCheckingBufferOnReplace(int location) {
 		return false;
 	}
 }
