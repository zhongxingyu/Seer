 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.v2.fragments;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import no.hials.muldvarp.R;
 
 public class WebzViewFragment extends MuldvarpFragment{
     View fragmentView;
    int id;
 
     public WebzViewFragment(String title, int iconResourceID, int id) {
         super.fragmentTitle = title;
         super.iconResourceID = iconResourceID;
        this.id = id;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String url = getActivity().getString(R.string.articlePath) + id;
         if(fragmentView == null) {
             fragmentView = inflater.inflate(R.layout.webview, container, false);
             WebView webView = (WebView)fragmentView.findViewById(R.id.webview);
             webView.loadUrl(url);
             System.out.println(url);
         }
         return fragmentView;
     }
 
 }
