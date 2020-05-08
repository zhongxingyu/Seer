 package ch.bli.mez.view.employee;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 
 public class EmployeePanel extends JPanel {
 
 	private static final long serialVersionUID = -1774268602344482972L;
 	
 	private JTextField lastname;
 	private JTextField firstname;
 	private JTextField street;
 	private JTextField city;
 	private JTextField plz;
 	private JTextField homeNumber;
 	private JTextField mobileNumber;
 	private JTextField email;
 	
 	private JLabel plzError;
 	private JLabel lastNameError;
 	private JLabel firstNameError;
 	
 	private JLabel confirmation;
 	
 	private JButton btnSave;
 	private JButton btnDelete;
 
 	private JLayeredPane layeredPane = new JLayeredPane();
 	
 	/**
 	 * Create the panel for employees
 	 * 
 	 * Panels for the contracts and timetransfers are separated
 	 * 
 	 */
 	public EmployeePanel() {
 		setLayout(new CardLayout(0, 0));
 		
 		add(layeredPane, "name_13971428008795");
 		
 		confirmation = new JLabel("xxyy gespeichert...");
 		confirmation.setBounds(25, 4, 210, 14);
 		layeredPane.add(confirmation);
 		confirmation.setForeground(new Color(34,139,34));
 		confirmation.setVisible(false);
 		
 		JLabel lbllastname = new JLabel("Name");
 		lbllastname.setBounds(25, 78, 111, 14);
 		lbllastname.setFont(lbllastname.getFont().deriveFont(Font.BOLD));
 		layeredPane.add(lbllastname);
 		
 		lastname = new JTextField();
 		lastname.setBounds(133, 73, 196, 30);
 		layeredPane.add(lastname);
 		lastname.setColumns(10);
 		
 		lastNameError = new JLabel("Dieses Feld muss ausgefüllt werden.");
 		lastNameError.setBounds(345, 78, 240, 14);
 		lastNameError.setForeground(Color.red);
 		lastNameError.setVisible(false);
 		layeredPane.add(lastNameError);
 		
 		JLabel lblfirstname = new JLabel("Vorname");
 		lblfirstname.setBounds(25, 37, 111, 14);
 		lblfirstname.setFont(lblfirstname.getFont().deriveFont(Font.BOLD));
 		layeredPane.add(lblfirstname);
 		
 		firstname = new JTextField();
 		firstname.setBounds(133, 32, 196, 30);
 		layeredPane.add(firstname);
 		firstname.setColumns(10);
 		
 		firstNameError = new JLabel("Dieses Feld muss ausgefüllt werden.");
 		firstNameError.setBounds(345, 37, 240, 14);
 		firstNameError.setForeground(Color.red);
 		firstNameError.setVisible(false);
 		layeredPane.add(firstNameError);
 		
 		JLabel lblstreet = new JLabel("Strasse");
 		lblstreet.setBounds(25, 119, 111, 14);
 		layeredPane.add(lblstreet);
 		
 		street = new JTextField();
 		street.setBounds(133, 114, 196, 30);
 		layeredPane.add(street);
 		street.setColumns(10);
 
 		JLabel lblcity = new JLabel("Ort");
 		lblcity.setBounds(25, 201, 111, 14);
 		layeredPane.add(lblcity);
 		
 		city = new JTextField();
 		city.setBounds(133, 196, 196, 30);
 		layeredPane.add(city);
 		city.setColumns(10);
 		
 		JLabel lblplz = new JLabel("PLZ");
 		lblplz.setBounds(25, 160, 111, 14);
 		layeredPane.add(lblplz);
 		
 		plz = new JTextField();
 		plz.setBounds(133, 155, 196, 30);
 		layeredPane.add(plz);
 		plz.setColumns(10);
 		
 		plzError = new JLabel("Bitte eine Zahl eingeben.");
 		plzError.setBounds(345, 160, 160, 14);
 		plzError.setForeground(Color.red);
 		plzError.setVisible(false);
 		layeredPane.add(plzError);
 
 		JLabel lblhomeNumber = new JLabel("Festnetz Tel.Nr.");
 		lblhomeNumber.setBounds(24, 242, 112, 14);
 		layeredPane.add(lblhomeNumber);
 		
 		homeNumber = new JTextField();
 		homeNumber.setBounds(133, 237, 196, 30);
 		layeredPane.add(homeNumber);
 		homeNumber.setColumns(10);
 		
 		JLabel lblmobileNumber = new JLabel("Natel Tel.Nr.");
 		lblmobileNumber.setBounds(25, 283, 111, 14);
 		layeredPane.add(lblmobileNumber);
 		
 		mobileNumber = new JTextField();
 		mobileNumber.setBounds(133, 278, 196, 30);
 		layeredPane.add(mobileNumber);
 		mobileNumber.setColumns(10);
 		
 		JLabel lblemail = new JLabel("E-Mail");
 		lblemail.setBounds(24, 324, 112, 14);
 		layeredPane.add(lblemail);
 		
 		email = new JTextField();
 		email.setBounds(133, 319, 196, 30);
 		layeredPane.add(email);
 		email.setColumns(10);
 		
 		btnSave = new JButton("Speichern");
 		btnSave.setBounds(25, 373, 89, 23);
 		layeredPane.add(btnSave);
 		
 		btnDelete = new JButton("Deaktivieren");
 		btnDelete.setBounds(125, 373, 120, 23);
 		layeredPane.add(btnDelete);
 	}
 	
 	public void setSaveEmployeeListener(ActionListener actionListener) {
 		btnSave.addActionListener(actionListener);
 	}
 	
 	public void setStatusButtonListener(ActionListener actionListener) {
 		btnDelete.addActionListener(actionListener);
 	}
 	
 	public void setStatusButtonName(String value){
 		btnDelete.setName(value);
 	}
 	
 	public String getDeleteButtonName(){
 		return btnDelete.getName();
 	}
 	
 	public void hideStatusButton(){
 		btnDelete.setVisible(false);
 	}
 	
 	public void showPlzError(){
 		plz.setBackground(new Color(255,90,90));
 		plzError.setVisible(true);
 	}
 	
 	private void hidePlzError(){
 		plz.setBackground(new Color(255,255,255));
 		plzError.setVisible(false);
 	}
 	
 	public void showFirstNameError(){
 		firstname.setBackground(new Color(255,90,90));
 		firstNameError.setVisible(true);
 	}
 	
 	private void hideFirstNameError(){
 		firstname.setBackground(new Color(255,255,255));
 		firstNameError.setVisible(false);
 	}
 	
 	public void showLastNameError(){
 		lastname.setBackground(new Color(255,90,90));
 		lastNameError.setVisible(true);
 	}
 	
 	private void hideLastNameError(){
 		lastname.setBackground(new Color(255,255,255));
 		lastNameError.setVisible(false);
 	}
 	
 	public void showConfirmation(String name){
 		confirmation.setText(name + " wurde gespeichert.");
 		confirmation.setVisible(true);
 	}
 	
 	public void hideConfirmation() {
 		confirmation.setVisible(false);
 	}
 	
 	public void hideErrors(){
 		hideFirstNameError();
 		hideLastNameError();
 		hidePlzError();
 	}
 	
 	public void cleanFields(){
 		setFirstname("");
 		setLastname("");
 		setStreet("");
 		setPlz("");
 		setCity("");
 		setMobileNumber("");
 		setHomeNumber("");
 		setEmail("");
 	}
 	
 	
 	/**
 	 * Ein JPanel auf diesem Panel platzieren. Zukünftig für Ferientage oder verträge.
 	 * @param panel JPanel
 	 * @param coordinates Rectangle koordinaten
 	 */
 	public void addAnotherPanel(JPanel panel, Rectangle coordinates){
 		panel.setBounds(coordinates);
 		this.layeredPane.add(panel);
 	}
 	
 	public String getFirstname(){
 		return firstname.getText();
 	}
 	
 	public String getLastname(){
 		return lastname.getText();
 	}
 	
 	public String getStreet(){
 		return street.getText();
 	}
 	
 	public String getCity(){
 		return city.getText();
 	}
 	
 	public String getPlz(){
 		return plz.getText();
 	}
 	
 	public String getMobileNumber(){
 		return mobileNumber.getText();
 	}
 	
 	public String getHomeNumber(){
 		return homeNumber.getText();
 	}
 	
 	public String getEmail(){
 		return email.getText();
 	}
 	
 	public JTextField getlastname() {
 		return lastname;
 	}
 
 	public void setLastname(String value) {
 		this.lastname.setText(value);
 	}
 
 	public JTextField getfirstname() {
 		return firstname;
 	}
 
 	public void setFirstname(String value) {
 		this.firstname.setText(value);
 	}
 
 	public JTextField getstreet() {
 		return street;
 	}
 
 	public void setStreet(String value) {
 		this.street.setText(value);
 	}
 
 	public JTextField getcity() {
 		return city;
 	}
 
 	public void setCity(String value) {
 		this.city.setText(value);
 	}
 
 	public JTextField getplz() {
 		return plz;
 	}
 
 	public void setPlz(String value) {
 		this.plz.setText(value);
 	}
 
 	public JTextField gethomeNumber() {
 		return homeNumber;
 	}
 
 	public void setHomeNumber(String value) {
 		this.homeNumber.setText(value);
 	}
 
 	public JTextField getmobileNumber() {
 		return mobileNumber;
 	}
 
 	public void setMobileNumber(String value) {
 		this.mobileNumber.setText(value);
 	}
 
 	public JTextField getemail() {
 		return email;
 	}
 
 	public void setEmail(String value) {
 		this.email.setText(value);
 	}
 	
 	public JButton getBtnSave() {
 		return btnSave;
 	}
 	
 	public JButton getBtnDelete() {
 		return btnDelete;
 	}
 }
