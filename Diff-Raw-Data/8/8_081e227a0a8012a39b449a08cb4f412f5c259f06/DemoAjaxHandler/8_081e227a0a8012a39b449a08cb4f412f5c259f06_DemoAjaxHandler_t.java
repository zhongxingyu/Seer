 package me.footlights.demo.plugins.good;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import me.footlights.plugin.AjaxHandler;
 import me.footlights.plugin.Context;
 import me.footlights.plugin.File;
 import me.footlights.plugin.JavaScript;
 import me.footlights.plugin.KernelInterface;
 import me.footlights.plugin.WebRequest;
 
 import static me.footlights.demo.plugins.good.DemoAjaxHandler.AjaxRequest.*;
 
 
 /** A demonstration of what a {@link Plugin}'s {@link AjaxHandler} can do. */
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
 	}
 
 	DemoAjaxHandler(final KernelInterface kernel, final Logger log)
 	{
 		register(INIT.name().toLowerCase(), new AjaxHandler()
 			{
 				@Override public JavaScript service(WebRequest request)
 					throws FileNotFoundException, SecurityException, Throwable
 				{
 					return new JavaScript()
 						.append(makeDiv("Initializing well-behaved plugin."))
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
 
 				response.append(ajax(AjaxRequest.SYSCALL.name()));
 				return response;
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
 						File file = kernel.save(ByteBuffer.wrap("Hello, world!".getBytes()));
 						response.append(makeDiv("saved file: " + file));
 					}
 					catch (IOException e)
 					{
 						response.append(makeDiv("Error saving data: " + e));
 						log.log(Level.SEVERE, "Error saving data", e);
 					}
 				}
 
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
 					.append("context.load('test.js');")
					.append(ajax(AjaxRequest.ALL_DONE.name()))
 					;
 			}
 		});
 
 		register(ALL_DONE.name(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				return new JavaScript().append(makeDiv("The plugin works!."));
 			}
 		});
 
 		register(CLICKED.name().toLowerCase(), new AjaxHandler()
 		{
 			@Override public JavaScript service(WebRequest request)
 				throws FileNotFoundException, SecurityException, Throwable
 			{
 				return new JavaScript()
 					.append("context.log('Clicked \\'" + request.shift().path() + "\\'');");
 			}
 		});
 	}
 
 
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
