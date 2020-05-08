 package pt.ist.expenditureTrackingSystem.domain.dto;
 
 import java.io.Serializable;
 
 public class Issue implements Serializable {
 
     public static enum IssueTypeLevel {
 	ERROR, WARNING;
     }
 
     public static enum IssueType {
	EMPTY_LINE(IssueTypeLevel.WARNING),
	WRONG_NUMBER_LINE_COLUMNS(IssueTypeLevel.ERROR),
	SUPPLIER_DOES_NOT_EXIST(IssueTypeLevel.ERROR),
	BAD_MONEY_VALUE_FORMAT(IssueTypeLevel.ERROR),
	BAD_VAT_VALUE_FORMAT(IssueTypeLevel.ERROR),
	CANNOT_ALLOCATE_MONEY_TO_SUPPLIER(IssueTypeLevel.ERROR);
 
 	private IssueTypeLevel issueTypeLevel;
 
 	private IssueType(final IssueTypeLevel issueTypeLevel) {
 	    this.issueTypeLevel = issueTypeLevel;
 	}
 
 	public IssueTypeLevel getIssueTypeLevel() {
 	    return issueTypeLevel;
 	}
     }
 
     private final IssueType issueType;
     private final int lineNumber;
     private final String[] messageArgs;
 
     public Issue(final IssueType issueType, final int lineNumber, final String... messageArgs) {
 	this.issueType = issueType;
 	this.lineNumber = lineNumber;
 	this.messageArgs = messageArgs;
     }
 
     public int getLineNumber() {
 	return lineNumber;
     }
 
     public IssueType getIssueType() {
 	return issueType;
     }
 
     public String[] getMessageArgs() {
 	return messageArgs;
     }
 
 }
