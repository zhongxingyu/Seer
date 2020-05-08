 package org.jboss.jca.adapters.sap.demo.service;
 
 import javax.annotation.Resource;
 import javax.resource.ResourceException;
 import javax.resource.cci.Connection;
 import javax.resource.cci.IndexedRecord;
 import javax.resource.cci.Interaction;
 import javax.resource.cci.MappedRecord;
 
 import org.jboss.jca.adapters.sap.cci.CciFactory;
 import org.jboss.jca.adapters.sap.cci.InteractionSpec;
 import org.jboss.jca.adapters.sap.demo.model.Customer;
 import org.jboss.jca.adapters.sap.demo.model.CustomerList;
 
 public class CustomerService {
 	
 	private static final int DEFAULT_INDEX = 0;
 	private static final int DEFAULT_CHUNK_SIZE = 10;
 
	private static final String INPUT_RECORD_NAME = "BAPI_FLCUST_GETLIST.INPUT_RECORD";
	private static final String OUTPUT_RECORD_NAME = "BAPI_FLCUST_GETLIST.OUTPUT_RECORD";
 
 	private static final String GET_CUSTOMER_LIST_FUNC = "BAPI_FLCUST_GETLIST";
 
 	private static final String CUSTOMER_LIST_PARAM = "CUSTOMER_LIST";
 
 	private static final String CUSTOMER_ID_PARAM = "CUSTOMERID";
 	private static final String CUSTOMER_NAME_PARAM = "CUSTNAME";
 	private static final String CUSTOMER_TITLE_PARAM = "FORM";
 	private static final String CUSTOMER_STREET_PARAM = "STREET";
 	private static final String CUSTOMER_PO_BOX_PARAM = "POBOX";
 	private static final String CUSTOMER_POSTAL_CODE_PARAM = "POSTCODE";
 	private static final String CUSTOMER_CITY_PARAM = "CITY";
 	private static final String CUSTOMER_COUNTRY_PARAM = "COUNTR";
 	private static final String CUSTOMER_COUNTRY_CODE_PARAM = "COUNTR_ISO";
 	private static final String CUSTOMER_REGION_PARAM = "REGION";
 	private static final String CUSTOMER_PHONE_NUMBER_PARAM = "PHONE";
 	private static final String CUSTOMER_EMAIL_PARAM = "EMAIL";
 
 	private static final String CUSTOMER_RANGE_PARAM = "CUSTOMER_RANGE";
 
 	private static final String CUSTOMER_RANGE_SIGN_PARAM = "SIGN";
 	private static final String CUSTOMER_RANGE_OPTION_PARAM = "OPTION";
 	private static final String CUSTOMER_RANGE_LOW_PARAM = "LOW";
 	private static final String CUSTOMER_RANGE_HIGH_PARAM = "HIGH";
 
 	private static final String CUSTOMER_RANGE_INCLUSIVE_VALUE = "I";
 	private static final String CUSTOMER_RANGE_EQUALS_VALUE = "EQ";
 
 	private static final String CUSTOMER_SEARCH_NAME_PARAM = "CUSTOMER_NAME";
 
 	@Resource(mappedName = "java:/eis/JBossSAPConnectionFactory")
 	private javax.resource.cci.ConnectionFactory connectionFactory;
 
 
 	@SuppressWarnings("unchecked")
 	public CustomerList searchCustomers(String name, String index, String chunkSize) throws ResourceException {
 		CustomerList list = new CustomerList();
 		list.setIndex(DEFAULT_INDEX);
 		list.setChunkSize(DEFAULT_CHUNK_SIZE);
 		list.setName(name);
 		if (index != null && index.length() > 0) {
 			try {
 				int idx = Integer.parseInt(index);
 				list.setIndex(idx);
 			} catch (NumberFormatException e) {
 				// Ignore and use default index.
 			}
 		}
 		if (chunkSize != null && chunkSize.length() > 0) {
 			try {
 				int chnkSize = Integer.parseInt(chunkSize);
 				list.setChunkSize(chnkSize);
 			} catch (NumberFormatException e) {
 				// Ignore and use default chunkSize.
 			}
 		}
 
 		Connection connection = null;
 		try {
 			connection = connectionFactory.getConnection();
 			Interaction interaction = connection.createInteraction();
 			InteractionSpec interactionSpec = CciFactory.INSTANCE.createInteractionSpec();
 			interactionSpec.setFunctionName(GET_CUSTOMER_LIST_FUNC);
 
 			MappedRecord input = connectionFactory.getRecordFactory().createMappedRecord(INPUT_RECORD_NAME);
 
 			// Build Customer Range for customer selection.
 			input.put(CUSTOMER_SEARCH_NAME_PARAM, name);
 
 			MappedRecord output = connectionFactory.getRecordFactory().createMappedRecord(OUTPUT_RECORD_NAME);
 			if (interaction.execute(interactionSpec, input, output)) {
 				IndexedRecord customerList = (IndexedRecord) output.get(CUSTOMER_LIST_PARAM);
 				if (customerList != null) {
 
 					Customer customer = null;
 					for (Object listEntry : customerList) {
 						MappedRecord entry = (MappedRecord) listEntry;
 						customer = new Customer();
 						customer.setId((String) entry.get(CUSTOMER_ID_PARAM));
 						customer.setName((String) entry.get(CUSTOMER_NAME_PARAM));
 						customer.setTitle((String) entry.get(CUSTOMER_TITLE_PARAM));
 						customer.setStreet((String) entry.get(CUSTOMER_STREET_PARAM));
 						customer.setPoBox((String) entry.get(CUSTOMER_PO_BOX_PARAM));
 						customer.setPostalCode((String) entry.get(CUSTOMER_POSTAL_CODE_PARAM));
 						customer.setCity((String) entry.get(CUSTOMER_CITY_PARAM));
 						customer.setCountry((String) entry.get(CUSTOMER_COUNTRY_PARAM));
 						customer.setCountryCode((String) entry.get(CUSTOMER_COUNTRY_CODE_PARAM));
 						customer.setRegion((String) entry.get(CUSTOMER_REGION_PARAM));
 						customer.setPhoneNumber((String) entry.get(CUSTOMER_PHONE_NUMBER_PARAM));
 						customer.setEmail((String) entry.get(CUSTOMER_EMAIL_PARAM));
 						list.getCustomers().add(customer);
 					}
 				}
 				
 				list.setTotal(list.getCustomers().size());
 				
 				if (list.getCustomers().size() > list.getIndex() + list.getChunkSize()) {
 					list.setCustomers(list.getCustomers().subList(list.getIndex(), list.getIndex() + list.getChunkSize()));
 				} else if (list.getCustomers().size() > list.getIndex()) {
 					list.setCustomers(list.getCustomers().subList(list.getIndex(), list.getCustomers().size()));
 				} else {
 					list.getCustomers().clear();
 				}
 			}
 			return list;
 		} finally {
 			if (connection != null)
 				connection.close();
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public Customer getCustomer(String id) throws ResourceException {
 		Customer customer = null;
 
 		Connection connection = null;
 		try {
 			connection = connectionFactory.getConnection();
 			Interaction interaction = connection.createInteraction();
 			InteractionSpec interactionSpec = CciFactory.INSTANCE.createInteractionSpec();
 			interactionSpec.setFunctionName(GET_CUSTOMER_LIST_FUNC);
 
 			// Build parameters for customer search.
 			IndexedRecord customerRange = connectionFactory.getRecordFactory().createIndexedRecord(CUSTOMER_RANGE_PARAM);
 			MappedRecord customerRangeEntry = connectionFactory.getRecordFactory().createMappedRecord(CUSTOMER_RANGE_PARAM);
 			customerRangeEntry.put(CUSTOMER_RANGE_SIGN_PARAM, CUSTOMER_RANGE_INCLUSIVE_VALUE);
 			customerRangeEntry.put(CUSTOMER_RANGE_OPTION_PARAM, CUSTOMER_RANGE_EQUALS_VALUE);
 			customerRangeEntry.put(CUSTOMER_RANGE_LOW_PARAM, id);
 			customerRangeEntry.put(CUSTOMER_RANGE_HIGH_PARAM, id);
 			customerRange.add(customerRangeEntry);
 
 			MappedRecord input = connectionFactory.getRecordFactory().createMappedRecord(INPUT_RECORD_NAME);
 			input.put(CUSTOMER_RANGE_PARAM, customerRange);
 			MappedRecord output = connectionFactory.getRecordFactory().createMappedRecord(OUTPUT_RECORD_NAME);
 			if (interaction.execute(interactionSpec, input, output)) {
 				IndexedRecord customerList = (IndexedRecord) output.get(CUSTOMER_LIST_PARAM);
 				for (Object listEntry : customerList) {
 					MappedRecord entry = (MappedRecord) listEntry;
 					customer = new Customer();
 					customer.setId((String) entry.get(CUSTOMER_ID_PARAM));
 					customer.setName((String) entry.get(CUSTOMER_NAME_PARAM));
 					customer.setTitle((String) entry.get(CUSTOMER_TITLE_PARAM));
 					customer.setStreet((String) entry.get(CUSTOMER_STREET_PARAM));
 					customer.setPoBox((String) entry.get(CUSTOMER_PO_BOX_PARAM));
 					customer.setPostalCode((String) entry.get(CUSTOMER_POSTAL_CODE_PARAM));
 					customer.setCity((String) entry.get(CUSTOMER_CITY_PARAM));
 					customer.setCountry((String) entry.get(CUSTOMER_COUNTRY_PARAM));
 					customer.setCountryCode((String) entry.get(CUSTOMER_COUNTRY_CODE_PARAM));
 					customer.setRegion((String) entry.get(CUSTOMER_REGION_PARAM));
 					customer.setPhoneNumber((String) entry.get(CUSTOMER_PHONE_NUMBER_PARAM));
 					customer.setEmail((String) entry.get(CUSTOMER_EMAIL_PARAM));
 				}
 			}
 
 			return customer;
 		} finally {
 			if (connection != null)
 				connection.close();
 		}
 	}
 
 }
