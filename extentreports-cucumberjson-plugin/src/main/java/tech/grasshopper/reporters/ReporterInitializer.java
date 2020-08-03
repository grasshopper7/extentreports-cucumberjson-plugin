package tech.grasshopper.reporters;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.reporter.ConfigurableReporter;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.properties.ReportProperties;
import tech.grasshopper.reporters.aggregates.ReportAggregateUpdater;

@Singleton
public class ReporterInitializer {

	private Map<String, ConfigurableReporter> reportKeyToInstance;
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
				ConfigurableReporter reportInstance = instantiateReporter(v, reportProperties.getReportOutProperty(k));
				if (reportInstance == null)
					return;
				loadReporterUISettings(reportInstance, reportProperties.getReportConfigProperty(k));
				reportKeyToInstance.put(k, reportInstance);
			}
		});
		if (reportKeyToInstance.size() == 0)
			throw new ExtentReportsCucumberPluginException(
					"Skipping reports generation as no report 'start' value set to 'true' in extent properties file.");		
	}

	public Map<String, ConfigurableReporter> getReportKeyToInstance() {
		return reportKeyToInstance;
	}

	private ConfigurableReporter instantiateReporter(String clsName, String reportOutputFolder) {
		ConfigurableReporter reportInstance = null;
		try {
			Class<?> reportClass = Class.forName(clsName);
			Constructor<?> reportConstructor = reportClass.getConstructor(String.class);
			reportInstance = (ConfigurableReporter) reportConstructor.newInstance(reportOutputFolder);
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InvocationTargetException
				| IllegalAccessException | InstantiationException e) {
			logger.warn("Skipping report of class - " + clsName
					+ ", as unable to instantiate reporter class. Check the value in extent properties.");
		} catch (ClassCastException e) {
			logger.warn("Skipping report of class - " + clsName
					+ ", as unable to cast to 'com.aventstack.extentreports.reporter.ConfigurableReporter' class.");
		} catch (Exception e) {
			logger.warn(
					"Skipping report of class - " + clsName + " due to following exception - " + e.getMessage() + ".");
		}
		return reportInstance;
	}

	private void loadReporterUISettings(ConfigurableReporter reportInstance, String reportConfigPath) {
		if (!reportConfigPath.isEmpty())
			reportInstance.loadXMLConfig(reportConfigPath);
	}
	
	public ReportAggregateUpdater instantiatReportAggregateUpdater(List<Feature> features) {
		return new ReportAggregateUpdater(features);
	}
}
