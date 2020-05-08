 package view;
 
 import controller.WindowController;
 import domain.IllegalLoanOperationException;
 import domain.Library;
 import domain.Loan;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Observable;
 
 public class DetailLoanWindow extends ListenerJFrame {
 
     private JPanel contentPane;
     private JButton btnAusleiheAbschliessen;
     private Loan loan;
     private JLabel lblNewLabel_3;
     private JTextField returnDatetextField;
     private JLabel lblRckgabedatum;
     private JLabel errorLabel;
     private JPanel panel_1;
     private JLabel lblberfllig;
 
 
     public DetailLoanWindow(Loan loan1, Library library, WindowController windowCtrl) {
         super(library, windowCtrl);
         this.loan = loan1;
         library.addObserver(this);
 		this.setMinimumSize(new Dimension(600,375));
 
         setTitle("Ausleihe Detail");
         setBounds(100, 100, 663, 361);
         contentPane = new JPanel();
         contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
         setContentPane(contentPane);
         contentPane.setLayout(new BorderLayout(0, 0));
 
         JPanel panel = new JPanel();
         contentPane.add(panel, BorderLayout.NORTH);
         GridBagLayout gbl_panel = new GridBagLayout();
         gbl_panel.columnWidths = new int[]{1, 0, 0};
         gbl_panel.rowHeights = new int[]{37, 0, 0};
         gbl_panel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
         gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
         panel.setLayout(gbl_panel);
 
         panel_1 = new JPanel();
         if (loan.isOverdue()) {
             panel_1.setForeground(Color.RED);
         }
         panel_1.setBorder(new TitledBorder(null, "Buch", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
         GridBagConstraints gbc_panel_1 = new GridBagConstraints();
         gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
         gbc_panel_1.anchor = GridBagConstraints.NORTH;
         gbc_panel_1.gridx = 0;
         gbc_panel_1.gridy = 0;
         panel.add(panel_1, gbc_panel_1);
         GridBagLayout gbl_panel_1 = new GridBagLayout();
         gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0};
         gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0};
         gbl_panel_1.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
         gbl_panel_1.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
         panel_1.setLayout(gbl_panel_1);
 
         JLabel lblNewLabel = new JLabel("Titel");
         GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
         gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
         gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
         gbc_lblNewLabel.gridx = 0;
         gbc_lblNewLabel.gridy = 0;
         panel_1.add(lblNewLabel, gbc_lblNewLabel);
 
         JLabel lblNewLabel_1 = new JLabel("<html>" + loan.getCopy().getTitle() + "</html>");
         GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
         gbc_lblNewLabel_1.fill = GridBagConstraints.HORIZONTAL;
         gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
         gbc_lblNewLabel_1.gridx = 2;
         gbc_lblNewLabel_1.gridy = 0;
         panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
 
         JLabel lblAusleiheDatum = new JLabel("Ausleihe Datum");
         GridBagConstraints gbc_lblAusleiheDatum = new GridBagConstraints();
         gbc_lblAusleiheDatum.anchor = GridBagConstraints.WEST;
         gbc_lblAusleiheDatum.insets = new Insets(0, 0, 5, 5);
         gbc_lblAusleiheDatum.gridx = 0;
         gbc_lblAusleiheDatum.gridy = 1;
         panel_1.add(lblAusleiheDatum, gbc_lblAusleiheDatum);
 
         JLabel lblNewLabel_7 = new JLabel(loan.getFormattedPickupDate());
         GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
         gbc_lblNewLabel_7.fill = GridBagConstraints.BOTH;
         gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 0);
         gbc_lblNewLabel_7.gridx = 2;
         gbc_lblNewLabel_7.gridy = 1;
         panel_1.add(lblNewLabel_7, gbc_lblNewLabel_7);
 
         JLabel lblErwRckgabeDatum = new JLabel("erw. Rückgabe Datum");
         GridBagConstraints gbc_lblErwRckgabeDatum = new GridBagConstraints();
         gbc_lblErwRckgabeDatum.insets = new Insets(0, 0, 5, 5);
         gbc_lblErwRckgabeDatum.anchor = GridBagConstraints.WEST;
         gbc_lblErwRckgabeDatum.gridx = 0;
         gbc_lblErwRckgabeDatum.gridy = 2;
         panel_1.add(lblErwRckgabeDatum, gbc_lblErwRckgabeDatum);
 
         JLabel lblNewLabel_2 = new JLabel(loan.getFormattedExpectedReturnDate());
         GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
         gbc_lblNewLabel_2.fill = GridBagConstraints.HORIZONTAL;
         gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 0);
         gbc_lblNewLabel_2.gridx = 2;
         gbc_lblNewLabel_2.gridy = 2;
         panel_1.add(lblNewLabel_2, gbc_lblNewLabel_2);
 
         JLabel lblEffRckgabeDatum = new JLabel("eff. Rückgabe Datum");
         GridBagConstraints gbc_lblEffRckgabeDatum = new GridBagConstraints();
         gbc_lblEffRckgabeDatum.anchor = GridBagConstraints.WEST;
         gbc_lblEffRckgabeDatum.insets = new Insets(0, 0, 0, 5);
         gbc_lblEffRckgabeDatum.gridx = 0;
         gbc_lblEffRckgabeDatum.gridy = 3;
         panel_1.add(lblEffRckgabeDatum, gbc_lblEffRckgabeDatum);
 
         lblNewLabel_3 = new JLabel(loan.getFormattedReturnDate());
         GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
         gbc_lblNewLabel_3.fill = GridBagConstraints.HORIZONTAL;
         gbc_lblNewLabel_3.gridx = 2;
         gbc_lblNewLabel_3.gridy = 3;
         panel_1.add(lblNewLabel_3, gbc_lblNewLabel_3);
 
         JPanel panel_2 = new JPanel();
         panel_2.setBorder(new TitledBorder(null, "Kunde", TitledBorder.LEADING, TitledBorder.TOP, null, null));
         GridBagConstraints gbc_panel_2 = new GridBagConstraints();
         gbc_panel_2.anchor = GridBagConstraints.NORTH;
         gbc_panel_2.fill = GridBagConstraints.HORIZONTAL;
         gbc_panel_2.gridx = 0;
         gbc_panel_2.gridy = 1;
         panel.add(panel_2, gbc_panel_2);
         GridBagLayout gbl_panel_2 = new GridBagLayout();
         gbl_panel_2.columnWidths = new int[]{169, 0, 0, 0};
         gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0};
         gbl_panel_2.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
         gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
         panel_2.setLayout(gbl_panel_2);
 
         JLabel lblName = new JLabel("Name");
         GridBagConstraints gbc_lblName = new GridBagConstraints();
         gbc_lblName.anchor = GridBagConstraints.WEST;
         gbc_lblName.insets = new Insets(0, 0, 5, 5);
         gbc_lblName.gridx = 0;
         gbc_lblName.gridy = 0;
         panel_2.add(lblName, gbc_lblName);
 
         JLabel lblNewLabel_4 = new JLabel(loan.getCustomer().getSurname() + " " + loan.getCustomer().getName());
         GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
         gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
         gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 0);
         gbc_lblNewLabel_4.gridx = 2;
         gbc_lblNewLabel_4.gridy = 0;
         panel_2.add(lblNewLabel_4, gbc_lblNewLabel_4);
 
         JLabel lblAdresse = new JLabel("Adresse");
         GridBagConstraints gbc_lblAdresse = new GridBagConstraints();
         gbc_lblAdresse.anchor = GridBagConstraints.WEST;
         gbc_lblAdresse.insets = new Insets(0, 0, 5, 5);
         gbc_lblAdresse.gridx = 0;
         gbc_lblAdresse.gridy = 1;
         panel_2.add(lblAdresse, gbc_lblAdresse);
 
         JLabel lblNewLabel_5 = new JLabel(loan.getCustomer().getStreet());
         GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
         gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
         gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 0);
         gbc_lblNewLabel_5.gridx = 2;
         gbc_lblNewLabel_5.gridy = 1;
         panel_2.add(lblNewLabel_5, gbc_lblNewLabel_5);
 
         JLabel lblOrt = new JLabel("Ort");
         GridBagConstraints gbc_lblOrt = new GridBagConstraints();
         gbc_lblOrt.anchor = GridBagConstraints.WEST;
         gbc_lblOrt.insets = new Insets(0, 0, 0, 5);
         gbc_lblOrt.gridx = 0;
         gbc_lblOrt.gridy = 2;
         panel_2.add(lblOrt, gbc_lblOrt);
 
         JLabel lblNewLabel_6 = new JLabel(loan.getCustomer().getCity());
         GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
         gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
         gbc_lblNewLabel_6.gridx = 2;
         gbc_lblNewLabel_6.gridy = 2;
         panel_2.add(lblNewLabel_6, gbc_lblNewLabel_6);
 
         JPanel panel_3 = new JPanel();
         contentPane.add(panel_3, BorderLayout.CENTER);
         GridBagLayout gbl_panel_3 = new GridBagLayout();
         gbl_panel_3.columnWidths = new int[]{163, 80, 0, 0};
         gbl_panel_3.rowHeights = new int[]{25, 0, 0, 0, 0};
         gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
         gbl_panel_3.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
         panel_3.setLayout(gbl_panel_3);
 
         lblberfllig = new JLabel("!!! Überfällig !!!");
 
         if (loan.isOverdue()) {
             panel_1.setForeground(Color.RED);
         } else {
             lblberfllig.setVisible(false);
         }
         lblberfllig.setFont(new Font("Dialog", Font.BOLD, 14));
         lblberfllig.setForeground(Color.RED);
         GridBagConstraints gbc_lblberfllig_1 = new GridBagConstraints();
         gbc_lblberfllig_1.anchor = GridBagConstraints.WEST;
         gbc_lblberfllig_1.insets = new Insets(0, 0, 5, 5);
         gbc_lblberfllig_1.gridx = 1;
         gbc_lblberfllig_1.gridy = 0;
         panel_3.add(lblberfllig, gbc_lblberfllig_1);
 
         lblRckgabedatum = new JLabel("Rückgabe-Datum");
         GridBagConstraints gbc_lblRckgabedatum = new GridBagConstraints();
         gbc_lblRckgabedatum.anchor = GridBagConstraints.WEST;
         gbc_lblRckgabedatum.insets = new Insets(0, 0, 5, 5);
         gbc_lblRckgabedatum.gridx = 0;
         gbc_lblRckgabedatum.gridy = 1;
         panel_3.add(lblRckgabedatum, gbc_lblRckgabedatum);
 
         returnDatetextField = new JTextField();
         GridBagConstraints gbc_returnDatetextField = new GridBagConstraints();
         gbc_returnDatetextField.insets = new Insets(0, 0, 5, 5);
         gbc_returnDatetextField.fill = GridBagConstraints.HORIZONTAL;
         gbc_returnDatetextField.gridx = 1;
         gbc_returnDatetextField.gridy = 1;
         panel_3.add(returnDatetextField, gbc_returnDatetextField);
         returnDatetextField.setColumns(10);
         returnDatetextField.setText(new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
 
         errorLabel = new JLabel("");
         errorLabel.setForeground(Color.RED);
         GridBagConstraints gbc_lblberfllig = new GridBagConstraints();
         gbc_lblberfllig.insets = new Insets(0, 0, 5, 0);
         gbc_lblberfllig.gridx = 2;
         gbc_lblberfllig.gridy = 2;
         panel_3.add(errorLabel, gbc_lblberfllig);
 
         btnAusleiheAbschliessen = new JButton("Ausleihe abschliessen");
         btnAusleiheAbschliessen.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 errorLabel.setText("");
                 String strDate = returnDatetextField.getText();
                 Date date = null;
                 try {
                     date = new SimpleDateFormat("dd.MM.yyyy").parse(strDate);
                     GregorianCalendar cal = new GregorianCalendar();
                     cal.setTime(date);
                     loan.returnCopy(cal);
                 } catch (IllegalLoanOperationException e1) {
                     errorLabel.setText(e1.getMessage());
                 } catch (ParseException e2) {
                     errorLabel.setText("Bitte geben Sie ein gültiges Datum ein");
                 }
 
 
                 updateButton(loan);
 
             }
         });
         btnAusleiheAbschliessen.setVerticalAlignment(SwingConstants.BOTTOM);
         GridBagConstraints gbc_btnAusleiheAbschliessen = new GridBagConstraints();
         gbc_btnAusleiheAbschliessen.insets = new Insets(0, 0, 0, 5);
         gbc_btnAusleiheAbschliessen.fill = GridBagConstraints.HORIZONTAL;
         gbc_btnAusleiheAbschliessen.anchor = GridBagConstraints.NORTH;
         gbc_btnAusleiheAbschliessen.gridx = 0;
         gbc_btnAusleiheAbschliessen.gridy = 3;
         panel_3.add(btnAusleiheAbschliessen, gbc_btnAusleiheAbschliessen);
         updateButton(loan);
     }
 
     private void updateButton(Loan loan) {
         if (loan != null) {
 
             if (loan.getFormattedReturnDate() != "00.00.00") {
                 returnDatetextField.setVisible(false);
                 lblRckgabedatum.setVisible(false);
                 btnAusleiheAbschliessen.setEnabled(false);
                 panel_1.setForeground(Color.BLACK);
                 lblberfllig.setVisible(false);
                 btnAusleiheAbschliessen.setText("Ausleihe abgeschlossen");
             } else {
                 returnDatetextField.setVisible(true);
                 lblRckgabedatum.setVisible(true);
                 btnAusleiheAbschliessen.setEnabled(true);
                this.getRootPane().setDefaultButton(btnAusleiheAbschliessen);
                 btnAusleiheAbschliessen.setText("Ausleihe abschliessen");
             }
         }
 
     }
 
     @Override
     public void update(Observable o, Object arg) {
         lblNewLabel_3.setText(loan.getFormattedReturnDate());
     }
 
 }
