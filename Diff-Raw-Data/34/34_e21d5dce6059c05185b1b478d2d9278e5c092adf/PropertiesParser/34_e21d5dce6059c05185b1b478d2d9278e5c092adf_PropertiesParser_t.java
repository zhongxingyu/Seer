 
 package com.liferay.portal.tools.propertiesconverter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

 public class PropertiesParser {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		Configuration cfg = new Configuration();
 		try {
 			cfg.setDirectoryForTemplateLoading(new File(
 				System.getProperty("user.dir") +
 					"/code/properties-converter/src/com/liferay/portal/tools/propertiesconverter/dependencies"));
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		cfg.setObjectWrapper(new DefaultObjectWrapper());
 		
 		// Create a data model for Freemarker
 		
 		Map root = new HashMap();
 		
 		String pageTitle = "Portal Properties";
 		if (!args[0].isEmpty()) {
 			pageTitle = args[0];
 		}
 		root.put("pageTitle", pageTitle);
 		
 		boolean toc = true;
 		if (!args[1].isEmpty()) {
 			toc = Boolean.parseBoolean(args[1]);
 		}
 		root.put("toc", toc);
 		
		String propertiesFileName = "portal.properties";
		if (!args[2].isEmpty()) {
			int ndx = args[2].lastIndexOf("/");
			if (ndx != -1) {
				propertiesFileName = args[2].substring(ndx + 1);
			}
		}
		root.put("propertiesFileName", propertiesFileName);
		
 		// Parse properties file and create sections for the data model
 		
		System.out.println("Converting " + System.getProperty("user.dir") +
			"/" + args[2] + " to HTML");
		File propertiesFile =
			new File(System.getProperty("user.dir") + "/" + args[2]);
 		String propertiesString = read(propertiesFile);
 		String[] paragraphs = propertiesString.split("\n\n");
 		ArrayList<Section> sections = new ArrayList<Section>();
 		
 		for (int i = 0; i < paragraphs.length; i++) {
 			if (paragraphs[i].startsWith("##")) {
 				Section section =
 					new Section(
 						new ArrayList<String>(), true, paragraphs[i].replace(
 							"#", "").trim(), paragraphs[i],
 						new ArrayList<String>(), "", new ArrayList<String>());
 				sections.add(section);
 			}
 			else {
 				ArrayList<String> properties = new ArrayList<String>();
 				String[] lines = paragraphs[i].split("\n");
 				for (int j = 0; j < lines.length; j++) {
 					if (lines[j].trim().contains("=") &&
 						!lines[j].trim().startsWith("# ")) {
 						int equalsNdx = lines[j].indexOf("=");
 						String property = lines[j].substring(0, equalsNdx);
 						if (!properties.contains(property)) {
 							properties.add(property);
 						}
 					}
 				}
 				Section section =
 					new Section(
 						new ArrayList<String>(), false, "", paragraphs[i],
 						properties, "", new ArrayList<String>());
 				sections.add(section);
 			}
 		}
 		
 		// Populate sectionProperties
 		
 		for (int i = 0; i < sections.size(); i++) {
 			if (sections.get(i).getIsSectionTitle()) {
 				for (int j = i + 1; j < sections.size(); j++) {
 					if (!sections.get(j).getProperties().isEmpty()) {
 						for (int k = 0; k < sections.get(j).getProperties().size(); k++) {
 							sections.get(i).getSectionProperties().add(
 								sections.get(j).getProperties().get(k));
 						}
 					}
 					else {
 						break;
 					}
 				}
 			}
 		}
 		
 		// Populate descriptionParagraphs
 		
 		for (int i = 0; i < sections.size(); i++) {
 			String paragraph = "";
 			List<String> descriptionParagraphs = new ArrayList<String>();
 			if (sections.get(i).getParagraph().trim().startsWith("#\n")) {
 				String[] lines =
 					sections.get(i).getParagraph().trim().split("\n", 0);
 				for (int j = 0; j < lines.length; j++) {
 					lines[j] = lines[j].trim();
 				}
 				for (int j = 0; j < lines.length; j++) {
 					if (lines[j].matches("#[\\s]+[^\\s].*")) {
 						paragraph += lines[j].substring(1);
 					}
 					else {
 						if (!paragraph.isEmpty()) {
 							descriptionParagraphs.add(paragraph.trim());
 							paragraph = "";
 						}
 					}
 				}
 			}
 			sections.get(i).setDescriptionParagraphs(descriptionParagraphs);
 		}
 		
 		// Populate propertiesParagraph
 		
 		for (int i = 0; i < sections.size(); i++) {
 			if (sections.get(i).getProperties().isEmpty()) {
 				continue;
 			}
 			String propertiesParagraph =
 				sections.get(i).getParagraph().substring(
 					sections.get(i).getParagraph().lastIndexOf("#\n") + 1);
 			sections.get(i).setPropertiesParagraph(propertiesParagraph);
 		}
 		
 		root.put("sections", sections);
 
 		// Get the Freemarker template and merge it with the data model
 		
		System.out.println("Writing " + System.getProperty("user.dir") + "/" +
			args[2] + ".html");
 		try {
 			Template temp = cfg.getTemplate("properties.ftl");
 			File propertiesHtml =
				new File(System.getProperty("user.dir") + "/" + args[2] +
					".html");
 			Writer out = new FileWriter(propertiesHtml);
 			try {
 				temp.process(root, out);
 			}
 			catch (TemplateException e) {
 				e.printStackTrace();
 			}
 			out.flush();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private static String read(File file) {
 
 		StringBuilder contents = new StringBuilder();
 		try {
 			BufferedReader input = new BufferedReader(new FileReader(file));
 			try {
 				String line = null;
 				while ((line = input.readLine()) != null) {
 					contents.append(line);
 					contents.append("\n");
 				}
 			}
 			finally {
 				input.close();
 			}
 		}
 		catch (IOException ex) {
 			ex.printStackTrace();
 		}
 		return contents.toString();
 	}
 
 }
