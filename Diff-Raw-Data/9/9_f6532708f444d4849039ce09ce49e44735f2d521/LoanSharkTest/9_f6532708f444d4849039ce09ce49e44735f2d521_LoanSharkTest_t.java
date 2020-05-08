 package com.jnape.loanshark;
 
 import com.jnape.loanshark.annotation.Todo;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.annotation.processing.Messager;
 import javax.annotation.processing.ProcessingEnvironment;
 import javax.annotation.processing.RoundEnvironment;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.TypeElement;
 import javax.tools.Diagnostic;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import static com.jnape.loanshark.MaxLifeResolver.DEFAULT_MAX_LIFE;
 import static com.jnape.loanshark.MaxLifeResolver.MAX_LIFE_OPTION;
 import static java.lang.String.format;
 import static java.util.Arrays.asList;
 import static org.joda.time.DateTime.now;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static testsupport.ElementTestFactory.elementWithTodo;
 import static testsupport.TodoTestFactory.todoCreated;
 
 @RunWith(MockitoJUnitRunner.class)
 public class LoanSharkTest {
 
     @Mock private ProcessingEnvironment processingEnvironment;
     @Mock private RoundEnvironment      roundEnvironment;
     @Mock private Messager              messager;
     @Mock private MaxLifeResolver       maxLifeResolver;
     private       LoanShark             loanShark;
     private       TodoFormatter         todoFormatter;
 
     @Before
     public void setUp() {
         when(processingEnvironment.getMessager()).thenReturn(messager);
 
         todoFormatter = new TodoFormatter();
         loanShark = new LoanShark();
         loanShark.init(processingEnvironment, maxLifeResolver, todoFormatter);
     }
 
     @Test
     public void supportsConfiguringMaxLifeInDaysThroughAnnotationProcessorOptions() {
         Set<String> supportedOptions = loanShark.getSupportedOptions();
         assertTrue(supportedOptions.contains(MAX_LIFE_OPTION));
     }
 
     @Test
     public void collectsExpiredTodos() {
         givenMaxLife(10);
 
         Todo fresh = todoCreated(now());
         Todo expired = todoCreated(now().minusDays(10));
         Todo rotten = todoCreated(now().minusDays(50));
 
         assertEquals(
                 asList(expired, rotten),
                 loanShark.collectExpiredTodos(asList(
                         elementWithTodo(fresh),
                         elementWithTodo(expired),
                         elementWithTodo(rotten)
                 ))
         );
     }
 
     @Test
     public void isNotFooledByFuturisticCreatedDates() {
         givenDefaultMaxLife();
 
         Todo fresh = todoCreated(now());
         Todo futuristic = todoCreated(now().plusDays(50));
 
         loanShark.collectExpiredTodos(asList(
                 elementWithTodo(fresh),
                 elementWithTodo(futuristic)
         ));
 
         verify(messager).printMessage(Diagnostic.Kind.ERROR, "Oh really? You've created a Todo in the future, have you? How interesting for you.\n" + todoFormatter.format(futuristic));
     }
 
     @Test
     @SuppressWarnings("unchecked")
     public void failsCompilationIfAnyTodosHaveExpired() {
         givenMaxLife(5);
 
         final Todo fresh = todoCreated(now());
         final Todo expiredJustNow = todoCreated(now().minusDays(5));
         final Todo expiredLastMonth = todoCreated(now().minusMonths(1));
 
        Set elements = new HashSet<Element>() {{
             add(elementWithTodo(fresh));
             add(elementWithTodo(expiredJustNow));
             add(elementWithTodo(expiredLastMonth));
         }};

         when(roundEnvironment.getElementsAnnotatedWith(Todo.class)).thenReturn(elements);
 
         loanShark.process(Collections.<TypeElement>emptySet(), roundEnvironment);
 
         verify(messager).printMessage(
                 Diagnostic.Kind.ERROR,
                 format(
                         "Found expired todos:\n%s\n%s",
                        todoFormatter.format(expiredJustNow),
                        todoFormatter.format(expiredLastMonth)
                 )
         );
     }
 
     private void givenMaxLife(int i) {
         when(maxLifeResolver.resolveMaxLifeInDays()).thenReturn(i);
     }
 
     private void givenDefaultMaxLife() {
         when(maxLifeResolver.resolveMaxLifeInDays()).thenReturn(DEFAULT_MAX_LIFE);
     }
 }
