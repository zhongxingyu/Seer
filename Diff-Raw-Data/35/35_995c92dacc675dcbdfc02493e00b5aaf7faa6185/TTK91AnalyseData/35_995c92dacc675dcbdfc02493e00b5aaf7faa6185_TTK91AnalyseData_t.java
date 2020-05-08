 package fi.helsinki.cs.koskelo.analyser;
 
 import fi.hu.cs.ttk91.TTK91CompileSource;
 import fi.hu.cs.ttk91.TTK91Cpu;
 import fi.hu.cs.ttk91.TTK91Memory;
 import fi.hu.cs.ttk91.TTK91Exception;
 import fi.hu.cs.ttk91.TTK91Application;
 import fi.hu.cs.ttk91.TTK91Core;
 
 
 import fi.hu.cs.titokone.Source;
 import fi.hu.cs.titokone.Control;
 import fi.hu.cs.titokone.Processor;
 import fi.hu.cs.titokone.RandomAccessMemory;
 import fi.hu.cs.titokone.MemoryLine;
 import fi.helsinki.cs.koskelo.common.TTK91TaskOptions;
 import fi.helsinki.cs.koskelo.common.TTK91Constant;
 
 public class TTK91AnalyseData{
 
 
 	// Controllit joilta saadaan dataa:
 
 	private TTK91Core controlCompiler = null;    
 	// kntj -> saadaan mahdolliset knnsvirheet ntimmin (kunhan ajetaan malli ensin... 
 	// Jos se ei knny, ei varmaan ole tarvetta knt opiskelijankaan ratkaisua...
 	private TTK91Core controlPublicInputStudent = null;
 	// publicinputeilla tai ilman unputteja opiskelijan vastaus
 	private TTK91Core controlPublicInputTeacher = null; 
 	// publicinputeilla tai ilman inputteja malliratkaisu jos vertailu on mritelty simuloitavaksi
 	private TTK91Core controlHiddenInputStudent = null;
 	// hiddeninputeilla jos ovat mritelty opiskelijan vastaus
 	private TTK91Core controlHiddenInputTeacher = null;
 	// hiddeninputeilla jos ovat mritelty malliratkaisu jos vertailu on mritelty simuloitavaksi
 
 	// Tehtvnmrittelydata
 	private TTK91TaskOptions taskOptions = null;        // taskoptions
 
 	// Malliratkaisu
 
 	private String exampleCode = null;                // haetaan taskoptionsista - malliratkaisun koodi
 	private String[] answer = null;
 
 	// Virheilmoitukset

	private boolean errors = false;
 	private String studentCompileError = null;
 	private String teacherCompileError = null;
 	private String studentRunError = null;
 	private String teacherRunError = null;
 
 	// Knnetyt koodit
 	private TTK91Application studentApplication = null; // opiskelijan vastaus
 	private TTK91Application teacherApplication = null; // malliratkaisu
 
 	// Analysointitapa
 	private int compareMethod = -1;
 
 
 	// Optionsin muuttujia:
 
 	private int steps = 0;
 	private String publicInput = null;
 	private String hiddenInput = null;
 
 
 	// Titokoneiden muistit:
 
 	private    RandomAccessMemory studentPublicMemory = null; 
 	private    RandomAccessMemory teacherPublicMemory = null; 
 	private    RandomAccessMemory studentHiddenMemory = null; 
 	private    RandomAccessMemory teacherHiddenMemory = null;
 
 
 	public TTK91AnalyseData(
 			String[] answer, 
 			TTK91TaskOptions taskOptions
 			) {
 
 		this.taskOptions = taskOptions;
 		this.answer = answer;
 
 		compileTeacherApplication();
 		compileStudentApplication();
 		getTaskData();
		if(!this.errors) {
 		run();
		}
 	}
 
 
 	
 	private void compileTeacherApplication() {
 
 
 		// compile teacherapp
 		
 		TTK91CompileSource src = null;
 		
 		if(exampleCode == null) {
 			return; // FIXME: virheenksittely
 		}
 		
 		
 		src = (TTK91CompileSource) new Source(exampleCode);
 
 		if (src == null) {
 			this.teacherCompileError = "Malliratkaisua ei pystytty"+
 					" muuntamaan TTK91CompileSource-muotoon";
 		}//if
 
 
 		TTK91Application app = null;
 
 		if (controlCompiler == null) {
 			controlCompiler = new Control(null, null);
 		}
 
 		try {
 			app = controlCompiler.compile(src);
 		} catch (TTK91Exception e) {
 			this.teacherCompileError = e.getMessage();
			this.errors = true;
 			return;
 		}//catch
 
 		this.teacherApplication = app;
 
 
 		// get the student app and the
 		// teacher app and make them into applications
 
 	}
 	private void compileStudentApplication() {
 
 
 		// compile studentapp
 		
 		TTK91CompileSource src = null;
 
 		if (this.answer != null) {
 			String ans = this.answer[0];
 			ans = ans + "\n SVC SP, =HALT";
 			src = (TTK91CompileSource) new Source(ans);
 			// FIXME: toimiiko tosiaan nin helposti?
 		}
 
 		if (src == null) {
 			this.studentCompileError = "Ratkaisua ei pystytty"+
 					" muuntamaan TTK91CompileSource-muotoon";
 		}//if
 
 
 		TTK91Application app = null;
 
 		if (controlCompiler == null) {
 			controlCompiler = new Control(null, null);
 		}
 
 		try {
 			app = controlCompiler.compile(src);
 		} catch (TTK91Exception e) {
 			this.studentCompileError = e.getMessage();
			this.errors = true;
 			return;
 		}//catch
 
 		this.studentApplication = app;
 
 
 		// get the student app and the
 		// teacher app and make them into applications
 
 	}
 
 	private void getTaskData() {
 		this.publicInput = parseInputString(
 				this.taskOptions.getPublicInput()
 				);
 		this.hiddenInput = parseInputString(
 				this.taskOptions.getHiddenInput()
 				);
 
 		this.steps = taskOptions.getMaxCommands();
 
 		this.compareMethod = taskOptions.getCompareMethod();
 	}
 
 
 
 	private void run() {
 
 
 		/* Koska titokoneesta metodilla .getCPU() saadaan
 		 * vain viite controlin sisiseen prosessiin kytetn
 		 * yhteens maksissaan nelj controlia, kuitenkin
 		 * siten, ett kullekin simulointikierrokselle
 		 * luodaan oma controlinsa.
 		 */
 
 		if(publicInput != null) {
 
 			this.studentApplication.setKbd(publicInput);
 			if(compareMethod == taskOptions.COMPARE_TO_SIMULATED) {
 				this.teacherApplication.setKbd(publicInput);
 			}
 		}
 
 		this.controlPublicInputStudent = new Control(null, null);
 
 		try {
 			this.controlPublicInputStudent.run(this.studentApplication, steps);
 			// 1. simulointi
 		} catch (TTK91Exception e) {
 			this.studentRunError = e.getMessage();
			this.errors = true;
 			return;
 		}
 
 		if(compareMethod == TTK91Constant.COMPARE_TO_SIMULATED) {
 			// 1. simulointi malliratkaisua
 			this.controlPublicInputTeacher = new Control(null, null);
 			try {
 				this.controlPublicInputTeacher.run(this.teacherApplication, steps);
 			} catch (TTK91Exception e) {
 				this.teacherRunError =  e.getMessage();
				this.errors = true;
 				return;
 			}
 		}
 
 
 		if (hiddenInput != null) {
 			// mahdollinen 2. simulointi opiskelijan ratkaisusta
 
 			this.controlHiddenInputStudent = new Control(null, null); // luodaan control vain jos hiddeninput mritelty --> "optimointia"
 			this.studentApplication.setKbd(hiddenInput);
 
 			try {
 				this.controlHiddenInputStudent.run(this.studentApplication, steps);
 			} catch (TTK91Exception e) {
 				this.studentRunError = e.getMessage();
				this.errors = true;
 				return;
 			}
 
 			if(compareMethod == TTK91Constant.COMPARE_TO_SIMULATED) {
 				// simuloidaa malliratkaisu
 				// 2. simulointi malliratkaisua
 
 				this.controlHiddenInputTeacher = new Control(null, null); // luodaan control vain jos hiddeninput mritelty --> "optimointia"
 				this.teacherApplication.setKbd(hiddenInput);
 				try {
 					this.controlHiddenInputTeacher.run(this.teacherApplication, steps);
 				} catch (TTK91Exception e) {
 					teacherRunError = e.getMessage();
					this.errors = true;
 					return;
 				}
 			}
 		} 
 
 
 
 		// Aja ohjelmat.
 		// Aseta muisti,
 		// Aseta rekisterit,
 		// Aseta tulosteet.
 
 	}
 
 	private String parseInputString(int[] inputTable) {
 
 		String input = "";
 
 		for(int i = 0; i < inputTable.length; i++) {
 			input = input + inputTable[i];
 		}
 
 		if(input.equals("")){
 			return null;
 		} else {
 			return input;
 		}
 	}
 
 	/* Tarvitaan viel: sopivat getterit */
 
 	public String getStudentCompileError() {
		return this.studentCompileError;
 	}
 	
 	public String getTeacherCompileError() {
		return this.teacherCompileError;
 	}
 	
 	public String getStudentRunError() {
		return this.studentRunError;
 	}
 	
 	public String getTeacherRunError() {
		return this.teacherRunError;
 	}
 
	public boolean Errors() {
		return this.errors;
	}
 
 }// class
