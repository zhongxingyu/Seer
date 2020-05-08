 package Utilitaire;
 
 import java.io.*;
 import java.util.*;
 
 public class Generator 
 {
 	private String code;
 	private File file;
 	
 	public Generator()
 	{
 		code = header();
 	}
 	
 	public void setFichier(File file)
 	{
 		this.file = file;
 	}
 	
 	private String header() 
 	{
 		return "<html>\n" +
 					"\t<head>\n" +
					"\t\t<title>Page 1</title>" +
 					"\t</head>\n" +
 					"\t<body>\n\t\t";
 	}
 	
 	public void addTitre(String s) 
 	{
 		code += "<div class=\"titre\">"+ s +"</div><br />\n\t\t";
 	}
 	
 	public void addParagraphe(String s)
 	{
 	    Scanner sc = new Scanner(s).useDelimiter("\n");
 	    
 	    while (sc.hasNext())
 	    	code += sc.next()+"<br />\n\t\t";
 	}
 	
 	public void ajouterImage(String chemin)
 	{
 		// TODO Auto-generated method stub
 		code += "<img src='" + chemin + "' />\n";
 	}
 	
 	public void generate() 
 	{
 		code += "\n\t</body>\n" +
 				"</html>\n";
 		
 		File file, content,css,img;
 		
 		file = new File ("site/a1.html");
 		content = new File ("site/content");
 		css = new File ("site/content/CSS");
 		img = new File ("site/content/IMG");
 		
 		if ( !content.exists())
 		{
 			content.mkdir();
 			if ( !css.exists())
 				css.mkdir();
 			if ( !img.exists())
 				img.mkdir();
 		}
 		
 		try
 		{
 			// on recree le fichier lorsqu'on genere
 			file.createNewFile();
 			
 			// on ecris le code dedans
 			BufferedWriter fichier = new BufferedWriter(new FileWriter(file));
 			fichier.write(code);
 			
 			fichier.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		System.out.println(code);
 	}
 }
