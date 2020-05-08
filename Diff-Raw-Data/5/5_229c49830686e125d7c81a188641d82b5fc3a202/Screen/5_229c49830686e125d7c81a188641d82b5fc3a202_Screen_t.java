 package platformer;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 
 public class Screen {
     private boolean vsync, resizable, fullscreen;
     private int width, height;
     private String title;
     private boolean available;
     
     public Screen ()
     {
         vsync      = true;
         fullscreen = false;
         available  = false;
         resizable  = false;
         
         title  = "No title set.";
         width  = 640;
         height = 480;
     }
     public void loadConfig (Config config)
     {
         setDimensions(config.width, config.height);
         setTitle(config.title);
         setResizable(config.resizable);
         setFullscreen(config.fullscreen);
         setVsync(config.vsync);
     }
     public int getWidth()  { return Display.getWidth(); }
     public int getHeight() { return Display.getHeight(); }
     public int getMonitorRefreshRate ()
     {
         if (!available) return 60;
         return Display.getDesktopDisplayMode().getFrequency();
     }
     public void destroy ()
     {
         Display.destroy();
     }
     public void prepareRender ()
     {
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, getWidth() + (width - getWidth()), getHeight() + (height - getHeight()));
     }
     public void finishRender ()
     {
         Display.update();
     }
     public void setDimensions (int width, int height)
     {
         this.width  = width;
         this.height = height;
         try { if (available) Display.setDisplayMode(new DisplayMode(width, height)); }
         catch (LWJGLException e) { e.printStackTrace(); }
     }
     public void setFullscreen (boolean flag)
     {
         fullscreen = flag;
         try { if (available) Display.setFullscreen(flag); }
         catch (LWJGLException e) { e.printStackTrace(); }
     }
     public void setVsync (boolean flag)
     {
         vsync = flag;
         if (available) Display.setVSyncEnabled(flag);
     }
     public void setResizable (boolean flag)
     {
         resizable = flag;
         if (available) Display.setResizable(flag);
     }
     public void setTitle (String newTitle)
     {
         title = newTitle;
         if (available) Display.setTitle(newTitle);  
     }
     public void createDisplay ()
     {
         try {
             Display.setDisplayMode(new DisplayMode(
                     width,
                     height));
             Display.setTitle(title);
             Display.setVSyncEnabled(vsync);
             Display.setResizable(resizable);
             Display.setFullscreen(fullscreen);
             Display.create();
         } catch (LWJGLException e) {
             e.printStackTrace();
         }
     
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
        prepareRender();
         glOrtho(0, width, -height, 0, -1, 1);
         available = true;
     }
     public boolean isCloseRequested()
     {
         return (Display.isCloseRequested() || Keyboard.isKeyDown(Keyboard.KEY_ESCAPE));
     }
 }
