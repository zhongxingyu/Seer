 package de.tum.in.cindy3dplugin.jogl.lighting;
 
 import java.awt.Color;
 
 import javax.media.opengl.GL2;
 
 import de.tum.in.cindy3dplugin.jogl.lighting.LightManager.LightType;
 
 public abstract class Light {
 	private Color ambientColor = new Color(0.0f, 0.0f, 0.0f);
 	private Color diffuseColor = new Color(1.0f, 1.0f, 1.0f);
 	private Color specularColor = new Color(1.0f, 1.0f, 1.0f);
 	
 	protected boolean enabled = false;
 	
 	public void setGLState(GL2 gl, int light) {
 		if (enabled) {
			gl.glEnable(light);
 		}
 		else
			gl.glDisable(light);
 
 		gl.glLightfv(light, GL2.GL_AMBIENT,
 				ambientColor.getComponents(null), 0);
 
 		gl.glLightfv(light, GL2.GL_DIFFUSE,
 				diffuseColor.getComponents(null), 0);
 
 		gl.glLightfv(light, GL2.GL_SPECULAR,
 				specularColor.getComponents(null), 0);
 	}
 	
 	public void setAmbientColor(Color ambient) {
 		ambientColor = ambient;
 	}
 	
 	public void setDiffuseColor(Color diffuse) {
 		diffuseColor = diffuse;
 	}
 	
 	public void setSpecularColor(Color specular) {
 		specularColor = specular;
 	}
 	
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 	
 	public boolean isEnabled() {
 		return enabled;
 	}
 	
 	public abstract String getShaderFillIn(int light);
 	
 	public abstract LightType getType();
 }
