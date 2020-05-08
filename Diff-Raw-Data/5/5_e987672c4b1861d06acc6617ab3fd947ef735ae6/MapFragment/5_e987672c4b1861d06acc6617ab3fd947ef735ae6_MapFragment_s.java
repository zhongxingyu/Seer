 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.cvut.moteto.main;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.ctc.android.widget.ImageMap;
 
 import cz.cvut.moteto.model.Test;
 import cz.cvut.moteto.model.WorkSpace;
 import android.app.Fragment;
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 
 /**
  *
  * @author Jan Zdrha
  */
 public class MapFragment extends Fragment {
 	ImageMap mImageMap;
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 		
 		mImageMap = createMap();
 		
 		// add a click handler to react when areas are tapped
         mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler() {
             @Override
             public void onImageMapClicked(int id) {
                 // when the area is tapped, show the name in a
                 // text bubble
                 mImageMap.showBubble(id);
             }
  
             @Override
             public void onBubbleClicked(int id) {
                 // react to info bubble for area being tapped
             }
         });
 		
 		return mImageMap;
 	}
 	
 	public ImageMap createMap() {
 		Test test = WorkSpace.getInstance().getCurrentTest();
 		Document doc = null;
 		try {
 			doc = test.getDocument();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Element map = (Element) doc.getElementsByTagName("map").item(0);
 
 		String filename = map.getElementsByTagName("filename").item(0).getTextContent();
		filename = WorkSpace.getInstance().getWorkspaceFolder()+"/"+filename;
 		Bitmap bm = BitmapFactory.decodeFile(filename);
 
         ImageMap imageView = new ImageMap((Context)getActivity());
         
         NodeList nodes = map.getElementsByTagName("location");
         for (int i = 0; i < nodes.getLength(); i+=1) {
             Element el = (Element) nodes.item(i);
             imageView.addDotArea(i+1, el.getAttribute("name"),
                     Float.parseFloat(el.getElementsByTagName("x").item(0).getTextContent()),
                     Float.parseFloat(el.getElementsByTagName("y").item(0).getTextContent()),
                     50);
         }
         
         imageView.setImageBitmap(bm);
         imageView.setVisibility(View.VISIBLE);
         return imageView;
 	}
 }
