 package org.eclipse.vjet.dsf.jst.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jst.FileBinding;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.JstCommentLocation;
 import org.eclipse.vjet.dsf.jst.JstSource.IBinding;
 import org.eclipse.vjet.dsf.jst.SimpleBinding;
 
 public class JstCommentHelper {
 
 	public static String getCommentAsString(IJstType type, JstCommentLocation location, boolean includeVjetDocs){
 		List<JstCommentLocation> locs = new ArrayList<JstCommentLocation>();
 		locs.add(location);
 		return getCommentsAsString(type, locs, includeVjetDocs).get(0);
 	}
 	
 	public static List<String> getCommentsAsString(IJstType jstType,
 			List<JstCommentLocation> commentLocations) {
 		return getCommentsAsString(jstType, commentLocations, false);
 	}
 
 	public static List<String> getCommentsAsString(IJstType jstType,
 			List<JstCommentLocation> commentLocations, boolean includeVjetDocs) { // locate
 																					// file
 		List<String> comments = new ArrayList<String>();
 		if (jstType ==null || jstType.getSource() == null) {
 			return Collections.EMPTY_LIST;
 		}
 		IBinding binding = jstType.getSource().getBinding();
 		
 //		if(binding==null){
 //			System.out.println("no binding for type" + jstType.getName());
 //		}
 		
 		if (binding instanceof SimpleBinding) {
 			handleSimpleBinding(commentLocations, comments,
 					(SimpleBinding) binding, includeVjetDocs);
 		}
 
 		if (binding instanceof FileBinding) {
 			
 			handleFileBinding(commentLocations, comments, binding,
 					includeVjetDocs);
 
 		}
 		return comments;
 	}
 
 	private static void handleSimpleBinding(
 			List<JstCommentLocation> commentLocations, List<String> comments,
 			SimpleBinding binding, boolean includeVjetDocs) {
 		String source = binding.toText();
 		for (JstCommentLocation jstCommentLocation : commentLocations) {
 			if (!jstCommentLocation.isVjetDoc()
 					|| (includeVjetDocs && jstCommentLocation.isVjetDoc())) {
 				
 				if(jstCommentLocation.getEndOffset()<source.length() &&
 						jstCommentLocation.getStartOffset()<source.length()){
 					comments.add(source.substring(
 							jstCommentLocation.getStartOffset(),
 							jstCommentLocation.getEndOffset()));
 				}
 			}
 
 		}
 
 	}
 
 	private static void handleFileBinding(
 			List<JstCommentLocation> commentLocations, List<String> comments,
 			IBinding binding, boolean includeVjetDocs) {
 		FileBinding fileBinding = (FileBinding) binding;
 		if(fileBinding.getFile()==null){
 			return;
 		}
 		RandomAccessFile random = null;
 		try {
 			random = new RandomAccessFile(fileBinding.getFile(), "r");
 
 			for (JstCommentLocation jstCommentLocation : commentLocations) {
 				ByteArrayOutputStream sb = new ByteArrayOutputStream();
 				int startOffset = jstCommentLocation.getStartOffset();
 				int max = jstCommentLocation.getEndOffset() - startOffset;

 				random.seek(startOffset);
 				
 				byte[] buffer = new byte[max];
 				int comment = random.read(buffer);
				if(comment!=-1){
					sb.write(buffer, 0, comment);
				}
 				comments.add(sb.toString("utf-8"));
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			try {
 				random.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
