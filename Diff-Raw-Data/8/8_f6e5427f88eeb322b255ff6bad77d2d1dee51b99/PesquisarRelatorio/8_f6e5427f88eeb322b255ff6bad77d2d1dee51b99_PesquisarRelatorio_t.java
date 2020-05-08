 package view;
 
 import java.awt.Checkbox;
 import java.awt.Event;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.JButton;
 
 import control.RelatorioController;
 import exception.RelatorioException;
 
 import model.Relatorio;
 
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class PesquisarRelatorio extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField txtDataInicial;
 	private JTextField txtDataFinal;
 	private JTextField txtBarbeiro;
 	private JTextField txtServico;
 	public static int tipoBusca = 0;
	public static String servico = "servico";
	public static String barbeiro = "barbeiro";
	public static String dataInicial = "dataInicial";
	public static String dataFinal = "dataFinal";
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					PesquisarRelatorio frame = new PesquisarRelatorio();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public PesquisarRelatorio() {
 		setTitle("Tipo de Pesquisa do Relat\u00F3rio");
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 372, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new LineBorder(new Color(0, 0, 0)));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		JPanel panelData = new JPanel();
 		panelData.setBorder(new TitledBorder(UIManager
 				.getBorder("TitledBorder.border"), "Por Data",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panelData.setBounds(10, 11, 221, 97);
 		contentPane.add(panelData);
 		panelData.setLayout(null);
 
 		txtDataInicial = new JTextField();
 		txtDataInicial.setEnabled(false);
 		txtDataInicial.setBounds(10, 66, 94, 20);
 		panelData.add(txtDataInicial);
 		txtDataInicial.setText("dd/MM/aaaa");
 		txtDataInicial.setColumns(10);
 
 		txtDataFinal = new JTextField();
 		txtDataFinal.setEnabled(false);
 		txtDataFinal.setBounds(114, 66, 94, 20);
 		panelData.add(txtDataFinal);
 		txtDataFinal.setText("dd/MM/aaaa\r\n");
 		txtDataFinal.setColumns(10);
 
 		JLabel lblDataInicial = new JLabel("Data Inicial");
 		lblDataInicial.setBounds(10, 53, 86, 14);
 		panelData.add(lblDataInicial);
 
 		final Checkbox checkPorData = new Checkbox("Ativar");
 		checkPorData.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (checkPorData.getState() == false) {
 					txtDataInicial.setEnabled(false);
 					txtDataFinal.setEnabled(false);
 					txtDataInicial.setText("dd/MM/aaaa\r\n");
 					txtDataFinal.setText("dd/MM/aaaa\r\n");
 				} else {
 					txtDataInicial.setEnabled(true);
 					txtDataFinal.setEnabled(true);
 					txtDataInicial.setText("");
 					txtDataFinal.setText("");
 				}
 			}
 		});
 		checkPorData.setBounds(6, 23, 71, 23);
 		panelData.add(checkPorData);
 
 		JLabel lblDataFinal = new JLabel("Data Final");
 		lblDataFinal.setBounds(114, 53, 71, 14);
 		panelData.add(lblDataFinal);
 
 		JPanel panelBarbeiro = new JPanel();
 		panelBarbeiro.setLayout(null);
 		panelBarbeiro.setBorder(new TitledBorder(UIManager
 				.getBorder("TitledBorder.border"), "Por Barbeiro",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panelBarbeiro.setBounds(10, 119, 221, 62);
 		contentPane.add(panelBarbeiro);
 
 		txtBarbeiro = new JTextField();
 		txtBarbeiro.setText("Nome do barbeiro");
 		txtBarbeiro.setEnabled(false);
 		txtBarbeiro.setColumns(10);
 		txtBarbeiro.setBounds(71, 23, 140, 20);
 		panelBarbeiro.add(txtBarbeiro);
 
 		final Checkbox checkBarbeiro = new Checkbox("Ativar");
 		checkBarbeiro.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (checkBarbeiro.getState() == false) {
 					txtBarbeiro.setEnabled(false);
 					txtBarbeiro.setText("Nome do barbeiro");
 				} else {
 					txtBarbeiro.setEnabled(true);
 					txtBarbeiro.setText("");
 				}
 			}
 		});
 
 		checkBarbeiro.setBounds(6, 23, 59, 23);
 		panelBarbeiro.add(checkBarbeiro);
 
 		JPanel panelServico = new JPanel();
 		panelServico.setLayout(null);
 		panelServico.setBorder(new TitledBorder(UIManager
 				.getBorder("TitledBorder.border"), "Por Servi\u00E7o",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panelServico.setBounds(10, 189, 221, 62);
 		contentPane.add(panelServico);
 
 		txtServico = new JTextField();
 		txtServico.setText("Tipo de Servi\u00E7o");
 		txtServico.setEnabled(false);
 		txtServico.setColumns(10);
 		txtServico.setBounds(71, 23, 140, 20);
 		panelServico.add(txtServico);
 
 		final Checkbox checkServico = new Checkbox("Ativar");
 		checkServico.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				if (checkServico.getState() == false) {
 					txtServico.setEnabled(false);
 					txtServico.setText("Tipo de Servi\u00E7o");
 				} else {
 					txtServico.setEnabled(true);
 					txtServico.setText("");
 				}
 			}
 		});
 		checkServico.setBounds(6, 23, 59, 23);
 		panelServico.add(checkServico);
 
 		JButton btnConcluir = new JButton("Concluir");
 		btnConcluir.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				
 				
 				if (checkBarbeiro.getState() == true && checkPorData.getState() == false && checkServico.getState() == false) {
 					barbeiro = txtBarbeiro.getText();
 					tipoBusca = 1;
 				}
 				if (checkBarbeiro.getState() == true && checkPorData.getState() == false && checkServico.getState() == true) {
 					barbeiro = txtBarbeiro.getText();
 					servico = txtServico.getText();
 					tipoBusca = 2;
 				}
 				if (checkBarbeiro.getState() == true && checkPorData.getState() == true && checkServico.getState() == false) {
 					barbeiro = txtBarbeiro.getText();
 					dataInicial = txtDataInicial.getText();
 					dataFinal = txtDataFinal.getText();
 					tipoBusca = 3;
 				}
 				if (checkBarbeiro.getState() == true && checkPorData.getState() == true && checkServico.getState() == true) {
 					barbeiro = txtBarbeiro.getText();
 					dataInicial = txtDataInicial.getText();
 					dataFinal = txtDataFinal.getText();
 					servico = txtServico.getText();
 					tipoBusca = 4;
 				}
 				if (checkBarbeiro.getState() == false && checkPorData.getState() == false && checkServico.getState() == true) {
 					servico = txtServico.getText();
 					tipoBusca = 5;
 				}
 				if (checkBarbeiro.getState() == false && checkPorData.getState() == true && checkServico.getState() == true) {
 					dataInicial = txtDataInicial.getText();
 					dataFinal = txtDataFinal.getText();
 					servico = txtServico.getText();
 					tipoBusca = 6;
 				}
 				if (checkBarbeiro.getState() == false && checkPorData.getState() == true && checkServico.getState() == false) {
 					dataInicial = txtDataInicial.getText();
 					dataFinal = txtDataFinal.getText();
 					tipoBusca = 7;
 				}
 				if (tipoBusca == 0) {
 					JOptionPane.showMessageDialog(null, "Selecione uma opo de busca");
 				} else {
 					try {
 						VisualizarRelatorios frame = new VisualizarRelatorios();
 						frame.setVisible(true);
 						frame.setLocationRelativeTo(null);
 						dispose();
 					} catch (SQLException e) {
 						mostrarMensagemDeErro(e.getMessage());
 					} catch (RelatorioException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					
 				}
 				
 			}
 		});
 		btnConcluir.setBounds(241, 11, 105, 62);
 		contentPane.add(btnConcluir);
 
 		JButton btnVoltar = new JButton("Voltar");
 		btnVoltar.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				try {
 					VisualizarRelatorios frame = new VisualizarRelatorios();
 					frame.setVisible(true);
 					frame.setLocationRelativeTo(null);
 					dispose();
 				} catch (SQLException e1) {
 					mostrarMensagemDeErro(e1.getMessage());
 				} catch (RelatorioException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 		btnVoltar.setBounds(241, 228, 105, 23);
 		contentPane.add(btnVoltar);
 	}
 
 	public boolean action(Event evento, Object arg) {
 		return false;
 	}
 
 	private void mostrarMensagemDeErro(String informacao) {
 		JOptionPane.showMessageDialog(null, informacao, "Ateno",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 
 }
