 package se.byggarmonster.lib;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.google.common.base.Charsets;
 import com.google.common.io.Files;
 
 public class ByggarMonsterAPIBuilder {
 	private String source;
 	private String template;
 
 	private File checkFileExists(final File file) {
 		checkState(file.exists(), file.getAbsolutePath() + " does not exist.");
 		return file;
 	}
 
 	private File checkFileExists(final String filePath) {
 		final File file = new File(filePath);
 		return checkFileExists(file);
 	}
 
 	private String content(final File file) {
 		try {
 			return Files.toString(file, Charsets.UTF_8);
 		} catch (final IOException e) {
 			throw new RuntimeException("Can not read file "
 			        + file.getAbsoluteFile());
 		}
 	}
 
 	public ByggarMonsterAPIBuilder toFile(final File file) {
 		try {
 			Files.write(toString().getBytes(), file);
 		} catch (final IOException e) {
 			System.err.println("Could not write to " + file.getAbsoluteFile());
 		}
 		return this;
 	}
 
 	public ByggarMonsterAPIBuilder toFile(final String path) {
 		return toFile(new File(path));
 	}
 
 	@Override
 	public String toString() {
		return new ByggarMonsterAPI(source, template).toString();
 	}
 
 	public ByggarMonsterAPIBuilder withSource(final File file) {
 		return withSource(content(checkFileExists(file)));
 	}
 
 	public ByggarMonsterAPIBuilder withSource(final String content) {
 		checkNotNull(content, "Parameter can not be null.");
 		this.source = content;
 		return this;
 	}
 
 	public ByggarMonsterAPIBuilder withSourceFile(final String path) {
 		return withSource(content(checkFileExists(path)));
 	}
 
 	public ByggarMonsterAPIBuilder withTemplate(final File file) {
 		return withTemplate(content(checkFileExists(file)));
 	}
 
 	public ByggarMonsterAPIBuilder withTemplate(final String content) {
 		checkNotNull(content, "Parameter can not be null.");
 		this.template = content;
 		return this;
 	}
 
 	public ByggarMonsterAPIBuilder withTemplateFile(final String path) {
 		return withTemplate(content(checkFileExists(path)));
 	}
 }
