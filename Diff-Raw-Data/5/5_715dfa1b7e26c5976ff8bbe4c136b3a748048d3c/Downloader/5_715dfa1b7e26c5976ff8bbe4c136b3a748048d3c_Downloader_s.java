 package com.github.geakstr.parser;
 
 import java.io.File;
 import java.io.IOException;
import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 
 public class Downloader<T extends IDoc> {
 
 	public Document getDocument(String url) throws IOException {
 		return Jsoup.connect(url).userAgent("Mozilla").get();
 	}
 
 	private static String sha1(String input) throws NoSuchAlgorithmException {
 		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
 		byte[] result = mDigest.digest(input.getBytes());
 		StringBuffer sb = new StringBuffer();
 		for (int i = 0; i < result.length; i++) {
 			sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16)
 					.substring(1));
 		}
 
 		return sb.toString();
 	}
 
 	public void putDocumentToFile(T doc, String dir) throws IOException,
 			NoSuchAlgorithmException {
 
 		File dirFile = new File(dir);
 		if (!dirFile.exists())
 			dirFile.mkdir();
 
 		String link = doc.getLink();
 
 		String fileName = dir + sha1(link) + ".html";
 
 		File f = new File(fileName);
 		if (f.exists()) {
 			f.delete();
 		}
 
 		try {
 			new URL(link);
 		} catch (MalformedURLException e) {
 			System.out.println("Невалидный адрес " + link);
 			System.out.println();
 			return;
 		}
 
 		Document getDoc = null;
 		try {
 			getDoc = getDocument(link);
 		} catch (Exception e) {
 			System.out.println("Не могу выкачать документ по адресу " + link);
 			System.out.println();
 			return;
 		}
 
		PrintWriter out = new PrintWriter(fileName);
 		out.println(getDoc);
 		out.close();
 	}
 
 	public void putDocumentsToFile(List<T> docs, String dir)
 			throws IOException, NoSuchAlgorithmException {
 
 		for (T doc : docs)
 			putDocumentToFile(doc, dir);
 	}
 }
