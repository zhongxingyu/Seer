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
 
 package net.aepik.casl.core.ldap.syntax;
 
 import net.aepik.casl.core.ldap.value.*;
 import net.aepik.casl.core.ldap.SchemaObject;
 import net.aepik.casl.core.ldap.SchemaFileReader;
 import net.aepik.casl.core.ldap.SchemaFileWriter;
 import net.aepik.casl.core.ldap.SchemaSyntax;
 import net.aepik.casl.core.ldap.SchemaValue;
 import net.aepik.casl.core.ldap.parser.RFCReader;
 import net.aepik.casl.core.ldap.parser.RFCWriter;
 import java.lang.String;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  * Syntaxe RFC 2252.
  * <br/><br/>
  * Pour gérer les définitions de paramètres, nous utilisons un procédé
  * particulier.
  * Nous avons définit un tableau comme suit pour gérer tous les attributs.
  * <br/><br/>
  *   Indice	 Clef			Valeur<br/>
  * ----------------------------------------------
  * { "0",	"OID",			""			},<br/>
  * { "1",	"NAME",			""			},<br/>
  * { "2",	"DESC",			""			},<br/>
  * { "3",	"OBSOLETE",		null		},<br/>
  * { "4",	"SUP",			""			},<br/>
  * { "5",	"ABSTRACT",		null		},<br/>
  * { "5",	"STRUCTURAL",	null		},<br/>
  * { "5",	"AUXILIARY",	null		},<br/>
  * { "6",	"EXEMPLE",		"Option1"	},<br/>
  * { "6",	"EXEMPLE",		"Option2"	},<br/>
  * { "7",	"OPT1",			"Valeur1"	},<br/>
  * { "7",	"OPT1",			"Valeur2"	},<br/>
  * { "7",	"OPT2",			"Valeur3"	},<br/>
  * { "7",	"OPT2",			"Valeur4"	},<br/>
  * { "7",	"OPT3",			null		},<br/>
  * <br/><br/>
  * Nous avons cherché à respecter quelques règles :
  * <br/><br/>
  * 1) Dans le tableau si dessus, l'indice indique la position de la clef dans
  * un certain ordre. Par exemple, OID est avant NAME, qui est avant DESC,
  * etc. L'ordre du tableau n'est pas pris en compte, seul les indices font
  * l'ordre.
  * <br/><br/>
  * 2) Si plusieurs indices sont identiques, alors cela veut dire que les
  * clefs correspondantes ne peuvent pas "cohabiter" pour cet indice. Par
  * exemple, pour l'indice 5, c'est soit ABSTRACT, soit STRUCTURAL, soit
  * AUXILIARY. C'est valables pour n clefs.
  * <br/><br/>
  * 3) Si plusieurs indices sont identiques, ainsi que les clefs
  * correspondantes, une clef peut avoir plusieurs valeurs définies, et pas
  * autres choses. Ainsi, pour la clef EXEMPLE, il ne peut ya voir comme valeur
  * que "Option1" et "Option2".
  * <br/><br/>
  * 4) Quand une valeur est indiquée null, cela veut dire que seul la clef
  * doit être prise en compte, cette clef n'attend pas de valeurs.
  * 5) Les règles 2, et et 4 sont combinables, si toute fois c'est géré par
  * les objets qui utiliseront cette classe.
  * <br/><br/>
  * 6) Les valeurs "" sont considérées comme valeurs vides. C'est à dire que ça
  * peut-être n'importe quoi. Au contraire des valeurs null, qui sont
  * restrictives.
  */
 public class RFCSyntax extends SchemaSyntax
 {
 
 	/**
 	 * Attribute definition (and used for type).
 	 */
 	private static final String RFC_ATTRIBUTE = "AttributeTypeDescription =";
 
 	/**
 	 * Attribute parameters.
 	 */
 	protected String[][] RFC_ATTRIBUTE_PARAMETERS = {
 		{ "1",	"NAME",			"" },
 		{ "2",	"DESC",			"" },
 		{ "3",	"OBSOLETE",		null },
 		{ "4",	"SUP",			"" },
 		{ "5",	"EQUALITY",		"" },
 		{ "6",	"ORDERING",		"" },
 		{ "7",	"SUBSTR",		"" },
 		{ "8",	"SYNTAX",		"" },
 		{ "9",	"SINGLE-VALUE",		null },
 		{ "10",	"COLLECTIVE",		null },
 		{ "11",	"NO-USER-MODIFICATION",	null },
 		{ "12",	"USAGE",		"userApplications" },
 		{ "12",	"USAGE",		"directoryOperation" },
 		{ "12",	"USAGE",		"distributedOperation" },
 		{ "12",	"USAGE",		"dSAOperation" }
 	};
 
 	/**
 	 * ObjectClass definition (and used for type).
 	 */
 	private static final String RFC_OBJECT = "ObjectClassDescription =";
 
 	/**
 	 * ObjectClass parameters.
 	 */
 	protected String[][] RFC_OBJECT_PARAMETERS = {
 		{ "1",	"NAME",		"" },
 		{ "2",	"DESC",		"" },
 		{ "3",	"OBSOLETE",	null },
 		{ "4",	"SUP",		"" },
 		{ "5",	"ABSTRACT",	null },
 		{ "5",	"STRUCTURAL",	null },
 		{ "5",	"AUXILIARY",	null },
 		{ "6",	"MUST",		"" },
 		{ "7",	"MAY",		"" }
 	};
 
 	/**
 	 * ObjectIdentifier definition (and used for type)
 	 */
 	private static final String RFC_OBJECTID = "ObjectIdentifier =";
 
 	/**
 	 * Build a new RFCSyntax object.
 	 */
 	public RFCSyntax () {
 		super(
 			RFC_ATTRIBUTE.substring(0, RFC_ATTRIBUTE.length() - 2),
 			RFC_ATTRIBUTE,
 			RFC_OBJECT.substring(0, RFC_OBJECT.length() - 2),
 			RFC_OBJECT,
 			RFC_OBJECTID.substring(0, RFC_OBJECTID.length() - 2),
 			RFC_OBJECTID
 		);
 	}
 
 	/**
 	 * Créer un reader pour lire un fichier de cette syntaxe.
 	 * @return SchemaFileReader Un reader spécifique à cette syntaxe.
 	 */
 	public SchemaFileReader createSchemaReader ()
 	{
 		return new RFCReader(this);
 	}
 
 	/**
 	 * Créer un nouvel objet SchemaObject d'un type donné.
 	 * @param type Un type d'objet.
 	 * @param id L'identifiant de l'objet.
 	 * @return SchemaObject L'objet de type SchemaObject.
 	 */
 	public SchemaObject createSchemaObject ( String type, String id )
 	{
 		SchemaObject objet = null;
 		if (type.equals(getObjectClassType())
 			|| type.equals(getAttributeType()))
 		{
 			objet = new SchemaObject(this, type, id);
 		}
 		return objet;
 	}
 
 	/**
 	 * Créer un nouvel objet SchemaValue d'une valeur donnée.
 	 * @param type Un type d'objet.
 	 * @param param Le nom du paramêtre, pour déterminer le type de la valeur;
 	 *		si le paramètre est null, le type créé est un Oid.
 	 * @param value Une chaîne de caractères, ou null pour ne pas initialiser.
 	 * @return SchemaValue L'objet de type SchemaValue.
 	 */
 	public SchemaValue createSchemaValue ( String type, String param, String value )
 	{
 		SchemaValue valeur = null;
 		String tmp = value;
 		if (tmp != null)
 		{
 			tmp = tmp.trim();
 		}
 		if (param == null && tmp == null)
 		{
 			valeur = new Oid();
 		}
 		else if (param == null)
 		{
 			valeur = new Oid(tmp);
 		}
 		else
 		{
 			if (type.equals(attributeType))
 			{
 				if (param.equals(RFC_ATTRIBUTE_PARAMETERS[0][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new QDescriptionList(tmp);
 					}
 					else
 					{
 						valeur = new QDescriptionList();
 					}
 				}
 				else if (param.equals(RFC_ATTRIBUTE_PARAMETERS[1][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new QString(tmp);
 					}
 					else
 					{
 						valeur = new QString();
 					}
 				}
 				else if (param.equals(RFC_ATTRIBUTE_PARAMETERS[2][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[7][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[8][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[9][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[10][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new SValue(tmp);
 					}
 					else
 					{
 						valeur = new SValue();
 					}
 				}
 				else if (param.equals(RFC_ATTRIBUTE_PARAMETERS[3][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[4][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[5][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[6][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[11][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[12][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[13][1])
 					|| param.equals(RFC_ATTRIBUTE_PARAMETERS[14][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new Oid(tmp);
 					}
 					else
 					{
 						valeur = new Oid();
 					}
 				}
 				else
 				{
 					if (tmp != null)
 					{
 						valeur = new SValue(tmp);
 					}
 					else
 					{
 						valeur = new SValue();
 					}
 				}
 			}
 
 			if (type.equals(objectClassType))
 			{
 				if (param.equals(RFC_OBJECT_PARAMETERS[0][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new QDescription(tmp);
 					}
 					else
 					{
 						valeur = new QDescription();
 					}
 				}
 				else if (param.equals(RFC_OBJECT_PARAMETERS[1][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new QString(tmp);
 					}
 					else
 					{
 						valeur = new QString();
 					}
 				}
 				else if (param.equals(RFC_OBJECT_PARAMETERS[2][1])
 					|| param.equals(RFC_OBJECT_PARAMETERS[4][1])
 					|| param.equals(RFC_OBJECT_PARAMETERS[5][1])
 					|| param.equals(RFC_OBJECT_PARAMETERS[6][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new SValue(tmp);
 					}
 					else
 					{
 						valeur = new SValue();
 					}
 				}
 				else if (param.equals(RFC_OBJECT_PARAMETERS[3][1])
 					|| param.equals(RFC_OBJECT_PARAMETERS[7][1])
 					|| param.equals(RFC_OBJECT_PARAMETERS[8][1]))
 				{
 					if (tmp != null)
 					{
 						valeur = new OidList(tmp);
 					}
 					else
 					{
 						valeur = new OidList();
 					}
 				}
 				else
 				{
 					if (tmp != null)
 					{
 						valeur = new SValue(tmp);
 					}
 					else
 					{
 						valeur = new SValue();
 					}
 				}
 			}
 		}
 		return valeur;
 	}
 
 	/**
 	 * Créer un writer pour écrire des données.
 	 * @return SchemaFileWriter Un writer spécifique à cette syntaxe.
 	 */
 	public SchemaFileWriter createSchemaWriter ()
 	{
 		return new RFCWriter();
 	}
 
 	/**
 	 * Retourne l'ensemble des valeurs possible d'un paramêtre d'attribut.
 	 * @param paramName Un nom de paramêtre.
 	 * @return String[] Une ensemble de valeurs pour ce nom de paramêtres.
 	 */
 	public String[] getAttributeParameterDefaultValues ( String paramName )
 	{
 		return searchParameterDefaultValues(RFC_ATTRIBUTE_PARAMETERS, paramName);
 	}
 
 	/**
 	 * Retourne l'ensemble des paramêtres d'attribut.
 	 * @return String[] Un ensemble de chaînes de caractères.
 	 */
 	public String[] getAttributeParameters ()
 	{
 		return searchParameters(RFC_ATTRIBUTE_PARAMETERS);
 	}
 
 	/**
 	 * Retourne le nom du paramêtre renseignant le nom usuel de l'objet.
 	 * @param type Le type de l'objet.
 	 */
 	public String getDisplayNameParameter ( String type )
 	{
 		String result = null;
 		if (type != null)
 		{
 			if (type.equals(attributeType))
 			{
 				result = RFC_ATTRIBUTE_PARAMETERS[0][1];
 			}
 			else if (type.equals(objectClassType))
 			{
 				result = RFC_OBJECT_PARAMETERS[0][1];
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Return parameters of object identifier.
 	 * @return String[] An array of strings.
 	 */
 	public String[] getObjectIdentifierParameters ()
 	{
 		return null;
 	}
 
 	/**
 	 * Return default parameters of object identifier.
 	 * @param paramName A parameter name
 	 * @return String[] An array of default values for the specified parameter.
 	 */
 	public String[] getObjectIdentifierParameterDefaultValues ( String paramName )
 	{
 		return null;
 	}
 
 	/**
 	 * Retourne l'ensemble des valeurs possible d'un paramêtre d'objet.
 	 * @param paramName Un nom de paramêtre.
 	 * @return String[] Une ensemble de valeurs pour ce nom de paramêtres.
 	 */
 	public String[] getObjectParameterDefaultValues ( String paramName )
 	{
 		return searchParameterDefaultValues(RFC_OBJECT_PARAMETERS, paramName);
 	}
 
 	/**
 	 * Retourne l'ensemble des paramêtres d'objet.
 	 * @return String[] Un ensemble de chaînes de caractères.
 	 */
 	public String[] getObjectParameters ()
 	{
 		return searchParameters(RFC_OBJECT_PARAMETERS);
 	}
 
 	/**
 	 * Retourne l'ensemble des autres paramêtres d'attribut possible pour
 	 * le paramêtre de nom 'paramName'.
 	 * @param paramName Un nom de paramêtre d'attribut.
 	 * @return String[] Un ensemble de chaînes de caractères.
 	 */
 	public String[] getOthersAttributeParametersFor ( String paramName )
 	{
 		return searchOthersParametersFor(RFC_ATTRIBUTE_PARAMETERS, paramName);
 	}
 
 	/**
 	 * Retourne l'ensemble des autres paramêtres d'objet possible pour
 	 * le paramêtre de nom 'paramName'.
 	 * @param paramName Un nom de paramêtre d'attribut.
 	 * @return String[] Un ensemble de chaînes de caractères.
 	 */
 	public String[] getOthersObjectParametersFor ( String paramName )
 	{
 		return searchOthersParametersFor(RFC_OBJECT_PARAMETERS, paramName);
 	}
 
 	/**
 	 * Test si la chaîne de caractères désigne un attribut.
 	 * @param str Une chaîne de caractères.
 	 * @return boolean True si c'est le cas, false sinon.
 	 */
 	public boolean isAttributeHeader ( String str )
 	{
 		if (attributeHeader == null)
 		{
 			return false;
 		}
 		return str.trim().toLowerCase().startsWith(attributeType.toLowerCase());
 	}
 
 	/**
 	 * Test si la chaîne de caractères désigne un objet.
 	 * @param str Une chaîne de caractères.
 	 * @return boolean True si c'est le cas, false sinon.
 	 */
 	public boolean isObjectClassHeader ( String str )
 	{
 		if (objectClassHeader == null)
 		{
 			return false;
 		}
 		return str.trim().toLowerCase().startsWith(objectClassType.toLowerCase());
 	}
 
 	/**
 	 * Test si la chaîne de caractères désigne un identifiant d'objet.
 	 * @param str Une chaîne de caractères.
 	 * @return boolean True si c'est le cas, false sinon.
 	 */
 	public boolean isObjectIdentifierHeader ( String str )
 	{
 		if (objectIdentifierHeader == null)
 		{
 			return false;
 		}
 		return str.trim().toLowerCase().startsWith(objectIdentifierType.toLowerCase());
 	}
 
 	/**
 	 * Cette méthode retourne toutes les autres clefs possibles qui peuvent
 	 * être au même index que la clef 'key'. La clef 'key' fait partie du résultat.
 	 * @param tab Un tableau formé comme dans l'exemple.
 	 * @param key Une clef de ce tableau.
 	 * @return String[] Un ensemble de valeurs possible, ou null.
 	 */
 	protected static String[] searchOthersParametersFor (String[][] tab, String key)
 	{
 		String index = null;
 		for (int i = 0; i < tab.length && index == null; i++)
 		{
 			if (tab[i][1].equals(key))
 			{
 				index = tab[i][0];
 			}
 		}
 		if (index == null)
 		{
 			return null;
 		}
 		Vector<String> result_tmp = new Vector<String>();
 		for (int i = 0; i < tab.length; i++)
 		{
 			if (tab[i][0].equals(index) && !result_tmp.contains(tab[i][1]))
 			{
 				result_tmp.add(tab[i][1]);
 			}
 		}
 		Enumeration<String> elem = result_tmp.elements();
 		String[] result = new String[result_tmp.size()];
 		int compteur = 0;
 		while (elem.hasMoreElements())
 		{
 			result[compteur] = elem.nextElement();
 			compteur++;
 		}
 		return result;
 	}
 
 	/**
 	 * Cette méthode retourne toutes les valeurs possibles pour une clef, dans
 	 * un tableau qui respecte le format définit dans la documentation en intro
 	 * de cette classe.<br/>
 	 * - Si, la clef n'existe pas dans le tableau, alors null est retourné.
 	 * - Si au contraire cette clef existe, alors les valeurs possibles pour
 	 * celle-ci sont retournées dans un tableau. Si cette clef n'admet pas de
 	 * valeurs, alors un tableau vide est retourné. Si cette clef admet
 	 * n'importe quelle valeur, un tableau d'un élément est retourné, avec
 	 * comme premier champs une chaîne vide.
 	 * @param tab Un tableau formé comme dans l'exemple.
 	 * @param key Une clef de ce tableau.
 	 * @return String[] Un ensemble de valeurs possible, ou null.
 	 */
 	protected static String[] searchParameterDefaultValues ( String[][] tab, String key )
 	{
 		Vector<String> valeurs = new Vector<String>();
 		Vector<Integer> indices = new Vector<Integer>();
 		for (int i = 0; i < tab.length; i++)
 		{
 			if (tab[i][1].equals(key) && tab[i][2] != null)
 			{
 				if (tab[i][2].equals(""))
 				{
 					indices.add(new Integer(i));
 					valeurs.add(tab[i][2]);
 				}
 				else if (!indices.contains(new Integer(i)))
 				{
 					valeurs.add(tab[i][2]);
 				}
 			}
 		}
 		String[] result = new String[valeurs.size()];
 		for (int i = 0; i < result.length; i++)
 		{
 			result[i] = valeurs.elementAt(i);
 		}
 		return result;
 	}
 
 	/**
 	 * Cette méthode retourne toutes les clefs possibles pour un tableau qui
 	 * respecte le format définit dans la documentation en introduction de cette
 	 * classe.
 	 * @param tab Un tableau formé comme dans l'exemple.
 	 * @return String[] Un ensemble de chaînes de caractères.
 	 */
 	protected static String[] searchParameters ( String[][] tab )
 	{
 		Vector<Integer> indices = new Vector<Integer>();
 		Vector<String> parametres = new Vector<String>();
 		for (int i = 0; i < tab.length; i++)
 		{
 			Integer indice = new Integer(tab[i][0]);
 			String parametre = tab[i][1];
 			String valeur = tab[i][2];
 			if (!indices.contains(indice))
 			{
 				indices.add(indice);
 				parametres.add(parametre);
 			}
 			else
 			{
 				int j = indices.indexOf(indice);
 				boolean ajout = true;
 				while (j < indices.size() && ajout)
 				{
 					if (parametres.elementAt(j).equals(parametre))
 					{
 						ajout = false;
 					}
 					if (j < indices.size() - 1)
 					{
 						j = indices.indexOf(indice, j + 1);
 					}
 					else
 					{
 						j++;
 					}
 				}
 				if (ajout)
 				{
 					parametres.add(parametre);
 				}
 			}
 		}
 		String[] result = new String[parametres.size()];
 		for (int i = 0; i < result.length; i++)
 		{
 			result[i] = parametres.elementAt(i);
 		}
 		return result;
 	}
 
 	/**
 	 * Retourne l'OID d'un objet du schéma à partir d'une chaîne
 	 * d'initialisation d'un tel objet.
 	 * @param type Le type utilisé : objet ou attribut.
 	 * @param initStr Une chaîne qui permet d'initialiser un objet du schéma.
 	 * @return String L'OID si il est trouvé, false sinon.
 	 */
 	public String searchSchemaObjectOID ( String type, String initStr )
 	{
 		String[] parametres = getParameters(type);
 		//
 		// L'OID, dans la syntax RFC, se situe tout au début de la chaîne,
 		// avant les paramêtre. Il n'y a pas de clef.
 		// C'est pourquoi on va aller chercher le premier paramêtre, puis
 		// prend le bout de chaîne possible depuis l'index 0 de la chaîne
 		// d'initialisation jusqu'à la position du premier paramêtre valide
 		// rencontré.
 		//
 		int indexOfFirstParameter = -1;
 		for (int i = 0; i < parametres.length && indexOfFirstParameter == -1; i++)
 		{
 			indexOfFirstParameter = initStr.indexOf(parametres[i]);
 		}
 		String oid = null;
 		if (indexOfFirstParameter != -1
 			&& initStr.substring(0, indexOfFirstParameter).trim().length() != 0
 			&& Oid.isValidFormat(initStr.substring(0, indexOfFirstParameter).trim()))
 		{
 			oid = initStr.substring(0, indexOfFirstParameter).trim();
 		}
 		return oid;
 	}
 
 	/**
 	 * Retourne les valeurs d'un objet du schéma à partir d'une chaîne
 	 * d'initialisation d'un tel objet.
 	 * @param type Le type utilisé : objet ou attribut.
 	 * @param initStr Une chaîne qui permet d'initialiser un objet du schéma.
 	 * @return String[][] Un tableau de 2 caractères, la première colonne
 	 * comprends les paramêtres, la seconde les valeurs associées sous forme
 	 * de chaîne de caractères.
 	 */
 	public String[][] searchSchemaObjectValues ( String type, String initStr )
 	{
 		String[] params_name = getParameters(type);
 		if (params_name == null)
 		{
 			return null;
 		}
 		//
 		// Dans un premier temps, on regarde si l'ordre des paramêtres est
 		// correct. Si ce n'est pas le cas, erreur de syntaxe !
 		// Dans l'algorithme suivant, il s'agit de ne pas
 		// prendre en compte les numéros d'index, mais plutôt
 		// l'ordre dans lequel les valeurs sont écrites.
 		// L'ordre établit est conforme à la RFC 2252, c'est à dire
 		// [NAME <valeur>] [DESC <valeur>] [OBSOLETE] [SUP <valeur>]
 		// [ABSTRACT | STRUCTURAL | AUXILIARY] [MUST <valeur>] [MAY <valeur>]
 		//
 		int pos_str_begin = 0;
 		int[] params_index = new int[params_name.length];
 		//
 		// On récupère toutes les positions.
 		// La position 0 est reservé à l'id.
 		//
 		for (int i = 0; i < params_name.length; i++)
 		{
 			int index = initStr.indexOf(params_name[i], pos_str_begin);
 			if (index != -1)
 			{
 				pos_str_begin = index + params_name[i].length();
 			}
 			params_index[i] = index;
 		}
 		//
 		// On vérifie l'ordre en regardant si l'index
 		// précédent est bien inférieur à l'index courant.
 		//
 		int last_index = 0;
 		boolean erreur = false;
 		for (int i = 1; i < params_index.length && !erreur; i++)
 		{
 			if (params_index[i-1] != -1)
 			{
 				last_index = params_index[i-1];
 			}
 			if (params_index[i] != -1 && last_index>params_index[i])
 			{
 				erreur = true;
 			}
 		}
 		if (erreur)
 		{
 			return null;
 		}
 		//
 		// Il s'agit maintenant d'obtenir les valeurs que l'on veut !
 		// On sait que l'ordre est correct, alors on fait une boucle
 		// pour récupérer toutes les valeurs pour chaque paramêtre.
 		//
 		String[][] params_value = new String[params_name.length][2];
 		for (int i = 0; i < params_index.length; i++)
 		{
 			String param_value = null;
			String[] param_values_default = getParameterDefaultValues(type,params_name[i]);
			if (params_index[i] != -1 && param_values_default.length != 0)
 			{
 				int indexOfBegin = params_index[i] + params_name[i].length() + 1;
 				int indexOfEnd = -1;
 				// On récupère l'index de fin de la valeur.
 				for (int j = i+1; j < params_index.length && indexOfEnd == -1; j++)
 				{
 					if (params_index[j] != -1)
 					{
 						indexOfEnd = params_index[j];
 					}
 				}
 				// On récupère la valeur.
 				if (indexOfEnd != -1)
 				{
 					param_value = initStr.substring(indexOfBegin, indexOfEnd);
 				}
 				else
 				{
 					param_value = initStr.substring(indexOfBegin);
 				}
 				param_value.trim();
 			}
			if (params_index[i] != -1 && param_value == null)
			{
				param_value = "";
			}
 			params_value[i][0] = params_name[i];
 			params_value[i][1] = param_value;
 		}
 		return params_value;
 	}
 
 }
