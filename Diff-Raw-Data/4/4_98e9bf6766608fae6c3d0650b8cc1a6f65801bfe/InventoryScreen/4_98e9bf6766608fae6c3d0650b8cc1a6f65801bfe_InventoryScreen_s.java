 package dungeon.ui.screens;
 
 import dungeon.messages.LifecycleEvent;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.Item;
 import dungeon.client.Client;
 import dungeon.ui.messages.EquipWeaponCommand;
 import dungeon.ui.messages.ShowGame;
 import dungeon.ui.messages.ShowInventory;
 import dungeon.ui.messages.UseItemCommand;
 
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.MouseInputAdapter;
 import java.awt.GridLayout;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 public class InventoryScreen extends JPanel implements MessageHandler {
   private final Client client;
 
   private final ItemList itemList = new ItemList();
 
   private final ItemPanel itemPanel = new ItemPanel();
 
   private final JPanel actionBar = new JPanel();
 
   private final JButton equipButton = new JButton("Ausrüsten");
 
   private final JButton useButton = new JButton("Benutzen");
 
   private final JButton backButton = new JButton("Zurück");
 
   public InventoryScreen (Client client) {
     this.client = client;
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message == LifecycleEvent.INITIALIZE) {
       this.initialize();
     } else if (message instanceof ShowInventory) {
       this.reset();
     }
   }
 
   /**
    * Synchronize the items in the list with the items in the player's bag and enable/disable the buttons.
    */
   private void reset () {
     List<Item> items = this.client.getPlayer().getItems();
 
     this.itemList.setItems(items);
 
     if (this.itemList.getSelectedValue() == null) {
       this.equipButton.setEnabled(false);
       this.useButton.setEnabled(false);
     }
   }
 
   private void initialize () {
     this.setLayout(new GridLayout(1, 3, 10, 0));
     this.add(this.itemList);
     this.add(this.itemPanel);
     this.add(this.actionBar);
 
     this.actionBar.setLayout(new BoxLayout(this.actionBar, BoxLayout.Y_AXIS));
     this.actionBar.add(this.equipButton);
     this.actionBar.add(this.useButton);
     this.actionBar.add(this.backButton);
 
     this.useButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         if (!InventoryScreen.this.useButton.isEnabled()) {
           return;
         }
 
         Item item = InventoryScreen.this.itemList.getSelectedValue();
 
         InventoryScreen.this.client.send(new UseItemCommand(InventoryScreen.this.client.getPlayerId(), item));
       }
     });
 
     this.equipButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         if (!InventoryScreen.this.equipButton.isEnabled()) {
           return;
         }
 
         Item item = InventoryScreen.this.itemList.getSelectedValue();
 
         InventoryScreen.this.client.send(new EquipWeaponCommand(InventoryScreen.this.client.getPlayerId(), item));
       }
     });
 
     this.backButton.addMouseListener(new MouseInputAdapter() {
       @Override
       public void mouseClicked (MouseEvent e) {
         InventoryScreen.this.client.send(new ShowGame(InventoryScreen.this.client.getPlayerId()));
       }
     });
 
     this.itemList.addListSelectionListener(new ListSelectionListener() {
       @Override
       public void valueChanged (ListSelectionEvent listSelectionEvent) {
         Item item = InventoryScreen.this.itemList.getSelectedValue();
 
         InventoryScreen.this.itemPanel.setItem(item);
 
         if (item == null) {
           InventoryScreen.this.reset();
         } else {
           InventoryScreen.this.equipButton.setEnabled(item.getType().isEquipable());
           InventoryScreen.this.useButton.setEnabled(item.getType().isUseable());
         }
       }
     });
   }
 }
