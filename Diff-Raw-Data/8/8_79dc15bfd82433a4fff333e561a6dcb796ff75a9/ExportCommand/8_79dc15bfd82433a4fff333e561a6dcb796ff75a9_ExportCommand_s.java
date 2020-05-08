 package commands;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import UI.UI;
 import exporter.FileExporter;
 import exporter.ListGenerator;
 
 public class ExportCommand implements Command {
 	UI ui;
 	
 	public ExportCommand(UI ui) {
 		this.ui = ui;
 	}
 	
 	@Override
 	public void execute() {
 		// TODO Auto-generated method stub
 		ExportParameters param = ui.getExportParameters();
 		
 		switch(param.type){
 			case Email: 
 				System.err.println("not implemented");
				return;
 			case File: 
 				FileExporter exp = new FileExporter(param.startDate, param.endDate);
 				try {
 					exp.export();
 				} catch (IOException e1) {
 					System.err.println("encountered IO error while exporting to a file.");
 					e1.printStackTrace();
 				}
				return;
 			case UI: 
 				// TODO: need to refactor this to remove println
 				ListGenerator listGen = new ListGenerator(param.startDate, param.endDate);
 				
 				ArrayList<String> list = listGen.getList();
 				
 				//TODO: Replace with Email/FileExporter depending on user input
 				for(String e : list) {
 					System.out.println(e);
 				}
			default: return; 
 		}		
 	}	
 }
