package tech.grasshopper.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import tech.grasshopper.logging.ExtentReportsCucumberLogger;

@Ignore
public class ErrorMessageProcessorTest {

	private ErrorMessageProcessor errorMessageProcessor;
	private ExtentReportsCucumberLogger logger;

	@Before
	public void setup() {
		logger = mock(ExtentReportsCucumberLogger.class);

		errorMessageProcessor = new ErrorMessageProcessor(logger);
	}

	@Test
	public void testThrowableWithMessage() {
		String errorMessage = "java.lang.AssertionError: expected [true] but found [false]\r\n\tat org.testng.Assert.fail(Assert.java:96)\r\n\tat org.testng.Assert.failNotEquals(Assert.java:776)\r\n\tat org.testng.Assert.assertEqualsImpl(Assert.java:137)\r\n\tat org.testng.Assert.assertEquals(Assert.java:118)\r\n\tat org.testng.Assert.assertEquals(Assert.java:568)\r\n\tat org.testng.Assert.assertEquals(Assert.java:578)\r\n\tat stepdefs.Stepdefs.raiseExcep(Stepdefs.java:52)\r\n\tat ✽.Raise exception(classpath:stepdefs/scenariosexcep.feature:11)\r\n";
		Throwable t = errorMessageProcessor.createThrowableObject(errorMessage);
		assertEquals("Exception class is wrong.", java.lang.AssertionError.class, t.getClass());
		assertEquals("Exception message is wrong.", "expected [true] but found [false]", t.getMessage());
		assertEquals("Stacktrace element length is wrong", 8, t.getStackTrace().length);
	}
	
	@Test
	public void testThrowableWithoutMessage() {		
		String errorMessage = "java.lang.AssertionError:";
		Throwable t = errorMessageProcessor.createThrowableObject(errorMessage);
		assertEquals("Exception class is wrong.", java.lang.AssertionError.class, t.getClass());
		assertEquals("Exception message is wrong.", null, t.getMessage());
		assertEquals("Stacktrace element length is wrong", 0, t.getStackTrace().length);
	}
	
	@Test
	public void testEmptyErrorMessage() {		
		String errorMessage = "";
		Throwable t = errorMessageProcessor.createThrowableObject(errorMessage);
		assertEquals("Exception class is wrong.", java.lang.Exception.class, t.getClass());
		assertEquals("Exception message is wrong.", "Error message details are missing.", t.getMessage());
		assertEquals("Stacktrace element length is wrong", 0, t.getStackTrace().length);
	}

}
