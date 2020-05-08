 /*
  * Copyright (c) 2012 Chris Ellison, Mike Deats, Liron Yahdav, Ryan Neal,
  * Brandon Sutherlin, Scott Griffin
  * 
  * This software is released under the MIT license
  * (http://www.opensource.org/licenses/mit-license.php)
  * 
  * Created on Feb 18, 2012
  */
 package edu.cmu.sv.arinc838.ui.item;
 
 import edu.cmu.sv.arinc838.builder.SoftwareDefinitionFileBuilder;
 import edu.cmu.sv.arinc838.writer.SdfWriter;
 
 public class SaveItem extends AbstractMenuItem {
 	private SdfWriter writer;
 
 	public SaveItem(String prompt, SdfWriter writer) {
 		super(prompt);
 		this.writer = writer;
 	}
 
 	@Override
 	public MenuItem[] execute(SoftwareDefinitionFileBuilder builder)
 			throws Exception {

 		String path = promptForResponse("Save where? (path only, filename will be appended)");
 		String fileName = this.writer.write(path, builder);
 
 		System.out.println("File saved to " + fileName);
 
 		return super.getEmptyItems();
 	}
 }
