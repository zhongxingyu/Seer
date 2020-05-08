 /**
  * @author Hadrien Milano <Sciper : 224340>
  * @author Christophe Tafani-Dereeper <Sciper : 223529>
  */
 
 package ch.epfl.flamemaker.flame;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import ch.epfl.flamemaker.geometry2d.AffineTransformation;
 import ch.epfl.flamemaker.geometry2d.Point;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 
 /**
  * Classe modélisant une fractale de type Flame
  */
 public class Flame {
 
 	/**
 	 * Contient la liste des transformations caractérisant la fractale
 	 */
 	final private List<FlameTransformation> m_transforms;
 	
 	final private double[] m_colorIndexes;
 
 	/**
 	 * Construit une nouvelle fractale à partir d'une liste de transformation la
 	 * caractérisant
 	 * 
 	 * @param transforms
 	 *            La liste des transformation
 	 */
 	public Flame(List<FlameTransformation> transforms) {
 		m_transforms = new ArrayList<FlameTransformation>(transforms);
 		
 		m_colorIndexes = new double[m_transforms.size()];
 		for(int i = 0 ; i < transforms.size() ; i++){
 			m_colorIndexes[i] = getColorIndex(i);
 		}
 	}
 
 	/**
 	 * @param frame
 	 *            La région du plan dans laquelle calculer la fractale
 	 * @param width
 	 *            La largeur de l'accumulateur à générer
 	 * @param height
 	 *            La hauteur de l'accumulateur à générer
 	 * @param density
 	 *            La densité utilisée pour générer les points de la fractale
 	 *            (influe sur le nombre de points calculés par l'algorithme)
 	 * @return L'accumulateur contenant les points de la fractale
 	 */
 	public FlameAccumulator compute(Rectangle frame, int width, int height,
 			int density) {
 		// On initialise un random déterminé par la graine 2013
 		Random randomizer = new Random(2013);
 
 		// Création des variables utilisées dans les boucles de calcul
 		Point point = new Point(0, 0);
 		int k = 20;
 		int transformationNum;
 
 		// On récupère une fois pour toutes la taille de la liste de
 		// transformations
 		int size = m_transforms.size();
		
 		// Création du builder
 		FlameAccumulator.Builder builder = new FlameAccumulator.Builder(frame,
 				width, height);
		
		if (size == 0) return builder.build();
		
 		// Garde en mémoire la couleur du dernier point accumulé
 		double lastColor = 0;
 
 		// 20 premières itérations dans le vide pour l'algorithme du chaos
 		for (int i = 0; i < k; i++) {
 			transformationNum = randomizer.nextInt(size);
 			point = m_transforms.get(transformationNum).transformPoint(point);
 			lastColor = (lastColor + m_colorIndexes[transformationNum]) / 2.0;
 		}
 
 		// Iterations accumulées pour le rendu
 		int m = density * width * height;
 		for (int i = 0; i < m; i++) {
 			transformationNum = randomizer.nextInt(size);
 			point = m_transforms.get(transformationNum).transformPoint(point);
 			lastColor = (lastColor + m_colorIndexes[transformationNum]) / 2.0;
 			
 			if(frame.contains(point))
 				builder.hit(point, lastColor);
 		}
 
 		// On construit l'accumulateur
 		return builder.build();
 	}
 
 	/**
 	 * @param index
 	 *            L'index de la transformation de laquelle on désire avoir
 	 *            l'index de couleur
 	 * @return L'index de couleur associé à la transformation
 	 */
 	private double getColorIndex(int index) {
 
 		if (index >= 2) {
 			double denominateur = Math.pow(2,
 					Math.ceil(Math.log(index) / Math.log(2)));
 
 			return ((2 * index - 1) % denominateur) / denominateur;
 		} else {
 			return index;
 		}
 	}
 
 	/**
 	 * Classe modélisant un bâtisseur pour une fractale Flame
 	 */
 	public static class Builder {
 
 		/**
 		 * La liste des bâtisseurs pour les transformations de la fractale Flame
 		 * qui sera construite
 		 */
 		private List<FlameTransformation.Builder> m_transformationsBuilders;
 
 		/**
 		 * Construit un bâtisseur à partir d'une fractale existante
 		 * 
 		 * @param flame
 		 *            La fractale flame
 		 */
 		public Builder(Flame flame) {
 			m_transformationsBuilders = new ArrayList<FlameTransformation.Builder>();
 			for (FlameTransformation transformation : flame.m_transforms) {
 				m_transformationsBuilders.add(new FlameTransformation.Builder(
 						transformation));
 			}
 		}
 
 		/**
 		 * @return Le nombre actuel de transformations de la fractale
 		 */
 		public int transformationsCount() {
 			return m_transformationsBuilders.size();
 		}
 
 		/**
 		 * Ajoute une transformation de type flame à la fractale
 		 * 
 		 * @param transformation
 		 *            La transformation
 		 */
 		public void addTransformation(FlameTransformation transformation) {
 			m_transformationsBuilders.add(new FlameTransformation.Builder(
 					transformation));
 		}
 
 		/**
 		 * @param index
 		 * @return La composante affine de la transformation d'index
 		 *         <i>index</i> de la fractale
 		 * @throws IllegalArgumentException
 		 *             Si l'index n'est pas valide
 		 */
 		public AffineTransformation affineTransformation(int index) {
 			checkIndex(index);
 
 			return m_transformationsBuilders.get(index).affineTransformation();
 		}
 
 		/**
 		 * Remplace la composante affine de la transformation d'index
 		 * <i>index</i> par <i>newTransformation</i>
 		 * 
 		 * @param index
 		 * @param newTransformation
 		 * @throws IllegalArgumentException
 		 *             Si l'index n'est pas valide
 		 */
 		public void setAffineTransformation(int index,
 				AffineTransformation newTransformation) {
 
 			checkIndex(index);
 			m_transformationsBuilders.get(index).setAffineTransformation(
 					newTransformation);
 		}
 
 		/**
 		 * Retourne le poids de la variation <i>variation</i> pour la
 		 * transformation d'index <i>index</i>
 		 * 
 		 * @param index
 		 *            L'index de la transformation
 		 * @param variation
 		 *            La variation dont on veut récupérer le poids
 		 * @return Le poids demandé
 		 * 
 		 * @throws IllegalArgumentException
 		 *             Si l'index n'est pas valide
 		 */
 		public double variationWeight(int index, Variations variation) {
 			checkIndex(index);
 
 			return m_transformationsBuilders.get(index).weight(
 					variation.index());
 		}
 
 		/**
 		 * Modifie le poids de la variation <i>variation</i> pour la
 		 * transformation d'index <i>index</i>
 		 * 
 		 * @param index
 		 *            L'index de la transformation
 		 * @param variation
 		 *            La variation dont on veut modifier le poids
 		 * @param newWeight
 		 *            Le nouveau poids à affecter à la variation, pour cette
 		 *            transformation
 		 * @throws IllegalArgumentException
 		 *             Si l'index n'est pas valide
 		 */
 		public void setVariationWeight(int index, Variations variation,
 				double newWeight) {
 
 			checkIndex(index);
 
 			m_transformationsBuilders.get(index).setWeight(variation.index(),
 					newWeight);
 		}
 
 		/**
 		 * Supprime le bâtisseur de transformation flame à l'index <i>index</i>
 		 * 
 		 * @param index
 		 * @throws IllegalArgumentException
 		 *             Si l'index n'est pas valide
 		 */
 		public void removeTransformation(int index) {
 			checkIndex(index);
 			m_transformationsBuilders.remove(index);
 		}
 
 		/**
 		 * Construit une fractale Flame à partir des informations récoltées
 		 * 
 		 * @return La fractale Flame construite
 		 */
 		public Flame build() {
 			List<FlameTransformation> builtTransformations = new ArrayList<FlameTransformation>();
 			for (FlameTransformation.Builder transfoBuilder : m_transformationsBuilders) {
 				builtTransformations.add(transfoBuilder.build());
 			}
 
 			return new Flame(builtTransformations);
 		}
 
 		/**
 		 * Vérifie si l'index passé en argument est valide pour la liste des
 		 * bâtisseurs de transformation flame
 		 * 
 		 * @param index
 		 *            L'index à vérifier
 		 * @throws IllegalArgumentException
 		 *             Si l'index n'est pas valide
 		 */
 		private void checkIndex(int index) {
 			if (index < 0 || index >= m_transformationsBuilders.size()) {
 				throw new IllegalArgumentException();
 			}
 		}
 
 		public FlameTransformation getTransformation(int index) {
 			return m_transformationsBuilders.get(index).build();
 		}
 	}
 
 }
