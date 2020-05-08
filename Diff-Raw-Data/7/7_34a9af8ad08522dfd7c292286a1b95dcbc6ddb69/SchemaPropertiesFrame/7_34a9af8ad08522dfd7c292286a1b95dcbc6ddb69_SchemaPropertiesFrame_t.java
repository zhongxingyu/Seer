 /*
  * Copyright (C) 2006-2010 Thomas Chemineau
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package net.aepik.casl.ui.ldap;
 
 import net.aepik.casl.core.ldap.Schema;
 import net.aepik.casl.core.ldap.SchemaObject;
 import net.aepik.casl.core.ldap.SchemaValue;
 import net.aepik.casl.core.util.Config;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Properties;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Permet d'avoir un accès visuel aux propriétés du schéma.
  * Il est possible d'en rajouter, d'en supprimer, d'en modifier.
  * La lecture des propriétés du schéma dans les fichiers sont réalisées par
  * les parseurs de lecture. C'est eux qui fixent les noms des propriétés.
  * Pour plus d'informations, voir l'aide.
  */
 public class SchemaPropertiesFrame extends JDialog implements ActionListener, ListSelectionListener, WindowListener
 {
 
 	private static final long serialVersionUID = 0;
 
 	/**
 	 * Le schéma
 	 */
 	private Schema schema;
 
 	/**
 	 * Objects identifiers.
 	 */
 	private Properties objectsIdentifiers;
 
 	/**
 	 * Objects identifiers list.
 	 */
 	private JList objectsIdentifiersList;
 
 	/**
 	 * Les propriétés du schéma
 	 */
 	private Properties properties;
 
 	/**
 	 * La liste des propriétés
 	 */
 	private JList propertiesList;
 
 	/**
 	 * Le bouton Ok
 	 */
 	private JButton boutonOk = new JButton("Valider");
 
 	/**
 	 * Le bouton Annuler
 	 */
 	private JButton boutonAnnuler = new JButton("Annuler");
 
 	/**
 	 * Le bouton ajouter un object identifier
 	 */
 	private JButton boutonAjouterObjectIdentifier = new JButton("Ajouter");
 
 	/**
 	 * Le bouton supprimer un object identifier
 	 */
 	private JButton boutonSupprimerObjectIdentifier = new JButton("Supprimer");
 
 	/**
 	 * Le bouton modifier un object identifier
 	 */
 	private JButton boutonModifierObjectIdentifier = new JButton("Modifier");
 
 	/**
 	 * Le bouton ajouter une propriété
 	 */
 	private JButton boutonAjouterProperty = new JButton("Ajouter");
 
 	/**
 	 * Le bouton supprimer une propriété
 	 */
 	private JButton boutonSupprimerProperty = new JButton("Supprimer");
 
 	/**
 	 * Le bouton modifier une propriété
 	 */
 	private JButton boutonModifierProperty = new JButton("Modifier");
 
 	/**
 	 * Build an new SchemaPropertiesFrame object.
 	 * @param parent The parent window.
 	 * @param s The corresponding schema object.
 	 * @param sName The name of the schema.
 	 */
 	public SchemaPropertiesFrame ( Window parent, Schema s, String sName )
 	{
 		super();
 		setTitle("Propriétés du schéma " + sName);
 		setModal(true);
 		setSize(400, 440);
 		setResizable(false);
 		setLocationRelativeTo(parent);
 
 		schema = s;
 		properties = (Properties) schema.getProperties().clone();
 		propertiesList = new JList();
 		objectsIdentifiers = (Properties) schema.getObjectsIdentifiers().clone();;
 		objectsIdentifiersList = new JList();
 
 		updateList();
 		initFrame();
 		updateButtons();
 	}
 
 	/**
 	 * Manage actions to allow to modify data of this object.
 	 * @param e An action of a panel's element.
 	**/
 	public void actionPerformed ( ActionEvent e )
 	{
 		Object o = e.getSource();
 		//
 		// On souhaite ajouter une nouvelle valeur.
 		// On fait appel à une classe interne pour afficher notre
 		// boîte de saisie des données.
 		//
 		if (o == boutonAjouterProperty)
 		{
 			SchemaPropertyEditorFrame f = new SchemaPropertyEditorFrame(
 				this,
 				properties,
 				null,
 				null
 			);
 			f.setVisible(true);
 		}
 		//
 		// Ici, nous affichons notre boîte de saisie des données, avec
 		// les valeurs pré-remplies.
 		//
 		else if (o == boutonModifierProperty)
 		{
 			String str = propertiesList.getSelectedValue().toString();
 			if (str != null)
 			{
 				int index = str.indexOf(':');
 				SchemaPropertyEditorFrame f = new SchemaPropertyEditorFrame(
 					this,
 					properties,
 					str.substring(0, index).trim(),
 					str.substring(index+1).trim()
 				);
 				f.setVisible(true);
 			}
 		}
 		//
 		// On supprime l'élément en cours de sélection.
 		//
 		else if (o == boutonSupprimerProperty)
 		{
 			String str = propertiesList.getSelectedValue().toString();
 			if (str != null)
 			{
 				int index = str.indexOf(':');
 				properties.remove(str.substring(0, index).trim());
 				updateList();
 			}
 		}
 		//
 		// On souhaite ajouter un nouveau object identifier
 		// On fait appel à une classe interne pour afficher notre
 		// boîte de saisie des données.
 		//
 		if (o == boutonAjouterObjectIdentifier)
 		{
 			SchemaPropertyEditorFrame f = new SchemaPropertyEditorFrame(
 				this,
 				objectsIdentifiers,
 				null,
 				null
 			);
 			f.setVisible(true);
 		}
 		//
 		// Ici, nous affichons notre boîte de saisie des données, avec
 		// les valeurs pré-remplies de l'object identifier.
 		//
 		else if (o == boutonModifierObjectIdentifier)
 		{
 			String str = objectsIdentifiersList.getSelectedValue().toString();
 			if (str != null)
 			{
 				int index = str.indexOf(' ');
 				SchemaPropertyEditorFrame f = new SchemaPropertyEditorFrame(
 					this,
 					objectsIdentifiers,
 					str.substring(0, index).trim(),
 					str.substring(index+1).trim()
 				);
 				f.setVisible(true);
 			}
 		}
 		//
 		// On supprime l'object identifier en cours de sélection.
 		//
 		else if (o == boutonSupprimerObjectIdentifier)
 		{
 			String str = objectsIdentifiersList.getSelectedValue().toString();
 			if (str != null)
 			{
 				int index = str.indexOf(' ');
 				objectsIdentifiers.remove(str.substring(0, index).trim());
 				updateList();
 			}
 		}
 		//
 		// Bouton annuler
 		//
 		else if (o == boutonAnnuler)
 		{
 			windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
 		}
 		//
 		// Bouton valider, on enregistre les nouvelles valeurs dans le
 		// schéma.
 		//
 		else if (o == boutonOk)
 		{
 			schema.setProperties(properties);
 			schema.setObjectsIdentifiers(objectsIdentifiers);
 			windowClosing(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
 		}
 	}
 
 	public void valueChanged ( ListSelectionEvent e )
 	{
 		Object o = (e != null) ? e.getSource() : null;
 		if (o == propertiesList || o == objectsIdentifiersList)
 		{
 			updateButtons();
 		}
 	}
 
 	public void windowActivated ( WindowEvent e )
 	{
 	}
 
 	public void windowClosed ( WindowEvent e )
 	{
 	}
 
  	public void windowClosing ( WindowEvent e )
 	{
 		e.getComponent().setVisible(false);
 	}
 
 	public void windowDeactivated ( WindowEvent e )
 	{
 	}
 
 	public void windowDeiconified ( WindowEvent e )
 	{
 	}
 
 	public void windowIconified ( WindowEvent e )
 	{
 	}
 
 	public void windowOpened ( WindowEvent e )
 	{
 	}
 
 	/**
 	 * Initialize all environment for this frame.
 	 */
 	private void initFrame ()
 	{
 		//
 		// Panel boutons du bas (valider/annuler)
 		//
 		JPanel boutonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		boutonsPanel.add(boutonOk);
 		boutonsPanel.add(boutonAnnuler);
 
 		//
 		// Panel de description
 		//
 		JTextArea textAreaDescription = new JTextArea(
 			"Les propriétés du schéma peuvent être modifiées. Elles ne sont"
 			+ " pas pré-définies et sont fonction de la syntaxe employée"
			+ " par ce schéma."
 		);
 		textAreaDescription.setEditable(false);
 		textAreaDescription.setLineWrap(true);
 		textAreaDescription.setWrapStyleWord(true);
 		textAreaDescription.setFont((new JLabel()).getFont());
 		textAreaDescription.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
 		textAreaDescription.setBackground((new JLabel()).getBackground());
 
 		//
 		// Panel liste des propriétés
 		//
 		JPanel propertiesBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		propertiesBoutons.add(boutonAjouterProperty);
 		propertiesBoutons.add(boutonSupprimerProperty);
 		propertiesBoutons.add(boutonModifierProperty);
 
 		JScrollPane propertiesScroller = new JScrollPane(propertiesList);
 		propertiesScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		propertiesList.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
		propertiesList.setVisibleRowCount(5);
 		propertiesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		JPanel propertiesPanel = new JPanel(new BorderLayout());
 		propertiesPanel.add(propertiesScroller, BorderLayout.CENTER);
 		propertiesPanel.add(propertiesBoutons, BorderLayout.SOUTH);
 		propertiesPanel.setBorder(BorderFactory.createCompoundBorder(
 			BorderFactory.createEmptyBorder(5, 5, 1, 5),
 			BorderFactory.createCompoundBorder( 
 				BorderFactory.createTitledBorder(" Liste des propriétés "),
 				BorderFactory.createEmptyBorder(0, 5, 0, 5)
 			)
 		));
 
 		//
 		// Objects identifiers panel
 		//
 		JPanel objectsIdentifiersBoutons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
 		objectsIdentifiersBoutons.add(boutonAjouterObjectIdentifier);
 		objectsIdentifiersBoutons.add(boutonSupprimerObjectIdentifier);
 		objectsIdentifiersBoutons.add(boutonModifierObjectIdentifier);
 
 		JScrollPane objectsIdentifiersScroller = new JScrollPane(objectsIdentifiersList);
 		objectsIdentifiersScroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		objectsIdentifiersList.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
 		objectsIdentifiersList.setVisibleRowCount(4);
 		objectsIdentifiersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		JPanel objectsIdentifiersPanel = new JPanel(new BorderLayout());
 		objectsIdentifiersPanel.add(objectsIdentifiersScroller, BorderLayout.CENTER);
 		objectsIdentifiersPanel.add(objectsIdentifiersBoutons, BorderLayout.SOUTH);
 		objectsIdentifiersPanel.setBorder(BorderFactory.createCompoundBorder(
 			BorderFactory.createEmptyBorder(5, 5, 1, 5),
 			BorderFactory.createCompoundBorder(
 				BorderFactory.createTitledBorder(" Liste des identifiants d'objet "),
 				BorderFactory.createEmptyBorder(0, 5, 0, 5)
 			)
 		));
 
 		//
 		// Organisation générales
 		//
 		JPanel mainPanel = new JPanel(new BorderLayout());
 		mainPanel.add(propertiesPanel, BorderLayout.NORTH);
 		mainPanel.add(objectsIdentifiersPanel, BorderLayout.CENTER);
 
 		JPanel mainPanelContainer = new JPanel(new BorderLayout());
 		mainPanelContainer.add(textAreaDescription, BorderLayout.NORTH);
 		mainPanelContainer.add(mainPanel, BorderLayout.CENTER);
 		mainPanelContainer.add(boutonsPanel, BorderLayout.SOUTH);
 		mainPanelContainer.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 1));
 
 		getContentPane().add(mainPanelContainer);
 
 		//
 		// Listeners
 		//
 		addWindowListener(this);
 		propertiesList.addListSelectionListener(this);
 		objectsIdentifiersList.addListSelectionListener(this);
 		boutonOk.addActionListener(this);
 		boutonAnnuler.addActionListener(this);
 		boutonAjouterProperty.addActionListener(this);
 		boutonModifierProperty.addActionListener(this);
 		boutonSupprimerProperty.addActionListener(this);
 		boutonAjouterObjectIdentifier.addActionListener(this);
 		boutonModifierObjectIdentifier.addActionListener(this);
 		boutonSupprimerObjectIdentifier.addActionListener(this);
 	}
 
 	/**
 	 * Update buttons status.
 	 */
 	private void updateButtons ()
 	{
 		if (propertiesList.getSelectedIndex() != -1)
 		{
 			boutonModifierProperty.setEnabled(true);
 			boutonSupprimerProperty.setEnabled(true);
 		}
 		else
 		{
 			boutonModifierProperty.setEnabled(false);
 			boutonSupprimerProperty.setEnabled(false);
 		}
 		if (objectsIdentifiersList.getSelectedIndex() != -1)
 		{
 			boutonModifierObjectIdentifier.setEnabled(true);
 			boutonSupprimerObjectIdentifier.setEnabled(true);
 		}
 		else
 		{
 			boutonModifierObjectIdentifier.setEnabled(false);
 			boutonSupprimerObjectIdentifier.setEnabled(false);
 		}
 	}
 
 	/**
 	 * Update lists contents
 	 */
 	private void updateList ()
 	{
 		DefaultListModel propertiesModel = new DefaultListModel();
 		DefaultListModel objectsIdentifiersModel = new DefaultListModel();
 		for (Enumeration keys = properties.propertyNames(); keys.hasMoreElements();)
 		{
 			String key = (String) keys.nextElement();
 			String value = properties.getProperty(key);
 			propertiesModel.addElement(key + ":" + value);
 		}
 		for (Enumeration keys = objectsIdentifiers.propertyNames(); keys.hasMoreElements();)
 		{
 			String key = (String) keys.nextElement();
 			String value = objectsIdentifiers.getProperty(key);
 			objectsIdentifiersModel.addElement(key + " " + value);
 		}
 		propertiesList.setModel(propertiesModel);
 		objectsIdentifiersList.setModel(objectsIdentifiersModel);
 		propertiesList.repaint();
 		objectsIdentifiersList.repaint();
 	}
 
 	private class SchemaPropertyEditorFrame extends JDialog implements ActionListener
 	{
 
 		private static final long serialVersionUID = 0;
 
 		private JButton boutonOk = new JButton("    Ok    ");
 
 		private JButton boutonAnnuler = new JButton("Annuler");
 
 		private JTextField varName;
 
 		private String varNameDefault;
 
 		private JTextField varValue;
 
 		private String varValueDefault;
 
 		private Properties list;
 
 		public SchemaPropertyEditorFrame ( JDialog f, Properties list, String varname, String varvalue )
 		{
 			super();
 			varName = new JTextField(varname);
 			varNameDefault = varname;
 			varValue = new JTextField(varvalue);
 			varValueDefault = varvalue;
 			this.list = list;
 			setTitle("Editer");
 			setModal(true);
 			setResizable(false);
 			setLocationRelativeTo(f);
 			initFrame();
 		}
 
 		public void actionPerformed ( ActionEvent e )
 		{
 			Object o = e.getSource();
 			//
 			// Bouton annuler boîte de saisie.
 			//
 			if (o == boutonAnnuler)
 			{
 				SchemaPropertiesFrame.this.windowClosing(new WindowEvent(
 					this,
 					WindowEvent.WINDOW_CLOSING
 				));
 			}
 			//
 			// Bouton de validation. On mets à jour les valeurs de la liste
 			// de la classe parente.
 			//
 			else if (o == boutonOk)
 			{
 				if (!varName.getText().equals(varNameDefault))
 				{
 					this.list.remove(varNameDefault);
 				}
 				this.list.setProperty(varName.getText(), varValue.getText());
 				SchemaPropertiesFrame.this.updateList();
 				SchemaPropertiesFrame.this.windowClosing(new WindowEvent(
 					this,
 					WindowEvent.WINDOW_CLOSING
 				));
 			}
 
 		}
 
 		/**
 		 * Initialize this internal frame.
 		 */
 		private void initFrame ()
 		{
 			JTextArea inputsDescription = new JTextArea("Veuillez remplir les champs ci-dessous.");
 			inputsDescription.setEditable(false);
 			inputsDescription.setLineWrap(true);
 			inputsDescription.setWrapStyleWord(true);
 			inputsDescription.setFont( (new JLabel()).getFont() );
 			inputsDescription.setBorder( BorderFactory.createEmptyBorder( 2, 3, 6, 3 ) );
 			inputsDescription.setBackground( (new JLabel()).getBackground() );
 
 			JPanel inputsPanel = new JPanel( new BorderLayout() );
 			inputsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 2, 0, 2 ) );
 
 			JPanel p1 = new JPanel( new BorderLayout() );
 			p1.add( new JLabel( "Nom de la variable :" ), BorderLayout.NORTH );
 			p1.add( varName, BorderLayout.CENTER );
 			p1.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
 			inputsPanel.add( p1, BorderLayout.NORTH );
 			JPanel p2 = new JPanel( new BorderLayout() );
 			p2.add( new JLabel( "Valeur de la variable :" ), BorderLayout.NORTH );
 			p2.add( varValue, BorderLayout.CENTER );
 			p2.setBorder( BorderFactory.createEmptyBorder( 5, 0, 0, 0 ) );
 			inputsPanel.add( p2, BorderLayout.CENTER );
 
 			JPanel inputsPanelContainer = new JPanel( new BorderLayout() );
 			inputsPanelContainer.add( inputsPanel, BorderLayout.NORTH );
 
 			JPanel boutonsPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
 			boutonsPanel.add( boutonOk );
 			boutonsPanel.add( boutonAnnuler );
 
 			JPanel mainPanel = new JPanel( new BorderLayout() );
 			mainPanel.add( inputsDescription, BorderLayout.NORTH );
 			mainPanel.add( inputsPanelContainer, BorderLayout.CENTER );
 			mainPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 1, 5 ) );
 
 			JPanel mainPanelContainer = new JPanel( new BorderLayout() );
 			mainPanelContainer.add( mainPanel, BorderLayout.NORTH );
 			mainPanelContainer.add( boutonsPanel, BorderLayout.SOUTH );
 			mainPanelContainer.setBorder( BorderFactory.createEmptyBorder( 2, 1, 1, 1 ) );
 
 			getContentPane().add( mainPanelContainer );
 
 			setSize( 300, 200 );
 			setLocationRelativeTo( SchemaPropertiesFrame.this );
 
 			addWindowListener( SchemaPropertiesFrame.this );
 			boutonOk.addActionListener(this);
 			boutonAnnuler.addActionListener(this);
 		}
 
 	}
 }
