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
 
 
 package net.aepik.casl.core.ldap;
 
 import net.aepik.casl.core.History;
 import java.io.File;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Observable;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.jar.*;
 
 /**
  * Cet objet est un schéma LDAP au sens large. Il doit contenir des définitions
  * de type objectClass ou AttributeType, par l'intermédiaire de l'objet Java
  * SchemaObject.
  */
 public class Schema extends Observable
 {
 
 	/**
 	 * L'ensemble des objets du schema
 	 */
 	private Hashtable<String,SchemaObject> objets;
 
 	/**
 	 * Schema properties
 	 */
 	private Properties proprietes;
 
 	/**
 	 * La syntaxe du schema
 	 */
 	private SchemaSyntax syntax;
 
 	/**
 	 * Indique si la syntaxe a changée
 	 */
 	private boolean isSyntaxChanged;
 
 	/**
 	 * L'historique d'ordre des objets
 	 */
 	private History objectsOrder;
 
 	/**
 	 * A random object identifier prefix
 	 */
 	private String objectIdentifierPrefix;
 
 	/**
 	 * A random object identifier suffix
 	 */
 	private String objectIdentifierSuffix;
 
 	/**
 	 * Construit un schema vide.
 	 */
 	public Schema ( SchemaSyntax s )
 	{	
 		objets = new Hashtable<String,SchemaObject>();
 		proprietes = new Properties();
 		syntax = s;
 		isSyntaxChanged = false;
 		objectsOrder = new History();
 		objectIdentifierPrefix = null;
 		objectIdentifierSuffix = null;
 	}
 
 	/**
 	 * Construit un schema en le remplissant
 	 * d'objets SchemaObject.
 	 * @param objets Des objets du schema.
 	 */
 	public Schema ( SchemaSyntax s, Vector<SchemaObject> objs )
 	{
 		objets = new Hashtable<String,SchemaObject>() ;
 		proprietes = new Properties();
 		syntax = s ;
 		isSyntaxChanged = false ;
 		addObjects( objs );
 		objectIdentifierPrefix = null;
 		objectIdentifierSuffix = null;
 	}
 
 	/**
 	 * Ajoute un objet au schema.
 	 * @param o Un objet du schema.
 	 * @return True si l'objet n'existe pas déjà , false sinon.
 	 */
 	public boolean addObject ( SchemaObject o )
 	{
 		try
 		{
			if (o == null || o.getId() == null)
			{
				return false;
			}
 			if (!contains(o.getId()))
 			{
 				objets.put(o.getId(), o);
 				objectsOrder.insertElementInLastPosition(o);
 				notifyUpdates();
 				return true;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Ajoute un ensemble d'objets au schema.
 	 * @param v Un ensemble d'objets du schema.
 	 * @return True si l'opération a réussi, false sinon.
 	 *		Si l'opération n'a pas réussi, aucun objet n'aura été ajouté.
 	 */
 	public boolean addObjects ( SchemaObject[] v )
 	{
 		boolean ok = true;
 		for (int i = 0; i < v.length && ok; i++)
 		{
 			ok = addObject(v[i]);
 		}
 		if (ok)
 		{
 			notifyUpdates();
 			return true;
 		}
 		//
 		// Si l'opération n'a pas réussi, c'est à dire qu'un objet, figurant
 		// dans l'ensemble des objets que l'on tente d'insérer, possède un id
 		// qui existe déjà dans le schema courant, alors on effectue
 		// l'opération inverse: on supprime tout objet inséré durant
 		// l'opération précédente.
 		//
 		for (SchemaObject o : v)
 		{
 			delObject(o.getId());
 		}
 		return false;
 	}
 
 	/**
 	 * Ajoute un ensemble d'objets au schema.
 	 * @param v Un ensemble d'objets du schema.
 	 * @return True si l'opération a réussi, false sinon.
 	 *		Si l'opération n'a pas réussi, aucun objet n'aura été ajouté.
 	 */
 	public boolean addObjects ( Vector<SchemaObject> v )
 	{
 		SchemaObject[] o = new SchemaObject[v.size()];
 		Iterator<SchemaObject> it = v.iterator();
 		int position = 0;
 		while (it.hasNext())
 		{
 			o[position] = it.next();
 			position++;
 		}
 		return addObjects(o);
 	}
 
 	/**
 	 * Indique si l'objet identifié par id existe dans le schema.
 	 * @param id Une chaine de caractères représentant l'id d'un objet du schéma.
 	 * @return True si l'objet d'identifiant id existe dans le schema, false sinon.
 	 */
 	public boolean contains ( String id )
 	{
 		try
 		{
			if (id == null || objets == null)
			{
				return false;
			}
 			if (objets.containsKey(id))
 			{
 				return true;
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * Retourne le nombre d'objets que contient le schéma.
 	 * @return int Le nombre d'objets du schéma.
 	 */
 	public int countObjects ()
 	{
 		return objets.size();
 	}
 
 	/**
 	 * Créer un schéma et charge les objets lu à partir d'un fichier dans ce
 	 * nouveau schéma.
 	 * @param SchemaSyntax syntax
 	 * @param String filename
 	 * @param boolean load
 	 * @return SchemaFile Un objet SchemaFile contenant les objets.
 	 */
 	public static SchemaFile createAndLoad ( SchemaSyntax syntax, String filename, boolean load )
 	{
 		SchemaFileReader sReader = syntax.createSchemaReader();
 		SchemaFile sFile = new SchemaFile(filename,sReader, null);
 		if (load)
 		{
 			sFile.read();
 		}
 		return sFile;
 	}
 
 	/**
 	 * Supprime un objet du schema.
 	 * @param id Une chaine de caractères représentant l'id d'un objet du schema.
 	 * @return True si l'objet d'identifiant id existe dans le schema, false sinon.
 	 */
 	public boolean delObject ( String id )
 	{	
 		if (contains(id))
 		{
 			SchemaObject o = objets.remove(id);
 			objectsOrder.removeElement(o);
 			notifyUpdates();
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Generate a random object identifier.
 	 * @return String A random object identifier string.
 	 */
 	public String generateRandomObjectIdentifier ()
 	{
 		if (this.objectIdentifierPrefix == null)
 		{
 			objectIdentifierPrefix = new String("0.1.2.3.4.5.6");
 		}
 		if (this.objectIdentifierSuffix == null)
 		{
 			objectIdentifierSuffix = new String("0");
 		}
 		int suffix = (new Integer(objectIdentifierSuffix)).intValue();
 		objectIdentifierSuffix = String.valueOf(++suffix);
 		String roid = new String(objectIdentifierPrefix + "." + objectIdentifierSuffix);
 		return roid;
 	}
 
 	/**
 	 * Retourne l'historique d'ajout du schÃ©ma.
 	 * @return History Un historique en fonction des donnÃ©es actuelles du schÃ©ma.
 	 */
 	public History getHistory ()
 	{
 		return objectsOrder;
 	}
 
 	/**
 	 * Accède à un objet du schema.
 	 * @param id Une chaine de caractères représentant l'id d'un objet du schema.
 	 * @return SchemaObject Un objet du schema.
 	 */
 	public SchemaObject getObject ( String id )
 	{	
 		if (contains(id))
 		{
 			return objets.get(id);
 		}
 		return null;
 	}
 
 	/**
 	 * Retourne un objet du schéma dont le nom est name.
 	 * @param name Le nom d'un objet, et non son id.
 	 * @return SchemaObject Un objet du schéma.
 	 */
 	public SchemaObject getObjectByName ( String name )
 	{
 		SchemaObject result = null ;
 		Enumeration<SchemaObject> it = objets.elements();
 		while (result == null && it.hasMoreElements())
 		{
 			SchemaObject o = it.nextElement();
 			if (o.getName() == null)
 			{
 				continue;
 			}
 			if (o.getName().toLowerCase().equals(name.toLowerCase()))
 			{
 				result = o;
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Retourne l'ensemble des objets du schema.
 	 * @return SchemaObject[] L'ensemble des objets du schema.
 	 */
 	public SchemaObject[] getObjects ()
 	{
 		SchemaObject[] result = new SchemaObject[objets.size()];
 		int position = 0;
 		for (Enumeration<SchemaObject> e=objets.elements(); e.hasMoreElements();)
 		{
 			result[position] = e.nextElement();
 			position++;
 		}
 		return result;
 	}
 
 	/**
 	 * Retourne l'ensemble des objets du schema d'un certain type.
 	 * @param type Le type des objets à selectionner.
 	 * @return SchemaObject[] L'ensemble des objets du schema.
 	 */
 	public SchemaObject[] getObjects ( String type )
 	{
 		Vector<SchemaObject> v = new Vector<SchemaObject>();
 		for (Enumeration<SchemaObject> e=objets.elements(); e.hasMoreElements();)
 		{
 			SchemaObject o = e.nextElement();
 			if (type.equals(o.getType()))
 			{
 				v.add(o);
 			}
 		}
 		SchemaObject[] result = new SchemaObject[v.size()];
 		int position = 0;
 		for (Enumeration<SchemaObject> e=v.elements(); e.hasMoreElements();)
 		{
 			result[position] = e.nextElement();
 			position++;
 		}
 		return result;
 	}
 
 	/**
 	 * Retourne l'ensemble des objets du schema dans l'ordre dans lequel
 	 * ils ont été ajouté au schéma.
 	 * @return SchemaObject[] L'ensemble des objets du schema.
 	 */
 	public SchemaObject[] getObjectsInOrder ()
 	{
 		Vector<SchemaObject> result = new Vector<SchemaObject>();
 		for (SchemaObject o : this.getObjectsInOrder(this.getSyntax().getObjectIdentifierType()))
 		{
 			result.add(o);
 		}
 		for (SchemaObject o : this.getObjectsInOrder(this.getSyntax().getAttributeType()))
 		{
 			result.add(o);
 		}
 		for (SchemaObject o : this.getObjectsInOrder(this.getSyntax().getObjectClassType()))
 		{
 			result.add(o);
 		}
 		return result.toArray(new SchemaObject[10]);
 	}
 
 	/**
 	 * Retourne l'ensemble des objets du schema d'un certain type, dans l'ordre
 	 * dans lequel ils ont été ajouté au schéma.
 	 * @param type Le type des objets à selectionner.
 	 * @return SchemaObject[] L'ensemble des objets du schema.
 	 */
 	public SchemaObject[] getObjectsInOrder ( String type )
 	{
 		Vector<SchemaObject> v = new Vector<SchemaObject>();
 		for (Enumeration<Object> e=objectsOrder.elements(); e.hasMoreElements();)
 		{
 			SchemaObject o = (SchemaObject) e.nextElement();
 			if (type.equals(o.getType()))
 			{
 				v.add(o);
 			}
 		}
 		SchemaObject[] result = new SchemaObject[v.size()];
 		int position = 0;
 		for (Enumeration<SchemaObject> e=v.elements(); e.hasMoreElements();)
 		{
 			result[position] = e.nextElement();
 			position++;
 		}
 		return result;
 	}
 
 	/**
 	 * Returns objects identifiers.
 	 * @return Properties All objects identifiers.
 	 */
 	public Properties getObjectsIdentifiers ()
 	{
 		Properties objectsIdentifiers = new Properties();
 		SchemaObject[] oids = this.getObjectsInOrder(this.getSyntax().getObjectIdentifierType());
 		for (SchemaObject object : oids)
 		{
 			String[] keys = object.getKeys();
 			SchemaValue value = object.getValue(keys[0]);
 			objectsIdentifiers.setProperty(keys[0], value.toString());
 		}
 		return objectsIdentifiers;
 	}
 
 	/**
 	 * Get objects sorted.
 	 * @return SchemaObject[] All schema objects sorted.
 	 */
 	public SchemaObject[] getObjectsSorted ()
 	{
 		SchemaObject[] objects = new SchemaObject[objets.size()];
 		int position = 0;
 		for (Enumeration<SchemaObject> e = objets.elements(); e.hasMoreElements(); position++)
 		{
 			objects[position] = e.nextElement();
 		}
 		return objects;
 	}
 
 	/**
 	 * Retourne les propriétés du schéma.
 	 * @return Properties L'ensemble des propriétés du schéma.
 	 */
 	public Properties getProperties ()
 	{
 		return proprietes;
 	}
 
 	/**
 	 * Retourne la syntaxe utilisée par le schema.
 	 * @return SchemaSyntax Une syntaxe spécifique.
 	 */
 	public SchemaSyntax getSyntax ()
 	{
 		return syntax;
 	}
 
 	/**
 	 * Retourne le nom du paquetage contenant toutes les syntaxes.
 	 * @return String Un nom de paquetage.
 	 */
 	public static String getSyntaxPackageName ()
 	{
 		Schema s = new Schema(null);
 		return s.getClass().getPackage().getName() + ".syntax";
 	}
 
 	/**
 	 * Retourne l'ensemble des syntaxes connues, qui sont
 	 * contenues dans le package 'ldap.syntax'.
 	 * @return String[] L'ensemble des noms de classes de syntaxes.
 	 */
 	public static String[] getSyntaxes ()
 	{
 		String[] result = null;
 		try
 		{
 			String packageName = getSyntaxPackageName();
 			URL url = Schema.class.getResource("/" + packageName.replace('.', '/'));
 			if (url == null)
 			{
 				return null;
 			}
 			if (url.getProtocol().equals("jar"))
 			{
 				Vector<String> vectTmp = new Vector<String>();
 				int index = url.getPath().indexOf('!');
 				String path = URLDecoder.decode(url.getPath().substring(index+1), "UTF-8");
 				JarFile jarFile = new JarFile(URLDecoder.decode(url.getPath().substring(5, index), "UTF-8"));
 				if (path.charAt(0) == '/')
 				{
 					path = path.substring(1);
 				}
 				Enumeration<JarEntry> jarFiles = jarFile.entries();
 				while (jarFiles.hasMoreElements())
 				{
 					JarEntry tmp = jarFiles.nextElement();
 					//
 					// Pour chaque fichier dans le jar, on regarde si c'est un
 					// fichier de classe Java.
 					//
 					if (!tmp.isDirectory()
 						&& tmp.getName().substring(tmp.getName().length() - 6).equals(".class")
 						&& tmp.getName().startsWith(path))
 					{
 						int i = tmp.getName().lastIndexOf('/');
 						vectTmp.add(tmp.getName().substring(i+1, tmp.getName().length() - 6));
 					}
 				}
 				jarFile.close();
 				result = new String[vectTmp.size()];
 				for (int i = 0; i < vectTmp.size(); i++)
 				{
 					result[i] = vectTmp.elementAt(i);
 				}
 			}
 			else if (url.getProtocol().equals("file"))
 			{
 				//
 				// On créé le fichier associé pour parcourir son contenu.
 				// En l'occurence, c'est un dossier.
 				//
 				File[] files = (new File(url.toURI())).listFiles();
 				//
 				// On liste tous les fichiers qui sont dedans.
 				// On les stocke dans un vecteur ...
 				//
 				Vector<File> vectTmp = new Vector<File>();
 				for (File f : files)
 				{
 					if (!f.isDirectory())
 					{
 						vectTmp.add(f);
 					}
 				}
 				//
 			 	// ... pour ensuite les mettres dans le tableau de resultat.
 				//
 				result = new String[vectTmp.size()];
 				for (int i = 0; i < vectTmp.size(); i++)
 				{
 					String name = vectTmp.elementAt(i).getName();
 					int a = name.indexOf('.');
 					name = name.substring(0, a);
 					result[i] = name;
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		if (result != null)
 		{
 			Arrays.sort(result);
 		}
 		return result;
 	}
 
 	/**
 	 * Teste si la syntaxe à changer depuis la dernière fois qu'on a appelé
 	 * cette méthode.
 	 * @return boolean
 	 */
 	public boolean isSyntaxChangedSinceLastTime ()
 	{
 		if (isSyntaxChanged)
 		{
 			isSyntaxChanged = false;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Permet de notifier que les données ont changées.
 	 * Tous les objets observant le schema verront la notification.
 	 */
 	public void notifyUpdates ()
 	{
 		this.notifyUpdates(false);
 	}
 
 	/**
 	 * Permet de notifier que les données ont changées.
 	 * Tous les objets observant le schema verront la notification.
 	 * @param boolean force Indicates wheter or not to force the update.
 	 */
 	public void notifyUpdates ( boolean force )
 	{
 		setChanged();
 		notifyObservers(Boolean.valueOf(force));
 	}
 
 	/**
 	 * Modify objects identifiers of this schema.
 	 * @param newOIDs New objects identifiers.
 	 */
 	public void setObjectsIdentifiers ( Properties newOIDs )
 	{
 		SchemaObject[] oids = this.getObjectsInOrder(this.getSyntax().getObjectIdentifierType());
 		for (SchemaObject object : oids)
 		{
 			this.delObject(object.getId());
 		}
 		for (Enumeration keys = newOIDs.propertyNames(); keys.hasMoreElements();)
 		{
 			String id = (String) keys.nextElement();
 			SchemaValue v = this.getSyntax().createSchemaValue(
 				this.getSyntax().getObjectIdentifierType(),
 				id,
 				newOIDs.getProperty(id)
 			);
 			SchemaObject o = this.getSyntax().createSchemaObject(
 				this.getSyntax().getObjectIdentifierType(),
 				v.toString()
 			);
 			o.addValue(id,v);
 			this.addObject(o);
 		}
 	}
 
 	/**
 	 * Modifies les propriétés du schéma.
 	 * @param newProp Le nouvel objet Properties.
 	 */
 	public void setProperties ( Properties newProp )
 	{
 		this.proprietes = newProp;
 	}
 
 	/**
 	 * Modifie la syntaxe du schéma.
 	 * @param newSyntax Une syntaxe spécifique.
 	 */
 	public void setSyntax ( SchemaSyntax newSyntax )
 	{
 		isSyntaxChanged = true;
 		syntax = newSyntax;
 	}
 
 }
