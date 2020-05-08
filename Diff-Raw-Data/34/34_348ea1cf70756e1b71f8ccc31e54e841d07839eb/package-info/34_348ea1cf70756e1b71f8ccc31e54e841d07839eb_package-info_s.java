 @Assets(
         scripts = {
                 @Script(src = "js/jquery-1.8.3.min.js", id = "jquery")
         }
 )
 
 @Application
 @Portlet package {{name}};
 
 import juzu.Application;
 import juzu.plugin.asset.Assets;
 import juzu.plugin.asset.Script;
 import juzu.plugin.portlet.Portlet;
 
