 package Utilitaire;
 
 import java.io.*;
 import java.util.*;
 
 import Main.Generateur;
 
 public class Generator 
 {
 	private String code;
 	
 	private void header() 
 	{
 		code = "<html>\n" +
 					"\t<head>\n" +
 					"\t\t<title>Page 1</title>\n" +
 					"\t</head>\n" +
 					"\t<body>\n";
 	}
 	
 	private void footer()
 	{
 		// TODO Auto-generated method stub
 		code += "\n\t</body>\n" +
 				"</html>\n";
 	}
 	
 /*	public void ajouterImage(String chemin)
 	{
 		// TODO Auto-generated method stub
 		alImage.add("<img src='" + chemin + "' />\n");
 	}
 	*/
 	public void generate() 
 	{
 		ArrayList<String> alS = Generateur.fenetre.getArborescence().getOrdreElement();
 		Page page = Generateur.alProjet.get(0).getPageSelectionne();
 		ArrayList<String> alTitre = Generateur.alProjet.get(0).getPage(page).getAlTitre();
 		ArrayList<String> alParagraphe = Generateur.alProjet.get(0).getPage(page).getAlParagraphe();
 		
 		header();
 		for (String s : alS )
 		{
 			Scanner sc = new Scanner(s);
 			sc.useDelimiter(" ");
 			
 			String type = sc.next();
 			int ind = Integer.parseInt(sc.next())-1;
 			
 			if (type.equals("Titre"))
 				code += "\t\t<div class=\"title\">"+alTitre.get(ind)+"</div>\n";
 			
 			if (type.equals("Paragraphe"))
				code += "\t\t<p>" + alParagraphe.get(ind) + "\t\t</p>\n";
 		}
 		
 		footer();
 		
 		File file, content,css,img;
 		
 		file = new File("site/" + page.getNom());
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
