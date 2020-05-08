 package com.mattwhipple.sproogle.closure.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.FileVisitOption;
 import java.nio.file.FileVisitResult;
 import java.nio.file.Files;
 import java.nio.file.Path;
import java.nio.file.Paths;
 import java.nio.file.SimpleFileVisitor;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Objects;
 
 import javax.annotation.Nonnull;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.Resource;
 
 import com.google.template.soy.SoyFileSet;
 import com.google.template.soy.tofu.SoyTofu;
 import com.mattwhipple.sproogle.closure.SoyTofuStore;
 
 //TODO: This needs integration testing
 public class FilesystemSoyTofuStore implements SoyTofuStore {
 
 	private static Logger logger = LoggerFactory.getLogger(FilesystemSoyTofuStore.class);
 	
 	protected Resource storeResource;
 	protected int visitDepth = Integer.MAX_VALUE;
 	
 	public void setStoreResource(Resource storeResource) {
 		this.storeResource = storeResource;
 	}
 
 	public void setVisitDepth(int visitDepth) {
 		this.visitDepth = visitDepth;
 	}
 	
 	
 	@SuppressWarnings("null") //Checked with Objects.
 	@Override
 	public @Nonnull SoyTofu getTemplates() throws IOException {
 
         SoyFileSet.Builder fileSetBuilder = new SoyFileSet.Builder();
         
         for (File templateFile: collectSoyFiles()) {
             fileSetBuilder.add(templateFile);
         }
 
         SoyFileSet soyFileSet = fileSetBuilder.build();
         
         SoyTofu toReturn = Objects.requireNonNull(soyFileSet.compileToTofu());
         return toReturn;
 	}
 	
 	@Nonnull List<File> collectSoyFiles() throws IOException {
 		List<File> toReturn = new ArrayList<>();
 		SoyFileCollector soyFileCollector = new SoyFileCollector();
 		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		Files.walkFileTree(Paths.get(storeResource.getURI()), opts, visitDepth, soyFileCollector);
 		toReturn.addAll(soyFileCollector.getSoyFiles());
 		return toReturn;
 	}
 	
 	static class SoyFileCollector extends SimpleFileVisitor<Path> {
 		
 		private List<File> soyFiles = new ArrayList<>();
 		
 		@Override
 		public FileVisitResult visitFile(Path path, BasicFileAttributes attr) {
 			logger.debug("visiting file {}", path);
 			if (attr.isRegularFile() && path.toString().endsWith(".soy")) {
 				this.soyFiles.add(path.toFile());
 			}
 			return FileVisitResult.CONTINUE;
 		}
 
 		public List<File> getSoyFiles() {
 			return soyFiles;
 		}
 	}
 
 }
