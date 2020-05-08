 package edu.isi.bmkeg.vpdmf.bin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.apache.maven.model.DistributionManagement;
 import org.apache.maven.model.Model;
 
 import utils.VPDMfGeneratorConverters;
 
 import com.google.common.io.Files;
 
 import edu.isi.bmkeg.uml.model.UMLmodel;
 import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
 import edu.isi.bmkeg.utils.Converters;
 import edu.isi.bmkeg.utils.mvnRunner.LocalMavenInstall;
 import edu.isi.bmkeg.vpdmf.controller.archiveBuilder.ActionscriptVpdmfInterface;
 import edu.isi.bmkeg.vpdmf.controller.archiveBuilder.JavaVpdmfServicesConstructor;
 import edu.isi.bmkeg.vpdmf.model.definitions.VPDMf;
 import edu.isi.bmkeg.vpdmf.model.definitions.specs.VpdmfSpec;
 import edu.isi.bmkeg.vpdmf.utils.VPDMfConverters;
 import edu.isi.bmkeg.vpdmf.utils.VPDMfParser;
 
 public class BuildVpdmfServicesMavenProject {
 
 	Logger log = Logger
 			.getLogger("edu.isi.bmkeg.vpdmf.bin.ConstructVpdmfServicesMavenProject");
 
 	public static String USAGE = "arguments: [<spec1> <spec2> ... <specN>] <directory> <bmkeg-parent-version>\n";
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 
 		if( args.length < 3 ) {
 			System.err.println(USAGE);
 			System.exit(-1);
 		}
 		
 		List<File> viewFiles = new ArrayList<File>();
 		List<File> dataFiles = new ArrayList<File>();
 		List<String> solrViews = new ArrayList<String>();
 		UMLmodel model = null;
 
 		File firstPom = new File( args[0].replaceAll("\\/$", "") + "/pom.xml" );
 		Model firstPomModel = VPDMfGeneratorConverters.readModelFromPom(firstPom);
 		VpdmfSpec firstSpecs = VPDMfGeneratorConverters.readVpdmfSpecFromPom(firstPomModel);
 
		List<File> specsFiles = new ArrayList<File>();
 		for (int i = 0; i < args.length - 2; i++) {
			File specsFile = new File(args[i]);
			specsFiles.add(specsFile);
 		}
 
 		File dir = new File(args[args.length - 2]);
 		dir.mkdirs();
 
 		String bmkegParentVersion = args[args.length - 1];
 				
		Iterator<File> it = specsFiles.iterator();
 		while (it.hasNext()) {
 			File pomFile = it.next();
 
 			//
 			// parse the specs files
 			//
 			Model pomModel = VPDMfGeneratorConverters.readModelFromPom(pomFile);
 			VpdmfSpec vpdmfSpec = VPDMfGeneratorConverters.readVpdmfSpecFromPom(pomModel);
 
 			// Model file
 			String modelPath = vpdmfSpec.getModel().getPath();
 			String modelType = vpdmfSpec.getModel().getType();
 			File modelFile = new File(pomFile.getParent() + "/" + modelPath);
 
 			// View directory
 			String viewsPath = vpdmfSpec.getViewsPath();
 			File viewsDir = new File(pomFile.getParent() + "/" + viewsPath);
 			viewFiles.addAll(VPDMfParser.getAllSpecFiles(viewsDir));
 
 			// solr views
 			solrViews.addAll(vpdmfSpec.getSolrViews());
 
 			// Data file
 			File data = null;
 			if (vpdmfSpec.getData() != null) {
 				String dataPath = vpdmfSpec.getData().getPath();
				data = new File(dataPath);
 				if (!data.exists())
 					data = null;
 				else
 					dataFiles.add(data);
 			}
 
 			if (data != null)
 				System.out.println("Data File: " + data.getPath());
 
 			UMLModelSimpleParser p = new UMLModelSimpleParser(
 					UMLmodel.XMI_MAGICDRAW);
 			p.parseUMLModelFile(modelFile);
 			UMLmodel m = p.getUmlModels().get(0);
 
 			if (model == null) {
 				model = m;
 			} else {
 				model.mergeModel(m);
 			}
 
 		}
 
 		VPDMfParser vpdmfP = new VPDMfParser();
 		VPDMf top = vpdmfP.buildAllViews(firstSpecs, model, viewFiles,
 				solrViews);
 
 		if( firstSpecs.getUimaPackagePattern() != null && firstSpecs.getUimaPackagePattern().length() > 0 ) {
 			top.setUimaPkgPattern(firstSpecs.getUimaPackagePattern());
 		}
 		
 		String group = top.getGroupId();
 		String artifactId = top.getArtifactId();
 		String version = top.getVersion();
 
 		// ~~~~~~~~~~~~~~~~~~~~
 		// Java component build
 		// ~~~~~~~~~~~~~~~~~~~~
 		
 		JavaVpdmfServicesConstructor java = new JavaVpdmfServicesConstructor(top);
 
 		String dAddr = dir.getAbsolutePath();
 		File srcJar = new File(dAddr + "/" + artifactId +"-services-src.jar");
 		
 		java.buildServiceMavenProject(srcJar, null, group, artifactId, version, bmkegParentVersion);
 
 		File srcDir = new File(dAddr + "/" + artifactId +"-services");
 		srcDir.mkdirs();
 		Converters.unzipIt(srcJar , srcDir);
 
 		
 		// ~~~~~~~~~~~~~~~~~~~~
 		// Flex component build
 		// ~~~~~~~~~~~~~~~~~~~~
 
 		ActionscriptVpdmfInterface as = new ActionscriptVpdmfInterface(top);
 
 		File zip = new File(dAddr + "/" + artifactId +"-as-services-src.zip");
 
 		as.buildServiceMavenProject(zip, null, group, artifactId, version, bmkegParentVersion);
 
 		File zipDir = new File(dAddr + "/" + artifactId +"-as-services");
 		zipDir.mkdirs();
 		Converters.unzipIt(zip , zipDir);
 		
 		System.out.println("Service libraries built:" + dir);
 
 		
 	}
 
 }
