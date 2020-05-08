 package org.code.metrxn.service;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Map;
 import java.util.UUID;
 import org.code.metrxn.model.uploader.FileColumn;
 import org.code.metrxn.model.uploader.TableMapper;
 import org.code.metrxn.repository.workflow.MapperRepository;
 import org.code.metrxn.repository.dml.*;
 import org.code.metrxn.service.workflow.UploaderService;
import org.code.metrxn.util.JsonUtil;
 import org.code.metrxn.util.Logger;
 
 /**
  * TO Test the uploader service
  * @author ambika_b
  *
  */
 public class UploaderServiceTest {
 	static Connection connection;
 
 	static MapperRepository mapperRepository;
 	
 	static InsertRepository insertRepository;
 	
 	static UploaderService uploaderService;
 	
 	public static void createConnection() {
 		String connectionURL = "jdbc:mysql://costas4086.engr.psu.edu:3306/test_Metrxn_version_2";
 		try {
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			connection = DriverManager.getConnection(connectionURL, "ambika", "ambika");
 			mapperRepository = new MapperRepository(connection);
 			insertRepository = new InsertRepository(connection);
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 		
 	public static void main (String[] args) {
 		Logger.info("creating connection", UploaderServiceTest.class);
 		createConnection();
 		uploaderService = new UploaderService(mapperRepository);
 		File file = new File("R:/univproject/otherDocs/sample.csv");
 		String delimitter = ",";
 		String entityType = "METABOLITE";
 		TableMapper tableMapper = null;
 		String workflowId = "";
 		Map<String, FileColumn> tableData = null;
 		try {
 			FileInputStream uploadedInputStream = new FileInputStream(file);
 			workflowId = UUID.randomUUID().toString();
 			Logger.info("reading contents", UploaderServiceTest.class);
 			tableData = uploaderService.readContents(uploadedInputStream, workflowId, entityType, delimitter, "\\", "TESTFILE");
 			Logger.info("fetching mappings", UploaderServiceTest.class);
 			tableMapper = mapperRepository.fetchDbMappings(workflowId, entityType, tableData);
 			tableMapper.setId(workflowId);
 			
 		} catch (IOException e) {
 			System.out.println("Error in reading csv files.");
 			e.printStackTrace();
 		}
 		insertRepository.metabolitesUpload(workflowId);
 		//System.out.println(JsonUtil.toJsonForObject(tableMapper).toString());
 		
 	}
 }
