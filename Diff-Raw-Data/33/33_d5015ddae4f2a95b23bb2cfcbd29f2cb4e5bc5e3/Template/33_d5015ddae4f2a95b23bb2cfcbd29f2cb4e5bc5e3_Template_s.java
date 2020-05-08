 /*
  *
  * Copyright (C) 2011 SW 11 Inc.
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package models;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import play.db.jpa.*;
 import play.data.validation.*;
 
 import java.util.*;
 import javax.persistence.*;
 import java.util.Map;
 import org.apache.commons.collections.MultiHashMap;
 //import org.apache.commons.collections.map.MultiValueMap;
 import utils.io.FileStringReader;
 import play.Play;
 import utils.Substitution;
 import utils.io.FileStringWriter;
 import utils.Zip;
 
 @Entity
 public class Template extends Model
 {
 
 //<<<<<<< HEAD
     @Lob
     @Required
   public String name_;
     @Lob
     @Required
   public String filename_;
     @Lob
   public String author_;
   public Date dateCreated_;
     @Lob
   public String description_;
   
   public int counterDownloads_;
   @Lob
   public HashMap templates_ = new HashMap<String, String>();
   
   public MultiHashMap labels_ = new MultiHashMap();
   
   @Lob
   public String textFile;
   
   public String documentPath;
   
   public String pathToFilledFile;
           
   public String userRegistered;
           
   public Boolean isHidden;
   
   
 
     public Template(String name_, String filename_, String author_, Date dateCreated_, String description_, int counterDownloads_)
     {
         this.name_ = name_;
         this.filename_ = filename_;
         this.author_ = author_;
         this.dateCreated_ = dateCreated_;
         this.description_ = description_;
         this.counterDownloads_ = counterDownloads_;
         this.pathToFilledFile = null;
         this.userRegistered = null;
         this.isHidden = false;
 
 
     }
 
 	 public void parsePlaceholder(File file) throws FileNotFoundException, IOException
 	{
 		FileStringReader reader = new FileStringReader(file);
 		String content = reader.read();
 		textFile = content;
 
 Set<String> commands = new TreeSet<String>();
 
         String[] commands_temp = this.textFile.split("%%");
 
 
         for (int i = 1; i < commands_temp.length; i += 2)
         {
             commands.add(commands_temp[i]);
         }
 
         Iterator iterator = commands.iterator();
 
         while (iterator.hasNext())
         {
             String command = (String) iterator.next();
             templates_.put(command, "");
 
             if(command.contains(":"))
             {
                 String[] command_label = command.split(":");
                 labels_.put(command_label[0], command_label[1]);
             }
             else
             {
                 labels_.put("0",command);
             }
         }
 	}
 
 	public static boolean deleteDir(File dir)
 	{
 		if (dir.isDirectory())
 		{
 			String[] children = dir.list();
 			for (int i = 0; i < children.length; i++)
 			{
 				boolean success = deleteDir(new File(dir, children[i]));
 				if (!success)
 				{
 					return false;
 				}
 			}
 		}
 
 		return dir.delete();
 	}
 
 	public void calculateForm()
 	{
 		String templatePath = Play.applicationPath.getAbsolutePath() + "/public/templates/";
 		String templateFile = templatePath + filename_;
 		// DOCX Blasdoidfoie
 		try
 		{
 			String extension = filename_.substring(filename_.lastIndexOf(".") + 1);
 			if (extension.equals("docx"))
 			{
 				Zip zip = new Zip();
 				zip.unzip(templateFile, templatePath);
 
 				String docxContent = templatePath + filename_.replace("." + extension, "/") + "word";
 				File file = new File(docxContent);
 				for (File currentFile : file.listFiles())
 				{
 					if (currentFile.isDirectory())
 					{
 						continue;
 					}
 					parsePlaceholder(currentFile);
 				}
 				File templateFolder = new File(templatePath + filename_.replace("." + extension, ""));
 				deleteDir(templateFolder);
 			}
 			else
 			{
 				parsePlaceholder(new File(Play.applicationPath.getAbsolutePath() + "/public/templates/" + filename_));
 			}
 		}
 		catch (Exception exception)
 		{
 			exception.printStackTrace();
 		}
 
 	}
 
     public static String upload(String name, String description, File template, String userRegistered, Boolean isHidden)
     {   
         try
         {
             FileStringReader reader = new FileStringReader(template);
             String text = reader.read();
             
 
             if(!Helper.isUtf8(text))
             {
                 return "File must be in Plaintext (UTF 8).";
             }
             
             String author = userRegistered;
             
             
             Date now = new Date();
             Template temp = new Template(name, template.getName(), author, now, description, 4);
             temp.userRegistered = userRegistered;
             temp.isHidden = isHidden;
             temp.save();
 
             int dotPos = template.getName().lastIndexOf(".");
             String newName;
             String extension = null;
 
             if (dotPos != -1)
             {
                 extension = template.getName().substring(dotPos);
                 newName = temp.id + "_" + name + extension;
             }
             else
             {
                 newName = temp.id + "_" + name;
             }
 
 
             File copy_to = new File(Play.applicationPath.getAbsolutePath()+"/public/templates/" + newName);
 
             //System.out.println(copy_to.getAbsolutePath());
             Helper.copy(template, copy_to);
 
             temp.filename_ = newName;
             temp.calculateForm();
             temp.save();
             
             Helper helper = new Helper();
             if(!extension.equals(".tex"))
             {
                 helper.templateToImage(temp);
             }
             else
             {
                 Substitution sub = new Substitution(temp.textFile);
                 Map map = new HashMap(temp.templates_);
                 Iterator it = map.keySet().iterator();
                 
                 while(it.hasNext())
                 {
                     String key = (String) it.next();                    
                     map.put(key, key);
                 }
                 sub.replace(map);
                 File replaced_file = new File(Play.applicationPath.getAbsolutePath()+"/public/tmp/"+temp.filename_);
                 File destination = new File(replaced_file.getParent());
                 FileStringWriter writer = new FileStringWriter(replaced_file);
                 
                 writer.write(sub.getText());
                 
                 helper.texToPdf(replaced_file, destination);
                 
                 String[] source_name = temp.filename_.split(".tex");
                 
                 File source = new File(destination+"/"+source_name[0]+".pdf");
                 destination = new File(Play.applicationPath.getAbsolutePath()+"/template/"+source_name[0]+".pdf.jpg");
                 
                 helper.pdfToImage(source, destination);
                 
                 
             }
 
             return null;
         }
         catch (Exception e)
         {
             System.out.println(e.toString());
             return e.toString();
         }
     }
     
     //this.textFile = null;
 
     public static void delete(long id)
     {
         Template temp = Template.find("id", id).first();
 
         if (temp != null)
         {
             temp.delete();
         }
     }
 
     public void addCommand(String command)
     {
         templates_.put(command, "");
     }
 
     public void addSubstitution(String key, String userInput)
     {
         templates_.put(key, userInput);
     }
 
     public String getValue(String command)
     {
         return templates_.get(command).toString();
     }
 
     @Override
     public String toString()
     {
         return this.name_;
     }
 
     public HashMap getTemplates_()
     {
         return templates_;
     }
 
     public void doMap(Map<String, String[]> map)
     {
         Iterator mapIterator = map.keySet().iterator();
         while (mapIterator.hasNext())
         {
             String temp = (String) mapIterator.next();
             if (this.templates_.containsKey(temp))
             {
                 this.addSubstitution(temp, map.get(temp)[0]);
             }
         }
     }
     
     
     public String getImagePath()
     {
         return "/public/templates/"+filename_+".jpg";
     }
     
     public String pathToFilledFileImage()
     {       
         
         return pathToFilledFile+".jpg";
     }
 
 }
