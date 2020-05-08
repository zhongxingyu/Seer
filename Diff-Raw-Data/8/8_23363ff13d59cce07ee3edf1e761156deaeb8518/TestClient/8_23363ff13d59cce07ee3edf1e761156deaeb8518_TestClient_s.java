 package test;
 
 import file.File;
 import file.FileClient;
 import folder.FolderTree;
 
 public class TestClient {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		FolderTree ft = new FolderTree("kuku\\");
 		ft.addUser("user1");
 		File f = new File("test.txt");
 		ft.addFile(f, "user1");
		FileClient client = new FileClient(args[0], "test.txt");
 
 	}
 
 }
