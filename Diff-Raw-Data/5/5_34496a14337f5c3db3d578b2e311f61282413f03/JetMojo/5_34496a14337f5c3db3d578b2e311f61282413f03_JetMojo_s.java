 package com.tikal.maven.plugin.jet;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.mapping.SourceMapping;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 
 import com.tikal.codegen.jet.EmitterException;
 import com.tikal.codegen.jet.ErrorCode;
 import com.tikal.codegen.jet.TemplateEmitter;
 
 /**
  * Goal generate Java Emitters from JET templates.
  * 
  * @goal generate
  * 
  * @phase generate-sources
  */
 public class JetMojo extends AbstractMojo {
 	/**
 	 * Location of the output directory.
 	 * 
 	 * @parameter expression="${project.build.directory}/generated-sources/jet"
 	 * @required
 	 */
 	private File generateDirectory;
 
 	/**
 	 * Location of the template directory.
 	 * 
 	 * @parameter expression="${basedir}/src/main/templates"
 	 * @required
 	 * 
 	 */
 	private File templateDirectory;
 
 	/**
 	 * List of included template patterns.
 	 * 
 	 * @parameter
 	 * 
 	 */
 	protected Set<String> includeTemplates = new HashSet<String>();
 
 	/**
 	 * List of excluded template patterns.
 	 * 
 	 * @parameter
 	 * 
 	 */
 	protected Set<String> excludeTemplates = new HashSet<String>();
 
 	/**
 	 * Generate Java source files in the read-only mode if true is specified.
 	 * 
 	 * @parameter default-value=false
 	 * 
 	 */
 	private boolean readonly;
 
 	/**
 	 * @parameter expression="${project}"
 	 * @required
 	 */
 	private MavenProject project;
 
 	private Set<File> templates = null;
 
 	public void execute() throws MojoExecutionException {
 
 		try {
 			if (!generateDirectory.exists()) {
 				generateDirectory.mkdirs();
 			}
 
 			Set<File> templates = getTemplates();
 			if (templates != null) {
 				for (File template : templates) {
 					getLog().info(String.format("Processing template - %s", template.getName()));
 					TemplateEmitter emitter = new TemplateEmitter(template.toURI().toURL());
 					emitter.parse();
 					File packageDir = new File(generateDirectory, emitter.getPackageName().replace('.', '/'));
 					if (!packageDir.exists()) {
 						packageDir.mkdirs();
 					}
 					File classFile = new File(packageDir, emitter.getClassName() + ".java");
 					FileWriter writer = new FileWriter(classFile);
 					writer.write(emitter.generate());
 					writer.flush();
 					writer.close();
 					if (readonly) {
 						classFile.setReadOnly();
 					}
 				}
 				if (project != null) {
 					project.addCompileSourceRoot(generateDirectory.getPath());
 				}
 			}
 		} catch (EmitterException e) {
 			if (e.getErrorCode().equals(ErrorCode.UNKNOWN)) {
 				throw new MojoExecutionException(e.getErrorCode().getMessage(e.getMessage()));
 			} else {
 				throw new MojoExecutionException(e.getErrorCode().getMessage(e.getParams()));
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage());
 		} catch (InclusionScanException e) {
 			throw new MojoExecutionException(e.getMessage());
 		}
 	}
 
 	public Set<String> getIncludesPatterns() {
 		if (includeTemplates == null || includeTemplates.isEmpty()) {
 			return Collections.singleton("**/*.template");
 		}
 		return includeTemplates;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Set<String> getExcludesPatterns() {
 		if (excludeTemplates == null || excludeTemplates.isEmpty()) {
 			return Collections.EMPTY_SET;
 		}
		return includeTemplates;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Set<File> getTemplates() throws InclusionScanException {
 		if (templates == null) {
 			SourceMapping mapping = new SuffixMapping("template", Collections.EMPTY_SET);
			SourceInclusionScanner scan = new SimpleSourceInclusionScanner(getIncludesPatterns(), excludeTemplates);
 			scan.addSourceMapping(mapping);
 			templates = scan.getIncludedSources(templateDirectory, null);
 		}
 		return templates;
 	}
 }
