 package gbc.i18n;
 
 import gbc.i18n.de.GermanTranscoder;
 import gbc.i18n.es.SpanishTranscoder;
 import gbc.i18n.fr.FrenchTranscoder;
 import gbc.i18n.pl.PolishTranscoder;
 import gbc.i18n.ru.RussianTranscoder;
 import gbc.i18n.ua.UkrainianTranscoder;
 
 public class SimpleExample {
 
 	/**
 	 * Showcase
 	 * @param args
 	 */
 	public static void main(String[] args) {
		
		// START SNIPPET: usage
		
 		// German
 		GermanTranscoder german = new GermanTranscoder();
 		System.out.println(german.decode("Victor jagt zwölf Boxkämpfer")); // result: Victor jagt zwoelf Boxkaempfer
 		System.out.println(german.fromNativeToEntities("Victor jagt zwölf Boxkämpfer")); // result: Victor jagt zw&#246;lf Boxk&#228;mpfer
 		System.out.println(german.fromEntitiesToNative("Victor jagt zw&#246;lf Boxk&#228;mpfer")); // result: Victor jagt zwölf Boxkämpfer
 		
 		// Spanish
 		SpanishTranscoder spanish = new SpanishTranscoder();
 		System.out.println(spanish.decode("El veloz murciélago hindú")); // result: El veloz murcielago hindu
 		System.out.println(spanish.fromNativeToEntities("El veloz murciélago hindú")); // result: El veloz murci&#233;lago hind&#250
 		System.out.println(spanish.fromEntitiesToNative("El veloz murci&#233;lago hind&#250;")); // result: El veloz murciélago hindú
 		
 		// French
 		FrenchTranscoder french = new FrenchTranscoder();
 		System.out.println(french.decode("Voix ambiguë d'un cœur")); // result: Voix ambigue d'un coeur
 		System.out.println(french.fromNativeToEntities("Voix ambiguë d'un cœur")); // result: Voix ambigu&#235; d'un c&#339;ur
 		System.out.println(french.fromEntitiesToNative("Voix ambigu&#235; d'un c&#339;ur")); // result: Voix ambiguë d'un cœur
 		
 		// Polish
 		PolishTranscoder polish = new PolishTranscoder();
 		System.out.println(polish.decode("Pchnąć w tę łódź jeża lub ośm skrzyń fig")); // result: Pchnac w te lodz jeza lub osm skrzyn fig
 		System.out.println(polish.fromNativeToEntities("Pchnąć w tę łódź jeża lub ośm skrzyń fig")); // result: Pchn&#261;&#263; w t&#281; &#322;&#243;d&#378; je&#380;a lub o&#347;m skrzy&#324; fig
 		System.out.println(polish.fromEntitiesToNative("Pchn&#261;&#263; w t&#281; &#322;&#243;d&#378; je&#380;a lub o&#347;m skrzy&#324; fig")); // result: Pchnąć w tę łódź jeża lub ośm skrzyń fig
 		
 		//Russian
 		RussianTranscoder russian = new RussianTranscoder();
 		System.out.println(russian.decode("Любя, съешь щипцы, — вздохнёт мэр, — кайф жгуч.")); // result: Luba, sesh shchiptsy, — vzdohnёt mer, — kaif zhguch.
 		System.out.println(russian.fromNativeToEntities("Любя, съешь щипцы, — вздохнёт мэр, — кайф жгуч.")); // result: &#1051;&#1102&#1073;&#1103;, &#1089;&#1098;&#1077;&#1096;&#1100; &#1097;&#1080;&#1087;&#1094;&#1099;, — &#1074;&#1079;&#1076;&#1086;&#1093;&#1085;ё&#1090; &#1084;&#1101;&#1088;, — &#1082;&#1072;&#1081;&#1092; &#1078;&#1075;&#1091;&#1095;.
 		System.out.println(russian.fromEntitiesToNative("&#1051;&#1102&#1073;&#1103;, &#1089;&#1098;&#1077;&#1096;&#1100; &#1097;&#1080;&#1087;&#1094;&#1099;, — &#1074;&#1079;&#1076;&#1086;&#1093;&#1085;&#1105;&#1090; &#1084;&#1101;&#1088;, — &#1082;&#1072;&#1081;&#1092; &#1078;&#1075;&#1091;&#1095;.")); // result: Любя, съешь щипцы, — вздохнёт мэр, — кайф жгуч.
 		
 		//Ukrainian
 		UkrainianTranscoder ukrainian = new UkrainianTranscoder();
 		System.out.println(ukrainian.decode("Чуєш їх, доцю, га? Кумедна ж ти, прощайся без ґольфів!")); // result: CHuesh ih, dotsu, ga? Kumedna zh ti, proshchaisa bez golfiv!
 		System.out.println(ukrainian.fromNativeToEntities("Чуєш їх, доцю, га? Кумедна ж ти, прощайся без ґольфів!")); // result: &#1051;&#1102&#1073;&#1103;, &#1089;&#1098;&#1077;&#1096;&#1100; &#1097;&#1080;&#1087;&#1094;&#1099;, — &#1074;&#1079;&#1076;&#1086;&#1093;&#1085;ё&#1090; &#1084;&#1101;&#1088;, — &#1082;&#1072;&#1081;&#1092; &#1078;&#1075;&#1091;&#1095;.
 		System.out.println(ukrainian.fromEntitiesToNative("&#1063;&#1091;&#1108;&#1096; &#1111;&#1093;, &#1076;&#1086;&#1094;&#1102, &#1075;&#1072;? &#1050;&#1091;&#1084;&#1077;&#1076;&#1085;&#1072; &#1078; &#1090;&#1080;, &#1087;&#1088;&#1086;&#1097;&#1072;&#1081;&#1089;&#1103; &#1073;&#1077;&#1079; &#1169;&#1086;&#1083;&#1100;&#1092;&#1110;&#1074;!")); // result: Чуєш їх, доцю, га? Кумедна ж ти, прощайся без ґольфів!

		// END SNIPPET: usage
 	}
 
 }
