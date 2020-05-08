 package Integrated;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingConstants;
 import javax.swing.border.Border;
 
 import Database.CustomerOrder;
 
 public class PayPane extends JPanel{
 	
 	/*
 	 * δ OrderPanel ޾ƿ cancel ư ڷΰ .
 	 * ڷΰ Flow : cancel Button Event
 	 *   Flow : resetTotal in orderPanel -> cancel Button Event
 	 */
 	
 	public static boolean payWorking = false;
 	
 	private OrderPanel parent;
 	private JLabel label = new JLabel();
 	private Border blackline;
 	private int total;						//ֹ Ѿ
 	
 	private JPanel centerPanel;
 	private JPanel bottomPanel;
 	
 	private JLabel dateLabel = new JLabel();	// Ͻ
 	private JLabel totalLabel = new JLabel();	//  ݾ
 	private JLabel recommandLabel = new JLabel();	//õ ޴
 	private JLabel ruleLabel = new JLabel();	//Ǹ 
 	
 	private JButton cardBtn;
 	private JButton cashBtn;
 	private JButton backBtn;
 	private JButton cancelBtn;
 	
 	
 	public PayPane(String text, OrderPanel orderPanel){
 		
 		this.parent = orderPanel;
 		Color FG_COLOR = new Color(0x3B5998);
 		label.setForeground(FG_COLOR);
 		label.setFont(new Font("Sans", Font.BOLD, 30));
 		label.setVerticalAlignment(SwingConstants.CENTER);
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		//this.db = this.parent.os.db;
 		
 		//blackline = BorderFactory.createLineBorder(Color.black,5);
 		//this.setBorder(blackline);
 		
 		this.centerPanel = new JPanel(new GridLayout(7,1,10,10));
 		this.bottomPanel = new JPanel(new GridLayout(1,4,10,10));
 		
 		this.cardBtn = new JButton(new ImageIcon("image/order/ī.png"));
 		this.cashBtn = new JButton(new ImageIcon("image/order/ݰ.png"));
 		this.backBtn = new JButton(new ImageIcon("image/order/.png"));
 		this.cancelBtn = new JButton(new ImageIcon("image/order/ڷΰ.png"));
 		cardBtn.setPreferredSize(new Dimension(136, 99));
 		cashBtn.setPreferredSize(new Dimension(136, 99));
 		backBtn.setPreferredSize(new Dimension(136, 99));
 		cancelBtn.setPreferredSize(new Dimension(136, 99));
 		this.total =  0;
 			
 		updatePanel();
 		updateBtnEvent();
 	}
 	
 	
 	private void updatePanel(){
 		this.setLayout(new BorderLayout());
 		
 		this.dateLabel.setFont(new Font("Sans", Font.BOLD, 25));
 		this.recommandLabel.setFont(new Font("Sans", Font.BOLD, 20));
 		this.ruleLabel.setFont(new Font("Sans", Font.BOLD, 11));
 		this.ruleLabel.setPreferredSize(new Dimension(400, 400));
 		this.totalLabel.setFont(new Font("Sans", Font.BOLD, 25));
 		
 		this.centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
 		updateCenterPanel();
 		this.add(this.centerPanel , BorderLayout.CENTER);
 		
 		this.bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		this.add(this.bottomPanel , BorderLayout.SOUTH);
 	}
 	
 	private void updateBtnEvent(){
 		
 		/*
 		 *   Flow -> resetTotal -> cancelBtn Push
 		 */
 		this.backBtn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				parent.resetTotal();
 				cancelBtn.doClick();
 			}
 		});
 		/*
 		 * 
 		 * DB QUERY MODIFICATION NEEDED
 		 * DATE TIME -> NOW() Function not working
 		 * Solution 1 : Query direct modification.
 		 * Solution 2 : Default value modification in myadmin Page
 		 */
 		this.cashBtn.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				//  
 				ArrayList<CustomerOrder> orderList = parent.getOrderList();
 				
 				if(orderList.isEmpty()) return;	//Error Check
 				
 				parent.latestOrderID++; //order_id 1 ߰
 				for(int i=0; i<orderList.size(); i++){
 					//System.out.println(orderList.get(i).getMenuName()
 					//		+ " : " + MenuList.herbMenuInt.get(orderList.get(i).getMenuName()));
 					//System.out.println("Order Count : " + orderList.get(i).getMenuCount());
 					parent.os.db.exec("INSERT INTO herb_order ("
 							+ "order_id, order_menu_id, order_count,  order_cash, order_date) VALUES( "
 							+ parent.latestOrderID + ", " // order_id
 							+ MenuList.herbMenuInt.get(orderList.get(i).getMenuName()).intValue() + ", "
 							+ orderList.get(i).getMenuCount() +", " // order_count
 							+ "1, "
 							+ "now());"); // order_date 
 					parent.resetTotal();
 					cancelBtn.doClick();
 					JOptionPane.showMessageDialog(null, "  ϷǾϴ!");
 				}
 			}
 		}); 
 		/*
 		 * 
 		 * DB QUERY MODIFICATION NEEDED
 		 * DATE TIME -> NOW() Function not working
 		 * Solution 1 : Query direct modification.
 		 * Solution 2 : Default value modification in myadmin Page
 		 */
 		this.cardBtn.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// ī 
 				ArrayList<CustomerOrder> orderList = parent.getOrderList();
 				
 				if(orderList.isEmpty()) return;	//Error Check
 				
 				parent.latestOrderID++; //order_id 1 ߰
 				for(int i=0; i<orderList.size(); i++){
 					//System.out.println(orderList.get(i).getMenuName()
 					//		+ " : " + MenuList.herbMenuInt.get(orderList.get(i).getMenuName()));
 					//System.out.println("Order Count : " + orderList.get(i).getMenuCount());
 					parent.os.db.exec("INSERT INTO herb_order ("
 							+ "order_id, order_menu_id, order_count,  order_cash, order_date) VALUES( "
 							+ parent.latestOrderID + ", " // order_id
 							+ MenuList.herbMenuInt.get(orderList.get(i).getMenuName()).intValue() + ", "
 							+ orderList.get(i).getMenuCount() +", " //order_count
 							+ "0, " //order_cash
 							+ "now());"); //order_date
 					parent.resetTotal();
 					cancelBtn.doClick();
 					JOptionPane.showMessageDialog(null, "ī  ϷǾϴ!");
 				}
 			}
 		});
 	}
 	
 	public void updateCenterPanel(){
 		
 		// ð Ʈ
 		Date dt = new Date();
 		SimpleDateFormat dtForm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String curTime = dtForm.format(dt.getTime());
 		dateLabel.setText(" Ͻ : \t\t" + curTime);
 		
 		//  Ʈ
 		String total = Integer.toString(this.total);
 		totalLabel.setText("  : \t" + total);
 		
 		//õ ޴ Ʈ
 		String recommand = "<html>õ ޴  ϴ.<br>";
 		recommand += PythonSyncModule.recommandString + "</html>";
 		recommandLabel.setText(recommand);
 		
 		//Ǹ  Ʈ
 		String rule = "<html>Ǹ <br>"
 				+ "1. ֹ  Ȯմϴ.<br>"
 				+ "2. ֹ   õ ޴ ֹ θ 庾ϴ.<br>"
 				+ "\tEx. Ƹ޸ī뿡   ô   Ű?<br>"
 				+ "3.  ݾ 帮  (ī, )  ϴ.<br>"
 				+ "4.  ϰ ǥ ߱ 帮, ٸ մ  ,"
 				+ " ̸ ظ մϴ.<br>"
 				+ "5. ֹ   Ȯմϴ."
 				+ "</html>";
 		ruleLabel.setText(rule);
 		
 		this.centerPanel.add(dateLabel);
 		this.centerPanel.add(label);
 		this.centerPanel.add(totalLabel);
 		this.centerPanel.add(label);
 		this.centerPanel.add(recommandLabel);
 		this.centerPanel.add(label);
 		//õ ޴
 		this.centerPanel.add(ruleLabel);
 		
 		
 	}
 	
 	public void setPayBackAction(ActionListener payBackActionListener){
 		this.cancelBtn.addActionListener(payBackActionListener);
 		addBottomBtns();
 		this.bottomPanel.add(cancelBtn);
 	}
 	
 	private void addBottomBtns(){
 		this.bottomPanel.add(cardBtn);
 		this.bottomPanel.add(cashBtn);
 		this.bottomPanel.add(backBtn);
 	}
 	
 	public void setTotal(int total){
 		this.total = total;
 		this.totalLabel.setText("  : " + Integer.toString(this.total));
 	}
 	
 	public void getRecommand(ArrayList<CustomerOrder> orderList){
		String recText = "õ ޴ ϱ⿡  ֹ մϴ.";
		
		
 		if(orderList.isEmpty()) //ֹ  
 			return;
 		if(MenuList.herbRecMenuList.isEmpty())
 			return;
 		//List    
 		
 		//׷ ʴٸ
		recText = "<html>";
 		boolean flag = false; //õ  ؽƮ Ÿ ϴ üũ
 		for(int i=0; i<orderList.size(); i++){
 			if(MenuList.herbRecMenuList.get(
 					orderList.get(i).getMenuName()) != null){
 				flag = true;
 				recText += orderList.get(i).getMenuName() + " Ͽ " +
 						MenuList.herbRecMenuList.get(orderList.get(i).getMenuName())
 						+ " õմϴ.<br>";
 			}
 		}
 		recText += "</html>";
 		if(flag)//õ ޴ ִ 
 			recommandLabel.setText(recText);
		else
			recommandLabel.setText("õ ޴ ϱ⿡  ֹ մϴ.");
 	}
 	
 	public void refreshDate(){
 		Date dt = new Date();
 		SimpleDateFormat dtForm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String curTime = dtForm.format(dt.getTime());
 		dateLabel.setText(" Ͻ : \t\t" + curTime);
 	}
 
 }
