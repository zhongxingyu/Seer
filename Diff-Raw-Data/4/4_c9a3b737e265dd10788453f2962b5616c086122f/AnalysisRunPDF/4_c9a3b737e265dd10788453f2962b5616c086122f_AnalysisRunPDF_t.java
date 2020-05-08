 package pals.actions.user;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import pals.Globals;
 import pals.actions.UserAwareAction;
 import pals.entity.Analysis;
 import pals.entity.DataSet;
 import pals.utils.PalsFileUtils;
 
 
 public class AnalysisRunPDF extends UserAwareAction {
 
 	Integer analysisRunId;
 	private InputStream inputStream;
 	public boolean bench;
 
 	public Integer getAnalysisRunId() {
 		return analysisRunId;
 	}
 
 	public void setAnalysisRunId(Integer analysisRunId) {
 		this.analysisRunId = analysisRunId;
 	}
 	
 	public Analysis getAnalysisRun() {
 		return getUserService().getAnalysisRun(getUser(), analysisRunId);
 	}
 	
 	public InputStream getInputStream() {
 		return inputStream;
 	}
 
 	public void setInputStream(InputStream inputStream) {
 		this.inputStream = inputStream;
 	}
 
 	public String getContentDisposition() {
 		Analysis analRun = getAnalysisRun();
		String fileName = analRun.getAnalysable().getName() + ":" + analRun.getAnalysisType().getName();
		if( bench ) fileName += "_bench";
		fileName += Globals.PDF_FILE_SUFFIX;
 		return "filename=\"" + fileName + "\"";
 	}
 	
 	public String execute() {
 		Analysis analRun = getAnalysisRun();
 		File pdfFile = null;
 		if( isBench() )
 		{
 			pdfFile = PalsFileUtils.getAnalysisRunFileBenchPDF(analRun);
 		}
 		else
 		{
 			pdfFile = PalsFileUtils.getAnalysisRunFilePDF(analRun);
 		}
 		//System.out.println("pdfFile is......:" + pdfFile.getAbsolutePath());
 		try {
 			inputStream = new FileInputStream(pdfFile);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return SUCCESS;
 	}
 
 	public boolean isBench() {
 		return bench;
 	}
 
 	public void setBench(boolean bench) {
 		this.bench = bench;
 	}
 	
 }
