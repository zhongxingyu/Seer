 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.GridLayout;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import exceptions.GUINoSuchPanelException;
 
 public class CheckoutPanel extends DisplayPanel {
 
     /**
      * 
      */
     private static final long serialVersionUID = -44778006080533928L;
     private static int curPanelNum;
     private static int totalPanelsNum;
     private static JPanel cartMainPanel;
     private static CheckoutActionsPanel cartActionsPanel;
     private static CheckoutProgressPanel cartProgressPanel;
 
     public static final int CART = 0;
     public static final int PAYMENT = 1;
     public static final int VERIFY = 2;
     public static final int THANKYOU = 3;
 
     public CheckoutPanel(){
 
         curPanelNum = 0;
         totalPanelsNum = 0;
         this.setLayout(new BorderLayout());
 
         cartMainPanel = new JPanel();
         cartMainPanel.setLayout(new CardLayout());
 
         /*
          * Add the payment panels in order
          */
        this.addIndexedPanel(new JScrollPane(new CheckoutMyCartPanel()), CART);
         this.addIndexedPanel(new CheckoutPaymentPanel(), PAYMENT);
         this.addIndexedPanel(new CheckoutVerifyPanel(), VERIFY);
         this.addIndexedPanel(new CheckoutThankyouPanel(), THANKYOU);
 
         this.add(cartMainPanel, BorderLayout.CENTER);
 
         JPanel bottomPanel = new JPanel();
         bottomPanel.setLayout(new GridLayout(0,1));
         cartActionsPanel = new CheckoutActionsPanel();
         bottomPanel.add(cartActionsPanel);
         cartProgressPanel = new CheckoutProgressPanel();
         bottomPanel.add(cartProgressPanel);
 
         this.add(bottomPanel, BorderLayout.SOUTH);
 
 
     }
 
     private void addIndexedPanel(final JComponent panel, final int stepNo){
 
         cartMainPanel.add(panel, Integer.toString(stepNo));
         totalPanelsNum++;
 
     }
 
     private static void updateStage(int inCheckoutStage)
     {
         // Updates the actions panel and progress panel where appropriate
 
         switch (inCheckoutStage)
         {
         case(CART):		CheckoutActionsPanel.loadCartActions(); 	cartProgressPanel.loadCartProgress(); 		break;	// Cart
         case(PAYMENT):	CheckoutActionsPanel.loadPaymentActions(); 	cartProgressPanel.loadPaymentProgress();	break;	// Payment
         case(VERIFY):	CheckoutActionsPanel.loadVerifyActions(); 	cartProgressPanel.loadVerifyProgress();		break;	// Verify Cart/Payment
         case(THANKYOU):	CheckoutActionsPanel.loadThankyouActions(); cartProgressPanel.loadThankyouProgress();	break;	// Thankyou
         default: 		throw new IllegalArgumentException("Invalid checkout stage.");								// Throw exception, invalid checkout stage
         }
 
     }
 
 
     public static void nextPaymentStep(){
 
         if(curPanelNum < totalPanelsNum){
 
             curPanelNum++;
             CardLayout cl = (CardLayout) cartMainPanel.getLayout();
             cl.show(cartMainPanel, Integer.toString(curPanelNum));
 
             updateStage(curPanelNum);
 
         }
     }
 
     public static void previousPaymentStep(){
 
         if(curPanelNum > -1){
 
             curPanelNum--;
             CardLayout cl = (CardLayout) cartMainPanel.getLayout();
             cl.show(cartMainPanel, Integer.toString(curPanelNum));
 
             updateStage(curPanelNum);
         }
     }
 
     public static void jumpToPaymentStep(int stepNo) throws GUINoSuchPanelException{
 
         if(stepNo > totalPanelsNum){
 
             throw new GUINoSuchPanelException();
 
         }
 
         CardLayout cl = (CardLayout) cartMainPanel.getLayout();
         cl.show(cartMainPanel, Integer.toString(stepNo));
 
         curPanelNum = stepNo;
 
         updateStage(curPanelNum);
 
     }
 
 }
