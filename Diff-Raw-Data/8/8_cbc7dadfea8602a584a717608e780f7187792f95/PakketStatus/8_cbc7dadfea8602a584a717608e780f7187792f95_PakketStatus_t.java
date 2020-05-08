 package java_backend;
 
 import Database.DbConnect;
 import java.awt.BorderLayout;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author Jelle
  */
 class PakketStatus extends JPanel implements ActionListener {
 
     private JLabel zoeklabel, informatie;
     private JTextField zoekveld;
     private JButton zoek;
     private JTable info;
 
     public PakketStatus() {
         zoeklabel = new JLabel("Pakket ID :");
         zoekveld = new JTextField(4);
         zoek = new JButton("Zoek");
         informatie = new JLabel("Achterhaal de pakketstatus door te zoeken op PakketID.");
         //instancieer JTable met datamodel van hierboven.
         this.info = new JTable(FillTabel(0));
         // Dit is de list selecetioner, die kijkt of je iets selecteert
         ListSelectionModel listMod = info.getSelectionModel();
         // Hierdoor kan je maar 1 regel selecteren
         listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         // Voegt de listener toe aan het frame
         listMod.addListSelectionListener(info);
         //set de layout van het panel op borderlayout
         this.setLayout(new BorderLayout());
         //voeg layout to aan jpanel top
         JPanel top = new JPanel();
         top.add(zoeklabel);
         top.add(this.zoekveld);
         top.add(this.zoek);
         //voeg layout toe aan Jpanel mid
         JPanel mid = new JPanel();
         mid.add(new JScrollPane(this.info));
 
 
         JPanel bot = new JPanel();
         bot.add(informatie);
 
         //voeg uiteindelijk alles toe aan het centrale geheel.
         this.add(top, BorderLayout.NORTH);
         this.add(mid, BorderLayout.CENTER);
         this.add(bot, BorderLayout.SOUTH);
 
         //add actionlisteners
         this.zoek.addActionListener(this);
 
         //tonen maar
         this.setVisible(true);
         final Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);
 
     }
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         //selectie button geladen.
         if (ae.getSource() == zoek) {
             //jaar geselecteerd.       
 
             if (zoekveld.getText().equals("")) {
                 //foutmelding niks ingevuld
                 foutmelding("Foutmelding", "U heeft geen waarde ingevoerd");
             } else if (!isInteger(zoekveld.getText())) {
                 //waarde is niet numeriek.
                 foutmelding("Foutmelding", "Het pakketnummer moet een getal zijn.");
             } else {
                 //vul table met info
                  //jaar geselecteerd.          
                         this.info.setModel(FillTabel(Integer.parseInt(zoekveld.getText())));
                        this.info.repaint();
                 //repaint de tabel om het opnieuw weer te geven.
                 this.info.repaint();
 
             }
         }
 
     
     }
     
 
     public TableModel FillTabel(final int pakketID) {
 
         TableModel dataModel = new AbstractTableModel() {
             //instancieer columnnamen
             final String[] tabelinhoud = {"Traject ID", "Type", "Vervoerder", "Van", "Naar", "Status"};
             DbConnect dbc = new DbConnect();
             final Object[][] data = dbc.getPakketWijzigen(pakketID);
 
             public int getColumnCount() {
                 return tabelinhoud.length;
             }
 
             public int getRowCount() {
                 return data.length;
             }
 
             public Object getValueAt(int row, int col) {
                 return data[row][col];
             }
 
             public String getColumnName(int column) {
                 return tabelinhoud[column];
             }
         ;
         };
         return dataModel;
 
     }
 
     /**
      * - * Toon foutmelding aan de hand van meegegeven parameters. -
      *
      * @author Jelle Smeets -
      * @param titel -
      * @param melding -
      */
     public static void foutmelding(String titel, String melding) {
         JDialog jd = new JDialog();
         jd.setSize(400, 175);
         jd.setTitle(titel);
         jd.add(new JLabel(melding));
         jd.setVisible(true);
     }
 
     /**
      * - * Controleer of string numeriek is. -
      *
      * @author Jelle -
      * @param String input -
      */
     public boolean isInteger(String input) {
         try {
             Integer.parseInt(input);
             return true;
         } catch (Exception e) {
             return false;
         }
     }
 }
 
 
