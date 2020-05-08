 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * GUI_Manager.java
  *
  * Created on 11/Nov/2010, 17:49:28
  */
 
 package gui_manager;
 
 import bd.DBHandler;
 import com.toedter.calendar.JCalendar;
 import gestores.*;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.text.NumberFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.ListModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import outros.Consts;
 import outros.OurListModel;
 import outros.Utils;
 
 /**
  *
  * @author Daniela
  */
 public class GUI_Manager extends javax.swing.JFrame implements PropertyChangeListener {
     //DATA BASE
     private DBHandler db;
 
     //Gestores
     private GestorUtilizadores gestorUtilizadores;
     private GestorFilmes gestorFilmes;
     private GestorClientes gestorClientes;
     private GestorEmpregados gestorEmpregados ;
     private GestorMaquinas gestorMaquinas ;
     private GestorEstatisticas gestorEstatisticas;
 
     //Variaveis adicionais
     private String filePath;
     private GregorianCalendar calendarBegin;
     private GregorianCalendar calendarEnd;
     private JCalendar jCalendarBegin;
     private JCalendar jCalendarEnd;
     private Vector<String> generosVector;
     
     
     
     public static void main(String args[]) {
     	Utils.dbg("Here");
         
         new GUI_Manager().setVisible(true);
         //adds the panels to the interface
 
         Scanner sc = new Scanner(System.in);
         sc.next();
     }
     
     
     
     
     /** Creates new form GUI_Manager */
     public GUI_Manager() {
 
 
         generosVector=new Vector<String>();
 
 
         filePath="";
         gestorUtilizadores=new GestorUtilizadores();
         gestorMaquinas =new GestorMaquinas();
         gestorEmpregados =new GestorEmpregados();
         gestorClientes=new GestorClientes();
         gestorFilmes=new GestorFilmes();
         gestorEstatisticas=new GestorEstatisticas();
 
         initComponents();
     //initializes all the componentes needed in the GUI
 
         setSize(800, 600);
 	setTitle("Manager");
 	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         initComponents();
         add(mainPanel);
         
         mainPanel.setVisible(true);
         init();
 
         //Cria um button Group para 2 radio buttons
         bgroup=new ButtonGroup();
         
         bgroup.add(adminRadio);
         bgroup.add(opRadio);
 
         //Listners para as listas
         listnersListas();
     }
 
       public void init(){
 
         mainPanel.setLayout(null);
         
         
         mainPanel.add(jLoginPanel);
         
         mainPanel.add(jMenuAdministradorPanel);
         mainPanel.add(jMenuOperatorPanel);
         
         //Set panel's visibility
         
         
         jLoginPanel.setVisible(true);
        
         jMenuOperatorPanel.setVisible(false);
         jMenuAdministradorPanel.setVisible(false);
        
         //Alignes the panels in the frame
 
         jAdicionarFilmePanel.setBounds(0, -1, 800, 600);
         jEliminarFilmePanel.setBounds(0, -1, 800, 600);
         jLoginPanel.setBounds(0, -1, 800, 600);
         jGenerosPanel.setBounds(0, -1, 800, 600);
         jMenuOperatorPanel.setBounds(0, -1, 800, 600);
         jMenuAdministradorPanel.setBounds(0, -1, 800, 600);
         jResultadosFilmePanel.setBounds(0, -1, 800, 600);
         jPesqisaFilmesPanel.setBounds(0, -1, 800, 600);
         jAdicionarClientePanel.setBounds(0, -1, 800, 600);
         jEliminarClientePanel.setBounds(0, -1, 800, 600);
         jNotificarClientePanel.setBounds(0, -1, 800, 600);
         jPesquisarClientePanel.setBounds(0, -1, 800, 600);
 
     }
 
       //Listners das Listas
 
       public void listnersListas(){
 
              listaRequisicoes1.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         String idReq=((String)listaRequisicoes1.getSelectedValue()).split(" ")[0];
                         jTextField3.setText(gestorFilmes.calcularPrecoRequisicao(idReq));
                     }
                 }
             });
 
             pagamentosAtraso1.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         String bi=(String) pagamentosAtraso1.getSelectedValue();
                         bi=bi.split(" ")[2];
                         bi=bi.substring(1, bi.length()-1);
                         Utils.dbg(bi);
                         listaRequisicoes1.setModel(new OurListModel(gestorFilmes.verListaRequisicoesPorEntregarClienteBI(bi)));
                     }
                 }
             });
 
             listaRequisicoes.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         String idReq=((String)listaRequisicoes.getSelectedValue()).split(" ")[0];
                         jTextField2.setText(gestorFilmes.calcularPrecoRequisicao(idReq));
                     }
                 }
             });
 
             pagamentosAtraso.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         String bi=(String) pagamentosAtraso.getSelectedValue();
                         bi=bi.split(" ")[2];
                         bi=bi.substring(1, bi.length()-1);
                         Utils.dbg(bi);
                         listaRequisicoes.setModel(new OurListModel(gestorFilmes.verListaRequisicoesPorEntregarClienteBI(bi)));
                     }
                 }
             });
 
             listaResultados.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         // TODO add your handling code here:
                         String idMovie=((String)listaResultados.getSelectedValue()).split(" ")[0];
                         //"ID_FIL", "TITULO", "ANO", "REALIZADOR", "RANKIMDB", "PAIS", "PRODUTORA", "DESCRICAO", "CAPA", "VALIDO"
                         String[] f = gestorFilmes.getFilme(idMovie);
                         String file="";
                         int i=1; // i=1 em vez de i=0 -> saltar campo ID_FIL
                         tituloResultadosFilme.setText(f[i++]);
                         anoResultadosFilme.setText(f[i++]);
                         realizadorResultadosFilme.setText(f[i++]);
                         imdbResultadosFilme.setText(f[i++]);
                         paisResultadosFilme.setText(f[i++]);
                         produtorResultadosFilme.setText(f[i++]);
                         jTextArea14.setText(f[i++]);
                         try{
                             file=f[i++];
 
                             File ficheiro= new File(file);
                             if(ficheiro.exists())
                                 jLabel76.setIcon(new ImageIcon(file));
                         }catch (Exception e){
                             Utils.dbg("Não foi encontrada a capa do filme!");
                         }
                         // extrair os generos do fim do array
                         jList5.setModel(new OurListModel(Utils.extract(f, i+1))); // i+1 em vez de i -> saltar campo VALIDO
 
                     }
                 }
             });
 
             listaEmpregados.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         String []out;
                         
                         String idEmpregado=(String)listaEmpregados.getSelectedValue();
                         if(idEmpregado!=null){
                             idEmpregado=idEmpregado.split(" ")[2];
                             idEmpregado=idEmpregado.substring(1, idEmpregado.length()-1);
                             out=gestorEmpregados.procuraEmpregadoBI(idEmpregado);
                             biEmpregados.setText(idEmpregado);
                             if(out!=null&&out.length>0){
                                 if(out[1].equals("1")){
                                     adminRadio.doClick();
                                 }else{
                                     opRadio.doClick();
                                 }
                                 salarioEmpregados.setText(out[2]);
                                 nomeEmpregados.setText(out[3]);
                                 passwordEmpregados.setText(out[5]);
                                 passwordEmpregados2.setText(out[5]);
                                 moradaEmpregados.setText(out[6]);
                                 emailEmpregados.setText(out[7]);
                                 telefoneEmpregados.setText(out[8]);
                             }
 
                         }
 
                     }
                 }
             });
 
             listaResultadosClientes.addListSelectionListener((ListSelectionListener) new ListSelectionListener(){
                 public void valueChanged(ListSelectionEvent evento) {
                     if (evento.getValueIsAdjusting())
                     //ainda selecionando
                     return;
                     JList list = (JList)evento.getSource();
                     if (list.isSelectionEmpty()) {
                             Utils.dbg("nenhuma seleção");
                     } else {
                         String idCliente=((String)listaResultadosClientes.getSelectedValue()).split(" ")[0];
                         String []out=gestorClientes.procuraCliente(idCliente);
                         //"ID_PES", "NOME_PESSOA", "BI", "PASSWORD", "MORADA", "E_MAIL", "TELEFONE", "VALIDO", "DATA_REGISTO"};}
                         nomeResultadosClientes.setText(out[1]);
                         emailResultadosClientes.setText(out[5]);
                         telefoneResultadosClientes.setText(out[6]);
                         moradaResultadosClientes.setText(out[4]);
                         biResultadosClientes.setText(out[2]);
 
 
 
                     }
                 }
             });
 
       }
 
       //Datas
 
       private void resetDatas(){
                                 calendarEnd = null;
 				calendarBegin = null;
 				dateBegin.setText("00/00/00");
 				dateEnd.setText("00/00/00");
        }
 
 
       /* Every time the user selects a new date, an event is generated */
 		public void propertyChange(PropertyChangeEvent evt) {
 			Object source = evt.getSource();
 			Calendar cal;
 			if (source == jCalendarEnd) {
 				cal = jCalendarEnd.getCalendar();
 				calendarEnd = new GregorianCalendar(cal.get(Calendar.YEAR),
 						cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
 				dateEnd.setText(calendarEnd.get(Calendar.DAY_OF_MONTH) + "/"
 						+ (calendarEnd.get(Calendar.MONTH)+1) + "/"
 						+ calendarEnd.get(Calendar.YEAR));
 
 			} else {
 				cal = jCalendarBegin.getCalendar();
 				calendarBegin = new GregorianCalendar(cal.get(Calendar.YEAR),
 						cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
 				dateBegin.setText(calendarBegin.get(Calendar.DAY_OF_MONTH)
 						+ "/" + (calendarBegin.get(Calendar.MONTH)+1) + "/"
 						+ calendarBegin.get(Calendar.YEAR));
 			}
 
 		}
 
 
       //
 
 
 
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLoginPanel = new javax.swing.JPanel();
         jLoginButton = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jPasswordField = new javax.swing.JPasswordField();
         jUsernameField = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         jMenuAdministradorPanel = new javax.swing.JPanel();
         jLabel6 = new javax.swing.JLabel();
         jTabbedPane2 = new javax.swing.JTabbedPane();
         jEstatisticasPanel = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         statsArea = new javax.swing.JTextArea();
         javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
         empregadosCheckBox = new javax.swing.JCheckBox();
         contabilidadeBox = new javax.swing.JCheckBox();
         filmesCheckBox = new javax.swing.JCheckBox();
         maquinasCheckBox = new javax.swing.JCheckBox();
         javax.swing.JLabel jLabel10 = new javax.swing.JLabel();
         dateBegin = new javax.swing.JTextField();
         javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
         dateEnd = new javax.swing.JTextField();
         jButton5 = new javax.swing.JButton();
         dataInit = new javax.swing.JButton();
         dataEnd = new javax.swing.JButton();
         clientesCheckBox = new javax.swing.JCheckBox();
         consultarEstatisticas = new javax.swing.JButton();
         jClientesManagerPanel = new javax.swing.JPanel();
         jScrollPane6 = new javax.swing.JScrollPane();
         pagamentosAtraso = new javax.swing.JList();
         jLabel23 = new javax.swing.JLabel();
         jPesquisarClientesButton = new javax.swing.JToggleButton();
         jAdicionarClientesButton = new javax.swing.JToggleButton();
         jEliminarClientesButton = new javax.swing.JButton();
         jNotificarClientesButton = new javax.swing.JButton();
         jVerificarPagamentosAtrasoButton = new javax.swing.JButton();
         jLabel31 = new javax.swing.JLabel();
         verificarRequesicoes = new javax.swing.JButton();
         jLabel40 = new javax.swing.JLabel();
         jScrollPane25 = new javax.swing.JScrollPane();
         listaRequisicoes = new javax.swing.JList();
         jLabel100 = new javax.swing.JLabel();
         entregaFilme = new javax.swing.JButton();
         jLabel101 = new javax.swing.JLabel();
         jTextField2 = new javax.swing.JTextField();
         jEmpregadosManagerPanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel12 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel13 = new javax.swing.JLabel();
         jScrollPane4 = new javax.swing.JScrollPane();
         listaEmpregados = new javax.swing.JList();
         jDespedirEmpregadoButton = new javax.swing.JToggleButton();
         javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel15 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel16 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel17 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel18 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel19 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel20 = new javax.swing.JLabel();
         adminRadio = new javax.swing.JRadioButton();
         opRadio = new javax.swing.JRadioButton();
         javax.swing.JLabel jLabel21 = new javax.swing.JLabel();
         nomeEmpregados = new javax.swing.JTextField();
         passwordEmpregados = new javax.swing.JPasswordField();
         emailEmpregados = new javax.swing.JTextField();
         moradaEmpregados = new javax.swing.JTextField();
         javax.swing.JLabel jLabel22 = new javax.swing.JLabel();
         passwordEmpregados2 = new javax.swing.JPasswordField();
         jAdicionarEmpregadoButton = new javax.swing.JToggleButton();
         jScrollPane5 = new javax.swing.JScrollPane();
         outEmpregados = new javax.swing.JTextArea();
         actualzarListaEmpregados = new javax.swing.JButton();
         obterDadosEmpregadoActualizacao = new javax.swing.JButton();
         biEmpregados = new javax.swing.JTextField();
         telefoneEmpregados = new javax.swing.JTextField();
         salarioEmpregados = new javax.swing.JTextField();
         jLabel38 = new javax.swing.JLabel();
         jFilmesManagerPanel = new javax.swing.JPanel();
         jAdicionarFilmesToggleButton = new javax.swing.JToggleButton();
         jActualizarStockButton = new javax.swing.JToggleButton();
         jEliminarFilmeButton = new javax.swing.JToggleButton();
         jPesquisarButton = new javax.swing.JToggleButton();
         jGeneroButton = new javax.swing.JToggleButton();
         jFormatosFrameButton = new javax.swing.JButton();
         jATMManagerPanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         listaMaquinas = new javax.swing.JList();
         jVenderATMButton = new javax.swing.JButton();
         jAdicionarATMButton = new javax.swing.JButton();
         javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         outMaquinas = new javax.swing.JTextArea();
         jSpinner6 = new javax.swing.JSpinner();
         jSpinner6.setModel(new SpinnerNumberModel(2000.00,0.00,10000.00,0.50));
         javax.swing.JLabel jLabel97 = new javax.swing.JLabel();
         actualizarListaMaquinas = new javax.swing.JButton();
         jSairButton = new javax.swing.JButton();
         jMenuOperatorPanel = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jTabbedPane3 = new javax.swing.JTabbedPane();
         jClientesManagerPanel1 = new javax.swing.JPanel();
         jScrollPane12 = new javax.swing.JScrollPane();
         pagamentosAtraso1 = new javax.swing.JList();
         javax.swing.JLabel jLabel41 = new javax.swing.JLabel();
         pesquisarClientes = new javax.swing.JToggleButton();
         adicionarClientes = new javax.swing.JToggleButton();
         eliminarClientes = new javax.swing.JButton();
         notificarClientes = new javax.swing.JButton();
         jVerificarPagamentosAtrasoButton1 = new javax.swing.JButton();
         verificarRequesicoes1 = new javax.swing.JButton();
         jLabel32 = new javax.swing.JLabel();
         entregaFilme1 = new javax.swing.JButton();
         jTextField3 = new javax.swing.JTextField();
         jLabel102 = new javax.swing.JLabel();
         jScrollPane26 = new javax.swing.JScrollPane();
         listaRequisicoes1 = new javax.swing.JList();
         jLabel103 = new javax.swing.JLabel();
         jLabel104 = new javax.swing.JLabel();
         jFilmesManagerPanel1 = new javax.swing.JPanel();
         jToggleButton17 = new javax.swing.JToggleButton();
         jToggleButton18 = new javax.swing.JToggleButton();
         eliminaFilmes2 = new javax.swing.JToggleButton();
         pesquisarFilmes2 = new javax.swing.JToggleButton();
         adicionaGenero2 = new javax.swing.JToggleButton();
         jFormatosFrameButton1 = new javax.swing.JButton();
         jSairButton2 = new javax.swing.JButton();
         ficheirosFrame = new javax.swing.JFrame();
         jFileChooser1 = new javax.swing.JFileChooser();
         adicionarFilmeFrame = new javax.swing.JFrame();
         jAdicionarFilmePanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel53 = new javax.swing.JLabel();
         listaGenerosAdicionaFilmes = new javax.swing.JComboBox();
         jSpinner2 = new javax.swing.JSpinner();
         jSpinner2.setModel(new SpinnerNumberModel(5.0,1,10,0.1));
         textTituloAdicionaFilme = new javax.swing.JTextField();
         anoAdicionaFilmeSpinner = new javax.swing.JSpinner();
         anoAdicionaFilmeSpinner.setModel(new SpinnerNumberModel(2010,1917,2300,1));
         javax.swing.JLabel jLabel55 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel56 = new javax.swing.JLabel();
         textRealizadorAdicionaFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel57 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel58 = new javax.swing.JLabel();
         textProdutorAdicionaFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel60 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel61 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel54 = new javax.swing.JLabel();
         jScrollPane14 = new javax.swing.JScrollPane();
         textDescricaoAdicionaFilme = new javax.swing.JTextArea();
         javax.swing.JLabel jLabel59 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel62 = new javax.swing.JLabel();
         jEscolherFicheiroButton = new javax.swing.JToggleButton();
         adicionarFilme = new javax.swing.JButton();
         jSeparator2 = new javax.swing.JSeparator();
         javax.swing.JLabel jLabel63 = new javax.swing.JLabel();
         jScrollPane15 = new javax.swing.JScrollPane();
         outAdicionaFilme = new javax.swing.JTextArea();
         javax.swing.JLabel jLabel64 = new javax.swing.JLabel();
         idAdicionaStock = new javax.swing.JTextField();
         listaFormatosAdicionaFilme = new javax.swing.JComboBox();
         javax.swing.JLabel jLabel65 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel66 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel67 = new javax.swing.JLabel();
         custoAdicionaStock = new javax.swing.JTextField();
         custoAluguerAdicionaStock = new javax.swing.JTextField();
         adicionarStock = new javax.swing.JToggleButton();
         jScrollPane16 = new javax.swing.JScrollPane();
         outAdicionaStock = new javax.swing.JTextArea();
         voltarAdcionaFilmes = new javax.swing.JToggleButton();
         countriesList = new javax.swing.JComboBox();
         jLabel37 = new javax.swing.JLabel();
         qtdAdicionaStock = new javax.swing.JSpinner();
         generoExtra = new javax.swing.JButton();
         eliminarFilmesFrame = new javax.swing.JFrame();
         jEliminarFilmePanel = new javax.swing.JPanel();
         eliminaFilmes = new javax.swing.JToggleButton();
         idEliminaFilmes = new javax.swing.JTextField();
         javax.swing.JLabel jLabel68 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel72 = new javax.swing.JLabel();
         voltarEliminaFilmes = new javax.swing.JToggleButton();
         listarFormatoEliminar = new javax.swing.JButton();
         jScrollPane17 = new javax.swing.JScrollPane();
         textEliminaFilmes = new javax.swing.JList();
         jLabel70 = new javax.swing.JLabel();
         pesquisarFilmesFrame = new javax.swing.JFrame();
         jPesqisaFilmesPanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel85 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel86 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel87 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel88 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel89 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel90 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel91 = new javax.swing.JLabel();
         imdbBSpinner = new javax.swing.JSpinner();
         imdbBSpinner.setModel(new SpinnerNumberModel(1.0,1,10,0.1));
         imdbESpinner = new javax.swing.JSpinner();
         imdbESpinner.setModel(new SpinnerNumberModel(10.0,1,10,0.1));
         javax.swing.JLabel jLabel92 = new javax.swing.JLabel();
         textRealizadorPesquisaFilmes = new javax.swing.JTextField();
         textIdPesquisaFilmes = new javax.swing.JTextField();
         textTituloPesquisaFilmes = new javax.swing.JTextField();
         textProdutorPesquisaFilmes = new javax.swing.JTextField();
         pesquisarFilme = new javax.swing.JToggleButton();
         voltarPesquisarFilmes = new javax.swing.JToggleButton();
         countriesList1 = new javax.swing.JComboBox();
         jLabel94 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel95 = new javax.swing.JLabel();
         anoBSpinner = new javax.swing.JSpinner();
         anoBSpinner.setModel(new SpinnerNumberModel(1917,1917,2300,1));
         anoESpinner = new javax.swing.JSpinner();
         anoESpinner.setModel(new SpinnerNumberModel(2011,1917,2300,1));
         pesquisarTodos = new javax.swing.JButton();
         procurarID = new javax.swing.JButton();
         generosBox = new javax.swing.JComboBox();
         jLabel39 = new javax.swing.JLabel();
         generosFrame = new javax.swing.JFrame();
         jGenerosPanel = new javax.swing.JPanel();
         jScrollPane21 = new javax.swing.JScrollPane();
         listaGeneros = new javax.swing.JList();
         javax.swing.JLabel jLabel71 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel73 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel93 = new javax.swing.JLabel();
         textGenero = new javax.swing.JTextField();
         adicionaGenero = new javax.swing.JButton();
         voltarGeneros = new javax.swing.JButton();
         eliminaGenero = new javax.swing.JButton();
         jScrollPane13 = new javax.swing.JScrollPane();
         outGenero = new javax.swing.JTextArea();
         resultadosFrame = new javax.swing.JFrame();
         jResultadosFilmePanel = new javax.swing.JPanel();
         jSeparator4 = new javax.swing.JSeparator();
         paisResultadosFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel74 = new javax.swing.JLabel();
         jScrollPane18 = new javax.swing.JScrollPane();
         jTextArea14 = new javax.swing.JTextArea();
         javax.swing.JLabel jLabel75 = new javax.swing.JLabel();
         imdbResultadosFilme = new javax.swing.JTextField();
         anoResultadosFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel77 = new javax.swing.JLabel();
         jScrollPane19 = new javax.swing.JScrollPane();
         listaResultados = new javax.swing.JList();
         voltarResultados = new javax.swing.JButton();
         jLabel78 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel79 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel80 = new javax.swing.JLabel();
         tituloResultadosFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel81 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel82 = new javax.swing.JLabel();
         realizadorResultadosFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel83 = new javax.swing.JLabel();
         produtorResultadosFilme = new javax.swing.JTextField();
         javax.swing.JLabel jLabel84 = new javax.swing.JLabel();
         jScrollPane20 = new javax.swing.JScrollPane();
         jList5 = new javax.swing.JList();
         jLabel76 = new javax.swing.JLabel();
         listaFormatosResultadosFilmes = new javax.swing.JComboBox();
         jLabel98 = new javax.swing.JLabel();
         jLabel99 = new javax.swing.JLabel();
         alugar = new javax.swing.JButton();
         jTextField1 = new javax.swing.JTextField();
         idAlugaFilme = new javax.swing.JTextField();
         adicionarStockFilme = new javax.swing.JButton();
         eliminarStockFilme = new javax.swing.JButton();
         adicionarClienteFrame = new javax.swing.JFrame();
         jAdicionarClientePanel = new javax.swing.JPanel();
         jLabel24 = new javax.swing.JLabel();
         moradaAdicionaClientes = new javax.swing.JTextField();
         passwordAdicionaClientes2 = new javax.swing.JPasswordField();
         jLabel29 = new javax.swing.JLabel();
         nomeAdicionaClientes = new javax.swing.JTextField();
         passwordAdicionaClientes = new javax.swing.JPasswordField();
         emailAdicionaClientes = new javax.swing.JTextField();
         jLabel33 = new javax.swing.JLabel();
         jScrollPane7 = new javax.swing.JScrollPane();
         outputAdicionaClientes = new javax.swing.JTextArea();
         jVoltarACFButton = new javax.swing.JButton();
         adicionarCliente = new javax.swing.JButton();
         obterDadosAdicionarClientes = new javax.swing.JButton();
         jLabel25 = new javax.swing.JLabel();
         jLabel26 = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         jLabel28 = new javax.swing.JLabel();
         jLabel30 = new javax.swing.JLabel();
         biAdicionaClientes = new javax.swing.JTextField();
         telefoneAdicionaClientes = new javax.swing.JTextField();
         eliminarClienteFrame = new javax.swing.JFrame();
         jEliminarClientePanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel50 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel51 = new javax.swing.JLabel();
         jScrollPane11 = new javax.swing.JScrollPane();
         outEliminaClientes = new javax.swing.JTextArea();
         jButton14 = new javax.swing.JButton();
         eliminarClienteBI = new javax.swing.JButton();
         jScrollPane8 = new javax.swing.JScrollPane();
         listaEliminarClientes = new javax.swing.JList();
         eliminarClientesLista = new javax.swing.JButton();
         biEliminaClientes = new javax.swing.JTextField();
         notificarClientesFrame = new javax.swing.JFrame();
         jNotificarClientePanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel35 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel42 = new javax.swing.JLabel();
         biNotificarClientes = new javax.swing.JTextField();
         jScrollPane9 = new javax.swing.JScrollPane();
         mensagem = new javax.swing.JTextArea();
         javax.swing.JLabel jLabel52 = new javax.swing.JLabel();
         jToggleButton34 = new javax.swing.JToggleButton();
         enviarEmail = new javax.swing.JButton();
         pesquisarClienteFrame = new javax.swing.JFrame();
         jPesquisarClientePanel = new javax.swing.JPanel();
         javax.swing.JLabel jLabel43 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel44 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel45 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel46 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel47 = new javax.swing.JLabel();
         javax.swing.JLabel jLabel48 = new javax.swing.JLabel();
         telefonePesquisarClientes = new javax.swing.JTextField();
         biPesquisarClientes = new javax.swing.JTextField();
         nomePesquisarClientes = new javax.swing.JTextField();
         emailPesquisarClientes = new javax.swing.JTextField();
         moradaPesquisarClientes = new javax.swing.JTextField();
         idPesquisarClientes = new javax.swing.JTextField();
         javax.swing.JLabel jLabel49 = new javax.swing.JLabel();
         pesquisarClientesButton = new javax.swing.JToggleButton();
         jScrollPane22 = new javax.swing.JScrollPane();
         outPesquisarClientes = new javax.swing.JTextArea();
         voltarPesquisarCliente = new javax.swing.JToggleButton();
         pesquisarPorBI = new javax.swing.JButton();
         pesquisarPorID = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
         formatosFrame = new javax.swing.JFrame();
         jPanel1 = new javax.swing.JPanel();
         jScrollPane23 = new javax.swing.JScrollPane();
         listaFormato = new javax.swing.JList();
         jLabel34 = new javax.swing.JLabel();
         textFormato = new javax.swing.JTextField();
         jLabel36 = new javax.swing.JLabel();
         adicionarFormato = new javax.swing.JButton();
         voltarFormatos = new javax.swing.JButton();
         eliminarFormato = new javax.swing.JButton();
         jScrollPane24 = new javax.swing.JScrollPane();
         outFormato = new javax.swing.JTextArea();
         resultadosClientes = new javax.swing.JFrame();
         resultadosClientesPanel = new javax.swing.JPanel();
         emailResultadosClientes = new javax.swing.JTextField();
         jLabel105 = new javax.swing.JLabel();
         jLabel106 = new javax.swing.JLabel();
         moradaResultadosClientes = new javax.swing.JTextField();
         jLabel107 = new javax.swing.JLabel();
         telefoneResultadosClientes = new javax.swing.JTextField();
         jLabel108 = new javax.swing.JLabel();
         biResultadosClientes = new javax.swing.JTextField();
         jLabel109 = new javax.swing.JLabel();
         nomeResultadosClientes = new javax.swing.JTextField();
         voltarResultadosClientes = new javax.swing.JButton();
         javax.swing.JLabel jLabel96 = new javax.swing.JLabel();
         jScrollPane10 = new javax.swing.JScrollPane();
         listaResultadosClientes = new javax.swing.JList();
         mainPanel = new javax.swing.JPanel();
 
         jLoginButton.setText("Login");
         jLoginButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jLoginButtonActionPerformed(evt);
             }
         });
 
         jLabel1.setText("Username:");
 
         jLabel2.setText("Password:");
 
         jUsernameField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jUsernameFieldActionPerformed(evt);
             }
         });
 
         jLabel5.setFont(new java.awt.Font("Tahoma", 3, 24));
         jLabel5.setText("[NOME DO VIDEOCLUBE]");
 
         javax.swing.GroupLayout jLoginPanelLayout = new javax.swing.GroupLayout(jLoginPanel);
         jLoginPanel.setLayout(jLoginPanelLayout);
         jLoginPanelLayout.setHorizontalGroup(
             jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jLoginPanelLayout.createSequentialGroup()
                 .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jLoginPanelLayout.createSequentialGroup()
                         .addGap(138, 138, 138)
                         .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel2)
                             .addComponent(jLabel1))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(jUsernameField)
                             .addComponent(jPasswordField)
                             .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(jLoginPanelLayout.createSequentialGroup()
                         .addGap(338, 338, 338)
                         .addComponent(jLoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         jLoginPanelLayout.setVerticalGroup(
             jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jLoginPanelLayout.createSequentialGroup()
                 .addGap(24, 24, 24)
                 .addComponent(jLabel5)
                 .addGap(95, 95, 95)
                 .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(jUsernameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(44, 44, 44)
                 .addGroup(jLoginPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel2)
                     .addComponent(jPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(jLoginButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel6.setFont(new java.awt.Font("Tahoma", 3, 24));
         jLabel6.setText("Administrador");
 
         statsArea.setColumns(20);
         statsArea.setEditable(false);
         statsArea.setRows(5);
         jScrollPane3.setViewportView(statsArea);
 
         jLabel9.setText("Estatísticas:");
 
         empregadosCheckBox.setText("Empregados");
         empregadosCheckBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 empregadosCheckBoxActionPerformed(evt);
             }
         });
 
         contabilidadeBox.setText("Contabilidade");
         contabilidadeBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 contabilidadeBoxActionPerformed(evt);
             }
         });
 
         filmesCheckBox.setText("Filmes");
 
         maquinasCheckBox.setText("Máquinas");
 
         jLabel10.setText("Data Início:");
 
         dateBegin.setText("00/00/00");
 
         jLabel11.setText("Data Fim");
 
         dateEnd.setText("00/00/00");
 
         jButton5.setText("Reset");
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         dataInit.setText("Escolher Data Início");
         dataInit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dataInitActionPerformed(evt);
             }
         });
 
         dataEnd.setText("Escolher Data Fim");
         dataEnd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 dataEndActionPerformed(evt);
             }
         });
 
         clientesCheckBox.setText("Clientes");
 
         consultarEstatisticas.setText("Consultar");
         consultarEstatisticas.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 consultarEstatisticasActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jEstatisticasPanelLayout = new javax.swing.GroupLayout(jEstatisticasPanel);
         jEstatisticasPanel.setLayout(jEstatisticasPanelLayout);
         jEstatisticasPanelLayout.setHorizontalGroup(
             jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jEstatisticasPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(empregadosCheckBox)
                     .addComponent(contabilidadeBox)
                     .addComponent(clientesCheckBox)
                     .addComponent(filmesCheckBox)
                     .addComponent(maquinasCheckBox)
                     .addComponent(jLabel9)
                     .addGroup(jEstatisticasPanelLayout.createSequentialGroup()
                         .addComponent(jLabel10)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(dateBegin, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel11)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jEstatisticasPanelLayout.createSequentialGroup()
                         .addGroup(jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(dataInit)
                             .addComponent(jButton5))
                         .addGap(18, 18, 18)
                         .addGroup(jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(consultarEstatisticas)
                             .addComponent(dataEnd)))
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 418, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(680, Short.MAX_VALUE))
         );
         jEstatisticasPanelLayout.setVerticalGroup(
             jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jEstatisticasPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel9)
                 .addGap(12, 12, 12)
                 .addComponent(empregadosCheckBox)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(contabilidadeBox)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(clientesCheckBox)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(filmesCheckBox)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(maquinasCheckBox)
                 .addGap(18, 18, 18)
                 .addGroup(jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel10)
                     .addComponent(dateBegin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(dateEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel11))
                 .addGap(26, 26, 26)
                 .addGroup(jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(dataInit)
                     .addComponent(dataEnd))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jEstatisticasPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButton5)
                     .addComponent(consultarEstatisticas))
                 .addGap(34, 34, 34)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(105, Short.MAX_VALUE))
         );
 
         jTabbedPane2.addTab("Estatísticas", jEstatisticasPanel);
 
         pagamentosAtraso.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane6.setViewportView(pagamentosAtraso);
 
         jLabel23.setText("Clientes com pagamentos em atraso:");
 
         jPesquisarClientesButton.setText("Pesquisar Clientes");
         jPesquisarClientesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jPesquisarClientesButtonActionPerformed(evt);
             }
         });
 
         jAdicionarClientesButton.setText("Adicionar Clientes");
         jAdicionarClientesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jAdicionarClientesButtonActionPerformed(evt);
             }
         });
 
         jEliminarClientesButton.setText("Eliminar Clientes");
         jEliminarClientesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jEliminarClientesButtonActionPerformed(evt);
             }
         });
 
         jNotificarClientesButton.setText("Notificar Clientes");
         jNotificarClientesButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jNotificarClientesButtonActionPerformed(evt);
             }
         });
 
         jVerificarPagamentosAtrasoButton.setText("Verificar");
         jVerificarPagamentosAtrasoButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jVerificarPagamentosAtrasoButtonActionPerformed(evt);
             }
         });
 
         jLabel31.setText("Clientes com requesições:");
 
         verificarRequesicoes.setText("Verificar");
         verificarRequesicoes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 verificarRequesicoesActionPerformed(evt);
             }
         });
 
         jLabel40.setText("Lista:");
 
         listaRequisicoes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane25.setViewportView(listaRequisicoes);
 
         jLabel100.setText("Requisições do Cliente:");
 
         entregaFilme.setText("Entregar Filme");
         entregaFilme.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 entregaFilmeActionPerformed(evt);
             }
         });
 
         jLabel101.setText("Valor a pagar:");
 
         jTextField2.setEditable(false);
 
         javax.swing.GroupLayout jClientesManagerPanelLayout = new javax.swing.GroupLayout(jClientesManagerPanel);
         jClientesManagerPanel.setLayout(jClientesManagerPanelLayout);
         jClientesManagerPanelLayout.setHorizontalGroup(
             jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                         .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel23)
                             .addComponent(jLabel31))
                         .addGap(38, 38, 38)
                         .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(verificarRequesicoes)
                             .addComponent(jVerificarPagamentosAtrasoButton)))
                     .addComponent(jLabel40)
                     .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                         .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jScrollPane6, javax.swing.GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jClientesManagerPanelLayout.createSequentialGroup()
                                 .addComponent(jPesquisarClientesButton)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(jAdicionarClientesButton)))
                         .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                                         .addComponent(jNotificarClientesButton)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jEliminarClientesButton))
                                     .addComponent(jLabel100, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(jScrollPane25, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jClientesManagerPanelLayout.createSequentialGroup()
                                         .addComponent(jLabel101)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))))
                             .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                                 .addGap(87, 87, 87)
                                 .addComponent(entregaFilme)))))
                 .addGap(618, 618, 618))
         );
         jClientesManagerPanelLayout.setVerticalGroup(
             jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel31)
                     .addComponent(verificarRequesicoes))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel23)
                     .addComponent(jVerificarPagamentosAtrasoButton))
                 .addGap(16, 16, 16)
                 .addComponent(jLabel40)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jClientesManagerPanelLayout.createSequentialGroup()
                         .addComponent(jLabel100)
                         .addGap(1, 1, 1)
                         .addComponent(jScrollPane25, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel101)
                             .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addGap(68, 68, 68)
                 .addGroup(jClientesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jPesquisarClientesButton)
                     .addComponent(jAdicionarClientesButton)
                     .addComponent(jNotificarClientesButton)
                     .addComponent(jEliminarClientesButton))
                 .addContainerGap(262, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jClientesManagerPanelLayout.createSequentialGroup()
                 .addContainerGap(218, Short.MAX_VALUE)
                 .addComponent(entregaFilme)
                 .addGap(341, 341, 341))
         );
 
         jTabbedPane2.addTab("Gestão Clientes", jClientesManagerPanel);
 
         jLabel12.setText("Adicionar Empregado:");
 
         jLabel13.setText("Lista Empregados:");
 
         listaEmpregados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane4.setViewportView(listaEmpregados);
 
         jDespedirEmpregadoButton.setText("Eliminar da Lista");
         jDespedirEmpregadoButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jDespedirEmpregadoButtonActionPerformed(evt);
             }
         });
 
         jLabel14.setText("Nome:");
 
         jLabel15.setText("BI:");
 
         jLabel16.setText("Password:");
 
         jLabel17.setText("E-mail:");
 
         jLabel18.setText("Morada:");
 
         jLabel19.setText("Telefone:");
 
         jLabel20.setText("Privilégios:");
 
         adminRadio.setText("Administrador");
 
         opRadio.setSelected(true);
         opRadio.setText("Operador");
 
         jLabel21.setText("Salário:");
 
         jLabel22.setText("Confirm Password:");
 
         jAdicionarEmpregadoButton.setText("Adicionar/Actualizar");
         jAdicionarEmpregadoButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jAdicionarEmpregadoButtonActionPerformed(evt);
             }
         });
 
         outEmpregados.setColumns(20);
        outEmpregados.setEditable(false);
         outEmpregados.setRows(5);
         jScrollPane5.setViewportView(outEmpregados);
 
         actualzarListaEmpregados.setText("Actualizar Lista");
         actualzarListaEmpregados.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 actualzarListaEmpregadosActionPerformed(evt);
             }
         });
 
         obterDadosEmpregadoActualizacao.setText("Obter dados para actualzação");
         obterDadosEmpregadoActualizacao.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 obterDadosEmpregadoActualizacaoActionPerformed(evt);
             }
         });
 
         salarioEmpregados.setText("2000");
 
         jLabel38.setText("€");
 
         javax.swing.GroupLayout jEmpregadosManagerPanelLayout = new javax.swing.GroupLayout(jEmpregadosManagerPanel);
         jEmpregadosManagerPanel.setLayout(jEmpregadosManagerPanelLayout);
         jEmpregadosManagerPanelLayout.setHorizontalGroup(
             jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel12)
                     .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                         .addGap(3, 3, 3)
                         .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel15)
                             .addComponent(jLabel16)
                             .addComponent(jLabel14)
                             .addComponent(jLabel17)
                             .addComponent(jLabel19)
                             .addComponent(jLabel20)
                             .addComponent(jLabel21)
                             .addComponent(jLabel18)
                             .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(moradaEmpregados)
                                     .addComponent(nomeEmpregados)
                                     .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                                         .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                             .addComponent(passwordEmpregados, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                             .addComponent(biEmpregados, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                                             .addComponent(telefoneEmpregados, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                         .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                             .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                                                 .addComponent(jLabel22)
                                                 .addGap(27, 27, 27)
                                                 .addComponent(passwordEmpregados2, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                                             .addComponent(obterDadosEmpregadoActualizacao)))
                                     .addComponent(emailEmpregados))
                                 .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                                     .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                                             .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                 .addComponent(jAdicionarEmpregadoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                                                     .addComponent(adminRadio, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                     .addComponent(opRadio)))
                                             .addGap(19, 19, 19))
                                         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jEmpregadosManagerPanelLayout.createSequentialGroup()
                                             .addComponent(jDespedirEmpregadoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addGap(37, 37, 37)))
                                     .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(actualzarListaEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))))
                             .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                                 .addComponent(salarioEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jLabel38)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(jLabel13)
                                 .addGap(64, 64, 64)))))
                 .addContainerGap(589, Short.MAX_VALUE))
         );
         jEmpregadosManagerPanelLayout.setVerticalGroup(
             jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel12)
                 .addGap(18, 18, 18)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(nomeEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel14))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel15)
                     .addComponent(obterDadosEmpregadoActualizacao)
                     .addComponent(biEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel22)
                         .addComponent(passwordEmpregados2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(passwordEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel16)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel19)
                     .addComponent(telefoneEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(emailEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel17))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(moradaEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel18))
                 .addGap(7, 7, 7)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel21)
                     .addComponent(salarioEmpregados, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel38)
                     .addComponent(jLabel13))
                 .addGap(18, 18, 18)
                 .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jEmpregadosManagerPanelLayout.createSequentialGroup()
                         .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel20)
                             .addComponent(opRadio)
                             .addComponent(adminRadio))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jAdicionarEmpregadoButton)
                         .addGap(18, 18, 18)
                         .addGroup(jEmpregadosManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jDespedirEmpregadoButton))))
                 .addGap(18, 18, 18)
                 .addComponent(actualzarListaEmpregados)
                 .addContainerGap(110, Short.MAX_VALUE))
         );
 
         jTabbedPane2.addTab("Gestão Empregados", jEmpregadosManagerPanel);
 
         jAdicionarFilmesToggleButton.setText("Adicionar Filmes");
         jAdicionarFilmesToggleButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jAdicionarFilmesToggleButtonActionPerformed(evt);
             }
         });
 
         jActualizarStockButton.setText("Actualizar Stock");
         jActualizarStockButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jActualizarStockButtonActionPerformed(evt);
             }
         });
 
         jEliminarFilmeButton.setText("Eliminar Stocks");
         jEliminarFilmeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jEliminarFilmeButtonActionPerformed(evt);
             }
         });
 
         jPesquisarButton.setText("Pesquisar Filme");
         jPesquisarButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jPesquisarButtonActionPerformed(evt);
             }
         });
 
         jGeneroButton.setText("Adicionar novo Género");
         jGeneroButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jGeneroButtonActionPerformed(evt);
             }
         });
 
         jFormatosFrameButton.setText("Adicionar novo Formato");
         jFormatosFrameButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jFormatosFrameButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jFilmesManagerPanelLayout = new javax.swing.GroupLayout(jFilmesManagerPanel);
         jFilmesManagerPanel.setLayout(jFilmesManagerPanelLayout);
         jFilmesManagerPanelLayout.setHorizontalGroup(
             jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jFilmesManagerPanelLayout.createSequentialGroup()
                 .addGap(149, 149, 149)
                 .addGroup(jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jEliminarFilmeButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jFormatosFrameButton, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                     .addComponent(jAdicionarFilmesToggleButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(18, 18, 18)
                 .addGroup(jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jActualizarStockButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPesquisarButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jGeneroButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(643, Short.MAX_VALUE))
         );
         jFilmesManagerPanelLayout.setVerticalGroup(
             jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jFilmesManagerPanelLayout.createSequentialGroup()
                 .addGap(156, 156, 156)
                 .addGroup(jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jActualizarStockButton, javax.swing.GroupLayout.DEFAULT_SIZE, 53, Short.MAX_VALUE)
                     .addComponent(jAdicionarFilmesToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jGeneroButton, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                     .addComponent(jEliminarFilmeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jFilmesManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jPesquisarButton, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                     .addComponent(jFormatosFrameButton, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE))
                 .addGap(237, 237, 237))
         );
 
         jTabbedPane2.addTab("Gestão de Filmes", jFilmesManagerPanel);
 
         jLabel3.setText("Adicionar Máquina:");
 
         jLabel4.setText("Vender Máquina:");
 
         listaMaquinas.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane1.setViewportView(listaMaquinas);
 
         jVenderATMButton.setText("Vender");
         jVenderATMButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jVenderATMButtonActionPerformed(evt);
             }
         });
 
         jAdicionarATMButton.setText("Adicionar");
         jAdicionarATMButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jAdicionarATMButtonActionPerformed(evt);
             }
         });
 
         jLabel8.setText("Custo:");
 
         outMaquinas.setColumns(20);
         outMaquinas.setEditable(false);
         outMaquinas.setRows(5);
         jScrollPane2.setViewportView(outMaquinas);
 
         jLabel97.setText("€");
 
         actualizarListaMaquinas.setText("Actualizar");
         actualizarListaMaquinas.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 actualizarListaMaquinasActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jATMManagerPanelLayout = new javax.swing.GroupLayout(jATMManagerPanel);
         jATMManagerPanel.setLayout(jATMManagerPanelLayout);
         jATMManagerPanelLayout.setHorizontalGroup(
             jATMManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jATMManagerPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jATMManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jATMManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(jLabel4)
                         .addComponent(jLabel3)
                         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jATMManagerPanelLayout.createSequentialGroup()
                             .addComponent(jLabel8)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(jLabel97)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jAdicionarATMButton))
                         .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                         .addComponent(jScrollPane1))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jATMManagerPanelLayout.createSequentialGroup()
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(actualizarListaMaquinas)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jVenderATMButton)))
                 .addContainerGap(723, Short.MAX_VALUE))
         );
         jATMManagerPanelLayout.setVerticalGroup(
             jATMManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jATMManagerPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel3)
                 .addGap(18, 18, 18)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jATMManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jAdicionarATMButton)
                     .addComponent(jLabel8)
                     .addComponent(jSpinner6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel97))
                 .addGap(55, 55, 55)
                 .addComponent(jLabel4)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jATMManagerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jVenderATMButton)
                     .addComponent(actualizarListaMaquinas))
                 .addContainerGap(267, Short.MAX_VALUE))
         );
 
         jTabbedPane2.addTab("Gestão Máquinas", jATMManagerPanel);
 
         jSairButton.setText("Sair");
         jSairButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jSairButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jMenuAdministradorPanelLayout = new javax.swing.GroupLayout(jMenuAdministradorPanel);
         jMenuAdministradorPanel.setLayout(jMenuAdministradorPanelLayout);
         jMenuAdministradorPanelLayout.setHorizontalGroup(
             jMenuAdministradorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jMenuAdministradorPanelLayout.createSequentialGroup()
                 .addGroup(jMenuAdministradorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jMenuAdministradorPanelLayout.createSequentialGroup()
                         .addGap(18, 18, 18)
                         .addComponent(jTabbedPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1113, Short.MAX_VALUE))
                     .addGroup(jMenuAdministradorPanelLayout.createSequentialGroup()
                         .addGap(93, 93, 93)
                         .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(40, 40, 40)
                         .addComponent(jSairButton)))
                 .addContainerGap())
         );
         jMenuAdministradorPanelLayout.setVerticalGroup(
             jMenuAdministradorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jMenuAdministradorPanelLayout.createSequentialGroup()
                 .addGroup(jMenuAdministradorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel6)
                     .addGroup(jMenuAdministradorPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jSairButton)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel7.setFont(new java.awt.Font("Tahoma", 3, 24));
         jLabel7.setText("Operador");
 
         jScrollPane12.setViewportView(pagamentosAtraso1);
 
         jLabel41.setText("Clientes com pagamentos em atraso:");
 
         pesquisarClientes.setText("Pesquisar Clientes");
         pesquisarClientes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pesquisarClientesActionPerformed(evt);
             }
         });
 
         adicionarClientes.setText("Adicionar Clientes");
         adicionarClientes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 adicionarClientesActionPerformed(evt);
             }
         });
 
         eliminarClientes.setText("Eliminar Clientes");
         eliminarClientes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminarClientesActionPerformed(evt);
             }
         });
 
         notificarClientes.setText("Notificar Clientes");
         notificarClientes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 notificarClientesActionPerformed(evt);
             }
         });
 
         jVerificarPagamentosAtrasoButton1.setText("Verificar");
         jVerificarPagamentosAtrasoButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jVerificarPagamentosAtrasoButton1ActionPerformed(evt);
             }
         });
 
         verificarRequesicoes1.setText("Verificar");
         verificarRequesicoes1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 verificarRequesicoes1ActionPerformed(evt);
             }
         });
 
         jLabel32.setText("Clientes com requesições:");
 
         entregaFilme1.setText("Entregar Filme");
         entregaFilme1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 entregaFilme1ActionPerformed(evt);
             }
         });
 
         jTextField3.setEditable(false);
 
         jLabel102.setText("Valor a pagar:");
 
         listaRequisicoes1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         jScrollPane26.setViewportView(listaRequisicoes1);
 
         jLabel103.setText("Requisições do Cliente:");
 
         jLabel104.setText("Lista:");
 
         javax.swing.GroupLayout jClientesManagerPanel1Layout = new javax.swing.GroupLayout(jClientesManagerPanel1);
         jClientesManagerPanel1.setLayout(jClientesManagerPanel1Layout);
         jClientesManagerPanel1Layout.setHorizontalGroup(
             jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                         .addContainerGap(123, Short.MAX_VALUE)
                         .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel41)
                             .addComponent(jLabel32))
                         .addGap(45, 45, 45))
                     .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(jLabel104)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(verificarRequesicoes1)
                     .addComponent(jVerificarPagamentosAtrasoButton1))
                 .addGap(230, 230, 230))
             .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(18, 18, 18)
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jLabel103, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane26, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jClientesManagerPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel102)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(150, 150, 150))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jClientesManagerPanel1Layout.createSequentialGroup()
                 .addGap(57, 57, 57)
                 .addComponent(pesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(adicionarClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                         .addComponent(entregaFilme1)
                         .addContainerGap())
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jClientesManagerPanel1Layout.createSequentialGroup()
                         .addComponent(notificarClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(eliminarClientes)
                         .addGap(68, 68, 68))))
         );
         jClientesManagerPanel1Layout.setVerticalGroup(
             jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(verificarRequesicoes1)
                             .addComponent(jLabel32))
                         .addGap(5, 5, 5)
                         .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel41)
                             .addComponent(jVerificarPagamentosAtrasoButton1))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jClientesManagerPanel1Layout.createSequentialGroup()
                         .addContainerGap(59, Short.MAX_VALUE)
                         .addComponent(jLabel104)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jClientesManagerPanel1Layout.createSequentialGroup()
                         .addComponent(jLabel103)
                         .addGap(1, 1, 1)
                         .addComponent(jScrollPane26, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(jLabel102)
                             .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(12, 12, 12)))
                 .addGap(18, 18, 18)
                 .addComponent(entregaFilme1)
                 .addGap(48, 48, 48)
                 .addGroup(jClientesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(pesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(notificarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(eliminarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(adicionarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(245, 245, 245))
         );
 
         jTabbedPane3.addTab("Gestão Clientes", jClientesManagerPanel1);
 
         jToggleButton17.setText("Adicionar Filmes");
         jToggleButton17.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jToggleButton17ActionPerformed(evt);
             }
         });
 
         jToggleButton18.setText("Actualizar Stock");
         jToggleButton18.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jToggleButton18ActionPerformed(evt);
             }
         });
 
         eliminaFilmes2.setText("Eliminar Stocks");
         eliminaFilmes2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eliminaFilmes2ActionPerformed(evt);
             }
         });
 
         pesquisarFilmes2.setText("Pesquisar Filme");
         pesquisarFilmes2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pesquisarFilmes2ActionPerformed(evt);
             }
         });
 
         adicionaGenero2.setText("Adicionar novo Género");
         adicionaGenero2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 adicionaGenero2ActionPerformed(evt);
             }
         });
 
         jFormatosFrameButton1.setText("Adicionar novo Formato");
         jFormatosFrameButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jFormatosFrameButton1ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jFilmesManagerPanel1Layout = new javax.swing.GroupLayout(jFilmesManagerPanel1);
         jFilmesManagerPanel1.setLayout(jFilmesManagerPanel1Layout);
         jFilmesManagerPanel1Layout.setHorizontalGroup(
             jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFilmesManagerPanel1Layout.createSequentialGroup()
                 .addContainerGap(164, Short.MAX_VALUE)
                 .addGroup(jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(jToggleButton17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(eliminaFilmes2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jFilmesManagerPanel1Layout.createSequentialGroup()
                         .addComponent(jFormatosFrameButton1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                 .addGroup(jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jFilmesManagerPanel1Layout.createSequentialGroup()
                         .addGap(28, 28, 28)
                         .addGroup(jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                             .addComponent(jToggleButton18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(adicionaGenero2, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFilmesManagerPanel1Layout.createSequentialGroup()
                         .addGap(31, 31, 31)
                         .addComponent(pesquisarFilmes2, javax.swing.GroupLayout.PREFERRED_SIZE, 147, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGap(157, 157, 157))
         );
         jFilmesManagerPanel1Layout.setVerticalGroup(
             jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jFilmesManagerPanel1Layout.createSequentialGroup()
                 .addGap(143, 143, 143)
                 .addGroup(jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(jToggleButton17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jToggleButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(eliminaFilmes2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(adicionaGenero2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(jFilmesManagerPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jFormatosFrameButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE)
                     .addComponent(pesquisarFilmes2, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                 .addGap(200, 200, 200))
         );
 
         jTabbedPane3.addTab("Gestão de Filmes", jFilmesManagerPanel1);
 
         jSairButton2.setText("Sair");
         jSairButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jSairButton2ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jMenuOperatorPanelLayout = new javax.swing.GroupLayout(jMenuOperatorPanel);
         jMenuOperatorPanel.setLayout(jMenuOperatorPanelLayout);
         jMenuOperatorPanelLayout.setHorizontalGroup(
             jMenuOperatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jMenuOperatorPanelLayout.createSequentialGroup()
                 .addGroup(jMenuOperatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jMenuOperatorPanelLayout.createSequentialGroup()
                         .addGap(18, 18, 18)
                         .addComponent(jTabbedPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 651, Short.MAX_VALUE))
                     .addGroup(jMenuOperatorPanelLayout.createSequentialGroup()
                         .addGap(93, 93, 93)
                         .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 354, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jSairButton2)))
                 .addContainerGap())
         );
         jMenuOperatorPanelLayout.setVerticalGroup(
             jMenuOperatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jMenuOperatorPanelLayout.createSequentialGroup()
                 .addGroup(jMenuOperatorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(jSairButton2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jFileChooser1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jFileChooser1ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout ficheirosFrameLayout = new javax.swing.GroupLayout(ficheirosFrame.getContentPane());
         ficheirosFrame.getContentPane().setLayout(ficheirosFrameLayout);
         ficheirosFrameLayout.setHorizontalGroup(
             ficheirosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 592, Short.MAX_VALUE)
         );
         ficheirosFrameLayout.setVerticalGroup(
             ficheirosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jFileChooser1, javax.swing.GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
         );
 
         adicionarFilmeFrame.setAlwaysOnTop(true);
         adicionarFilmeFrame.setMinimumSize(new java.awt.Dimension(800, 600));
         adicionarFilmeFrame.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 adicionarFilmeFrameWindowClosing(evt);
             }
         });
 
         jLabel53.setText("Género:");
 
         listaGenerosAdicionaFilmes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
         jLabel55.setText("Ano:");
 
         jLabel56.setText("Realizador:");
 
         jLabel57.setText("Produtor:");
 
         jLabel58.setText("Título:");
 
         jLabel60.setText("IMDB RATING: ");
 
         jLabel61.setFont(new java.awt.Font("Tahoma", 0, 16));
         jLabel61.setText("Adicionar Filme:");
 
         jLabel54.setText("País:");
 
         textDescricaoAdicionaFilme.setColumns(20);
         textDescricaoAdicionaFilme.setRows(5);
         jScrollPane14.setViewportView(textDescricaoAdicionaFilme);
 
         jLabel59.setText("Descrição:");
 
         jLabel62.setText("Capa:");
 
         jEscolherFicheiroButton.setText("Escolher Ficheiro");
         jEscolherFicheiroButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jEscolherFicheiroButtonActionPerformed(evt);
             }
         });
 
         adicionarFilme.setText("Adicionar Filme");
         adicionarFilme.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 adicionarFilmeActionPerformed(evt);
             }
         });
 
         jLabel63.setText("Adicionar Stock");
 
         outAdicionaFilme.setColumns(20);
         outAdicionaFilme.setRows(5);
         jScrollPane15.setViewportView(outAdicionaFilme);
 
         jLabel64.setText("ID:");
 
         listaFormatosAdicionaFilme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Blu-Ray", "DVD", "UMD"}));
 
         jLabel65.setText("Formato:");
 
         jLabel66.setText("Custo:");
 
         jLabel67.setText("Custo Aluguer:");
 
         adicionarStock.setText("Adicionar Stock");
         adicionarStock.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 adicionarStockActionPerformed(evt);
             }
         });
 
         outAdicionaStock.setColumns(20);
         outAdicionaStock.setRows(5);
         jScrollPane16.setViewportView(outAdicionaStock);
 
         voltarAdcionaFilmes.setText("Voltar");
         voltarAdcionaFilmes.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 voltarAdcionaFilmesActionPerformed(evt);
             }
         });
 
         countriesList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Afghanistan",
             "Albania",
             "Algeria",
             "Andorra",
             "Angola",
             "Antigua",
             "Argentina",
             "Armenia",
             "Australia",
             "Austria",
             "Azerbaijan",
             "Bahamas",
             "Bahrain",
             "Bangladesh",
             "Barbados",
             "Belarus",
             "Belgium",
             "Belize",
             "Benin",
             "Bhutan",
             "Bolivia",
             "Bosnia Herzegovina",
             "Botswana",
             "Brazil",
             "Brunei",
             "Bulgaria",
             "Burkina",
             "Burundi",
             "Cambodia",
             "Cameroon",
             "Canada",
             "Cape Verde",
             "Central African Rep",
             "Chad",
             "Chile",
             "China",
             "Colombia",
             "Comoros",
             "Congo",
             "Costa Rica",
             "Croatia",
             "Cuba",
             "Cyprus",
             "Czech Republic",
             "Denmark",
             "Djibouti",
             "Dominica",
             "Dominican Republic",
             "East Timor",
             "Ecuador",
             "Egypt",
             "El Salvador",
             "Equatorial Guinea",
             "Eritrea",
             "Estonia",
             "Ethiopia",
             "Fiji",
             "Finland",
             "France",
             "Gabon",
             "Gambia",
             "Georgia",
             "Germany",
             "Ghana",
             "Greece",
             "Grenada",
             "Guatemala",
             "Guinea",
             "Guinea-Bissau",
             "Guyana",
             "Haiti",
             "Honduras",
             "Hungary",
             "Iceland",
             "India",
             "Indonesia",
             "Iran",
             "Iraq",
             "Ireland",
             "Israel",
             "Italy",
             "Ivory Coast",
             "Jamaica",
             "Japan",
             "Jordan",
             "Kazakhstan",
             "Kenya",
             "Kiribati",
             "Korea North",
             "Korea South",
             "Kosovo",
             "Kuwait",
             "Kyrgyzstan",
             "Laos",
             "Latvia",
             "Lebanon",
             "Lesotho",
             "Liberia",
             "Libya",
             "Liechtenstein",
             "Lithuania",
             "Luxembourg",
             "Macedonia",
             "Madagascar",
             "Malawi",
             "Malaysia",
             "Maldives",
             "Mali",
             "Malta",
             "Marshall Islands",
             "Mauritania",
             "Mauritius",
             "Mexico",
             "Micronesia",
             "Moldova",
             "Monaco",
             "Mongolia",
             "Montenegro",
             "Morocco",
             "Mozambique",
             "Myanmar",
             "Namibia",
             "Nauru",
             "Nepal",
             "Netherlands",
             "New Zealand",
             "Nicaragua",
             "Niger",
             "Nigeria",
             "Norway",
             "Oman",
             "Pakistan",
             "Palau",
             "Panama",
             "Papua New Guinea",
             "Paraguay",
             "Peru",
             "Philippines",
             "Poland",
             "Portugal",
             "Qatar",
             "Romania",
             "Russian Federation",
             "Rwanda",
             "St Kitts & Nevis",
             "St Lucia",
             "Saint Vincent & the Grenadines",
             "Samoa",
             "San Marino",
             "Sao Tome & Principe",
             "Saudi Arabia",
             "Senegal",
             "Serbia",
             "Seychelles",
             "Sierra Leone",
             "Singapore",
             "Slovakia",
             "Slovenia",
             "Solomon Islands",
             "Somalia",
             "South Africa",
             "Spain",
             "Sri Lanka",
             "Sudan",
             "Suriname",
             "Swaziland",
             "Sweden",
             "Switzerland",
             "Syria",
             "Taiwan",
             "Tajikistan",
             "Tanzania",
             "Thailand",
             "Togo",
             "Tonga",
             "Trinidad & Tobago",
             "Tunisia",
             "Turkey",
             "Turkmenistan",
             "Tuvalu",
             "Uganda",
             "Ukraine",
             "United Arab Emirates",
             "United Kingdom",
             "United States",
             "Uruguay",
             "Uzbekistan",
             "Vanuatu",
             "Vatican City",
             "Venezuela",
             "Vietnam",
             "Yemen",
             "Zambia",
             "Zimbabwe" }));
 
 jLabel37.setText("Nova Qtd.:");
 
 generoExtra.setText("Adicionar +");
 generoExtra.addActionListener(new java.awt.event.ActionListener() {
     public void actionPerformed(java.awt.event.ActionEvent evt) {
         generoExtraActionPerformed(evt);
     }
     });
 
     javax.swing.GroupLayout jAdicionarFilmePanelLayout = new javax.swing.GroupLayout(jAdicionarFilmePanel);
     jAdicionarFilmePanel.setLayout(jAdicionarFilmePanelLayout);
     jAdicionarFilmePanelLayout.setHorizontalGroup(
         jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                     .addGap(28, 28, 28)
                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                     .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGap(51, 51, 51)
                                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(adicionarFilme)
                                         .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                 .addComponent(jLabel61)
                                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                     .addGap(8, 8, 8)
                                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(jLabel65)
                                                 .addComponent(jLabel64))
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(idAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addComponent(listaFormatosAdicionaFilme, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE))
                                             .addGap(18, 18, 18)
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(jLabel67)
                                                 .addComponent(jLabel66))
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                 .addComponent(custoAdicionaStock)
                                                 .addComponent(custoAluguerAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addComponent(jLabel37)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                             .addComponent(qtdAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                             .addGap(18, 18, 18)
                                             .addComponent(jScrollPane16)))
                                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addGap(18, 18, 18)
                                             .addComponent(adicionarStock))
                                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addGap(34, 34, 34)
                                             .addComponent(voltarAdcionaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                 .addComponent(jLabel63)
                                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addComponent(jLabel54)
                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                             .addComponent(countriesList, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(jLabel57)
                                                 .addComponent(jLabel56)
                                                 .addComponent(jLabel58))
                                             .addGap(51, 51, 51)
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(textTituloAdicionaFilme, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                                 .addComponent(textRealizadorAdicionaFilme, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                                 .addComponent(textProdutorAdicionaFilme, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE))))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(jLabel62)
                                         .addComponent(jLabel53)
                                         .addComponent(jLabel60)
                                         .addComponent(jLabel55))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(jEscolherFicheiroButton)
                                         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAdicionarFilmePanelLayout.createSequentialGroup()
                                             .addGap(40, 40, 40)
                                             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                 .addComponent(anoAdicionaFilmeSpinner)
                                                 .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                         .addComponent(listaGenerosAdicionaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                     .addComponent(generoExtra)))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                             .addComponent(jLabel59)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))))
                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                     .addGap(75, 75, 75)
                     .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 391, javax.swing.GroupLayout.PREFERRED_SIZE)))
             .addGap(115, 115, 115))
     );
     jAdicionarFilmePanelLayout.setVerticalGroup(
         jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
             .addGap(27, 27, 27)
             .addComponent(jLabel61)
             .addGap(18, 18, 18)
             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(textTituloAdicionaFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel55)
                         .addComponent(jLabel58))
                     .addGap(18, 18, 18)
                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(textProdutorAdicionaFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel60)
                         .addComponent(jLabel57))
                     .addGap(18, 18, 18)
                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(textRealizadorAdicionaFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel53)
                         .addComponent(jLabel56))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(countriesList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel62)
                         .addComponent(jLabel54)))
                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                     .addComponent(anoAdicionaFilmeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(18, 18, 18)
                     .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(18, 18, 18)
                     .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(listaGenerosAdicionaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(generoExtra))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(jEscolherFicheiroButton)))
             .addGap(18, 18, 18)
             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                     .addComponent(jLabel59)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jAdicionarFilmePanelLayout.createSequentialGroup()
                     .addComponent(adicionarFilme)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
             .addGap(15, 15, 15)
             .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jLabel63)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel64)
                 .addComponent(idAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel66)
                 .addComponent(custoAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGap(18, 18, 18)
             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel65)
                 .addComponent(jLabel67)
                 .addComponent(custoAluguerAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(listaFormatosAdicionaFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(adicionarStock))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel37)
                     .addComponent(qtdAdicionaStock, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jAdicionarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(voltarAdcionaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)))
             .addGap(158, 158, 158))
     );
 
     qtdAdicionaStock.setModel(new SpinnerNumberModel(1,1,100,1));
 
     javax.swing.GroupLayout adicionarFilmeFrameLayout = new javax.swing.GroupLayout(adicionarFilmeFrame.getContentPane());
     adicionarFilmeFrame.getContentPane().setLayout(adicionarFilmeFrameLayout);
     adicionarFilmeFrameLayout.setHorizontalGroup(
         adicionarFilmeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 659, Short.MAX_VALUE)
         .addGroup(adicionarFilmeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(adicionarFilmeFrameLayout.createSequentialGroup()
                 .addGap(0, 11, Short.MAX_VALUE)
                 .addComponent(jAdicionarFilmePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 637, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 11, Short.MAX_VALUE)))
     );
     adicionarFilmeFrameLayout.setVerticalGroup(
         adicionarFilmeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 701, Short.MAX_VALUE)
         .addGroup(adicionarFilmeFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(adicionarFilmeFrameLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(jAdicionarFilmePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 701, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE)))
     );
 
     eliminarFilmesFrame.setAlwaysOnTop(true);
     eliminarFilmesFrame.setMinimumSize(new java.awt.Dimension(800, 600));
 
     eliminaFilmes.setText("Eliminar Stock");
     eliminaFilmes.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             eliminaFilmesActionPerformed(evt);
         }
     });
 
     jLabel68.setText("ID:");
 
     jLabel72.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel72.setText("Eliminar Stock");
 
     voltarEliminaFilmes.setText("Voltar");
     voltarEliminaFilmes.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarEliminaFilmesActionPerformed(evt);
         }
     });
 
     listarFormatoEliminar.setText("Listar");
     listarFormatoEliminar.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             listarFormatoEliminarActionPerformed(evt);
         }
     });
 
     jScrollPane17.setViewportView(textEliminaFilmes);
 
     jLabel70.setText("Lista de Stocks:");
 
     javax.swing.GroupLayout jEliminarFilmePanelLayout = new javax.swing.GroupLayout(jEliminarFilmePanel);
     jEliminarFilmePanel.setLayout(jEliminarFilmePanelLayout);
     jEliminarFilmePanelLayout.setHorizontalGroup(
         jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jEliminarFilmePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addGroup(jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(voltarEliminaFilmes)
                     .addGroup(jEliminarFilmePanelLayout.createSequentialGroup()
                         .addGroup(jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jLabel72, javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jEliminarFilmePanelLayout.createSequentialGroup()
                                 .addComponent(jLabel68)
                                 .addGap(29, 29, 29)
                                 .addGroup(jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(jEliminarFilmePanelLayout.createSequentialGroup()
                                         .addGap(140, 140, 140)
                                         .addComponent(eliminaFilmes))
                                     .addGroup(jEliminarFilmePanelLayout.createSequentialGroup()
                                         .addGap(27, 27, 27)
                                         .addComponent(idEliminaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(52, 52, 52)
                                         .addComponent(listarFormatoEliminar)))))
                         .addGap(85, 85, 85)))
                 .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 264, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel70))
             .addContainerGap(167, Short.MAX_VALUE))
     );
     jEliminarFilmePanelLayout.setVerticalGroup(
         jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jEliminarFilmePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jLabel72)
             .addGap(11, 11, 11)
             .addGroup(jEliminarFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel68)
                 .addComponent(idEliminaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(listarFormatoEliminar))
             .addGap(12, 12, 12)
             .addComponent(jLabel70)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jScrollPane17, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGap(18, 18, 18)
             .addComponent(eliminaFilmes)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(voltarEliminaFilmes)
             .addContainerGap(158, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout eliminarFilmesFrameLayout = new javax.swing.GroupLayout(eliminarFilmesFrame.getContentPane());
     eliminarFilmesFrame.getContentPane().setLayout(eliminarFilmesFrameLayout);
     eliminarFilmesFrameLayout.setHorizontalGroup(
         eliminarFilmesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(eliminarFilmesFrameLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jEliminarFilmePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
     eliminarFilmesFrameLayout.setVerticalGroup(
         eliminarFilmesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(eliminarFilmesFrameLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jEliminarFilmePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
 
     pesquisarFilmesFrame.setAlwaysOnTop(true);
     pesquisarFilmesFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     pesquisarFilmesFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             pesquisarFilmesFrameWindowClosing(evt);
         }
     });
 
     jLabel85.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel85.setText("Pesquisar:");
 
     jLabel86.setText("Título:");
 
     jLabel87.setText("Produtora:");
 
     jLabel88.setText("País:");
 
     jLabel89.setText("ID:");
 
     jLabel90.setText("IMBD RATING: Between");
 
     jLabel91.setText("And");
 
     jLabel92.setText("Realizador:");
 
     pesquisarFilme.setText("Procurar");
     pesquisarFilme.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             pesquisarFilmeActionPerformed(evt);
         }
     });
 
     voltarPesquisarFilmes.setText("Voltar");
     voltarPesquisarFilmes.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarPesquisarFilmesActionPerformed(evt);
         }
     });
 
     countriesList1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "","Afghanistan",
         "Albania",
         "Algeria",
         "Andorra",
         "Angola",
         "Antigua",
         "Argentina",
         "Armenia",
         "Australia",
         "Austria",
         "Azerbaijan",
         "Bahamas",
         "Bahrain",
         "Bangladesh",
         "Barbados",
         "Belarus",
         "Belgium",
         "Belize",
         "Benin",
         "Bhutan",
         "Bolivia",
         "Bosnia Herzegovina",
         "Botswana",
         "Brazil",
         "Brunei",
         "Bulgaria",
         "Burkina",
         "Burundi",
         "Cambodia",
         "Cameroon",
         "Canada",
         "Cape Verde",
         "Central African Rep",
         "Chad",
         "Chile",
         "China",
         "Colombia",
         "Comoros",
         "Congo",
         "Costa Rica",
         "Croatia",
         "Cuba",
         "Cyprus",
         "Czech Republic",
         "Denmark",
         "Djibouti",
         "Dominica",
         "Dominican Republic",
         "East Timor",
         "Ecuador",
         "Egypt",
         "El Salvador",
         "Equatorial Guinea",
         "Eritrea",
         "Estonia",
         "Ethiopia",
         "Fiji",
         "Finland",
         "France",
         "Gabon",
         "Gambia",
         "Georgia",
         "Germany",
         "Ghana",
         "Greece",
         "Grenada",
         "Guatemala",
         "Guinea",
         "Guinea-Bissau",
         "Guyana",
         "Haiti",
         "Honduras",
         "Hungary",
         "Iceland",
         "India",
         "Indonesia",
         "Iran",
         "Iraq",
         "Ireland",
         "Israel",
         "Italy",
         "Ivory Coast",
         "Jamaica",
         "Japan",
         "Jordan",
         "Kazakhstan",
         "Kenya",
         "Kiribati",
         "Korea North",
         "Korea South",
         "Kosovo",
         "Kuwait",
         "Kyrgyzstan",
         "Laos",
         "Latvia",
         "Lebanon",
         "Lesotho",
         "Liberia",
         "Libya",
         "Liechtenstein",
         "Lithuania",
         "Luxembourg",
         "Macedonia",
         "Madagascar",
         "Malawi",
         "Malaysia",
         "Maldives",
         "Mali",
         "Malta",
         "Marshall Islands",
         "Mauritania",
         "Mauritius",
         "Mexico",
         "Micronesia",
         "Moldova",
         "Monaco",
         "Mongolia",
         "Montenegro",
         "Morocco",
         "Mozambique",
         "Myanmar",
         "Namibia",
         "Nauru",
         "Nepal",
         "Netherlands",
         "New Zealand",
         "Nicaragua",
         "Niger",
         "Nigeria",
         "Norway",
         "Oman",
         "Pakistan",
         "Palau",
         "Panama",
         "Papua New Guinea",
         "Paraguay",
         "Peru",
         "Philippines",
         "Poland",
         "Portugal",
         "Qatar",
         "Romania",
         "Russian Federation",
         "Rwanda",
         "St Kitts & Nevis",
         "St Lucia",
         "Saint Vincent & the Grenadines",
         "Samoa",
         "San Marino",
         "Sao Tome & Principe",
         "Saudi Arabia",
         "Senegal",
         "Serbia",
         "Seychelles",
         "Sierra Leone",
         "Singapore",
         "Slovakia",
         "Slovenia",
         "Solomon Islands",
         "Somalia",
         "South Africa",
         "Spain",
         "Sri Lanka",
         "Sudan",
         "Suriname",
         "Swaziland",
         "Sweden",
         "Switzerland",
         "Syria",
         "Taiwan",
         "Tajikistan",
         "Tanzania",
         "Thailand",
         "Togo",
         "Tonga",
         "Trinidad & Tobago",
         "Tunisia",
         "Turkey",
         "Turkmenistan",
         "Tuvalu",
         "Uganda",
         "Ukraine",
         "United Arab Emirates",
         "United Kingdom",
         "United States",
         "Uruguay",
         "Uzbekistan",
         "Vanuatu",
         "Vatican City",
         "Venezuela",
         "Vietnam",
         "Yemen",
         "Zambia",
         "Zimbabwe" }));
 
 jLabel94.setText("Ano: Between");
 
 jLabel95.setText("And");
 
 pesquisarTodos.setText("ver Todos os Filmes");
 pesquisarTodos.addActionListener(new java.awt.event.ActionListener() {
 public void actionPerformed(java.awt.event.ActionEvent evt) {
     pesquisarTodosActionPerformed(evt);
     }
     });
 
     procurarID.setText("Procurar por ID");
     procurarID.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             procurarIDActionPerformed(evt);
         }
     });
 
     jLabel39.setText("Genero:");
 
     javax.swing.GroupLayout jPesqisaFilmesPanelLayout = new javax.swing.GroupLayout(jPesqisaFilmesPanel);
     jPesqisaFilmesPanel.setLayout(jPesqisaFilmesPanelLayout);
     jPesqisaFilmesPanelLayout.setHorizontalGroup(
         jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
             .addContainerGap()
             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel87)
                                 .addComponent(jLabel88)
                                 .addComponent(jLabel39))
                             .addGap(18, 18, 18)
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(countriesList1, 0, 118, Short.MAX_VALUE)
                                 .addComponent(textProdutorPesquisaFilmes, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                 .addComponent(generosBox, 0, 118, Short.MAX_VALUE)))
                         .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel86)
                                 .addComponent(jLabel89))
                             .addGap(40, 40, 40)
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(textIdPesquisaFilmes, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                                 .addComponent(textTituloPesquisaFilmes, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))))
                     .addGap(18, 18, 18)
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                             .addComponent(jLabel92)
                             .addGap(18, 18, 18)
                             .addComponent(textRealizadorPesquisaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel94)
                                 .addComponent(jLabel90))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addComponent(anoBSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(imdbBSpinner, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPesqisaFilmesPanelLayout.createSequentialGroup()
                                     .addComponent(jLabel91)
                                     .addGap(18, 18, 18)
                                     .addComponent(imdbESpinner))
                                 .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPesqisaFilmesPanelLayout.createSequentialGroup()
                                     .addComponent(jLabel95)
                                     .addGap(18, 18, 18)
                                     .addComponent(anoESpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))))
                         .addComponent(procurarID))
                     .addGap(96, 96, 96))
                 .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addComponent(jLabel85)
                     .addContainerGap(503, Short.MAX_VALUE))
                 .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addComponent(voltarPesquisarFilmes)
                     .addGap(112, 112, 112))
                 .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addComponent(pesquisarTodos)
                     .addContainerGap(449, Short.MAX_VALUE))
                 .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addComponent(pesquisarFilme)
                     .addContainerGap(503, Short.MAX_VALUE))))
     );
     jPesqisaFilmesPanelLayout.setVerticalGroup(
         jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
             .addGap(34, 34, 34)
             .addComponent(jLabel85)
             .addGap(28, 28, 28)
             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel89)
                 .addComponent(textIdPesquisaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(procurarID))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel90)
                         .addComponent(imdbBSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel91)
                         .addComponent(imdbESpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGap(18, 18, 18)
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel94)
                         .addComponent(jLabel95)
                         .addComponent(anoBSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(anoESpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGroup(jPesqisaFilmesPanelLayout.createSequentialGroup()
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel86)
                         .addComponent(textTituloPesquisaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGap(18, 18, 18)
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel87)
                         .addComponent(textProdutorPesquisaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGap(18, 18, 18)
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel88)
                         .addComponent(countriesList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel92)
                         .addComponent(textRealizadorPesquisaFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGap(18, 18, 18)
                     .addGroup(jPesqisaFilmesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(voltarPesquisarFilmes)
                         .addComponent(generosBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel39))))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
             .addComponent(pesquisarFilme)
             .addGap(18, 18, 18)
             .addComponent(pesquisarTodos)
             .addGap(45, 45, 45))
     );
 
     javax.swing.GroupLayout pesquisarFilmesFrameLayout = new javax.swing.GroupLayout(pesquisarFilmesFrame.getContentPane());
     pesquisarFilmesFrame.getContentPane().setLayout(pesquisarFilmesFrameLayout);
     pesquisarFilmesFrameLayout.setHorizontalGroup(
         pesquisarFilmesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(pesquisarFilmesFrameLayout.createSequentialGroup()
             .addGap(0, 29, Short.MAX_VALUE)
             .addComponent(jPesqisaFilmesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGap(0, 30, Short.MAX_VALUE))
     );
     pesquisarFilmesFrameLayout.setVerticalGroup(
         pesquisarFilmesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(pesquisarFilmesFrameLayout.createSequentialGroup()
             .addGap(0, 0, Short.MAX_VALUE)
             .addComponent(jPesqisaFilmesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGap(0, 0, Short.MAX_VALUE))
     );
 
     generosFrame.setAlwaysOnTop(true);
     generosFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     generosFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             generosFrameWindowClosing(evt);
         }
     });
 
     listaGeneros.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
     jScrollPane21.setViewportView(listaGeneros);
 
     jLabel71.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel71.setText("Géneros Existentes:");
 
     jLabel73.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel73.setText("Adicionar Género:");
 
     jLabel93.setText("Nome:");
 
     adicionaGenero.setText("Adicionar");
     adicionaGenero.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             adicionaGeneroActionPerformed(evt);
         }
     });
 
     voltarGeneros.setText("Voltar");
     voltarGeneros.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarGenerosActionPerformed(evt);
         }
     });
 
     eliminaGenero.setText("Eliminar");
     eliminaGenero.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             eliminaGeneroActionPerformed(evt);
         }
     });
 
     outGenero.setColumns(20);
     outGenero.setEditable(false);
     outGenero.setRows(5);
     jScrollPane13.setViewportView(outGenero);
 
     javax.swing.GroupLayout jGenerosPanelLayout = new javax.swing.GroupLayout(jGenerosPanel);
     jGenerosPanel.setLayout(jGenerosPanelLayout);
     jGenerosPanelLayout.setHorizontalGroup(
         jGenerosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jGenerosPanelLayout.createSequentialGroup()
             .addGap(118, 118, 118)
             .addGroup(jGenerosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addComponent(eliminaGenero)
                 .addGroup(jGenerosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel73)
                     .addComponent(jLabel71)
                     .addComponent(jScrollPane21, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 266, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(jGenerosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jGenerosPanelLayout.createSequentialGroup()
                             .addComponent(jLabel93)
                             .addGap(35, 35, 35)
                             .addComponent(textGenero, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(adicionaGenero))
                         .addComponent(jScrollPane13, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 257, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(voltarGeneros))))
             .addContainerGap(99, Short.MAX_VALUE))
     );
     jGenerosPanelLayout.setVerticalGroup(
         jGenerosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jGenerosPanelLayout.createSequentialGroup()
             .addGap(19, 19, 19)
             .addComponent(jLabel71)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jScrollPane21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(eliminaGenero)
             .addGap(9, 9, 9)
             .addComponent(jLabel73)
             .addGap(18, 18, 18)
             .addGroup(jGenerosPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel93)
                 .addComponent(adicionaGenero)
                 .addComponent(textGenero, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(voltarGeneros)
             .addContainerGap(32, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout generosFrameLayout = new javax.swing.GroupLayout(generosFrame.getContentPane());
     generosFrame.getContentPane().setLayout(generosFrameLayout);
     generosFrameLayout.setHorizontalGroup(
         generosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 542, Short.MAX_VALUE)
         .addGroup(generosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(generosFrameLayout.createSequentialGroup()
                 .addGap(0, 29, Short.MAX_VALUE)
                 .addComponent(jGenerosPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 30, Short.MAX_VALUE)))
     );
     generosFrameLayout.setVerticalGroup(
         generosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 414, Short.MAX_VALUE)
         .addGroup(generosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(generosFrameLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(jGenerosPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE)))
     );
 
     resultadosFrame.setAlwaysOnTop(true);
     resultadosFrame.setMinimumSize(new java.awt.Dimension(800, 600));
 
     paisResultadosFilme.setEditable(false);
 
     jLabel74.setText("País:");
 
     jTextArea14.setColumns(20);
     jTextArea14.setEditable(false);
     jTextArea14.setRows(5);
     jScrollPane18.setViewportView(jTextArea14);
 
     jLabel75.setText("Descrição:");
 
     imdbResultadosFilme.setEditable(false);
 
     anoResultadosFilme.setEditable(false);
 
     jLabel77.setText("Resultados:");
 
     jScrollPane19.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
     listaResultados.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
     jScrollPane19.setViewportView(listaResultados);
 
     voltarResultados.setText("Voltar");
     voltarResultados.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarResultadosActionPerformed(evt);
         }
     });
 
     jLabel78.setFont(new java.awt.Font("Tahoma", 0, 24));
     jLabel78.setText("Resultados Filme");
 
     jLabel79.setText("Título:");
 
     jLabel80.setText("Género:");
 
     tituloResultadosFilme.setEditable(false);
 
     jLabel81.setText("Ano:");
 
     jLabel82.setText("Realizador:");
 
     realizadorResultadosFilme.setEditable(false);
 
     jLabel83.setText("Produtor:");
 
     produtorResultadosFilme.setEditable(false);
 
     jLabel84.setText("IMDB RATING: ");
 
     jScrollPane20.setViewportView(jList5);
 
     listaFormatosResultadosFilmes.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Blu-Ray", "DVD", "UMD"}));
 
     jLabel98.setText("Formato:");
 
     jLabel99.setText("BI do Cliente:");
 
     alugar.setText("Alugar");
     alugar.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             alugarActionPerformed(evt);
         }
     });
 
     jTextField1.setEditable(false);
 
     adicionarStockFilme.setText("Adicionar Stock ao Filme");
     adicionarStockFilme.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             adicionarStockFilmeActionPerformed(evt);
         }
     });
 
     eliminarStockFilme.setText("Eliminar Stock do Filme");
     eliminarStockFilme.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             eliminarStockFilmeActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout jResultadosFilmePanelLayout = new javax.swing.GroupLayout(jResultadosFilmePanel);
     jResultadosFilmePanel.setLayout(jResultadosFilmePanelLayout);
     jResultadosFilmePanelLayout.setHorizontalGroup(
         jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addGap(490, 490, 490)
                     .addComponent(jSeparator4, javax.swing.GroupLayout.DEFAULT_SIZE, 463, Short.MAX_VALUE))
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addGap(83, 83, 83)
                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel77)
                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jResultadosFilmePanelLayout.createSequentialGroup()
                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(jLabel82)
                                                 .addComponent(jLabel74)
                                                 .addComponent(jLabel83)
                                                 .addComponent(jLabel75)
                                                 .addComponent(jLabel79))
                                             .addGap(31, 31, 31)
                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                 .addComponent(listaFormatosResultadosFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                                                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                         .addComponent(jScrollPane18, javax.swing.GroupLayout.Alignment.LEADING)
                                                         .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jResultadosFilmePanelLayout.createSequentialGroup()
                                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                                 .addComponent(produtorResultadosFilme, javax.swing.GroupLayout.Alignment.LEADING)
                                                                 .addComponent(realizadorResultadosFilme, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                 .addComponent(tituloResultadosFilme, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
                                                                 .addComponent(paisResultadosFilme, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
                                                             .addGap(31, 31, 31)
                                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                 .addComponent(jLabel84)
                                                                 .addComponent(jLabel81)
                                                                 .addComponent(jLabel80))))
                                                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                         .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                                                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                 .addComponent(imdbResultadosFilme)
                                                                 .addComponent(anoResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                 .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                         .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                                                             .addGap(54, 54, 54)
                                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                 .addComponent(eliminarStockFilme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                 .addComponent(adicionarStockFilme, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))))
                                         .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                                             .addGap(1, 1, 1)
                                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                 .addComponent(jLabel98)
                                                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                                                     .addComponent(jLabel99)
                                                     .addGap(18, 18, 18)
                                                     .addComponent(idAlugaFilme, javax.swing.GroupLayout.DEFAULT_SIZE, 52, Short.MAX_VALUE)
                                                     .addGap(18, 18, 18)
                                                     .addComponent(alugar)
                                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                     .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                     .addComponent(voltarResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                     .addGap(120, 120, 120)))
                                             .addGap(106, 106, 106)))
                                     .addGap(15, 15, 15)
                                     .addComponent(jLabel76, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addComponent(jScrollPane19, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 481, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                             .addGap(148, 148, 148)
                             .addComponent(jLabel78)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 481, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGap(62, 62, 62)))
             .addContainerGap())
     );
     jResultadosFilmePanelLayout.setVerticalGroup(
         jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResultadosFilmePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jLabel78)
             .addGap(9, 9, 9)
             .addComponent(jLabel77)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(jScrollPane19, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addGap(32, 32, 32)
                     .addComponent(jLabel83)
                     .addGap(26, 26, 26)
                     .addComponent(jLabel82)
                     .addGap(18, 18, 18)
                     .addComponent(jLabel74))
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(tituloResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(jLabel79))
                     .addGap(18, 18, 18)
                     .addComponent(produtorResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGap(20, 20, 20)
                     .addComponent(realizadorResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(paisResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel81)
                         .addComponent(anoResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGap(18, 18, 18)
                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLabel76, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(jLabel84)
                                 .addComponent(imdbResultadosFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGap(20, 20, 20)
                             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jScrollPane20, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jLabel80))))))
             .addGap(25, 25, 25)
             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLabel75)
                         .addComponent(jScrollPane18, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel98)
                         .addComponent(listaFormatosResultadosFilmes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGroup(jResultadosFilmePanelLayout.createSequentialGroup()
                     .addComponent(adicionarStockFilme)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(eliminarStockFilme)))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jResultadosFilmePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel99)
                 .addComponent(idAlugaFilme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(alugar)
                 .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(voltarResultados))
             .addGap(35, 35, 35)
             .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap())
     );
 
     javax.swing.GroupLayout resultadosFrameLayout = new javax.swing.GroupLayout(resultadosFrame.getContentPane());
     resultadosFrame.getContentPane().setLayout(resultadosFrameLayout);
     resultadosFrameLayout.setHorizontalGroup(
         resultadosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultadosFrameLayout.createSequentialGroup()
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jResultadosFilmePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap())
     );
     resultadosFrameLayout.setVerticalGroup(
         resultadosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultadosFrameLayout.createSequentialGroup()
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(jResultadosFilmePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 555, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap())
     );
 
     adicionarClienteFrame.setAlwaysOnTop(true);
     adicionarClienteFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     adicionarClienteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             adicionarClienteFrameWindowClosing(evt);
         }
     });
 
     jLabel24.setText("Confirm Password:");
 
     jLabel29.setText("BI:");
 
     jLabel33.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel33.setText("Adicionar Novo Cliente");
 
     outputAdicionaClientes.setColumns(20);
     outputAdicionaClientes.setRows(5);
     jScrollPane7.setViewportView(outputAdicionaClientes);
 
     jVoltarACFButton.setText("Voltar");
     jVoltarACFButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jVoltarACFButtonActionPerformed(evt);
         }
     });
 
     adicionarCliente.setText("Adicionar/Actualizar");
     adicionarCliente.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             adicionarClienteActionPerformed(evt);
         }
     });
 
     obterDadosAdicionarClientes.setText("Obter Dados Para Actualização");
     obterDadosAdicionarClientes.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             obterDadosAdicionarClientesActionPerformed(evt);
         }
     });
 
     jLabel25.setText("Nome:");
 
     jLabel26.setText("Telefone:");
 
     jLabel27.setText("Password:");
 
     jLabel28.setText("Morada:");
 
     jLabel30.setText("E-mail:");
 
     javax.swing.GroupLayout jAdicionarClientePanelLayout = new javax.swing.GroupLayout(jAdicionarClientePanel);
     jAdicionarClientePanel.setLayout(jAdicionarClientePanelLayout);
     jAdicionarClientePanelLayout.setHorizontalGroup(
         jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jAdicionarClientePanelLayout.createSequentialGroup()
             .addGap(123, 123, 123)
             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jLabel33)
                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                     .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLabel26)
                         .addComponent(jLabel25)
                         .addComponent(jLabel29)
                         .addComponent(jLabel27)
                         .addComponent(jLabel28)
                         .addComponent(jLabel30))
                     .addGap(18, 18, 18)
                     .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(nomeAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                                     .addComponent(biAdicionaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                                     .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                         .addComponent(passwordAdicionaClientes, javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(telefoneAdicionaClientes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE))
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                     .addComponent(jLabel24)
                                     .addGap(18, 18, 18)
                                     .addComponent(passwordAdicionaClientes2, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                                     .addGap(31, 31, 31)
                                     .addComponent(obterDadosAdicionarClientes))))
                         .addComponent(moradaAdicionaClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                         .addComponent(emailAdicionaClientes)))
                 .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jVoltarACFButton)
                     .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(adicionarCliente)
                         .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 476, javax.swing.GroupLayout.PREFERRED_SIZE))))
             .addContainerGap())
     );
     jAdicionarClientePanelLayout.setVerticalGroup(
         jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
             .addGap(76, 76, 76)
             .addComponent(jLabel33)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel25)
                 .addComponent(nomeAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                     .addGap(10, 10, 10)
                     .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(biAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(obterDadosAdicionarClientes))
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel26)
                         .addComponent(telefoneAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGroup(jAdicionarClientePanelLayout.createSequentialGroup()
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(jLabel29)))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel27)
                 .addComponent(passwordAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel24)
                 .addComponent(passwordAdicionaClientes2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGap(11, 11, 11)
             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel28)
                 .addComponent(moradaAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGap(18, 18, 18)
             .addGroup(jAdicionarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(emailAdicionaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel30))
             .addGap(42, 42, 42)
             .addComponent(adicionarCliente)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jVoltarACFButton)
             .addContainerGap(110, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout adicionarClienteFrameLayout = new javax.swing.GroupLayout(adicionarClienteFrame.getContentPane());
     adicionarClienteFrame.getContentPane().setLayout(adicionarClienteFrameLayout);
     adicionarClienteFrameLayout.setHorizontalGroup(
         adicionarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 943, Short.MAX_VALUE)
         .addGroup(adicionarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(adicionarClienteFrameLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jAdicionarClientePanel, javax.swing.GroupLayout.PREFERRED_SIZE, 709, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(224, Short.MAX_VALUE)))
     );
     adicionarClienteFrameLayout.setVerticalGroup(
         adicionarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 638, Short.MAX_VALUE)
         .addGroup(adicionarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, adicionarClienteFrameLayout.createSequentialGroup()
                 .addContainerGap(40, Short.MAX_VALUE)
                 .addComponent(jAdicionarClientePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(26, 26, 26)))
     );
 
     eliminarClienteFrame.setAlwaysOnTop(true);
     eliminarClienteFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     eliminarClienteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             eliminarClienteFrameWindowClosing(evt);
         }
     });
 
     jLabel50.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel50.setText("Eliminar Clientes:");
 
     jLabel51.setText("BI:");
 
     outEliminaClientes.setColumns(20);
     outEliminaClientes.setEditable(false);
     outEliminaClientes.setRows(5);
     jScrollPane11.setViewportView(outEliminaClientes);
 
     jButton14.setText("Voltar");
     jButton14.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton14ActionPerformed(evt);
         }
     });
 
     eliminarClienteBI.setText("Eliminar");
     eliminarClienteBI.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             eliminarClienteBIActionPerformed(evt);
         }
     });
 
     listaEliminarClientes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
     jScrollPane8.setViewportView(listaEliminarClientes);
 
     eliminarClientesLista.setText("Eliminar da Lista de Clientes");
     eliminarClientesLista.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             eliminarClientesListaActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout jEliminarClientePanelLayout = new javax.swing.GroupLayout(jEliminarClientePanel);
     jEliminarClientePanel.setLayout(jEliminarClientePanelLayout);
     jEliminarClientePanelLayout.setHorizontalGroup(
         jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jEliminarClientePanelLayout.createSequentialGroup()
             .addGroup(jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addGroup(jEliminarClientePanelLayout.createSequentialGroup()
                     .addContainerGap()
                     .addComponent(jButton14))
                 .addGroup(jEliminarClientePanelLayout.createSequentialGroup()
                     .addGap(135, 135, 135)
                     .addGroup(jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jScrollPane8, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE)
                             .addComponent(jLabel50, javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jScrollPane11, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 426, Short.MAX_VALUE))
                         .addGroup(jEliminarClientePanelLayout.createSequentialGroup()
                             .addGroup(jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addComponent(eliminarClientesLista)
                                 .addGroup(jEliminarClientePanelLayout.createSequentialGroup()
                                     .addComponent(jLabel51)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                     .addComponent(biEliminaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addGap(56, 56, 56)
                                     .addComponent(eliminarClienteBI)))
                             .addGap(140, 140, 140)))))
             .addGap(117, 117, 117))
     );
     jEliminarClientePanelLayout.setVerticalGroup(
         jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jEliminarClientePanelLayout.createSequentialGroup()
             .addGap(27, 27, 27)
             .addComponent(jLabel50)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 134, Short.MAX_VALUE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(eliminarClientesLista)
             .addGap(25, 25, 25)
             .addGroup(jEliminarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel51)
                 .addComponent(biEliminaClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(eliminarClienteBI))
             .addGap(18, 18, 18)
             .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jButton14)
             .addGap(50, 50, 50))
     );
 
     javax.swing.GroupLayout eliminarClienteFrameLayout = new javax.swing.GroupLayout(eliminarClienteFrame.getContentPane());
     eliminarClienteFrame.getContentPane().setLayout(eliminarClienteFrameLayout);
     eliminarClienteFrameLayout.setHorizontalGroup(
         eliminarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 678, Short.MAX_VALUE)
         .addGroup(eliminarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(eliminarClienteFrameLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(jEliminarClientePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE)))
     );
     eliminarClienteFrameLayout.setVerticalGroup(
         eliminarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 439, Short.MAX_VALUE)
         .addGroup(eliminarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(eliminarClienteFrameLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(jEliminarClientePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE)))
     );
 
     notificarClientesFrame.setAlwaysOnTop(true);
     notificarClientesFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     notificarClientesFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             notificarClientesFrameWindowClosing(evt);
         }
     });
 
     jLabel35.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel35.setText("Notificar Cliente:");
 
     jLabel42.setText("BI :");
 
     mensagem.setColumns(20);
     mensagem.setRows(5);
     jScrollPane9.setViewportView(mensagem);
 
     jLabel52.setText("Mensagem:");
 
     jToggleButton34.setText("Voltar");
     jToggleButton34.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jToggleButton34ActionPerformed(evt);
         }
     });
 
     enviarEmail.setText("Enviar E-Mail");
     enviarEmail.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             enviarEmailActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout jNotificarClientePanelLayout = new javax.swing.GroupLayout(jNotificarClientePanel);
     jNotificarClientePanel.setLayout(jNotificarClientePanelLayout);
     jNotificarClientePanelLayout.setHorizontalGroup(
         jNotificarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jNotificarClientePanelLayout.createSequentialGroup()
             .addGap(78, 78, 78)
             .addGroup(jNotificarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jLabel35)
                 .addGroup(jNotificarClientePanelLayout.createSequentialGroup()
                     .addComponent(jLabel42)
                     .addGap(18, 18, 18)
                     .addComponent(biNotificarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addComponent(jLabel52)
                 .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 364, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGroup(jNotificarClientePanelLayout.createSequentialGroup()
                     .addGap(197, 197, 197)
                     .addComponent(enviarEmail)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(jToggleButton34)))
             .addContainerGap(93, Short.MAX_VALUE))
     );
     jNotificarClientePanelLayout.setVerticalGroup(
         jNotificarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jNotificarClientePanelLayout.createSequentialGroup()
             .addGap(47, 47, 47)
             .addComponent(jLabel35)
             .addGap(18, 18, 18)
             .addGroup(jNotificarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel42)
                 .addComponent(biNotificarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGap(24, 24, 24)
             .addComponent(jLabel52)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGap(24, 24, 24)
             .addGroup(jNotificarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jToggleButton34)
                 .addComponent(enviarEmail))
             .addContainerGap(61, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout notificarClientesFrameLayout = new javax.swing.GroupLayout(notificarClientesFrame.getContentPane());
     notificarClientesFrame.getContentPane().setLayout(notificarClientesFrameLayout);
     notificarClientesFrameLayout.setHorizontalGroup(
         notificarClientesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notificarClientesFrameLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jNotificarClientePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addContainerGap())
     );
     notificarClientesFrameLayout.setVerticalGroup(
         notificarClientesFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, notificarClientesFrameLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jNotificarClientePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addContainerGap())
     );
 
     pesquisarClienteFrame.setAlwaysOnTop(true);
     pesquisarClienteFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     pesquisarClienteFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             pesquisarClienteFrameWindowClosing(evt);
         }
     });
 
     jLabel43.setText("ID:");
 
     jLabel44.setText("BI:");
 
     jLabel45.setText("Morada:");
 
     jLabel46.setText("E-mail:");
 
     jLabel47.setText("Telefone:");
 
     jLabel48.setText("Nome:");
 
     jLabel49.setFont(new java.awt.Font("Tahoma", 0, 16));
     jLabel49.setText("Pesquisar Cliente:");
 
     pesquisarClientesButton.setText("Pesquisar Clientes");
     pesquisarClientesButton.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             pesquisarClientesButtonActionPerformed(evt);
         }
     });
 
     outPesquisarClientes.setColumns(20);
     outPesquisarClientes.setEditable(false);
     outPesquisarClientes.setRows(5);
     jScrollPane22.setViewportView(outPesquisarClientes);
 
     voltarPesquisarCliente.setText("Voltar");
     voltarPesquisarCliente.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarPesquisarClienteActionPerformed(evt);
         }
     });
 
     pesquisarPorBI.setText("Pesquisar por BI");
     pesquisarPorBI.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             pesquisarPorBIActionPerformed(evt);
         }
     });
 
     pesquisarPorID.setText("Pesquisar por ID");
     pesquisarPorID.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             pesquisarPorIDActionPerformed(evt);
         }
     });
 
     jButton1.setText("Ver Lista de Clientes");
     jButton1.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             jButton1ActionPerformed(evt);
         }
     });
 
     javax.swing.GroupLayout jPesquisarClientePanelLayout = new javax.swing.GroupLayout(jPesquisarClientePanel);
     jPesquisarClientePanel.setLayout(jPesquisarClientePanelLayout);
     jPesquisarClientePanelLayout.setHorizontalGroup(
         jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPesquisarClientePanelLayout.createSequentialGroup()
             .addGap(98, 98, 98)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPesquisarClientePanelLayout.createSequentialGroup()
                     .addComponent(jLabel47)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                     .addComponent(telefonePesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPesquisarClientePanelLayout.createSequentialGroup()
                         .addComponent(pesquisarClientesButton)
                         .addGap(55, 55, 55)
                         .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(194, 194, 194))
                     .addGroup(jPesquisarClientePanelLayout.createSequentialGroup()
                         .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(48, 48, 48)
                         .addComponent(voltarPesquisarCliente)))
                 .addComponent(jLabel49)
                 .addComponent(jLabel43)
                 .addComponent(jLabel44)
                 .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPesquisarClientePanelLayout.createSequentialGroup()
                         .addComponent(jLabel45)
                         .addGap(11, 11, 11)
                         .addComponent(moradaPesquisarClientes))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPesquisarClientePanelLayout.createSequentialGroup()
                         .addComponent(jLabel46)
                         .addGap(18, 18, 18)
                         .addComponent(emailPesquisarClientes))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPesquisarClientePanelLayout.createSequentialGroup()
                         .addComponent(jLabel48)
                         .addGap(19, 19, 19)
                         .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(nomePesquisarClientes)
                             .addGroup(jPesquisarClientePanelLayout.createSequentialGroup()
                                 .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                     .addComponent(biPesquisarClientes, javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(idPesquisarClientes, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE))
                                 .addGap(18, 18, 18)
                                 .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(pesquisarPorBI)
                                     .addComponent(pesquisarPorID)))))))
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
     jPesquisarClientePanelLayout.setVerticalGroup(
         jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPesquisarClientePanelLayout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jLabel49)
             .addGap(21, 21, 21)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel43)
                 .addComponent(idPesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(pesquisarPorID))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel44)
                 .addComponent(biPesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(pesquisarPorBI))
             .addGap(18, 18, 18)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel48)
                 .addComponent(nomePesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel47)
                 .addComponent(telefonePesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel46)
                 .addComponent(emailPesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel45)
                 .addComponent(moradaPesquisarClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGap(21, 21, 21)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(pesquisarClientesButton)
                 .addComponent(jButton1))
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addGroup(jPesquisarClientePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                 .addComponent(jScrollPane22, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(voltarPesquisarCliente))
             .addContainerGap(139, Short.MAX_VALUE))
     );
 
     javax.swing.GroupLayout pesquisarClienteFrameLayout = new javax.swing.GroupLayout(pesquisarClienteFrame.getContentPane());
     pesquisarClienteFrame.getContentPane().setLayout(pesquisarClienteFrameLayout);
     pesquisarClienteFrameLayout.setHorizontalGroup(
         pesquisarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(pesquisarClienteFrameLayout.createSequentialGroup()
             .addComponent(jPesquisarClientePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
     );
     pesquisarClienteFrameLayout.setVerticalGroup(
         pesquisarClienteFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(pesquisarClienteFrameLayout.createSequentialGroup()
             .addComponent(jPesquisarClientePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(12, Short.MAX_VALUE))
     );
 
     formatosFrame.setAlwaysOnTop(true);
     formatosFrame.setMinimumSize(new java.awt.Dimension(800, 600));
     formatosFrame.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             formatosFrameWindowClosing(evt);
         }
     });
 
     jScrollPane23.setViewportView(listaFormato);
 
     jLabel34.setText("Lista de Formatos:");
 
     jLabel36.setText("Nome:");
 
     adicionarFormato.setText("Adicionar");
     adicionarFormato.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             adicionarFormatoActionPerformed(evt);
         }
     });
 
     voltarFormatos.setText("Voltar");
     voltarFormatos.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarFormatosActionPerformed(evt);
         }
     });
 
     eliminarFormato.setText("Eliminar");
     eliminarFormato.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             eliminarFormatoActionPerformed(evt);
         }
     });
 
     outFormato.setColumns(20);
     outFormato.setRows(5);
     jScrollPane24.setViewportView(outFormato);
 
     javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
     jPanel1.setLayout(jPanel1Layout);
     jPanel1Layout.setHorizontalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
             .addGap(111, 111, 111)
             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                     .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                         .addComponent(jScrollPane24, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                         .addGroup(jPanel1Layout.createSequentialGroup()
                             .addComponent(jLabel36)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(textFormato, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addComponent(adicionarFormato))
                         .addComponent(voltarFormatos))
                     .addGap(33, 33, 33))
                 .addComponent(jLabel34)
                 .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 203, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGroup(jPanel1Layout.createSequentialGroup()
                     .addGap(70, 70, 70)
                     .addComponent(eliminarFormato)))
             .addGap(124, 124, 124))
     );
     jPanel1Layout.setVerticalGroup(
         jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(jPanel1Layout.createSequentialGroup()
             .addContainerGap()
             .addComponent(jLabel34)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(jScrollPane23, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
             .addComponent(eliminarFormato)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 76, Short.MAX_VALUE)
             .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(textFormato, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(adicionarFormato)
                 .addComponent(jLabel36))
             .addGap(18, 18, 18)
             .addComponent(jScrollPane24, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
             .addComponent(voltarFormatos)
             .addGap(14, 14, 14))
     );
 
     javax.swing.GroupLayout formatosFrameLayout = new javax.swing.GroupLayout(formatosFrame.getContentPane());
     formatosFrame.getContentPane().setLayout(formatosFrameLayout);
     formatosFrameLayout.setHorizontalGroup(
         formatosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
     );
     formatosFrameLayout.setVerticalGroup(
         formatosFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(formatosFrameLayout.createSequentialGroup()
             .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addContainerGap(31, Short.MAX_VALUE))
     );
 
     resultadosClientes.setAlwaysOnTop(true);
     resultadosClientes.setMinimumSize(new java.awt.Dimension(800, 600));
     resultadosClientes.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             resultadosClientesWindowClosing(evt);
         }
     });
 
     emailResultadosClientes.setEditable(false);
 
     jLabel105.setText("E-mail:");
 
     jLabel106.setText("Morada:");
 
     moradaResultadosClientes.setEditable(false);
 
     jLabel107.setText("Telefone:");
 
     telefoneResultadosClientes.setEditable(false);
 
     jLabel108.setText("BI:");
 
     biResultadosClientes.setEditable(false);
 
     jLabel109.setText("Nome:");
 
     nomeResultadosClientes.setEditable(false);
 
     voltarResultadosClientes.setText("Voltar");
     voltarResultadosClientes.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent evt) {
             voltarResultadosClientesActionPerformed(evt);
         }
     });
 
     jLabel96.setText("Lista de Clientes:");
 
     listaResultadosClientes.setModel(new javax.swing.AbstractListModel() {
         String[] strings = { ""};
         public int getSize() { return strings.length; }
         public Object getElementAt(int i) { return strings[i]; }
     });
     jScrollPane10.setViewportView(listaResultadosClientes);
 
     javax.swing.GroupLayout resultadosClientesPanelLayout = new javax.swing.GroupLayout(resultadosClientesPanel);
     resultadosClientesPanel.setLayout(resultadosClientesPanelLayout);
     resultadosClientesPanelLayout.setHorizontalGroup(
         resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultadosClientesPanelLayout.createSequentialGroup()
             .addContainerGap(365, Short.MAX_VALUE)
             .addComponent(voltarResultadosClientes)
             .addGap(241, 241, 241))
         .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
             .addGap(39, 39, 39)
             .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 361, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel96)
                 .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
                     .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addComponent(jLabel107)
                         .addComponent(jLabel109)
                         .addComponent(jLabel108)
                         .addComponent(jLabel106)
                         .addComponent(jLabel105))
                     .addGap(18, 18, 18)
                     .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(nomeResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
                             .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
                                     .addComponent(biResultadosClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                 .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
                                     .addComponent(telefoneResultadosClientes, javax.swing.GroupLayout.DEFAULT_SIZE, 119, Short.MAX_VALUE)
                                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                             .addGap(237, 237, 237))
                         .addComponent(emailResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(moradaResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, 286, javax.swing.GroupLayout.PREFERRED_SIZE))))
             .addContainerGap(206, Short.MAX_VALUE))
     );
     resultadosClientesPanelLayout.setVerticalGroup(
         resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, resultadosClientesPanelLayout.createSequentialGroup()
             .addGap(34, 34, 34)
             .addComponent(jLabel96)
             .addGap(2, 2, 2)
             .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
             .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel109)
                 .addComponent(nomeResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
                     .addGap(10, 10, 10)
                     .addComponent(biResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(jLabel107)
                         .addComponent(telefoneResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addGroup(resultadosClientesPanelLayout.createSequentialGroup()
                     .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                     .addComponent(jLabel108)))
             .addGap(37, 37, 37)
             .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(jLabel106)
                 .addComponent(moradaResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
             .addGap(18, 18, 18)
             .addGroup(resultadosClientesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(emailResultadosClientes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(jLabel105))
             .addGap(18, 18, 18)
             .addComponent(voltarResultadosClientes)
             .addGap(50, 50, 50))
     );
 
     javax.swing.GroupLayout resultadosClientesLayout = new javax.swing.GroupLayout(resultadosClientes.getContentPane());
     resultadosClientes.getContentPane().setLayout(resultadosClientesLayout);
     resultadosClientesLayout.setHorizontalGroup(
         resultadosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 667, Short.MAX_VALUE)
         .addGroup(resultadosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(resultadosClientesLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(resultadosClientesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE)))
     );
     resultadosClientesLayout.setVerticalGroup(
         resultadosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGap(0, 465, Short.MAX_VALUE)
         .addGroup(resultadosClientesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(resultadosClientesLayout.createSequentialGroup()
                 .addGap(0, 0, Short.MAX_VALUE)
                 .addComponent(resultadosClientesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE)))
     );
 
     setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
     addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosing(java.awt.event.WindowEvent evt) {
             formWindowClosing(evt);
         }
     });
 
     mainPanel.setBackground(new java.awt.Color(204, 255, 153));
     mainPanel.setDoubleBuffered(false);
     mainPanel.setMaximumSize(new java.awt.Dimension(800, 600));
     mainPanel.setMinimumSize(new java.awt.Dimension(800, 600));
     mainPanel.setOpaque(false);
     mainPanel.setPreferredSize(new java.awt.Dimension(800, 600));
     mainPanel.setLayout(null);
 
     javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
     getContentPane().setLayout(layout);
     layout.setHorizontalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
             .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 922, Short.MAX_VALUE)
             .addContainerGap())
     );
     layout.setVerticalGroup(
         layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
         .addGroup(layout.createSequentialGroup()
             .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 657, Short.MAX_VALUE)
             .addContainerGap())
     );
 
     pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jUsernameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jUsernameFieldActionPerformed
         // TODO add your handling code here:
 }//GEN-LAST:event_jUsernameFieldActionPerformed
 
     private void jVenderATMButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVenderATMButtonActionPerformed
 
         String idMaquina=((String)listaMaquinas.getSelectedValue()).split(" ")[0];
         outMaquinas.setText(gestorMaquinas.invalidaMaquinaATM(idMaquina));
         listaMaquinas.setModel(new OurListModel(gestorMaquinas.verListaMaquinasATM()));
 
     }//GEN-LAST:event_jVenderATMButtonActionPerformed
 
     private void empregadosCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_empregadosCheckBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_empregadosCheckBoxActionPerformed
 
     private void contabilidadeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contabilidadeBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_contabilidadeBoxActionPerformed
 
     private void jPesquisarClientesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPesquisarClientesButtonActionPerformed
         listaResultadosClientes.setModel(new OurListModel(gestorClientes.verListaClientes()));
         pesquisarClienteFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jPesquisarClientesButtonActionPerformed
 
     private void jAdicionarClientesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAdicionarClientesButtonActionPerformed
         // TODO add your handling code here:
         adicionarClienteFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jAdicionarClientesButtonActionPerformed
 
     private void jEliminarFilmeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEliminarFilmeButtonActionPerformed
         
         eliminarFilmesFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jEliminarFilmeButtonActionPerformed
 
     private void pesquisarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarClientesActionPerformed
         // TODO add your handling code here:
         listaResultadosClientes.setModel(new OurListModel(gestorClientes.verListaClientes()));
         pesquisarClienteFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_pesquisarClientesActionPerformed
 
     private void adicionarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarClientesActionPerformed
         // TODO add your handling code here:
         adicionarClienteFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_adicionarClientesActionPerformed
 
     private void jFileChooser1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser1ActionPerformed
         //TODO : Código seleccionar fiheiro!
         filePath=jFileChooser1.getSelectedFile().getAbsolutePath();
         
         ficheirosFrame.setVisible(false);
         ficheirosFrame.transferFocusBackward();
     }//GEN-LAST:event_jFileChooser1ActionPerformed
 
     private void pesquisarFilmeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarFilmeActionPerformed
         
         //Fazer a pesquisa
         //TODO fazer parsing do output do método
         String []lista;
         String[] generos={(String) generosBox.getSelectedItem()};
         if(generos[0].isEmpty())
             generos=null;
         //procuraFilmes(String titulo, String anoLow, String anoHigh, String realizador, String ratingIMDBLow, String ratingIMDBHigh, String pais, String produtora, String[] generos)
         if((Integer)anoBSpinner.getValue()<(Integer)anoESpinner.getValue()
                 &&(Double)imdbBSpinner.getValue()<(Double)imdbESpinner.getValue()){
             Utils.dbg("Pesquisa!");
             lista=gestorFilmes.procuraFilmesPlusInvalidos(
                     textTituloPesquisaFilmes.getText(),
                     ""+(Integer)anoBSpinner.getValue(),
                     ""+(Integer)anoESpinner.getValue(),
                     textRealizadorPesquisaFilmes.getText(),
                     ""+(Double)imdbBSpinner.getValue(),
                     ""+(Double)imdbESpinner.getValue(),
                     (String) countriesList1.getSelectedItem(),
                     textProdutorPesquisaFilmes.getText(),
                     generos);
             listaResultados.setModel(new OurListModel(lista) );
         }
         
         
         //Reset aos campos
       textRealizadorPesquisaFilmes.setText(null);
       textIdPesquisaFilmes.setText(null);
       textTituloPesquisaFilmes.setText(null);
       textProdutorPesquisaFilmes.setText(null);
       anoBSpinner.setValue((Integer)1917);
       anoESpinner.setValue((Integer)2011);
       imdbBSpinner.setValue((Double)1.0);
       imdbESpinner.setValue((Double)10.0);
       pesquisarFilmesFrame.setVisible(false);
       pesquisarFilmesFrame.transferFocusBackward();
       listaFormatosResultadosFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
 
       resultadosFrame.setVisible(true);
       transferFocus();
     }//GEN-LAST:event_pesquisarFilmeActionPerformed
 
     private void jSairButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSairButtonActionPerformed
         
         //Set panel's visibility
 
         db.close();
         jLoginPanel.setVisible(true);
         
         jMenuOperatorPanel.setVisible(false);
         jMenuAdministradorPanel.setVisible(false);
         
     }//GEN-LAST:event_jSairButtonActionPerformed
 
     private void jSairButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jSairButton2ActionPerformed
         
         //Set panel's visibility
         db.close();
         jLoginPanel.setVisible(true);
        
         jMenuOperatorPanel.setVisible(false);
         jMenuAdministradorPanel.setVisible(false);
         
     }//GEN-LAST:event_jSairButton2ActionPerformed
 
     private void jEscolherFicheiroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEscolherFicheiroButtonActionPerformed
         // TODO add your handling code here:
         ficheirosFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jEscolherFicheiroButtonActionPerformed
 
     private void jLoginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jLoginButtonActionPerformed
          try{
             db=new DBHandler();
         }catch (Exception e){
             System.exit(-1);
         }
         String out=gestorUtilizadores.loginEmpregado(jUsernameField.getText(), jPasswordField.getText());
        
         if(!out.equals("FAIL")){
             if(out.equals("1")){
                 //administrador
                 jMenuAdministradorPanel.setVisible(true);
             }else{
                 //empregado
                 jMenuOperatorPanel.setVisible(true);
             }
             jLoginPanel.setVisible(false);
         }
        jUsernameField.setText("");
        jPasswordField.setText("");
     }//GEN-LAST:event_jLoginButtonActionPerformed
 
     private void jAdicionarATMButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAdicionarATMButtonActionPerformed
         String output="";
         output=gestorMaquinas.adicionaMaquinaATM(""+((Double)jSpinner6.getValue()));
         outMaquinas.setText(output);
         listaMaquinas.setModel(new OurListModel(gestorMaquinas.verListaMaquinasATM()));
     }//GEN-LAST:event_jAdicionarATMButtonActionPerformed
 
     private void jVerificarPagamentosAtrasoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVerificarPagamentosAtrasoButtonActionPerformed
 
         pagamentosAtraso.setModel(new OurListModel(gestorClientes.getClientesComEntregasForaDePrazo()));
     }//GEN-LAST:event_jVerificarPagamentosAtrasoButtonActionPerformed
 
     private void jEliminarClientesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jEliminarClientesButtonActionPerformed
         listaEliminarClientes.setModel(new OurListModel(gestorClientes.verListaClientes()));
         eliminarClienteFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jEliminarClientesButtonActionPerformed
 
     private void jNotificarClientesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jNotificarClientesButtonActionPerformed
         notificarClientesFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jNotificarClientesButtonActionPerformed
 
     private void jPesquisarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jPesquisarButtonActionPerformed
         String[] lista=gestorFilmes.verListaGeneros();
         String[] inputLista=new String[lista.length+1];
         inputLista[0]="";
         System.arraycopy(lista, 0, inputLista, 1, lista.length);
         generosBox.setModel(new javax.swing.DefaultComboBoxModel(inputLista));
         pesquisarFilmesFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jPesquisarButtonActionPerformed
 
     private void jGeneroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jGeneroButtonActionPerformed
         // TODO add your handling code here:
         //refresh à lista de géneros
         
         listaGeneros.setModel(new OurListModel(gestorFilmes.verListaGeneros()));
 
         jScrollPane21.setViewportView(listaGeneros);
         //mostrar a frame
         generosFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jGeneroButtonActionPerformed
 
     private void jAdicionarFilmesToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAdicionarFilmesToggleButtonActionPerformed
         // TODO add your handling code here:
         //Set aos generos existentes
         listaFormatosAdicionaFilme.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
         listaGenerosAdicionaFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         //listaGenerosAdicionaFilmes1.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         adicionarFilmeFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jAdicionarFilmesToggleButtonActionPerformed
 
     private void adicionarClienteFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_adicionarClienteFrameWindowClosing
        adicionarClienteFrame.setVisible(false);
        adicionarClienteFrame.transferFocusBackward();
     }//GEN-LAST:event_adicionarClienteFrameWindowClosing
 
     private void jVoltarACFButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVoltarACFButtonActionPerformed
        adicionarClienteFrame.setVisible(false);
        adicionarClienteFrame.transferFocusBackward();
     }//GEN-LAST:event_jVoltarACFButtonActionPerformed
 
     private void voltarGenerosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarGenerosActionPerformed
        outGenero.setText("");
        textGenero.setText("");
        generosFrame.setVisible(false);
        generosFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarGenerosActionPerformed
 
     private void generosFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_generosFrameWindowClosing
        outGenero.setText("");
        textGenero.setText("");
        generosFrame.setVisible(false);
        generosFrame.transferFocusBackward();
     }//GEN-LAST:event_generosFrameWindowClosing
 
     private void adicionaGeneroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionaGeneroActionPerformed
 
         String output=gestorFilmes.adicionaGenero(textGenero.getText());
         textGenero.setText(null);
         outGenero.setText(output);
         listaGeneros.setModel(new OurListModel(gestorFilmes.verListaGeneros()));
 
         jScrollPane21.setViewportView(listaGeneros);
     }//GEN-LAST:event_adicionaGeneroActionPerformed
 
     private void adicionaGenero2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionaGenero2ActionPerformed
         // TODO add your handling code here:
 
         listaGeneros.setModel(new OurListModel(gestorFilmes.verListaGeneros()));
 
         jScrollPane21.setViewportView(listaGeneros);
         //mostrar a frame
         generosFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_adicionaGenero2ActionPerformed
 
     private void listarFormatoEliminarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listarFormatoEliminarActionPerformed
     	// TODO by Lobo: o verListaStocksFilmeFull() so recebe o ID do filme e lista-te a informa��o dos stocks do filme em todos os formatos.
     	// aki so pego no 1� elemento do array de strings que � para o setText funcionar.
     	// Depois tens de p�r isto como queres ou dizer-me para fazer um metodo k fa�a o que queres mesmo
     	
         // textEliminaFilmes.setText(gestorFilmes.verListaFormatos(idEliminaFilmes.getText(), (String)listaFormatos.getSelectedItem()));
         if(Utils.isInt(idEliminaFilmes.getText()) )
             textEliminaFilmes.setModel(new OurListModel(gestorFilmes.verListaStocksFilmeFull(idEliminaFilmes.getText())));
     }//GEN-LAST:event_listarFormatoEliminarActionPerformed
 
     private void eliminaFilmes2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminaFilmes2ActionPerformed
         // TODO add your handling code here:
         
         eliminarFilmesFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_eliminaFilmes2ActionPerformed
 
     private void voltarEliminaFilmesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarEliminaFilmesActionPerformed
         // TODO add your handling code here:
         textEliminaFilmes.setModel(new OurListModel(null));
         idEliminaFilmes.setText(null);
        eliminarFilmesFrame.setVisible(false);
        eliminarFilmesFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarEliminaFilmesActionPerformed
 
     private void eliminaFilmesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminaFilmesActionPerformed
         // TODO by Lobo: h� um m�todo para actualizar o stock (pre�os e quantidade total),
     	// outro para incrementar ou decrementar a quantidade total em stock
     	// e outro para fazer set da quantidade total em stock.
     	// n�o sei qual queres aqui mas assumi que o "(Integer)eliminaSpinner.getValue()" tinha o valor da quantidade em stock (nao vi na gui).
         if(Utils.isInt(idEliminaFilmes.getText())){
              String aux=((String)textEliminaFilmes.getSelectedValue()).split(" ")[0];
 
              gestorFilmes.removeStock(idEliminaFilmes.getText(), aux);
              textEliminaFilmes.setModel(new OurListModel(gestorFilmes.verListaStocksFilmeFull(idEliminaFilmes.getText())));
     
         }
     }//GEN-LAST:event_eliminaFilmesActionPerformed
 
     private void jToggleButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton17ActionPerformed
         // TODO add your handling code here:
         listaFormatosAdicionaFilme.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
         listaGenerosAdicionaFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         //listaGenerosAdicionaFilmes1.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         adicionarFilmeFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jToggleButton17ActionPerformed
 
     private void jToggleButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton18ActionPerformed
         // TODO add your handling code here:
         listaFormatosAdicionaFilme.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
         listaGenerosAdicionaFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         //listaGenerosAdicionaFilmes1.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         adicionarFilmeFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jToggleButton18ActionPerformed
 
     private void jActualizarStockButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jActualizarStockButtonActionPerformed
        listaFormatosAdicionaFilme.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
         listaGenerosAdicionaFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         //listaGenerosAdicionaFilmes1.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaGeneros()));
         adicionarFilmeFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jActualizarStockButtonActionPerformed
 
     private void adicionarFilmeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarFilmeActionPerformed
         // TODO : Só DA PARA ESCOLHER 1GENERO POR AGORA
         String output="";
         String []generos=new String[generosVector.size()+1];
         generos[0]=(String)listaGenerosAdicionaFilmes.getSelectedItem();
         for(int i=1;i<generos.length;i++)
             generos[i]=generosVector.elementAt(i-1);
         
         
         
         //adicionaFilme(String titulo, String ano, String realizador, String ratingIMDB, String pais, String produtora, String descricao, String capa, String[] generos
         output=gestorFilmes.adicionaFilme(
         		textTituloAdicionaFilme.getText(),
                 ""+(Integer)anoAdicionaFilmeSpinner.getValue(),
                 textRealizadorAdicionaFilme.getText(),
                 ""+(Double)jSpinner2.getValue(),
                 (String)countriesList.getSelectedItem(),
                 textProdutorAdicionaFilme.getText(),
                 textDescricaoAdicionaFilme.getText(),
                 filePath,
                 generos);
         generosVector=new Vector <String>();
         outAdicionaFilme.setText(output);
     }//GEN-LAST:event_adicionarFilmeActionPerformed
 
     private void adicionarStockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarStockActionPerformed
         String output="";
         output=gestorFilmes.actualizaStock(idAdicionaStock.getText(),
                 (String)listaFormatosAdicionaFilme.getSelectedItem(),
                 ""+((Integer)qtdAdicionaStock.getValue()),
                 custoAdicionaStock.getText(),
                 custoAluguerAdicionaStock.getText());
         outAdicionaStock.setText(output);
     }//GEN-LAST:event_adicionarStockActionPerformed
 
     private void pesquisarFilmes2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarFilmes2ActionPerformed
         String[] lista=gestorFilmes.verListaGeneros();
         String[] inputLista=new String[lista.length+1];
         inputLista[0]="";
         System.arraycopy(lista, 0, inputLista, 1, lista.length);
         generosBox.setModel(new javax.swing.DefaultComboBoxModel(inputLista));
         pesquisarFilmesFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_pesquisarFilmes2ActionPerformed
 
     private void voltarPesquisarFilmesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarPesquisarFilmesActionPerformed
         
       //Reset aos campos
          textRealizadorPesquisaFilmes.setText(null);
          textIdPesquisaFilmes.setText(null);
           textTituloPesquisaFilmes.setText(null);
            textProdutorPesquisaFilmes.setText(null);
           anoBSpinner.setValue((Integer)1917);
            anoESpinner.setValue((Integer)2011);
            imdbBSpinner.setValue((Double)1.0);
            imdbESpinner.setValue((Double)10.0);
       pesquisarFilmesFrame.setVisible(false);
       pesquisarFilmesFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarPesquisarFilmesActionPerformed
 
     private void pesquisarFilmesFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_pesquisarFilmesFrameWindowClosing
        //Reset aos campos
          textRealizadorPesquisaFilmes.setText(null);
          textIdPesquisaFilmes.setText(null);
           textTituloPesquisaFilmes.setText(null);
            textProdutorPesquisaFilmes.setText(null);
 
         pesquisarFilmesFrame.setVisible(false);
       pesquisarFilmesFrame.transferFocusBackward();
     }//GEN-LAST:event_pesquisarFilmesFrameWindowClosing
 
     private void pesquisarClienteFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_pesquisarClienteFrameWindowClosing
         // TODO add your handling code here:
         pesquisarClienteFrame.setVisible(false);
       pesquisarClienteFrame.transferFocusBackward();
     }//GEN-LAST:event_pesquisarClienteFrameWindowClosing
 
     private void voltarPesquisarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarPesquisarClienteActionPerformed
         // TODO add your handling code here:
         pesquisarClienteFrame.setVisible(false);
       pesquisarClienteFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarPesquisarClienteActionPerformed
 
     private void pesquisarClientesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarClientesButtonActionPerformed
         String [] out=gestorClientes.procuraClientes(
                 
                 nomePesquisarClientes.getText(),
                 moradaPesquisarClientes.getText(),
                 emailPesquisarClientes.getText(),
                 telefonePesquisarClientes.getText());
         listaResultadosClientes.setModel(new OurListModel(out));
         
         pesquisarClienteFrame.setVisible(false);
         pesquisarClienteFrame.transferFocusBackward();
         resultadosClientes.setVisible(true);
         transferFocus();
         /*if(out!=null&&out.length!=0){
             outPesquisarClientes.setText("Foram encontrados "+out.length+"resultados\n" +
                     "-------------------------------------\n");
             for(int i=0; i<out.length;i++)
                 outPesquisarClientes.append(out[i]+"\n");
         }else{
             outPesquisarClientes.setText(
                 "Não foram encontrados resultados");
         }
         */
     }//GEN-LAST:event_pesquisarClientesButtonActionPerformed
 
     private void notificarClientesFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_notificarClientesFrameWindowClosing
         // TODO add your handling code here:
         mensagem.setText(null);
         biNotificarClientes.setText(null);
         notificarClientesFrame.setVisible(false);
       notificarClientesFrame.transferFocusBackward();
     }//GEN-LAST:event_notificarClientesFrameWindowClosing
 
     private void jToggleButton34ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton34ActionPerformed
         mensagem.setText(null);
         biNotificarClientes.setText(null);
         notificarClientesFrame.setVisible(false);
       notificarClientesFrame.transferFocusBackward();
     }//GEN-LAST:event_jToggleButton34ActionPerformed
 
     private void enviarEmailActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enviarEmailActionPerformed
         
         gestorClientes.notificarCliente(biNotificarClientes.getText(), mensagem.getText());
     }//GEN-LAST:event_enviarEmailActionPerformed
 
     private void voltarAdcionaFilmesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarAdcionaFilmesActionPerformed
         // TODO add your handling code here:
         //TODO: RESET
         generosVector= new Vector<String>();
        adicionarFilmeFrame.setVisible(false);
       adicionarFilmeFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarAdcionaFilmesActionPerformed
 
     private void jVerificarPagamentosAtrasoButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jVerificarPagamentosAtrasoButton1ActionPerformed
         pagamentosAtraso1.setModel(new OurListModel(gestorClientes.getClientesComEntregasForaDePrazo()));
     }//GEN-LAST:event_jVerificarPagamentosAtrasoButton1ActionPerformed
 
     private void actualizarListaMaquinasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualizarListaMaquinasActionPerformed
 
         listaMaquinas.setModel(new OurListModel(gestorMaquinas.verListaMaquinasATM()));
     }//GEN-LAST:event_actualizarListaMaquinasActionPerformed
 
     private void eliminarClienteFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_eliminarClienteFrameWindowClosing
         
         biEliminaClientes.setText("");
         outEliminaClientes.setText("");
         eliminarClienteFrame.setVisible(false);
         eliminarClienteFrame.transferFocusBackward();
     }//GEN-LAST:event_eliminarClienteFrameWindowClosing
 
     private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
         biEliminaClientes.setText("");
         outEliminaClientes.setText("");
         eliminarClienteFrame.setVisible(false);
         eliminarClienteFrame.transferFocusBackward();
     }//GEN-LAST:event_jButton14ActionPerformed
 
     private void dataInitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataInitActionPerformed
         JFrame date = new JFrame("Calendário");
 	jCalendarBegin = new JCalendar();
 
 	date.getContentPane().add(jCalendarBegin);
 	date.pack();
 	date.setVisible(true);
 	jCalendarBegin.addPropertyChangeListener(this);
 
     }//GEN-LAST:event_dataInitActionPerformed
 
     private void dataEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dataEndActionPerformed
         JFrame date = new JFrame("Calendário");
         jCalendarEnd = new JCalendar();
 
         date.getContentPane().add(jCalendarEnd);
         date.pack();
         date.setVisible(true);
         jCalendarEnd.addPropertyChangeListener(this);
 
     }//GEN-LAST:event_dataEndActionPerformed
 
     private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
         resetDatas();
     }//GEN-LAST:event_jButton5ActionPerformed
 
     private void consultarEstatisticasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_consultarEstatisticasActionPerformed
         // TODO add your handling code here:
         statsArea.setText("");
        
         if(clientesCheckBox.isSelected()){
             //Clientes
             statsArea.append(gestorEstatisticas.estatisticasClientes(calendarBegin, calendarEnd));
         }
          if(filmesCheckBox.isSelected()){
             //Clientes
             statsArea.append(gestorEstatisticas.estatisticasFilmes(calendarBegin, calendarEnd));
         }
          if(empregadosCheckBox.isSelected()){
             //Clientes
             statsArea.append(gestorEstatisticas.estatisticasEmpregados(calendarBegin, calendarEnd));
         }
         if(maquinasCheckBox.isSelected()){
             //Clientes
             statsArea.append(gestorEstatisticas.estatisticasMaquinas(calendarBegin, calendarEnd));
         }
         if(contabilidadeBox.isSelected()){
             //Clientes
             statsArea.append(gestorEstatisticas.getEstatisticas(calendarBegin, calendarEnd));
         }
     }//GEN-LAST:event_consultarEstatisticasActionPerformed
 
     private void notificarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notificarClientesActionPerformed
         notificarClientesFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_notificarClientesActionPerformed
 
     private void eliminarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarClientesActionPerformed
         listaEliminarClientes.setModel(new OurListModel(gestorClientes.verListaClientes()));
         eliminarClienteFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_eliminarClientesActionPerformed
 
     private void alugarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alugarActionPerformed
         String output="";
         String idMovie=((String)listaResultados.getSelectedValue()).split(" ")[0];
         if(Utils.isInt(idAlugaFilme.getText())){
         	//TODO by Lobo: pus aki um placeholder para o novo "alugaFilme", pk tem mais coisas do que as que aki tavam. E, btw, chama-se adicionaRequisicao() xD
             /*output=gestorFilmes.alugaFilme(idMovie,
                     (String) listaFormatosResultadosFilmes.getSelectedItem(),
                     jFormattedTextField4.getText(),
                     gestorUtilizadores.getIdEmpregado());*/
         	output=gestorFilmes.adicionaRequisicao("null",
                            gestorUtilizadores.getUsername(),
                            idAlugaFilme.getText(),
                            idMovie,
                            (String) listaFormatosResultadosFilmes.getSelectedItem());
         }
        
         jTextField1.setText(output);
     }//GEN-LAST:event_alugarActionPerformed
 
     private void adicionarClienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarClienteActionPerformed
         String output="";
         
         
         if(Utils.isInt(biAdicionaClientes.getText())
                 &&!biAdicionaClientes.getText().isEmpty()
                 &&!passwordAdicionaClientes2.getText().isEmpty()
                 &&(passwordAdicionaClientes2.getText().equals(passwordAdicionaClientes.getText()))){
                 
             output=gestorClientes.actualizaCliente(nomeAdicionaClientes.getText(),
                     biAdicionaClientes.getText(),
                     passwordAdicionaClientes2.getText(),
                     moradaAdicionaClientes.getText(), 
                     emailAdicionaClientes.getText(),
                     telefoneAdicionaClientes.getText());
             outputAdicionaClientes.setText(output);
         }else{
             outputAdicionaClientes.setText("Introduza o BI.");
         }
     }//GEN-LAST:event_adicionarClienteActionPerformed
 
     private void obterDadosAdicionarClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_obterDadosAdicionarClientesActionPerformed
         System.out.println(biAdicionaClientes.getText());
         String [] out=gestorClientes.procuraClienteBI(biAdicionaClientes.getText());
         
         if(out!=null){
             nomeAdicionaClientes.setText(out[1]);
             passwordAdicionaClientes.setText(out[3]);
             passwordAdicionaClientes2.setText(out[3]);
             moradaAdicionaClientes.setText(out[4]);
             emailAdicionaClientes.setText(out[5]);
             telefoneAdicionaClientes.setText(out[6]);
         }
     }//GEN-LAST:event_obterDadosAdicionarClientesActionPerformed
 
     private void eliminarClienteBIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarClienteBIActionPerformed
 
         if(Utils.isInt(biEliminaClientes.getText())){
             outEliminaClientes.setText(
                     gestorClientes.invalidaClienteBI(biEliminaClientes.getText()));
         }else{
             outEliminaClientes.setText("Nao foi possível eliminar.");
         }
     }//GEN-LAST:event_eliminarClienteBIActionPerformed
 
     private void eliminarClientesListaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarClientesListaActionPerformed
         // TODO add your handling code here:
         try{
             String idCliente=((String)listaEliminarClientes.getSelectedValue()).split(" ")[0];
             outEliminaClientes.setText(
                     gestorClientes.invalidaCliente(idCliente));
         }catch (NullPointerException e){
             outEliminaClientes.setText("Erro: Seleccione o cliente a eliminar.");
         }
     }//GEN-LAST:event_eliminarClientesListaActionPerformed
 
     private void pesquisarPorIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarPorIDActionPerformed
         String [] out;
         if(idPesquisarClientes!=null&&!idPesquisarClientes.getText().isEmpty()){
             
             if(Utils.isInt(idPesquisarClientes.getText())){
                 out=gestorClientes.procuraCliente(idPesquisarClientes.getText());
                 String[] aux=new String[1];
                 aux[0]=out[0]+" : ["+out[2]+"] "+out[1];
                 listaResultadosClientes.setModel(new OurListModel(aux));
                 pesquisarClienteFrame.setVisible(false);
                 pesquisarClienteFrame.transferFocusBackward();
                 resultadosClientes.setVisible(true);
                 transferFocus();
                 /*if(out!=null&&out.length!=0){
                     outPesquisarClientes.setText("");
                     for(int i=0; i<out.length;i++)
                         outPesquisarClientes.append(out[i]+"\n");
                 }else{
                     outPesquisarClientes.setText(
                         "Não foram encontrados resultados");
                 }
                  *
                  */
             }else{
                 outPesquisarClientes.setText("Erro: ID tem de ser um Número");
             }
         }
     }//GEN-LAST:event_pesquisarPorIDActionPerformed
 
     private void pesquisarPorBIActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarPorBIActionPerformed
         String [] out;
         if(biPesquisarClientes!=null&&!biPesquisarClientes.getText().isEmpty()){
             if(Utils.isInt(biPesquisarClientes.getText())){
                
                 out=gestorClientes.procuraClienteBI(biPesquisarClientes.getText());
                 listaResultadosClientes.setModel(new OurListModel(out));
                 pesquisarClienteFrame.setVisible(false);
                 pesquisarClienteFrame.transferFocusBackward();
                 resultadosClientes.setVisible(true);
                 transferFocus();
                 /*if(out!=null&&out.length!=0){
                     outPesquisarClientes.setText("");
                     for(int i=0; i<out.length;i++)
                         outPesquisarClientes.append(out[i]+"\n");
                 }else{
                     outPesquisarClientes.setText(
                         "Não foram encontrados resultados");
                 }
                  *
                  */
             }else{
                 outPesquisarClientes.setText("Erro: BI tem de ser um Número");
             }
         }
     }//GEN-LAST:event_pesquisarPorBIActionPerformed
 
     private void verificarRequesicoesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarRequesicoesActionPerformed
         pagamentosAtraso.setModel(new OurListModel(gestorClientes.getClientesComEntregasPorFazer()));
     }//GEN-LAST:event_verificarRequesicoesActionPerformed
 
     private void verificarRequesicoes1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_verificarRequesicoes1ActionPerformed
         pagamentosAtraso1.setModel(new OurListModel(gestorClientes.getClientesComEntregasPorFazer()));
     }//GEN-LAST:event_verificarRequesicoes1ActionPerformed
 
     private void eliminaGeneroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminaGeneroActionPerformed
         String nomeGenero=((String)listaGeneros.getSelectedValue());
         String output=gestorFilmes.removeGeneroNome(nomeGenero);
         outGenero.setText(output);
         if(!output.equals(Consts.GENERO_EM_USO))
             listaGeneros.setModel(new OurListModel(gestorFilmes.verListaGeneros()));
 
     }//GEN-LAST:event_eliminaGeneroActionPerformed
 
     private void eliminarFormatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarFormatoActionPerformed
         String nomeFormato=((String)listaFormato.getSelectedValue());
         String output=gestorFilmes.removeFormatoNome(nomeFormato);
         outFormato.setText(output);
         if(!output.equals(Consts.FORMATO_EM_USO))
             listaFormato.setModel(new OurListModel(gestorFilmes.verListaFormatos()));
 
     }//GEN-LAST:event_eliminarFormatoActionPerformed
 
     private void adicionarFormatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarFormatoActionPerformed
         String output=gestorFilmes.adicionaFormato(textFormato.getText());
         textFormato.setText(null);
         outFormato.setText(output);
         
         listaFormato.setModel(new OurListModel(gestorFilmes.verListaFormatos()));
     }//GEN-LAST:event_adicionarFormatoActionPerformed
 
     private void voltarFormatosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarFormatosActionPerformed
         textFormato.setText("");
         outFormato.setText("");
         formatosFrame.setVisible(false);
         formatosFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarFormatosActionPerformed
 
     private void formatosFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formatosFrameWindowClosing
         textFormato.setText("");
         outFormato.setText("");
         formatosFrame.setVisible(false);
         formatosFrame.transferFocusBackward();
     }//GEN-LAST:event_formatosFrameWindowClosing
 
     private void jFormatosFrameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormatosFrameButtonActionPerformed
         listaFormato.setModel(new OurListModel(gestorFilmes.verListaFormatos()));
         formatosFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jFormatosFrameButtonActionPerformed
 
     private void jFormatosFrameButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFormatosFrameButton1ActionPerformed
         listaFormato.setModel(new OurListModel(gestorFilmes.verListaFormatos()));
         formatosFrame.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jFormatosFrameButton1ActionPerformed
 
     private void obterDadosEmpregadoActualizacaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_obterDadosEmpregadoActualizacaoActionPerformed
         // TODO add your handling code here:
         String []out;
         if(Utils.isInt(biEmpregados.getText())){
             out=gestorEmpregados.procuraEmpregadoBI(biEmpregados.getText());
             if(out!=null&&out.length>0){
                 if(out[1].equals("1")){
                     adminRadio.doClick();
                 }else{
                     opRadio.doClick();
                 }
                 salarioEmpregados.setText(out[2]);
                 nomeEmpregados.setText(out[3]);
                 passwordEmpregados.setText(out[5]);
                 passwordEmpregados2.setText(out[5]);
                 moradaEmpregados.setText(out[6]);
                 emailEmpregados.setText(out[7]);
                 telefoneEmpregados.setText(out[8]);
             }
 
         }
     }//GEN-LAST:event_obterDadosEmpregadoActualizacaoActionPerformed
 
     private void actualzarListaEmpregadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actualzarListaEmpregadosActionPerformed
 
         listaEmpregados.setModel(new OurListModel(gestorEmpregados.verListaEmpregados()));
 }//GEN-LAST:event_actualzarListaEmpregadosActionPerformed
 
     private void jAdicionarEmpregadoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jAdicionarEmpregadoButtonActionPerformed
         String isAdmin="0";
         if(!nomeEmpregados.getText().isEmpty()
                 &&Utils.isInt(biEmpregados.getText())
                 &&Utils.isDouble(salarioEmpregados.getText())
                 &&Utils.isInt(telefoneEmpregados.getText())
                 &&(passwordEmpregados.getText().equals(passwordEmpregados2.getText()))//TODO : Deprecated
                 ){
 
             if(adminRadio.isSelected())
                 isAdmin="1";
             //String is_admin, String salario, String nome, String bi, String password, String morada, String email, String telefone
             String out=gestorEmpregados.actualizaEmpregado(isAdmin,
                     salarioEmpregados.getText(),
                     nomeEmpregados.getText(),
                     biEmpregados.getText(),
                     passwordEmpregados.getText(),
                     moradaEmpregados.getText(),
                     emailEmpregados.getText(),
                     telefoneEmpregados.getText());
             outEmpregados.setText(out);
         }
        outEmpregados.setText("Não foi possivel adicionar o empregado.");
 }//GEN-LAST:event_jAdicionarEmpregadoButtonActionPerformed
 
     private void jDespedirEmpregadoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jDespedirEmpregadoButtonActionPerformed
         String output="";
         String idEmpregado=((String)listaEmpregados.getSelectedValue()).split(" ")[0];
         output=gestorEmpregados.invalidaEmpregado(idEmpregado);
         //TODO: Output message
         outEmpregados.setText(output);
 }//GEN-LAST:event_jDespedirEmpregadoButtonActionPerformed
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         try{
             DBHandler.close();
         }catch(Exception e){}
         
     }//GEN-LAST:event_formWindowClosing
 
     private void voltarResultadosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarResultadosActionPerformed
 
         resultadosFrame.setVisible(false);
         resultadosFrame.transferFocusBackward();
     }//GEN-LAST:event_voltarResultadosActionPerformed
 
     private void pesquisarTodosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pesquisarTodosActionPerformed
          listaResultados.setModel(new OurListModel(gestorFilmes.verListaFilmesOrdTituloPlusInvalidos()) );
         //Reset aos campos
          textRealizadorPesquisaFilmes.setText(null);
          textIdPesquisaFilmes.setText(null);
           textTituloPesquisaFilmes.setText(null);
            textProdutorPesquisaFilmes.setText(null);
           anoBSpinner.setValue((Integer)1917);
            anoESpinner.setValue((Integer)2011);
            imdbBSpinner.setValue((Double)1.0);
            imdbESpinner.setValue((Double)10.0);
         pesquisarFilmesFrame.setVisible(false);
       pesquisarFilmesFrame.transferFocusBackward();
       listaFormatosResultadosFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
 
       resultadosFrame.setVisible(true);
       transferFocus();
     }//GEN-LAST:event_pesquisarTodosActionPerformed
 
     private void procurarIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_procurarIDActionPerformed
         if(Utils.isInt(textIdPesquisaFilmes.getText())){
             //{"ID_FIL", "TITULO", "ANO", "REALIZADOR", "RANKIMDB", "PAIS", "PRODUTORA", "DESCRICAO", "CAPA", "VALIDO"};}
             String[] out= gestorFilmes.getFilme(textIdPesquisaFilmes.getText());
             String[] lista=new String[1];
             lista[0]=out[0]+" "+out[1];
             listaResultados.setModel(new OurListModel(lista) );
             //Reset aos campos
          textRealizadorPesquisaFilmes.setText(null);
          textIdPesquisaFilmes.setText(null);
           textTituloPesquisaFilmes.setText(null);
            textProdutorPesquisaFilmes.setText(null);
            anoBSpinner.setValue((Integer)1917);
            anoESpinner.setValue((Integer)2011);
            imdbBSpinner.setValue((Double)1.0);
            imdbESpinner.setValue((Double)10.0);
 
             pesquisarFilmesFrame.setVisible(false);
           pesquisarFilmesFrame.transferFocusBackward();
           listaFormatosResultadosFilmes.setModel(new javax.swing.DefaultComboBoxModel(gestorFilmes.verListaFormatos()));
 
           resultadosFrame.setVisible(true);
           transferFocus();
       }
     }//GEN-LAST:event_procurarIDActionPerformed
 
     private void entregaFilmeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entregaFilmeActionPerformed
         if(((String)listaRequisicoes.getSelectedValue())==null){
             gestorFilmes.entregaRequisicao(((String)listaRequisicoes.getSelectedValue()).split(" ")[0]);
             pagamentosAtraso.setModel(new OurListModel(null));
             listaRequisicoes.setModel(new OurListModel(null));
         }
     }//GEN-LAST:event_entregaFilmeActionPerformed
 
     private void entregaFilme1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_entregaFilme1ActionPerformed
         gestorFilmes.entregaRequisicao(((String)listaRequisicoes1.getSelectedValue()).split(" ")[0]);
         pagamentosAtraso1.setModel(new OurListModel(null));
         listaRequisicoes1.setModel(new OurListModel(null));
     }//GEN-LAST:event_entregaFilme1ActionPerformed
 
     private void eliminarStockFilmeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarStockFilmeActionPerformed
         // TODO add your handling code here:
        String idMovie=((String)listaResultados.getSelectedValue()).split(" ")[0];
        idEliminaFilmes.setText(idMovie);
        textEliminaFilmes.setModel(new OurListModel(gestorFilmes.verListaStocksFilmeFull(idMovie)));
        resultadosFrame.setVisible(false);
        resultadosFrame.transferFocusBackward();
        eliminarFilmesFrame.setVisible(true);
        transferFocus();
     }//GEN-LAST:event_eliminarStockFilmeActionPerformed
 
     private void adicionarStockFilmeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarStockFilmeActionPerformed
         // TODO add your handling code here:
          // TODO add your handling code here:
        String idMovie=((String)listaResultados.getSelectedValue()).split(" ")[0];
        idAdicionaStock.setText(idMovie);
        resultadosFrame.setVisible(false);
        resultadosFrame.transferFocusBackward();
        adicionarFilmeFrame.setVisible(true);
        transferFocus();
     }//GEN-LAST:event_adicionarStockFilmeActionPerformed
 
     private void adicionarFilmeFrameWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_adicionarFilmeFrameWindowClosing
       generosVector= new Vector<String>();
       adicionarFilmeFrame.setVisible(false);
       adicionarFilmeFrame.transferFocusBackward();
     }//GEN-LAST:event_adicionarFilmeFrameWindowClosing
 
     private void generoExtraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generoExtraActionPerformed
         //generosVector.add((String)listaGenerosAdicionaFilmes1.getSelectedItem());
     }//GEN-LAST:event_generoExtraActionPerformed
 
     private void voltarResultadosClientesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltarResultadosClientesActionPerformed
 
         resultadosClientes.setVisible(false);
         resultadosClientes.transferFocusBackward();
     }//GEN-LAST:event_voltarResultadosClientesActionPerformed
 
     private void resultadosClientesWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_resultadosClientesWindowClosing
         resultadosClientes.setVisible(false);
         resultadosClientes.transferFocusBackward();
     }//GEN-LAST:event_resultadosClientesWindowClosing
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         listaResultadosClientes.setModel(new OurListModel(gestorClientes.verListaClientes()));
         pesquisarClienteFrame.setVisible(false);
         pesquisarClienteFrame.transferFocusBackward();
         resultadosClientes.setVisible(true);
         transferFocus();
     }//GEN-LAST:event_jButton1ActionPerformed
 
    
     //OUR GUI VARS
     private javax.swing.ButtonGroup bgroup;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton actualizarListaMaquinas;
     private javax.swing.JButton actualzarListaEmpregados;
     private javax.swing.JButton adicionaGenero;
     private javax.swing.JToggleButton adicionaGenero2;
     private javax.swing.JButton adicionarCliente;
     private javax.swing.JFrame adicionarClienteFrame;
     private javax.swing.JToggleButton adicionarClientes;
     private javax.swing.JButton adicionarFilme;
     private javax.swing.JFrame adicionarFilmeFrame;
     private javax.swing.JButton adicionarFormato;
     private javax.swing.JToggleButton adicionarStock;
     private javax.swing.JButton adicionarStockFilme;
     private javax.swing.JRadioButton adminRadio;
     private javax.swing.JButton alugar;
     private javax.swing.JSpinner anoAdicionaFilmeSpinner;
     private javax.swing.JSpinner anoBSpinner;
     private javax.swing.JSpinner anoESpinner;
     private javax.swing.JTextField anoResultadosFilme;
     private javax.swing.JTextField biAdicionaClientes;
     private javax.swing.JTextField biEliminaClientes;
     private javax.swing.JTextField biEmpregados;
     private javax.swing.JTextField biNotificarClientes;
     private javax.swing.JTextField biPesquisarClientes;
     private javax.swing.JTextField biResultadosClientes;
     private javax.swing.JCheckBox clientesCheckBox;
     private javax.swing.JButton consultarEstatisticas;
     private javax.swing.JCheckBox contabilidadeBox;
     private javax.swing.JComboBox countriesList;
     private javax.swing.JComboBox countriesList1;
     private javax.swing.JTextField custoAdicionaStock;
     private javax.swing.JTextField custoAluguerAdicionaStock;
     private javax.swing.JButton dataEnd;
     private javax.swing.JButton dataInit;
     private javax.swing.JTextField dateBegin;
     private javax.swing.JTextField dateEnd;
     private javax.swing.JToggleButton eliminaFilmes;
     private javax.swing.JToggleButton eliminaFilmes2;
     private javax.swing.JButton eliminaGenero;
     private javax.swing.JButton eliminarClienteBI;
     private javax.swing.JFrame eliminarClienteFrame;
     private javax.swing.JButton eliminarClientes;
     private javax.swing.JButton eliminarClientesLista;
     private javax.swing.JFrame eliminarFilmesFrame;
     private javax.swing.JButton eliminarFormato;
     private javax.swing.JButton eliminarStockFilme;
     private javax.swing.JTextField emailAdicionaClientes;
     private javax.swing.JTextField emailEmpregados;
     private javax.swing.JTextField emailPesquisarClientes;
     private javax.swing.JTextField emailResultadosClientes;
     private javax.swing.JCheckBox empregadosCheckBox;
     private javax.swing.JButton entregaFilme;
     private javax.swing.JButton entregaFilme1;
     private javax.swing.JButton enviarEmail;
     private javax.swing.JFrame ficheirosFrame;
     private javax.swing.JCheckBox filmesCheckBox;
     private javax.swing.JFrame formatosFrame;
     private javax.swing.JButton generoExtra;
     private javax.swing.JComboBox generosBox;
     private javax.swing.JFrame generosFrame;
     private javax.swing.JTextField idAdicionaStock;
     private javax.swing.JTextField idAlugaFilme;
     private javax.swing.JTextField idEliminaFilmes;
     private javax.swing.JTextField idPesquisarClientes;
     private javax.swing.JSpinner imdbBSpinner;
     private javax.swing.JSpinner imdbESpinner;
     private javax.swing.JTextField imdbResultadosFilme;
     private javax.swing.JPanel jATMManagerPanel;
     private javax.swing.JToggleButton jActualizarStockButton;
     private javax.swing.JButton jAdicionarATMButton;
     private javax.swing.JPanel jAdicionarClientePanel;
     private javax.swing.JToggleButton jAdicionarClientesButton;
     private javax.swing.JToggleButton jAdicionarEmpregadoButton;
     private javax.swing.JPanel jAdicionarFilmePanel;
     private javax.swing.JToggleButton jAdicionarFilmesToggleButton;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton14;
     private javax.swing.JButton jButton5;
     private javax.swing.JPanel jClientesManagerPanel;
     private javax.swing.JPanel jClientesManagerPanel1;
     private javax.swing.JToggleButton jDespedirEmpregadoButton;
     private javax.swing.JPanel jEliminarClientePanel;
     private javax.swing.JButton jEliminarClientesButton;
     private javax.swing.JToggleButton jEliminarFilmeButton;
     private javax.swing.JPanel jEliminarFilmePanel;
     private javax.swing.JPanel jEmpregadosManagerPanel;
     private javax.swing.JToggleButton jEscolherFicheiroButton;
     private javax.swing.JPanel jEstatisticasPanel;
     private javax.swing.JFileChooser jFileChooser1;
     private javax.swing.JPanel jFilmesManagerPanel;
     private javax.swing.JPanel jFilmesManagerPanel1;
     private javax.swing.JButton jFormatosFrameButton;
     private javax.swing.JButton jFormatosFrameButton1;
     private javax.swing.JToggleButton jGeneroButton;
     private javax.swing.JPanel jGenerosPanel;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel100;
     private javax.swing.JLabel jLabel101;
     private javax.swing.JLabel jLabel102;
     private javax.swing.JLabel jLabel103;
     private javax.swing.JLabel jLabel104;
     private javax.swing.JLabel jLabel105;
     private javax.swing.JLabel jLabel106;
     private javax.swing.JLabel jLabel107;
     private javax.swing.JLabel jLabel108;
     private javax.swing.JLabel jLabel109;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel23;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel28;
     private javax.swing.JLabel jLabel29;
     private javax.swing.JLabel jLabel30;
     private javax.swing.JLabel jLabel31;
     private javax.swing.JLabel jLabel32;
     private javax.swing.JLabel jLabel33;
     private javax.swing.JLabel jLabel34;
     private javax.swing.JLabel jLabel36;
     private javax.swing.JLabel jLabel37;
     private javax.swing.JLabel jLabel38;
     private javax.swing.JLabel jLabel39;
     private javax.swing.JLabel jLabel40;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel70;
     private javax.swing.JLabel jLabel76;
     private javax.swing.JLabel jLabel78;
     private javax.swing.JLabel jLabel94;
     private javax.swing.JLabel jLabel98;
     private javax.swing.JLabel jLabel99;
     private javax.swing.JList jList5;
     private javax.swing.JButton jLoginButton;
     private javax.swing.JPanel jLoginPanel;
     private javax.swing.JPanel jMenuAdministradorPanel;
     private javax.swing.JPanel jMenuOperatorPanel;
     private javax.swing.JPanel jNotificarClientePanel;
     private javax.swing.JButton jNotificarClientesButton;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPasswordField jPasswordField;
     private javax.swing.JPanel jPesqisaFilmesPanel;
     private javax.swing.JToggleButton jPesquisarButton;
     private javax.swing.JPanel jPesquisarClientePanel;
     private javax.swing.JToggleButton jPesquisarClientesButton;
     private javax.swing.JPanel jResultadosFilmePanel;
     private javax.swing.JButton jSairButton;
     private javax.swing.JButton jSairButton2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane10;
     private javax.swing.JScrollPane jScrollPane11;
     private javax.swing.JScrollPane jScrollPane12;
     private javax.swing.JScrollPane jScrollPane13;
     private javax.swing.JScrollPane jScrollPane14;
     private javax.swing.JScrollPane jScrollPane15;
     private javax.swing.JScrollPane jScrollPane16;
     private javax.swing.JScrollPane jScrollPane17;
     private javax.swing.JScrollPane jScrollPane18;
     private javax.swing.JScrollPane jScrollPane19;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane20;
     private javax.swing.JScrollPane jScrollPane21;
     private javax.swing.JScrollPane jScrollPane22;
     private javax.swing.JScrollPane jScrollPane23;
     private javax.swing.JScrollPane jScrollPane24;
     private javax.swing.JScrollPane jScrollPane25;
     private javax.swing.JScrollPane jScrollPane26;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JScrollPane jScrollPane5;
     private javax.swing.JScrollPane jScrollPane6;
     private javax.swing.JScrollPane jScrollPane7;
     private javax.swing.JScrollPane jScrollPane8;
     private javax.swing.JScrollPane jScrollPane9;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JSpinner jSpinner2;
     private javax.swing.JSpinner jSpinner6;
     private javax.swing.JTabbedPane jTabbedPane2;
     private javax.swing.JTabbedPane jTabbedPane3;
     private javax.swing.JTextArea jTextArea14;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JToggleButton jToggleButton17;
     private javax.swing.JToggleButton jToggleButton18;
     private javax.swing.JToggleButton jToggleButton34;
     private javax.swing.JTextField jUsernameField;
     private javax.swing.JButton jVenderATMButton;
     private javax.swing.JButton jVerificarPagamentosAtrasoButton;
     private javax.swing.JButton jVerificarPagamentosAtrasoButton1;
     private javax.swing.JButton jVoltarACFButton;
     private javax.swing.JList listaEliminarClientes;
     private javax.swing.JList listaEmpregados;
     private javax.swing.JList listaFormato;
     private javax.swing.JComboBox listaFormatosAdicionaFilme;
     private javax.swing.JComboBox listaFormatosResultadosFilmes;
     private javax.swing.JList listaGeneros;
     private javax.swing.JComboBox listaGenerosAdicionaFilmes;
     private javax.swing.JList listaMaquinas;
     private javax.swing.JList listaRequisicoes;
     private javax.swing.JList listaRequisicoes1;
     private javax.swing.JList listaResultados;
     private javax.swing.JList listaResultadosClientes;
     private javax.swing.JButton listarFormatoEliminar;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JCheckBox maquinasCheckBox;
     private javax.swing.JTextArea mensagem;
     private javax.swing.JTextField moradaAdicionaClientes;
     private javax.swing.JTextField moradaEmpregados;
     private javax.swing.JTextField moradaPesquisarClientes;
     private javax.swing.JTextField moradaResultadosClientes;
     private javax.swing.JTextField nomeAdicionaClientes;
     private javax.swing.JTextField nomeEmpregados;
     private javax.swing.JTextField nomePesquisarClientes;
     private javax.swing.JTextField nomeResultadosClientes;
     private javax.swing.JButton notificarClientes;
     private javax.swing.JFrame notificarClientesFrame;
     private javax.swing.JButton obterDadosAdicionarClientes;
     private javax.swing.JButton obterDadosEmpregadoActualizacao;
     private javax.swing.JRadioButton opRadio;
     private javax.swing.JTextArea outAdicionaFilme;
     private javax.swing.JTextArea outAdicionaStock;
     private javax.swing.JTextArea outEliminaClientes;
     private javax.swing.JTextArea outEmpregados;
     private javax.swing.JTextArea outFormato;
     private javax.swing.JTextArea outGenero;
     private javax.swing.JTextArea outMaquinas;
     private javax.swing.JTextArea outPesquisarClientes;
     private javax.swing.JTextArea outputAdicionaClientes;
     private javax.swing.JList pagamentosAtraso;
     private javax.swing.JList pagamentosAtraso1;
     private javax.swing.JTextField paisResultadosFilme;
     private javax.swing.JPasswordField passwordAdicionaClientes;
     private javax.swing.JPasswordField passwordAdicionaClientes2;
     private javax.swing.JPasswordField passwordEmpregados;
     private javax.swing.JPasswordField passwordEmpregados2;
     private javax.swing.JFrame pesquisarClienteFrame;
     private javax.swing.JToggleButton pesquisarClientes;
     private javax.swing.JToggleButton pesquisarClientesButton;
     private javax.swing.JToggleButton pesquisarFilme;
     private javax.swing.JToggleButton pesquisarFilmes2;
     private javax.swing.JFrame pesquisarFilmesFrame;
     private javax.swing.JButton pesquisarPorBI;
     private javax.swing.JButton pesquisarPorID;
     private javax.swing.JButton pesquisarTodos;
     private javax.swing.JButton procurarID;
     private javax.swing.JTextField produtorResultadosFilme;
     private javax.swing.JSpinner qtdAdicionaStock;
     private javax.swing.JTextField realizadorResultadosFilme;
     private javax.swing.JFrame resultadosClientes;
     private javax.swing.JPanel resultadosClientesPanel;
     private javax.swing.JFrame resultadosFrame;
     private javax.swing.JTextField salarioEmpregados;
     private javax.swing.JTextArea statsArea;
     private javax.swing.JTextField telefoneAdicionaClientes;
     private javax.swing.JTextField telefoneEmpregados;
     private javax.swing.JTextField telefonePesquisarClientes;
     private javax.swing.JTextField telefoneResultadosClientes;
     private javax.swing.JTextArea textDescricaoAdicionaFilme;
     private javax.swing.JList textEliminaFilmes;
     private javax.swing.JTextField textFormato;
     private javax.swing.JTextField textGenero;
     private javax.swing.JTextField textIdPesquisaFilmes;
     private javax.swing.JTextField textProdutorAdicionaFilme;
     private javax.swing.JTextField textProdutorPesquisaFilmes;
     private javax.swing.JTextField textRealizadorAdicionaFilme;
     private javax.swing.JTextField textRealizadorPesquisaFilmes;
     private javax.swing.JTextField textTituloAdicionaFilme;
     private javax.swing.JTextField textTituloPesquisaFilmes;
     private javax.swing.JTextField tituloResultadosFilme;
     private javax.swing.JButton verificarRequesicoes;
     private javax.swing.JButton verificarRequesicoes1;
     private javax.swing.JToggleButton voltarAdcionaFilmes;
     private javax.swing.JToggleButton voltarEliminaFilmes;
     private javax.swing.JButton voltarFormatos;
     private javax.swing.JButton voltarGeneros;
     private javax.swing.JToggleButton voltarPesquisarCliente;
     private javax.swing.JToggleButton voltarPesquisarFilmes;
     private javax.swing.JButton voltarResultados;
     private javax.swing.JButton voltarResultadosClientes;
     // End of variables declaration//GEN-END:variables
 
 }
