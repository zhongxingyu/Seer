 package pcc.test.integration;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import pcc.vercon.ProjectVersion;
 import pcc.vercon.SourceFileRecord;
 
 public class StubVersion2 extends ProjectVersion {
 	private static final long serialVersionUID = 4623446490667911804L;
 	
 	StubFile3 stubFileA = new StubFile3();
 	StubFile4 stubFileC = new StubFile4();
 
 	public StubVersion2() throws IOException {
		super("", "", "", new ArrayList<String>());
 	}
 	
 	@Override
 	public ArrayList<SourceFileRecord> getFiles(){
 		ArrayList<SourceFileRecord> files = new ArrayList<SourceFileRecord>();
 		files.add(stubFileA);
 		files.add(stubFileC);
 		return files;
 	}
 	
 	@Override
 	public SourceFileRecord getFile(String name){
 		if(name.equals("StubFileA.txt"))
 			return stubFileA;
 		else if(name.equals("StubFileC.txt"))
 			return stubFileC;
 		else
 			return null;
 	}
 	
 	@Override
 	public String getMetaData(){
 		return "<Meta Data Version 2>";
 	}
 	
 	@Override
 	public String getNumber(){
 		return "2";
 	}
}
