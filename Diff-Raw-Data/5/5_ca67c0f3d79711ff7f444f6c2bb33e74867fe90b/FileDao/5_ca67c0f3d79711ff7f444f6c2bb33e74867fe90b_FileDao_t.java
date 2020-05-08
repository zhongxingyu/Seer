 package ba.kickboxing.draw.persistence;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.util.List;
 
 import ba.kickboxing.draw.common.Player;
 
 public class FileDao implements DAO {
 
 	private String fileName;
 	private boolean append;
 
 	public FileDao(String fileName, boolean append) {
 		this.fileName = fileName;
 		this.append = append;
 	}
 
 	@Override
 	public void savePlayer(Player player) {
 		Writer out = null;
 		try {
			out = new OutputStreamWriter(new FileOutputStream(fileName, append), "UTF-8");			
 			out.write(player.toString());
			out.write(System.getProperty("line.separator"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				out.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public List<Player> listAllPlayers() throws IOException {
 		return IO.readFromTxt(fileName);		
 	}
 
 }
