 package kkckkc.jsourcepad.model;
 
 import com.google.common.collect.Maps;
 import kkckkc.jsourcepad.util.BeanFactoryLoader;
 import kkckkc.utils.swing.ComponentUtils;
 import org.jetbrains.annotations.NotNull;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.support.DefaultListableBeanFactory;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.io.File;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Properties;
 
 
 public class WindowManagerImpl implements WindowManager {
 	private Map<Container, Window> openWindows = Maps.newHashMap();
 	private int lastId = 0;
     private Window focusedWindow;
 
 	// Collaborators
 	private Application app;
 	private BeanFactoryLoader beanFactoryLoader;
 
 	@Autowired
 	public void setApp(Application app) {
 	    this.app = app;
     }
 
 	@Autowired
 	public void setBeanFactoryLoader(BeanFactoryLoader beanFactoryLoader) {
 	    this.beanFactoryLoader = beanFactoryLoader;
     }
 
     @Override
     public Window getWindow(int id) {
         for (Window window : openWindows.values()) {
             if (window.getId() == id) return window;
         }
         return null;
     }
 
     @Override
 	public Window getWindow(Container c) {
 		return openWindows.get(ComponentUtils.getToplevelAncestor(c));
 	}
 
     @Override
     public Window getFocusedWindow() {
         return focusedWindow;
     }
 
     @NotNull
     @Override
 	public Window newWindow(File file) {
         Properties props = new Properties();
         props.put("projectDir", file == null ? "" : file.toString());
 
         DefaultListableBeanFactory container =
             beanFactoryLoader.load(BeanFactoryLoader.WINDOW, app, file, props);
 
         final Window window = container.getBean("window", Window.class);
         ((WindowImpl) window).setId(++lastId);
 
         JFrame frame = window.getContainer();
         container.registerSingleton("frame", frame);
 
         openWindows.put(frame, window);
         app.topic(Listener.class).post().created(window);
 
         if (file != null && ! file.isDirectory()) {
             window.getDocList().open(file);
         }
 
         frame.addWindowFocusListener(new WindowFocusListener() {
             @Override
             public void windowGainedFocus(WindowEvent e) {
                 focusedWindow = window;
             }
 
             @Override
             public void windowLostFocus(WindowEvent e) {
             }
         });
         focusedWindow = window;
 
         return window;
 	}
 
 	@Override
 	public void closeWindow(@NotNull Window window) {
         window.saveState();
         window.getBeanFactory().destroySingletons();
 
        if (focusedWindow == window) focusedWindow = null;

 		openWindows.remove(window.getContainer());
 		app.topic(Listener.class).post().destroyed(window);
 	}
 
 	@NotNull
     @Override
     public Collection<Window> getWindows() {
 	    return openWindows.values();
     }
 
     @Override
     public void minimize(@NotNull Window window) {
         JFrame container = window.getContainer();
         container.setState(JFrame.ICONIFIED);
     }
 
     @Override
     public void maximize(@NotNull Window window) {
         JFrame container = window.getContainer();
         if (container.getState() == JFrame.MAXIMIZED_BOTH) {
             container.setState(JFrame.NORMAL);
         } else {
             container.setState(JFrame.MAXIMIZED_BOTH);
         }
     }
 }
