 package lo52.messaging.fragments;
 
 import java.io.File;
 
 import lo52.messaging.R;
 import lo52.messaging.activities.ConversationPagerActivity;
 import lo52.messaging.model.Conversation;
 import lo52.messaging.model.Message;
 import lo52.messaging.services.NetworkService;
 
 import org.xml.sax.XMLReader;
 
 import android.content.Context;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.Html;
 import android.text.Spannable;
 import android.text.method.LinkMovementMethod;
 import android.text.style.ClickableSpan;
 import android.text.style.ImageSpan;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Contenu d'un "tab" (fragment) à l'intérieur de ConversationPagerActivity.
  */
 public class ConversationFragment extends Fragment {
 
 	private static final String TAG = "ConversationFragment";
 	final ConversationFragment thisFrag = this;
 
 	// La conversation associée à ce fragment
 	private Conversation conversation = null;
 
 	int conversation_id = -1;
 	View fv;
 
 	int progressBarVisibility = View.GONE;
 
 	String conversationName_str = "";
 	String conversationText_str = "";
 
 	ConversationPagerActivity parentActivity;
 
 	@Override
 	public void onResume() {
 		super.onResume();
 
 		// Rafraichit le texte
 		//EditText conversText_edit = (EditText) fv.findViewById(R.id.conversation_content);	// FIXME
 		TextView conversText_edit = (TextView) fv.findViewById(R.id.conversation_content);	// FIXME
 		conversText_edit.setMovementMethod(LinkMovementMethod.getInstance()); 	// FIXME
 
 		// Met à jour la conversation en reprenant celle du service. Permet d'avoir un objet Conversation à jour,
 		// qui peut contenir des messages qui ont été reçus pendant que le fragment était onPause.
 		updateConversationFromService();
 
 		if (conversText_edit != null && conversation != null && parentActivity != null) {
 			conversText_edit.setText(Html.fromHtml(conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext()), new ImageGetter(), new MediaGetter(parentActivity.getBaseContext())));
 		}
 
 		// Affiche ou cache la progressBar
 		ProgressBar pbar = (ProgressBar) fv.findViewById(R.id.conversation_progressBar);
 		if (pbar != null) {
 			pbar.setVisibility(progressBarVisibility);
 		}
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// Inflate le layout depuis le xml
 		fv = inflater.inflate(R.layout.conversation, container, false);
 
 		// Initialise les différents éléments du layout
 		TextView conversName_tv 		= (TextView) fv.findViewById(R.id.conversation_name);
 		//EditText conversText_edit		= (EditText) fv.findViewById(R.id.conversation_content);
 
 		TextView conversText_edit		= (TextView) fv.findViewById(R.id.conversation_content);	// FIXME
 		//EditText conversUserText_edit	= (EditText) v.findViewById(R.id.conversation_usermessage);	// FIXME
 		conversText_edit.setMovementMethod(LinkMovementMethod.getInstance()); 	// FIXME
 
 
 		Button conversMedia_btn 	= (Button) fv.findViewById(R.id.conversation_media_button);
 		Button conversSend_btn 		= (Button) fv.findViewById(R.id.conversation_send_button);
 
 		// On rend le Text Edit de la convers non éditable
 		conversText_edit.setEnabled(true);
 		conversText_edit.setFocusable(true); // FIXME
 		conversText_edit.setClickable(true);
 
 		parentActivity = (ConversationPagerActivity) getActivity();
 
 		// Initialise leur valeur
 		conversName_tv.setText(conversationName_str);
 		conversationText_str = conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext());
 		Log.d(TAG, "== Update texte");
 		conversText_edit.setText(Html.fromHtml(conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext()), new ImageGetter(), new MediaGetter(parentActivity.getBaseContext())));
 		Log.d(TAG, "== Updated texte");
 
 		conversMedia_btn.setOnClickListener(mediaButtonClickListener);
 		conversSend_btn.setOnClickListener(sendButtonClickListener);
 
 		// Affiche ou cache la progressBar
 		ProgressBar pbar = (ProgressBar) fv.findViewById(R.id.conversation_progressBar);
 		pbar.setVisibility(progressBarVisibility);
 
 		return fv;
 	}
 
 	// Création et assignation des onClickListerners
 	OnClickListener mediaButtonClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			parentActivity.startFilePickerActivity(getConversation_id());
 		}
 	};
 
 	OnClickListener sendButtonClickListener = new OnClickListener() {
 		public void onClick(View v) {
 			String messageText = getConversUserText();
 
 			if (messageText.equals("")) return;
 
 			makeSureConversationIsNotNull();
 
 			// Ajout du message texte à la conversation locale
 			conversation.addMessage(new Message(NetworkService.getUser_me().getId(), messageText));
 
 			// Mise à jour de l'UI
 			tryTextRefresh();
 
 			// Reset du champ d'entrée de texte			
 			EditText conversUserText_edit	= (EditText) fv.findViewById(R.id.conversation_usermessage);
 			conversUserText_edit.setText("");
 
 			parentActivity.onFragmentSendButtonClick(messageText, getConversation_id());
 		}
 	};
 
 	/**
 	 * Met à jour son objet Conversation en prenant celui du service, qui est plus à jour
 	 */
 	public void updateConversationFromService() {
 		if (conversation != null)	// car peut ne pas avoir encore été initialisé par le pager
 			conversation = NetworkService.getListConversations().get(conversation.getConversation_id());
 		// Si la conversation est nulle on a une chance de pouvoir la récupérer si l'ID de conversation n'est pas null
 		else 
 			makeSureConversationIsNotNull();
 	}
 
 	/**
 	 * S'assure que l'objet conversation n'est pas null
 	 */
 	protected void makeSureConversationIsNotNull() {
 		if (conversation == null && conversation_id != -1) {
 			conversation = NetworkService.getListConversations().get(conversation_id);
 			Log.d(TAG, "Updated conversation object");
 		}
 	}
 
 
 	/**
 	 * 
 	 * Getters / Setters
 	 * 
 	 */
 
 	public void setConversation(Conversation c) {
 		conversation = c;
 	}
 
 	/**
 	 * Retourne l'ID associé à cette conversation
 	 * @return
 	 */
 	public int getConversation_id() {
 		return conversation_id;
 	}
 
 	/**
 	 * Attribue un ID à la conversation. Doit (devrait) être appelé une fois que le fragment de la conversation a été créé.
 	 * @param conversation_id
 	 */
 	public void setConversation_id(int conversation_id) {
 		this.conversation_id = conversation_id;
 	}
 
 	/**
 	 * Met à jour le nom de la conversation, affiché en haut du fragment de conversation 
 	 * @param conversName
 	 */
 	public void setConversName(String conversName) {
 		// FIXME éventuellement, histoire de faire plus classe. Mais là ca crashe
 		//conversationName_str = getString(R.string.conversations_name_prefix);
 		conversationName_str = conversName;
 	}
 
 	/**
 	 * Retourne le texte contenu dans le champ de conversation
 	 * @return
 	 */
 	public String getConversText() {
 		return conversationText_str;
 	}
 
 	/**
 	 * Met du texte dans le champ de conversation de la vue. Attention, si du texte est déjà présent il sera écrasé.
 	 * @deprecated utiliser la méthode pour générer le texte depuis la conversation
 	 * @param conversText
 	 * @see appendConversationText()
 	 */
 	public void setConversText(String conversText) {
 		this.conversationText_str = conversText;
 	}
 
 
 	/**
 	 * Retourne le texte entré par l'utilisateur
 	 * @return
 	 */
 	public String getConversUserText() {
 		EditText conversUserText_edit = (EditText) fv.findViewById(R.id.conversation_usermessage);
 		return conversUserText_edit.getText().toString();
 	}
 
 
 	/**
 	 * Set le texte dans le champ dans lequel l'uilisateur écrit. Ne devrait pas être utilisé directement.
 	 * @param conversUserText
 	 */
 	/*public void setConversUserText(String conversUserText) {
 		this.conversUserText_edit.setText(conversUserText);
 	}*/
 
 
 	/**
 	 * Ajoute du texte à la suite du texte présent dans le champ de conversation sans écraser le texte présent
 	 * @deprecated Ajouter le message reçu à la conversation du fragment et regénérer le corps du texte de la conversation
 	 * @param text
 	 */
 	public void appendConversationText(String text) {
 		//this.setConversText(this.getConversText() + " " + text);
 		this.conversationText_str += " " + text;
 	}
 
 
 	/**
 	 * @deprecated
 	 * @return
 	 */
 	public View getFragmentView() {
 		return fv;
 	}
 
 	/**
 	 * @deprecated
 	 * @return
 	 */
 	public ConversationFragment getThisFrag() {
 		return thisFrag;
 	}
 
 	public Conversation getConversation() {
 		return conversation;
 	}
 
 	/**
 	 * Essaye de regénérer le texte de la conversation si la vue est active
 	 */
 	public void tryTextRefresh() {
 		// Régénère le texte de la conversation
 		conversationText_str = conversation.generateUserFriendlyConversationText(parentActivity.getBaseContext());
 		Log.d(TAG, "Texte mis à jour " + conversationText_str);
 
 		// Essaye de rafraichir le textEdit
 		if (fv != null) {
 			//EditText conversText_edit = (EditText) fv.findViewById(R.id.conversation_content);	// FIXME
 			TextView conversText_edit = (TextView) fv.findViewById(R.id.conversation_content);	// FIXME
 
 			if (conversText_edit != null) {
 				conversText_edit.setMovementMethod(LinkMovementMethod.getInstance()); 	// FIXME
 				conversText_edit.setText(Html.fromHtml(conversationText_str));
 			}
 			else Log.d(TAG, "Champ de texte null");
 		} else {
 			Log.d(TAG, "N'a pas pu refresh la vue");
 		}
 	}
 
 
 	/**
 	 * Set la visiblité du spinner de chargement
 	 * @param visibility
 	 */
 	public void setProgressBarVisibility(int visibility) {
 		progressBarVisibility = visibility;
 
 		// Essaye de rafraichir la progressBar en direct
 		if (fv != null) {
 			ProgressBar pbar = (ProgressBar) fv.findViewById(R.id.conversation_progressBar);
 			if (pbar != null) {
 				pbar.setVisibility(progressBarVisibility);
 			}
 		}
 	}
 
 
 	/**
 	 *	Permet de convertir les tags <img> en image dans l'EditText de la conversation
 	 */
 	private class ImageGetter implements Html.ImageGetter {
 
 		public Drawable getDrawable(String source) {
 
 			Drawable d = Drawable.createFromPath(source);
 			d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
 
 			return d;   
 		}
 	};
 
 
 	/**
 	 *	Permet d'interpréter les tags html non pris en charge de base. 
 	 *	Ici, on s'en sert pour interpréter <code>&lt;sound&gt;LinkToAudioFile&lt;/sound&gt;</code>
 	 */
 	private class MediaGetter implements Html.TagHandler {
 
 		Context ctx;
 
 		public MediaGetter(Context ctx) {
 			this.ctx = ctx;
 		}
 
		@Override
 		public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
 
 			if (tag.equalsIgnoreCase("sound")) {
 				processSoundLink(opening, output);
 			}
 		}
 
 		private void processSoundLink(boolean opening, Editable output) {
 			int len = output.length();
 			if(opening) {
 				output.setSpan(new ImageSpan(ctx, R.drawable.speaker_small), len, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 				output.setSpan(new SoundClickable(ctx, ""), len, len, Spannable.SPAN_MARK_MARK);
 			} else {
 				Object obj = getLast(output, SoundClickable.class);
 				int where = output.getSpanStart(obj);
 
 				output.removeSpan(obj);
 
 				if (where != len) {
 					CharSequence uri = output.subSequence(where, len);
 
 					output.setSpan(new ImageSpan(ctx, R.drawable.speaker_small), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 					output.setSpan(new SoundClickable(ctx, uri.toString()), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 				}
 			}
 		}
 
 		private Object getLast(Editable text, Class<?> kind) {
 			Object[] objs = text.getSpans(0, text.length(), kind);
 
 			if (objs.length == 0) {
 				return null;
 			} else {
 				for(int i = objs.length;i>0;i--) {
 					if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
 						return objs[i-1];
 					}
 				}
 				return null;
 			}
 		}
 	}
 
 
 	/**
 	 * Permet d'interpréter les tags <sound>linkToAudioFile</sound>
 	 */
 	private class SoundClickable extends ClickableSpan {
 
 		Context ctx;
 		String soundUri;
 
 		public SoundClickable(Context ctx, String soundUri) {
 			this.ctx = ctx;
 			this.soundUri = soundUri;
 			Log.d(TAG, "Crée span " + soundUri);
 		}
 
 		@Override
 		public void onClick(View widget) {
 			// Lecture du son
 			File file = new File(soundUri);
 
 			if (file.exists()) {
 				Uri myUri = Uri.parse("file://"+soundUri);
 				MediaPlayer mediaPlayer = new MediaPlayer();
 				mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 
 				try {
 					mediaPlayer.setDataSource(ctx, myUri);
 					mediaPlayer.prepare();
 					mediaPlayer.start();
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage());
 				}
 
 			} else {
 				Toast.makeText(ctx, ctx.getString(R.string.generic_error), Toast.LENGTH_LONG).show();
 			}
 		}
 	}
 
 }
