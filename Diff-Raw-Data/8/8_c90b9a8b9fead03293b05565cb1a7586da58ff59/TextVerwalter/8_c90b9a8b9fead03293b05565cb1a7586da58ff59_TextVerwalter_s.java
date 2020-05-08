 package de.uni_hamburg.informatik.sep.zuul.server.util;
 
 /**
  * Zentraler Speicher der Ausgabetexte, sowie der Befehls- und Richtungstexte.
  * 
  * @author 0klein, 1jost
  * 
  */
 public final class TextVerwalter
 {
 	private TextVerwalter()
 	{
 	}
 
 	public static final String LYRISCHEREINLEITUNGSTEXT = "Willkommen zu Zuul, dem Abenteurspiel mit Dr. Little.\n\nNach einem fatalen Unfall in seinem Chemielabor ist der Wissenschaftler Dr. Little dazu verdammt, in geringer Körpergröße eine Lösung seines Dilemmas zu finden. Er weiß, dass sein Kollege Prof. Dr. Evenbigger bei seinen Forschungen über das Verhalten von Sonnenblumen unter Aussetzung von Beta-Strahlung zufälligerweise auf ein Mittel gestoßen ist, mithilfe dessen Dr. Little seine normale Größe zurückerlangen könnte."
 			+ "\nProf. Dr. Evenbiggers Labor befindet sich jedoch auf der anderen Seite des Universitätscampus und in Dr. Littles aktueller körperlichen Verfassung stellt die Reise eine große Herausforderung für ihn dar. Er muss sich nun mit seiner mäßigen Orientierung das Labor des Professors finden."
 			+ "\nIhnen obliegt die Verantwortung, Dr. Little sicher durch das Labyrinth von Räumen zu seinem Ziel zu bringen."
 			+ "\nDabei besteht aber noch ein Problem: Dr. Little leidet stark unter dem Einfluss seines fehlgeschlagenen Experiments und die körperliche Anstrengung des Durchreisens der Räume setzt so zu, dass er nach 8 Räumen ohne Nahrungsaufnahme den qualvollen Tod erwarten muss."
 			+ "\nDoch zum Glück haben unachtsame Studenten Kuchenkrümel in verschiedenen Teilen des Campus liegengelassen. Wenn es Dr. Little gelingt, einige davon zu erwischen, so verbessert sich sein Gesundheitszustand, sodass er weitere Räume durchqueren kann."
 			+ "\n\nSo starten Sie nun furchtlos in ein spannendes Abenteuer und retten Sie den Doktor vor seinem Verderben.";
 
 	public static final String EINLEITUNGSTEXT = "Willkommen zu Zuul, dem Abenteurspiel mit Dr. Little."
 			+ "\n\nNach einem fatalen Unfall in seinem Chemielabor ist der Wissenschaftler Dr. Little geschrumpft. Er weiß, dass sein Kollege Prof. Dr. Evenbigger bei seinen Forschungen über das Verhalten von Sonnenblumen unter Aussetzung von Beta-Strahlung zufälligerweise auf ein Mittel gestoßen ist, mithilfe dessen Dr. Little seine normale Größe zurückerlangen könnte."
 			+ "\nProf. Dr. Evenbiggers Labor befindet sich jedoch auf der anderen Seite des Universitätscampus und in Dr. Littles aktueller körperlichen Verfassung stellt die Reise eine große Herausforderung für ihn dar. Er muss sich nun mit seiner mäßigen Orientierung das Labor des Professors finden."
 			+ "\nIhnen obliegt die Verantwortung, Dr. Little sicher durch das Labyrinth von Räumen zu seinem Ziel zu bringen."
 			+ "\nDabei besteht aber noch ein Problem: Dr. Little leidet stark unter dem Einfluss seines fehlgeschlagenen Experiments und die körperliche Anstrengung des Durchreisens der Räume setzt so zu, dass er nach 8 Räumen ohne Nahrungsaufnahme den qualvollen Tod erwarten muss."
 			+ "\nDoch zum Glück haben unachtsame Studenten Kuchenkrümel in verschiedenen Teilen des Campus liegengelassen. Wenn es Dr. Little gelingt, einige davon zu erwischen, so verbessert sich sein Gesundheitszustand, sodass er weitere Räume durchqueren kann."
 			+ "\n\nSo starten Sie nun furchtlos in ein spannendes Abenteuer und retten Sie den Doktor vor seinem Verderben.";
 
 	public static final String KUCHENIMRAUMTEXT = "In diesem Raum nimmt Dr. Little den dezent-süßen Geruch frisch verkrümelten Kuchens wahr.";
 	public static final String KUCHENGENOMMENTEXT = "Dr. Little findet einen Kuchenkrümel! Er tut ihn in seine Tasche.";
 
 	public static final String SIEGTEXT = "Gute Arbeit. Sie haben Dr. Little erfolgreich an sein Ziel gebracht. Sein Kollege Prof. Dr. Evenbigger verabreicht ihm nun das Gegenmittel und verhilft ihm wieder zu seiner vollen Größe.";
 
 	public static final String kuchengegessentext(int energie)
 	{
 		return "Dr. Little vernascht genüsslich einen Kuchenkrümel. Er fühlt sich belebt und kann nun wieder "
 				+ energie + " weitere Räume bechreiten.";
 	}
 
 	public static final String kuchenVomBodenGegessenText(int energie)
 	{
 		return "Dr. Little vernascht genüsslich einen Kuchenkrümel vom Boden des Raumes. Er fühlt sich belebt und kann nun wieder "
 				+ energie + " weitere Räume bechreiten.";
 	}
 
 	public static final String giftkuchengegessentext(int energie)
 	{
 		return "Dr. Little isst einen Kuchenkrümel und bereut es sofort. Sein Magen krampft und er spürt, dass er nur noch "
 				+ energie + " Räume betreten können wird.";
 	}
 
 	public static String giftkuchenVomBodenGegessenText(int energie)
 	{
 		return "Dr. Little isst einen Kuchenkrümel und bereut es sofort. Sein Magen krampft und er spürt, dass er nur noch "
 				+ energie + " Räume betreten können wird.";
 	}
 
 	public static final String KEINIDENTIFIZIERTERKUCHEN = "Dr. Little hat keinen identifizierten Krümel dieser Art.";
 
 	public static final String NIEDERLAGETEXT = "Dr. Little ist vor Erschöpfung und Hunger zusammengebrochen. Starte erneut.";
 	public static final String BEENDENTEXT = "Dr. Little dankt Ihnen für Ihre Hilfe bei seinem aufregenden Abenteuer. Bis zum nächsten Mal bei Zuul.";
 
 	public static final String HILFETEXT = "Sie haben die Hilfe aufgerufen. Ihr Ziel ist es, in möglichst wenigen Schritten das Labor von Prof. Dr. Evenbigger zu finden. Sie können nur eine bestimmte Anzahl an Räumen durchqueren. Wenn Sie den Raum wechseln, verringert sich diese Anzahl um einen Raum. Mit Krümeln kann diese Anzahl aber wieder erhöht werden, allerdings sind unter ihnen auch vergiftete Krümel. Sollten sie unterwegs eine Maus treffen, so können Sie ihr, als Gegenleistung für einen Krümel, einen Hinweis auf den Richtigen Weg entlocken. Aber auch hier können sie sich nicht sicher sein, dass der Krümel wirklich gesund für die Maus ist... \nUm über die Texteingabe zu interagieren, stehen Ihnen folgende Befehle zur Verfügung: \ngehe | nehme | untersuche | essen\nschaue ost | süd | nord | west | o | s | n | w \nhilfe | laden | beenden";
 	public static final String HILFE_GO = "Geben sie eine richtung ein, um sich dorthin zu bewegen, wenn dort eine Tür ist. \nDie Richtungen sind: \n\"nord\", \"ost\", \"süd\", \"west\" \nSie können aber auch nur den Anfangsbuchstaben der Richtung einegeben, in die Sie gehen möchten.";
 	public static final String HILFE_EAT = "Geben sie \"essen tasche\" oder \"essen boden\" um von der gegebenen Lokalität einen zufälligen Krümel zu Essen. \nTippen sie \"essen tasche guter krümel\" oder \"essen tasche schlechter krümel\" um einen entsprechenden Krümel aus ihrer Tasche zu essen.";
 	public static final String HILFE_LOOK = "Geben sie \"schauen\" und eine Himmelsrichtung ein, um in den Raum zu schauen, wenn dort einer ist. \nDie Richtungen sind: \n\"nord\", \"ost\", \"süd\", \"west\"";
 	public static final String HILFE_TAKE = "Geben sie \"nehmen\" ein, um einen Krümel aus dem Raum aufzuheben.";
 	public static final String HILFE_GIVE = "Geben sie \"gib\" ein um einen Krümel an das Labor zu geben. Dort wird er untersucht.";
 	public static final String HILFE_FEED = "Geben sie \"füttere\" ein um einen Krümel an die Katze oder die Maus zu geben. \nTippen sie \"füttere krümel\" ein, um einen zufälligen Krümel zu füttern.\nAlternativ können sie \"füttere guter krümel\" oder \"füttere schlechter krümel\" tippen, wenn sie einen entsprechend identifizierten Krümel haben.";
 	public static final String HILFE_INVENTAR = "Geben sie \"Inventar\" ein, um zu sehen, welche Gegenstände sie bei sich tragen.";
 	public static final String HILFE_ABLEGEN = "Geben sie \"ablegen\" ein, um einen zufälligen Krümel abzulegen.\nTippen sie \"ablegen guter krümel\" oder \"ablegen schlechter krümel\" um einen solchen abzulegen, wenn sie über einen verfügen.";
 	public static final String HILFE_AUSGAENGE = "Geben sie \"ausgänge\" ein, um die möglichen Ausgänge aus diesem Raum anzuzeigen.";
 	public static final String HILFE_QUIT = "Geben sie \"beenden\" ein, um das Spiel zu beenden.";
 	public static final String HILFE_LOAD = "Geben sie \"laden\" ein, um eine Karte zu laden. Tippen sie \"laden\" und einen Dateinamen ein, um eine Karte aus dem Standard-Pfad zu laden.";
 	public static final String HILFE_HELP = "Geben sie \"hilfe\" ein, um die Hilfe zu zeigen.";
 
 	public static final String RAUMWECHSELTEXT = "Lebensenergie: ";
 	public static final String IMMERNOCHKUCHENTEXT = "Dr. Little erahnt jedoch noch weitere Krümel in direkter Umgebung.";
 	public static final String NICHTSZUMNEHMENTEXT = "Dr. Little streckt erwartungsvoll die Hand aus, doch er greift nur nach Luft."
 			+ "\nIn diesem Raum wird er nichts mehr zum Einsammeln finden.";
 	public static final String NICHTSZUMESSENTEXT = "Dr. Little sucht in seiner Tasche vergeblich nach einem Kuchenkrümel.";
 	public static final String NICHTSZUMESSENTEXTBODEN = "Dr.Little sucht vergeblich nach einem Kuchenkrümel in diesem Raum, leider kann er aber keinen finden.";
 	public static final String DORTLIEGTNICHTS = "Dort liegt nichts...";
 	public static final String FALSCHEEINGABE = "Ich weiß nicht, was Sie meinen...";
 	public static final String KUCHENTODTEXT = "Dr. Little ahnte nicht, dass dieser Kuchenkrümel sein letzter sein würde.\nEine fiese Chemikalie im Krümel zerstört die letzen Reste seines angegriffenen Organsystems.";
 
 	public static final String KEINERICHTUNG = "Wohin möchten Sie gehen?";
 	public static final String KEINESCHAURICHTUNG = "Wohin möchten sie schauen?";
 	public static final String KEINETUER = "Dort ist keine Tür. Wählen Sie eine andere Richtung.";
 	public static final String KEINORT = "Woraus möchten sie essen?";
 	public static final String AUSGAENGE = "Ausgänge";
 
 	public static final String BEFEHL_GEHEN = "gehe";
 	public static final String BEFEHL_SCHAUEN = "schaue";
 	public static final String BEFEHL_NEHMEN = "nehmen";
 
 	public static final String BEFEHL_ESSEN = "essen";
 	public static final String BEFEHL_ESSEN_TASCHE = "essen tasche";
 	public static final String BEFEHL_ESSEN_TASCHE_GUT = "essen tasche guter krümel";
 	public static final String BEFEHL_ESSEN_TASCHE_SCHLECHT = "essen tasche schlechter krümel";
 	public static final String BEFEHL_ESSEN_TASCHE_UNBEKANNT = "essen tasche krümel";
 
 	public static final String BEFEHL_HILFE = "hilfe";
 	public static final String BEFEHL_BEENDEN = "beenden";
 	public static final String BEFEHL_UNTERSUCHE = "untersuche";
 
 	public static final String BUTTON_EINGEBEN = "enter";
 
 	public static final String RICHTUNG_NORDEN = "nord";
 	public static final String RICHTUNG_SUEDEN = "süd";
 	public static final String RICHTUNG_WESTEN = "west";
 	public static final String RICHTUNG_OSTEN = "ost";
 
 	public static final String ORT_BODEN = "boden";
 	public static final String ORT_TASCHE = "tasche";
 
 	public static final String MAUS_GEFUNDEN = "Eine kleine, pelzige Maus schaut hinter der Ecke hervor. Sie schaut hungrig auf Dr. Littles Tasche.";
 	public static final String MAUS_FRAGE = "Möchten Sie der Maus einen Krümel geben, damit sie Dr. Little den Weg weist?";
 	public static final String MAUS_RICHTUNGSANGABE = "Maus: \"Ich vermute, dass %s die richtige Richtung ist.\"";
 	public static final String MAUS_KEIN_KRUEMEL = "Dr. Little hat keinen Krümel, den er abgeben könnte...";
 	public static final String BEFEHL_GIB_KEIN_OBJEKT = "Hier ist niemand, dem Dr. Little einen Krümel geben könnte...";
 
 	public static final String LABOR_KEIN_KRUEMEL = "Dr. Little hat keinen Krümel, den er untersuchen könnte...";
 	public static final String LABOR_GESUNDER_KUCHEN = "Der Krümel stellt sich zwar als Kalorienbombe, aber prinzipiell ungiftig heraus.";
 	public static final String LABOR_GIFTIGER_KUCHEN = "Das Verfallsdatum des Krümmels wurde schon lange überschritten...";
 
 	public static final String BEFEHL_LADEN = "lade";
 
 	public static final String ABLEGEN_TEXT = "Sie legen einen Krümel ab.";
 	public static final String NICHTS_ZUM_ABLEGEN = "Sie haben nichts zum ablegen...";
 
 	public static final String KATZE_IM_AKTUELLEN_RAUM = "Plötzlich erscheint eine grimmige Katze, die Dr. Little angestarrt. Vielleicht lässt sie sich mit einem Krümel besänftigen.";
 	public static final String KATZE_GREIFT_AN = "Als Dr. Little in den nächsten Raum schielte, griff ihn die Katze an.";
 	public static final String KATZE_STIRBT = "Die Katze fällt taumelnd zu Boden. Es scheint so, als sei der Krümel für Katzen nicht sonderlich gesund.";
 	public static final String KATZE_IST_SATT_GEWORDEN = "Die Katze frisst den Krümel und sieht jetzt sehr satt aus.";
 
 	public static final String BEFEHL_FUETTERE = "füttere";
 	public static final String BEFEHL_FUETTERE_GUT = "füttere guter krümel";
 	public static final String BEFEHL_FUETTERE_SCHLECHT = "füttere schlechter Krümel";
 	public static final String BEFEHL_FUETTERE_UNBEKANNT = "füttere krümel";
 
 	public static final String BEFEHL_FUETTERE_NICHTS_DA_ZUM_FUETTERN = "Hier ist nichts, was Dr. Little füttern könnte.";
 
 	public static final String KATZE_VERJAGT_DIE_MAUS = "Die Katze verjagt die Maus in diesem Raum.";
 
 	public static final String KATZE_HAT_KEINEN_HUNGER = "Die Katze scheint keinen Hunger mehr zu haben.";
 
 	public static final String BEFEHL_ABLEGEN = "ablegen";
 
 	public static final String BEFEHL_INVENTAR = "inventar";
 
 	public static final String KEINRAUMZUMSCHAUN = "Dort ist leider kein Raum, in den Dr. Little schauen kann.";
 
 	public static final String MODUS_AUSWAHL_SINGLEPLAYER = "Einzelspieler";
 
 	public static final String MODUS_AUSWAHL_MULTIPLAYER = "Mehrspieler";
 	public static final String MODUS_AUSWAHL_SERVERIPLABEL = "Geben Sie die Host-IP ein: ";
 	public static final String MODUS_AUSWAHL_NAMEPLABEL = "Geben Sie Ihren Spielernamen ein: ";
 	public static final String MODUS_AUSWAHL_SERVERPORTLABEL = "Geben sie den Host-Port ein: ";
 
 	public static final String BEFEHL_BEINSTELLEN = "bein stellen";
 	public static final String BEINSTELLEN_KEINER_DA = "Hier ist niemand, dem du ein Bein stellen könntest.";
 	public static final String BEINSTELLEN_AUFSTEHEN = "Du stehst langsam wieder auf und kannst weiterspielen.";
 	public static final String BEINSTELLEN_GEFALLEN_INAKTIV = " liegt am Boden und kann sich vor Schmerzen nicht bewegen.";
 
 	public static final String DAISTKEINRAUM = "In dieser Richtung ist keine Tür.";
 
 	public static final String beinstellenAnderemSpieler(String spielername)
 	{
 		return "Du schmeißt " + spielername + " zu Boden.";
 	}
 
 	public static final String beinstellenBekommen(String spielername)
 	{
 		return "Du wirst von " + spielername + " zu Boden geschmissen.";
 	}
 
 	/**
 	 * @param spieler
 	 * @return
 	 */
 	public static final String beinStellenSchaden(String name)
 	{
 		return "Das Bein Stellen war für " + name
				+ " so anstrengend, so dass er ein Lebenspunkt verloren hat.";
 	}
 
 }
