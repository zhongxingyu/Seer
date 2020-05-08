 package com.github.stefanliebenberg.javascript;
 
 
 import com.github.stefanliebenberg.internal.*;
 import com.github.stefanliebenberg.utilities.FsTool;
 import com.google.common.base.Function;
 
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 
 public class JsBuilder
         extends AbstractBuilder<JsBuildOptions>
         implements IBuilder {
 
     private final ClosureDependencyParser dependencyParser =
             new ClosureDependencyParser();
 
     private final DependencyBuilder<ClosureSourceFile> dependencyBuilder =
             new DependencyBuilder<ClosureSourceFile>();
 
     private static final String JS_EXT = "js";
 
     private static final Function<File, ClosureSourceFile> FILE_TO_CLOSURE =
             new Function<File, ClosureSourceFile>() {
                 @Nullable
                 @Override
                public ClosureSourceFile apply(@Nullable File file) {
                     if (file != null) {
                         return new ClosureSourceFile(file);
                     } else {
                         return null;
                     }
                 }
             };
 
     private Collection<ClosureSourceFile> sourceFiles;
 
     @Override
     public void build() throws BuildException {
         Collection<File> sourceDirectories =
                 buildOptions.getJavaScriptSourceDirectories();
         Collection<File> rawSourceFiles =
                 FsTool.find(sourceDirectories, JS_EXT);
         sourceFiles = new HashSet<ClosureSourceFile>();
         try {
             for (File rawFile : rawSourceFiles) {
                 ClosureSourceFile sourceFile = FILE_TO_CLOSURE.apply(rawFile);
                 if (sourceFile != null) {
                     dependencyParser.parse(sourceFile, FsTool.read(rawFile));
                     sourceFiles.add(sourceFile);
                 }
             }
         } catch (IOException ioException) {
             throwBuildException(ioException);
         }
 
         final DependencyBuildOptions<ClosureSourceFile> dependencyBuildOptions =
                 new DependencyBuildOptions<ClosureSourceFile>();
         dependencyBuildOptions.setEntryPoints(buildOptions.getEntryPoints());
         dependencyBuilder.setBuildOptions(dependencyBuildOptions);
         dependencyBuilder.build();
     }
 
     @Override
     public void reset() {
         super.reset();
         dependencyBuilder.reset();
         sourceFiles = null;
     }
 }
