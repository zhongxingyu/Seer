 /*
  * Copyright (c) 2013, Francis Galiegue <fgaliegue@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the Lesser GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Lesser GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.github.fge.msgsimple.provider;
 
 import com.github.fge.msgsimple.InternalBundle;
 import com.github.fge.msgsimple.source.MessageSource;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import static org.mockito.Mockito.*;
 import static org.testng.Assert.*;
 
 public final class LoadingMessageSourceProviderTest
 {
     private static final InternalBundle BUNDLE
         = InternalBundle.getInstance();
 
     private LoadingMessageSourceProvider.Builder builder;
     private MessageSourceLoader loader;
     private MessageSource defaultSource;
     private MessageSource source;
 
     @BeforeMethod
     public void init()
     {
         builder = LoadingMessageSourceProvider.newBuilder();
         loader = mock(MessageSourceLoader.class);
         defaultSource = mock(MessageSource.class);
         source = mock(MessageSource.class);
     }
 
     @Test
     public void cannotBuildWithoutALoader()
     {
         try {
             builder.build();
             fail("No exception thrown!");
         } catch (IllegalArgumentException e) {
             assertEquals(e.getMessage(), BUNDLE.getMessage("cfg.noLoader"));
         }
     }
 
     @Test
     public void cannotSetNullLoader()
     {
         try {
             builder.setLoader(null);
             fail("No exception thrown!");
         } catch (NullPointerException e) {
             assertEquals(e.getMessage(), BUNDLE.getMessage("cfg.nullLoader"));
         }
     }
 
     @Test(dependsOnMethods = "cannotSetNullLoader")
     public void loaderIsUsedWhenItIsSet()
         throws IOException
     {
         final MessageSourceProvider provider = builder.setLoader(loader).build();
 
         final Locale locale = Locale.ROOT;
 
         provider.getMessageSource(locale);
         verify(loader, only()).load(locale);
     }
 
     @Test
     public void cannotProvideNullDefaultSource()
     {
         try {
             builder.setDefaultSource(null);
             fail("No exception thrown!");
         } catch (NullPointerException e) {
             assertEquals(e.getMessage(),
                 BUNDLE.getMessage("cfg.nullDefaultSource"));
         }
     }
 
     @Test(dependsOnMethods = {
         "loaderIsUsedWhenItIsSet",
         "cannotProvideNullDefaultSource"
     })
     public void defaultSourceIsReturnedWhenLoaderHasNoMatch()
     {
         final MessageSourceProvider provider = builder.setLoader(loader)
             .setDefaultSource(defaultSource).build();
 
         assertSame(provider.getMessageSource(Locale.ROOT), defaultSource);
     }
 
     @Test(dependsOnMethods = {
         "loaderIsUsedWhenItIsSet",
         "cannotProvideNullDefaultSource"
     })
     public void defaultSourceIsReturnedIfLoaderThrowsAnException()
         throws IOException
     {
         when(loader.load(any(Locale.class))).thenThrow(new IOException());
         final MessageSourceProvider provider = builder.setLoader(loader)
             .setDefaultSource(defaultSource).build();
 
         assertSame(provider.getMessageSource(Locale.ROOT), defaultSource);
     }
 
     @Test(dependsOnMethods = "loaderIsUsedWhenItIsSet")
     public void loadingIsOnlyCalledOnce()
         throws IOException, InterruptedException, ExecutionException
     {
         final Locale locale = Locale.ROOT;
         when(loader.load(locale)).thenReturn(source);
 
         final MessageSourceProvider provider = builder.setLoader(loader).build();
 
         final int nThreads = 30;
         final ExecutorService service = Executors.newFixedThreadPool(nThreads);
         final List<Callable<MessageSource>> callables
             = new ArrayList<Callable<MessageSource>>(nThreads);
 
         for (int i = 0; i < nThreads; i++)
             callables.add(new Callable<MessageSource>()
             {
                 @Override
                 public MessageSource call()
                     throws IOException
                 {
                     return provider.getMessageSource(Locale.ROOT);
                 }
             });
 
         final List<Future<MessageSource>> results = service.invokeAll(callables);
         service.shutdown();
 
         for (int i = 0; i < nThreads; i++)
             assertSame(results.get(i).get(), source);
 
         verify(loader, only()).load(Locale.ROOT);
     }
 
     @Test
     public void cannotSetNonsensicalTimeoutDuration()
     {
         try {
             builder.setLoadTimeout(0L, null);
             fail("No exception thrown!");
         } catch (IllegalArgumentException e) {
             assertEquals(e.getMessage(),
                 BUNDLE.getMessage("cfg.nonPositiveDuration"));
         }
 
         try {
             builder.setLoadTimeout(-1L, null);
             fail("No exception thrown!");
         } catch (IllegalArgumentException e) {
             assertEquals(e.getMessage(),
                 BUNDLE.getMessage("cfg.nonPositiveDuration"));
         }
     }
 
     @Test(dependsOnMethods = "cannotSetNonsensicalTimeoutDuration")
     public void cannotSetNullTimeoutUnit()
     {
         try {
             builder.setLoadTimeout(1L, null);
             fail("No exception thrown!");
         } catch (NullPointerException e) {
             assertEquals(e.getMessage(), BUNDLE.getMessage("cfg.nullTimeUnit"));
         }
     }
 
     @Test(dependsOnMethods = {
         "loaderIsUsedWhenItIsSet",
         "cannotSetNullTimeoutUnit"
     })
     public void whenLoadTimesOutDefaultSourceIsReturned()
         throws IOException
     {
         when(loader.load(Locale.ROOT)).then(new Answer<MessageSource>()
         {
             @Override
             public MessageSource answer(final InvocationOnMock invocation)
                 throws IOException
             {
                 try {
                     TimeUnit.SECONDS.sleep(1L);
                 } catch (InterruptedException ignored) {
                     Thread.currentThread().interrupt();
                 }
                 return source;
             }
         }).thenReturn(source);
 
         final MessageSourceProvider provider
             = builder.setLoader(loader)
             .setLoadTimeout(100L, TimeUnit.MILLISECONDS)
             .setDefaultSource(defaultSource).build();
 
         assertSame(provider.getMessageSource(Locale.ROOT), defaultSource);
         assertSame(provider.getMessageSource(Locale.ROOT), source);
     }
 
     @Test
     public void cannotSetNonsensicalExpiryDuration()
     {
         try {
             builder.setExpiryTime(0L, null);
             fail("No exception thrown!");
         } catch (IllegalArgumentException e) {
             assertEquals(e.getMessage(),
                 BUNDLE.getMessage("cfg.nonPositiveDuration"));
         }
 
         try {
             builder.setLoadTimeout(-1L, null);
             fail("No exception thrown!");
         } catch (IllegalArgumentException e) {
             assertEquals(e.getMessage(),
                 BUNDLE.getMessage("cfg.nonPositiveDuration"));
         }
     }
 
     @Test(dependsOnMethods = "cannotSetNonsensicalExpiryDuration")
     public void cannotSetNullExpiryTimeUnit()
     {
         try {
             builder.setExpiryTime(1L, null);
             fail("No exception thrown!");
         } catch (NullPointerException e) {
             assertEquals(e.getMessage(), BUNDLE.getMessage("cfg.nullTimeUnit"));
         }
     }
 
     @Test
     public void expiryWorksAsExpected()
         throws IOException, InterruptedException
     {
         final MessageSource source2 = mock(MessageSource.class);
         final MessageSource source3 = mock(MessageSource.class);
 
         when(loader.load(any(Locale.class)))
             .thenReturn(source)
             .thenReturn(source2)
             .thenReturn(source3);
 
         final MessageSourceProvider provider = builder.setLoader(loader)
             .setExpiryTime(10L, TimeUnit.MILLISECONDS).build();
 
         final MessageSource first = provider.getMessageSource(Locale.ROOT);
         TimeUnit.MILLISECONDS.sleep(50L);
         final MessageSource second = provider.getMessageSource(Locale.ROOT);
         TimeUnit.MILLISECONDS.sleep(50L);
         final MessageSource third = provider.getMessageSource(Locale.ROOT);
 
         verify(loader, times(3)).load(Locale.ROOT);
 
         assertSame(first, source);
         assertSame(second, source2);
         assertSame(third, source3);
     }
 
     @Test
     public void expiryCausesFailedLoadsToRetry()
         throws IOException, InterruptedException
     {
         when(loader.load(any(Locale.class)))
             .thenThrow(new IOException())
             .thenReturn(source);
 
         final MessageSourceProvider provider = builder.setLoader(loader)
             .setExpiryTime(10L, TimeUnit.MILLISECONDS)
             .setDefaultSource(defaultSource).build();
 
         final MessageSource before = provider.getMessageSource(Locale.ROOT);
         TimeUnit.MILLISECONDS.sleep(50L);
         final MessageSource after = provider.getMessageSource(Locale.ROOT);
 
         verify(loader, times(2)).load(Locale.ROOT);
 
         assertSame(before, defaultSource);
         assertSame(after, source);
     }
 
     /*
      * Adapted version of the test at the URL below:
      *
      *  http://codereview.stackexchange.com/questions/27452/review-custom-simple-cache-with-load-timeout-and-expiry-thread-safety-tests/27486?noredirect=1#27486
      */
     @Test
     public void expiryDoesNotInterfereWithLongMessageLoad()
         throws IOException
     {
         when(loader.load(any(Locale.class))).then(new Answer<MessageSource>()
         {
             @Override
             public MessageSource answer(final InvocationOnMock invocation)
                 throws IOException
             {
                 try {
                    TimeUnit.MILLISECONDS.sleep(50L);
                 } catch (InterruptedException ignored) {
                     Thread.currentThread().interrupt();
                 }
                 return source;
             }
         });
 
         final MessageSourceProvider provider = builder.setLoader(loader)
             .setExpiryTime(20L, TimeUnit.MILLISECONDS)
             .setDefaultSource(defaultSource).build();
 
        assertSame(provider.getMessageSource(Locale.ROOT), source);
     }
 }
