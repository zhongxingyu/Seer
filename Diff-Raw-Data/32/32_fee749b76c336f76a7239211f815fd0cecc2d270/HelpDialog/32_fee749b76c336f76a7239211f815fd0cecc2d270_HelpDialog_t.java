 package pleocmd.itfc.gui;
 
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 
 import javax.swing.JDialog;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 
 import pleocmd.Log;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.itfc.gui.help.HelpLoader;
 
 public final class HelpDialog extends JDialog {
 
 	private static final long serialVersionUID = 2506240487807329597L;
 
 	private static HelpDialog helpDialog;
 
 	private final JTextPane tpHelp;
 
 	private HelpDialog(final Window owner) {
 		super(owner);
 		helpDialog = this;
 		setTitle("Help");
 		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(final WindowEvent e) {
 				closeHelp();
 			}
 		});
 
 		final Layouter lay = new Layouter(this);
 
 		tpHelp = new JTextPane();
 		tpHelp.setEditable(false);
 		lay.addWholeLine(new JScrollPane(tpHelp), true);
 
 		lay.addSpacer();
		getRootPane().setDefaultButton(lay.addButton(Button.Ok, new Runnable() {
 			@Override
 			public void run() {
 				closeHelp();
 			}
		}));
 
 		setSize(300, Toolkit.getDefaultToolkit().getScreenSize().height);
 		setLocation(0, 0);
 
 		setAlwaysOnTop(true);
 
 		display("Welcome");
 	}
 
 	public static HelpDialog the(final Window owner) {
 		if (helpDialog == null) new HelpDialog(owner);
 		return helpDialog;
 	}
 
 	public void display(final String category) {
 		setVisible(true);
 		try {
 			tpHelp.setPage(HelpLoader.getHelp(category));
 		} catch (final IOException e) {
 			Log.error(e);
 		}
 	}
 
 	public void closeHelp() {
 		dispose();
 		helpDialog = null;
 	}
 
 	public static void closeHelpIfOpen() {
 		if (helpDialog != null) helpDialog.closeHelp();
 	}
 
 }
