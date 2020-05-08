 package View;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 import java.awt.BorderLayout;
 import java.awt.Graphics2D;
 import java.awt.SystemColor;
 import java.awt.Toolkit;
 
 import javax.swing.JFileChooser;
 
 import PDFCreator.ImStr;
 import PDFCreator.SavePdfSectionDialog;
 
 import com.github.sarxos.webcam.Webcam;
 import com.github.sarxos.webcam.WebcamPanel;
 
 import java.awt.Dimension;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JSpinner;
 import javax.swing.JTextArea;
 import javax.swing.JToolBar;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.awt.ScrollPane;
 
 import javax.swing.JSlider;
 import javax.swing.JLabel;
 
 import java.awt.Font;
 
 import javax.swing.JToggleButton;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import java.awt.Color;
 
 import javax.swing.JTextPane;
 import javax.swing.JCheckBox;
 public class MainWindow implements Runnable{
 
 	private JFrame frmAplicaoDeRealidade;
 	public final String IMG_FORMAT = ".png";
 	private CamRAPanel RAPanel;
 	private JToggleButton tglbtnAcionarAlgoritmo;
 	private JSlider sliderThreshold;
 	private boolean algoritmoLigado;
 	private JCheckBox chckbxImgBinria;
 	private JCheckBox chckbxDesativarRa;
 	private JCheckBox chckbxEixos;
 	
 	private boolean axes;
 	
 	private JToolBar toolBar;
 	private JButton buttonKmeans;
 	private JButton buttonFarthestFirst;
 	private JButton buttonHierarchical;
 	private JButton buttonLinear;
 	private JButton buttonPolinomial;
 	private JPanel panelConfig;
 	private String algorithmSelected;
 	private JSpinner spinnerKmeans;
 	private JSpinner spinnerFarthestFirst;
 	private JSpinner spinnerHierarchical;
 	private JSpinner spinnerPolinomio;
 	private JLabel functionLinear;
 	private JLabel functionPolinomioValue;
 	private JComboBox linkTypesComboBox;
 	
 	private ArrayList<ImStr> pdfItemList;
 	
 
 	public MainWindow() {
 		initialize();
 	}
 	
 	private void initialize() {
 		pdfItemList  = new ArrayList<ImStr>();
 		
 		algoritmoLigado = false;
 		algorithmSelected = "";		
 		
 		setFrmAplicaoDeRealidade(new JFrame());
 		getFrmAplicaoDeRealidade().setIconImage(new ImageIcon("icons/iconWindow.png").getImage());
 		getFrmAplicaoDeRealidade().setPreferredSize(new Dimension(1000, 600));
 		getFrmAplicaoDeRealidade().pack();
 		getFrmAplicaoDeRealidade().setTitle("Seta Realidade Aumentada");
 		getFrmAplicaoDeRealidade().getContentPane().setLayout(null);
 		camConfig();		
 		setRAPanel(new CamRAPanel(Webcam.getDefault().getImage()));
 		getFrmAplicaoDeRealidade().getContentPane().add(getRAPanel());
 		
 		getFrmAplicaoDeRealidade().setForeground(SystemColor.inactiveCaptionBorder);
 		
 		getFrmAplicaoDeRealidade().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		//Tool Bar
 		createToolBar();
 		
 		setSliderThreshold(new JSlider(JSlider.HORIZONTAL, 0, 255, 110));
 		getSliderThreshold().setMinorTickSpacing(5);
 		getSliderThreshold().setMajorTickSpacing(20);
 		getSliderThreshold().setBounds(655, 31, 300, 40);
 		getSliderThreshold().setPaintTicks(true);
 		getSliderThreshold().setPaintLabels(true);
 		getSliderThreshold().setFont(new Font("Tahoma", Font.PLAIN, 11));
 		frmAplicaoDeRealidade.getContentPane().add(getSliderThreshold());
 		
 		JLabel lblNewLabel = new JLabel("Threshold: ");
 		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblNewLabel.setBounds(661, 11, 136, 23);
 		frmAplicaoDeRealidade.getContentPane().add(lblNewLabel);
 		
 		JLabel lblNewLabel2 = new JLabel("Defina o limite de limiarizao da imagem.");
 		lblNewLabel2.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblNewLabel2.setBounds(730, 11, 270, 23);
 		frmAplicaoDeRealidade.getContentPane().add(lblNewLabel2);
 		
 		generatePainelInitial();
 		
 		
 		JLabel lblPainelAlgoritmo = new JLabel("Painel Algoritmo:");
 		lblPainelAlgoritmo.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblPainelAlgoritmo.setBounds(665, 115, 117, 14);
 		frmAplicaoDeRealidade.getContentPane().add(lblPainelAlgoritmo);
 		
 		chckbxImgBinria = new JCheckBox("Img Bin\u00E1ria");
 		chckbxImgBinria.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		chckbxImgBinria.setBounds(708, 80, 79, 23);
 		frmAplicaoDeRealidade.getContentPane().add(chckbxImgBinria);
 		
 		chckbxEixos = new JCheckBox("Eixos");
 		chckbxEixos.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		chckbxEixos.setBounds(789, 80, 47, 23);
 		frmAplicaoDeRealidade.getContentPane().add(chckbxEixos);
 		
 		chckbxDesativarRa = new JCheckBox("Desativar RA");
 		chckbxDesativarRa.setFont(new Font("Tahoma", Font.PLAIN, 9));
 		chckbxDesativarRa.setBounds(840, 80, 86, 23);
 		frmAplicaoDeRealidade.getContentPane().add(chckbxDesativarRa);
 		
 		
 		JMenuBar menuBar = new JMenuBar();
 		getFrmAplicaoDeRealidade().setJMenuBar(menuBar);
 		
 		JMenu mnArquivo = new JMenu("Arquivo");
 		menuBar.add(mnArquivo);
 		JMenuItem mntmSalvarImagem = new JMenuItem("Salvar Imagem...");
 		mntmSalvarImagem.addActionListener(actionListSalvarImg());
 		mnArquivo.add(mntmSalvarImagem);
 		JMenuItem mntmSair = new JMenuItem("Sair");
 		mntmSair.addActionListener(closeFrame());
 		mnArquivo.add(mntmSair);
 		
 		
 		JMenu mnClusterers = new JMenu("Clusterizao");
 		menuBar.add(mnClusterers);
 		JMenuItem mntmKmeans = new JMenuItem("K Means");
 		mntmKmeans.addActionListener(this.buttonKmeansAction());
 		mnClusterers.add(mntmKmeans);
 		JMenuItem mntmFarthestFirst = new JMenuItem("Farthest First");
 		mntmFarthestFirst.addActionListener(this.buttonFarthestFirstAction());
 		mnClusterers.add(mntmFarthestFirst);
 		JMenuItem mntmHierarchical = new JMenuItem("Hierrquico");
 		mntmHierarchical.addActionListener(this.buttonHierarchicalAction());
 		mnClusterers.add(mntmHierarchical);
 				
 		JMenu mnRegressao = new JMenu("Regresso");
 		menuBar.add(mnRegressao);
 		JMenuItem mntmLinear = new JMenuItem("Linear");
 		mntmLinear.addActionListener(this.buttonLinearAction());
 		mnRegressao.add(mntmLinear);
 		JMenuItem mntmPolinomial = new JMenuItem("Polinomial");
 		mntmPolinomial.addActionListener(this.buttonPolinomialAction());
 		mnRegressao.add(mntmPolinomial);
 		
 		
 	}
 
 	public void generatePainelInitial() {
 		panelConfig = new JPanel();
 		panelConfig.setBorder(new LineBorder(Color.GRAY, 1, true));
 		panelConfig.setBounds(666, 140, 309, 385);
 		frmAplicaoDeRealidade.getContentPane().add(panelConfig);
 		JLabel initialText = new JLabel("Selecione um algoritmo na barra acima da image.");
 		initialText.setFont(new Font("Tahoma", Font.BOLD, 11));
 		panelConfig.add(initialText);
 	}
 
 	public void createToolBar() {
 		toolBar = new JToolBar();
 		
 		buttonKmeans = new JButton();
 		buttonKmeans.setIcon(new ImageIcon("icons/kmeans.jpg"));
 		buttonKmeans.addActionListener(this.buttonKmeansAction());
 		toolBar.add(buttonKmeans);
 		
 		buttonFarthestFirst = new JButton();
 		buttonFarthestFirst.setIcon(new ImageIcon("icons/farthestfirst.jpg"));
 		buttonFarthestFirst.addActionListener(this.buttonFarthestFirstAction());
 		toolBar.add(buttonFarthestFirst);
 		
 		buttonHierarchical = new JButton();
 		buttonHierarchical.setIcon(new ImageIcon("icons/hierarquico.jpg"));
 		buttonHierarchical.addActionListener(this.buttonHierarchicalAction());
 		toolBar.add(buttonHierarchical);
 		
 		buttonLinear = new JButton();
 		buttonLinear.setIcon(new ImageIcon("icons/linear.jpg"));
 		buttonLinear.addActionListener(this.buttonLinearAction());
 		toolBar.add(buttonLinear);
 		
 		buttonPolinomial = new JButton();
 		buttonPolinomial.setIcon(new ImageIcon("icons/polinomial.jpg"));
 		buttonPolinomial.addActionListener(this.buttonPolinomialAction());
 		toolBar.add(buttonPolinomial);
 		
 		toolBar.setBounds(0, 0, 645, 40);
 		toolBar.setFloatable(false);
 		frmAplicaoDeRealidade.getContentPane().add(toolBar);
 	}
 
 	public ActionListener closeFrame() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				getFrmAplicaoDeRealidade().dispose();
 			}
 		};
 	}
 
 	public void camConfig() {
 		Webcam webcam = Webcam.getDefault();
 		webcam.setViewSize(new Dimension(640, 480));
 		
 		WebcamPanel panelCam = new WebcamPanel(webcam);
 	}
 	
 	public ActionListener actionListSalvarImg() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				BufferedImage image = new BufferedImage(RAPanel.getWidth(), RAPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                 Graphics2D graphics2D = image.createGraphics();
                 RAPanel.paint(graphics2D);
                 
                 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
         		Calendar cal = Calendar.getInstance();
				String caminhoArquivo = "tmp/" +dateFormat.format(cal.getTime())+".png";
 				Model.Cam cam = new Model.Cam();
 				cam.saveImage(caminhoArquivo, image);
				SavePdfSectionDialog savePdf = new SavePdfSectionDialog(pdfItemList,caminhoArquivo);
 			}
 		};
 	}
 	
 	public ActionListener buttonKmeansAction() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				algorithmSelected = "kmeans";
 				panelConfig.removeAll();
 				JLabel kmeans = new JLabel("K-means");
 				kmeans.setFont(new Font("Tahoma", Font.BOLD, 11));
 				panelConfig.add(kmeans);
 				ImageIcon imageKmeans = new ImageIcon("textos/kmeans.jpg"); 
 				JLabel imageKmeansLabel = new JLabel(); 
 				imageKmeansLabel.setIcon(imageKmeans); 
 				panelConfig.add(imageKmeansLabel);
 				JLabel numeroPontos = new JLabel("Nmero de pontos detectados: ");
 				numeroPontos.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(numeroPontos);
 				JLabel numeroPontosField = new JLabel("1");
 				numeroPontosField.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(numeroPontosField);
 				JLabel numeroClustersField = new JLabel("Defina o nmero de clusters:");
 				numeroClustersField.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(numeroClustersField);
 				SpinnerNumberModel model = new SpinnerNumberModel(1.0, 1.0, 30.0, 1.0);  
 				spinnerKmeans = new JSpinner(model);
 				JFormattedTextField tf = ((JSpinner.DefaultEditor)spinnerKmeans.getEditor())
 						.getTextField();
 						  tf.setEditable(false);
 				panelConfig.add(spinnerKmeans);
 				panelConfig.revalidate();
 				panelConfig.repaint();
 				
 			}
 		};
 	}
 	
 	
 	public ActionListener buttonFarthestFirstAction() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				algorithmSelected = "farthestfirst";
 				panelConfig.removeAll();
 				JLabel farthestFirst  = new JLabel("Farthest First");
 				farthestFirst.setFont(new Font("Tahoma", Font.BOLD, 11));
 				panelConfig.add(farthestFirst);
 				JLabel numeroClustersField = new JLabel("Defina o nmero de clusters:");
 				numeroClustersField.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(numeroClustersField);
 				SpinnerNumberModel model = new SpinnerNumberModel(1.0, 1.0, 30.0, 1.0);  
 				spinnerFarthestFirst = new JSpinner(model);
 				JFormattedTextField tf = ((JSpinner.DefaultEditor)spinnerFarthestFirst.getEditor())
 						.getTextField();
 						  tf.setEditable(false);
 				panelConfig.add(spinnerFarthestFirst);
 				panelConfig.revalidate();
 				panelConfig.repaint();
 				
 			}
 		};
 	}
 	
 	public ActionListener buttonHierarchicalAction() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				algorithmSelected = "hierarchical";
 				panelConfig.removeAll();
 				JLabel hierarchical = new JLabel("Hierarquico");
 				hierarchical.setFont(new Font("Tahoma", Font.BOLD, 11));
 				panelConfig.add(hierarchical);
 				JLabel numeroClustersField = new JLabel("Defina o nmero de clusters:");
 				numeroClustersField.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(numeroClustersField);
 				SpinnerNumberModel model = new SpinnerNumberModel(1.0, 1.0, 30.0, 1.0);  
 				spinnerHierarchical = new JSpinner(model);
 				JFormattedTextField tf = ((JSpinner.DefaultEditor)spinnerHierarchical.getEditor())
 						.getTextField();
 						  tf.setEditable(false);
 				panelConfig.add(spinnerHierarchical);
 				String[] linkTypes = { "SINGLE", "COMPLETE", "AVERAGE", "MEAN", "CENTROID", "WARD", "ADJCOMLPETE", "NEIGHBOR_JOINING" };
 				linkTypesComboBox = new JComboBox(linkTypes);
 				linkTypesComboBox.setSelectedIndex(1);
 				panelConfig.add(linkTypesComboBox);
 				panelConfig.revalidate();
 				panelConfig.repaint();
 				
 			}
 		};
 	}
 	
 	public ActionListener buttonLinearAction() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				algorithmSelected = "linear";
 				panelConfig.removeAll();
 				JLabel linear = new JLabel("Linear");
 				linear.setFont(new Font("Tahoma", Font.BOLD, 11));
 				panelConfig.add(linear);
 				ImageIcon imageLinear = new ImageIcon("textos/linear.jpg"); 
 				JLabel imageLinearLabel = new JLabel(); 
 				imageLinearLabel.setIcon(imageLinear); 
 				panelConfig.add(imageLinearLabel);
 				JLabel linearFuncao = new JLabel("Fun\u00E7\u00E3o Linear encontrada:");
 				linearFuncao.setFont(new Font("Tahoma", Font.BOLD, 11));
 				panelConfig.add(linearFuncao);
 				functionLinear = new JLabel("");
 				functionLinear.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(functionLinear);
 				panelConfig.revalidate();
 				panelConfig.repaint();
 				
 			}
 		};
 	}
 	public ActionListener buttonPolinomialAction() {
 		return new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				algorithmSelected = "polinomial";
 				panelConfig.removeAll();
 				JLabel kmeans = new JLabel("Polinomial");
 				kmeans.setFont(new Font("Tahoma", Font.BOLD, 11));
 				panelConfig.add(kmeans);
 				ImageIcon imagePolinomial = new ImageIcon("textos/polinomial.jpg"); 
 				JLabel imagePolinomialLabel = new JLabel(); 
 				imagePolinomialLabel.setIcon(imagePolinomial); 
 				panelConfig.add(imagePolinomialLabel);
 				JLabel grauPolinomio = new JLabel("Defina o grau do polinomio:");
 				grauPolinomio.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(grauPolinomio);
 				SpinnerNumberModel modelPolinomio = new SpinnerNumberModel(2.0, 2.0, 3.0, 1.0);  
 				spinnerPolinomio = new JSpinner(modelPolinomio);
 				JFormattedTextField tfPolinomio = ((JSpinner.DefaultEditor)spinnerPolinomio.getEditor())
 						.getTextField();
 				tfPolinomio.setEditable(false);
 				panelConfig.add(spinnerPolinomio);
 				JLabel functionPolinomio = new JLabel("Fun\u00E7\u00E3o Polinomial encontrada:");
 				functionPolinomio.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(functionPolinomio);
 				functionPolinomioValue = new JLabel("");
 				functionPolinomioValue.setFont(new Font("Tahoma", Font.PLAIN, 11));
 				panelConfig.add(functionPolinomioValue);
 				panelConfig.revalidate();
 				panelConfig.repaint();
 				
 			}
 		};
 	}
 	
 	public void setFileChooser(JFileChooser arquivo) {
 		arquivo.setFileFilter(setFileFilter());
 		arquivo.setApproveButtonText("Salvar");
 	}
 	
 	public FileFilter setFileFilter () {
 		return new javax.swing.filechooser.FileFilter(){
 				public boolean accept(File f){
 					return f.getName().toLowerCase().endsWith(IMG_FORMAT) || f.isDirectory();
 				}
 				public String getDescription() {
 					return "Arquivos de imagem "+ IMG_FORMAT;
 				}
 			};
 	}
 
 	public JFrame getFrmAplicaoDeRealidade() {
 		return frmAplicaoDeRealidade;
 	}
 
 	private void setFrmAplicaoDeRealidade(JFrame frmAplicaoDeRealidade) {
 		this.frmAplicaoDeRealidade = frmAplicaoDeRealidade;
 	}
 
 	@Override
 	public void run() {
 		try {
 			frmAplicaoDeRealidade.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public CamRAPanel getRAPanel() {
 		return RAPanel;
 	}
 
 	private void setRAPanel(CamRAPanel rAPanel) {
 		RAPanel = rAPanel;
 		RAPanel.setSize(640, 480);
 	}
 
 	public JSlider getSliderThreshold() {
 		return sliderThreshold;
 	}
 
 	public void setSliderThreshold(JSlider sliderThreshold) {
 		this.sliderThreshold = sliderThreshold;
 	}
 
 	public JToggleButton getTglbtnAcionarAlgoritmo() {
 		return tglbtnAcionarAlgoritmo;
 	}
 
 	public void setTglbtnAcionarAlgoritmo(JToggleButton tglbtnAcionarAlgoritmo) {
 		this.tglbtnAcionarAlgoritmo = tglbtnAcionarAlgoritmo;
 	}
 
 	public boolean isAlgoritmoLigado() {
 		return algoritmoLigado;
 	}
 
 	public void setAlgoritmoLigado(boolean algoritmoLigado) {
 		this.algoritmoLigado = algoritmoLigado;
 	}
 
 	public String getAlgorithmSelected() {
 		return algorithmSelected;
 	}
 
 	public void setAlgorithmSelected(String algorithmSelected) {
 		this.algorithmSelected = algorithmSelected;
 	}
 
 	public JSpinner getSpinnerKmeans() {
 		return spinnerKmeans;
 	}
 
 	public void setSpinnerKmeans(JSpinner spinnerKmeans) {
 		this.spinnerKmeans = spinnerKmeans;
 	}
 
 	public JLabel getFunctionLinear() {
 		return functionLinear;
 	}
 
 	public void setFunctionLinear(JLabel functionLinear) {
 		this.functionLinear = functionLinear;
 	}
 
 	public JLabel getFunctionPolinomioValue() {
 		return functionPolinomioValue;
 	}
 
 	public void setFunctionPolinomioValue(JLabel functionPolinomioValue) {
 		this.functionPolinomioValue = functionPolinomioValue;
 	}
 
 	public JSpinner getSpinnerPolinomio() {
 		return spinnerPolinomio;
 	}
 
 	public void setSpinnerPolinomio(JSpinner spinnerPolinomio) {
 		this.spinnerPolinomio = spinnerPolinomio;
 	}
 
 	public JSpinner getSpinnerFarthestFirst() {
 		return spinnerFarthestFirst;
 	}
 
 	public void setSpinnerFarthestFirst(JSpinner spinnerFarthestFirst) {
 		this.spinnerFarthestFirst = spinnerFarthestFirst;
 	}
 
 	public JSpinner getSpinnerHierarchical() {
 		return spinnerHierarchical;
 	}
 
 	public void setSpinnerHierarchical(JSpinner spinnerHierarchical) {
 		this.spinnerHierarchical = spinnerHierarchical;
 	}
 
 	public JComboBox getLinkTypesComboBox() {
 		return linkTypesComboBox;
 	}
 
 	public void setLinkTypesComboBox(JComboBox linkTypesComboBox) {
 		this.linkTypesComboBox = linkTypesComboBox;
 	}
 
 	public JCheckBox getChckbxImgBinria() {
 		return chckbxImgBinria;
 	}
 
 	public void setChckbxImgBinria(JCheckBox chckbxImgBinria) {
 		this.chckbxImgBinria = chckbxImgBinria;
 	}
 
 	public JCheckBox getChckbxDesativarRa() {
 		return chckbxDesativarRa;
 	}
 
 	public void setChckbxDesativarRa(JCheckBox chckbxDesativarRa) {
 		this.chckbxDesativarRa = chckbxDesativarRa;
 	}
 
 	public JCheckBox getChckbxEixos() {
 		return chckbxEixos;
 	}
 
 	public void setChckbxEixos(JCheckBox chckbxEixos) {
 		this.chckbxEixos = chckbxEixos;
 	}
 }
