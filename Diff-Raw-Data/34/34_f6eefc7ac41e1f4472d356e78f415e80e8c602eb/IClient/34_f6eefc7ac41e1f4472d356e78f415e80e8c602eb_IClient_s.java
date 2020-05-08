 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.iface;
 
 import edu.kit.iti.pse.iface.IServer;
 
 /**
  * Minimale Schnittstelle um als Gegenpart für das Interface IServer zu dienen.
  * 
  * @see {@link IServer}
  * @author Fabian Neundorf
  */
 public interface IClient {
 
 	/**
 	 * Gibt den Namen des Spielers zurück.
 	 * 
 	 * @return Der Name des Spielers.
 	 */
 	String getName();
 
 	/**
 	 * Gibt die Sprache des Clients zurück.
 	 * 
 	 * @return Die Clientsprache nach ISO 639-3.
 	 */
 	String getLanguage();
 
 	/**
 	 * Informiert den Spieler, dass das Spiel beginnt.
 	 * 
 	 * @param ids
 	 *            Die Anzahl der Spieler.
 	 */
 	void informStartGame(int[] ids);
 
 	/**
 	 * Informiert den Spieler, dass der Spieler mit der id "player" an der Reihe
 	 * ist und nun Würfeln darf.
 	 * 
 	 * @param player
 	 *            Der Spieler der an die Reihe gekommen ist.
 	 */
 	void informTurn(int player);
 
 	/**
 	 * Informiert den Spieler, dass der Spieler mit der ID das folgende
 	 * Würfelergebnis hatte.
 	 * 
 	 * @param diceValues
 	 *            Augenwerte der einzelnen Würfel.
 	 */
 	void informDiceValues(int[] diceValues);
 
 	/**
 	 * Informiert den Spieler dass sich das Geld eines Spielers geändert hat.
 	 * 
 	 * @param player
 	 *            Der Spieler mit dem geänderten Bargeld.
 	 * @param cashChange
 	 *            Die Bargeldänderung.
 	 */
 	void informCashChange(int player, int cashChange);
 
 	/**
 	 * Informiert den Spieler, dass ein Spieler ein Objekt gekauft hat.
 	 * 
 	 * @param player
 	 *            der neue Besitzer der Straße.
 	 * @param position
 	 *            Die Position des Objekts, welches gekauft wurde.
 	 * @since SVN revision 13 (davor <code>informStreetBuy(int)</code>).
 	 */
 	void informBuy(int player, int position);
 
 	/**
 	 * Informiert den Spieler, dass ein Spieler <i>ein</i> Gebäude gebaut hat.
 	 * 
 	 * @param street
 	 *            Die Straße auf der das Gebäude gebaut wurde.
 	 * @param playerId
 	 *            Der Spieler der das Gebäude gebaut hat.
 	 */
 	void informConstruct(int street);
 
 	/**
 	 * Informiert den Spieler, dass ein Spieler <i>ein</i> Gebäude aufgelöst
 	 * hat.
 	 * 
 	 * @param street
 	 *            Die Straße auf der das Gebäude gestanden hat.
 	 * @param playerId
 	 *            Der Spieler der das Gebäude aufgelöst hat.
 	 */
 	void informDestruct(int street);
 
 	/**
 	 * Informiert den Spieler, dass ein Spieler eine Hypothek auf die Straße
 	 * aufgenommen/zurückgezahlt hat.
 	 * 
 	 * @param street
 	 *            Die Straße auf der die Hypothek aufgenommen wurde bzw. dessen
 	 *            Hypothek zurückgezahlt wurde.
 	 * @param playerId
 	 *            Der handelnde Spieler.
 	 */
 	void informMortgageToogle(int street);
 
 	/**
 	 * Informiert den Spieler, dass der aktive Spieler eine Karte gezogen hat.
 	 * 
 	 * @param text
 	 *            Inhalt der Karte.
 	 * @param communityCard
 	 *            Wenn der Wert wahr ist, handelt es sich um eine
 	 *            Gemeinschaftskarte, sonst um eine Ereigniskarte.
 	 */
 	void informCardPull(String text, boolean communityCard);
 
 	/**
 	 * Informiert den Spieler darüber, dass der aktive Spieler bankrott ist.
 	 */
 	void informBankruptcy();
 
 	/**
 	 * Informiert den Spieler, dass eine Chat Nachricht eingegangen ist.
 	 * 
 	 * @param text
 	 *            Der Text der Chat Nachricht.
 	 * @param sender
	 *            Der Empfänger der Nachricht.
 	 * @param privateMessage
 	 *            Ist wahr, wenn es sich um eine private Nachricht handelt.
 	 */
 	void informMessage(String text, int sender, boolean privateMessage);
 
 	/**
 	 * Informiert den Spieler, dass ein Handelsangebot vorliegt, bzw. dass eine
 	 * Antwort vorliegt.
 	 */
 	void informTrade(int actingPlayer, int partnerPlayer);
 
 	/**
 	 * Informiert den Spieler, dass eine Auktion begonnen hat.
 	 * 
 	 * @param auctionState
 	 *            Gibt den aktuellen Status der Auktion an:
 	 *            <ul>
 	 *            <li>&lt;0 → Auktion hat begonnen.</li>
 	 *            <li>0 → Jemand hat geboten und der Zähler wird zurückgesetzt</li>
 	 *            <li>1 → „Zum ersten“</li>
 	 *            <li>2 → „Zum zweiten“</li>
 	 *            <li>3 → „Zum dritten“ (verkauft :P)</li>
 	 *            </ul>
 	 * @since SVN revision 13.
 	 */
 	void informAuction(int auctionState);
 
 	/**
 	 * Informiert den Spieler, dass ein Spieler sich bewegt hat.
 	 * 
 	 * @param playerId
 	 *            Die Id des Spielers der bewegt wurde.
 	 * @param position
 	 *            Die neue Position des Spielers. Siehe
 	 *            {@link IServer#getPlayerPiecePosition(int)}.
 	 * @since SVN revision 13.
 	 */
 	void informMove(int playerId, int position);
 }
