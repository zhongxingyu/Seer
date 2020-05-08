 import java.awt.Dialog.ModalityType;
 import java.awt.EventQueue;
 import javax.swing.JFrame;
 import cisc_sim.*;
 
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import java.awt.FlowLayout;
 import javax.swing.SwingConstants;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JTextPane;
 import javax.swing.JTextArea;
 import javax.swing.JScrollPane;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import java.awt.Font;
 
 public class MainGui implements MemoryEventListener, RegisterEventListener,
 		ControllerEventListener, DeviceEventListener {
 
 	private final int			__DEBUG__				= 0;
 	public final static String	__INIT_DIR__			= "/Users/cooniur/Projects/workspace-java/cisc_sim/Sample";
 
 	private JFrame				frmMain;
 	private JTextField			txtReg0;
 	private JTextField			txtReg1;
 	private JTextField			txtReg2;
 	private JTextField			txtReg3;
 	private JTextField			txtCC0;
 	private JTextField			txtCC1;
 	private JTextField			txtCC2;
 	private JTextField			txtCC3;
 	private JTextField			txtX0;
 	private JTextField			txtMAR;
 	private JTextField			txtMBR;
 	private JTextField			txtPC;
 	private JTextField			txtMSR;
 	private JTextField			txtIR;
 	private JTextField			txtMFR;
 	private JButton				btnPowerOn;
 	private JButton				btnPowerOff;
 	private JTextArea			txtInputView;
 	private JLabel				lblOutput;
 	private JTextArea			txtOutput;
 	private JTextField			txtRomFile;
 	private JTextField			txtBootFile;
 	private JTextField			txtProgramFile;
 	private JButton				btnStepRun;
 	private JButton				btnRun;
 	private JButton				btnLoadBootFile;
 	private JButton				btnLoadProgramFile;
 	private JButton				btnLoadRomFile;
 	private JButton				btnCompiler;
 	private JButton				btnMemoryDump;
 
 	private Controller			_controller;
 	private File				_romFile				= null;
 	private File				_bootFile				= null;
 	private File				_programFile			= null;
 
 	private byte[]				_keyboardBuffer			= null;
 	private int					_keyboardBufferPos		= Integer.MAX_VALUE;
 	private boolean				_keyboardBuffered		= false;
 
 	private byte[]				_cardReaderBuffer		= null;
 	private int					_cardReaderBufferPos	= Integer.MAX_VALUE;
 	private boolean				_cardReaderBuffered		= false;
 	private boolean				_cardReaderError		= false;
 
 	private frmMemDump			_memDumpWindow			= null;
 	private JButton				btnStep;
 	private JLabel				lblCurrentInstruction;
 	private JTextField			txtInstruction;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainGui window = new MainGui();
 					window.frmMain.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainGui() {
 		initialize();
 		this.frmMain.setTitle("Simulator");
 		registerControlEvents();
 		this._controller = new Controller();
 		this._controller.addControllerEventListener(this);
 		this._controller.addRegisterUpdatedEventListener(this);
 		this._controller.addMemoryUpdatedEventListener(this);
 		this._controller.addDeviceEventListener(this);
 
 		btnPowerOn.setEnabled(true);
 		btnPowerOff.setEnabled(false);
 		btnRun.setEnabled(false);
 		btnStepRun.setEnabled(false);
 		btnMemoryDump.setEnabled(false);
 	}
 
 	private void registerControlEvents() {
 		btnPowerOn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if (_romFile == null || !_romFile.exists()) {
 					JOptionPane.showMessageDialog(frmMain,
 							"An existing ROM file must be selected.", "Error",
 							JOptionPane.ERROR_MESSAGE);
 					txtRomFile.setText("");
 					btnLoadRomFile.doClick();
 					return;
 				}
 
 				if (_bootFile == null || !_bootFile.exists()) {
 					JOptionPane.showMessageDialog(frmMain,
 							"An existing BOOT file must be selected.", "Error",
 							JOptionPane.ERROR_MESSAGE);
 					txtBootFile.setText("");
 					btnLoadBootFile.doClick();
 					return;
 				}
 
 				if (_programFile == null || !_programFile.exists()) {
 					JOptionPane.showMessageDialog(frmMain,
 							"An existing PROGRAM file must be selected.",
 							"Error", JOptionPane.ERROR_MESSAGE);
 					txtProgramFile.setText("");
 					btnLoadProgramFile.doClick();
 					return;
 				}
 
 				clearDumps();
 
 				_controller.init();
 				_controller.loadRom(_romFile.getAbsolutePath());
 				_controller.loadBoot(_bootFile.getAbsolutePath());
 				JOptionPane.showMessageDialog(frmMain, "ROM and BOOT loaded.",
 						"Information", JOptionPane.INFORMATION_MESSAGE);
 
 				btnPowerOn.setEnabled(false);
 				btnPowerOff.setEnabled(true);
 				btnMemoryDump.setEnabled(true);
 
 				btnRun.setEnabled(true);
 				btnStepRun.setEnabled(true);
 				btnStep.setEnabled(false);
 				btnLoadRomFile.setEnabled(false);
 				btnLoadBootFile.setEnabled(false);
 				btnLoadProgramFile.setEnabled(true);
 			}
 		});
 
 		btnPowerOff.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				btnPowerOn.setEnabled(true);
 				btnPowerOff.setEnabled(false);
 
 				btnLoadRomFile.setEnabled(true);
 				btnLoadBootFile.setEnabled(true);
 
 				btnLoadProgramFile.setEnabled(true);
 
 				btnRun.setEnabled(false);
 				btnStepRun.setEnabled(false);
 				btnStep.setEnabled(false);
 				btnMemoryDump.setEnabled(false);
 				haltController();
 			}
 		});
 
 		btnRun.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				_controller.setExeMode();
 				btnStepRun.setEnabled(false);
 				btnRun.setEnabled(false);
 				btnPowerOff.setEnabled(true);
 				btnLoadRomFile.setEnabled(false);
 				btnLoadBootFile.setEnabled(false);
 				btnLoadProgramFile.setEnabled(false);
 				if (__DEBUG__ == 0) {
 					Thread t = new Thread(_controller);
 					t.start();
 				} else
 					_controller.run();
 			}
 		});
 
 		btnStepRun.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				_controller.setStepMode();
 				btnRun.setEnabled(false);
 				btnStepRun.setEnabled(false);
 				btnStep.setEnabled(true);
 				btnPowerOff.setEnabled(true);
 				btnLoadRomFile.setEnabled(false);
 				btnLoadBootFile.setEnabled(false);
 				btnLoadProgramFile.setEnabled(false);
 				if (__DEBUG__ == 0) {
 					Thread t = new Thread(_controller);
 					t.start();
 				} else
 					_controller.run();
 			}
 		});
 
 		btnStep.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				synchronized (_controller) {
 					_controller.notify();
 				}
 			}
 		});
 
 		this.btnLoadRomFile.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final JFileChooser fc = new JFileChooser(new File(__INIT_DIR__));
 				if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(frmMain)) {
 					_romFile = fc.getSelectedFile();
 					txtRomFile.setText(_romFile.getAbsolutePath());
 				}
 			}
 		});
 
 		this.btnLoadBootFile.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final JFileChooser fc = new JFileChooser(new File(__INIT_DIR__));
 				if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(frmMain)) {
 					_bootFile = fc.getSelectedFile();
 					txtBootFile.setText(_bootFile.getAbsolutePath());
 				}
 			}
 		});
 
 		this.btnLoadProgramFile.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final JFileChooser fc = new JFileChooser(new File(__INIT_DIR__));
 				if (JFileChooser.APPROVE_OPTION == fc.showOpenDialog(frmMain)) {
 					_programFile = fc.getSelectedFile();
 					txtProgramFile.setText(_programFile.getAbsolutePath());
 				}
 			}
 		});
 
 		this.btnCompiler.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frmCompiler c = new frmCompiler();
 				c.setLocationRelativeTo(frmMain);
 				c.setVisible(true);
 			}
 		});
 
 		this.btnMemoryDump.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (_memDumpWindow == null) {
 					_memDumpWindow = new frmMemDump();
 					_memDumpWindow
 							.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 					_memDumpWindow.setLocationRelativeTo(frmMain);
 				}
 
 				_memDumpWindow.setVisible(true);
 				updateMemDumpWindow();
 			}
 		});
 	}
 
 	private void updateMemDumpWindow() {
 		if (_memDumpWindow != null) {
 			_memDumpWindow.setMemory(_controller.getMemoryDump());
 			_memDumpWindow.setStartingAddress("0");
 			_memDumpWindow.showMemory();
 		}
 	}
 
 	private void clearDumps() {
 		this.txtCC0.setText("");
 		this.txtCC1.setText("");
 		this.txtCC2.setText("");
 		this.txtCC3.setText("");
 
 		this.txtReg0.setText("");
 		this.txtReg1.setText("");
 		this.txtReg2.setText("");
 		this.txtReg3.setText("");
 
 		this.txtInstruction.setText("");
 		this.txtIR.setText("");
 		this.txtMAR.setText("");
 		this.txtMBR.setText("");
 		this.txtMFR.setText("");
 		this.txtMSR.setText("");
 		this.txtOutput.setText("");
 		this.txtInputView.setText("");
 		this.txtPC.setText("");
 		this.txtX0.setText("");
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frmMain = new JFrame();
 		frmMain.setBounds(100, 100, 758, 573);
 		frmMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel pnlMain = new JPanel();
 		frmMain.getContentPane().add(pnlMain, BorderLayout.CENTER);
 		pnlMain.setLayout(null);
 
 		JLabel lblReg0 = new JLabel("Register #0:");
 		lblReg0.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblReg0.setBounds(6, 23, 83, 16);
 		pnlMain.add(lblReg0);
 
 		txtReg0 = new JTextField();
 		txtReg0.setEditable(false);
 		lblReg0.setLabelFor(txtReg0);
 		txtReg0.setBounds(101, 17, 134, 28);
 		pnlMain.add(txtReg0);
 		txtReg0.setColumns(10);
 
 		JLabel lblReg1 = new JLabel("Register #1:");
 		lblReg1.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblReg1.setBounds(6, 52, 83, 16);
 		pnlMain.add(lblReg1);
 
 		txtReg1 = new JTextField();
 		txtReg1.setEditable(false);
 		lblReg1.setLabelFor(txtReg1);
 		txtReg1.setColumns(10);
 		txtReg1.setBounds(101, 46, 134, 28);
 		pnlMain.add(txtReg1);
 
 		JLabel lblReg2 = new JLabel("Register #2:");
 		lblReg2.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblReg2.setBounds(6, 80, 83, 16);
 		pnlMain.add(lblReg2);
 
 		txtReg2 = new JTextField();
 		txtReg2.setEditable(false);
 		lblReg2.setLabelFor(txtReg2);
 		txtReg2.setColumns(10);
 		txtReg2.setBounds(101, 74, 134, 28);
 		pnlMain.add(txtReg2);
 
 		JLabel lblReg3 = new JLabel("Register #3:");
 		lblReg3.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblReg3.setBounds(6, 108, 83, 16);
 		pnlMain.add(lblReg3);
 
 		txtReg3 = new JTextField();
 		txtReg3.setEditable(false);
 		txtReg3.setColumns(10);
 		txtReg3.setBounds(101, 102, 134, 28);
 		pnlMain.add(txtReg3);
 
 		JLabel lblCC0 = new JLabel("CondC #0:");
 		lblCC0.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCC0.setBounds(6, 154, 83, 16);
 		pnlMain.add(lblCC0);
 
 		txtCC0 = new JTextField();
 		txtCC0.setEditable(false);
 		txtCC0.setColumns(10);
 		txtCC0.setBounds(101, 148, 134, 28);
 		pnlMain.add(txtCC0);
 
 		JLabel lblCC1 = new JLabel("CondC #1:");
 		lblCC1.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCC1.setBounds(6, 182, 83, 16);
 		pnlMain.add(lblCC1);
 
 		txtCC1 = new JTextField();
 		txtCC1.setEditable(false);
 		txtCC1.setColumns(10);
 		txtCC1.setBounds(101, 176, 134, 28);
 		pnlMain.add(txtCC1);
 
 		JLabel lblCC2 = new JLabel("CondC #2:");
 		lblCC2.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCC2.setBounds(6, 210, 83, 16);
 		pnlMain.add(lblCC2);
 
 		txtCC2 = new JTextField();
 		txtCC2.setEditable(false);
 		txtCC2.setColumns(10);
 		txtCC2.setBounds(101, 204, 134, 28);
 		pnlMain.add(txtCC2);
 
 		JLabel lblCC3 = new JLabel("CondC #3:");
 		lblCC3.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCC3.setBounds(6, 238, 83, 16);
 		pnlMain.add(lblCC3);
 
 		txtCC3 = new JTextField();
 		txtCC3.setEditable(false);
 		txtCC3.setColumns(10);
 		txtCC3.setBounds(101, 232, 134, 28);
 		pnlMain.add(txtCC3);
 
 		JLabel lblX0 = new JLabel("Reg X0:");
 		lblX0.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblX0.setBounds(6, 284, 83, 16);
 		pnlMain.add(lblX0);
 
 		txtX0 = new JTextField();
 		txtX0.setEditable(false);
 		txtX0.setColumns(10);
 		txtX0.setBounds(101, 278, 134, 28);
 		pnlMain.add(txtX0);
 
 		JLabel lblMAR = new JLabel("MAR:");
 		lblMAR.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMAR.setBounds(6, 329, 83, 16);
 		pnlMain.add(lblMAR);
 
 		txtMAR = new JTextField();
 		txtMAR.setEditable(false);
 		txtMAR.setColumns(10);
 		txtMAR.setBounds(101, 323, 134, 28);
 		pnlMain.add(txtMAR);
 
 		JLabel lblMBR = new JLabel("MBR:");
 		lblMBR.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMBR.setBounds(6, 361, 83, 16);
 		pnlMain.add(lblMBR);
 
 		txtMBR = new JTextField();
 		txtMBR.setEditable(false);
 		txtMBR.setColumns(10);
 		txtMBR.setBounds(101, 355, 134, 28);
 		pnlMain.add(txtMBR);
 
 		JLabel lblPC = new JLabel("PC:");
 		lblPC.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPC.setBounds(44, 395, 45, 16);
 		pnlMain.add(lblPC);
 
 		txtPC = new JTextField();
 		txtPC.setEditable(false);
 		txtPC.setColumns(10);
 		txtPC.setBounds(101, 389, 134, 28);
 		pnlMain.add(txtPC);
 
 		JLabel lblMSR = new JLabel("MSR:");
 		lblMSR.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMSR.setBounds(44, 452, 45, 16);
 		pnlMain.add(lblMSR);
 
 		txtMSR = new JTextField();
 		txtMSR.setEditable(false);
 		txtMSR.setColumns(10);
 		txtMSR.setBounds(101, 446, 134, 28);
 		pnlMain.add(txtMSR);
 
 		JLabel lblIR = new JLabel("IR:");
 		lblIR.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblIR.setBounds(44, 424, 45, 16);
 		pnlMain.add(lblIR);
 
 		txtIR = new JTextField();
 		txtIR.setEditable(false);
 		txtIR.setColumns(10);
 		txtIR.setBounds(101, 418, 134, 28);
 		pnlMain.add(txtIR);
 
 		btnPowerOn = new JButton("Power ON");
 		btnPowerOn.setBounds(618, 16, 117, 29);
 		pnlMain.add(btnPowerOn);
 
 		btnPowerOff = new JButton("Halt");
 		btnPowerOff.setBounds(618, 45, 117, 29);
 		pnlMain.add(btnPowerOff);
 
 		JLabel lblMfr = new JLabel("MFR:");
 		lblMfr.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMfr.setBounds(44, 480, 45, 16);
 		pnlMain.add(lblMfr);
 
 		txtMFR = new JTextField();
 		txtMFR.setEditable(false);
 		txtMFR.setColumns(10);
 		txtMFR.setBounds(101, 474, 134, 28);
 		pnlMain.add(txtMFR);
 
 		JLabel lblInput = new JLabel("Input History:");
 		lblInput.setBounds(249, 154, 128, 16);
 		pnlMain.add(lblInput);
 
 		JScrollPane scrollPaneInputView = new JScrollPane();
 		scrollPaneInputView.setBounds(247, 176, 219, 320);
 		pnlMain.add(scrollPaneInputView);
 
 		txtInputView = new JTextArea();
 		scrollPaneInputView.setViewportView(txtInputView);
 		txtInputView.setEditable(false);
 
 		lblOutput = new JLabel("Output:");
 		lblOutput.setBounds(478, 154, 128, 16);
 		pnlMain.add(lblOutput);
 
 		JScrollPane scrollPaneOutput = new JScrollPane();
 		scrollPaneOutput.setBounds(478, 176, 219, 320);
 		pnlMain.add(scrollPaneOutput);
 
 		txtOutput = new JTextArea();
 		scrollPaneOutput.setViewportView(txtOutput);
 		txtOutput.setEditable(false);
 
 		JLabel lblRomFile = new JLabel("Rom File:");
 		lblRomFile.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblRomFile.setBounds(248, 23, 94, 16);
 		pnlMain.add(lblRomFile);
 
 		txtRomFile = new JTextField();
 		txtRomFile.setEditable(false);
 		txtRomFile.setColumns(10);
 		txtRomFile.setBounds(354, 17, 212, 28);
 		pnlMain.add(txtRomFile);
 
 		JLabel lblBootFile = new JLabel("Boot File:");
 		lblBootFile.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblBootFile.setBounds(248, 52, 94, 16);
 		pnlMain.add(lblBootFile);
 
 		txtBootFile = new JTextField();
 		txtBootFile.setEditable(false);
 		txtBootFile.setColumns(10);
 		txtBootFile.setBounds(354, 46, 212, 28);
 		pnlMain.add(txtBootFile);
 
 		JLabel lblProgramFile = new JLabel("Program File:");
 		lblProgramFile.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblProgramFile.setBounds(248, 80, 94, 16);
 		pnlMain.add(lblProgramFile);
 
 		txtProgramFile = new JTextField();
 		txtProgramFile.setEditable(false);
 		txtProgramFile.setColumns(10);
 		txtProgramFile.setBounds(354, 74, 212, 28);
 		pnlMain.add(txtProgramFile);
 
 		btnLoadRomFile = new JButton(">>");
 		btnLoadRomFile.setBounds(569, 24, 37, 16);
 		pnlMain.add(btnLoadRomFile);
 
 		btnLoadBootFile = new JButton(">>");
 		btnLoadBootFile.setBounds(569, 52, 37, 16);
 		pnlMain.add(btnLoadBootFile);
 
 		btnLoadProgramFile = new JButton(">>");
 		btnLoadProgramFile.setBounds(569, 80, 37, 16);
 		pnlMain.add(btnLoadProgramFile);
 
 		btnCompiler = new JButton("Compiler");
 		btnCompiler.setBounds(618, 75, 117, 29);
 		pnlMain.add(btnCompiler);
 
 		btnMemoryDump = new JButton("Memory Dump");
 		btnMemoryDump.setBounds(618, 103, 117, 29);
 		pnlMain.add(btnMemoryDump);
 
 		JPanel panel = new JPanel();
 		panel.setBounds(247, 108, 319, 41);
 		pnlMain.add(panel);
 
 		btnRun = new JButton("Run");
 		panel.add(btnRun);
 		btnRun.setEnabled(true);
 
 		btnStepRun = new JButton("Step Run");
 		panel.add(btnStepRun);
 		btnStepRun.setEnabled(true);
 
 		btnStep = new JButton("Step");
 		panel.add(btnStep);
 		btnStep.setEnabled(false);
 
 		lblCurrentInstruction = new JLabel("Instruction:");
 		lblCurrentInstruction.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCurrentInstruction.setBounds(6, 508, 83, 16);
 		pnlMain.add(lblCurrentInstruction);
 
 		txtInstruction = new JTextField();
 		txtInstruction.setFont(new Font("Lucida Console", Font.PLAIN, 13));
 		txtInstruction.setEditable(false);
 		txtInstruction.setColumns(10);
 		txtInstruction.setBounds(101, 502, 327, 28);
 		pnlMain.add(txtInstruction);
 	}
 
 	@Override
 	public void invokeRegisterUpdatedEvent(final Register reg) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				txtReg0.setText("0x"
 						+ Integer.toHexString(reg.getReg(0) & 0xFFFF)
 								.toUpperCase());
 				txtReg1.setText("0x"
 						+ Integer.toHexString(reg.getReg(1) & 0xFFFF)
 								.toUpperCase());
 				txtReg2.setText("0x"
 						+ Integer.toHexString(reg.getReg(2) & 0xFFFF)
 								.toUpperCase());
 				txtReg3.setText("0x"
 						+ Integer.toHexString(reg.getReg(3) & 0xFFFF)
 								.toUpperCase());
 
 				txtCC0.setText(Integer.toString(reg.getCC(0)));
 				txtCC1.setText(Integer.toString(reg.getCC(1)));
 				txtCC2.setText(Integer.toString(reg.getCC(2)));
 				txtCC3.setText(Integer.toString(reg.getCC(3)));
 
 				txtX0.setText("0x"
 						+ Integer.toHexString(reg.getX0() & 0xFFFF)
 								.toUpperCase());
 
 				txtIR.setText("0x"
 						+ Integer.toHexString(reg.getIR() & 0xFFFF)
 								.toUpperCase());
 				txtPC.setText("0x"
 						+ Integer.toHexString(reg.getPC() & 0xFFFF)
 								.toUpperCase());
 
 				txtMFR.setText("0x"
 						+ Integer.toHexString(reg.getMFR() & 0xFFFF)
 								.toUpperCase());
 				txtMSR.setText("0x"
 						+ Integer.toHexString(reg.getMSR() & 0xFFFF)
 								.toUpperCase());
 			}
 
 		});
 	}
 
 	@Override
 	public void invokeMemoryUpdatedEvent(final Memory mem) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				txtMAR.setText("0x"
 						+ Integer.toHexString(mem.getMAR() & 0xFFFF)
 								.toUpperCase());
 				txtMBR.setText("0x"
 						+ Integer.toHexString(mem.getMBR() & 0xFFFF)
 								.toUpperCase());
 
 				updateMemDumpWindow();
 			}
 		});
 	}
 
 	@Override
 	public void invokeControllerCurrentInstructionEvent(Instruction instr) {
 		txtInstruction.setText(instr.toString());
 	}
 
 	@Override
 	public void invokeControllerExceptionEvent(final Exception e) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
				haltController();
 				JOptionPane.showMessageDialog(frmMain, e.getMessage(), "Error",
 						JOptionPane.ERROR_MESSAGE);
 			}
 		});
 	}
 
 	@Override
 	public void invokeControllerExecutionFinished() {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				JOptionPane.showMessageDialog(frmMain, "Execution finished",
 						"Information", JOptionPane.INFORMATION_MESSAGE);
 
 				btnPowerOn.setEnabled(true);
 				btnPowerOff.setEnabled(false);
 				btnRun.setEnabled(false);
 				btnStepRun.setEnabled(false);
 				btnStep.setEnabled(false);
 
 				btnLoadRomFile.setEnabled(true);
 				btnLoadBootFile.setEnabled(true);
 				btnLoadProgramFile.setEnabled(true);
 
 				_keyboardBuffered = false;
 				_cardReaderBuffered = false;
 			}
 		});
 	}
 
 	@Override
 	public int invokeDeviceInputEvent(int devid) {
 		int data = 0;
 		switch (devid) {
 			case 0: // keyboard
 				if (!this._keyboardBuffered) {
 					String s = JOptionPane.showInputDialog("Please input: ");
 					while (s.trim().length() == 0)
 						s = JOptionPane.showInputDialog("Please input: ");
 
 					final String line = s;
 					SwingUtilities.invokeLater(new Runnable() {
 						@Override
 						public void run() {
 							txtInputView.append(line + "\n");
 						}
 					});
 
 					Short number = null;
 					try {
 						number = Short.valueOf(s);
 					} catch (NumberFormatException e) {
 					}
 
 					if (number != null)
 						data = number.intValue();
 					else {
 						s = s.trim();
 						this._keyboardBuffer = s.getBytes();
 						this._keyboardBufferPos = 1;
 						this._keyboardBuffered = true;
 						data = this._keyboardBuffer[0];
 					}
 				} else {
 					if (this._keyboardBufferPos < this._keyboardBuffer.length)
 						data = this._keyboardBuffer[this._keyboardBufferPos++];
 					else
 						this._keyboardBuffered = false;
 				}
 				break;
 			case 2: // card reader
 				if (!this._cardReaderBuffered) {
 					try {
 						this._cardReaderError = false;
 						this._cardReaderBuffer = BinaryFileReader
 								.toByteArray(this._programFile
 										.getAbsolutePath());
 						if (this._cardReaderBuffer.length >= 2) {
 							data = (this._cardReaderBuffer[0]) & 0xFF;
 							data += ((this._cardReaderBuffer[1] << 8) & 0xFFFF);
 						} else
 							data = 0;
 					} catch (FileNotFoundException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 					this._cardReaderBufferPos = 2;
 					this._cardReaderBuffered = true;
 				} else {
 					if (this._cardReaderBufferPos + 1 < this._cardReaderBuffer.length) {
 						data = (this._cardReaderBuffer[this._cardReaderBufferPos]) & 0xFF;
 						this._cardReaderBufferPos += 1;
 						data += ((this._cardReaderBuffer[this._cardReaderBufferPos] << 8) & 0xFFFF);
 						this._cardReaderBufferPos += 1;
 						if (this._cardReaderBufferPos >= this._cardReaderBuffer.length) {
 							this._cardReaderBuffered = false;
 						}
 					} else {
 						this._cardReaderError = true;
 						this._cardReaderBuffered = false;
 					}
 				}
 				break;
 		}
 		return data;
 	}
 
 	@Override
 	public int invokeDeviceStatusEvent(int devid) {
 		int status = 0;
 		switch (devid) {
 			case 0: // keyboard
 				if (this._keyboardBuffered
 						&& this._keyboardBufferPos < this._keyboardBuffer.length)
 					status = 1;
 				else {
 					this._keyboardBuffer = null;
 					this._keyboardBuffered = false;
 					this._keyboardBufferPos = Integer.MAX_VALUE;
 					status = 0;
 				}
 				break;
 			case 1: // printer
 				status = 0;
 				break;
 			case 2: // card reader
 				if (this._cardReaderError)
 					status = 2;
 				else if (this._cardReaderBuffered)
 					status = 1;
 				else
 					status = 0;
 				break;
 		}
 		return status;
 	}
 
 	@Override
 	public void invokeDeviceOutputEvent(int devid, int data) {
 		switch (devid) {
 			case 1: // printer
 				StringBuffer sb = new StringBuffer();
 				if (data == '\n')
 					sb.append("\n");
 				else if (data >= 32 && data < 127) {
 					sb.append((char) data);
 				} else
 					sb.append(Integer.toString(data));
 
 				final String text = sb.toString();
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						txtOutput.append(text);
 					}
 				});
 				break;
 		}
 	}
 
 	private void haltController() {
		_controller.stop();
 		synchronized (_controller) {
 			_controller.notify();
 		}
 	}
 }
