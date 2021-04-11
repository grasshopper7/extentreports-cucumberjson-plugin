package tech.grasshopper.reporters;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.observer.ExtentObserver;
import com.aventstack.extentreports.reporter.ReporterConfigurable;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.properties.ReportProperties;

@Singleton
public class ReporterInitializer {

	private Map<String, ExtentObserver<?>> reportKeyToInstance;
	private ReportProperties reportProperties;

	private ExtentReportsCucumberLogger logger;

	@Inject
	public ReporterInitializer(ReportProperties reportProperties, ExtentReportsCucumberLogger logger) {
		this.reportKeyToInstance = new HashMap<>();
		this.reportProperties = reportProperties;
		this.logger = logger;
	}

	public void instantiate() {
		reportProperties.retrieveReportIdToClassNameMappings().forEach((k, v) -> {
			if (reportProperties.checkReportRequired(k)) {
				ExtentObserver<?> reportInstance = instantiateReporter(k, v);
				if (reportInstance == null)
					return;
				loadReporterUISettings(reportInstance, reportProperties.getReportConfigProperty(k));
				reportKeyToInstance.put(k, (ExtentObserver<?>) reportInstance);
			}
		});
		if (reportKeyToInstance.size() == 0)
			throw new ExtentReportsCucumberPluginException(
					"Skipping reports generation as no report 'start' value set to 'true' in extent properties file.");
	}

	public Map<String, ExtentObserver<?>> getReportKeyToInstance() {
		return reportKeyToInstance;
	}

	private ExtentObserver<?> instantiateReporter(String key, String clsName) {
		ExtentObserver<?> reportInstance = null;
		try {
			Class<?> reportClass = Class.forName(clsName);
			Constructor<?> reportConstructor = reportClass.getConstructor(String.class, ReportProperties.class);
			ReporterAdapter reportAdapter = (ReporterAdapter) reportConstructor.newInstance(key, reportProperties);
			reportInstance = reportAdapter.createReporter();
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException
				| IllegalAccessException | InstantiationException e) {
			logger.warn("Skipping report of class - " + clsName
					+ ", as unable to instantiate reporter class. Check the value in extent properties.");
		} catch (ClassCastException e) {
			logger.warn("Skipping report of class - " + clsName
					+ ", as unable to cast to 'com.aventstack.extentreports.observer.ExtentObserver' class.");
		} catch (Exception e) {
			logger.warn(
					"Skipping report of class - " + clsName + " due to following exception - " + e.getMessage() + ".");
		}
		return reportInstance;
	}

	private void loadReporterUISettings(ExtentObserver<?> reportInstance, String reportConfigPath) {
		if (!reportConfigPath.isEmpty())
			try {
				((ReporterConfigurable) reportInstance).loadXMLConfig(reportConfigPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
