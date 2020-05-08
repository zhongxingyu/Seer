 import java.io.IOException;
 
 abstract public class Copy {
 	String url;
 	int count;
 	Copy(String enterURL) throws IOException{
 		url = enterURL;
 		this.save();
 		this.count++;
 	};
 	
 	
 	abstract public  void save() throws IOException;
 	abstract public  void open() throws IOException;
 
 
 
 
 public static void main(String[] args) throws IOException {
 	if (args.length > 0) {
 		String enterString = args[0];
 		int dotPos = enterString.lastIndexOf(".");
 		String ext = enterString.substring(dotPos);
 		if(ext=="jpg"){
 			Copy pic = new Pictures(enterString);
 			pic.open();
 			
 			
 			
 		}
 		if(ext=="html"){
 			Copy html = new Html(enterString);
 			html.open();
 			}
 		
 		if(ext=="txt"){
 			Copy txt = new Text(enterString);
 			txt.open();
 			
 		}
        else{
			Copy thing = new Thing(enterString);
			txt.open();
			
		}
 		
 		
 		
 	   
 	  
 	
 	
 	
 	
 	
 	}
 	
 	
     
 }
 }
