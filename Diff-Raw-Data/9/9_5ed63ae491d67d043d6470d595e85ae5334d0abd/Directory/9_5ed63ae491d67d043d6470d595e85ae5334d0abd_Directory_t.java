 package org.bestgrid.virtscreen.model.gold;
 
 import org.apache.commons.lang.StringUtils;
 
 public class Directory extends AbstractGoldParameter {
 
 	private String newLine;
 
 	@Override
 	protected boolean configLineIsValid() {
 		return true;
 	}
 
 	@Override
 	protected String getNewConfigLine() {
 		return newLine;
 	}
 
 	@Override
 	public String getParameterName() {
 		return "directory";
 	}
 
 	@Override
 	public void initParameter() {
 		setNewValue("./Results");
//		setNewValue("/tmp/Results");
 		addMessage("static",
 				"  -> Set output directory to be named \"Results\"\n");
 	}
 
 	@Override
 	boolean isOptional() {
 		return false;
 	}
 
 	@Override
 	boolean isResponsibleForLine(String line) {
 		return hasKey(line, getParameterName());
 	}
 
 	@Override
 	protected void setNewConfigValue(String value) {
 		if (StringUtils.isBlank(value)) {
 			throw new IllegalArgumentException(
 					"New value for concatenated_output is blank");
 		}
 
 		newLine = replaceValue(getOriginalLine(), value);
 	}
 
 }
