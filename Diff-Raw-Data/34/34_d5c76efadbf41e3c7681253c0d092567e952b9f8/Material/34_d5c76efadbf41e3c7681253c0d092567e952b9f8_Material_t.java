 package atlantis.engine.graphics3d;
 
 import java.awt.Color;
 
 import atlantis.framework.graphics.Texture2D;
 
 /**
  * Define a material applied to a 3D object.
  * @author Yannick
  */
 public class Material {
 	protected String name;
     protected String id;
     protected String diffuseTextureName;
     protected Texture2D diffuseTexture;
     protected Color ambientColor;
     protected Color diffuseColor;
     protected Color specularColor;
     protected float specularPower;
     protected Color emissiveColor;
     protected float alpha;
     protected boolean backFaceCulling;
 
     public Material()
     {
         this.name = "Material";
         this.id = "Material";
         this.diffuseTextureName = "";
         this.ambientColor = Color.white;
         this.diffuseColor = Color.white;
         this.specularColor = Color.black;
         this.specularPower = 1.0f;
         this.emissiveColor = Color.white;
         this.alpha = 1.0f;
         this.backFaceCulling = true;
     }
 
    public void load()
     {
         if (diffuseTextureName != "") {
             this.diffuseTexture = Texture2D.createFromPath(this.diffuseTextureName, 1);
         }
     }
 
 	/**
 	 * @return the name
 	 */
 	public final String getName() {
 		return name;
 	}
 
 	/**
 	 * @return the id
 	 */
 	public final String getId() {
 		return id;
 	}
 
 	/**
 	 * @return the diffuseTextureName
 	 */
 	public final String getDiffuseTextureName() {
 		return diffuseTextureName;
 	}
 
 	/**
 	 * @return the diffuseTexture2D
 	 */
 	public final Texture2D getTexture() {
 		return diffuseTexture;
 	}
 
 	/**
 	 * @return the ambientColor
 	 */
 	public final Color getAmbientColor() {
 		return ambientColor;
 	}
 
 	/**
 	 * @return the diffuseColor
 	 */
 	public final Color getDiffuseColor() {
 		return diffuseColor;
 	}
 
 	/**
 	 * @return the specularColor
 	 */
 	public final Color getSpecularColor() {
 		return specularColor;
 	}
 
 	/**
 	 * @return the specularPower
 	 */
 	public final float getSpecularPower() {
 		return specularPower;
 	}
 
 	/**
 	 * @return the emissiveColor
 	 */
 	public final Color getEmissiveColor() {
 		return emissiveColor;
 	}
 
 	/**
 	 * @return the alpha
 	 */
 	public final float getAlpha() {
 		return alpha;
 	}
 
 	/**
 	 * @return the backFaceCulling
 	 */
 	public final boolean isBackFaceCulling() {
 		return backFaceCulling;
 	}
 
 	public final void setId(String id) {
 		this.id = id;
 	}
 	
 	/**
 	 * @param name the name to set
 	 */
 	public final void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * @param diffuseTextureName the diffuseTextureName to set
 	 */
 	public final void setDiffuseTextureName(String diffuseTextureName) {
 		this.diffuseTextureName = diffuseTextureName;
 	}
 
 	/**
 	 * @param diffuseTexture2D the diffuseTexture2D to set
 	 */
 	public final void setDiffuseTexture(Texture2D diffuseTexture) {
 		this.diffuseTexture = diffuseTexture;
 	}
 
 	/**
 	 * @param ambientColor the ambientColor to set
 	 */
 	public final void setAmbientColor(Color ambientColor) {
 		this.ambientColor = ambientColor;
 	}
 
 	/**
 	 * @param diffuseColor the diffuseColor to set
 	 */
 	public final void setDiffuseColor(Color diffuseColor) {
 		this.diffuseColor = diffuseColor;
 	}
 
 	/**
 	 * @param specularColor the specularColor to set
 	 */
 	public final void setSpecularColor(Color specularColor) {
 		this.specularColor = specularColor;
 	}
 
 	/**
 	 * @param specularPower the specularPower to set
 	 */
 	public final void setSpecularPower(float specularPower) {
 		this.specularPower = specularPower;
 	}
 
 	/**
 	 * @param emissiveColor the emissiveColor to set
 	 */
 	public final void setEmissiveColor(Color emissiveColor) {
 		this.emissiveColor = emissiveColor;
 	}
 
 	/**
 	 * @param alpha the alpha to set
 	 */
 	public final void setAlpha(float alpha) {
 		this.alpha = alpha;
 	}
 
 	/**
 	 * @param backFaceCulling the backFaceCulling to set
 	 */
 	public final void setBackFaceCulling(boolean backFaceCulling) {
 		this.backFaceCulling = backFaceCulling;
 	}
 }
