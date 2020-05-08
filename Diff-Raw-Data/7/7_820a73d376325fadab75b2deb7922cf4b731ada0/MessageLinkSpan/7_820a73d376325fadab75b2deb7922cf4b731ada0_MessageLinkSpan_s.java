 package com.ioabsoftware.gameraven.views;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.text.style.ClickableSpan;
 import android.util.Log;
 import android.view.View;
 
 import com.ioabsoftware.gameraven.AllInOneV2;
 import com.ioabsoftware.gameraven.networking.HandlesNetworkResult.NetDesc;
 import com.ioabsoftware.gameraven.networking.Session;
 
 public class MessageLinkSpan extends ClickableSpan {
 	
 	String url;
 	AllInOneV2 aio;
 	public MessageLinkSpan(String urlIn, AllInOneV2 aioIn) {
 		url = urlIn;
 		aio = aioIn;
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		Log.d("MessageLinkSpan", url);
 		if (!url.contains("www.gamefaqs.com") && url.contains("gamefaqs.com")) {
 			url = url.replace("gamefaqs.com", "www.gamefaqs.com");
 			Log.d("MessageLinkSpan", "added www: " + url);
 		}
 		
 		if (url.startsWith(Session.ROOT)) {
 			Log.d("MessageLinkSpan", "URL is within the GFAQs domain");
 			
 			// check if PM
 			if(url.equals(Session.ROOT + "/pm"))
 				url += "/";
 			if (url.contains("/pm/")) {
 				if (url.contains("?id=")) {
 					aio.getSession().get(NetDesc.PM_DETAIL, url, null);
 				}
 				else {
 					aio.getSession().get(NetDesc.PM_INBOX, url, null);
 				}
 				return;
 			}
 			// check if AMP
 			else if (url.contains("myposts.php")) {
 				aio.getSession().get(NetDesc.AMP_LIST, url, null);
 				return;
 			}
 			// check if this is a board or topic url
 			else if (url.contains("/boards")) {
 				if (url.contains("/users/")) {
 					aio.getSession().get(NetDesc.USER_DETAIL, url, null);
 				}
 				else if (url.contains("boardlist.php")) {
 					aio.getSession().get(NetDesc.BOARD_LIST, url, null);
 				}
 				else {
 					String boardUrl = url.substring(url.indexOf("boards"));
 					if (boardUrl.contains("/")) {
 						 String slashUrl = boardUrl.substring(boardUrl.indexOf("/") + 1);
 						 if (slashUrl.contains("/")) {
 							 // should be a topic
 							 aio.getSession().get(NetDesc.TOPIC, url, null);
 						 }
 						 else {
 							 // should be a board
 							 aio.getSession().get(NetDesc.BOARD, url, null);
 						 }
 					 }
 					 else {
 						 // should be home
 						 aio.getSession().get(NetDesc.BOARD_JUMPER, url, null);
 					 }
 				}
 				return;
 			}
 		}
 		
 		// getting to this point means there was no match, open the page in browser
 		openInBrowser();
 	}
 	
 	private void openInBrowser() {
 		aio.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
 	}
 
 }
