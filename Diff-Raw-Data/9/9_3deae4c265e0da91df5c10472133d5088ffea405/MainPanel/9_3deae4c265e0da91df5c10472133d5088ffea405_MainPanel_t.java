 package br.com.caio.blackjack.main;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.GridBagLayout;
 import java.awt.CardLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.LayoutStyle.ComponentPlacement;
 
 import br.com.caio.blackjack.util.ProcessadorDeAcoes;
 import javax.swing.SwingConstants;
 
 public class MainPanel {
 
 	private JFrame frame;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					MainPanel window = new MainPanel();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public MainPanel() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 450, 300);
 		
 		final ProcessadorDeAcoes processador = new ProcessadorDeAcoes();
 		final JLabel lblSeusPontos = new JLabel("Seus Pontos: \n "+ processador.getPlayer().valorTotal());
 		
 		JLabel lblPontosDoAdversario = new JLabel("Pontos do adversario: ");
 		
 		JButton btnNovoJogo = new JButton("Novo Jogo");
 		
 		
 		JButton btnContinuar = new JButton("Mais uma carta");
 		
 		JButton btnParar = new JButton("Parar");
 		final JLabel suasCartas = new JLabel(processador.getPlayer().mostraMao());
 		suasCartas.setHorizontalAlignment(SwingConstants.CENTER);
 		
 		btnParar.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				
 			}
 		});
 		
 		
		
		btnNovoJogo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});
 		btnContinuar.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				processador.processaContinuar();
 				lblSeusPontos.setText("Seus Pontos: \n "+ processador.getPlayer().valorTotal());
 				suasCartas.setText(processador.getPlayer().mostraMao());
 			}
 		});
 		
 
 		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
 		groupLayout.setHorizontalGroup(
 			groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(62)
 							.addComponent(btnParar, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
 							.addGap(54)
 							.addComponent(btnContinuar))
 						.addComponent(btnNovoJogo, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE)
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(153)
 							.addComponent(lblPontosDoAdversario))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(164)
 							.addComponent(lblSeusPontos))
 						.addGroup(groupLayout.createSequentialGroup()
 							.addGap(22)
 							.addComponent(suasCartas, GroupLayout.PREFERRED_SIZE, 406, GroupLayout.PREFERRED_SIZE)))
 					.addContainerGap(22, Short.MAX_VALUE))
 		);
 		groupLayout.setVerticalGroup(
 			groupLayout.createParallelGroup(Alignment.TRAILING)
 				.addGroup(groupLayout.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(btnNovoJogo, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE)
 					.addGap(10)
 					.addComponent(lblPontosDoAdversario)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(suasCartas, GroupLayout.DEFAULT_SIZE, 123, Short.MAX_VALUE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(lblSeusPontos)
 					.addGap(18)
 					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnParar)
 						.addComponent(btnContinuar))
 					.addGap(21))
 		);
 		frame.getContentPane().setLayout(groupLayout);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 }
