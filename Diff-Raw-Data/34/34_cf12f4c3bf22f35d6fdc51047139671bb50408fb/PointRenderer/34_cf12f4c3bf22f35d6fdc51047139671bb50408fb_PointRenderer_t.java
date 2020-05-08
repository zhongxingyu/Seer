 package edu.virginia.gfx.gensat.iregistration.util;
 
 import java.util.Arrays;
 
 import javax.media.opengl.GL2;
 
 import com.jogamp.opengl.util.texture.Texture;
 import com.jogamp.opengl.util.texture.TextureData;
 
 public class PointRenderer implements Renderable {
 	private final float[] sqVert = new float[] { -0.5f, -0.5f, 0.5f, -0.5f,
 			0.5f, 0.5f, -0.5f, 0.5f };
 	private final float[] sqTex = new float[] { 0, 0, 1, 0, 1, 1, 0, 1 };
 	private final int[] triangles = new int[] { 0, 1, 2, 0, 2, 3 };
 
 	private Mesh2DShader shader;
 
 	private final TextureData[] img;
 	private Texture[] tex;
 	/**
 	 * Interlaced (x, y) point data pairs.
 	 * 
 	 * The nth point is (xy[2*n + 0], xy[2*n + 1]).
 	 */
 	public final float[] xy;
 	/**
 	 * Mapping from point indices to indices in img for the texture to be used
 	 * to render that point.
 	 */
 	public final int[] texIndex;
 	/**
 	 * The color to use when rendering each point. The rendered texture is
 	 * multiplied by this value.
 	 */
 	public final int[] color;
 
 	public float radius = 0.025f;
 
 	public PointRenderer(TextureData[] img, float[] xy) {
 		this(img, xy, 0, 0xffffffff);
 	}

	public PointRenderer(TextureData[] img, float[] xy, int defaultImgIndex,
			int defaultColor) {
 		this.img = img;
 		this.xy = xy;
 		this.texIndex = new int[xy.length / 2];
 		Arrays.fill(texIndex, defaultImgIndex);
 		this.color = new int[xy.length / 2];
 		Arrays.fill(color, defaultColor);
 	}
 
 	@Override
 	public void render(GL2 gl, float[] parent) {
 		gl.glBlendEquation(GL2.GL_ADD);
 		gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

 		float[] mat = new float[16];
		Matrix.setIdentityM(mat, 0);
		Matrix.scaleM(mat, 0, radius, radius, 0);
		for (int i = 0; i < xy.length / 2; i++) {
			// TODO this would ideally be done in a vertex shader

			// Transform the origin by the parent matrix to know where to
			// translate the point. This lets us ignore all other components
			// of the transformation and render points with constant radius and
			// rotation.
			float[] translation = new float[4];
			translation[0] = xy[i * 2 + 0];
			translation[1] = xy[i * 2 + 1];
			translation[2] = 1;
			translation[3] = 1;
			Matrix.multiplyMV(translation, 0, parent, 0, translation, 0);

			// set the x and y translational components of the matrix to what
			// we computed is the translational component of all top matrices
			// when acting on the center of the point we're rendering
			mat[12] = translation[0];
			mat[13] = translation[1];
			shader.use(gl, sqVert, sqTex, triangles, mat, tex[texIndex[i]],
					color[i]);
 		}
 	}
 
 	@Override
 	public void destroy(GL2 gl) {
 		for (int i = 0; i < tex.length; i++) {
 			tex[i].destroy(gl);
 		}
 		shader.destroy(gl);
 	}
 
 	@Override
 	public void init(GL2 gl) {
 		tex = new Texture[img.length];
 		for (int i = 0; i < tex.length; i++) {
 			tex[i] = new Texture(gl, img[i]);
 		}
 		shader = new Mesh2DShader(gl);
 	}
 }
