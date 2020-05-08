 package bu.edu.cs673.edukid.learn;
 
 import java.util.Locale;
 
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import bu.edu.cs673.edukid.EDUkid;
 import bu.edu.cs673.edukid.R;
 import bu.edu.cs673.edukid.db.model.Word;
 import bu.edu.cs673.edukid.db.model.category.CategoryType;
 import bu.edu.cs673.edukid.settings.utils.RecordUtility;
 
 public class WordFragment extends Fragment implements OnClickListener,
 		OnInitListener {
 
 	private CategoryType categoryType;
 
 	private int itemIndex;
 
 	private int wordIndex;
 
 	private TextToSpeech textToSpeech;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		if (textToSpeech == null) {
 			textToSpeech = new TextToSpeech(getActivity(), this);
 		}
 
 		return inflater.inflate(R.layout.learn_content_word_fragment,
 				container, false);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 		Bundle arguments = getArguments();
 
 		categoryType = (CategoryType) arguments
 				.getSerializable(EDUkid.CATEGORY_TYPE);
 		itemIndex = arguments.getInt(EDUkid.ITEM_INDEX);
 		wordIndex = arguments.getInt(EDUkid.WORD_INDEX);
 
 		Word word = categoryType.getLearnItemWord(itemIndex, wordIndex);
 
 		// Set content image
 		ImageButton learnContentImage = (ImageButton) view
 				.findViewById(R.id.learnContentImage);
 
 		if (word.isDefaultWord()) {
 			learnContentImage.setImageDrawable(getResources().getDrawable(
 					categoryType.getLearnItemDrawableId(itemIndex, wordIndex)));
 		} else {
 			learnContentImage.setOnClickListener(this);
 			learnContentImage.setMaxHeight(300);
 			learnContentImage.setMaxWidth(300);
 			learnContentImage.setAdjustViewBounds(false);
 			learnContentImage.setImageDrawable(categoryType
 					.getLearnItemDrawable(itemIndex, wordIndex));
 		}
 
 		// Set content word
 		TextView learnContentWord = (TextView) view
 				.findViewById(R.id.learnContentWord);
 		learnContentWord.setText(word.getWord());
 
 		learnContentWord.setOnClickListener(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onClick(View view) {
 		String text = "";
 
 		switch (view.getId()) {
 		case R.id.learnContentImage:
 		case R.id.learnContentWord:
 			Word word = categoryType.getLearnItemWord(itemIndex, wordIndex);
 			String wordSound = word.getWordSound();
 			System.out.println("block");
 
 			if (wordSound != null && !wordSound.isEmpty()) {
 				System.out.println("if block");
 				RecordUtility.playbackRecording(wordSound);
 				System.out.println("after");
 				return;
 			}
 
 			text = categoryType.getLearnItemWord(itemIndex, wordIndex)
 					.getWord();
 			break;
 		default:
 			return;
 		}
 
 		try {
 			textToSpeech.stop();
 			textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onInit(int status) {
 		textToSpeech.setLanguage(Locale.getDefault());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void onDestroyView() {
 		super.onDestroyView();
 	}
 }
