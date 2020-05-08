 /*
  * Copyright (c) 2013 Noveo Group
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * Except as contained in this notice, the name(s) of the above copyright holders
  * shall not be used in advertising or otherwise to promote the sale, use or
  * other dealings in this Software without prior written authorization.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
  * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.noveogroup.android.task;
 
 /* todo onQueueInsert и onQueueRemove - это какая-то ересz
 эти операции должны быть мгновенны.
 все пошло от желания иметь гарантированно вызываемые onCreate-onDestroy
 а при добавлении задачи в отмененную очередь у меня жизненный цикл шел вообще не так
 то есть есть необходимость - не вызывать кучу раз листенеры и не дергать
 progress dialog для по-любому отмененных задач.
  */
 ////////////////////////////////////////////////////////////////////////////////
 // * Методы при создании задачи:      onCreate      - onDestroy
 // * Методы при добавлении в очередь: onQueueInsert - onQueueRemove
 // * Методы при исполнении задачи:    onStart       - onFinish
 // * Задача отменена:   onCanceled
 // * Задача поломалась: onFailed
 // * Задача выполнена:  onSucceed
 ////////////////////////////////////////////////////////////////////////////////
 // Порядок исполнения листенеров подобран специально - сначала прямой, а в конце
 // обратный порядку добаления. Это сделано для симметрии исполнения.
 ////////////////////////////////////////////////////////////////////////////////
 // * Создали задачу и добавили в очередь
 //   > CREATE onCreate                        { in direct order
 // * Прервали задачу пока она была в очереди
 //   > проставляется флаг interrupted
 //   > CANCELED onCanceled onDestroy          } in reverse order
 // * Задача поступила на исполнение
 //   > STARTED onStart                        { in direct order
 // * Задачу пытаются прервать во время исполнения
 //   > проставляется флаг interrupted
 //   > ничего не происходит
 // * Задача корректно завершилась
 //   > SUCCEED onFinish onSucceed onDestroy   } in reverse order
 // * Задача выбросила throwable (в том числе и InterruptedException)
 //   > проставляется throwable
 //   > FAILED onFinish onFailed onDestroy     } in reverse order
 // * Задачу создали в убитом executor-е:
 //   > CANCELED onCanceled
 ////////////////////////////////////////////////////////////////////////////////
 // Таким образом возможны следующие последовательности:
 // CREATE [ADD] onCreate CANCELED onCanceled [DEL] onDestroy
 // CREATE [ADD] onCreate STARTED onStart [RUN] SUCCEED onFinish onSucceed [DEL] onDestroy
 // CREATE [ADD] onCreate STARTED onStart [RUN] FAILED  onFinish onFailed  [DEL] onDestroy
 // CANCELED onCanceled
 ////////////////////////////////////////////////////////////////////////////////
 
 /**
  * Interface definition for a callbacks to be invoked during task lifecycle.
  * <p/>
  * Each task can have a set of listeners to report its state to. When it is
  * needed the corresponding callback methods (the same for each listener) are
 * called is direct or reverse order, one by one. The task won't call a next
  * set of callbacks before the current is done.
  * <p/>
  * The callback method can take any time to execute - it only makes the task
  * lifetime longer. If some exception is thrown from a callback it won't be
  * caught and will be reported to the standard {@link Thread.UncaughtExceptionHandler}.
  * <p/>
  * The whole set of callbacks is divided onto two subsets:
  * <ul>
  * <li>
  * Life cycle callbacks
  * <ul>
  * <li>{@link #onCreate(TaskHandler)} and {@link #onDestroy(TaskHandler)}</li>
  * <li>{@link #onQueueInsert(TaskHandler)} and {@link #onQueueRemove(TaskHandler)}</li>
  * <li>{@link #onStart(TaskHandler)} and {@link #onFinish(TaskHandler)}</li>
  * </ul>
  * </li>
  * <li>
  * Informational callbacks
  * <ul>
  * <li>{@link #onCanceled(TaskHandler)} </li>
  * <li>{@link #onFailed(TaskHandler)}</li>
  * <li>{@link #onSucceed(TaskHandler)} </li>
  * </ul>
  * </li>
  * </ul>
  *
  * @see TaskListener.Default
  * @see com.noveogroup.android.task.TaskHandler.State
  */
 public interface TaskListener {
 
     /**
      * Default implementation of {@link TaskListener}.
      * <p/>
      * Does nothing.
      */
     public class Default implements TaskListener {
 
         @Override
         public void onCreate(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onQueueInsert(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onStart(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onFinish(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onQueueRemove(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onDestroy(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onCanceled(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onFailed(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
         @Override
         public void onSucceed(TaskHandler<?, ?> handler) {
             // do nothing
         }
 
     }
 
     public void onCreate(TaskHandler<?, ?> handler);
 
     public void onQueueInsert(TaskHandler<?, ?> handler);
 
     public void onStart(TaskHandler<?, ?> handler);
 
     public void onFinish(TaskHandler<?, ?> handler);
 
     public void onQueueRemove(TaskHandler<?, ?> handler);
 
     public void onDestroy(TaskHandler<?, ?> handler);
 
     public void onCanceled(TaskHandler<?, ?> handler);
 
     public void onFailed(TaskHandler<?, ?> handler);
 
     public void onSucceed(TaskHandler<?, ?> handler);
 
 }
