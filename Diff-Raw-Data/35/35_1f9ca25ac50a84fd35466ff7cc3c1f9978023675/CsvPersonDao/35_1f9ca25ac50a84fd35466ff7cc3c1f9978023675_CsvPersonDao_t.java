 package fr.arbre.dao.csv;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import fr.arbre.model.Gender;
 import fr.arbre.model.PersonFrame;
 import fr.arbre.model.SimplePerson;
 
 /**
  * Singleton contenant une liste de personne pralablement charg par la fonction load().
  */
 public class CsvPersonDao {
 	private static CsvPersonDao instance = new CsvPersonDao();
 	// ---------------------------------------------------------------------------------------------
 
 	protected List<SimplePerson> persons;
 	protected String[] header; // Entetes des colonnes du JTable
 	protected String[][] table; // Donnes pour le JTable
 
 	// ---------------------------------------------------------------------------------------------
 	private CsvPersonDao() {
 		// priv
 	}
 
 	public static CsvPersonDao getInstance() {
 		return instance;
 	}
 
 	/**
 	 * Obtenir la table des donnes des personnes.
 	 * 
 	 * @return Un tableau  deux dimensions [index ligne][index colonne] des donnes (sans entetes).
 	 */
 	public String[][] getTable() {
 		return table;
 	}
 
 	/**
 	 * Charge les donnes d'un fichier <i>csv</i> et le stocke.
 	 * 
 	 * @param filename
 	 *            Nom du fichier contenant les donnes.
 	 */
 	public void load(String filename) {
 		Csv2Array data = new Csv2Array(filename);
 		String[][] tab = data.toArray();
 
 		header = tab[0].clone();
 		table = new String[tab.length - 1][data.getMaxColumn()];
 		for (int i = 0; i < table.length; i++) {
 			for (int j = 0; j < table[0].length; j++) {
 				if (j < tab[i + 1].length)
 					table[i][j] = tab[i + 1][j];
 				else
 					table[i][j] = "";
 			}
 		}
 
 		persons = new ArrayList<SimplePerson>();
 		for (int row = 0, size = table.length; row < size; row++) {
 			SimplePerson person = new SimplePerson();
 
 			/*
 			 * index 0 : id, 1 : nom, 2 : prnom, 3 : genre, 4 : id du pre, 5 : id de la mre, 6 :
 			 * date de naissance, 7 : nom du fichier de la photo, 8 : liste d'id des enfants
 			 */
 			person.setId(Integer.parseInt(table[row][0]));
 			person.setFirstname(table[row][1]);
 			person.setName(table[row][2]);
 			person.setGender("M".equalsIgnoreCase(table[row][3]) ? Gender.MALE : Gender.FEMALE);
 			person.setFatherId("".equals(table[row][4]) ? 0 : Integer.parseInt(table[row][4]));
 			person.setMotherId("".equals(table[row][5]) ? 0 : Integer.parseInt(table[row][5]));
 			person.setBirthdate(table[row][6]);
 			person.setPicname(table[row][7]);
 			if (person.getPicname().isEmpty()) {
 				person.setPicname("vide.jpg");
 			}
 			if (!table[row][8].isEmpty()) {
 				String[] childrenId = table[row][8].split(" ");
 				for (int i = childrenId.length - 1; i >= 0; i--) {
 					person.addChildId(Integer.parseInt(childrenId[i]));
 				}
 			}
 
 			persons.add(person);
 		}
 
 		calculateTreeInfos();
 	}
 
 	/**
 	 * Affiche les donnes de toutes les personnes sur la console.
 	 */
 	public void print() {
 		for (SimplePerson person : persons) {
 			System.out.println(person.toString());
 		}
 	}
 
 	/**
 	 * Rcupre une personne en fonction de <i>id</i>.
 	 * 
 	 * @return Une SimplePerson si elle a t trouve dans la liste sinon lance une exception.
 	 * @throws PersonIdException
 	 *             si l'id est incorrect ou si la personne n'a pas t trouv.
 	 */
 	public SimplePerson getPerson(int personId) throws PersonIdException {
 		if (personId <= 0) // id<=0:erreur, valide sinon
 			throw new PersonIdException("Valeur de l'index de la personne incorrect");
 
 		SimplePerson returnPerson = null;
 		for (SimplePerson person : persons) {
 			if (person.getId() == personId) {
 				returnPerson = person;
 				break;
 			}
 		}
 
 		if (returnPerson == null)
 			throw new PersonIdException("Personne non trouve");
 
 		return returnPerson;
 	}
 
 	public List<SimplePerson> getPersons() {
 		return persons;
 	}
 
 	/**
 	 * Permet d'obtenir une liste de personne tri en fonction du genre.
 	 * 
 	 * @param filter
 	 *            {@link Gender#MALE} ou {@link Gender#FEMALE}
 	 * @return Une table de SimplePerson filtr.
 	 * @throws GenderException
 	 */
 	public String[][] getTableByGender(Gender filter) throws GenderException {
 		if (filter != Gender.MALE && filter != Gender.FEMALE)
 			throw new GenderException("Le genre est incorrect");
 
 		int count = 0;
 		for (SimplePerson person : persons) {
 			if (person.getGender() == filter) {
 				count++;
 			}
 		}
 
 		String[][] array = new String[count][header.length];
 		for (int row = 0, i = 0; row < table.length; row++) {
 			if (filter.isMale() && "M".equalsIgnoreCase(table[row][3])) {
 				array[i++] = table[row].clone();
 			} else if ((!filter.isMale()) && "F".equalsIgnoreCase(table[row][3])) {
 				array[i++] = table[row].clone();
 			}
 		}
 
 		return array;
 	}
 
 	/**
 	 * Obtenir les noms des colonnes.
 	 */
 	public String[] getHeader() {
 		return header;
 	}
 
 	// ---------------------------------------------------------------------------------------------
 
 	private void calculateTreeInfos() {
 		/**
 		 * <b>Cl :</b> Profondeur d'indentation de la gnration.<br/>
 		 * <b>Valeur :</b> Une liste d'identifiants des personnes sur cette ligne.
 		 */
 		Map<Integer, List<Integer>> multimap = new HashMap<Integer, List<Integer>>();
 
 		// remplir multimap en partant de la personne d'id 1 avec une indentation=0
 		walkTree(multimap, persons.get(0).getId(), 0);
 
 		// dfinir position des frames
 		makeFrames(multimap);
 	}
 
 	/**
 	 * Identifie le nombre de generation ainsi que l'id des personnes sur chacune d'entre elle.<br/>
 	 * <p>
 	 * <b>Note : </b>Utile pour dfinir la taille de l'image du treedrawpanel.
 	 * </p>
 	 */
 	private void walkTree(Map<Integer, List<Integer>> multimap, int personId, int indent) {
 		SimplePerson person = null;
 
 		if (!multimap.containsKey(indent)) {
 			// ajoute l'id de la gnration si elle n'existe pas dj et crer la liste
 			multimap.put(indent, new ArrayList<Integer>());
 		}
 
 		if (multimap.get(indent).contains(personId)) {
 			// personne dj trait
 			return;
 		} else {
 			// ajoute la personne si elle n'est pas dans cette gnration
 			multimap.get(indent).add(personId);
 		}
 
 		try {
 			person = this.getPerson(personId);
 		} catch (PersonIdException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		if (person.getFatherId() > 0) {
 			walkTree(multimap, person.getFatherId(), indent + 1);
 		}
 
 		if (person.getMotherId() > 0) {
 			walkTree(multimap, person.getMotherId(), indent + 1);
 		}
 
 		if (person.getChildrenId().size() > 0) {
 			for (int i = person.getChildrenId().size() - 1; i >= 0; i--) {
 				walkTree(multimap, person.getChildrenId().get(i), indent - 1);
 			}
 		}
 	}
 
	private int imageWidth = 0;
	private int imageHeight = 0;
 
 	public int getImageWidth() {
 		return imageWidth;
 	}
 
 	public int getImageHeight() {
 		return imageHeight;
 	}
 
 	/**
 	 * Calcule la position des frames de chaque personne dans l'image de l'arbre
 	 * 
 	 * @param multimap
 	 */
 	private void makeFrames(Map<Integer, List<Integer>> multimap) {
 		/**
 		 * TODO <br/>
 		 * ->Image deux fois plus large<br/>
 		 * On copie depuis la personne la plus  gauche jusqu' la plus  droite sur l'image plus
 		 * petite<br/>
 		 * ->On dessine les personnes recursivement vers le haut<br/>
 		 */
 		final int MARGIN = 50;
 		int minY = Integer.MAX_VALUE;
 		int maxY = Integer.MIN_VALUE;
 		int offsetY = 0;
 		int maxW = 0;
 
 		for (Integer key : multimap.keySet()) {
 			if (multimap.get(key).size() > maxW) {
 				maxW = multimap.get(key).size();
 			}
 		}
 		imageWidth = PersonFrame.WIDTH + 2 * PersonFrame.WIDTH * maxW;
 
 		for (Integer key : multimap.keySet()) {
 			if (key < minY)
 				minY = key;
 			if (key > maxY)
 				maxY = key;
 		}
 		imageHeight = (int) (1.5 * (maxY - minY + 1) * PersonFrame.HEIGHT + 2 * MARGIN);
 
 		if (minY != 0) {
 			offsetY = -minY;
 		}
 
 		// parcourir les gnrations
 		for (int key = minY; key <= maxY; key++) {
 			int size = multimap.get(key).size();
 			int width = PersonFrame.WIDTH + size * PersonFrame.WIDTH * 2; // largeur de la
 																			// ligne
 			int cx = (((int) imageWidth) - width) / 2;
 			int x = MARGIN + cx;
 
 			// parcourir les personnes sur la ligne
 			while (size > 0) {
 				SimplePerson person = null;
 				try {
 					person = getPerson(multimap.get(key).get(0));
 				} catch (PersonIdException e) {
 					e.printStackTrace();
 				}
 				if (person != null) {
 					Integer[] brothers = getBrothers(person);
 					int y = (int) (((int) imageHeight) - (MARGIN + PersonFrame.HEIGHT * 1.5
 							* (key + offsetY + 1)));
 					for (int j = 0; j < brothers.length; j++, x += 2 * PersonFrame.WIDTH) {
 						try {
 							getPerson(brothers[j]).setFrame(
 									new PersonFrame(getPerson(brothers[j]), x, y));
 							// System.out.println("id=" + brothers[j] + ", x=" + x + ",y=" + y);
 						} catch (PersonIdException e) {
 							e.printStackTrace();
 						}
 
 						// supprimer la personne ajout de la liste d'attente
 						multimap.get(key).remove((Object) brothers[j]);
 						size--;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return Un set d'identifiants (sans rptition donc).
 	 */
 	private Integer[] getBrothers(SimplePerson person) {
 		Set<Integer> brothers = new HashSet<Integer>();
 		List<Integer> fatherChildren = null;
 		List<Integer> motherChildren = null;
 
 		brothers.add(person.getId());
 
 		if (person.getFatherId() > 0) {
 			try {
 				fatherChildren = getPerson(person.getFatherId()).getChildrenId();
 			} catch (PersonIdException e) {
 				e.printStackTrace();
 				fatherChildren = null;
 			}
 		}
 		if (person.getMotherId() > 0) {
 			try {
 				motherChildren = getPerson(person.getMotherId()).getChildrenId();
 			} catch (PersonIdException e) {
 				e.printStackTrace();
 				motherChildren = null;
 			}
 		}
 
 		if (fatherChildren != null)
 			brothers.addAll(fatherChildren);
 
 		if (motherChildren != null)
 			brothers.addAll(motherChildren);
 
 		Integer[] array = new Integer[brothers.size()];
 		brothers.toArray(array);
 
 		return array;
 	}
 }
