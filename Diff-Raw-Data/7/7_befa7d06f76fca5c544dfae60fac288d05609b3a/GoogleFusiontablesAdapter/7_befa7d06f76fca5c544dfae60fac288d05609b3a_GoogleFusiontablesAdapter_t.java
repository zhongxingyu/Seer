 package com.crowdplatform.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 
 import org.springframework.stereotype.Service;
 
 import com.crowdplatform.model.Batch;
 import com.crowdplatform.model.Field;
 import com.crowdplatform.model.Project;
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
 import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
 import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
 import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
 import com.google.api.client.http.ByteArrayContent;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson2.JacksonFactory;
 import com.google.api.services.fusiontables.Fusiontables;
 import com.google.api.services.fusiontables.FusiontablesScopes;
 import com.google.api.services.fusiontables.model.Column;
 import com.google.api.services.fusiontables.model.Table;
 import com.google.common.collect.Lists;
 
 @Service
 public class GoogleFusiontablesAdapter {
 
 	private static final String APPLICATION_NAME = "Crowd Platform";
 	
 	private static final String FUSIONTABLES_URL = "https://www.google.com/fusiontables/data?docid=";
 
 	private HttpTransport HTTP_TRANSPORT;
 
 	private final JsonFactory JSON_FACTORY = new JacksonFactory();
 
 	private Fusiontables fusiontables;
 
 	public String exportDataURL(Project project, Batch batch) {
 		try {
 			try {
 				HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
 				// authorization
 				Credential credential = authorize();
 				// set up global FusionTables instance
 				fusiontables = new Fusiontables.Builder(
 						HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
 
 				// List tables to see if it exists
 				Table table = retrieveTable(project, batch);
 
 				// Inserts
 				insertData(table, project, batch);
 				
 				return FUSIONTABLES_URL + table.getTableId();
 				
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
				e.printStackTrace();
 			}
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 
 		return null;
 	}
 
 	public Credential authorize() throws Exception {
 		// load client secrets
 		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
 				JSON_FACTORY, GoogleFusiontablesAdapter.class.getResourceAsStream("/client_secrets.json"));
 		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
 				|| clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
 			System.out.println(
 					"Enter Client ID and Secret from https://code.google.com/apis/console/?api=fusiontables "
 							+ "into src/main/resources/client_secrets.json");
 		}
 		// set up file credential store
 		FileCredentialStore credentialStore = new FileCredentialStore(
 				new File(System.getProperty("user.home"), ".credentials/fusiontables.json"), JSON_FACTORY);
 		// set up authorization code flow
 		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
 				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets,
 				Collections.singleton(FusiontablesScopes.FUSIONTABLES)).setCredentialStore(credentialStore)
 				.build();
 		// authorize
 		return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
 	}
 	
 	private Table retrieveTable(Project project, Batch batch) throws IOException {
 		if (batch.getFusiontableId() != null) {
 			Fusiontables.Table.Get query = fusiontables.table().get(batch.getFusiontableId());
 			try {
 				Table table = query.execute();
 				Fusiontables.Query.Sql delete = fusiontables.query().sql("DELETE FROM " + table.getTableId());
 				delete.execute();
 				return table;
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
 				System.out.println("Table could not be retrieved, creating a new one.");
 			}
 		}
 		return createTable(project, batch);
 	}
 	
 	private Table createTable(Project project, Batch batch) throws IOException {
 		Table table = new Table();
 		table.setName(batch.getName());
 		table.setIsExportable(false);
 		table.setDescription("Data from Crowd Platform batch " + batch.getId());
 		List<Column> columns = Lists.newArrayList();
 		
 		columns.add(new Column().setName("task_id").setType("NUMBER"));
 		for (Field field : project.getOrderedInputFields()) {
 			if (field.getType().equals(Field.Type.DOUBLE) || field.getType().equals(Field.Type.INTEGER)) {
 				columns.add(new Column().setName(field.getName()).setType("NUMBER"));
 			} else {
 				columns.add(new Column().setName(field.getName()).setType("STRING"));
 			}
 		}
 		columns.add(new Column().setName("execution_id").setType("NUMBER"));
 		columns.add(new Column().setName("date").setType("DATETIME"));
 		columns.add(new Column().setName("userId").setType("NUMBER"));
 		for (Field field : project.getOrderedOutputFields()) {
 			if (field.getType().equals(Field.Type.DOUBLE) || field.getType().equals(Field.Type.INTEGER)) {
 				columns.add(new Column().setName(field.getName()).setType("NUMBER"));
 			} else {
 				columns.add(new Column().setName(field.getName()).setType("STRING"));
 			}
 		}
 		
 		table.setColumns(columns);
 		
 		Fusiontables.Table.Insert insert = fusiontables.table().insert(table);
 		
 		Table result = insert.execute();
 		batch.setFusiontableId(result.getTableId());
 		
 		return result;
 	}
 	
 	private void insertData(Table table, Project project, Batch batch) throws IOException {
 		String writer = (new FileWriter()).writeTasksExecutions(Lists.newArrayList(batch.getOrderedTasks()), 
 				project.getOrderedInputFields(), project.getOrderedOutputFields(), project.getOrderedUserFields(), false);
 		ByteArrayContent byteArrayContent = ByteArrayContent.fromString("application/octet-stream", writer);
 		Fusiontables.Table.ImportRows importRows = fusiontables.table().importRows(table.getTableId(), byteArrayContent);
 		importRows.setIsStrict(false);
 		importRows.execute();
 	}
 }
