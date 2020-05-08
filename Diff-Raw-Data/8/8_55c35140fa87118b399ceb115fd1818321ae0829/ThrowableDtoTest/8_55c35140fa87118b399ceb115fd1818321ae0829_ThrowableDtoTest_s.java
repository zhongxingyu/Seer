 package org.kalibro.dto;
 
 import org.junit.Test;
 import org.kalibro.core.concurrent.VoidTask;
 
 public class ThrowableDtoTest extends AbstractDtoTest<Throwable> {
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
		doReturn(Exception.class.getName()).when(dto, "throwableClass");
 		doReturn(entity.getStackTrace()).when(dto, "stackTrace");
 	}
 
 	@Override
 	protected Throwable loadFixture() {
 		return new Throwable("ThrowableDtoTest message", new NullPointerException());
 	}
 
 	@Test
 	public void shouldThrowErrorIfCannotConvert() throws Exception {
 		doReturn("inexistent.Class").when(dto, "throwableClass");
 		assertThat(new VoidTask() {
 
 			@Override
 			protected void perform() throws Throwable {
 				dto.convert();
 			}
 		}).throwsError().withMessage("Could not convert Throwable.").withCause(ClassNotFoundException.class);
 	}
 }
