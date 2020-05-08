 package com.zuehlke.jhp.bucamp.android.jass;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
import ch.mbaumeler.jass.core.Match;
 import ch.mbaumeler.jass.core.card.Card;
 import ch.mbaumeler.jass.core.card.CardSuit;
 import ch.mbaumeler.jass.core.card.CardValue;
 import ch.mbaumeler.jass.core.game.PlayerToken;
 import ch.mbaumeler.jass.extended.ui.JassModelObserver;
 import ch.mbaumeler.jass.extended.ui.ObserverableMatch.Event;
 
 public class HandFragment extends Fragment implements JassModelObserver {
 
 	private MainActivity mainActivity;
 	private RelativeLayout layout;
 	private HashMap<String, Integer> cardImageMap;
 	
 	private int height = 200;
 	private int width = 480;
 	
 	{
 		cardImageMap = new HashMap<String, Integer>();
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.TEN.name(), R.drawable.clubs_10);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.SIX.name(), R.drawable.clubs_6);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.SEVEN.name(), R.drawable.clubs_7);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.EIGHT.name(), R.drawable.clubs_8);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.NINE.name(), R.drawable.clubs_9);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.ACE.name(), R.drawable.clubs_ace);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.JACK.name(), R.drawable.clubs_jake);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.KING.name(), R.drawable.clubs_king);
 		cardImageMap.put(CardSuit.CLUBS.name() + CardValue.QUEEN.name(), R.drawable.clubs_queen);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.TEN.name(), R.drawable.diamonds_10);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.SIX.name(), R.drawable.diamonds_6);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.SEVEN.name(), R.drawable.diamonds_7);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.EIGHT.name(), R.drawable.diamonds_8);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.NINE.name(), R.drawable.diamonds_9);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.ACE.name(), R.drawable.diamonds_ace);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.JACK.name(), R.drawable.diamonds_jake);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.KING.name(), R.drawable.diamonds_king);
 		cardImageMap.put(CardSuit.DIAMONDS.name() + CardValue.QUEEN.name(), R.drawable.diamonds_queen);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.TEN.name(), R.drawable.hearts_10);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.SIX.name(), R.drawable.hearts_6);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.SEVEN.name(), R.drawable.hearts_7);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.EIGHT.name(), R.drawable.hearts_8);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.NINE.name(), R.drawable.hearts_9);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.ACE.name(), R.drawable.hearts_ace);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.JACK.name(), R.drawable.hearts_jake);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.KING.name(), R.drawable.hearts_king);
 		cardImageMap.put(CardSuit.HEARTS.name() + CardValue.QUEEN.name(), R.drawable.hearts_queen);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.TEN.name(), R.drawable.spades_10);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.SIX.name(), R.drawable.spades_6);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.SEVEN.name(), R.drawable.spades_7);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.EIGHT.name(), R.drawable.spades_8);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.NINE.name(), R.drawable.spades_9);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.ACE.name(), R.drawable.spades_ace);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.JACK.name(), R.drawable.spades_jake);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.KING.name(), R.drawable.spades_king);
 		cardImageMap.put(CardSuit.SPADES.name() + CardValue.QUEEN.name(), R.drawable.spades_queen);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.hand_fragment, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		mainActivity.getGame().addObserver(this);
 	}
 
 	private void initCard(Card card, int numberTotal, int index) {
 		ImageView imageView = new ImageView(mainActivity.getApplicationContext()); // findViewById(id);
		Match currentMatch = mainActivity.getGame().getCurrentMatch();
		if (!mainActivity.getGame().getCurrentMatch().isCardPlayable(card) && currentMatch.getAnsage() != null) {
 			imageView.setAlpha(0.1f);
 		} else {
 			// imageView.setBackgroundResource(R.drawable.card_shape);
 			imageView.setOnTouchListener(new CardTouchListener(card));
 		}
 		imageView.setLongClickable(true);
 		imageView.setImageResource(cardImageMap.get(card.getSuit().name() + card.getValue().name()));
 		if (this.getView().getWidth() > 0) {
 			height = this.getView().getHeight()-20;
 			width = this.getView().getWidth();
 		}
 		int card_width = (height * 2) / 3;
 		int xpos = ((width-card_width) / numberTotal) * index;
 		LayoutParams params = new RelativeLayout.LayoutParams(card_width, this.getView().getHeight());
 		params.topMargin = 0;
 		params.leftMargin = xpos;
 		layout.addView(imageView, params);
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		mainActivity = (MainActivity) activity;
 	}
 	
 	public void updated(Event event, PlayerToken playerToken, Object object) {
 		layout = (RelativeLayout) this.getView().findViewById(R.id.handFragmentRow1);
 		if (layout != null) {
 			int index = 0;
 			List<Card> cardsInHand = mainActivity.getGame().getCurrentMatch().getCards(
 					mainActivity.getGameController().getHumanPlayerToken());
 			layout.removeAllViews();
 			
 			cardsInHand = new ArrayList<Card>(cardsInHand);
 			Collections.sort(cardsInHand, new Comparator<Card>() {
 
 				public int compare(Card card1, Card card2) {
 				return card1.getSuit() == card2.getSuit() ? card1.getValue().compareTo(card2.getValue()) : card1.getSuit().compareTo(card2.getSuit());
 				}
 			});
 			
 			for (Card card : cardsInHand) {
 				initCard(card, cardsInHand.size(), index);
 				index++;
 			}
 			layout.requestLayout();
 
 		}
 	}
 
 }
