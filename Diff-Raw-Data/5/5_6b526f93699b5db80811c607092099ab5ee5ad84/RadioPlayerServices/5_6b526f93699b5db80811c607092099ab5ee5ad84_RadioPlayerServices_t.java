 package org.atlasapi.feeds.radioplayer;
 
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Maps;
 
 public class RadioPlayerServices {
 
 	public static final Set<RadioPlayerService> services;
 	
 	public static final Map<String, RadioPlayerService> all;
 
 	static {
 		services = ImmutableSet.<RadioPlayerService> builder().
 			add(new RadioPlayerService(300, "london")).
 			add(new RadioPlayerService(301, "berkshire")).
 			add(new RadioPlayerService(302, "bristol")).
 			add(new RadioPlayerService(303, "cambridgeshire")).
 			add(new RadioPlayerService(304, "cornwall")).
 			add(new RadioPlayerService(305, "coventry")).
 			add(new RadioPlayerService(306, "cumbria")).
 			add(new RadioPlayerService(307, "derby")).
 			add(new RadioPlayerService(308, "devon")).
 			add(new RadioPlayerService(309, "essex")).
 			add(new RadioPlayerService(310, "gloucestershire")).
 			add(new RadioPlayerService(311, "guernsey")).
 			add(new RadioPlayerService(312, "herefordandworcester")).
 			add(new RadioPlayerService(313, "humberside")).
 			add(new RadioPlayerService(314, "jersey")).
 			add(new RadioPlayerService(315, "kent")).
 			add(new RadioPlayerService(316, "lancashire")).
 			add(new RadioPlayerService(317, "leeds")).
 			add(new RadioPlayerService(318, "leicester")).
 			add(new RadioPlayerService(319, "lincolnshire")).
 			add(new RadioPlayerService(320, "manchester")).
 			add(new RadioPlayerService(321, "merseyside")).
 			add(new RadioPlayerService(322, "newcastle")).
 			add(new RadioPlayerService(323, "norfolk")).
 			add(new RadioPlayerService(324, "northampton")).
 			add(new RadioPlayerService(325, "nottingham")).
 			add(new RadioPlayerService(326, "oxford")).
 			add(new RadioPlayerService(327, "sheffield")).
 			add(new RadioPlayerService(328, "shropshire")).
 			add(new RadioPlayerService(329, "solent")).
 			add(new RadioPlayerService(330, "somerset")).
 			add(new RadioPlayerService(331, "stoke")).
 			add(new RadioPlayerService(332, "suffolk")).
 			add(new RadioPlayerService(333, "surrey")).
 			add(new RadioPlayerService(334, "sussex")).
 			add(new RadioPlayerService(335, "wiltshire")).
 			add(new RadioPlayerService(336, "york")).
 			add(new RadioPlayerService(337, "tees")).
 			add(new RadioPlayerService(338, "threecounties")).
 			add(new RadioPlayerService(339, "wm")).
 			add(new RadioPlayerService(340, "radio1").withServiceUriSuffix("radio1/england").withScheduleUri("http://www.bbc.co.uk/radio1/programmes/schedules/england")).
 			add(new RadioPlayerService(341, "1xtra")).
 			add(new RadioPlayerService(342, "radio2")).
 			add(new RadioPlayerService(343, "radio3")).
 			add(new RadioPlayerService(344, "radio4").withServiceUriSuffix("radio4/fm").withScheduleUri("http://www.bbc.co.uk/radio4/programmes/schedules/fm")).
 			add(new RadioPlayerService(345, "5live")).
 			add(new RadioPlayerService(346, "5livesportsextra")).
 			add(new RadioPlayerService(347, "6music")).
 			add(new RadioPlayerService(348, "radio7")).
 			add(new RadioPlayerService(349, "asiannetwork")).
 			add(new RadioPlayerService(350, "worldservice")).
			add(new RadioPlayerService(351, "radioscotland").withServiceUriSuffix("radioscotland/fm").withScheduleUri("http://www.bbc.co.uk/radioscotland/programmes/schedules/fm")).
 			add(new RadioPlayerService(352, "radionangaidheal")).
 			add(new RadioPlayerService(353, "radioulster")).
 			add(new RadioPlayerService(354, "radiofoyle")).
			add(new RadioPlayerService(355, "radiowales").withServiceUriSuffix("radiowales/fm").withScheduleUri("http://www.bbc.co.uk/radiowales/programmes/schedules/fm")).
 			add(new RadioPlayerService(356, "radiocymru")).
 			add(new RadioPlayerService(357, "radio4lw").withServiceUriSuffix("radio4/lw").withScheduleUri("http://www.bbc.co.uk/radio4/programmes/schedules/lw")).
 		build();
 		
 		all = Maps.uniqueIndex(services, new Function<RadioPlayerService, String>() {
 			@Override
 			public String apply(RadioPlayerService input) {
 				return String.valueOf(input.getRadioplayerId());
 			}
 		});
 	};
 
 }
