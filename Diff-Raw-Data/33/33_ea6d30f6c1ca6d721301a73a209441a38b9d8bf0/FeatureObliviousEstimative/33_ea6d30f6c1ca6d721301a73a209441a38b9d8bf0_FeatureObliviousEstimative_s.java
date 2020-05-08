 //#ifdef METRICS
 package br.ufal.cideei.soot.count;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import profiling.ProfilingTag;
 import soot.Body;
 import soot.BodyTransformer;
 import br.ufal.cideei.soot.instrument.FeatureTag;
 
 public class FeatureObliviousEstimative extends BodyTransformer {
 
 	private static FeatureObliviousEstimative instance = null;
 
 	private long rdTotal = 0;
 	private long uvTotal = 0;
 	private long jimplificationTotal = 0;
 	private long preprocessingTotal = 0;
 
 	private Workbook workBook = null;
 	private FileOutputStream fileOut = null;
 	private String sheetName = "fo-metrics";
 	private final short CLASS_COL = 0;
 	private final short METHOD_COL = 1;
 	private final short CONFIGS_COL = 2;
 	private final short RD_COL = 3;
 	private final short UV_COL = 4;
 	private final short PP_COL = 5;
 	private final short JIMPLIFICATION_COL = 6;
 	private final short UNITS_COL = 7;
 
 	private int methodCount = 1;
 	private int times = 0;
 
 	public static FeatureObliviousEstimative v() {
 		if (instance == null)
 			instance = new FeatureObliviousEstimative();
 		return instance;
 	}
 
 	private FeatureObliviousEstimative() {
 		openMetricsFile();
 		createNewSheet();
 	}
 
 	@Override
 	protected void internalTransform(Body body, String phase, Map opt) {
 		methodCount++;
 
 		FeatureTag featureTag = (FeatureTag) body.getTag("FeatureTag");
 		ProfilingTag profilingTag = (ProfilingTag) body.getTag("ProfilingTag");
 
 		int size = featureTag.size();
 		long rdAnalysisTime = profilingTag.getRdAnalysisTime();
 		long uvAnalysisTime = profilingTag.getUvAnalysisTime();
 		long jimplificationTime = profilingTag.getJimplificationTime();
 		long preprocessingTime = profilingTag.getPreprocessingTime();
 
		long numberOfConfigurations = (long) (Math.log(size) / Math.log(2));

 		this.rdTotal += rdAnalysisTime;
 		this.uvTotal += uvAnalysisTime;
 		this.preprocessingTotal += preprocessingTime;
 
 		// if contains color
 		if (size > 1) {
			this.jimplificationTotal += (numberOfConfigurations * jimplificationTime);
 		} else {
 			this.jimplificationTotal += jimplificationTime;
 		}
 
 		// write row to excel file
 		{
 			Sheet sheet = workBook.getSheet(sheetName + "-" + times);
 
 			Row methodRow = sheet.createRow(methodCount);
 			methodRow.createCell(this.CLASS_COL).setCellValue(body.getMethod().getDeclaringClass().getName());
 			methodRow.createCell(this.METHOD_COL).setCellValue(body.getMethod().getName());
 			methodRow.createCell(this.CONFIGS_COL).setCellValue(size);
 			methodRow.createCell(this.UNITS_COL).setCellValue(body.getUnits().size());
 
 			methodRow.createCell(this.RD_COL).setCellValue(rdAnalysisTime);
 			methodRow.createCell(this.UV_COL).setCellValue(uvAnalysisTime);
 			methodRow.createCell(this.PP_COL).setCellValue(preprocessingTime);
 			
 			if (size > 1) {
				methodRow.createCell(this.JIMPLIFICATION_COL).setCellValue(numberOfConfigurations * jimplificationTime);
 			} else {
 				methodRow.createCell(this.JIMPLIFICATION_COL).setCellValue(jimplificationTime);
 			}
 		}
 	}
 
 	public long getRdTotal() {
 		return rdTotal;
 	}
 
 	public long getUvTotal() {
 		return uvTotal;
 	}
 
 	public long getJimplificationTotal() {
 		return jimplificationTotal;
 	}
 
 	public long getPreprocessingTotal() {
 		return preprocessingTotal;
 	}
 
 	public void reset() {
 		this.rdTotal = 0;
 		this.uvTotal = 0;
 		this.jimplificationTotal = 0;
 		this.preprocessingTotal = 0;
 
 		createNewSheet();
 	}
 
 	private void openMetricsFile() {
 		methodCount = 1;
 		workBook = new XSSFWorkbook();
 
 		String workBookFilePath = "fo-metrics.xls";
 		File file = new File(workBookFilePath);
 		try {
 			fileOut = new FileOutputStream(file);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void createNewSheet() {
 		methodCount = 1;
 		times++;
 
 		Sheet sheet = workBook.createSheet(sheetName + "-" + times);
 
 		Row headerRow = sheet.createRow((short) 0);
 
 		headerRow.createCell(this.CLASS_COL).setCellValue("Class");
 		headerRow.createCell(this.METHOD_COL).setCellValue("Method");
 		headerRow.createCell(this.CONFIGS_COL).setCellValue("No. Configs");
 		headerRow.createCell(this.RD_COL).setCellValue("RD");
 		headerRow.createCell(this.UV_COL).setCellValue("UV");
 		headerRow.createCell(this.PP_COL).setCellValue("PP");
 		headerRow.createCell(this.JIMPLIFICATION_COL).setCellValue("j12n");
 		headerRow.createCell(this.UNITS_COL).setCellValue("Units");
 	}
 
 	public void closeMetricsFile() {
 		try {
 			workBook.write(fileOut);
 			fileOut.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
 //#endif
