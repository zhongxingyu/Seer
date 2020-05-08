 package ee.ut.math.tvt.salessystem.ui.tabs;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Time;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import ee.ut.math.tvt.BSS.SubmitOrderTab;
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.domain.data.OrderHeader;
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.exception.VerificationFailedException;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.panels.PurchaseItemPanel;
 import ee.ut.math.tvt.salessystem.util.HibernateUtil;
 /**
  * Encapsulates everything that has to do with the purchase tab (the tab
  * labelled "Point-of-sale" in the menu).
  */
 public class PurchaseTab {
 
   private static final Logger log = Logger.getLogger(PurchaseTab.class);
 
   private final SalesDomainController domainController;
 
   private static JButton newPurchase;
 
   private static JButton submitPurchase;
 
   private static JButton cancelPurchase;
 
   private static PurchaseItemPanel purchasePane;
 
   private SalesSystemModel model;
   
 
 
   public PurchaseTab(SalesDomainController controller,
       SalesSystemModel model)
   {
     this.domainController = controller;
     this.model = model;
   }
 
 
   /**
    * The purchase tab. Consists of the purchase menu, current purchase dialog and
    * shopping cart table.
    */
   public Component draw() {
     JPanel panel = new JPanel();
     panel.setName("POS-panel"); //dzh 2013-10-28 need for identify panel
 
     // Layout
     panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
     panel.setLayout(new GridBagLayout());
 
     // Add the purchase menu
     panel.add(getPurchaseMenuPane(), getConstraintsForPurchaseMenu());
 
     // Add the main purchase-panel
     purchasePane = new PurchaseItemPanel(model);
     panel.add(purchasePane, getConstraintsForPurchasePanel());
 
     return panel;
   }
 
 
 
 
   // The purchase menu. Contains buttons "New purchase", "Submit", "Cancel".
   private Component getPurchaseMenuPane() {
     JPanel panel = new JPanel();
 
     // Initialize layout
     panel.setLayout(new GridBagLayout());
     GridBagConstraints gc = getConstraintsForMenuButtons();
 
     // Initialize the buttons
     newPurchase = createNewPurchaseButton();
     submitPurchase = createConfirmButton();
     cancelPurchase = createCancelButton();
 
     // Add the buttons to the panel, using GridBagConstraints we defined above
     panel.add(newPurchase, gc);
     panel.add(submitPurchase, gc);
     panel.add(cancelPurchase, gc);
 
     return panel;
   }
 
 
   // Creates the button "New purchase"
   private JButton createNewPurchaseButton() {
     JButton b = new JButton("New purchase");
     b.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         newPurchaseButtonClicked();
       }
     });
 
     return b;
   }
 
   // Creates the "Confirm" button
   private JButton createConfirmButton() {
     JButton b = new JButton("Confirm");
     b.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         submitPurchaseButtonClicked();
       }
     });
     b.setEnabled(false);
 
     return b;
   }
 
 
   // Creates the "Cancel" button
   private JButton createCancelButton() {
     JButton b = new JButton("Cancel");
     b.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         cancelPurchaseButtonClicked();
       }
     });
     b.setEnabled(false);
 
     return b;
   }
 
 
 
 
 
   /* === Event handlers for the menu buttons
    *     (get executed when the buttons are clicked)
    */
 
 
   /** Event handler for the <code>new purchase</code> event. */
   protected void newPurchaseButtonClicked() {
     log.info("New sale process started");
     try {
       domainController.startNewPurchase();
       startNewSale();
     } catch (VerificationFailedException e1) {
       log.error(e1.getMessage());
     }
   }
 
 
   /**  Event handler for the <code>cancel purchase</code> event. */
   protected void cancelPurchaseButtonClicked() {
     log.info("Sale cancelled");
     try {
       domainController.cancelCurrentPurchase();
       endSale();
       model.getCurrentPurchaseTableModel().clear();
     } catch (VerificationFailedException e1) {
       log.error(e1.getMessage());
     }
   }
 
 
   /** Event handler for the <code>submit purchase</code> event. */
 	protected void submitPurchaseButtonClicked() {
 		log.info("Sale complete");
 		try {
 			log.debug("Contents of the current basket:\n"
 					+ model.getCurrentPurchaseTableModel());
 			
 			if(model.getCurrentPurchaseTableModel().getRowCount() < 1){
 				JOptionPane.showMessageDialog(null,
 						"Please add a item to submit a purchase!", "Warning",
 						JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 
 			double totalPrice = model.getCurrentPurchaseTableModel().getTotalAmount();
 			if (totalPrice < 0) {
 				JOptionPane.showMessageDialog(null,
 						"Total amount cannot be below zero!", "Warning",
 						JOptionPane.WARNING_MESSAGE);
 				return;
 			}
 			SubmitOrderTab submitordertab = new SubmitOrderTab(totalPrice);
 			submitordertab.setVisible(true);
 			if (submitordertab.ModalResult) {
 				domainController.submitCurrentPurchase(model.getCurrentPurchaseTableModel().getTableRows());
 				saveSale();
 				endSale();
 				model.getCurrentPurchaseTableModel().clear();
 			}
 		} 
 		catch (VerificationFailedException e1) {
 			log.error(e1.getMessage());
 		}
 	}
   protected Boolean saveSale() {
 		Boolean result = false;
 		Session session = HibernateUtil.currentSession();
 		try {
 			session.beginTransaction();
 			Date dt = new Date();
 			Time t = new Time(dt.getTime());
 			OrderHeader orderHeader = new OrderHeader();
 			orderHeader.setId(this.model.getHistoryTableModel().genId());
 			orderHeader.setDate(dt);
 			orderHeader.setTime(t);
 			orderHeader.setSum(this.model.getCurrentPurchaseTableModel().getTotalAmount());
 						
 			
 			List <SoldItem>  myList = this.model.getCurrentPurchaseTableModel().getTableRows();
 			Set<SoldItem> myList2 = new HashSet<SoldItem>(myList);
 			orderHeader.setOrderDetail(myList2);
 
 			session.save(orderHeader);
 			for (SoldItem item : orderHeader.getOrderDetail()) {
 				item.setSaleId(orderHeader);
 				session.save(item);
 			}
 
 			this.model.getHistoryTableModel().addItem(orderHeader);
 
 			for (SoldItem item : this.model.getCurrentPurchaseTableModel().getTableRows()) {
 				if (item.getQuantity().intValue() > 0) {
 					log.info("Product name: " + item.getName() + "quantity: "
 							+ item.getQuantity().intValue());
 					this.model.getWarehouseTableModel().addQuantity(item.getId(), -1 * item.getQuantity());
 				}
 			}
			session.getTransaction().commit();
 			result = true;
 		} 
 		catch (Exception E) {
 			log.error(E.getMessage());
 			session.getTransaction().rollback();
 		}
 		return result;
   }
 
   /* === Helper methods that bring the whole purchase-tab to a certain state
    *     when called.
    */
 
   // switch UI to the state that allows to proceed with the purchase
   private void startNewSale() {
     purchasePane.reset();
 
     purchasePane.setEnabled(true);
     submitPurchase.setEnabled(true);
     cancelPurchase.setEnabled(true);
     newPurchase.setEnabled(false);
   }
 
   // switch UI to the state that allows to initiate new purchase
   public static void endSale() {
     purchasePane.reset();    
 
     cancelPurchase.setEnabled(false);
     submitPurchase.setEnabled(false);
     newPurchase.setEnabled(true);
     purchasePane.setEnabled(false);
   }
 
 
 
 
   /* === Next methods just create the layout constraints objects that control the
    *     the layout of different elements in the purchase tab. These definitions are
    *     brought out here to separate contents from layout, and keep the methods
    *     that actually create the components shorter and cleaner.
    */
 
   private GridBagConstraints getConstraintsForPurchaseMenu() {
     GridBagConstraints gc = new GridBagConstraints();
 
     gc.fill = GridBagConstraints.HORIZONTAL;
     gc.anchor = GridBagConstraints.NORTH;
     gc.gridwidth = GridBagConstraints.REMAINDER;
     gc.weightx = 1.0d;
     gc.weighty = 0d;
 
     return gc;
   }
 
 
   private GridBagConstraints getConstraintsForPurchasePanel() {
     GridBagConstraints gc = new GridBagConstraints();
 
     gc.fill = GridBagConstraints.BOTH;
     gc.anchor = GridBagConstraints.NORTH;
     gc.gridwidth = GridBagConstraints.REMAINDER;
     gc.weightx = 1.0d;
     gc.weighty = 1.0;
 
     return gc;
   }
 
 
   // The constraints that control the layout of the buttons in the purchase menu
   private GridBagConstraints getConstraintsForMenuButtons() {
     GridBagConstraints gc = new GridBagConstraints();
 
     gc.weightx = 0;
     gc.anchor = GridBagConstraints.CENTER;
     gc.gridwidth = GridBagConstraints.RELATIVE;
 
     return gc;
   }
 
 }
