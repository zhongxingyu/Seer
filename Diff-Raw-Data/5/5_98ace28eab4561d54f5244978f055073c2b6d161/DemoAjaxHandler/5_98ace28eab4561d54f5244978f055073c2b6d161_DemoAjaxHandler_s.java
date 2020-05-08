 package me.footlights.demos.good;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import scala.Option;
 
 import me.footlights.api.File;
 import me.footlights.api.KernelInterface;
 import me.footlights.api.WebRequest;
 import me.footlights.api.ajax.AjaxHandler;
 import me.footlights.api.ajax.Context;
 import me.footlights.api.ajax.JavaScript;
 
 import me.footlights.demos.library.Library;
 
 import static me.footlights.demos.good.DemoAjaxHandler.AjaxRequest.*;
 
 
 /** A demonstration of what a {@link Application}'s {@link AjaxHandler} can do. */
 class DemoAjaxHandler extends Context
 {
 	enum AjaxRequest
 	{
 		INIT,
 		TEST_METHODS,
 		SYSCALL,
 		CONTENT,
 		ALL_DONE,
 		CLICKED,
 		OPEN_FILE,
 		LOG,
 	}
 
 	DemoAjaxHandler(final KernelInterface kernel, final Logger log)
 	{
 		register(INIT.name().toLowerCase(), new AjaxHandler()
 			{
 				@Override public JavaScript service(WebRequest request)
 					throws FileNotFoundException, SecurityException, Throwable
 				{
 					return new JavaScript()
 						.append(makeDiv("Initializing well-behaved application."))
 						.append(makeDiv("The time is " + new Date()))
 						.append(ajax(AjaxRequest.TEST_METHODS.name()));
 				}
 			});
 
 		register(TEST_METHODS.name(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				JavaScript response = new JavaScript();
 
 				response.append(
 					makeDiv("Test static method in the Helper class... " + Helper.staticHelp()));
 
 				response.append(makeDiv("Ok, that was fine. Now a constructor... "));
 				Helper h = new Helper();
 
 				response.append(makeDiv("And a regular method... "+ h.help()));
/*
 				response.append(
 					makeDiv("Test static method from a library: '" + Library.staticMethod() + "'"));
 
 				response.append(
 					makeDiv("Test regular library method: '" + new Library().method() + "'"));
*/
 				response.append(ajax(AjaxRequest.CONTENT.name()));
 				return response;
 			}
 		});
 
 		register(CONTENT.name(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				return new JavaScript()
 					.append(makeDiv("loading static JavaScript..."))
 					.append("context.load('test.js');")
 					.append(ajax(AjaxRequest.SYSCALL.name()))
 					;
 			}
 		});
 
 		register(SYSCALL.name(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				JavaScript response = new JavaScript();
 
 				response.append(makeDiv("Finally, do a 'syscall'..."));
 				if (kernel == null)
 					response.append(makeDiv("but we can't! our kernel reference is null."));
 				else
 				{
 					try
 					{
 						File file = kernel.save(ByteBuffer.wrap("Hello, world!".getBytes())).get();
 						response.append(makeDiv("saved file: " + file));
 					}
 					catch (IOException e)
 					{
 						response.append(makeDiv("Error saving data: " + e));
 						log.log(Level.SEVERE, "Error saving data", e);
 					}
 				}
 
 				response.append(ajax(AjaxRequest.ALL_DONE.name()));
 				return response;
 			}
 		});
 
 		register(ALL_DONE.name(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				return new JavaScript().append("context.log('The app works!');");
 			}
 		});
 
 		register(CLICKED.name().toLowerCase(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				final String name = request.shift().path();
 				int count = clicks.containsKey(name) ? clicks.get(name) + 1 : 1;
 				clicks.put(name, count);
 				if ((count % 5) != 1) return new JavaScript();
 
 				return new JavaScript()
 					.append("context.log('Click #").appendText(Integer.toString(count))
 					.appendText(" for '").appendText(name).appendText("'").append("');");
 			}
 		});
 
 		register(OPEN_FILE.name().toLowerCase(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				Option<File> file = kernel.openLocalFile();
 				if (file.isEmpty())
 					return new JavaScript()
 						.append("context.log('User cancelled file dialog');");
 
 				return new JavaScript()
 					.append(
 						makeDiv("Opened " + file.get().getInputStream().available() + " B file"));
 			}
 		});
 
 		register(LOG.name().toLowerCase(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				return new JavaScript().append("this is a message to log");
 			}
 		});
 	}
 
 
 	/** The number of times the user has clicked the 'local' button. */
 	private final Map<String,Integer> clicks = new HashMap<String,Integer>();
 
 
 	private static JavaScript makeDiv(String text)
 	{
 		return new JavaScript()
 		.append("context.root.appendElement('div')")
 		.append(".appendText('")
 		.append(JavaScript.sanitizeText(text))
 		.append("')");
 	}
 
 	private static JavaScript ajax(String command)
 	{
 		return new JavaScript()
 			.append("context.ajax('")
 			.append(JavaScript.sanitizeText(command))
 			.append("')");
 	}
 }
