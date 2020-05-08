 import java.util.Date; 
 import models.Surfer;
 import models.SurferDB;
 import models.UserInfoDB;
 import play.Application;
 import play.GlobalSettings;
 import play.Play;
 import views.formdata.SurferFormData;
 
 public class Global extends GlobalSettings {
 
   public void onStart(Application app) {
 
     String adminEmail = Play.application().configuration().getString("digits.admin.email");
     String adminPassword = Play.application().configuration().getString("digits.admin.password");
     UserInfoDB.defineAdmin("Andrew", adminEmail, adminPassword);
     
     if (SurferDB.getSurferList().isEmpty()) {
     Surfer surfer21 =
         new Surfer(
             "Pauline Ado",
             "Anglet, France",
             "http://cdn.surf.transworld.net/files/2009/01/06/paulineadof_l.jpg",
             "http://www.freaksurfmag.com/Images/oakley-pro-junior-girls/pauline-ado.jpg",
             "As the 2009 ASP World Junior Champion, Pauline Ado is one of Europe’s most promising young faces. "
             + "Reserved yet competitively driven, the diminutive blonde was raised in the wave-rich Basque Country "
             + "before moving to Biarritz, where she's refined her surfing at some of the best beachbreaks in Europe. "
             + "Now, having finished high school, she'll be devoting herself fully to the WQS, and judging from what "
             + "we've seen so far, she may just be joining countryman Lee Ann Curren in the upper echelons of women's "
             + "surfing in years to come.",
             "https://pbs.twimg.com/profile_images/3136992645/ac7850df00bb561fee245b86b2545ef9.jpeg", 
             "Ado", "Female", "Regular", "France");
     Surfer surfer20 =
         new Surfer(
             "Bianca Buitendag",
             "Victoria Bay, South Africa",
             "",
             "http://ugc4.mporatrons.com/thumbs/AA691UAqcGc_1280x720_0005.jpg",
             "Bianca grew up in a Surfing family with two brothers, surfing from age 7.  "
             + "She learned to surf in the beach breaks of the Strand as well as at the Jongensfontein point break.  "
             + "The family then moved to Victoria Bay when she was age 11.  It was here that Bianca started focusing "
             + "more on competitive surfing.  Bianca soon stood out at a young age winning the Billabong u/20 girls "
             + "finals at age 13 as well as coming 9th at Mr Price WQS at age 14.  By the year 2009 Bianca won every "
             + "single contest she entered in South Africa.",
             "http://sphotos-b.xx.fbcdn.net/hphotos-ash3/s720x720/555242_461808770502092_1679674257_n.jpg", "Bianca", 
             "Female", "Goofy", "South Africa");
     Surfer surfer19 =
         new Surfer(
             "Lakey Peterson",
             "Santa Barbara, California USA",
             "3rd place at US Open of Surfing, 1st place, ASP North American Champ",
             "http://img.bnqt.com/CMS/bnqt/network/bnqt.com/media/editor/girls/0001-9f368307-516ecc72-d13d-efe63410.jpg",
             "In 2000, when Lakey was just 5 years old, her parents, David and Sue, rounded up their youngest child and "
             + "her two older siblings —Whitney, then 13, and Parker, 10 — and set out on a year-long, around-the-world "
             + "adventure. It was during this trip that Lakey learned to surf. For three months, the Petersons set up "
             + "shop in Manly Beach, Australia, where their littlest member instantly earned the nickname \"Lakey "
             + "Legend\" from the locals for effortlessly catching wave after wave on her boogie board.",
             "http://img246.imageshack.us/img246/4176/img9612.jpg", "Lakey", "Female", "Regular", "United States");
     Surfer surfer18 =
         new Surfer(
             "Coco Ho",
             "Sunset Beach, Hawaii, USA",
             "2007 Triple Crown Rookie of the year\n"
             + "2009 ASP Rookie of the year",
             "http://a3.espncdn.com/photo/2012/1112/as_realsurf_coco_ho.jpg",
             "When she was just seven years old, Coco Ho learned to surf in a spirited effort to impress her older "
             + "brother. \"My brother's one of the most innovative surfers around, and I wanted to be just like him,\" "
             + "says Coco, now 21, of professional surfer Mason Ho. \"My first memory surfing was going before school "
             + "on a new board I got for my birthday, and coming to class with wet hair and telling everyone I went "
             + "surfing before school,\" she adds. \"I thought I was so cool.\" But while her family also includes "
             + "surf legends Michael Ho (her dad) and Derek Ho (her uncle), Coco soon became an idol in her own right. "
             + "Qualifying for the Association of Surfing Professionals Women's World Tour at age 17—and repeatedly "
             + "voted a top three fan favorite in Surfer magazine's Surfer Poll—Coco now seeks to build on her "
             + "award-studded surf career while inspiring other girls to break through barriers and pursue their "
             + "passions.",
             "http://bikinibird.com/wp-content/uploads/2012/12/BB_TSD_Vco_Coco_Quinsey_0291_f.jpg", 
             "Coco", "Female", "Regular", "United States");
     Surfer surfer17 =
         new Surfer(
             "Stephanie Gilmore",
             "Tweed Heads, New South Wales, Australia",
             "Five-time world champion on the Women's ASP World Tour (2007, 2008, 2009, 2010, 2012)",
             "http://a.espncdn.com/photo/2012/1127/as_surf_steph_reo_servais_2048.jpg",
             "Gilmore's life as a surfer began at age 10 when she stood on a bodyboard. By age 17 she was entering world"
             + " tour events as a wild card competitor, which paid off with a victory at the 2005 Roxy Pro Gold Coast. "
             + "In her next season she won another wild card event, the 2006 Havaianas Beachley Classic. Gilmore's "
             + "success on the WQS (World Qualifying Series) tour qualified her for the 2007 Women's ASP World Tour and"
             + " she did not disappoint. She won four of the eight events and claimed the 2007 World Title. "
             + "She would repeat her success in 2008, 2009 and 2010",
             "http://upload.wikimedia.org/wikipedia/commons/thumb/5/52/"
             + "Stephanie_Gilmore.jpg/419px-Stephanie_Gilmore.jpg",
             "Gilmore", "Female", "Regular", "Australia");
     Surfer surfer16 =
         new Surfer(
             "Courtney Conlogue",
             "Santa Ana, California",
             "",
             "http://a1.espncdn.com/photo/2013/0709/espnw_g_conlogue1_800.jpg",
             "Courtney Conlogue is the kind of California kid who was good at almost every sport she tried. She could "
             + "have pursued track or soccer, but her heart was in surfing from the moment her father put her on a "
             + "board at age 4.\nNow the 20-year-old is a three-year veteran of the Association of Surfing "
             + "Professionals,"
             + " ranked fourth on the list of elite pros and set to compete for the Roxy Pro title in Biarritz, France. "
             + "She won an event in New Zealand in April, and is in the hunt for her first title as ASP women's world "
             + "champion. It's a challenge she relishes.",
             "http://media.outsideonline.com/images/364*488/WCMDEV_151673_03_courtney_100.jpg", "Conlogue", "Female", 
             "Regular", "United States");
     Surfer surfer15 =
         new Surfer(
             "Sally Fitzgibbons",
             "Gerroa, New South Wales, Australia",
             "",
             "http://cdn.bleacherreport.net/images_root/slides/photos/002/097/417/sally_fitzgibbons_display_"
             + "image.jpg?1333545704",
             "Surfing superstar Sally Fitzgibbons is one of the top ranked female surfers in the world. In March, 2012, "
             + "Fitzgibbons won her second straight ASP 6-Star win in a row at Newcastle's Surfest.",
             "https://pbs.twimg.com/profile_images/710644548/Sally2.jpg", "Fitzgibbons", "Female", "Regular", 
             "Australia");
     Surfer surfer14 =
         new Surfer(
             "Tyler Wright",
             "Lennox Head, New South Wales, Australia",
             "ASP Rookie of the year",
            "http://images.smh.com.au/2012/02/11/3030926/art-353-tyler-wright-200x0.jpg",
             "Born into a dedicated surfing family, Tyler is making her mark in the surfing world having already "
             + "defeated some of the top 17 female surfers on the planet and earned the critics’ praise as one to "
             + "watch in coming years.\n"
             + "Tyler attracted the surfing world’s attention after she received a wildcard entry into the Beachley "
             + "Classic after winning the Oakley trails in 2008. She went on and defeated some of the world’s leading "
             + "surfers including Brazil’s Silvana Lima and ASP Women’s World Champion Stephanie Gilmore, and claimed "
             + "the biggest prize purse on the ASP World Women’s Tour. At only 14 years-old, Tyler broke a record for "
             + "being the youngest winner of a CT event, both male and female.",
            "http://www.aspworldtour.com/wp-content/uploads/2011/01/wright_t4080pipe10cestari_l1.jpg", 
             "Tyler", "Female", "Regular", "Australia");
     Surfer surfer13 =
         new Surfer(
             "Carissa Moore",
             "Honolulu, Hawaii, USA",
             "2011 and 2013 ASP Women's World Tour Champion",
             "http://www.lat34.org/quick_hits/wp-content/uploads/2008/11/moore_c1985reefpro08rowland_m.jpg",
             "Back in her NSSA days, Carissa Moore stunned the world with her skill. Everyone who watched her surf "
             + "swore that she would be queen one day.\n"
             + "Sure enough, Carissa already has two ASP World Championship titles to her name. On top of that, "
             + "she regularly produces clips that redefine women’s surfing. She’s got the power of a moose and the "
             + "finesse of a leopard. She’ll do the biggest hack you’ve ever seen and follow it up with an air-reverse. "
             + "And, most importantly, she does it all with a smile.",
             "http://cdn.surf.transworld.net/wp-content/blogs.dir/443/files/carissa-moore-"
             + "hottest-girls-in-pro-surfing/carissa_photobrent006.JPG", "Carissa", "Female", "Regular", 
             "United States");
     Surfer surfer12 =
         new Surfer(
             "Nat Young",
             "Sydney, New South Wales",
             "2 times World Surfing Champion 1996, 1970.",
             "http://www.surfline.com/surfaz/images/young_nat/grannis_natyoung2.jpg",
             "Born in Sydney, New South Wales, Young grew up in the small coastal suburb of Collaroy. "
             + "In 1964, he was runner-up in the Australian junior championship at Manly, and two years later was named "
             + "world surfing champion in 1966. He won the title again (then called the Smirnoff World Pro/Am) in 1970. "
             + "Young won three Australian titles in 1966, 1967 and 1969, and won the Bells Beach Surf Classic a record "
             + "four times. Young featured in a number of important surf films of '60s and '70s including the classic "
             + "1973 surf movie Crystal Voyager and he also had a featured role as surfer Nick Naylor in the 1979 "
             + "Australian drama film Palm Beach",
             "http://cdn2.coresites.mpora.com/surfeurope_new/wp-content/uploads/2013/09/lg_Nat_Young_Albert_Falzon.jpg", 
             "Nat", "Male", "Goofy", "Australia");
     Surfer surfer11 =
         new Surfer(
             "Josh Kerr",
             "Gold Coast, QLD Australia",
             "2 times airshow winner",
             "http://cdn.surf.transworld.net/files/2011/01/tj-kerr.jpg",
             "He of the Kerr-upt flip. He of the Kerr-azy times. Josh Kerr was christened on a bodyboard but graduated "
             + "to a surfboard at age eleven and was winning airshows by 16. Stomping tweaked reverse slobs. "
             + "(He won two airshow titles. One in 2001 and the other in 2003). Yet he is no one trick pony. "
             + "Josh Kerr competed on the Dream Tour stage in both 2007 and 2009",
             "http://www.rusty.com/files/Profiles/josh_kerr_mugshot.jpg", "Kerr", "Male", "Regular", "Australia");
     Surfer surfer10 =
         new Surfer(
             "Julian Wilson",
             "Coolum Beach, QLD Australia",
             "2010 Triple Crown Rookie of the Year",
             "http://b.vimeocdn.com/ts/430/185/430185000_640.jpg",
             "Julian Wilson (born November 8, 1988) is an Australian professional surfer who competes on the "
             + "Association of Surfing Professionals Men's World Tour.  "
             + "He is an ambassador for the National Breast Cancer Foundation. Julian's mother is a breast cancer "
             + "survivor and he was inspired to ride a pink board by close family friend and international cricketer "
             + "Matt Hayden that plays with a pink bat for the same cause",
             "http://blog.stylesight.com/wp-content/uploads/2011/08/"
             + "Nike_US_Open_of_Surfing_Julian_Wilson_Interview6.jpg", "Julian", "Male", "Regular", "Australia");
     Surfer surfer9 =
         new Surfer(
             "Kai Otton",
             "Queenscliff, Sydney, NSW, Australia",
             "2 times ASP World Champion 2007, 2009.",
             "http://cdn.surf.transworld.net/wp-content/blogs.dir/443/files/2011/08/kai-otton.jpg",
             "Kai Otton embraced all sports as a skinny grom from football to riding dirt bikes. Now a staple on the "
             + "ASP World Tour, he simply didn’t have a junior career. Originally hailing from the off-the-radar town "
             + "country town of Tathra, Australia, he drove to Australia’s more high profile spots, surfing events "
             + "without a seed, and living out of his car. He surfed on his own and worked in surf shops and surf "
             + "factories",
             "http://iamwok.com/wp-content/uploads/2013/01/kai_otton_m.jpg", "Kai", "Male", "Goofy", "Australia");
     Surfer surfer8 =
         new Surfer(
             "Taj Burrow",
             "Yallingup, WA Australia",
             "ASP World Tour Runner Up 1999, 2007"
             + "ASP Rookie of the Year 1998"
             + "Australian Male Surfer of the Year 1997",
             "http://costaricasurfing.org/wp-content/uploads/2013/07/Taj-Burrow.jpg",
             "In 1998 he qualified for the ASP World Tour at 18-years-old, becoming the youngest surfer to do so at "
             + "the time. Burrow had already earned a place on the world tour a year earlier, but he turned it down "
             + "stating that, as a 17-year-old, he was \"too young to do the tour full-on\". After his first year on "
             + "tour in 1998, Burrow claimed the ASP World Tour Rookie of the Year award after finishing 12th place "
             + "in the rankings",
             "http://www.lat34.com/_/Photo/200xNone/taj_burrow_2.jpg", "Taj", "Male", "Regular", "Australia");
     Surfer surfer7 =
         new Surfer(
             "Jordy Smith",
             "Cape Town, South Africa",
             "2 times ASP World Champion 2007, 2009.",
             "http://cdn.actionrecon.com/wp-content/uploads/2011/06/jordy-smith-flip-starter.jpg",
             "Jordan Michael \"Jordy\" Smith (born 11 February 1988) is a professional surfer from South Africa, "
             + "competing on the World Championship Tour (WCT). In 2007 Smith won surfing's World Qualifying Series,"
             + " the second-tier tour which leads to qualification for the WCT.\nJordy Smith won both the 2010 and "
             + "2011 Billabong J-Bay competitions in South Africa, which is part of the WCT Dream Tour, "
             + "making him the no. 1 ranked surfer in the world.\nSmith grew up in Durban and started surfing at age 6. "
             + "He attended a local Durban high school, Glenwood High School.\nSmith is known for the manoeuvres "
             + "\"rodeo flip\" and full rotation \"alley-oops\" and he has been sponsored by O'Neill since 2007.",
             "http://cdn.business.transworld.net/files/2008/06/10/jordy_smith_vestal.jpg", "Jordy", "Male", "Regular",
             "South Africa");
     Surfer surfer6 =
         new Surfer(
             "Joel Parkinson",
             "Tweed Heads, New South Wales, Australia",
             "2 times ASP World Champion 2007, 2009.",
             "http://resources2.news.com.au/images/2010/03/02/1225835/987586-joel-parkinson.jpg",
             "Joel Parkinson is an Australian surfer who competes on the ASP (Association Of Surfing Professionals) "
             + "World Tour. After twelve years competing at the elite level on the ASP World Championship Tour, "
             + "a stretch that saw him win eleven elite ASP World Title Events, plus nine additional ASP tour events, "
             + "and achieve runner-up second place to the ASP World Title four times, Parkinson won his only "
             + "ASP World Championship Tour Surfing Title on 14 December 2012 in Hawaii at the Banzai Pipeline during "
             + "the ASP World Tours' final event for 2012–the Billabong Pipeline Masters",
             "http://www.lat34.com/_/Photo/200xNone/joel_parkinson_2.jpg", "Parkinson", "Male", "Regular", "Australia");
     Surfer surfer5 =
         new Surfer(
             "Kelly Slater",
             "Cocoa Beach, Florida",
             "11 time ASP World Champion",
             "http://costaricasurfing.org/wp-content/uploads/2013/07/Kelly-Slater-1.jpg",
             "Robert Kelly Slater (born February 11, 1972, Cocoa Beach, Florida, US) is an American professional surfer "
             + "known for his competitive prowess and style. He has been crowned ASP World Tour Champion "
             + "a record 11 times, including 5 consecutive titles from 1994–98. He is the youngest (at age 20) "
             + "and the oldest (at age 39) to win the title. Upon winning his 5th world title in 1997, "
             + "Slater passed Australian surfer Mark Richards to become the most successful champion in the history of "
             + "the sport. In 2007 he also became the all-time leader in career event wins by winning "
             + "the Boost Mobile Pro event at Lower Trestles near San Clemente, California. "
             + "The previous record was held by Slater's childhood hero, 3-time world champion Tom Curren",
             "http://www.athletepromotions.com/blog/wp-content/uploads/2013/08/kelly-slater.jpg", "Kelly", "Male", 
             "Regular", "United States");
     Surfer surfer4 =
         new Surfer(
             "Mick Fanning",
             "Tweed Heads, New South Wales, Australia",
             "2 times ASP World Champion 2007, 2009."
             + "6 times Australian Male Surfer of the Year 2002, 2004, 2007, 2008, 2010, 2011.",
             "http://www.smh.com.au/ffximage/2005/04/11/mickfanning_wideweb__430x350.jpg",
             "He was born in Penrith, New South Wales, Australia on 13 June 1981 to Irish parents. "
             + "Fanning learned to surf at the age of 5 in coastal South Australia at a town called Mt Gambier, "
             + "but did not go full on until his family moved to Tweed Heads when he was twelve. "
             + "He grew up with fellow professional surfer, Joel Parkinson and fellow ripper Nat West. "
             + "On the edge of the Queensland border, Fanning had access to epic surf north and south "
             + "and he began to make a name for himself. In 1996 he established himself as one the very best surfers "
             + "to rule the Queensland points by placing in the top three at the Australian National Titles.",
             "http://upload.wikimedia.org/wikipedia/commons/b/b2/Mick_Fanning.jpg", "Mick", "Male", "Regular",
             "Australia");
     Surfer surfer3 =
         new Surfer(
             "Adriano de Souza",
             "Sao Paulo, Brazil",
             "Ranked #1 on the 2013 ASP World Tour",
             "http://epikoo.com/sites/default/files/adriano_de_souza_2.jpg",
             "Adriano De Souza rode his first wave at eight "
                 + "years old and eight years later the surf world would take notice of this young, talented surfer at the "
                 + "Billabong ASP World Junior Championships. At the 2004 event, he defeated opponents four years his senior"
                 + " and was named the youngest ASP World Junior Champion ever at 16.",
             "http://thumbs.dreamstime.com/z/adriano-de-souza-quicksilver-pro-18699912.jpg", "Adriano", "Male", 
             "Regular", "Brazil");
     Surfer surfer2 =
         new Surfer(
             "Jake Marshall",
             "San Diego, California",
             "",
             "http://www.surfingamerica.org/wp-content/uploads/2011/01/JakeMarshall2-3.jpg",
             "Many young surfers have the "
                 + "potential to make an impact on our sport, but none look more poised to do so than Jake Marshall. "
                 + "Raised on the rippable beachbreaks and reefs of San Diego's North County, Jake has developed a "
                 + "solid base of smooth rail work as well as the kind of flair that few 14-year-old surfers can match.",
             "http://www.nssa.org/photogallery/gallery/2010-11_Season/JakeMarshallPOWweb.jpg", "Jake", "Grom", 
             "Regular", "United States");
     Surfer surfer =
         new Surfer(
             "Bethany Hamilton",
             "Kauai, Hawaii",
             "Wahine O Ke Kai (Woman of the Sea) Award at the " + "SIM AWaterman’s Ball (2004)",
             "http://cdn3.dogonews.com/pictures/7220/content/Bethany-Hamilton.jpg?1302472752",
             "Bethany Hamilton has become "
                 + "a source of inspiration to millions through her story of faith, determination, and hope. Born into a "
                 + "family of surfers on February 8, 1990, on the island of Kauai, Hawaii, Bethany began surfing at a "
                 + "young age. At the age of eight, Bethany entered her first surf competition, "
                 + "the Rell Sun Menehune event on Oahu, where she won both the short and long board divisions. "
                 + "This sparked a love for surf competition within her spirit.",
             "http://www.bellaonline.com/review/issues/fall2012/images/PubPortrait_Cr_bethanyHamilton.jpg", "Bethany",
             "Female", "Regular", "United States");
     SurferDB.add(surfer.getSlug(), new SurferFormData(surfer));
     SurferDB.add(surfer2.getSlug(), new SurferFormData(surfer2));
     SurferDB.add(surfer3.getSlug(), new SurferFormData(surfer3));
     SurferDB.add(surfer4.getSlug(), new SurferFormData(surfer4));
     SurferDB.add(surfer5.getSlug(), new SurferFormData(surfer5));
     SurferDB.add(surfer6.getSlug(), new SurferFormData(surfer6));
     SurferDB.add(surfer7.getSlug(), new SurferFormData(surfer7));
     SurferDB.add(surfer8.getSlug(), new SurferFormData(surfer8));
     SurferDB.add(surfer9.getSlug(), new SurferFormData(surfer9));
     SurferDB.add(surfer10.getSlug(), new SurferFormData(surfer10));
     SurferDB.add(surfer11.getSlug(), new SurferFormData(surfer11));
     SurferDB.add(surfer12.getSlug(), new SurferFormData(surfer12));
     SurferDB.add(surfer13.getSlug(), new SurferFormData(surfer13));
     SurferDB.add(surfer14.getSlug(), new SurferFormData(surfer14));
     SurferDB.add(surfer15.getSlug(), new SurferFormData(surfer15));
     SurferDB.add(surfer16.getSlug(), new SurferFormData(surfer16));
     SurferDB.add(surfer17.getSlug(), new SurferFormData(surfer17));
     SurferDB.add(surfer18.getSlug(), new SurferFormData(surfer18));
     SurferDB.add(surfer19.getSlug(), new SurferFormData(surfer19));
     SurferDB.add(surfer20.getSlug(), new SurferFormData(surfer20));
     SurferDB.add(surfer21.getSlug(), new SurferFormData(surfer21));
     }
   }
 
 }
