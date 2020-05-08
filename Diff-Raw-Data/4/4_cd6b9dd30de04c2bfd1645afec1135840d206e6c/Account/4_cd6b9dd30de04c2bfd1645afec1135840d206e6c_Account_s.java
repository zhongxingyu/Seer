 package account;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 import stock.Stock;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 
 
 public class Account {
 	
 	private String username;
 	private String password;
 	private double balance;
 	private ArrayList<Stock> stocks;
 	
 	public Account(String username, String password) throws IOException, NoSuchAlgorithmException{
 		
 		this.username = username;
 		this.password = hashPassword(password);
 		this.balance = 100000;	   
 		this.stocks = new ArrayList<Stock>();
 
 		addAccount();
 	}
 	public Account(String username, String password, double balance) throws IOException, NoSuchAlgorithmException{
 
 		this.username = username;
 		this.password = hashPassword(password);
 		this.balance = balance;	   
 		this.stocks = new ArrayList<Stock>();
 		
 		addAccount();
 	}
 	public Account(){
 		this.username = null;
 		this.password = null;
 	}
 
 	private void addAccount() throws IOException{
 		
 		CSVReader csvReader = new CSVReader(new FileReader("accounts.csv"));
 		List<String []> content = csvReader.readAll();
 		content.add((this.username + "#" + this.password + "#" + this.balance).split("#"));
 		csvReader.close();
 		CSVWriter writer = new CSVWriter(new FileWriter("accounts.csv"));
 	    writer.writeAll(content);
 	    writer.close();
 	}
 	
 	public boolean accountLookup(String user, String pass) throws IOException, NumberFormatException, NoSuchAlgorithmException{
 		
 		CSVReader csvReader = new CSVReader(new FileReader("accounts.csv"));
 		List<String []> content = csvReader.readAll();
 		
 		for(int i = 0; i < content.size(); i++){
 			if(content.get(i)[0].equals(user)){
 				if(BCrypt.checkpw(pass, content.get(i)[1])){
 					this.username = user;
					this.password = hashPassword(pass);
 					this.balance = Double.parseDouble(content.get(i)[2]);
 					this.stocks = new ArrayList<Stock>();
 					for(int j = 3; j < content.get(i).length; j++){
 						Stock s = new Stock();
 						this.stocks.add(s.parseStock(content.get(i)[j]));
 					}
 					return true;
 				}
 			}
 		}
 			
 		return false;
 	}
 
 	public boolean doesUserExist(String user) throws IOException, NumberFormatException, NoSuchAlgorithmException{
 		CSVReader csvReader = new CSVReader(new FileReader("accounts.csv"));
 		List<String []> content = csvReader.readAll();
 		for(int i = 0; i < content.size(); i++){
 			if(content.get(i)[0].equals(user)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public String getUsername(){
 		return this.username;
 	}
 	public double getBalance(){
 		return this.balance;
 	}
 	public void buyStock(String ticker, double price, int shares) throws IOException {
 		this.balance -= (price * shares);
 		this.stocks.add(new Stock(ticker, shares, price));
 		this.updateAccount();
 	}
 	public void updateAccount() throws IOException{
 		CSVReader csvReader = new CSVReader(new FileReader("accounts.csv"));
 		List<String []> content = csvReader.readAll();
 		
 		for(int i = 0; i < content.size(); i++){
 			if(content.get(i)[0].equals(this.username)){
 				if(content.get(i)[1].equals(this.password)){
 					String temp[] = new String[this.stocks.size() + 3];
 					temp[0] = content.get(i)[0];
 					temp[1] = content.get(i)[1];
 					temp[2] = this.balance + "";
 					
 					int j = 3;
 					for(Stock s : this.stocks){
 						s.updateStock();
 						temp[j] = s.writeStock();
 						j++;
 					}
 					content.set(i, temp);
 					csvReader.close();
 					
 					CSVWriter writer = new CSVWriter(new FileWriter("accounts.csv"));
 				    writer.writeAll(content);
 				    writer.close();
 				    return;
 				}
 			}
 		}
 		
 	}
 	
 	public void updateAllAccounts() throws IOException{
 		CSVReader csvReader = new CSVReader(new FileReader("accounts.csv"));
 		List<String []> content = csvReader.readAll();
 		ArrayList<Account> accounts = new ArrayList<Account>();
 		for(int i = 0; i < content.size(); i++){
 			Account a = new Account();
 			a.username = content.get(i)[0];
 			a.password = content.get(i)[1];
 			a.balance = Double.parseDouble(content.get(i)[2]);
 			a.stocks = new ArrayList<Stock>();
 			for(int j = 3; j < content.get(i).length; j++){
 				Stock s = new Stock();
 				a.stocks.add(s.parseStock(content.get(i)[j]));
 			}
 			accounts.add(a);
 		}
 		csvReader.close();
 		for(Account a: accounts){
 			a.updateAccount();
 		}
 	}
 	
 	public void clearAccountData() throws IOException{
 		CSVReader csvReader = new CSVReader(new FileReader("accounts.csv"));
 		List<String []> content = csvReader.readAll();
 		List<String []> newcontent = new ArrayList<String []>();
 		newcontent.add(content.get(0));
 		csvReader.close();
 		CSVWriter writer = new CSVWriter(new FileWriter("accounts.csv"));
 	    writer.writeAll(newcontent);
 	    writer.close();
 	}
 	
 	public void showStocks() throws NumberFormatException, IOException{
 		for(Stock s : this.stocks){
 			s.printStock();
 		}
 	}
 	
 	public void sellStock(String ticker, int shares) throws IOException{
 		for(int i = 0; i < this.stocks.size(); i++){
 			Stock s = this.stocks.get(i);
 			if(s.getTicker().equalsIgnoreCase(ticker)){
 				if(s.getShares() == shares){
 					this.balance += s.modifyStock(shares);
 					this.stocks.remove(s);
 				}
 				else{
 					balance += s.modifyStock(shares);
 				}
 			}
 		}
 		this.updateAccount();
 	}
 	
 	public String hashPassword(String pass){
 		return BCrypt.hashpw(pass, BCrypt.gensalt());	
 	}
 }
