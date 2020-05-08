 package com.github.jflygare.mojo.discovery;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Mojo;
 
 import com.github.jflygare.mojo.discovery.gui.DependenciesPanel;
 import com.github.jflygare.mojo.discovery.util.GraphFactory;
 
 @Mojo(name = "discovery", threadSafe = true, aggregator = true)
 public class DependenciesMojo extends AbstractMojo {
 
 	@Component(hint = "dependency")
 	private GraphFactory graphFactory;
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.apache.maven.plugin.Mojo#execute()
 	 */
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		JFrame jf = new JFrame();
 		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel jp;
 		try {
 			jp = new DependenciesPanel(graphFactory).startPanel();
 			jf.getContentPane().add(jp);
 			jf.pack();
 			jf.setVisible(true);
 		} catch (Exception e) {
 			throw new MojoExecutionException("Unable to start GUI", e);
 		}
 	}
 
 }
