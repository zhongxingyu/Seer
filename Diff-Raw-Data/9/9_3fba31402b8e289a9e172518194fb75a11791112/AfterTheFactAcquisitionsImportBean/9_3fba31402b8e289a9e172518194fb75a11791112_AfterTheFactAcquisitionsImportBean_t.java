 package pt.ist.expenditureTrackingSystem.domain.dto;
 
 import java.io.Serializable;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 
 import pt.ist.expenditureTrackingSystem.domain.DomainException;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionProcess;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.AfterTheFactAcquisitionType;
 import pt.ist.expenditureTrackingSystem.domain.acquisitions.afterthefact.ImportFile;
 import pt.ist.expenditureTrackingSystem.domain.dto.Issue.IssueType;
 import pt.ist.expenditureTrackingSystem.domain.dto.Issue.IssueTypeLevel;
 import pt.ist.expenditureTrackingSystem.domain.organization.Supplier;
 import pt.ist.expenditureTrackingSystem.domain.util.Money;
 import pt.ist.expenditureTrackingSystem.presentationTier.util.FileUploadBean;
 import pt.ist.fenixWebFramework.services.Service;
 import pt.utl.ist.fenix.tools.util.StringNormalizer;
 
 public class AfterTheFactAcquisitionsImportBean extends FileUploadBean implements Serializable {
 
     private AfterTheFactAcquisitionType afterTheFactAcquisitionType;
 
     private String contents;
 
     private List<Issue> issues = new ArrayList<Issue>();
 
     private int importedLines = 0;
     private int errorCount = 0;
     private int warningCount = 0;
 
     private boolean createData;
 
     public AfterTheFactAcquisitionsImportBean() {
     }
 
     public AfterTheFactAcquisitionType getAfterTheFactAcquisitionType() {
 	return afterTheFactAcquisitionType;
     }
 
     public void setAfterTheFactAcquisitionType(AfterTheFactAcquisitionType afterTheFactAcquisitionType) {
 	this.afterTheFactAcquisitionType = afterTheFactAcquisitionType;
     }
 
     public void setFileContents(final byte[] contents) {
 	this.contents = new String(contents);
     }
 
     public String getContents() {
 	return contents;
     }
 
     public List<Issue> getIssues() {
 	return issues;
     }
 
     public void registerIssue(final IssueType issueType, final int lineNumber, final String... messageArgs) {
 	if (issueType.getIssueTypeLevel() == IssueTypeLevel.ERROR) {
 	    errorCount++;
 	} else if (issueType.getIssueTypeLevel() == IssueTypeLevel.WARNING) {
 	    warningCount++;
 	}
 	issues.add(new Issue(issueType, lineNumber + 1, messageArgs));
     }
 
     public void reset() {
 	importedLines = errorCount = warningCount = 0;
 	issues.clear();
     }
 
     @Service
     public void importAcquisitions() {
 	final String[] lines = getContents().split("\n");
 	final ImportFile file = new ImportFile(getContents().getBytes());
 	for (int i = 0; i < lines.length; i++) {
 	    final String line = lines[i];
 	    if (line.isEmpty()) {
 		registerIssue(IssueType.EMPTY_LINE, i);
 	    } else {
 		final String[] parts = line.split("\t");
 		if (parts.length == 5 || parts.length == 4) {
 		    final String supplierNif = cleanupNIF(parts[0]);
 		    final String supplierName = StringNormalizer.normalizePreservingCapitalizedLetters(cleanup(parts[1]));
 		    Supplier supplier = findSupplier(supplierNif, supplierName);
 		    if (supplier == null) {
 			if (!createData) {
 			    registerIssue(IssueType.SUPPLIER_DOES_NOT_EXIST, i, supplierNif, supplierName);
 			} else {
 			    supplier = createSupplier(supplierName, supplierNif);
 			}
 		    }
 		    final String description = cleanup(parts[2]);
 		    final String valueString = cleanup(parts[3]);
 		    Money value;
 		    try {
			value = new Money(valueString.replace('.', 'X').replace("X", "").replace(',', '.'));
 		    } catch (DomainException ex) {
 			value = null;
 			registerIssue(IssueType.BAD_MONEY_VALUE_FORMAT, i, valueString);
 		    } catch (NumberFormatException ex) {
 			value = null;
 			registerIssue(IssueType.BAD_MONEY_VALUE_FORMAT, i, valueString);
 		    }
 		    final String vatValueString = parts.length == 5 ? cleanup(parts[4]) : "0";
 		    BigDecimal vatValue;
 		    try {
			vatValue = new BigDecimal(vatValueString.replace('.', 'X').replace("X", "").replace(',', '.'));
 		    } catch (NumberFormatException ex) {
 			vatValue = null;
 			registerIssue(IssueType.BAD_VAT_VALUE_FORMAT, i, vatValueString);
 		    }
 		    if (supplier != null && value != null && vatValue != null) {
 			if (supplier.isFundAllocationAllowed(value)) {
 			    if (createData) {
 				try {
 				    importAcquisition(supplier, description, value, vatValue, file);
 				} catch (DomainException e) {
 				    registerIssue(IssueType.CANNOT_ALLOCATE_MONEY_TO_SUPPLIER, i, supplierNif, supplierName,
 					    valueString);
 				}
 			    }
 			    importedLines++;
 			} else {
			    System.out.println(supplier.getName());
			    System.out.println("   total allocated:" + supplier.getTotalAllocated().getValue());
			    System.out.println("   trying to allocate value: " + value.getValue());
 			    registerIssue(IssueType.CANNOT_ALLOCATE_MONEY_TO_SUPPLIER, i, supplierNif, supplierName, valueString);
 			}
 		    }
 		} else {
 		    registerIssue(IssueType.WRONG_NUMBER_LINE_COLUMNS, i);
 		}
 	    }
 	}
 	if (issues.size() > 0 && createData) {
 	    throw new ImportError();
 	}
     }
 
     private Supplier createSupplier(String supplierName, String supplierNif) {
 	Supplier supplier = new Supplier(supplierNif);
 	supplier.setName(supplierName);
 	return supplier;
     }
 
     public static class ImportError extends Error {
     }
 
     public void importAcquisition(final Supplier supplier, final String description, final Money value,
 	    final BigDecimal vatValue, ImportFile file) {
 	final AfterTheFactAcquisitionProcessBean afterTheFactAcquisitionProcessBean = new AfterTheFactAcquisitionProcessBean();
 	afterTheFactAcquisitionProcessBean.setAfterTheFactAcquisitionType(getAfterTheFactAcquisitionType());
 	afterTheFactAcquisitionProcessBean.setDescription(description);
 	afterTheFactAcquisitionProcessBean.setSupplier(supplier);
 	afterTheFactAcquisitionProcessBean.setValue(value);
 	afterTheFactAcquisitionProcessBean.setVatValue(vatValue);
 	file.addAfterTheFactAcquisitionProcesses(AfterTheFactAcquisitionProcess
 		.createNewAfterTheFactAcquisitionProcess(afterTheFactAcquisitionProcessBean));
     }
 
     private Supplier findSupplier(final String supplierNif, final String supplierName) {
 	final Supplier supplier = Supplier.readSupplierByFiscalIdentificationCode(supplierNif);
 	return supplier == null ? Supplier.readSupplierByName(supplierName) : supplier;
     }
 
     public int getErrorCount() {
 	return errorCount;
     }
 
     public int getWarningCount() {
 	return warningCount;
     }
 
     public boolean hasError() {
 	return getErrorCount() > 0;
     }
 
     public int getImportedLines() {
 	return importedLines;
     }
 
     private String cleanupNIF(final String string) {
 	return cleanup(string).replace(" ", "");
     }
 
     private String cleanup(final String string) {
 	String result = string;
 	if (result.length() >= 2 && result.charAt(0) == '"' && result.charAt(result.length() - 1) == '"') {
 	    result = result.substring(1, result.length() - 1);
 	}
 	result = result.replace('€', ' ');
 	result = result.trim();
 	return result;
     }
 
     public boolean isCreateData() {
 	return createData;
     }
 
     public void setCreateData(boolean createData) {
 	this.createData = createData;
     }
 
 }
