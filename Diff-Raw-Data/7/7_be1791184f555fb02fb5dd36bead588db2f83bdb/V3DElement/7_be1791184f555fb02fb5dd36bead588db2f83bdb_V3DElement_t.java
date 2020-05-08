 // Copyright 2010 DEF
 //
 // This file is part of V3dScene.
 //
 // V3dScene is free software: you can redistribute it and/or modify
 // it under the terms of the GNU Lesser General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // V3dScene is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public License
 // along with V3dScene.  If not, see <http://www.gnu.org/licenses/>.
 package fr.def.iss.vd2.lib_v3d.element;
 
 import java.nio.FloatBuffer;
 
 import org.lwjgl.opengl.GL11;
 
import com.irr310.server.Vect3;

 import fr.def.iss.vd2.lib_v3d.V3DContext;
 import fr.def.iss.vd2.lib_v3d.V3DContextElement;
 import fr.def.iss.vd2.lib_v3d.V3DVect3;
 import fr.def.iss.vd2.lib_v3d.camera.V3DCamera;
 import fr.def.iss.vd2.lib_v3d.element.animation.V3DAnimation;
 import fr.def.iss.vd2.lib_v3d.element.animation.V3DNullAnimation;
 
 /**
  * Un élement représente un objet présent dans une scène.
  * 
  * <pre>
  * Une scène contient des éléments organisés en arborescence. Si un élément
  * ne peut pas intrinsèquement posséder des fils, certains types d'éléments le
  * peuvent.
  * 
  * Chaque élément possèdent une position, un rotation et une échelle qui sont
  * relatives à leur parent dans l'arborescence. Chaque élément possède aussi une
  * méthode de dessin qui va lui permettre de se dessiner sur le canvas. Les
  * éléments ayant des fils vont souvent aussi dessiner leurs enfants.
  * 
  * V3DElement est une classe abstraite. Les éléments nativement disponible dans
  * V3DScene sont listés ici.
  * - Les éléments de dessin permettent de dessiner des formes 2d ou 3d dans la
  *   scène :
  *     - V3DRectangle : dessine un rectangle en 2d.
  *     - V3DLine : dessine une ligne.
  *     - V3DBox : dessine un parallélépipède rectangle.
  *     - V3DCircle : dessine un cercle en 2d.
  *     - V3DPoint : dessine un point.
  *     - V3DPolygon : dessine un polygon convexe, concave et/ou troué.
  *     - V3DPolygonBox : dessine un prisme dont le polygone directeur peut être
  *     convexe, concave et/ou troué.
  *     - V3DPolygonWall : dessine une surface prismatique dont le polygone
  *       directeur peut être convexe, concave et/ou troué.
  *     - V3DSprite : dessine un carré texturé.
  *     - V3DrawElement : dessine un modèle 2d ou 3d stocké au format v3draw.
  * 
  * - Les éléments d'arborescence permettent de créer un arbre d'éléments à
  *   afficher :
  *     - V3DGroupElement : élément conteneur qui dessine ses fils dans l'ordre
  *       d'ajout.
  *     - V3DOrderedGroupElement : élément conteneur qui dessine ses fils dans
  *       l'ordre choisi.
  *     - V3DNeutralElement : Élément transparent qui se contente de dessiner
  *       son unique fils. Cet élement peut servir à choisir si une rotation est
  *       appliquée avant ou après la translation.
  * 
  * - Les modificateurs permettent de configurer le dessin de leur sous-arbre:
  *     - V3DColorElement : definit la couleur à appliquer pour le dessin du
  *       sous-arbre fils.
  *     - V3DAbsoluteSizeElement : contraint le sous-arbre fils à avoir toujours
  *       une taille constante en pixels par rapport à l'écran, quel que soit le
  *       zoom de la caméra.
  *     - V3DNoDepthElement : dessine le sous-arbre fils en ignorant la profondeur
  *       des objets (masquage réalisé par le Z-buffer).
  * 
  * Un élement peut se voir attribuer une animation (V3DAnimation) qui peut en
  * modifier le rendu ou même empécher son affichage.
  * 
  * Il est possible de masquer totalement un élément et ses fils en jouant sur
  * l'attribut "visible".
  * 
  * Une des fonctionalités de V3DScene est de fournir la liste des éléments
  * actuellement sous le curseur de la souris ou de réagir aux clics sur des
  * éléments. Ces fonctionnalités utilisent un mécanisme appelé picking qui
  * qui consiste à dessiner une petite scene dans un cadre de quelques pixels
  * autour du curseur de la souris et d'enregistrer la liste des objets.
  * Après chaque rendu de scène, une autre passe de rendu appelée "select" est
  * faite autour du curseur. Mais en fonction des besoins, on veut pouvoir
  * choisir précisement quels sont les objets qui peuvent être "selectable" et
  * apparaitre dans la liste des éléments sous la souris.
  * 
  * Pour configurer le select précisement, il faut utiliser 2 attributs : 
  * tangible et selectable.
  * Par défaut, aucun élément n'est selectable et tous sont tangibles.
  * 
  * On distingue 3 cas:
  *   - tangible et selectable : l'élément peut être selectionné. Il ne compte
  *     pas pour ses parents et compte pour ses enfant qui ne sont pas
  *     selectable.
  *   - tangible et non selectable : l'élement ne peut pas être selectionnné mais
  *     compte pour son parent selectionnable le plus proche. Ses enfants
  *     selectable peuvent être selectionnable.
  *   - non tangible : cet élément ne peut pas être selectionné, ne compte pas 
  *     pour ses parents et aucun de ses enfants ne peut être selectionné.
  * 
  * Exemple d'utilisation de tangible et selectable.
  * 
  * Supposons que le graphe de scène ressemble à cela :
  * 
  *                       * racine de la scène
  *                       |
  *         +-------------+-------------+
  *         |                           |
  *         * plan (route)              * groupe (vélo)
  *                                     |
  *                                     |
  *    +-------------------------+------+--------------------+
  *    |                         |                           |
  *    * cylindre (roue avant)   * cylindre (roue arrière)   * boite (cadre)
  * 
  * 
  * Voici une version allégée:
  * 
  *         * 
  *         |
  *    +----+----+
  *    |         |
  *    * P       * V
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1      * R2      * C
  * 
  * Pour décrire les 3 cas pour chaque élément, voici le formalisme utilisé:
  *  - (s) : selectable et tangible
  *  - (t) : tanglible non selectable
  *  - (0) : non tangible
  * 
  * Nous considèrerons que le dessins des objets P, R1, R2 et C se trouvent sous
  * la souris et qu'ils sont tous potentiellement selectionnable avec les bons
  * réglages. V, en tant que groupe élement ne dessine rien lui même mais utilise
  * ses 3 fils pour se dessiner.
  * 
  * Exemple 1: Tous les objets tangibles
  * 
  *         *
  *         |
  *    +----+----+
  *    |         |
  *    * P (t)   * V (t)
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1 (t)  * R2 (t)  * C (t)
  * 
  * Résulat : []. Aucun élément n'est selectionné car aucun élément n'est
  * selectionnable et tangible.
  * 
  * Exemple 2: Un éléments feuille tangible et selectable
  * 
  *         *
  *         |
  *    +----+----+
  *    |         |
  *    * P (s)   * V (t)
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1 (t)  * R2 (t)  * C (t)
  * 
  * Résulat : [P]. P est selectionné car il est selectionnable et tangible et
  * produit un dessin.
  * 
  * Exemple 3: Un élément non feuille tangible et selectable
  * 
  *         *
  *         |
  *    +----+----+
  *    |         |
  *    * P (s)   * V (s)
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1 (t)  * R2 (t)  * C (t)
  * 
  * Résulat : [P, V]. P est selectionné pour les même raisons que dans
  * l'exemple 2. V l'est aussi car il est selectionnable et tangible et même s'il
  * ne produit pas de dessin,  ses fils R1, R2 et C ne sont pas selectable et
  * produisent un dessin et comptent donc pour V.
  * 
  * Exemple 4: Un éléments non feuille tangible et selectable dont aucune
  * feuille est tangible.
  * 
  *         *
  *         |
  *    +----+----+
  *    |         |
  *    * P (s)   * V (s)
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1 (0)  * R2 (0)  * C (0)
  * 
  * Résulat : [P]. P est selectionné pour les même raisons que dans l'exemple 2.
  * V n'est pas selectionné car bien que selectable et tangible, ni lui ni des
  * fils qui compte pour lui ne produisent de dessin.
  * 
  * Exemple 5: Un éléments non feuille tangible et selectable dont tous les fils
  * sont aussi tangible et selectable.
  * 
  *         *
  *         |
  *    +----+----+
  *    |         |
  *    * P (s)   * V (s)
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1 (s)  * R2 (s)  * C (s)
  * 
  * Résulat : [P, R1, R2, C]. P, R1, R2 et C sont selectable et tangible et
  * produisent un dessin, donc ils sont selectionnés. V n'est pas
  * selectionné car bien que selectable et tangible, ses fils étant aussi
  * selectable, ils produisent un dessin pour eux même.
  * 
  * Exemple 6: Un élément non feuille tangible et selectable dont tous les fils
  * sont aussi tangible et selectable sauf un qui est juste tangible.
  * 
  *         *
  *         |
  *    +----+----+
  *    |         |
  *    * P (s)   * V (s)
  *              |
  *    +---------+---------+
  *    |         |         |
  *    * R1 (s)  * R2 (t)  * C (s)
  * 
  * Résulat : [P, R1, C, V]. P, R1 et C sont selectable et tangible et
  * produisent un dessin donc ils sont selectionnés. V est selectionné, R2 dessine
  * pour lui.
  * 
  * Dans l'exemple 6, imaginons que l'on deplace la souris de tel sorte qu'elle ne
  * survole plus que le dessin de R1, alors le résultat sera [R1]. Si la souris
  * ne survolle que R2, le résultat sera [V].
  * </pre>
  * 
  * @author fberto
  */
 abstract public class V3DElement extends V3DContextElement {
 
 	/**
 	 * Crée un nouvel élément dans le context fournis en paramètre
 	 * 
 	 * @param context
 	 */
 	public V3DElement(V3DContext context) {
 		super(context);
 		id = context.getIdAllocator().getNewId(this);
 	}
 
 	public V3DVect3 getPosition() {
 		return position;
 	}
 
 	public void setPosition(V3DVect3 position) {
 		this.position.copy(position);
 	}
 
 	public V3DVect3 getRotation() {
 		return rotation;
 	}
 
 	public void setRotation(V3DVect3 rotation) {
 		this.rotation = rotation;
 	}
 
 	public V3DVect3 getScale() {
 		return scale;
 	}
 
 	public void setScale(V3DVect3 scale) {
 		this.scale = scale;
 	}
 
 	public void setPosition(float x, float y, float z) {
 		this.position.x = x;
 		this.position.y = y;
 		this.position.z = z;
 	}
 
 	public void setRotation(float x, float y, float z) {
 		this.rotation.x = x;
 		this.rotation.y = y;
 		this.rotation.z = z;
 	}
 
 	public void setTransformMatrix(FloatBuffer matrix) {
 		this.transformMatrix = matrix;
		
		this.position.x = matrix.get(12);
		this.position.y = matrix.get(13);
		this.position.z = matrix.get(14);
 	}
 	
 	public void setScale(float x, float y, float z) {
 		this.scale.x = x;
 		this.scale.y = y;
 		this.scale.z = z;
 	}
 
 	public void setScale(float scale) {
 		this.scale.x = scale;
 		this.scale.y = scale;
 		this.scale.z = scale;
 	}
 
 	/**
 	 * Renvoie la boite englobante
 	 * 
 	 * @return
 	 */
 	abstract public V3DBoundingBox getBoundingBox();
 
 	/**
 	 * Permet de choisir ce qui sera dessiné. Utilisé cette fonction masque la
 	 * bounding box si elle était affichée
 	 * 
 	 * @param b
 	 *            active ou désactive le rendu normal
 	 */
 	public void setShowMode(boolean b) {
 		setShowMode(b, false);
 	}
 
 	/**
 	 * Permet de choisir ce qui sera dessiné.
 	 * 
 	 * @param shape
 	 *            active ou désactive le rendu normal
 	 * @param boundingBox
 	 *            active ou désactive le rendu de la bounding box
 	 */
 	public void setShowMode(boolean shape, boolean boundingBox) {
 		showShape = shape;
 		showBoundingBox = boundingBox;
 	}
 
 	/**
 	 * Définie l'animation de l'élément
 	 * 
 	 * @param animation
 	 */
 	public void setAnimation(V3DAnimation animation) {
 		this.animation = animation;
 	}
 
 	public boolean isShowShape() {
 		return showShape;
 	}
 
 	public boolean isShowBoundingBox() {
 		return showBoundingBox;
 	}
 
 	public boolean isSelectable() {
 		return selectable;
 	}
 
 	public void setSelectable(boolean selectable) {
 		this.selectable = selectable;
 	}
 
 	public boolean isTangible() {
 		return tangible;
 	}
 
 	public void setTangible(boolean tangible) {
 		this.tangible = tangible;
 	}
 
 	public void setVisible(boolean visible) {
 		this.visible = visible;
 	}
 
 	//
 	// ------------------
 	// Private stuff
 	// ------------------
 	//
 	private Boolean inited = false;
 	private V3DVect3 position = new V3DVect3(0, 0, 0);
 	private V3DVect3 rotation = new V3DVect3(0, 0, 0);
 	private FloatBuffer transformMatrix = null;
 	private V3DVect3 scale = new V3DVect3(1, 1, 1);
 	private V3DAnimation animation = new V3DNullAnimation();
 	private boolean showBoundingBox = false;
 	private boolean showShape = true;
 	private boolean selectable = false;
 	private boolean tangible = true;
 	private boolean visible = true;
 	private long id = 0;
 
 	/**
 	 * Internal public method Méthode principal du rendu d'un élément. Cette
 	 * méthode appelle le code de rendu spécifique de l'élément via la méthode
 	 * doDisplay si nécessaire.
 	 * 
 	 * @param gl
 	 *            context gl du rendu
 	 * @param camera
 	 *            point de vue
 	 */
 	final public void display(V3DCamera camera) {
 		if (!visible) {
 			return;
 		}
 
 		if (!inited) {
 			init();
 		}
 
 		GL11.glPushMatrix();
 		if (transformMatrix != null) {
 			GL11.glMultMatrix(transformMatrix);
 		} else {
 			if (rotation.x != 0) {
 				GL11.glRotatef(rotation.x, 1, 0, 0);
 			}
 			if (rotation.y != 0) {
 				GL11.glRotatef(rotation.y, 0, 1, 0);
 			}
 			if (rotation.z != 0) {
 				GL11.glRotatef(rotation.z, 0, 0, 1);
 			}
 			
 			if (position.x != 0 || position.y != 0 || position.z != 0) {
 				GL11.glTranslatef(position.x, position.y, position.z);
 			}
 		}
 
 		
 
 		if (scale.x != 1 || scale.y != 1 || scale.z != 1) {
 			GL11.glScalef(scale.x, scale.y, scale.z);
 		}
 
 		animation.preDisplay();
 
 		if (animation.doDisplay()) {
 
 			if (showShape) {
 				doDisplay(camera);
 			}
 
 			if (showBoundingBox) {
 				getBoundingBox().display();
 			}
 		}
 		animation.postDisplay();
 
 		GL11.glPopMatrix();
 	}
 
 	/**
 	 * Internal public method Méthode principal de l'initialisation d'un
 	 * élément. Cette méthode appelle le code d'initialisation spécifique de
 	 * l'élément via la méthode doInit si nécessaire.
 	 * 
 	 * @param gl
 	 *            context gl de l'initialisation
 	 */
 	final public void init() {
 		if (!inited) {
 			inited = true;
 			doInit();
 		}
 
 	}
 
 	/**
 	 * Internal public method Méthode appelé par init. Contient le code
 	 * spécifique d'initialisation de l'élément
 	 * 
 	 * @param gl
 	 */
 	abstract protected void doInit();
 
 	/**
 	 * Internal public method Méthode appelé par display. Contient le code
 	 * spécifique de rendu de l'élément
 	 * 
 	 * @param gl
 	 * @param camera
 	 */
 	abstract protected void doDisplay(V3DCamera camera);
 
 	/**
 	 * Internal public method Méthode appelé par select. Contient le code
 	 * spécifique du rendu pour la selection de l'élément. Par défautl, le rendu
 	 * pour la selection est le dessin de la bounding box de l'élément.
 	 * 
 	 * @param gl
 	 * @param camera
 	 * @param parentId
 	 */
 	protected void doSelect(V3DCamera camera, long parentId) {
 		if (parentId != 0) {
 			getBoundingBox().displayFaces();
 		}
 	}
 
 	/**
 	 * Internal public method Renvoie l'état d'initialisation de l'élément
 	 * 
 	 * @param gl
 	 *            context gl courant. Si le context gl change, il faut
 	 *            initialiser l'élément à nouveau.
 	 * @return état d'initialisation de l'élément
 	 */
 	public boolean isInit() {
 		return inited;
 	}
 
 	/**
 	 * Internal public method Méthode principal du rendu de selection d'un
 	 * élément. Cette méthode appelle le code de rendu de selection spécifique
 	 * de l'élément via la méthode doSelect si nécessaire. Les animations
 	 * n'entrent pas en jeu dans le rendu de selection.
 	 * 
 	 * @param gl
 	 *            context gl du rendu de selection
 	 * @param camera
 	 *            point de vue
 	 */
 	final public void select(V3DCamera camera, long parentId) {
 
 		if (!inited) {
 			init();
 		}
 
 		if (!isTangible() || !showShape) {
 			return;
 		}
 
 		GL11.glPushMatrix();
 
 		// TODO: verify this cast. Maybe change id from long to int
 		if (selectable) {
 			GL11.glLoadName((int) id);
 		}
 
 		if (rotation.x != 0) {
 			GL11.glRotatef(rotation.x, 1, 0, 0);
 		}
 		if (rotation.y != 0) {
 			GL11.glRotatef(rotation.y, 0, 1, 0);
 		}
 		if (rotation.z != 0) {
 			GL11.glRotatef(rotation.z, 0, 0, 1);
 		}
 
 		if (position.x != 0 || position.y != 0 || position.z != 0) {
 			GL11.glTranslatef(position.x, position.y, position.z);
 		}
 
 		if (scale.x != 1 || scale.y != 1 || scale.z != 1) {
 			GL11.glScalef(scale.x, scale.y, scale.z);
 		}
 
 		if (selectable) {
 			doSelect(camera, id);
 		} else {
 			doSelect(camera, parentId);
 		}
 
 		if (selectable) {
 			GL11.glLoadName((int) parentId);
 		}
 
 		GL11.glPopMatrix();
 
 	}
 }
