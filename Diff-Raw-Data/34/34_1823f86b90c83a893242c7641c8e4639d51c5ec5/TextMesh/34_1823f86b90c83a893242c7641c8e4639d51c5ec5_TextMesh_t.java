 package com.unknownloner.lonelib.graphics.font;
 
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL15;
 
 import com.unknownloner.lonelib.graphics.Mesh;
 import com.unknownloner.lonelib.graphics.buffers.VertexArrayObject;
 import com.unknownloner.lonelib.graphics.buffers.VertexAttribPointer;
 import com.unknownloner.lonelib.graphics.buffers.VertexBufferObject;
 import com.unknownloner.lonelib.math.Vec3;
 import com.unknownloner.lonelib.math.Vec4;
 
 public class TextMesh implements Mesh {
 	
 	private List<String> lines = new ArrayList<String>();
 	private VertexBufferObject vertBuffer = new VertexBufferObject(GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW, 0);
 	private VertexBufferObject indBuffer = new VertexBufferObject(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_STATIC_DRAW, 0);
 	private VertexArrayObject vao = new VertexArrayObject(
 			new VertexAttribPointer(vertBuffer, 0, 3, GL11.GL_FLOAT, false, 9 * 4, 0),     //Position
 			new VertexAttribPointer(vertBuffer, 1, 4, GL11.GL_FLOAT, false, 9 * 4, 3 * 4), //Color
 			new VertexAttribPointer(vertBuffer, 2, 2, GL11.GL_FLOAT, false, 9 * 4, 7 * 4)  //Tex Coords
 	);
 	private FontRenderer fr;
 	private int vertCount;
 	private int indCount;
 	private float textSize;
 	private Vec3 pos;
 	private Vec4 color;
 	
	public TextMesh(FontRenderer fr, float textSize, Vec3 topLeft, Vec4 color) {
 		this.fr = fr;
 		this.textSize = textSize;
		this.pos = topLeft.sub(new Vec3(0, textSize, 0));
 		this.color = color;
 	}
 	
	public TextMesh(FontRenderer fr, float textSize, Vec3 topLeft, Vec4 color, String initialText) {
		this(fr, textSize, topLeft, color);
 		addLine(initialText);
 	}
 	
 	public int addLine(String line) {
 		StringBuilder filteredLine = new StringBuilder();
 		for(char c : line.toCharArray()) {
 			if(fr.canDisplay(c)) {
 				filteredLine.append(c);
 			}
 		}
 		line = filteredLine.toString();
 		vertCount += line.length() * 4;
 		indCount += line.length() * 6;
 		lines.add(line);
 		return lines.size() - 1;
 	}
 	
 	public void removeLine(int index) {
 		String removal = lines.remove(index);
 		vertCount -= removal.length() * 4;
 		indCount -= removal.length() * 6;
 	}
 	
 	public List<String> getLines() {
 		return lines;
 	}
 	
 	@Override
 	public void finalizeMesh() {
 		vertBuffer.bufferData(vertCount * 9 * 4 /*9 floats per vert*/, GL15.GL_STATIC_DRAW);
 		indBuffer.bufferData(indCount * 2 /* shorts */, GL15.GL_STATIC_DRAW);
 		FloatBuffer verts = GL15.glMapBuffer(GL15.GL_ARRAY_BUFFER, GL15.GL_WRITE_ONLY, vertCount * 9 * 4, null).asFloatBuffer();
 		ShortBuffer inds = GL15.glMapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_WRITE_ONLY, indCount * 2, null).asShortBuffer();
 		float x = pos.getX();
 		float y = pos.getY();
 		float z = pos.getZ();
 		float r = color.getX();
 		float g = color.getY();
 		float b = color.getZ();
 		float a = color.getW();
 		short ind = 0;
 		for(String line : lines) {
 			fr.addString(verts, line, x, y, z, r, g, b, a, textSize, true);
			for(int i = 0; i < line.length(); i++) {
				inds.put(new short[] {
						ind,
						(short)(ind + 1),
						(short)(ind + 2),
						(short)(ind + 2),
						(short)(ind + 3),
						ind
				});
				ind += 4;
			}
			y -= textSize;
			
 		}
 		GL15.glUnmapBuffer(GL15.GL_ARRAY_BUFFER);
 		GL15.glUnmapBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER);
 	}
 
 	@Override
 	public void render() {
 		vao.assign();
 		indBuffer.assign();
 		GL11.glDrawElements(GL11.GL_TRIANGLES, indCount, GL11.GL_UNSIGNED_SHORT, 0);
 		if(!VertexArrayObject.vaoSupport) {
 			vao.unassign();
 		}
 	}
 
 	@Override
 	public void delete() {
 		vao.delete();
 		indBuffer.delete();
 		vertBuffer.delete();
 	}
 
 	@Override
 	public void reset() {
 		vertBuffer.bufferData(0, GL15.GL_STATIC_DRAW);
 		indBuffer.bufferData(0, GL15.GL_STATIC_DRAW);
 	}
 
 }
