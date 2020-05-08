 package gauss2;
 /**
  * @author Roberto Kahn ( beto.java@hotmail.com )
  * @version 2.0/2012
  * Copyright 2012 Roberto Kahn
  */
 import java.awt.Container;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.InputMismatchException;
 import java.util.Scanner;
 
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class Gauss2  extends JFrame{
 	
 	Container panel;
 	JTextArea textArea1, textArea2;
 	JMenuItem menuNew, menuSave;
 	JButton buttonCalculate;
 	File outputFile, inputFile;
 	ActionHandler actionHandler;
 	private JMenuItem menuAbout, menuExit, menuHelp;
 	
 	public Gauss2() {	
 		super("Gauss");
 		createGUI();
 			}
 	
 	private void createGUI() {
 		
 		panel = getContentPane();
 //		setSize(350, 300);
 		
 		JMenuBar menuBar;
 		JMenu menu;
 		actionHandler = new ActionHandler();
 		
 		menuBar = new JMenuBar();
 		menu = new JMenu("Sistema");
 		menuBar.add(menu);
 		menuNew = new JMenuItem("Novo");
 		menuNew.addActionListener(actionHandler);
 		menu.add(menuNew);
 		menuSave = new JMenuItem("Salvar");
 		menuSave.addActionListener(actionHandler);
 		menu.add(menuSave);
 		menu.addSeparator();
 		menuExit = new JMenuItem("Sair");
 		menuExit.addActionListener(actionHandler);
 		menu.add(menuExit);
 		
 		menu = new JMenu("Ajuda");
 		menuBar.add(menu);
 		menuHelp = new JMenuItem("Como usar");
 		menuHelp.addActionListener(actionHandler);
 		menu.add(menuHelp);
 		menuAbout = new JMenuItem("Sobre");
 		menuAbout.addActionListener(actionHandler);
 		menu.add(menuAbout);
 		
 		setJMenuBar(menuBar);
 		
 		panel.setLayout(new BoxLayout(panel,BoxLayout.PAGE_AXIS));
 		
 		JLabel label1 = new JLabel("Este é o sistema que será resolvido:");
 		label1.setAlignmentX(CENTER_ALIGNMENT);
 		panel.add(label1);
 		textArea1 = new JTextArea(10,20);
 		JScrollPane sp1 = new JScrollPane(textArea1);
 		panel.add(sp1);
 		
 		buttonCalculate = new JButton("Resolve");
 		buttonCalculate.setAlignmentX(CENTER_ALIGNMENT);
 		panel.add(buttonCalculate);
 		buttonCalculate.addActionListener(actionHandler);
 		
 		JLabel label2 = new JLabel("Sistema escalonado com o vetor resposta:");
 		label2.setAlignmentX(CENTER_ALIGNMENT);
 		panel.add(label2);
 		textArea2 = new JTextArea(10,20);
 		JScrollPane sp2 = new JScrollPane(textArea2);
 		textArea2.setEditable(false);
 		panel.add(sp2);
 		
 		pack();
 		setVisible(true);
 	}
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 
 		Gauss2 m = new Gauss2();
 		m.addWindowListener(
 				new WindowAdapter() {
 					@Override
 					public void windowClosing(WindowEvent e) {
 						System.exit(0);
 					}
 				});
 	}
 //Inner class
 	class ActionHandler implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if (e.getSource()== menuNew) {
 				textArea1.setText("");
 				textArea2.setText("");
 				textArea1.repaint();
 				textArea2.repaint();
 			}
 			if (e.getSource() == buttonCalculate) {
 				solveTextArea1();
 			}
 			if (e.getSource() == menuSave) {//mudar isso tudo
 				String msg1 = "Digite o nome do arquivo para salvar os dados";
 				String nameFile = JOptionPane.showInputDialog(rootPane, msg1, "Salvar", JOptionPane.QUESTION_MESSAGE);
 				try{
 					File fl = new File(nameFile);
 					if (!fl.exists()) fl.createNewFile();
 					PrintStream ps=new PrintStream(fl);
 					ps.println(textArea1.getText());
 					ps.print(textArea2.getText());
 				}
 				catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 			if (e.getSource()==menuHelp) {
 				String msg = "Para usar esse programa você deve digitar o seu sistema na janela superior\n" +
 						" no formato a seguir:\nNa 1a linha o número de equações do sistema e nas outras o sistema.\n" +
 						"Ex. O sistema:\n1x+2y=3\n3x+2y=1\nVocê digita:\n2\n1 2 3\n3 2 1\nEspero que você goste.\n" +
 						"Dúvidas, comentários, sugestões: beto.java@hotmail.com\n" +
						"Código disponivel em: https://github.com/BetoKahn/Gauss\n" +
						"Copyright 2012 Roberto Kahn\nVocê pode copiar a vontade.";
 				JOptionPane.showMessageDialog(getParent(), msg, "Como usar", JOptionPane.PLAIN_MESSAGE);
 			}
 			if (e.getSource()==menuAbout) {
 				String text = "Este software foi escrito por Roberto Kahn\nbeto.java@hotmail.com\nCódigo disponivel em:\n" +
 						"http://github.com/BetoKahn/Gauss";
 				JOptionPane.showMessageDialog(null, text, "Gauss", JOptionPane.PLAIN_MESSAGE);
 			}
 			if (e.getSource()==menuExit) {
 				System.exit(0);
 			}
 		}
 
 		private void solveTextArea1() {
 			Scanner sc = new Scanner(textArea1.getText());
 			try{
 				int numberOfEquations = sc.nextInt();
 				double matrix[][] = new double[numberOfEquations][numberOfEquations+1];
 				int j = 0;
 				do  {
 					for (int i = 0; i <= numberOfEquations; i++){
 						matrix[j][i] = sc.nextDouble();
 					}
 					j++;
 				} while (sc.hasNextLine());
 				Sistemas s=new Sistemas(matrix);
 				textArea2.setText(s.toString());
 				textArea2.repaint();
 			} catch (InputMismatchException ex) {
 				String msg = "Você digitou um caractere errado\nTente de novo.";
 				JOptionPane.showMessageDialog(rootPane, msg, "Erro", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 	}
 }
