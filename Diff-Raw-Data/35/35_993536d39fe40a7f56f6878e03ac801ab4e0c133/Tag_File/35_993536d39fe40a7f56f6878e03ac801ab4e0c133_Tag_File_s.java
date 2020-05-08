 package tagexplorerprocessing2;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 import processing.core.PConstants;
 import processing.core.PGraphics;
 import processing.core.PShape;
 
 public class Tag_File extends Tag {
 
 	static public enum FileType {
 		IMAGE, MESSAGE, TEXT, WEB, FONT, VIDEO, AUDIO, LAYOUT, VECTOR
 	}
 
 	// files
 	FileType fileType = null;
 	float size;
 	String path;
 	Timestamp creation_time;
 	Timestamp expiration_time;
 	Timestamp delete_time;
 	int parent_ID = 0;
 	int origin_ID = 0;
 	int score;
 	
 	int matches = 0;
 	boolean newest = false;
 	
 	PShape shape;
 
 	ArrayList<Tag> attributeBindings = new ArrayList<Tag>();
 	ArrayList<Tag_File> fileBindings = new ArrayList<Tag_File>();
 	ArrayList<Tag_File> versionBindings = new ArrayList<Tag_File>();
 	
 	private ArrayList<Access> accesses = new ArrayList<Access>();
 
 	public ArrayList<Access> getAccesses() {
 		return accesses;
 	}
 
 	public String viewName;
 
 	public Tag_File(String tableName, int id, String name, float size, String path, Timestamp creation_time,
 			Timestamp expiration_time, int parent_ID, int origin_ID, int score) {
 		super(tableName, id, name);
 		this.type = "files";
 		this.size = size;
 		this.path = path;
 		this.creation_time = creation_time;
 		this.expiration_time = expiration_time;
 		this.parent_ID = parent_ID;
 		this.origin_ID = origin_ID;
 		this.score = score;
 
 		this.viewName = name;
 	}
 	
 	public void setShape(PShape shape){		
 		this.shape = shape;
 	}
 	
	public void setShape(PShape shape, float x, float y){		
		this.shape = shape;
		this.shape.translate(x, y);
	}
 	
 	public void setFileType(FileType fileType){
 		this.fileType = fileType;
 	}
 
 	public void setDeleteTime(Timestamp time) {
 		this.delete_time = time;
 	}
 
 	public void setAttributeBindings(ArrayList<Tag> attributes) {
 		this.attributeBindings = attributes;
 	}
 
 	public void setFileBindings(ArrayList<Tag_File> files) {
 		this.fileBindings = files;
 	}
 
 	public void setVersionBindings(ArrayList<Tag_File> files) {
 		this.versionBindings = files;
 	}
 
 	public void setVersionBinding(Tag_File file) {
 		this.versionBindings.add(file);
 	}
 	
 	public void addAccess(Access c){
 		accesses.add(c);
 	}
 	
 	public void setAccesses(ArrayList<Access> accesses){
 		this.accesses = accesses;
 	}
 
 	public void updateViewName() {
 		String anhang = "";
 
 		for (Tag t : attributeBindings) {
 			anhang += ", " + t.type + ": " + t.name;
 		}
 
 		viewName = super.name + anhang;
 	}
 
 	@Override
 	public String toString() {
 		return "Tag_File [fileType=" + fileType + ", size=" + size + ", path=" + path + ", creation_time="
 				+ creation_time + ", expiration_time=" + expiration_time + ", delete_time=" + delete_time
 				+ ", parent_ID=" + parent_ID + ", origin_ID=" + origin_ID + ", score=" + score + ", shape=" + shape
 				+ ", attributeBindings=" + attributeBindings + ", fileBindings=" + fileBindings + ", versionBindings="
 				+ versionBindings + ", accesses=" + accesses + ", viewName=" + viewName + ", id=" + id + ", name=" + name
 				+ ", type=" + type + ", x=" + x + ", y=" + y + ", z=" + z + "]";
 	}
 	
 	public void renderFileName(TagExplorerProcessing2 p5, PGraphics renderer){
 		renderPlane(p5, renderer, 100, 100);
 	}
 	
 	public void renderPlane(TagExplorerProcessing2 p5, PGraphics renderer, int w, int h){
 		
 		
 		
 //		renderer.shape(shape, x, y);
 		renderer.fill(0);
 //		renderer.textFont(p5.font, 14);
 		renderer.textAlign(PConstants.LEFT);
 		renderer.text(name, -(w/2-10), -(h/2-20));
 	}
 
 	
 }
