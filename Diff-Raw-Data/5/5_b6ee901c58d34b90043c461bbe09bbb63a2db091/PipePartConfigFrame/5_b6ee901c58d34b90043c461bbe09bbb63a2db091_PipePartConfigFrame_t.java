 package pleocmd.itfc.gui;
 
 import javax.swing.JDialog;
 import javax.swing.JTabbedPane;
 
 import pleocmd.Log;
 import pleocmd.exc.PipeException;
 import pleocmd.itfc.gui.Layouter.Button;
 import pleocmd.pipe.Pipe;
 import pleocmd.pipe.cvt.Converter;
 import pleocmd.pipe.cvt.EmotionConverter;
 import pleocmd.pipe.cvt.PassThroughConverter;
 import pleocmd.pipe.cvt.SimpleConverter;
 import pleocmd.pipe.in.ConsoleInput;
 import pleocmd.pipe.in.FileInput;
 import pleocmd.pipe.in.Input;
 import pleocmd.pipe.in.TcpIpInput;
 import pleocmd.pipe.out.ConsoleOutput;
 import pleocmd.pipe.out.FileOutput;
import pleocmd.pipe.out.InternalCommandOutput;
 import pleocmd.pipe.out.Output;
 import pleocmd.pipe.out.PleoRXTXOutput;
 
 public final class PipePartConfigFrame extends JDialog {
 
 	private static final long serialVersionUID = -1967218353263515865L;
 
 	@SuppressWarnings("unchecked")
 	private final PipePartPanel<Input> pppInput = new PipePartPanel<Input>(
 			(Class<Input>[]) new Class<?>[] { FileInput.class,
 					ConsoleInput.class, TcpIpInput.class });
 
 	@SuppressWarnings("unchecked")
 	private final PipePartPanel<Converter> pppConverter = new PipePartPanel<Converter>(
 			(Class<Converter>[]) new Class<?>[] { SimpleConverter.class,
 					EmotionConverter.class, PassThroughConverter.class });
 
 	@SuppressWarnings("unchecked")
 	private final PipePartPanel<Output> pppOutput = new PipePartPanel<Output>(
 			(Class<Output>[]) new Class<?>[] { FileOutput.class,
					ConsoleOutput.class, PleoRXTXOutput.class,
					InternalCommandOutput.class });
 
 	private final Pipe pipe;
 
 	public PipePartConfigFrame(final Pipe pipe) {
 		Log.detail("Creating Config-Frame");
 		setTitle("Configure Pipe");
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		this.pipe = pipe;
 		pppInput.getTableModel().clear();
 		pppInput.getTableModel().addPipeParts(pipe.getInputList());
 		pppConverter.getTableModel().clear();
 		pppConverter.getTableModel().addPipeParts(pipe.getConverterList());
 		pppOutput.getTableModel().clear();
 		pppOutput.getTableModel().addPipeParts(pipe.getOutputList());
 
 		// Add components
 		final Layouter lay = new Layouter(this);
 		final JTabbedPane tabs = new JTabbedPane();
 		tabs.addTab("Input", pppInput);
 		tabs.addTab("Converter", pppConverter);
 		tabs.addTab("Output", pppOutput);
 		lay.addWholeLine(tabs, true);
 
 		lay.addSpacer();
 		getRootPane().setDefaultButton(lay.addButton(Button.Ok, new Runnable() {
 			@Override
 			public void run() {
 				applyChanges();
 				dispose();
 			}
 		}));
 		lay.addButton(Button.Apply, new Runnable() {
 			@Override
 			public void run() {
 				applyChanges();
 			}
 		});
 		lay.addButton(Button.Cancel, new Runnable() {
 			@Override
 			public void run() {
 				Log.detail("Canceled Config-Frame");
 				dispose();
 			}
 		});
 
 		// Center window on screen
 		setSize(700, 400);
 		setLocationRelativeTo(null);
 
 		Log.detail("Config-Frame created");
 		setModal(true);
 		setVisible(true);
 	}
 
 	public void applyChanges() {
 		try {
 			pipe.reset();
 			for (int i = 0; i < pppInput.getTableModel().getRowCount(); ++i)
 				pipe.addInput(pppInput.getTableModel().getPipePart(i));
 			for (int i = 0; i < pppConverter.getTableModel().getRowCount(); ++i)
 				pipe.addConverter(pppConverter.getTableModel().getPipePart(i));
 			for (int i = 0; i < pppOutput.getTableModel().getRowCount(); ++i)
 				pipe.addOutput(pppOutput.getTableModel().getPipePart(i));
 			Log.detail("Applied Config-Frame");
 		} catch (final PipeException e) {
 			Log.error(e);
 		}
 	}
 
 }
