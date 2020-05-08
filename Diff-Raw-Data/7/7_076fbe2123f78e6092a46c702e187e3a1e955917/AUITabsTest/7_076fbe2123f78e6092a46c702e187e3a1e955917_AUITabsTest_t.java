 package com.atlassian.javascript.ajs.selenium;
 
 public class AUITabsTest extends AUISeleniumTestCase
 {
 
     public void setUpTest(boolean needToCreateObjects)
     {
         openTestPage("test.html");  //open test page
         addHTMLtoElement("body",
                 "<div class=\"aui-tabs h-tabs\">\n" +
                         "    <ul class=\"tabs\">\n" +
                         "        <li class=\"active-tab first\">\n" +
                         "            <a href=\"#first\"><strong>Tab 1 - Active</strong></a>\n" +
                         "        </li>\n" +
                        "        <li  class=\"nothing\">\n" +
                        "            <a href=\"#second\" id=\"menuSecond\"><strong>Tab 2</strong></a>\n" +
                         "        </li>\n" +
                         "        <li  class=\"nothing\">\n" +
                         "            <a href=\"#third\"><strong>Tab 3 has a very long tab name</strong></a>\n" +
                         "        </li>\n" +
                         "        <li  class=\"nothing\">\n" +
                         "            <a href=\"#fourth\"><strong>Tab4hasaveryverylongnonspacedname</strong></a>\n" +
                         "        </li>\n" +
                         "    </ul>\n" +
                         "    <div class=\"pane active-pane\" id=\"first\">\n" +
                         "        <h2>This is Tab 1</h2>\n" +
                         "        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor volutpat posuere. Nunc non lacinia ligula. In convallis iaculis interdum. Sed quis risus gravida enim adipiscing semper. In magna tortor, scelerisque vel lobortis sed, tincidunt quis mauris. Sed libero neque, hendrerit ac sagittis eget, rhoncus vel augue. Proin eget blandit ligula. Praesent placerat, nisl a laoreet ultrices, mi sem suscipit nisl, non ornare neque massa nec augue. Integer consectetur elementum posuere. Integer feugiat aliquam rutrum. Praesent sapien lectus, pharetra quis pharetra vel, mattis ut orci. In vehicula nibh et enim lacinia interdum. Sed aliquam vehicula risus, vitae commodo sapien vulputate eu. Phasellus vulputate tempor aliquet.</p>\n" +
                         "        <p>Quisque commodo, lectus at venenatis volutpat, urna lacus egestas nisl, interdum fermentum risus nibh molestie mauris. Etiam a tellus ac elit accumsan scelerisque. Vivamus pellentesque, ligula eget dictum sagittis, arcu lacus scelerisque augue, in dictum eros eros rhoncus enim. Aliquam nunc metus, vestibulum eget fermentum et, gravida ut turpis. Sed bibendum bibendum sem, ac ultricies risus mollis sit amet. Duis dapibus erat placerat nunc laoreet vel mollis justo consequat. Nam tristique eleifend magna, a ultricies leo ultricies in. Etiam ac purus a ante rhoncus sollicitudin. Aenean vel quam id dui mattis mattis in molestie quam. Nulla sed ante arcu, eu lacinia enim. Ut tincidunt mi sapien, vitae vehicula magna. Aliquam condimentum orci sed dui faucibus eu laoreet massa euismod. Suspendisse potenti.</p>\n" +
                         "        <p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec gravida interdum feugiat. Aenean consequat fermentum augue sodales pulvinar. Nunc ut diam non odio sodales convallis. Suspendisse potenti. Nulla elit velit, gravida ullamcorper viverra faucibus, faucibus non odio. Duis et orci a massa laoreet viverra. Etiam porttitor vehicula lacus, id tincidunt purus commodo et. Maecenas euismod, nunc et scelerisque varius, eros magna tristique magna, ac feugiat velit lectus eu libero. Vivamus ipsum est, aliquet non scelerisque vitae, tristique a magna. Praesent in neque ac eros volutpat ultricies. Suspendisse dapibus justo et ipsum egestas lobortis. Donec sagittis luctus ipsum, non convallis magna mollis quis. Proin condimentum metus a nulla fermentum volutpat. Nullam vel erat non dolor venenatis egestas. Nam ornare, massa vitae pellentesque ornare, ipsum nisi hendrerit erat, egestas lacinia augue enim in magna. In ut sem quam, imperdiet elementum neque. Vestibulum iaculis nisl sit amet dolor iaculis gravida. Etiam a lorem sit amet lacus malesuada vehicula.</p>\n" +
                         "    </div>\n" +
                         "    <div class=\"pane\" id=\"second\">\n" +
                         "        <h2>This is Tab 2</h2>\n" +
                         "        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor volutpat posuere. Nunc non lacinia ligula. In convallis iaculis interdum. Sed quis risus gravida enim adipiscing semper. In magna tortor, scelerisque vel lobortis sed, tincidunt quis mauris. Sed libero neque, hendrerit ac sagittis eget, rhoncus vel augue. Proin eget blandit ligula. Praesent placerat, nisl a laoreet ultrices, mi sem suscipit nisl, non ornare neque massa nec augue. Integer consectetur elementum posuere. Integer feugiat aliquam rutrum. Praesent sapien lectus, pharetra quis pharetra vel, mattis ut orci. In vehicula nibh et enim lacinia interdum. Sed aliquam vehicula risus, vitae commodo sapien vulputate eu. Phasellus vulputate tempor aliquet.</p>\n" +
                         "    </div>\n" +
                         "    <div class=\"pane\" id=\"third\">\n" +
                         "        <h2>This is Tab 3</h2>\n" +
                         "        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor volutpat posuere. Nunc non lacinia ligula. In convallis iaculis interdum. Sed quis risus gravida enim adipiscing semper. In magna tortor, scelerisque vel lobortis sed, tincidunt quis mauris. Sed libero neque, hendrerit ac sagittis eget, rhoncus vel augue. Proin eget blandit ligula. Praesent placerat, nisl a laoreet ultrices, mi sem suscipit nisl, non ornare neque massa nec augue. Integer consectetur elementum posuere. Integer feugiat aliquam rutrum. Praesent sapien lectus, pharetra quis pharetra vel, mattis ut orci. In vehicula nibh et enim lacinia interdum. Sed aliquam vehicula risus, vitae commodo sapien vulputate eu. Phasellus vulputate tempor aliquet.</p>\n" +
                         "        <p>Quisque commodo, lectus at venenatis volutpat, urna lacus egestas nisl, interdum fermentum risus nibh molestie mauris. Etiam a tellus ac elit accumsan scelerisque. Vivamus pellentesque, ligula eget dictum sagittis, arcu lacus scelerisque augue, in dictum eros eros rhoncus enim. Aliquam nunc metus, vestibulum eget fermentum et, gravida ut turpis. Sed bibendum bibendum sem, ac ultricies risus mollis sit amet. Duis dapibus erat placerat nunc laoreet vel mollis justo consequat. Nam tristique eleifend magna, a ultricies leo ultricies in. Etiam ac purus a ante rhoncus sollicitudin. Aenean vel quam id dui mattis mattis in molestie quam. Nulla sed ante arcu, eu lacinia enim. Ut tincidunt mi sapien, vitae vehicula magna. Aliquam condimentum orci sed dui faucibus eu laoreet massa euismod. Suspendisse potenti.</p>\n" +
                         "    </div>\n" +
                         "    <div class=\"pane\" id=\"fourth\">\n" +
                         "        <h2>This is Tab 4</h2>\n" +
                         "        <p>Quisque commodo, lectus at venenatis volutpat, urna lacus egestas nisl, interdum fermentum risus nibh molestie mauris. Etiam a tellus ac elit accumsan scelerisque. Vivamus pellentesque, ligula eget dictum sagittis, arcu lacus scelerisque augue, in dictum eros eros rhoncus enim. Aliquam nunc metus, vestibulum eget fermentum et, gravida ut turpis. Sed bibendum bibendum sem, ac ultricies risus mollis sit amet. Duis dapibus erat placerat nunc laoreet vel mollis justo consequat. Nam tristique eleifend magna, a ultricies leo ultricies in. Etiam ac purus a ante rhoncus sollicitudin. Aenean vel quam id dui mattis mattis in molestie quam. Nulla sed ante arcu, eu lacinia enim. Ut tincidunt mi sapien, vitae vehicula magna. Aliquam condimentum orci sed dui faucibus eu laoreet massa euismod. Suspendisse potenti.</p>\n" +
                         "        <p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec gravida interdum feugiat. Aenean consequat fermentum augue sodales pulvinar. Nunc ut diam non odio sodales convallis. Suspendisse potenti. Nulla elit velit, gravida ullamcorper viverra faucibus, faucibus non odio. Duis et orci a massa laoreet viverra. Etiam porttitor vehicula lacus, id tincidunt purus commodo et. Maecenas euismod, nunc et scelerisque varius, eros magna tristique magna, ac feugiat velit lectus eu libero. Vivamus ipsum est, aliquet non scelerisque vitae, tristique a magna. Praesent in neque ac eros volutpat ultricies. Suspendisse dapibus justo et ipsum egestas lobortis. Donec sagittis luctus ipsum, non convallis magna mollis quis. Proin condimentum metus a nulla fermentum volutpat. Nullam vel erat non dolor venenatis egestas. Nam ornare, massa vitae pellentesque ornare, ipsum nisi hendrerit erat, egestas lacinia augue enim in magna. In ut sem quam, imperdiet elementum neque. Vestibulum iaculis nisl sit amet dolor iaculis gravida. Etiam a lorem sit amet lacus malesuada vehicula.</p>\n" +
                         "    </div>\n" +
                         "</div> <!-- // .aui-tabs -->\n" +
                         "<div class=\"aui-tabs v-tabs\">\n" +
                         "    <ul class=\"tabs\">\n" +
                         "        <li class=\"active-tab first\">\n" +
                         "            <a href=\"#fifth\"><strong>Tab 1 - ActiveTab4hasaveryverylongnonspacedname</strong></a>\n" +
                         "        </li>\n" +
                         "        <li  class=\"nothing\">\n" +
                         "            <a href=\"#sixth\"><strong>Tab 2</strong></a>\n" +
                         "        </li>\n" +
                         "        <li class=\"nothing\">\n" +
                         "            <a href=\"#seventh\"><strong>Tab 3 has a very long tab name</strong></a>\n" +
                         "        </li>\n" +
                         "        <li class=\"nothing\">\n" +
                         "            <a href=\"#eighth\"><strong>asdfasd</strong></a>\n" +
                         "        </li>\n" +
                         "    </ul>\n" +
                         "    <div class=\"pane active-pane\" id=\"fifth\">\n" +
                         "        <h2>This is Tab 1</h2>\n" +
                         "        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor volutpat posuere. Nunc non lacinia ligula. In convallis iaculis interdum. Sed quis risus gravida enim adipiscing semper. In magna tortor, scelerisque vel lobortis sed, tincidunt quis mauris. Sed libero neque, hendrerit ac sagittis eget, rhoncus vel augue. Proin eget blandit ligula. Praesent placerat, nisl a laoreet ultrices, mi sem suscipit nisl, non ornare neque massa nec augue. Integer consectetur elementum posuere. Integer feugiat aliquam rutrum. Praesent sapien lectus, pharetra quis pharetra vel, mattis ut orci. In vehicula nibh et enim lacinia interdum. Sed aliquam vehicula risus, vitae commodo sapien vulputate eu. Phasellus vulputate tempor aliquet.</p>\n" +
                         "        <p>Quisque commodo, lectus at venenatis volutpat, urna lacus egestas nisl, interdum fermentum risus nibh molestie mauris. Etiam a tellus ac elit accumsan scelerisque. Vivamus pellentesque, ligula eget dictum sagittis, arcu lacus scelerisque augue, in dictum eros eros rhoncus enim. Aliquam nunc metus, vestibulum eget fermentum et, gravida ut turpis. Sed bibendum bibendum sem, ac ultricies risus mollis sit amet. Duis dapibus erat placerat nunc laoreet vel mollis justo consequat. Nam tristique eleifend magna, a ultricies leo ultricies in. Etiam ac purus a ante rhoncus sollicitudin. Aenean vel quam id dui mattis mattis in molestie quam. Nulla sed ante arcu, eu lacinia enim. Ut tincidunt mi sapien, vitae vehicula magna. Aliquam condimentum orci sed dui faucibus eu laoreet massa euismod. Suspendisse potenti.</p>\n" +
                         "        <p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec gravida interdum feugiat. Aenean consequat fermentum augue sodales pulvinar. Nunc ut diam non odio sodales convallis. Suspendisse potenti. Nulla elit velit, gravida ullamcorper viverra faucibus, faucibus non odio. Duis et orci a massa laoreet viverra. Etiam porttitor vehicula lacus, id tincidunt purus commodo et. Maecenas euismod, nunc et scelerisque varius, eros magna tristique magna, ac feugiat velit lectus eu libero. Vivamus ipsum est, aliquet non scelerisque vitae, tristique a magna. Praesent in neque ac eros volutpat ultricies. Suspendisse dapibus justo et ipsum egestas lobortis. Donec sagittis luctus ipsum, non convallis magna mollis quis. Proin condimentum metus a nulla fermentum volutpat. Nullam vel erat non dolor venenatis egestas. Nam ornare, massa vitae pellentesque ornare, ipsum nisi hendrerit erat, egestas lacinia augue enim in magna. In ut sem quam, imperdiet elementum neque. Vestibulum iaculis nisl sit amet dolor iaculis gravida. Etiam a lorem sit amet lacus malesuada vehicula.</p>\n" +
                         "    </div>\n" +
                         "    <div class=\"pane\" id=\"sixth\">\n" +
                         "        <h2>This is Tab 2</h2>\n" +
                         "        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor volutpat posuere. Nunc non lacinia ligula. In convallis iaculis interdum. Sed quis risus gravida enim adipiscing semper. In magna tortor, scelerisque vel lobortis sed, tincidunt quis mauris. Sed libero neque, hendrerit ac sagittis eget, rhoncus vel augue. Proin eget blandit ligula. Praesent placerat, nisl a laoreet ultrices, mi sem suscipit nisl, non ornare neque massa nec augue. Integer consectetur elementum posuere. Integer feugiat aliquam rutrum. Praesent sapien lectus, pharetra quis pharetra vel, mattis ut orci. In vehicula nibh et enim lacinia interdum. Sed aliquam vehicula risus, vitae commodo sapien vulputate eu. Phasellus vulputate tempor aliquet.</p>\n" +
                         "    </div>\n" +
                         "    <div class=\"pane\" id=\"seventh\">\n" +
                         "        <h2>This is Tab 3</h2>\n" +
                         "        <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspendisse porttitor volutpat posuere. Nunc non lacinia ligula. In convallis iaculis interdum. Sed quis risus gravida enim adipiscing semper. In magna tortor, scelerisque vel lobortis sed, tincidunt quis mauris. Sed libero neque, hendrerit ac sagittis eget, rhoncus vel augue. Proin eget blandit ligula. Praesent placerat, nisl a laoreet ultrices, mi sem suscipit nisl, non ornare neque massa nec augue. Integer consectetur elementum posuere. Integer feugiat aliquam rutrum. Praesent sapien lectus, pharetra quis pharetra vel, mattis ut orci. In vehicula nibh et enim lacinia interdum. Sed aliquam vehicula risus, vitae commodo sapien vulputate eu. Phasellus vulputate tempor aliquet.</p>\n" +
                         "        <p>Quisque commodo, lectus at venenatis volutpat, urna lacus egestas nisl, interdum fermentum risus nibh molestie mauris. Etiam a tellus ac elit accumsan scelerisque. Vivamus pellentesque, ligula eget dictum sagittis, arcu lacus scelerisque augue, in dictum eros eros rhoncus enim. Aliquam nunc metus, vestibulum eget fermentum et, gravida ut turpis. Sed bibendum bibendum sem, ac ultricies risus mollis sit amet. Duis dapibus erat placerat nunc laoreet vel mollis justo consequat. Nam tristique eleifend magna, a ultricies leo ultricies in. Etiam ac purus a ante rhoncus sollicitudin. Aenean vel quam id dui mattis mattis in molestie quam. Nulla sed ante arcu, eu lacinia enim. Ut tincidunt mi sapien, vitae vehicula magna. Aliquam condimentum orci sed dui faucibus eu laoreet massa euismod. Suspendisse potenti.</p>\n" +
                         "    </div>\n" +
                         "    <div class=\"pane\" id=\"eighth\">\n" +
                         "        <h2>This is Tab 4</h2>\n" +
                         "        <p>Quisque commodo, lectus at venenatis volutpat, urna lacus egestas nisl, interdum fermentum risus nibh molestie mauris. Etiam a tellus ac elit accumsan scelerisque. Vivamus pellentesque, ligula eget dictum sagittis, arcu lacus scelerisque augue, in dictum eros eros rhoncus enim. Aliquam nunc metus, vestibulum eget fermentum et, gravida ut turpis. Sed bibendum bibendum sem, ac ultricies risus mollis sit amet. Duis dapibus erat placerat nunc laoreet vel mollis justo consequat. Nam tristique eleifend magna, a ultricies leo ultricies in. Etiam ac purus a ante rhoncus sollicitudin. Aenean vel quam id dui mattis mattis in molestie quam. Nulla sed ante arcu, eu lacinia enim. Ut tincidunt mi sapien, vitae vehicula magna. Aliquam condimentum orci sed dui faucibus eu laoreet massa euismod. Suspendisse potenti.</p>\n" +
                         "        <p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec gravida interdum feugiat. Aenean consequat fermentum augue sodales pulvinar. Nunc ut diam non odio sodales convallis. Suspendisse potenti. Nulla elit velit, gravida ullamcorper viverra faucibus, faucibus non odio. Duis et orci a massa laoreet viverra. Etiam porttitor vehicula lacus, id tincidunt purus commodo et. Maecenas euismod, nunc et scelerisque varius, eros magna tristique magna, ac feugiat velit lectus eu libero. Vivamus ipsum est, aliquet non scelerisque vitae, tristique a magna. Praesent in neque ac eros volutpat ultricies. Suspendisse dapibus justo et ipsum egestas lobortis. Donec sagittis luctus ipsum, non convallis magna mollis quis. Proin condimentum metus a nulla fermentum volutpat. Nullam vel erat non dolor venenatis egestas. Nam ornare, massa vitae pellentesque ornare, ipsum nisi hendrerit erat, egestas lacinia augue enim in magna. In ut sem quam, imperdiet elementum neque. Vestibulum iaculis nisl sit amet dolor iaculis gravida. Etiam a lorem sit amet lacus malesuada vehicula.</p>\n" +
                         "    </div>\n" +
                         "</div>"
         );
 
         if (needToCreateObjects)
         {
             client.getEval("window.AJS.$(window.AJS.Tabs.setup)");
         }
     }
 
     // First pane of tabs should  be visible by default
     public void testAUITabsFirstPaneVisibleByDefault()
     {
         setUpTest(true);
         assertThat.elementVisible("css=div.h-tabs #first");
         assertThat.elementVisible("css=div.v-tabs #fifth");
 
     }
 
     // Only one pane should be visible
     public void testAUITabsOnlyOnePaneVisible()
     {
         setUpTest(true);
         assertThat.elementVisible("css=div.h-tabs #first");
 
         assertThat.elementNotVisible("css=div.h-tabs #second");
         assertThat.elementNotVisible("css=div.h-tabs #third");
         assertThat.elementNotVisible("css=div.h-tabs #fourth");
 
         assertThat.elementVisible("css=div.v-tabs #fifth");
 
         assertThat.elementNotVisible("css=div.v-tabs #sixth");
         assertThat.elementNotVisible("css=div.v-tabs #seventh");
         assertThat.elementNotVisible("css=div.v-tabs #eight");
 
     }
 
     // The visible pane should be the active-pane
     public void testAUITabsVisiblePaneIsActive()
     {
         setUpTest(true);
 
         //check that the first horizontal pane is visible
         assertThat.elementVisible("css=div.h-tabs #first");
 
         //check that the first horizontal pane is active
         assertThat.attributeContainsValue("css=div.h-tabs #first", "class", "active-pane");
 
         //check that all other panes are not active
         assertThat.attributeDoesntContainValue("css=div.h-tabs #second", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div.h-tabs #third", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div.h-tabs #fourth", "class", "active-pane");
 
         //check that the first vertical pane is visible
         assertThat.elementVisible("css=div.v-tabs #fifth");
 
         //check that the first vertical pane is active
         assertThat.attributeContainsValue("css=div.v-tabs #fifth", "class", "active-pane");
 
         //check that the rest of the panes are notactive
         assertThat.attributeDoesntContainValue("css=div.v-tabs #sixth", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div.v-tabs #seventh", "class", "active-pane");
         assertThat.attributeDoesntContainValue("css=div.v-tabs #eighth", "class", "active-pane");
     }
 
     // First  menu item should be active by default
     public void testAUITabsFirstMenuItemActiveByDefault()
     {
         setUpTest(true);
         assertThat.attributeContainsValue("css=div.h-tabs #first", "class", "active-pane");
         assertThat.attributeContainsValue("css=div.v-tabs #fifth", "class", "active-pane");
 
     }
 
     // Only one menu item should be active
     public void testAUITabsOnlyOneMenuItemActive()
     {
         setUpTest(true);
 
         //check tab first horizontal menu item is active
         assertThat.attributeContainsValue("css=div.h-tabs ul.tabs li:nth-child(1)", "class", "active-tab");
 
         //check that the rest of the menu items arent active
         assertThat.attributeDoesntContainValue("css=div.h-tabs ul.tabs li:nth-child(2)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div.h-tabs ul.tabs li:nth-child(3)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div.h-tabs ul.tabs li:nth-child(4)", "class", "active-tab");
 
         //check tab first vertical menu item is active
         assertThat.attributeContainsValue("css=div.v-tabs ul.tabs li:nth-child(1)", "class", "active-tab");
 
         //check that the rest of the menu items arent active
         assertThat.attributeDoesntContainValue("css=div.v-tabs ul.tabs li:nth-child(2)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div.v-tabs ul.tabs li:nth-child(3)", "class", "active-tab");
         assertThat.attributeDoesntContainValue("css=div.v-tabs ul.tabs li:nth-child(4)", "class", "active-tab");
     }
 
     // clicking menu item should show associated pane and hide all others
     public void testAUITabsClickingMenuItemShouldShowAssociatedPane()
     {
         setUpTest(true);
 
        client.click("css=div.h-tabs ul.tabs #menuSecond");
 
         assertThat.elementVisible("css=div.h-tabs #second");
         assertThat.elementNotVisible("css=div.h-tabs #first");
         assertThat.elementNotVisible("css=div.h-tabs #third");
         assertThat.elementNotVisible("css=div.h-tabs #fourth");
     }
 
 
 }
