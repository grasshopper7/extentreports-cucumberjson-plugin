package tech.grasshopper.logging;

import javax.inject.Singleton;

import org.apache.maven.plugin.logging.Log;

@Singleton
public class ExtentReportsCucumberLogger {

	private Log logger;

	public void initializeLogger(Log mojoLogger) {
		this.logger = mojoLogger;
	}
	
	public void debug(CharSequence seq) {
		logger.debug(seq);
	}
	
	public void error(CharSequence seq) {
		logger.error(seq);
	}
	
	public void info(CharSequence seq) {
		logger.info(seq);
	}
	
	public void warn(CharSequence seq) {
		logger.warn(seq);
	}
}
