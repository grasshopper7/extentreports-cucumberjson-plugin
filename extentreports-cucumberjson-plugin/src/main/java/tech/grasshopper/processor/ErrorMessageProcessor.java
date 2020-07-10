package tech.grasshopper.processor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

import tech.grasshopper.logging.ExtentReportsCucumberLogger;

@Singleton
public class ErrorMessageProcessor {

	private ExtentReportsCucumberLogger logger;
	
	@Inject
	public ErrorMessageProcessor(ExtentReportsCucumberLogger logger) {
		this.logger = logger;
	}

	public Throwable createThrowableObject(String errorMessage) {

		Pattern pattern = Pattern.compile("([\\w\\.]+)(:\\s?(.*))?");
		Matcher matcher = pattern.matcher(errorMessage);
		String className = "";
		String parameter = "";
		Throwable throwableInstance = null;
		
		if(errorMessage.isEmpty() || errorMessage == null) {
			logger.error("Exception message is empty or null.");
			throwableInstance = new Exception("Error message details are missing.");
			throwableInstance.setStackTrace(new StackTraceElement[0]);
			return throwableInstance;
		}

		if (matcher.find()) {
			className = matcher.group(1);
			if (matcher.group(3) != null)
				parameter = matcher.group(3);
			throwableInstance = createThrowableInstance(className, parameter);
		} else {
			logger.error("Exception message not parseable, creating generic exception instance.");
			throwableInstance = new Exception(errorMessage);
		}
		createStackTrace(throwableInstance, errorMessage);
		return throwableInstance;
	}

	private Throwable createThrowableInstance(String className, String parameter) {
		Throwable throwableInstance = null;
		Class<?> throwableClass = null;
		try {
			throwableClass = Class.forName(className);
		} catch (ClassNotFoundException e) {
			logger.warn(
					"Exception class not found with name - " + className + ". Creating a generic exception instance.");
			if (parameter.isEmpty())
				throwableInstance = new Exception();
			else
				throwableInstance = new Exception(parameter);
			return throwableInstance;
		}

		if (parameter.isEmpty())
			throwableInstance = createThrowableInstanceWithoutMessage(className, throwableClass);
		else
			throwableInstance = createThrowableInstanceWithMessage(className, parameter, throwableClass);

		return throwableInstance;
	}

	private Throwable createThrowableInstanceWithoutMessage(String className, Class<?> throwableClass) {
		Constructor<?> throwableConstructor = null;
		Throwable throwableInstance = null;
		try {
			throwableConstructor = throwableClass.getConstructor();
			throwableInstance = (Throwable) throwableConstructor.newInstance();
		} catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
			logger.warn("Error in instantiating exception class with name - " + className
					+ ". Creating a generic exception instance.");
			throwableInstance = new Exception("Generic Exception");
		}
		return throwableInstance;
	}

	private Throwable createThrowableInstanceWithMessage(String className, String message, Class<?> throwableClass) {
		Constructor<?> throwableConstructor = null;
		Throwable throwableInstance = null;
		try {
			throwableConstructor = throwableClass.getConstructor(Object.class);
			throwableInstance = (Throwable) throwableConstructor.newInstance(message);
		} catch (ReflectiveOperationException | SecurityException | IllegalArgumentException e) {
			logger.warn("Error in instantiating exception class with name - " + className + " and message - '"
					+ message + "'. Creating a generic exception instance.");
			throwableInstance = new Exception("Generic Exception " + message);
		}
		return throwableInstance;
	}

	public void createStackTrace(Throwable throwable, String errorMessage) {

		Pattern pattern = Pattern.compile("\\s*at\\s+(.*)[(](.*)[)](\\n|\\r\\n)");
		Matcher matcher = pattern.matcher(errorMessage);
		List<StackTraceElement> stackTrace = new ArrayList<StackTraceElement>();

		String qualClsName = "", methodName = "", fileName = "", lineNumber = "";
		String clsMthDetails = "", fileLineDetails = "";

		while (matcher.find()) {
			clsMthDetails = matcher.group(1);
			int lastDot = clsMthDetails.lastIndexOf(".");
			if (lastDot != -1) {
				qualClsName = clsMthDetails.substring(0, lastDot);
				methodName = clsMthDetails.substring(lastDot + 1);
			}
			fileLineDetails = matcher.group(2);
			int lastColon = fileLineDetails.lastIndexOf(":");
			if (lastColon != -1) {
				fileName = fileLineDetails.substring(0, lastColon);
				lineNumber = fileLineDetails.substring(lastColon + 1);
			}
			stackTrace.add(new StackTraceElement(qualClsName, methodName, fileName, Integer.parseInt(lineNumber)));
		}
		throwable.setStackTrace(stackTrace.toArray(new StackTraceElement[0]));
	}
}
