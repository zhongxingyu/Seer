 package me.qmx.jruby.ext.date;
 
 import org.jruby.Ruby;
 import org.jruby.RubyClass;
 import org.jruby.anno.JRubyMethod;
 import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.Visibility;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.jruby.runtime.load.BasicLibraryService;
 
 import java.io.IOException;
 
 public class DateService implements BasicLibraryService {
 
     public boolean basicLoad(Ruby runtime) throws IOException {
         runtime.getLoadService().require("time");
         final RubyClass dateClass = runtime.getClass("Date");
         final RubyClass singletonClass = dateClass.getSingletonClass();
         singletonClass.defineAlias("_strptime_i_old", "_strptime_i");
         singletonClass.defineAnnotatedMethods(DateService.class);
 
         return true;
     }
 
    @JRubyMethod(name = "_strptime_i", visibility = Visibility.PRIVATE)
     public static IRubyObject strptime_i(ThreadContext ctx, IRubyObject self, IRubyObject str, IRubyObject fmt, IRubyObject bag) {
         System.out.println("overriden, forwarding to ruby");
         return self.callMethod(ctx, "_strptime_i_old", new IRubyObject[]{str, fmt, bag});
     }
 }
