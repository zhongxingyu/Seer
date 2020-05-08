 package org.jbehave.core.story;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import org.jbehave.core.story.codegen.parser.TextStoryParser;
 import org.jbehave.core.story.domain.Story;
 import org.jbehave.core.story.renderer.PlainTextRenderer;
 
 public class StoryToDirectoryPrinter {
     
     private final StoryLoader loader;
     private final File directory;
 
     public StoryToDirectoryPrinter(StoryLoader loader, File directory) {
         if (!directory.exists()) {
             throw new IllegalArgumentException("Directory " + directory + " does not exist.");
         }
         if (!directory.isDirectory()) {
             throw new IllegalArgumentException("Directory " + directory + " is not a directory.");
         }
         
         this.loader = loader;
         this.directory = directory;
     }
 
 
     private void print(String storyClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException {
         Story story = loader.loadStoryClass(storyClass);
         String[] storyNameParts = story.getClass().getName().split("\\.");
         String simpleStoryName = storyNameParts[storyNameParts.length - 1];
         
         File file = new File(directory, simpleStoryName + ".story");
         if (!file.exists()) {
             file.createNewFile();
         }
         OutputStream outputStream = new FileOutputStream(file);
         new StoryPrinter(loader, new PlainTextRenderer(new PrintStream(outputStream))).print(storyClass);
         
         outputStream.close();
     }
 
     public static void main(String[] args) throws ClassNotFoundException {
         File directory = new File(args[0]);                      
         Thread.currentThread().getContextClassLoader().loadClass("com.sirenian.hellbound.stories.TheGlyphIsConstrainedByThePit");
         
         try {
             StoryToDirectoryPrinter printer = new StoryToDirectoryPrinter(
                     new StoryLoader(new TextStoryParser(), Thread.currentThread().getContextClassLoader()),
                     directory);
                     
             for (int i = 1; i < args.length; i++) {
                 printer.print(args[i]); 
             }           
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
