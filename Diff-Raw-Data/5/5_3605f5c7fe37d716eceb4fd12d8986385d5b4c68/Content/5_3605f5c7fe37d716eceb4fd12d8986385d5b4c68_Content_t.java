 package sgit.action;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 
 public class Content extends BaseBrowse {
 	public String getContent() throws IOException {
 		InputStream is = getRepository().getFile(getPath());
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		StringBuilder result = new StringBuilder();
		String line;		
 		while((line = reader.readLine()) != null) {
 			result.append(line);
 			result.append("\n");
 		}
		return result.substring(0, result.length() - 1).toString();
 	}
 	@DefaultHandler
 	public Resolution content() {
 		return new ForwardResolution("/WEB-INF/sgit/content.jsp");
 	}
 }
