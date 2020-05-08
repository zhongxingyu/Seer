 package com.arenz.spriteeditor.ui;
 
 
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.TitledBorder;
 
 import com.arenz.spriteeditor.controller.SpriteSelectionController.CategorySelectionListener;
 import com.arenz.spriteeditor.model.SpriteCategories;
 import com.arenz.spriteeditor.model.SpriteCategory;
 import com.arenz.spriteeditor.ui.components.CategoryButton;
 
 	public class SpriteSelectionView {
 		private JPanel spriteSelectionPanel = new JPanel();
 		private JScrollPane categoriesScrollPane;
 		private JScrollPane spritesScrollPane;
 		private JPanel categoriesPanel = new JPanel();
 		private JPanel spritesPanel = new JPanel();
 		private CategoryButton selectedCategory;
 		private JButton selectedSprite;
 		
 		private SpriteCategories categories;
 		
 		public SpriteSelectionView(SpriteCategories categories)
 		{
 			this.categories = categories;
 			spriteSelectionPanel.setBorder(BorderFactory.createRaisedBevelBorder());
 			
 			spriteSelectionPanel.setLayout(new BorderLayout());
 			categoriesPanel = new JPanel();
 			spritesPanel = new JPanel();
 			
 			categoriesScrollPane = createScrollPane("Categories", categoriesPanel);
 			spritesScrollPane = createScrollPane("Sprites", spritesPanel);
 			
 			verifyCategoryNumber();
 			spriteSelectionPanel.add(spritesScrollPane, BorderLayout.CENTER);
 		}
 		
 		private JScrollPane createScrollPane(String titre, JPanel contenu)
 		{
 			JScrollPane jsp = new JScrollPane(contenu);
 			jsp.setBorder(new TitledBorder(BorderFactory.createLoweredBevelBorder(), titre));
 			
 			jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 			jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
 
 			return jsp;
 		}
 /*
 		public void updateCategories(ArrayList<String> categories)
 	    {
 			categoriesPanel.removeAll();
 			selectedCategory = null;
 		    for (String categorie : categories)
 		    {
 		    	JButton bCategorie = new JButton(categorie);
 		    	bCategorie.addActionListener(new ActionListener(){
 
 					@Override
 	                public void actionPerformed(ActionEvent arg0)
 	                {
 						// déselection de l'élément sélectionné
 						if (selectedSprite != null)
 						{
 							selectedSprite.setEnabled(true);
 							ctrlPS.deselectionnerElement(selectedSprite.getName());
 							selectedSprite = null;
 						}
 
 						JButton boutonClique = ((JButton)arg0.getSource());
 		                if (selectedCategory != null)
 		                {
 		                	selectedCategory.setEnabled(true);
 		                }
 		                
 		                selectedCategory = boutonClique;
 		                boutonClique.setEnabled(false);
 		                
 		                ctrlPS.selectionnerCategorie(boutonClique.getText());
 	                }
 		    		
 		    	});
 		    	categoriesPanel.add(bCategorie);
 		    }
 		    
 		    verifyCategoryNumber();
 		    categoriesPanel.revalidate();
 		    categoriesPanel.repaint();
 		    categoriesScrollPane.revalidate();
 		    categoriesScrollPane.repaint();
 		    spritesPanel.revalidate();
 		    spritesPanel.repaint();
 		    spritesScrollPane.revalidate();
 		    spritesScrollPane.repaint();
 		    spriteSelectionPanel.revalidate();
 		    spriteSelectionPanel.repaint();
 		}
 */		
 		private void verifyCategoryNumber()
 		{
 			int nbrCategories = categories.getCategoriesCount();
 			if (nbrCategories > 1)
 			{
 				spriteSelectionPanel.add(categoriesScrollPane, BorderLayout.NORTH);
 			}
 		}
 /*
 		public void selectionnerCategorie(SpriteCategory categorie)
 	    {
 			spritesPanel.removeAll();
 			
 			Component[] composantspCategories = categoriesPanel.getComponents();
 			for (Component composant : composantspCategories)
 			{
 				if (composant instanceof JButton)
 				{
 					JButton bouton = ((JButton) composant);
 					if (bouton.getText().equalsIgnoreCase(categorie.getNom()) && bouton.isEnabled())
 					{
 						((JButton) composant).doClick();
 					}
 				}
 			}
 			
 			ArrayList<DisplayableElement> elements = categorie.getElements();
 		    for (DisplayableElement element : elements)
 		    {
 		    	JButton bElement = new JButton(element.getIcon());
 		    	bElement.setName(element.getNom());
 		    	bElement.addActionListener(new ActionListener(){
 
 					@Override
 	                public void actionPerformed(ActionEvent arg0)
 	                {
 						JButton boutonClique = ((JButton)arg0.getSource());
 		                if (selectedSprite != null)
 		                {
 		                	selectedSprite.setEnabled(true);
 		                }
 		                
 		                selectedSprite = boutonClique;
 		                boutonClique.setEnabled(false);
 		                ctrlPS.selectionnerElement(boutonClique.getName());
 	                }
 		    		
 		    	});
 		    	spritesPanel.add(bElement);
 		    }
 		    
 		    spritesPanel.revalidate();
 		    spritesPanel.repaint();
 		    spritesScrollPane.revalidate();
 		    spritesScrollPane.repaint();
 		    spriteSelectionPanel.revalidate();
 		    spriteSelectionPanel.repaint();
 	    }
 
 		public void deselectionnerCategories()
 	    {
 			this.selectedCategory.setEnabled(true);
 			this.selectedCategory = null;
 			this.selectionnerCategorie(null);
 	    }
 */
 
 		public Component getMainComponent() {
 			return spriteSelectionPanel;
 		}
 
 		public void addCategory(SpriteCategory category, ActionListener listener) {
 			CategoryButton catButton = new CategoryButton(category);
 			catButton.addActionListener(listener);
 			categoriesPanel.add(catButton);
 			
 			verifyCategoryNumber();
 			spriteSelectionPanel.revalidate();
 		}
 
 		public void selectCategory(SpriteCategory category) {
 			deselectSelectedCategory();
 			CategoryButton categoryButton = retrieveCategoryButton(category);
 			this.selectedCategory = categoryButton;
 			System.out.println("Category " + category.getName() + " selected");
 		}
 
 		private CategoryButton retrieveCategoryButton(SpriteCategory category) {
 			CategoryButton catButton = null;
 			int i = 0;
 			while (i < categoriesPanel.getComponentCount() && catButton == null) {
 				Component component = categoriesPanel.getComponent(i);
 				catButton = getCategoryButtonIfSameCategory(component, category);
				i ++;
 			}
 			return catButton;
 		}
 		
 		private CategoryButton getCategoryButtonIfSameCategory(Component component, SpriteCategory category) {
 			CategoryButton catButton = null;
 			
 			if (component instanceof CategoryButton) {
 				CategoryButton catChecked = (CategoryButton) component;
 				if (catChecked.isSameCategory(category)) {
 					catButton = catChecked;
 				}
 			}
 			
 			return catButton;
 		}
 
 		private void deselectSelectedCategory() {
 			if (selectedCategory != null) {
				selectedCategory.setEnabled(true);
 			}
 		}
 		
 		public void addSpriteSelectionListener(SpriteCategory category, ActionListener listener) {
 			CategoryButton catButton = retrieveCategoryButton(category);	
 			catButton.addActionListener(listener);
 		}
 }
