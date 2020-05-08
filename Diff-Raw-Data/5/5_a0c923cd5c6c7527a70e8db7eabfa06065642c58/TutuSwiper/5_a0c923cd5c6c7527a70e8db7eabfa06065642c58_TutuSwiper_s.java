 package kr.co.ddononi;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 import java.util.List;
 
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Source;
 
 public class TutuSwiper {
 	//private String dir;
 	public static void main(final String[] args){
 		/*
 		if(args.length < 1) {
 			System.err.println("Usage: java TutuSwiper [tutu-id Numbers]... ");
 			System.exit(1);
 		}
 		*/
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				System.in)); // ŰԷ½Ʈ
 		int count = 0;
 		String line;
 		while(true){
 			System.out.print(" ̵ Էϼ : ");
 			line = "";
 			try {
 				line = reader.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 
 			if(line.length() < 5){
 				continue;
 			}else if(line.equalsIgnoreCase("bye")){			//	
 				break;
 			}
 
 			//  ð üũ
 			long startTime = System.currentTimeMillis();
 
 			Swiper sp = new Swiper(line);
 			sp.getStartContents();
 
 			long endTime = System.currentTimeMillis();
 			endTime = (endTime - startTime);
 			System.out.println("ڷ  ð : " + endTime + "Millis" );
 			count++;
 		}
 
 		System.out.println("  ڷ : " + count + "");
 		System.out.println("bye~ bye~");
 	}
 }
 
 
 class Swiper{
 	private String dir = null;
 	public final static String TUTU_URL =
 			"http://www.tutudisk.com//main/popup/bbs_info_0.php?idx=";
 	public final static String SAVE_FILE_NAME = "info.html";
 	private String id;
 
 	public Swiper(){
 		//12107151
 		System.out.println("̵ ϴ.");
 	}
 
 	public Swiper(final String id){
 		//  
 		this.id = id;
 		File file = makeDir();
 		if( file.mkdirs() ){
 			System.out.println(file.toString() + "  ϴ.");
 		}
 
 		//  
 		dir = file.getAbsolutePath();
 
 		// ҽ ó
 		//String source = downloadSource();
 	}
 
 	/**
 	 *     丮 ش.
 	 * 丮 ش.
 	 * @return  ũ
 	 */
 	private File makeDir() {
 		Calendar cal = Calendar.getInstance();
 		int year = cal.get(Calendar.YEAR);
 		int month = cal.get(Calendar.MONTH) + 1;
 		int day = cal.get(Calendar.DAY_OF_MONTH);
 		StringBuilder sb = new StringBuilder("C:\\onehard");
 		sb.append(File.separator);
 		sb.append(year);
 		sb.append(File.separator);
		sb.append(month);
 		sb.append(File.separator);
		sb.append(day + "");
 		sb.append(File.separator);
 		sb.append(getTitle());	//   
 		sb.append(File.separator);
 		File file = new File(sb.toString());
 
 		return file;
 	}
 
 	protected boolean doSaveFile(final String source) {
 		File sourcefile = new File(this.dir, SAVE_FILE_NAME);
 		BufferedWriter bw = null;
 		boolean flag = true;
 		try {
 			bw = new BufferedWriter(new FileWriter(sourcefile));
 			bw.write(source);
 			bw.flush();
 			System.out.println("ҽ ߽ϴ.");
 		} catch (IOException e) {
 			flag = false;
 			e.printStackTrace();
 		}finally{
 			try {
 				bw.close();
 			} catch (IOException e) {}
 		}
 
 		return flag;
 	}
 
 	/**
 	 * ־ url ش urlҽ ´.
 	 * @return url source
 	 */
 	protected String downloadSource(){
 		URL url = null;
 		InputStream is = null;
 		StringBuilder sb = new StringBuilder();
 		String line;
 		BufferedReader br = null;
 		try {
 			url = new URL(TUTU_URL+ this.id);
 			 URLConnection conn = url.openConnection();
 			 is = conn.getInputStream();
 			 br = new BufferedReader(new InputStreamReader(is));
 				while((line = br.readLine()) != null) {
 					sb.append(line);
 					sb.append("\r\n");
 				}
 
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				br.close();
 				is.close();
 			} catch (IOException e) {}
 		}
 
 		return sb.toString();
 	}
 
 	private void saveToImageFile(final String source){
 		File filename = new File(source);
 		//File file = new File("C:\\onehard", source);
 		FileOutputStream imageFile = null;
 		BufferedInputStream bis = null;
 		try {
 			imageFile = new FileOutputStream(new File(this.dir, filename.getName()));
 			URL url = new URL(source);
 			bis = new BufferedInputStream(url.openStream());
 			int size;
 			while( (size = bis.read()) > -1){
 				imageFile.write(size);
 			}
 
 			imageFile.flush();
 			System.out.println(filename.getName() + " ̹  Ϸ");
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			try {
 				imageFile.close();
 				bis.close();
 			} catch (IOException e) {}
 		}
 
 	}
 
 	private String getTitle(){
 		String title = null;
 		try {
 			Source source = new Source(new URL(TUTU_URL+ this.id));
 			source.fullSequentialParse();
 			List<Element> rootList = source.getAllElements(HTMLElementName.TD);
 			String width;
 			for(Element elem : rootList){
 				width = elem.getAttributeValue("width");
 				if(width == null) {
 					continue;
 				}else if(width.contains("605")){
 					// ŸƲ  ¿ 
 					title = elem.getAttributeValue("title").toString().trim();
 					System.out.println(" : " + title);
 				}
 
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return title.replace(".", "");
 	}
 
 	public void getStartContents(){
 		try {
 			Source source = new Source(new URL(TUTU_URL+ this.id));
 			source.fullSequentialParse();
 			List<Element> tdList = source.getAllElements(HTMLElementName.TD);
 			for(Element elem : tdList){
 				String style = elem.getAttributeValue("style");
 				if(style == null) {
 					continue;
 				}else if(style.contains("word-break:break-all;font-size:9pt")
 						&& elem.getAttributeValue("valign").contains("top")
 						&& elem.getAttributeValue("align").contains("center")){	// ε κ 
 					String contents = elem.getContent().toString().trim();
 					// ̹  븸 
 					contents = contents.replaceAll("<IMG ([^>]+)>", "");
 					doSaveFile(contents);	//  
 					List<Element> imageList = elem.getAllElements(HTMLElementName.IMG);	// ̹ 
 					for(Element subElem : imageList){
 						String src = subElem.getAttributeValue("src");
 						if(src == null) {
 							continue;
 						}
 						// ε ̹ 
 						System.out.println(src + " ãҽϴ.");
 						saveToImageFile(src);
 					}
 				}
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
