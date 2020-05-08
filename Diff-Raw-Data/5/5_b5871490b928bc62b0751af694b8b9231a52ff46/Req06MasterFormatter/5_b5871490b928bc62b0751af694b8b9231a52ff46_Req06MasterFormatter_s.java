 package com.sforce.parser;
 
 import java.util.ArrayList;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sforce.column.Column;
 import com.sforce.column.DateColumn;
 import com.sforce.column.DoubleColumn;
 import com.sforce.column.FakeColumn;
 import com.sforce.column.StringColumn;
 import com.sforce.soap.enterprise.sobject.Opportunity;
 import com.sforce.to.SfSqlConfig;
 /**
  * 
  */
 public class Req06MasterFormatter extends BaseParser<Opportunity> {
 	private static final Logger logger = LoggerFactory.getLogger(Req06MasterFormatter.class);
 	
 	@Override
 	public boolean accept(String[] source) {
 		return 105 == source.length && "H".equals(source[0]);
 	}
 
 	@Override
 	public Logger getLogger() {
 		return logger;
 	}
 
 	@Override
 	protected void initDefaultColumns() {
 		this.columns = new ArrayList<Column<?>>();
 		int i = 0;
 		columns.add(new FakeColumn(i++, "H", ""));
 		
 		columns.add(new StringColumn(i++, "name", "Name, Id"));
 		columns.add(new StringColumn(i++, "documentStatusC", "Document_Status__c"));
 		columns.add(new StringColumn(i++, "stageName", "StageName"));
 		columns.add(new StringColumn(i++, "stopTrackingTypeC", "Stop_Tracking_Type__c"));
 		columns.add(new DateColumn(i++, "stopTrackingDateC", "Stop_Tracking_Date__c"));
 		columns.add(new StringColumn(i++, "projectFailedReasonC", "Project_Failed_Reason__c"));
 		columns.add(new StringColumn(i++, "projectFailedReasonRemarkC", "Project_Failed_Reason_Remark__c"));
 		columns.add(new StringColumn(i++, "proposerCompanyNoC", "Proposer_Company_No__c"));
 		columns.add(new StringColumn(i++, "proposerNameC", "Proposer_Name__c"));
 		columns.add(new StringColumn(i++, "groupNameC", "Group_Name__c"));
 		
		columns.add(new StringColumn(i++, "majorApplicationC", "Major_Application__r.Name"));//cheat 1
 		columns.add(new StringColumn(i++, "realApplicationC", "Real_Application__c"));
 		columns.add(new StringColumn(i++, "modelNameC", "Model_Name__r.Name")); //cheat 2
 		columns.add(new StringColumn(i++, "coreChipVendorC", "Core_chip_Vendor__r.AccountNumber"));//cheat 3
 		columns.add(new StringColumn(i++, "coreChipModelC", "Core_chip_Model__r.Name"));//cheat 4
 		columns.add(new StringColumn(i++, "brandCompanyC", "Brand_Company__r.AccountNumber"));//cheat 5 Brand_Company__r.AccountNumber
 		columns.add(new StringColumn(i++, "brandModelNameC", "Brand_Model_Name__r.Name"));//cheat 6 Brand_Model_Name__r.Name
 		columns.add(new StringColumn(i++, "operatingSystemC", "Operating_System__c"));
 		columns.add(new StringColumn(i++, "bootLoaderC", "Boot_Loader__c"));
 		columns.add(new DoubleColumn(i++, "yearlyVolumeC", "Yearly_Volume__c"));
 		
 		columns.add(new StringColumn(i++, "accountId", "Account.AccountNumber"));//cheat 7
 		columns.add(new StringColumn(i++, "platformDescriptionC", "Platform_Description__c"));
 		columns.add(new StringColumn(i++, "projectTypeC", "Project_Type__c"));
 		columns.add(new StringColumn(i++, "repOrDistyC", " Rep_Or_Disty__r.AccountNumber"));//cheat 8
 		columns.add(new DateColumn(i++, "PVCSampleDateC", "PVC_Sample_Date__c"));
 		columns.add(new DateColumn(i++, "closeDate", "CloseDate"));
 		columns.add(new StringColumn(i++, "nextStep", "NextStep"));
 		columns.add(new DateColumn(i++, "milestoneDateC", "Milestone_Date__c"));
 		columns.add(new StringColumn(i++, "orderInFromC", "Order_in_from__c"));
 		columns.add(new StringColumn(i++, "ownerId", "Owner.FirstName,Owner.LastName"));//cheat 9 -0
 
 		columns.add(new StringColumn(i++, "tempRecordTypeC", "Owner.Region__c"));//cheat 9 -1
 		columns.add(new StringColumn(i++, "manufactureSiteAMC", "Owner.DI_Region__c"));//cheat 9 -2
 		columns.add(new StringColumn(i++, "manufactureSiteC", "Owner.DEPT__c"));//cheat 9 -3
 		columns.add(new DateColumn(i++, "createdDate", "CreatedDate"));
 		columns.add(new StringColumn(i++, "createdById", "CreatedBy.FirstName,CreatedBy.LastName"));//cheat 10
 		columns.add(new StringColumn(i++, "creatorDeptC", "Creator_Dept__c"));
 		columns.add(new StringColumn(i++, "recordTypeC", "Record_Type__c"));
 		columns.add(new StringColumn(i++, "DIURLC", "DI_URL__c"));
 		columns.add(new DateColumn(i++, "needBDIDateC", "Need_BDI_Date__c"));
 		columns.add(new DateColumn(i++, "incentiveAppliedDateC", "Incentive_Applied_Date__c"));
 
 		columns.add(new DateColumn(i++, "submitDateC", "Submit_Date__c"));
 		columns.add(new StringColumn(i++, "lastModifiedById", "LastModifiedBy.FirstName,LastModifiedBy.LastName"));//cheat 11
 		columns.add(new DateColumn(i++, "lastModifiedDate", "LastModifiedDate"));
 		//43,3,2,5,5,36,10
 		for (int index = 0; index < 3; index++) {
 			columns.add(new FakeColumn(i++, "", ""));
 		}
 		for (int index = 0; index < 2; index++) {
 			columns.add(new FakeColumn(i++, "", ""));
 		}
 		for (int index = 0; index < 5; index++) {
 			columns.add(new FakeColumn(i++, "", ""));
 		}
 		for (int index = 0; index < 5; index++) {
 			columns.add(new FakeColumn(i++, "", ""));
 		}
 		for (int index = 0; index < 36; index++) {
 			columns.add(new FakeColumn(i++, "", ""));
 		}
 		for (int index = 0; index < 10; index++) {
 			columns.add(new FakeColumn(i++, "", ""));
 		}
 		this.tableName = "Opportunity";
 	}
 
 	@Override
 	public void postParse(Opportunity entity) {
 	}
 
 	protected String buildSfCondition(SfSqlConfig config) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(" and StageName <> 'Draft' ");
 		/*
 		if (null != config.getLasySyncDate()) {
 			sb.append(" and LastModifiedDate > "+DateUtils.formatSfDateTime(config.getLasySyncDate()));
 		}
 		*/
 		return sb.toString();
 	}
 
 	@Override
 	public void preFormat(Opportunity entity) {
 		//cheat 1
 		if (null != entity.getMajorApplicationR()) {
			entity.setMajorApplicationC(entity.getMajorApplicationR().getName());
 		}
 		//cheat 2
 		if (null != entity.getModelNameR()) {
 			entity.setModelNameC(entity.getModelNameR().getName());
 		}
 		//cheat 3
 		if (null != entity.getCoreChipVendorR()) {
 			entity.setCoreChipVendorC(entity.getCoreChipVendorR().getAccountNumber());
 		}
 		if (null != entity.getCreatedBy()) {
 			entity.setCreatedById(this.formateAsName(entity.getCreatedBy()));
 		}
 		//cheat 4
 		if (null != entity.getCoreChipModelR()) {
 			entity.setCoreChipModelC(entity.getCoreChipModelR().getName());
 		}
 		//cheat 5
 		if (null != entity.getBrandCompanyR()) {
 			entity.setBrandCompanyC(entity.getBrandCompanyR().getName());
 		}
 		//cheat 6
 		if (null != entity.getBrandModelNameR()) {
 			entity.setBrandModelNameC(entity.getBrandModelNameR().getName());
 		}
 		//cheat 7
 		if (null != entity.getAccount()) {
 			entity.setAccountId(entity.getAccount().getAccountNumber());
 		}
 		//cheat 8 Rep_Or_Disty__r.AccountNumber
 		if (null != entity.getRepOrDistyR()) {
 			entity.setRepOrDistyC(entity.getRepOrDistyR().getAccountNumber());
 		}
 		//cheat 9 -0
 		if (null != entity.getOwner()) {
 			entity.setOwnerId(this.formateAsName(entity.getOwner()));
 		}
 		if (null != entity.getOwner()) {
 			entity.setOwnerId(this.formateAsName(entity.getOwner()));
 			entity.setTempRecordTypeC(entity.getOwner().getRegionC());
 			entity.setManufactureSiteAMC(entity.getOwner().getDIRegionC());
 			entity.setManufactureSiteC(entity.getOwner().getDEPTC());
 		}
 		//cheat 10
 		if (null != entity.getCreatedBy()) {
 			entity.setCreatedById(this.formateAsName(entity.getCreatedBy()));
 		}
 		//cheat 11
 		if (null != entity.getLastModifiedBy()) {
 			entity.setLastModifiedById(this.formateAsName(entity.getLastModifiedBy()));
 		}
 	}
 	
 }
