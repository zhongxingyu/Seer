 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.quickshare;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Config;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.widget.Button;
 import com.janrain.android.engage.types.JRActivityObject;
 import com.janrain.android.engage.utils.AndroidUtils;
 
 public class StoryDetailFragment extends Fragment implements View.OnClickListener {
     private static final String TAG = StoryDetailFragment.class.getSimpleName();
 
     private QuickShare mQuickShare;
     private WebView mWebView;
 
    public StoryDetailFragment() {}
 
     public static StoryDetailFragment newInstance(int index) {
         StoryDetailFragment f = new StoryDetailFragment();
 
         Bundle args = new Bundle();
         args.putInt("index", index);
         f.setArguments(args);
         f.mQuickShare = QuickShare.getInstance();
 
         return f;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         if (container == null) {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 
         if (getShownIndex() >= mQuickShare.getFeed().size()) {
             if (isAdded()) getFragmentManager().beginTransaction().remove(this).commit();
             return null;
         }
 
         Log.d(TAG, "[onCreateView]");
 
         View v = inflater.inflate(R.layout.story_detail_webview, container, false);
 
         Button mShareStory = (Button) v.findViewById(R.id.share_button);
         mShareStory.setOnClickListener(this);
 
         mWebView = (WebView) v.findViewById(R.id.story_webview);
 
         Log.d(TAG, "[onCreateView] url: loading the current story");
 
         loadCurrentStory();
         return v;
     }
 
     public void loadCurrentStory() {
         Story story = getShownStory();
 
         String styleCommon = getResources().getString(R.string.html_style_sheet_common);
         String style = getString(R.string.html_style_sheet);
 
         int targetWidth = (int) getResources().getDimension(R.dimen.fragmentImageWidth);
         String htmlString =
                 "<html>" +
                     "<head>" +
                         "<style type=\"text/css\">" +
                             styleCommon +
                             style +
                         "</style>" +
                     "</head>" +
                     "<body>" +
                         "<div class=\"main\">" +
                         "<div class=\"title\">" + story.getTitle() + "</div>" +
                         "<div class=\"date\">" + story.getFormattedDate() + "</div>" +
                         "<div class=\"posted_by\">Posted by: " + story.getPostedBy() + "</div>" +
                         "<div class=\"body\">" + story.getDescriptionWithScaledImages(targetWidth) + "</div>" +
                     "</body>" +
                 "</html>";
 
         Log.d(TAG, "[loadCurrentStory] html: "
                 + htmlString.substring(0, Math.min(50, htmlString.length())));
 
         mWebView.loadDataWithBaseURL("http://www.janrain.com/blogs/", htmlString, "text/html", "UTF-8", "");
     }
 
     public int getShownIndex() {
         return getArguments().getInt("index", 0);
     }
 
     public Story getShownStory() {
         return mQuickShare.getFeed().get(getShownIndex());
     }
     
     public void onClick(View view) {
         if (getActivity() instanceof StoryDetailActivity) {
             ((StoryDetailActivity) getActivity()).startWaiting();
         }
 
         JRActivityObject jra = mQuickShare.getFeed().get(getShownIndex()).toJRActivityObject();
         if (AndroidUtils.isXlarge() && AndroidUtils.isLandscape()) {
             mQuickShare.getJREngage().showSocialPublishingFragment(jra, getActivity(), getId(), true,
                     null, null, null, null);
         } else {
             mQuickShare.getJREngage().showSocialPublishingDialog(jra);
         }
     }
 }
