 package mpa.manager.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.text.MaskFormatter;
 
 import mpa.manager.bean.MpaConfiguracao;
 import mpa.manager.control.MpaControl;
 import net.miginfocom.swing.MigLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 
 public class MpaNovo extends JFrame {
 
     private static final long serialVersionUID = 2L;
     private JPanel contentPane;
     private JTextArea taDuplas;
     private JFormattedTextField tfDataInicio;
     private JFormattedTextField tfDataFim;
     private MpaControl controller;
     private SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
     private JButton btnSalvar;
     private JCheckBox chkAdicionar;
     private JComboBox cbMpaOrigem;
     private JLabel lblInicio;
     private JLabel lblFim;
     
     public MpaNovo(Date data, String devs) throws ParseException {
         super();
         controller = new MpaControl();
 
         setTitle("Novo Mpa");
 
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         contentPane = new JPanel(new MigLayout("", "[grow]", "[][grow][]"));
        setBounds(100, 100, 480, 500);
         setContentPane(contentPane);
         
         chkAdicionar = new JCheckBox("Adicionar a:");
         chkAdicionar.addActionListener(new ActionListener() {
         	public void actionPerformed(ActionEvent e) {
         		refreshTipoGeracao();
         	}
         });
         contentPane.add(chkAdicionar, "flowx,cell 0 0,alignx center,aligny center");
         
         cbMpaOrigem = new JComboBox();
         contentPane.add(cbMpaOrigem, "cell 0 0,alignx center,aligny center");
         
         lblInicio = new JLabel("Início:");
         contentPane.add(lblInicio, "cell 0 0,alignx center,aligny center");
         
         tfDataInicio = new JFormattedTextField(new MaskFormatter("##/##/####"));
         contentPane.add(tfDataInicio, "cell 0 0,shrinkx 0,alignx center,aligny center");
         
         lblFim = new JLabel("Fim:");
         contentPane.add(lblFim, "cell 0 0,alignx center,gapx 10,aligny center");
         
         tfDataFim = new JFormattedTextField(new MaskFormatter("##/##/####"));
         contentPane.add(tfDataFim, "cell 0 0,shrinkx 0,alignx center,aligny center");
         
         taDuplas = new JTextArea();
         taDuplas.setText(devs);
         taDuplas.setBorder(BorderFactory.createEtchedBorder());
         contentPane.add(taDuplas, "cell 0 1,grow");
         
         btnSalvar = new JButton("Salvar");
         btnSalvar.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 new Thread() {
                     public void run() { salvarMpa(); };
                 }.start();
             }
         });
         contentPane.add(btnSalvar, "cell 0 2,alignx right,aligny center");
 
         inicializaData(data);
         preencheMpasOrigem();
         refreshTipoGeracao();
     }
 
 	private void refreshTipoGeracao() {
 		boolean atualizar = chkAdicionar.isSelected();
 		tfDataInicio.setEnabled(!atualizar);
 		tfDataFim.setEnabled(!atualizar);
 		cbMpaOrigem.setEnabled(atualizar);
 	}
 
 	private void inicializaData(Date data) {
         Calendar c = Calendar.getInstance();
         c.setTime(data);
         c.add(Calendar.DATE, 1);
         tfDataInicio.setText(format.format(c.getTime()));
         c.add(Calendar.DATE, 6);
         tfDataFim.setText(format.format(c.getTime()));
     }
 	
 	private void preencheMpasOrigem() {
 		cbMpaOrigem.removeAllItems();
		List<MpaConfiguracao> mpas = controller.getMpasEditaveis();
		
		for (MpaConfiguracao mpaConf : mpas)
 			cbMpaOrigem.addItem(mpaConf);
		
		if (mpas.isEmpty())
			chkAdicionar.setEnabled(false);
 	}
 
     private void salvarMpa() {
     	try {
         	if (chkAdicionar.isSelected())
         		controller.atualizaMpaComMesas((MpaConfiguracao) cbMpaOrigem.getSelectedItem(), taDuplas.getText());
         	else
 	            controller.criaMpaComMesas(format.parse(tfDataInicio.getText()), format.parse(tfDataFim.getText()), taDuplas.getText());
 	            
             dispose();
         } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Formato de data inválido (Utilize: dd/mm/aaaa)");
         } catch (Exception e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(this, e.toString());
         }
     }
 }
