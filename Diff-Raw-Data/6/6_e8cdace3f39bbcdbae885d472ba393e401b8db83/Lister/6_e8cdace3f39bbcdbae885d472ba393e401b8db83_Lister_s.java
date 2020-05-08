 package es.foxcav.foxcaves.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Lister extends Authenticatable {
 	public Lister() {
 		super();
 	}
 
 	public Lister(String username, String password) {
 		super(username, password);
 	}
 
 	public List<FileInfo> getFiles() throws IOException {
 		HttpURLConnection httpURLConnection = makeHttpURLConnection("list");
 		httpURLConnection.setRequestMethod("GET");
 		httpURLConnection.connect();
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
 
 		List<FileInfo> ret = new ArrayList<FileInfo>();
 
 		String curLine;
 		while ((curLine = reader.readLine()) != null)
 		{
 			String[] lineSplit = curLine.split(">");
			ret.add(new FileInfo(lineSplit[0], lineSplit[1], lineSplit[2], Long.parseLong(lineSplit[3]), lineSplit[4]));
 		}
 
 		reader.close();
 
 		return ret;
 	}
 
 	public class FileInfo
 	{
 		public final String id;
 		public final String name;
 		public final String extension;
 		public final long size;
 		public final String thumbnail;
 
 		private FileInfo(String id, String name, String extension, long size, String thumbnail)
 		{
 			this.id = id;
 			this.name = name;
 			this.extension = extension;
 			this.size = size;
 			this.thumbnail = thumbnail;
 		}
 	}
 }
