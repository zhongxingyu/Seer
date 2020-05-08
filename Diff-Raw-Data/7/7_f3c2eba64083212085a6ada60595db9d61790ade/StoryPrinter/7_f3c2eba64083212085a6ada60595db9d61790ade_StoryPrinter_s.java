 /*
  * Created on 25-Aug-2004
  * 
  * (c) 2003-2004 ThoughtWorks Ltd
  *
  * See license.txt for license details
  */
 package org.jbehave.core.story;
 import java.net.MalformedURLException;
 
 import org.jbehave.core.story.codegen.parser.TextStoryParser;
 import org.jbehave.core.story.domain.Story;
 import org.jbehave.core.story.renderer.PlainTextRenderer;
 import org.jbehave.core.story.renderer.Renderer;
 
 /**
  * A StoryPrinter loads a story and narrates it to a given renderer.
  * 
  * @author <a href="mailto:dan.north@thoughtworks.com">Dan North</a>
  * @author Mauro Talevi
  */
 public class StoryPrinter {
 
     private StoryLoader storyLoader;
     private Renderer renderer;
     
     public StoryPrinter(StoryLoader storyLoader,  Renderer renderer) {
         this.storyLoader = storyLoader;
         this.renderer = renderer;
     }
 
     public void print(String storyPath) throws MalformedURLException, InstantiationException, IllegalAccessException, ClassNotFoundException {
         Story story = storyLoader.loadStoryClass(storyPath);
         story.specify();
         story.narrateTo(renderer);
     }
 
     public void print(Class storyClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
         Story story = storyLoader.loadStory(storyClass);
         story.specify();
         story.narrateTo(renderer);
     }
 
     public static void main(String[] args) {        
         try {
             StoryPrinter printer = new StoryPrinter(
                     new StoryLoader(new TextStoryParser(), StoryPrinter.class.getClassLoader()), 
                     new PlainTextRenderer(System.out));
             for (int i = 0; i < args.length; i++) {
                 printer.print(args[i]); 
             }           
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
