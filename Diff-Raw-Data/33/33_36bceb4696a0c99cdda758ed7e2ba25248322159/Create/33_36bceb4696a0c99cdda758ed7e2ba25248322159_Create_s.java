 package excel;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import excel.sheet.Settings;
 import excel.sheet.cell.Formulas;
 
 public class Create {
 	
 	
 	public static void main(String[] _) throws IOException {
 		//System.out.println(Settings.dateColumn());
 		//String workbook = Excel.DEFAULT_PATH + Excel.DEFAULT_WORKBOOK_NAME;
 		//System.out.println(workbook);
 		
 		//System.out.println(Excel.rowToLetter(27));
 		//System.out.println(Excel.rowToLetter(26));
 		//System.out.println(Excel.rowToLetter(25));
 		//System.out.println(Excel.rowToLetter(28));
 		//System.out.println(Excel.rowToLetter(100));
 		
 		/*
 		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		int r = fc.showOpenDialog(null);
		String workbook = (r == JFileChooser.CANCEL_OPTION ? Excel.DEFAULT_PATH : fc.getSelectedFile().getAbsolutePath()) + Excel.DEFAULT_WORKBOOK_NAME;
		System.out.println(workbook);
 		*/
 		String workbook = "C:\\My_Data\\" + Excel.DEFAULT_WORKBOOK_NAME;
 		//String workbook = Excel.DEFAULT_PATH + Excel.DEFAULT_WORKBOOK_NAME;
 		Excel.writeToExcel(createWorkbook(), workbook);
 		Desktop.getDesktop().open(new File(workbook));
 		
 		
 		/*
 		
 		if(!wb.exists()) {
 			Excel.writeToExcel(createWorkbook());
 		}
 		try {
 			Workbook w = Excel.readExcel(wb);
 			Intake.updateSheet(w);
 			Excel.writeToExcel(w);
 			Desktop.getDesktop().open(wb);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		*/
 	}
 	
 	private static Workbook createWorkbook() {
 		Workbook wb = new XSSFWorkbook();
 		Settings s = new Settings(wb);
 		s.createSettingsSheet();
 		//Intake i = new Intake(wb);
 		//i.createIntakeSheet();
 		//StoredData sd = new StoredData(wb);
 		//sd.createStoredDataSheet();
 		return wb;
 	}
 	
 }
