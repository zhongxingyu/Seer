 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ospedale;
  
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 /**
  *
  * @author alex
  */
 public class ModelJTable extends JFrame{
     private String data_table,data_table2,ora_table,ora_table2,id_prenot,reparto,reparto2,info,prior,paziente;
     private Paziente paz=null;
     private Amministratore amm=null;
     private DefaultTableModel model,model2;
     private Database db=new Database();
     private JTable table, table2;
     final int identificativo;
     private JRadioButton priorita;
     private int chiamante;
     
     public ModelJTable(String rep,Paziente p){ ///effettuare prenotazioni (classe Paziente)
         super();
         identificativo=1;
     data_table=null;
     ora_table=null;
     reparto=rep;
     paz=p;
     model = new DefaultTableModel();
     model.addColumn("DATA (aaaa-mm-gg)");
     model.addColumn("ORARIO");
     
     popolaTable("select data,ora from visite where reparto='"+reparto+"' and priorita='0' order by data,ora asc;",model.getColumnCount());
     
     table = new JTable(model){
             @Override
         public boolean isCellEditable(int rowIndex, int colIndex) {
             return false; //Disallow the editing of any cell
         }
     };
         generaGrafica("ELENCO DATE DISPONIBILI",paz.getPaziente(),identificativo);
         this.setTitle("Elenco date disponibili "+reparto+" - Paziente");
     }
     
     
     public ModelJTable(Paziente p){         //visualizza storico prenotazioni (Paziente)
         identificativo=2;
         paz=p;
         model = new DefaultTableModel();
         model.addColumn("ID PRENOTAZIONE");
         model.addColumn("REPARTO");
         model.addColumn("DATA (aaaa-mm-gg)");
         model.addColumn("ORARIO");
         
        popolaTable("select idprenotazione,reparto,data,ora from prenotazioni where idpaziente='"+paz.getCod_Fisc()+"' order by idprenotazione asc;",model.getColumnCount());
         
         table = new JTable(model){
             @Override
         public boolean isCellEditable(int rowIndex, int colIndex) {
             return false; //Disallow the editing of any cell
         }
     };
        generaGrafica("ELENCO PRENOTAZIONI",paz.getPaziente(),identificativo); 
        this.setTitle("Elenco prenotazioni effettuate - Paziente");
     }
     
     public ModelJTable(Amministratore a,int chiam, String inform){
         identificativo=3;
         chiamante=chiam;
         info=inform;
         amm=a;
         model = new DefaultTableModel();
         model.addColumn("ID PRENOTAZIONE");
         model.addColumn("REPARTO");
         model.addColumn("DATA (aaaa-mm-gg)");
         model.addColumn("ORARIO");
         model.addColumn("PRIORITÀ");
         model.addColumn("ID PAZIENTE");
         
         switch(chiamante){
             case 1:
             popolaTable("select * from prenotazioni order by idprenotazione asc;",model.getColumnCount());
             break;
             case 2:
             popolaTable("select * from prenotazioni where reparto='"+info+"' order by idprenotazione asc;",model.getColumnCount());   
             break;
             case 3:
             popolaTable("select * from prenotazioni where idpaziente='"+info+"' order by idprenotazione asc;",model.getColumnCount());     
             break;
             case 4:
             popolaTable("select * from prenotazioni where idprenotazione='"+info+"';",model.getColumnCount());
             break;
         }
         table = new JTable(model){
             @Override
             public boolean isCellEditable(int rowIndex, int colIndex) {
                 return false; //Disallow the editing of any cell
             }
         };
            
                 
      
             generaGrafica("ELENCO PRENOTAZIONI",a.getAmministratore(),identificativo);
            
         
     }
     
     public ModelJTable(Amministratore a){
         identificativo=4;
         amm=a;
         model = new DefaultTableModel();
         model.addColumn("REPARTO");
         model.addColumn("DATA (aaaa-mm-gg)");
         model.addColumn("ORARIO");
         model.addColumn("PRIORITÀ");
         
         popolaTable("select * from visite;",model.getColumnCount());
         table = new JTable(model){
             @Override
         public boolean isCellEditable(int rowIndex, int colIndex) {
             return false; //Disallow the editing of any cell
         }
     };  
   
         generaGrafica("GESTIONE DELLE VISITE",amm.getAmministratore(),identificativo);
         
         
     }
     
     public ModelJTable(Amministratore a,int i){
         identificativo=i; // (=5)
         amm=a;
         model = new DefaultTableModel();
         model.addColumn("ID PRENOTAZIONE");
         model.addColumn("REPARTO");
         model.addColumn("DATA (aaaa-mm-gg)");
         model.addColumn("ORARIO");
         model.addColumn("PRIORITÀ");
         model.addColumn("ID PAZIENTE");
         
         popolaTable("select * from prenotazioni where priorita='1' order by reparto,data,ora;",model.getColumnCount());
         table = new JTable(model){
             @Override
         public boolean isCellEditable(int rowIndex, int colIndex) {
             return false; //Disallow the editing of any cell
         }
     };           
          generaGrafica("GESTIONE DELLE PRENOTAZIONI",amm.getAmministratore(),identificativo);       
     }
     
     
     private void generaGrafica(String titolo,String utente,final int identificativo){
         JLabel informazioni=new JLabel(titolo);
         JLabel user =new JLabel(utente);
         JButton esci=new JButton("ESCI");
         JButton indietro=new JButton("INDIETRO");
         priorita=new JRadioButton("Richiedi Priorità");
         JButton referto=new JButton("REFERTO");
         JButton conferma=new JButton("CONFERMA");
         model2=new DefaultTableModel();
         table.setColumnSelectionAllowed(false);
         table.setRowSelectionAllowed(true);
         
     
         Container container = getContentPane();
         
         JPanel inputPanel1 = new JPanel();
         JPanel inputPanel2 = new JPanel();
         inputPanel1.add(informazioni);
         inputPanel2.add(user);
         inputPanel2.add(esci);
         inputPanel2.add(indietro);
         inputPanel2.add(priorita);
         inputPanel2.add(referto);
         inputPanel2.add(conferma); 
         
         switch(identificativo){
             case 1:
                 referto.setEnabled(false); 
                 container.add(inputPanel1,BorderLayout.NORTH);
                 container.add(new JScrollPane(table), BorderLayout.CENTER);
                 break;
             case 2:
             case 3:
                 priorita.setEnabled(false);
                 conferma.setEnabled(false);
                 this.setTitle("Elenco delle prenotazioni");
                 container.add(inputPanel1,BorderLayout.NORTH);
                 container.add(new JScrollPane(table), BorderLayout.CENTER);
                 break;
             case 4:
                 priorita.setEnabled(false);
                
                 conferma.setText("AGGIUNGI");
                 referto.setText("RIMUOVI");
                 model2=new DefaultTableModel();
                 model2.addColumn("REPARTO");
                 model2.addColumn("DATA (aaaa-mm-gg)");
                 model2.addColumn("ORARIO");
                 model2.addColumn("PRIORITÀ");
                 model2.addRow(new String[4]);
                 table2 = new JTable(model2){
           
             @Override
                 public boolean isCellEditable(int rowIndex, int colIndex) {
                     return true; //Allow the editing of any cell
                 }
                 };
                 
                 String[] values1 = new String[]{"ortopedia", "pediatria"};
                 String[] values2 = new String[]{"0","1"};
                 // Set the combobox editor on the columns
                 int vColIndex = 0;
                 TableColumn col = table2.getColumnModel().getColumn(vColIndex);
                 col.setCellEditor(new MyComboBoxEditor(values1));
                 vColIndex=3;
                 col = table2.getColumnModel().getColumn(vColIndex);
                 col.setCellEditor(new MyComboBoxEditor(values2));
                 JScrollPane J2=new JScrollPane(table2);
                 J2.setPreferredSize(new Dimension(620,40));
                 container.add(J2, BorderLayout.CENTER);
                 this.setTitle("Gestione disponibilità delle visite");               
                 container.add(new JScrollPane(table), BorderLayout.NORTH);
                 break;  
             case 5:
                 priorita.setEnabled(false);
                 referto.setText("RIFIUTA");
                  model2=new DefaultTableModel();
                 model2.addColumn("REPARTO");
                 model2.addColumn("DATA (aaaa-mm-gg)");
                 model2.addColumn("ORARIO");
                 model2.addColumn("PRIORITÀ");
                 table2 = new JTable(model2){
             @Override
                 public boolean isCellEditable(int rowIndex, int colIndex) {
                     return false; //Disallow the editing of any cell
                 }
                 };
                  table.setColumnSelectionAllowed(false);
                 table.setRowSelectionAllowed(true);
                 container.add(new JScrollPane(table2), BorderLayout.CENTER);
                 this.setTitle("Gestione richieste di priorità");
                 JScrollPane J1=new JScrollPane(table);
                 J1.setPreferredSize(new Dimension(620,270));
                 container.add(J1, BorderLayout.NORTH);
                 
                 table2.addMouseListener(new MouseAdapter() {
         @Override
          public void mouseClicked(MouseEvent me) {
             switch (identificativo){
                 case 5:
                    selezionaVisita(me,5); break; 
             }
         }
     });
                 
                 break;
                  
     }
         container.add(inputPanel2,BorderLayout.AFTER_LAST_LINE);      
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         setSize(850, 540);
         setVisible(true);
         setResizable(false);
                 
         esci.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 esciActionPerformed(evt);
             }
         });
    indietro.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 indietroActionPerformed(evt);
             }
         });
    conferma.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 try {
                     confermaActionPerformed(evt);
                 } catch (SQLException ex) {
                     Logger.getLogger(ModelJTable.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
    
    referto.addActionListener(new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refertoActionPerformed(evt);
             }
         });
     table.addMouseListener(new MouseAdapter() {
             @Override
          public void mouseClicked(MouseEvent me) {
             switch (identificativo){
                 case 1:
                 selezionaData(me); break;
                 case 2:
                 case 3:
                 selezionaPrenotazione(me,2,null); break;
                 case 4:
                 selezionaVisita(me,1); break;
                 case 5:
                    selezionaPrenotazione(me,5,model2); break; 
             }
         }
     });
     
     
             
     
     }
     
     private void popolaTable(String query,int numColonne){
         String SQL=query;
         
         model.setRowCount(0); //svuota la tabella
         try {
             db.connetti();
             try (ResultSet rs = db.eseguiQuery(SQL)) {
                 while(rs.next()){    
                     switch (numColonne){
                         case 2:
                             //System.out.println(rs.getString("data")+"  -  "+rs.getString("ora"));
                             String[] stringa2={rs.getString(1),rs.getString(2)};
                             model.addRow(stringa2);
                             break;
                         case 4:
                             String[] stringa4={rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4)};
                             model.addRow(stringa4);
                             break;
                         case 5:
                            String[] stringa5={rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5)};
                            model.addRow(stringa5);
                            break;
                         case 6: 
                            String[] stringa6={rs.getString(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6)};
                            model.addRow(stringa6);
                            break;
                     }
                 }
             }
             db.disconnetti();
         }catch(SQLException e){ System.out.println(e); }
     }
     
     private void popolaTable(String query){
         String SQL=query;
         try {
             model2.setRowCount(0); //svuota la tabella
             db.connetti();
             try (ResultSet rs2 = db.eseguiQuery(SQL)) {
                 while(rs2.next()){ 
                     String[] stringa={rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4)};
                             model2.addRow(stringa);
                 }
             }
             db.disconnetti();
     }catch(  SQLException | NullPointerException e){ System.out.println(e); }
     
     }
     
     private void esciActionPerformed(java.awt.event.ActionEvent evt){
       this.setVisible(false);
       Login l=new Login();
       l.setVisible(true);
   }
     
     private void indietroActionPerformed(java.awt.event.ActionEvent evt){
         switch(identificativo){
             case 1:
             case 2:        
                 paz.setVisible(true);
                 this.setVisible(false);
                 break;
             case 3:
             case 4:
             case 5:
                 amm.setVisible(true);
                 this.setVisible(false);
                 break;
            
                
         }
   }
     
     private void refertoActionPerformed(java.awt.event.ActionEvent evt){
         String SQL;
         ResultSet rs;
         switch(identificativo){
             case 2:
                 if (id_prenot==null){
                     JOptionPane.showMessageDialog(null,"Effettua una scelta prima di proseguire");
                 }else{
                     
                 Visualizza_Referto vr= new Visualizza_Referto(id_prenot,false);
                 vr.setVisible(true);
           
             
            }
             break;
             case 3:
                 if (id_prenot==null){
                     JOptionPane.showMessageDialog(null,"Effettua una scelta prima di proseguire");
                 }else{
                     
                 Visualizza_Referto vr= new Visualizza_Referto(id_prenot,true);
                 vr.setVisible(true);
           
             
            }
             break;
             
             case 4:
                 if(reparto==null){
                     JOptionPane.showMessageDialog(null,"Effettua una scelta prima di proseguire");
                 }else{
                     db.connetti();
                     SQL="delete from visite where reparto='"+reparto+"' and data='"+data_table+"' and ora='"+ora_table+"';";
                     boolean ris=db.eseguiAggiornamento(SQL);
                     if (ris==true) System.out.println("ok");
                     popolaTable("select * from visite",4);
                 }
                 break;
             case 5:
                 if(reparto==null){
                     JOptionPane.showMessageDialog(null,"Effettua una scelta prima di proseguire");
                 }else{
                     db.connetti();
                     System.out.println(id_prenot);
                     SQL="update prenotazioni set priorita='0' where idprenotazione='"+id_prenot+"';";
                     boolean ris=db.eseguiAggiornamento(SQL);
                     System.out.println(ris);
                     popolaTable("select * from prenotazioni where priorita='1';",6);
                     model2.setRowCount(0);
                 }
                 break;
                 
                 
         }
     }
     
     private void confermaActionPerformed(java.awt.event.ActionEvent evt) throws SQLException{
         String SQL;
         switch (identificativo){
             case 1:
                 if((data_table==null) && (ora_table==null)) {
                     JOptionPane.showMessageDialog(null,"Effettua una scelta prima di proseguire");
                 }else{
                     db.connetti();
                     if (priorita.isSelected()){
                         SQL="insert into prenotazioni (reparto,data,ora,priorita,idpaziente) values ('"+reparto+"','"+data_table+"','"+ora_table+"','1','"+paz.getCod_Fisc()+"');";
                     }else { 
                         SQL="insert into prenotazioni (reparto,data,ora,priorita,idpaziente) values ('"+reparto+"','"+data_table+"','"+ora_table+"','0','"+paz.getCod_Fisc()+"');";
                     }
                     boolean ris=db.eseguiAggiornamento(SQL);
                     SQL="delete from visite where reparto='"+reparto+"' and data='"+data_table+"'and ora='"+ora_table+"';";
                     if (ris==true) System.out.println("ok");
                     ris=db.eseguiAggiornamento(SQL);
                     if (ris==true) System.out.println("inserimento avvenuto");
                     String codice=null;
                     try{ 
                         SQL="select idprenotazione from prenotazioni where reparto='"+reparto+"' and data='"+data_table+"' and ora='"+ora_table+"';";
                 try (ResultSet rs = db.eseguiQuery(SQL)) {
                     while(rs.next()){
                         codice=rs.getString("idprenotazione");
                     }
                 }
                     } catch(SQLException e){ System.out.println(e); }
 
                     SQL="insert into referti (idprenotazione,cod_fisc) values ('"+codice+"','"+paz.getCod_Fisc()+"');";
                     ris=db.eseguiAggiornamento(SQL);
                     if (ris==true) System.out.println("inserimento avvenuto");
                     popolaTable("select data,ora from visite where reparto='"+reparto+"' and priorita='0' order by data,ora asc;",model.getColumnCount());
                     data_table=null;
                     ora_table=null;
                     db.disconnetti();
                     priorita.setSelected(false);
                 }
                 break;
             case 4: //inserimento nuova data
                 if(table2.getValueAt(0, 0)!=null && table2.getValueAt(0, 1)!=null && table2.getValueAt(0, 2)!=null && table2.getValueAt(0, 3)!=null){
                 reparto=(String) table2.getValueAt(0, 0);
                 data_table = (String) table2.getValueAt(0, 1);
                 ora_table = (String) table2.getValueAt(0, 2);
                 prior = (String) table2.getValueAt(0, 3);
                 int p=Integer.parseInt(prior);
                
                 
                  
                     if (isValidDate(data_table)==false){
                         JOptionPane.showMessageDialog(null,"Inserire la data nella forma aaaa-mm-gg");                                  
                     }
                     boolean corretta=oraSyntaxCheck(ora_table); 
                     if(corretta==false){
                         JOptionPane.showMessageDialog(null,"Inserire l'ora nella forma hh:mm");
                     }
                     if(isValidDate(data_table) && corretta){
                         db.connetti();
                         SQL="insert into visite (reparto,data,ora,priorita) values ('"+reparto+"','"+data_table+"','"+ora_table+"','"+p+"');";
                         boolean ris=db.eseguiAggiornamento(SQL);
                         popolaTable("select * from visite;",4);
                     }
                     db.disconnetti();
                     model2.setRowCount(0);
                     model2.setRowCount(1);
                 } else JOptionPane.showMessageDialog(null,"Inserire tutti i dati prima di proseguire");
                 break;
                 
             case 5:
                 db.connetti();
                 int riga1=table.getSelectedRow();
                 int riga2=table2.getSelectedRow();
                 if((riga1==-1 && riga2==-1) || (riga1!=-1 && riga2==-1)){
                     JOptionPane.showMessageDialog(null,"Effettuare una scelta");
                 }else{
                    SQL="delete from visite where reparto='"+reparto2+"' and data='"+data_table2+"' and ora='"+ora_table2+"';";
                    boolean ris=db.eseguiAggiornamento(SQL);
                    System.out.println(ris);
                    SQL="update prenotazioni set data='"+data_table2+"',ora='"+ora_table2+"',priorita='0' where idprenotazione='"+id_prenot+"';";
                    ris=db.eseguiAggiornamento(SQL);
                     System.out.println(ris);
                    SQL="insert into visite (reparto,data,ora,priorita) values ('"+reparto+"','"+data_table+"','"+ora_table+"','0');";
                    ris=db.eseguiAggiornamento(SQL);
                     System.out.println(ris);
                     popolaTable("select * from prenotazioni where priorita='1';",6);
                    model2.setRowCount(0);
                 }
                
                 db.disconnetti();
                
                 break;
         }
     }
   
     private void selezionaData(MouseEvent me){
        
         data_table = (String) table.getValueAt(table.getSelectedRow(), 0);
        ora_table = (String) table.getValueAt(table.getSelectedRow(), 1);
        //if((data_table!=null) && (ora_table!=null)) {
        //    JOptionPane.showMessageDialog(null,"Contenuto riga selezionata: "+data_table+" "+ora_table);
        // }
                
     }
     
     private void selezionaPrenotazione(MouseEvent me,int id,DefaultTableModel model2){
        switch(id){
            case 2:
         id_prenot = (String) table.getValueAt(table.getSelectedRow(), 0);
        //if(id_prenot!=null) {
        //    JOptionPane.showMessageDialog(null,"Contenuto riga selezionata: "+id_prenot);
        //}
           break; 
             case 5:
                 System.out.println("5");
                 id_prenot = (String) table.getValueAt(table.getSelectedRow(), 0);
               reparto= (String) table.getValueAt(table.getSelectedRow(), 1);
               data_table=(String) table.getValueAt(table.getSelectedRow(), 2);
               ora_table=(String) table.getValueAt(table.getSelectedRow(), 3);
                 prior=(String) table.getValueAt(table.getSelectedRow(), 4);
                 System.out.println(reparto+data_table+ora_table+prior);
                 String SQL="select * from visite where reparto ='"+reparto+"' and data <='"+data_table+"' and ora <='"+ora_table+"' order by data,ora asc;";
                 System.out.println(SQL);
                 try {
             model2.setRowCount(0); //svuota la tabella
             db.connetti();
                 try (ResultSet rs2 = db.eseguiQuery(SQL)) {
                     while(rs2.next()){ 
                         String[] stringa={rs2.getString(1),rs2.getString(2),rs2.getString(3),rs2.getString(4)};
                         System.out.println(stringa);        
                         model2.addRow(stringa);
                     }
                 }
             db.disconnetti();
             }catch(SQLException | NullPointerException e){ System.out.println(e); }
                 break;
         }
            
        
      
     }
     
     private void selezionaVisita(MouseEvent me,int id){
         switch(id){
             case 1:
         reparto = (String) table.getValueAt(table.getSelectedRow(), 0);
         data_table=(String) table.getValueAt(table.getSelectedRow(), 1);
         ora_table=(String) table.getValueAt(table.getSelectedRow(), 2);
       // if(reparto!=null) {
       //     JOptionPane.showMessageDialog(null,"Contenuto riga selezionata: "+reparto+" "+data_table+" "+ora_table);
         
            
       // }
                 break;
             case 5:
                 reparto2 = (String) table2.getValueAt(table2.getSelectedRow(), 0);
                  data_table2 = (String) table2.getValueAt(table2.getSelectedRow(), 1);
        ora_table2 = (String) table2.getValueAt(table2.getSelectedRow(), 2);
                 // if((data_table2!=null) && (ora_table2!=null)) {
                 // JOptionPane.showMessageDialog(null,"Contenuto riga selezionata: "+data_table2+" "+ora_table2);
                 // }
     }
     }
     
   private boolean isValidDate(String inDate) {
 
     if (inDate == null)
       return false;
 
     //set the format to use as a constructor argument
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
     
     if (inDate.trim().length() != dateFormat.toPattern().length())
       return false;
 
     dateFormat.setLenient(false);
     
     try {
       //parse the inDate parameter
       dateFormat.parse(inDate.trim());
     }
     catch (ParseException pe) {
       return false;
     }
     return true;
   }
   
   private boolean oraSyntaxCheck(String orario)
    {
         // Create the Pattern using the regex
         Pattern p1 = Pattern.compile("[0-1]+[0-9]+:+[0-5]+[0-9]");
         Pattern p2= Pattern.compile("[2]+[0-3]+:[0-5]+[0-9]");
         // Match the given string with the pattern
         Matcher m1 = p1.matcher(orario);
         Matcher m2= p2.matcher(orario);
  
         // check whether match is found
         boolean matchFound1 = m1.matches();
         boolean matchFound2 = m2.matches();
         
         if( matchFound1 || matchFound2){
             return true;
         }else return false;
  
     }
 } 
 
 class MyComboBoxEditor extends DefaultCellEditor {
     public MyComboBoxEditor(String[] items) {
         super(new JComboBox(items));
     }
 }
