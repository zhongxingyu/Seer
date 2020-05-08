 package emmaforeclipse.model;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Date;
 
 
 public class HtmlPlacer {
 
 	public static void main(String[] args){
 		String s = "/Users/sheng/Desktop";
 		HtmlPlacer h = new HtmlPlacer(s);
 		boolean b = h.finishUp();
 		System.out.println(b);
 		if (b) {
 			h.updateIndexHtml();
 		}
 	}
 
 	private static final String header = "<HTML><HEAD><META CONTENT=\"text/html; charset=UTF-8\" HTTP-EQUIV=\"Content-Type\"/><TITLE>EMMA Coverage Report (generated Wed Dec 05 11:56:48 EST 2012)</TITLE><STYLE TYPE=\"text/css\"> TABLE,TD,TH {border-style:solid; border-color:black;} TD,TH {background:white;margin:0;line-height:100%;padding-left:0.5em;padding-right:0.5em;} TD {border-width:0 1px 0 0;} TH {border-width:1px 1px 1px 0;} TR TD.h {color:red;} TABLE {border-spacing:0; border-collapse:collapse;border-width:0 0 1px 1px;} P,H1,H2,H3,TH {font-family:verdana,arial,sans-serif;font-size:10pt;} TD {font-family:courier,monospace;font-size:10pt;} TABLE.hdft {border-spacing:0;border-collapse:collapse;border-style:none;} TABLE.hdft TH,TABLE.hdft TD {border-style:none;line-height:normal;} TABLE.hdft TH.tl,TABLE.hdft TD.tl {background:#6699CC;color:white;} TABLE.hdft TD.nv {background:#6633DD;color:white;} .nv A:link {color:white;} .nv A:visited {color:white;} .nv A:active {color:yellow;} TABLE.hdft A:link {color:white;} TABLE.hdft A:visited {color:white;} TABLE.hdft A:active {color:yellow;} .in {color:#356085;} TABLE.s TD {padding-left:0.25em;padding-right:0.25em;} TABLE.s TD.l {padding-left:0.25em;padding-right:0.25em;text-align:right;background:#F0F0F0;} TABLE.s TR.z TD {background:#FF9999;} TABLE.s TR.p TD {background:#FFFF88;} TABLE.s TR.c TD {background:#CCFFCC;} A:link {color:#0000EE;text-decoration:none;} A:visited {color:#0000EE;text-decoration:none;} A:hover {color:#0000EE;text-decoration:underline;} TABLE.cn {border-width:0 0 1px 0;} TABLE.s {border-width:1px 0 1px 1px;} TD.h {color:red;border-width:0 1px 0 0;} TD.f {border-width:0 1px 0 1px;} TD.hf {color:red;border-width:0 1px 0 1px;} TH.f {border-width:1px 1px 1px 1px;} TR.cis TD {background:#F0F0F0;} TR.cis TD {border-width:1px 1px 1px 0;} TR.cis TD.h {color:red;border-width:1px 1px 1px 0;} TR.cis TD.f {border-width:1px 1px 1px 1px;} TR.cis TD.hf {color:red;border-width:1px 1px 1px 1px;} TD.b {border-style:none;background:transparent;line-height:50%;}  TD.bt {border-width:1px 0 0 0;background:transparent;line-height:50%;} TR.o TD {background:#F0F0F0;}TABLE.it {border-style:none;}TABLE.it TD,TABLE.it TH {border-style:none;}</STYLE></HEAD><BODY>";
 
 
 	public void updateIndexHtml() {
 
 		String indexFileDir = testProjectDir + "report/index.html";
 		String srcFileDir = testProjectDir + "report/" + runNumber +"/coverage.html";
 
 		try {
 			
 			String newCoverageFile = readFile(srcFileDir);
 
 			File indexFile = new File(indexFileDir); 
 			if (!indexFile.exists()) {
 				indexFile.createNewFile();
 				PrintWriter pw = new PrintWriter(indexFile);
 				pw.write(newCoverageFile);
 				pw.flush();
 				pw.close();
 			
 			} else {
 				
 				String indexFileString = readFile(indexFile);
 				int start = header.length();
 				int end = newCoverageFile.indexOf("</BODY></HTML>");
 				String coverageStatsNeeded = newCoverageFile.substring(start, end);
 				PrintWriter pw = new PrintWriter(indexFile);
 				pw.write(header);
 				String temp = changePathForHref(coverageStatsNeeded);
 				pw.write(temp);
 				pw.write("<div style='height=20px;'></div>");
 				pw.write(indexFileString.substring(start));
 				pw.flush();
 				pw.close();
 
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private String changePathForHref(String s) {
 		if (s == null) return "";
 //		System.out.println(s);
 		String s1 = s.replaceAll("HREF=\"_files/", "HREF=\""+ this.runNumber +"/_files/");
 //		System.out.println(s1);
 		String s2 = s1.replaceAll("HREF=\"coverage.html", "HREF=\"" + this.runNumber + "/coverage.html");
  		return s2;
 	}
 
 	private String readFile(String fileDir) {
 		File file = new File(fileDir);
 		return readFile(file);
 	}
 	
 	private String readFile(File file) {
 		StringBuilder sb = new StringBuilder();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(file));
 			int read = 0;
			while (true) {
 				read = br.read();
				if (read == -1) break;
				else sb.append( (char) read);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return sb.toString();
 	}
 
 
 	private String testProjectDir;
 	private String runNumber;
 
 	public HtmlPlacer(String testProjectDirectory) {
 		if (testProjectDirectory.endsWith("/")) {
 			this.testProjectDir = testProjectDirectory;
 		} else {
 			this.testProjectDir = testProjectDirectory + "/";
 		}
 	}
 
 	public boolean finishUp() {
 		if (validatePath()) {
 			move();
 		}
 		return true;
 
 	}
 
 	private boolean validatePath(){
 		String reportFolderDir = testProjectDir+"report/";
 		File reportFolder = new File(reportFolderDir);  
 		if (!reportFolder.exists()) {
 			if (!reportFolder.mkdir()) {
 				return false;
 			}
 		}
 		setInfo();
 		File coverageFolder = new File(reportFolderDir + this.runNumber);
 		return coverageFolder.mkdir();
 	}
 
 	private boolean move() {
 		boolean b1 = moveFile("coverage.html");
 		boolean b2 = moveFile("_files");
 		//optional move
 		moveFile("coverage.xml");
 		moveFile("coverage.txt");
 		return b1 && b2; 
 	}
 
 	private boolean moveFile(String sourceFile) {
 		File src  = new File(testProjectDir + "bin/" + sourceFile); 
 		File dest = new File(testProjectDir + "report/" + this.runNumber + "/" + sourceFile);
 		if (!src.exists()) return false;
 		return src.renameTo(dest);
 	}
 
 	//should be override, info should be extracted from html generated
 	private void setInfo() {
 		runNumber = new Date().toString();
 	}
 
 
 
 
 
 
 
 
 }
