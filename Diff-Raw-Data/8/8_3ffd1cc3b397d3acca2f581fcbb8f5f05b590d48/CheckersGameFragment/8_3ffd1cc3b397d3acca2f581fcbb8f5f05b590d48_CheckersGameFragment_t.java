 package com.charlesmadere.android.classygames;
 
 
 import android.content.res.Resources;
 import android.graphics.drawable.BitmapDrawable;
 import android.view.View;
 import android.widget.ImageButton;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.charlesmadere.android.classygames.models.games.Coordinate;
 import com.charlesmadere.android.classygames.models.games.Position;
 import com.charlesmadere.android.classygames.models.games.checkers.Board;
 import com.charlesmadere.android.classygames.models.games.checkers.Piece;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 public class CheckersGameFragment extends GenericGameFragment
 {
 
 
 	/**
 	 * Bitmap representing the player's normal piece.
 	 */
 	private BitmapDrawable playerNormal;
 
 
 	/**
 	 * Bitmap representing the player's king piece.
 	 */
 	private BitmapDrawable playerKing;
 
 
 	/**
 	 * Bitmap representing the opponent's normal piece.
 	 */
 	private BitmapDrawable opponentNormal;
 
 
 	/**
 	 * Bitmap representing the opponent's king piece.
 	 */
 	private BitmapDrawable opponentKing;
 
 
 
 
 	@Override
 	protected void createView()
 	{
 
 	}
 
 
 	@Override
 	protected void createOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 
 	}
 
 
 	@Override
 	protected boolean optionsItemSelected(final MenuItem item)
 	{
 		return super.onOptionsItemSelected(item);
 	}
 
 
 	@Override
 	protected void prepareOptionsMenu(final Menu menu)
 	{
 
 	}
 
 
 	@Override
 	protected void flush(final Position position)
 	{
 		final String tag = createTag(position.getCoordinate());
 		final Piece piece = (Piece) position.getPiece();
 		final ImageButton imageButton = (ImageButton) getView().findViewWithTag(tag);
 
 		switch (piece.getType())
 		{
 			case Piece.TYPE_NORMAL:
 				if (piece.isTeamPlayer())
 				{
 					imageButton.setImageDrawable(playerNormal);
 				}
 				else
 				{
 					imageButton.setImageDrawable(opponentNormal);
 				}
 				break;
 
 			case Piece.TYPE_KING:
 				if (piece.isTeamPlayer())
 				{
 					imageButton.setImageDrawable(playerKing);
 				}
 				else
 				{
 					imageButton.setImageDrawable(opponentKing);
 				}
 				break;
 		}
 	}
 
 
 	@Override
 	protected String getDefaultPlayersPieceColor()
 	{
 		return getString(R.string.green);
 	}
 
 
 	@Override
 	protected String getDefaultOpponentsPieceColor()
 	{
 		return getString(R.string.orange);
 	}
 
 
 	@Override
 	protected int getGameView()
 	{
 		return R.layout.checkers_game_fragment;
 	}
 
 
 	@Override
 	protected int getLoadingText()
 	{
 		return R.string.loading_checkers_game_against_x;
 	}
 
 
 	@Override
 	protected int getSettingsKeyForPlayersPieceColor()
 	{
 		return R.string.settings_key_players_checkers_piece_color;
 	}
 
 
 	@Override
 	protected int getSettingsKeyForOpponentsPieceColor()
 	{
 		return R.string.settings_key_opponents_checkers_piece_color;
 	}
 
 
 	@Override
 	protected void initNewBoard() throws JSONException
 	{
 		board = new Board();
 	}
 
 
 	@Override
 	protected void initViews()
 	{
 		final View view = getView();
 
 		setBoardOnClickListeners
 		(
 			view.findViewById(R.id.checkers_game_fragment_x0y0),
 			view.findViewById(R.id.checkers_game_fragment_x1y0),
 			view.findViewById(R.id.checkers_game_fragment_x2y0),
 			view.findViewById(R.id.checkers_game_fragment_x3y0),
 			view.findViewById(R.id.checkers_game_fragment_x4y0),
 			view.findViewById(R.id.checkers_game_fragment_x5y0),
 			view.findViewById(R.id.checkers_game_fragment_x6y0),
 			view.findViewById(R.id.checkers_game_fragment_x7y0),
 			view.findViewById(R.id.checkers_game_fragment_x0y1),
 			view.findViewById(R.id.checkers_game_fragment_x1y1),
 			view.findViewById(R.id.checkers_game_fragment_x2y1),
 			view.findViewById(R.id.checkers_game_fragment_x3y1),
 			view.findViewById(R.id.checkers_game_fragment_x4y1),
 			view.findViewById(R.id.checkers_game_fragment_x5y1),
 			view.findViewById(R.id.checkers_game_fragment_x6y1),
 			view.findViewById(R.id.checkers_game_fragment_x7y1),
 			view.findViewById(R.id.checkers_game_fragment_x0y2),
 			view.findViewById(R.id.checkers_game_fragment_x1y2),
 			view.findViewById(R.id.checkers_game_fragment_x2y2),
 			view.findViewById(R.id.checkers_game_fragment_x3y2),
 			view.findViewById(R.id.checkers_game_fragment_x4y2),
 			view.findViewById(R.id.checkers_game_fragment_x5y2),
 			view.findViewById(R.id.checkers_game_fragment_x6y2),
 			view.findViewById(R.id.checkers_game_fragment_x7y2),
 			view.findViewById(R.id.checkers_game_fragment_x0y3),
 			view.findViewById(R.id.checkers_game_fragment_x1y3),
 			view.findViewById(R.id.checkers_game_fragment_x2y3),
 			view.findViewById(R.id.checkers_game_fragment_x3y3),
 			view.findViewById(R.id.checkers_game_fragment_x4y3),
 			view.findViewById(R.id.checkers_game_fragment_x5y3),
 			view.findViewById(R.id.checkers_game_fragment_x6y3),
 			view.findViewById(R.id.checkers_game_fragment_x7y3),
 			view.findViewById(R.id.checkers_game_fragment_x0y4),
 			view.findViewById(R.id.checkers_game_fragment_x1y4),
 			view.findViewById(R.id.checkers_game_fragment_x2y4),
 			view.findViewById(R.id.checkers_game_fragment_x3y4),
 			view.findViewById(R.id.checkers_game_fragment_x4y4),
 			view.findViewById(R.id.checkers_game_fragment_x5y4),
 			view.findViewById(R.id.checkers_game_fragment_x6y4),
 			view.findViewById(R.id.checkers_game_fragment_x7y4),
 			view.findViewById(R.id.checkers_game_fragment_x0y5),
 			view.findViewById(R.id.checkers_game_fragment_x1y5),
 			view.findViewById(R.id.checkers_game_fragment_x2y5),
 			view.findViewById(R.id.checkers_game_fragment_x3y5),
 			view.findViewById(R.id.checkers_game_fragment_x4y5),
 			view.findViewById(R.id.checkers_game_fragment_x5y5),
 			view.findViewById(R.id.checkers_game_fragment_x6y5),
 			view.findViewById(R.id.checkers_game_fragment_x7y5),
 			view.findViewById(R.id.checkers_game_fragment_x0y6),
 			view.findViewById(R.id.checkers_game_fragment_x1y6),
 			view.findViewById(R.id.checkers_game_fragment_x2y6),
 			view.findViewById(R.id.checkers_game_fragment_x3y6),
 			view.findViewById(R.id.checkers_game_fragment_x4y6),
 			view.findViewById(R.id.checkers_game_fragment_x5y6),
 			view.findViewById(R.id.checkers_game_fragment_x6y6),
 			view.findViewById(R.id.checkers_game_fragment_x7y6),
 			view.findViewById(R.id.checkers_game_fragment_x0y7),
 			view.findViewById(R.id.checkers_game_fragment_x1y7),
 			view.findViewById(R.id.checkers_game_fragment_x2y7),
 			view.findViewById(R.id.checkers_game_fragment_x3y7),
 			view.findViewById(R.id.checkers_game_fragment_x4y7),
 			view.findViewById(R.id.checkers_game_fragment_x5y7),
 			view.findViewById(R.id.checkers_game_fragment_x6y7),
 			view.findViewById(R.id.checkers_game_fragment_x7y7)
 		);
 
 		// create an array of the board's rows
 		final int [] xPositions = new int[8];
 		xPositions[0] = R.id.checkers_game_fragment_x0;
 		xPositions[1] = R.id.checkers_game_fragment_x1;
 		xPositions[2] = R.id.checkers_game_fragment_x2;
 		xPositions[3] = R.id.checkers_game_fragment_x3;
 		xPositions[4] = R.id.checkers_game_fragment_x4;
 		xPositions[5] = R.id.checkers_game_fragment_x5;
 		xPositions[6] = R.id.checkers_game_fragment_x6;
 		xPositions[7] = R.id.checkers_game_fragment_x7;
 
 		// create an array of the board's columns
 		final int [] yPositions = new int[8];
 		yPositions[0] = R.id.checkers_game_fragment_y0;
 		yPositions[1] = R.id.checkers_game_fragment_y1;
 		yPositions[2] = R.id.checkers_game_fragment_y2;
 		yPositions[3] = R.id.checkers_game_fragment_y3;
 		yPositions[4] = R.id.checkers_game_fragment_y4;
 		yPositions[5] = R.id.checkers_game_fragment_y5;
 		yPositions[6] = R.id.checkers_game_fragment_y6;
 		yPositions[7] = R.id.checkers_game_fragment_y7;
 
 		setAllBoardPositionsToEqualHeightAndWidth(view, R.id.checkers_game_fragment_x0y7, xPositions, yPositions);
 	}
 
 
 	@Override
 	protected void loadBluePieceResources(final Resources res, final boolean isPlayersColor)
 	{
 		if (isPlayersColor)
 		{
 			playerNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_blue);
 			playerKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_blue);
 		}
 		else
 		{
 			opponentNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_blue);
 			opponentKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_blue);
 		}
 	}
 
 
 	@Override
 	protected void loadGreenPieceResources(final Resources res, final boolean isPlayersColor)
 	{
 		if (isPlayersColor)
 		{
 			playerNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_green);
 			playerKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_green);
 		}
 		else
 		{
 			opponentNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_green);
 			opponentKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_green);
 		}
 	}
 
 
 	@Override
 	protected void loadOrangePieceResources(final Resources res, final boolean isPlayersColor)
 	{
 		if (isPlayersColor)
 		{
 			playerNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_orange);
 			playerKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_orange);
 		}
 		else
 		{
 			opponentNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_orange);
			opponentKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_orange);
 		}
 	}
 
 
 	@Override
 	protected void loadPinkPieceResources(final Resources res, final boolean isPlayersColor)
 	{
 		if (isPlayersColor)
 		{
 			playerNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_pink);
			playerKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_pink);
 		}
 		else
 		{
 			opponentNormal = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_normal_pink);
			opponentKing = (BitmapDrawable) res.getDrawable(R.drawable.piece_checkers_king_pink);
 		}
 	}
 
 
 	@Override
 	protected void onBoardClick(final ImageButton positionCurrent)
 	{
 		if (board.isBoardLocked())
 		{
 			clearSelectedPositions();
 		}
 		else
 		{
 			final Coordinate coordinateCurrent = new Coordinate((String) positionCurrent.getTag());
 			final Position current = board.getPosition(coordinateCurrent);
 
 			if (current.hasPiece() && current.getPiece().isTeamPlayer())
 			{
 				setPositionBackground(positionCurrent, true);
 			}
 			else
 			{
 				clearSelectedPositions();
 			}
 		}
 	}
 
 
 	@Override
 	protected void onBoardClick(final ImageButton positionPrevious, final ImageButton positionCurrent)
 	{
 		if (!board.isBoardLocked())
 		{
 			final Coordinate coordinatePrevious = new Coordinate((String) positionPrevious.getTag());
 			final Position previous = board.getPosition(coordinatePrevious);
 			setPositionBackground(positionPrevious, false);
 
 			final Coordinate coordinateCurrent = new Coordinate((String) positionCurrent.getTag());
 			final Position current = board.getPosition(coordinateCurrent);
 
 			if (!current.hasPiece())
 			{
 				setPositionBackground(positionCurrent, true);
 
 				if (board.move(previous, current))
 				{
 					flush();
 					readyToSendMove(true);
 
 					if (board.isBoardLocked())
 					{
 						clearSelectedPositions();
 					}
 				}
 				else
 				{
 					clearSelectedPositions();
 				}
 			}
 			else
 			{
 				clearSelectedPositions();
 			}
 		}
 	}
 
 
 	@Override
 	protected void resumeOldBoard(final JSONObject boardJSON) throws JSONException
 	{
 		board = new Board(boardJSON);
 	}
 
 
 }
