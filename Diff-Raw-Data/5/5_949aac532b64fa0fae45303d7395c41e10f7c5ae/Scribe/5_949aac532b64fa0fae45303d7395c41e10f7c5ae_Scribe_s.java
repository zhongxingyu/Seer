 package gutenberg.workers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 
 import gutenberg.blocs.AssignmentType;
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PageType;
 import gutenberg.blocs.QuizType;
 import gutenberg.blocs.StudentType;
 
 public class Scribe {
 
 	public Scribe(String mint, String webroot) throws Exception {
 		MINT = mint;
 		this.webroot = webroot;
 	}
 
 	public void setVault(Vault vault) {
 		this.vault = vault;
 	}
 
 	/**
 	 * creates an answer key pdf for a quiz and preview jpgs
 	 * 
 	 * @param quiz
 	 * @throws Exception
 	 */
 	public void generate(QuizType quiz) throws Exception {
 		String quizId = quiz.getQuiz().getId();
 		quizDir = new File(MINT + "/" + quizId);
 		staging = new File(quizDir + "/staging");
 		if (!quizDir.exists()) {
 			quizDir.mkdir();
 			staging.mkdir();
 		}
 
 		PrintWriter answerkey = new PrintWriter(staging + "/answer-key.tex");
 		writePreamble(answerkey, quiz.getSchool().getName(), quiz.getTeacher().getName());
 		beginDoc(answerkey);
 		answerkey.println(printanswers);
 
 		PageType[] pages = quiz.getPage();
 		PrintWriter preview = null;
 		String page = null;
 		for (int i = 0; i < pages.length; i++) {
 			preview = new PrintWriter(staging + "/page-" + i + ".tex");
 			writePreamble(preview, quiz.getSchool().getName(), quiz.getTeacher().getName());
 			beginDoc(preview);
 			preview.println(printanswers);
 			
 			page = preparePage(pages[i]);
 			preview.println(page);
 			answerkey.println(page);
 			endDoc(preview);
 			preview.close();
 			if (i < pages.length - 1) {
 				answerkey.println(newpage);
 			}
 		}
 		endDoc(answerkey);
 		answerkey.close();
 
 		System.out.println("Return Code: " + make(quizId));
 		prepareManifest(quiz);
 	}
 
 	/**
 	 * create an assignment
 	 * 
 	 * @param assignment
 	 * @throws Exception
 	 */
 	public void generate(AssignmentType assignment) throws Exception {
 		String quizId = assignment.getQuiz().getId();
 		String instanceId = assignment.getInstance().getId(); 
 		quizDir = new File(MINT + "/" + quizId);		
 		if (!quizDir.exists()) {
 			throw new Exception("Cannot assign non-existant quiz: "
 					+ quizId);
 		} else {
 			instanceDir = new File(quizDir + "/" + instanceId);
 			instanceDir.mkdir();
 			//Note: staging directory is different from quizDir/staging
 			//this is quizDir/instanceDir/staging
 			staging = new File(instanceDir + "/staging");
 			staging.mkdir();
 			referenceQuizResources(quizId);
 		}
 
 		int totalPages = quizDir.list(new NameFilter("page")).length;
 
 		PrintWriter composite = new PrintWriter(staging + "/assignment.tex");
 		PrintWriter individual = null;
 
 		EntryType[] students = assignment.getStudents();
 		for (int i = 0; i < students.length; i++) {
 
 			individual = new PrintWriter(staging + "/" + students[i].getId()
 					+ "-assignment.tex");
 			int pageNumber = 1, questionNumber = 1;
 
 			String line = null, text = null;
 			BufferedReader reader = new BufferedReader(new FileReader(quizDir + "/staging/answer-key.tex"));
 			while ((line = reader.readLine()) != null) {
 
 				text = line.trim();
 				if (text.startsWith(newpage)) {
 					pageNumber++;
 				}
 
 				if (text.startsWith(question)) {
 					questionNumber++;
 				}
 					
 				if (text.startsWith(insertQR)) {
 					line = line.replace("QRC", String.format("%s.%s.%s.%s.%s.%s",
 							quizId, pageNumber, totalPages, questionNumber,
 							students[i].getId(), students[i].getName()));
 				}
 
 				if (text.startsWith(docAuthor)) {
 					line = String.format(docAuthor + "{%s}",
 							students[i].getName());
 				}
 
 				if (text.startsWith(printanswers)) {
 					line = "";
 				}
 
 				individual.println(line);
 				// docBegin and docEnd print only once for composite document
 				if (line.startsWith(beginDocument) && i != 0) {
 					line = "";
 				}
 				if (line.startsWith(endDocument) && i != students.length - 1) {
 					line = "";
 				}
 				composite.println(line);
 
 			}
 			
 		      // We are not yet done with the composite document. So, do the following
 		      resetPageNumbering(composite);
 		      resetQuestionNumbering(composite);
 		      
 			endDoc(individual);
 			individual.close();
 			composite.flush();			
 		}
 		endDoc(composite);
 		composite.close();
 
		//System.out.println("Return Code: " + make(quizId + "/" + instanceId));
 		prepareManifest(assignment);
 	}
 
 	
 	public ManifestType getManifest() {
 		return manifest;
 	}
 
 	private String MINT;
 	private File staging, quizDir, instanceDir;
 	private String webroot;
 	private final String printanswers = "\\printanswers",
 			docAuthor = "\\DocAuthor", newpage = "\\newpage",
 			question = "\\question", beginDocument = "\\begin{document}",
 			beginQuestions = "\\begin{questions}", insertQR = "\\insertQR{QRC}",
 			endQuestions = "\\end{questions}", endDocument = "\\end{document}";
 	private Vault vault;
 	private ManifestType manifest;
 
 	private String preparePage(PageType page) throws Exception {
 		StringBuilder contents = new StringBuilder();
 		EntryType[] questionIds = page.getQuestion();
 		String question = null;
 		File[] resources = null;
 		for (int i = 0; i < questionIds.length; i++) {
 			question = vault.getContent(questionIds[i].getId(), "question.tex")[0];
 			contents.append(insertQR).append('\n');
 			contents.append(question);
 			resources = vault
 					.getFiles(questionIds[i].getId(), "figure.gnuplot");
 			linkResources(resources[0], questionIds[i].getId());
 		}
 		return contents.toString();
 	}
 
 	private void linkResources(File resource, String plotId) throws Exception {
 		File target = new File(staging.getPath() + "/" + plotId + ".gnuplot");
 		if (!target.exists())
 			Files.createSymbolicLink(target.toPath(), resource.toPath());
 	}
 	
 	private void referenceQuizResources(String quizId) throws IOException {
 		File[] files = (new File(quizDir + "/staging")).listFiles(new NameFilter("gnuplot"));
 		for (int i = 0; i < files.length; i++) {
			Files.createSymbolicLink(files[i].toPath(), new File(staging + files[i].getName()).toPath());
 		}
 	}
 
 
 	private void writePreamble(PrintWriter writer, String school, String author)
 			throws Exception {
 		writer.println("\\documentclass[justified]{tufte-exam}");
 		writer.println("\\School{" + school + "}");
 		writer.println("\\DocAuthor{" + author + "}");
 		writer.println("\\fancyfoot[C]{\\copyright\\, Gutenberg}");
 	}
 
 	private void beginDoc(PrintWriter writer) throws Exception {
 		writer.println(beginDocument);
 		writer.println(beginQuestions);
 	}
 
 	private void endDoc(PrintWriter writer) throws Exception {
 		writer.println(endQuestions);
 		writer.println(endDocument);
 	}
 
 	private void resetPageNumbering(PrintWriter writer) throws Exception {
 		writer.println("\\setcounter{page}{1}");
 	}
 
 	private void resetQuestionNumbering(PrintWriter writer) throws Exception {
 		writer.println("\\setcounter{question}{0}");
 	}
 	
 	private int make(String dir) throws Exception {
 		ProcessBuilder processBuilder = new ProcessBuilder("make", "dir="
 				+ dir);
 		File mintDir = new File(MINT);
 		processBuilder.directory(mintDir);
 		processBuilder.redirectErrorStream(true);
 		Process process = processBuilder.start();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				process.getInputStream()));
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			System.out.println(line);
 		}
 		return process.waitFor();
 	}
 
 	private void prepareManifest(QuizType quiz) throws Exception {
 		manifest = new ManifestType();
 		manifest.setRoot(webroot + quizDir.getName());
 		int pages = quiz.getPage().length;
 		EntryType[] images = new EntryType[pages * 2];
 		String filename = null;
 		for (int i = 0; i < pages; i++) {
 			filename = "page-" + i + ".jpg";
 			if (new File(quizDir.getPath() + filename).exists()) {
 				images[2 * i] = new EntryType();
 				images[2 * i].setId(filename);
 			} else {
 				throw new Exception(filename
 						+ " missing! All files not prepared.");
 			}
 			filename = "thumb" + i + ".jpg";
 			if (new File(quizDir.getPath() + filename).exists()) {
 				images[2 * i + 1] = new EntryType();
 				images[2 * i + 1].setId(filename);
 			} else {
 				throw new Exception(filename
 						+ " missing! All Quiz files not prepared.");
 			}
 		}
 		manifest.setImage(images);
 		EntryType[] documents = new EntryType[1];
 		documents[0] = new EntryType();
 		documents[0].setId("answerkey.pdf");
 		manifest.setDocument(documents);
 	}
 
 	private void prepareManifest(AssignmentType assignment) throws Exception {
 		manifest = new ManifestType();
 		manifest.setRoot(webroot + quizDir.getName() + "/" + instanceDir.getName());
 		EntryType[] students = assignment.getStudents();
 		EntryType[] documents = new EntryType[students.length + 1];
 		String filename = null;
 		for (int i = 0; i < students.length; i++) {
 			filename = instanceDir.getPath() + "/" + students[i].getId()
 					+ "-assignment.jpg";
 			if (new File(filename).exists()) {
 				documents[i] = new EntryType();
 				documents[i].setId(filename);
 			} else {
 				throw new Exception(filename
 						+ " missing! All assignment files not prepared.");
 			}
 		}
 		documents[students.length] = new EntryType();
 		documents[students.length].setId(quizDir.getParent()
 				+ "/assignment.jpg");
 		manifest.setDocument(documents);
 	}
 
 }
