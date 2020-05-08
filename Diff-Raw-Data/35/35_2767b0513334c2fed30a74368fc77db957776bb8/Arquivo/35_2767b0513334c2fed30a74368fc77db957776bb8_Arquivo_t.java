 package br.com.caelum.vraptor.infra;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import org.apache.tomcat.util.http.fileupload.IOUtils;
 
 import com.sun.jmx.snmp.Timestamp;
 
 import br.com.caelum.vraptor.interceptor.multipart.UploadedFile;
 
 public class Arquivo {
 	
 	private long timeStamp;
 	private UploadedFile arquivo;
	 //private static final String caminhoAbsolutoDaPastaUploads = "C:\\Users\1110508\\Documents\\GitHub\\blender\\implementacao\\WebContent\\WEB-INF\\imagens\\uploads\\";
	private static final String caminhoAbsolutoDaPastaUploads = "/Users/mateusgomesfreitas/Documents/workspace/blender/implementacao/WebContent/uploads/";
 	
 	private String caminhoPastaDentroDeUploads;
 	
 	private boolean cropped = false;
 
 	private int width;
 	private int height;
 	
 	public Arquivo(){
 		
 	}
 
 	public Arquivo(UploadedFile arquivo, String caminhoPastaDentroDeUploads, long timeStamp){
 		this.arquivo = arquivo;
 		this.timeStamp = timeStamp;
 		this.caminhoPastaDentroDeUploads = caminhoPastaDentroDeUploads;
 	}
 
 	public void salvaArquivo() {
 
 		File fotoSalva = new File(Arquivo.caminhoAbsolutoDaPastaUploads + this.caminhoPastaDentroDeUploads, timeStamp + this.arquivo.getFileName());
 	
 		try {
 			IOUtils.copyLarge(this.arquivo.getFile(), new FileOutputStream(fotoSalva));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			System.out.println("Problema no IO");
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public UploadedFile getArquivo() {
 		return arquivo;
 	}
 
 
 	public boolean isCropped() {
 		return cropped;
 	}
 
 	public void setCropped(boolean cropped) {
 		this.cropped = cropped;
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public void setWidth(int width) {
 		this.width = width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public void setHeight(int height) {
 		this.height = height;
 	}
 
 	
 }
