package tech.grasshopper.exception;

public class ExtentReportsCucumberPluginException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExtentReportsCucumberPluginException(String message) {
		super(message);
	}

	public ExtentReportsCucumberPluginException(String message, Exception exception) {
		super(message, exception);
	}
}
