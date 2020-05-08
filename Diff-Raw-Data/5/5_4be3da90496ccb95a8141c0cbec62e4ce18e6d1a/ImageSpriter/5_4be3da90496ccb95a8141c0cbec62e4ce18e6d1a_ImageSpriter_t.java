 package org.qrone.r7.parser;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.qrone.img.ImageBuffer;
 import org.qrone.img.ImageBufferService;
 import org.qrone.img.ImageRect;
 import org.qrone.r7.QrONEUtils;
 import org.qrone.r7.resolver.URIResolver;
 
 public class ImageSpriter {
 	private String isprite = "/sprite-i.png";
 	private String vsprite = "/sprite-v.png";
 	private String hsprite = "/sprite-h.png";
 	private String tsprite = "/sprite-t.png";
 
 	private URI ispriteURI;
 	private URI vspriteURI;
 	private URI hspriteURI;
 	private URI tspriteURI;
 	
 	private List<ImagePart> isprites = new LinkedList<ImagePart>();
 	private Map<ImagePart, String> iresults = new Hashtable<ImagePart, String>();
 	private int iWidth;
 	private int iHeight;
 	private int ilastsize = -1;
 	
 	private List<ImagePart> vsprites = new LinkedList<ImagePart>();
 	private Map<ImagePart, String> vresults = new Hashtable<ImagePart, String>();
 	private int vWidth;
 	private int vHeight;
 	private int vlastsize = -1;
 	
 	private List<ImagePart> hsprites = new LinkedList<ImagePart>();
 	private Map<ImagePart, String> hresults = new Hashtable<ImagePart, String>();
 	private int hWidth;
 	private int hHeight;
 	private int hlastsize = -1;
 	
 	private boolean useTransparentDot = false;
 	private boolean outTransparentDot = false;
 	
 	private Map<URI, ImageBuffer> map = new Hashtable<URI, ImageBuffer>();
 
 	private URI basedir;
 	private URIResolver resolver;
 	private ImageBufferService service;
 	
 	public ImageSpriter(URIResolver resolver, ImageBufferService service) {
 		this.resolver = resolver;
 		this.service = service;
 		
 		try {
 			setBaseURI(new URI("."));
 		} catch (URISyntaxException e) {}
 	}
 	/*
 	public static ImageSpriter instance(){
 		if(ins == null) ins = new ImageSpriter();
 		return ins;
 	}
 	*/
 	public ImageBuffer getImage(URI file) throws IOException{
 		if(map.containsKey(file)){
 			return map.get(file);
 		}else{
 			InputStream in = resolver.getInputStream(file);
 			ImageBuffer i = service.createImage(in);
 			in.close();
 			map.put(file, i);
 			return i;
 		}
 	}
 	
 	public void setBaseURI(URI imgdir){
 		basedir = imgdir;
 		ispriteURI = basedir.resolve(isprite);
 		tspriteURI = basedir.resolve(tsprite);
 		vspriteURI = basedir.resolve(vsprite);
 		hspriteURI = basedir.resolve(hsprite);
 	}
 	
 	public void update(URI uri) throws IOException{
 		if(uri.equals(ispriteURI)){
 			createi();
 		}else if(uri.equals(hspriteURI)){
 			createh();
 		}else if(uri.equals(vspriteURI)){
 			createv();
 		}else if(uri.equals(tspriteURI)){
 			createt();
 		}
 	}
 	
 	private URI getPath(URI from, String name){
 		return QrONEUtils.relativize(from,basedir.resolve(name));
 	}
 	
 	public void create() throws IOException {
 		createi();
 		createh();
 		createv();
 		createt();
 	}
 
 	public void createi() throws IOException {
 		int currentY = 0;
 		int currentX = 0;
 		if(isprites.size() > 0 && ilastsize != isprites.size()){
 			ilastsize = isprites.size();
 			
 			ImageBuffer iimage = service.createImage(iWidth, iHeight);
 			currentY = 0;
 			for (Iterator<ImagePart> i = isprites.iterator(); i
 					.hasNext();) {
 				ImagePart part = i.next();
 				iimage.drawImage(getImage(part.file), 
 						new ImageRect(currentX, currentY, part.w, part.h), 
 						new ImageRect(part.x, part.y, part.w, part.h));
 				currentY += part.h;
 			}
 			iimage.writeTo(resolver.getOutputStream(ispriteURI));
 		}
 	}
 	
 	public void createv() throws IOException {
 		int currentY = 0;
 		int currentX = 0;
 		if(vsprites.size() > 0 && vlastsize != vsprites.size()){
 			vlastsize = vsprites.size();
 			
 			ImageBuffer vimage = service.createImage(vWidth, vHeight);
 			currentX = 0;
 			for (Iterator<ImagePart> i = vsprites.iterator(); i
 					.hasNext();) {
 				ImagePart part = i.next();
 	
 				currentY = 0;
 				while (currentY < vWidth) {
 					if(currentY + part.h > vWidth){
 						vimage.drawImage(getImage(part.file), 
 								new ImageRect(currentX, currentY, 
 								part.w, 
 								part.h - (currentY + part.h - vWidth)), 
 								new ImageRect(part.x, part.y, 
 								part.w, 
 								part.h - (currentY + part.h - vWidth)));
 					}else{
 						vimage.drawImage(getImage(part.file), 
 								new ImageRect(currentX, currentY, 
 								part.w, 
 								part.h), 
 								new ImageRect(part.x, part.y, 
 								part.w, 
 								part.h));
 					}
					currentY += part.h-1;
 				}
 	
 				currentX += part.w;
 			}
 			vimage.writeTo(resolver.getOutputStream(vspriteURI));
 		}
 	}
 	
 	public void createh() throws IOException {
 		int currentY = 0;
 		int currentX = 0;
 		
 		if(hsprites.size() > 0 && hlastsize != hsprites.size()){
 			hlastsize = hsprites.size();
 			
 			ImageBuffer himage = service.createImage(hWidth, hHeight);
 			currentY = 0;
 			for (Iterator<ImagePart> i = hsprites.iterator(); i
 					.hasNext();) {
 				ImagePart part = i.next();
 	
 				currentX = 0;
 				while (currentX < hHeight) {
 					if(currentX + part.w > hHeight){
 						himage.drawImage(getImage(part.file), 
 								new ImageRect(currentX, currentY, 
 								part.w - (currentX + part.w - hHeight), 
 								part.h), 
 								new ImageRect(part.x, part.y, 
 								part.w - (currentX + part.w - hHeight), 
 								part.h));
 					}else{
 	
 						himage.drawImage(getImage(part.file), 
 								new ImageRect(currentX, currentY, 
 								part.w, 
 								part.h), 
 								new ImageRect(part.x, part.y, 
 								part.w, 
 								part.h));
 					}
					currentX += part.w-1;
 				}
 	
 				currentY += part.h;
 			}
 			himage.writeTo(resolver.getOutputStream(hspriteURI));
 		}
 	}
 	
 	public void createt() throws IOException {
 		if(useTransparentDot && !outTransparentDot){
 			outTransparentDot = true;
 			InputStream in = QrONEUtils.getResourceAsStream("1dot.png");
 			OutputStream out = resolver.getOutputStream(tspriteURI);
 			int buf;
 		    while ((buf = in.read()) >= 0)
 		        out.write(buf);
 		    in.close();
 		    out.close();
 		}
 	}
 	
 	public String addISprite(URI file) throws IOException{
 		ImageBuffer b = getImage(file);
 		isprites.add(new ImagePart(file, 0, 0, b.getWidth(), b.getHeight()));
 		
 		if(iWidth < b.getWidth()){
 			iWidth = b.getWidth();
 		}
 		iHeight += b.getHeight();
 		
 		return "width:" + b.getWidth() + "px;" 
 			 + "height:" + b.getHeight() + "px;" 
 			 + "background: no-repeat 0px -" + (iHeight-b.getHeight()) + "px url(" + getPath(file, isprite) + ");";
 	}
 
 	public String addISprite(ImagePart file) throws IOException{
 		if(iresults.containsKey(file)){
 			return iresults.get(file);
 		}
 		
 		isprites.add(file);
 		if(iWidth < file.w){
 			iWidth = file.w;
 		}
 		iHeight += file.h;
 		
 		String res = "width:" + file.w + "px;" 
 			 + "height:" + file.h + "px;" 
 			 + "background: no-repeat 0px -" 
 			 	+ (iHeight-file.h) + "px url(" + getPath(file.file, isprite) + ");";
 		iresults.put(file, res);
 		return res;
 	}
 	public String addVSprite(ImagePart file) throws IOException{
 		if(vresults.containsKey(file)){
 			return vresults.get(file);
 		}
 		
 		vsprites.add(file);
 		if(vHeight < file.h){
 			vHeight = file.h;
 		}
 		vWidth += file.w;
 		
 		String res = "width:" + file.w + "px;" 
 			 + "background: repeat-y -" + (vWidth-file.w) + "px 0px url(" + getPath(file.file, vsprite) + ");";
 		vresults.put(file, res);
 		return res;
 	}
 
 	public String addHSprite(ImagePart file) throws IOException{
 		if(hresults.containsKey(file)){
 			return hresults.get(file);
 		}
 		
 		hsprites.add(file);
 		if(hWidth < file.w){
 			hWidth = file.w;
 		}
 		hHeight += file.h;
 		
 		String res = "height:" + file.h + "px;" 
 			 + "background: repeat-x 0px -" + (hHeight-file.h) + "px url(" + getPath(file.file, hsprite) + ");";
 		hresults.put(file, res);
 		return res;
 	}
 	
 	public URI addTransparentDot(){
 		useTransparentDot = true;
 		return basedir.resolve(tsprite);
 	}
 	
 }
