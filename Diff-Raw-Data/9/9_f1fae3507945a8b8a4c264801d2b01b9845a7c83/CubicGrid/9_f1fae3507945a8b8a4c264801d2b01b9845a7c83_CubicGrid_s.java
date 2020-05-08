 package editor;
 
 import javax.vecmath.Vector3f;
 
 import de.matthiasmann.twl.Color;
 
 import engine.importing.XGL_Parser;
 import engine.importing.pieces.Material;
 import engine.importing.pieces.Mesh;
 
 public class CubicGrid<E> {
 	private E items[];
 	private Integer size;
 	
 	@SuppressWarnings("unchecked")
 	public CubicGrid(int dim){
 		//Can't instantiate array of E, but can do this:
 		items = (E[]) new Object[dim*dim*dim];
 		size=dim;
 	}
 	
 	public void set(int x, int y, int z, E item){
 		items[x+(size*y)+(size*size*z)] = item;
 	}
 	
 	public E get(int x, int y, int z){
 		return items[x+(size*y)+(size*size*z)];
 	}
 	
 	public Integer size() {
 		return size;
 	}
 	
 	public void setFromXML() {
 		//TODO: parse the XML
 	}
 	
 	@SuppressWarnings("unchecked")
 	public engine.importing.pieces.Model getModel(String path_to_shape) {
 		XGL_Parser parser = new XGL_Parser();
 		engine.importing.pieces.Model base_model = new engine.importing.pieces.Model();
 		engine.importing.pieces.Model full_model = new engine.importing.pieces.Model();
 		Mesh mesh;
 		
 		try {
 			parser.readFile(path_to_shape);
 			base_model = parser.createModel();
 			Block<Integer> current_block;
 			Color mat_color;
 			for(int z=0;z<size;z++) {
 				for(int y=0;y<size;y++) {
 					for(int x=0;x<size;x++) {
 						current_block = ((Block<Integer>)this.get(x,y,z));
 						if(current_block.getActive()) {
 							mesh = new Mesh(base_model.getMesh(0));
 							mesh.transform(
 								new Vector3f(x,-y,z),
 								new Vector3f(0,0,1),
 								new Vector3f(0,1,0)
 							);
 							mat_color = ((Block<Integer>)this.get(x,y,z)).getColor();
 							mesh.setMaterial(
 								new Material(
 									new Vector3f(
 										mat_color.getRedFloat(),
 										mat_color.getGreenFloat(),
 										mat_color.getBlueFloat()
 									),
 									new Vector3f(
 										mat_color.getRedFloat(),
 										mat_color.getGreenFloat(),
 										mat_color.getBlueFloat()
 									)
 								)
 							);
							mesh.calcNormals();
 							full_model.addMesh(mesh);
 						}
 					}
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return full_model;
 	}
 	
 	public String toString() {
 		String value = new String();
 		for(int z=0;z<size;z++) {
 			for(int y=0;y<size;y++) {
 				for(int x=0;x<size;x++) {
 					value+="<cell>\n" +
 						"\t<position>\n" +
 						"\t\t<x>" + x + "</x>\n" +
 						"\t\t<y>" + y + "</y>\n" +
 						"\t\t<z>" + z + "</z>\n" +
 						"\t</position>\n" +
 						"\n\t<color>" +
 						this.get(x, y, z).toString() +
 						"\t</color>\n" +
 						"</cell>\n";
 				}
 			}
 		}
 		return value;
 	}
 }
