 package com.tastymonster.automation.codegen;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PatentMojoPresentationLayerInfo extends PresentationLayerInfo implements IPresentationLayerInfo {
 
 	private static final String DERIVED_PAGE_PATH = "src/test/java/com/tastymonster/automation/page/base/";
 	private static final String BASE_PAGE_PATH = "src/test/java/com/tastymonster/automation/page/base/";
 	private static final String TEMPLATE_PATH = "src/main/webapp/templates/";
 	private static final String CODEGEN_TEMPLATE_PATH = "src/test/java/com/tastymonster/automation/codegen/templates/";
	
	//TODO I'd like to make this a different folder, so it can be cleaned easily. At the moment I'm not sure how to do that, because
	// putting the files in src/test/generated/com... would not be part of the "maven convention". The question is, can I put it 
	// in a different folder and keep the IDE *and* Maven happy?
	private static final String GENERATED_PAGE_PATH = "src/test/java/com/tastymonster/automation/page/base/";
 
 	@Override
 	public List<File> getFileList() {
 		List<File> list = new ArrayList<File>();
 		list.add( new File( TEMPLATE_PATH + "index.vm" ) );
 		list.add( new File( TEMPLATE_PATH + "footer.vm" ) );
 		list.add( new File( TEMPLATE_PATH + "login.vm" ) );
 		list.add( new File( TEMPLATE_PATH + "user/createUser.vm" ) );
 		list.add( new File( TEMPLATE_PATH + "landing.vm" ) );
 		list.add( new File( TEMPLATE_PATH + "login.vm" ) );
 		list.add( new File( TEMPLATE_PATH + "inventory/addPatent.vm" ) );
 		return list;
 	}
 
 	@Override
 	public String getBasePagePath() {
 		return BASE_PAGE_PATH;
 	}
 
 	@Override
 	public String getDerivedPagePath() {
 		return DERIVED_PAGE_PATH;
 	}
 	
 	@Override
 	public String getGeneratedPagePath() {
 		return GENERATED_PAGE_PATH;
 	}
 
 	@Override
 	public String getCodegenTemplatePath() {
 		return CODEGEN_TEMPLATE_PATH;
 	}
 	
 	public String getTemplatePath() {
 		return TEMPLATE_PATH;
 	}
 }
