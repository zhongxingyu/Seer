 package org.sonar.ide.idea.utils;
 
 import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.Nullable;
 import org.sonar.ide.ui.AbstractIconLoader;
 
 import javax.swing.*;
 
 /**
  * @author Evgeny Mandrikov
  */
 public final class IdeaIconLoader extends AbstractIconLoader {
   @Override
  public Icon getIcon(@Nullable String path) {
    if (path == null) {
      return null;
    }
     return IconLoader.getIcon(path);
   }
 }
