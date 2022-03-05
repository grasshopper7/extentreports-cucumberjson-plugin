package tech.grasshopper.test;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.observer.ExtentObserver;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.properties.ReportProperties;
import tech.grasshopper.reporters.ReporterInitializer;
import tech.grasshopper.tests.ExtentTestHeirarchy;

@Singleton
public class ExtentTestManager {

	private ExtentReports extent;
	private ReporterInitializer reportInitializer;
	private ReportProperties reportProperties;

	@Inject
	public ExtentTestManager(ReporterInitializer reportInitializer, ReportProperties reportProperties) {
		this.extent = new ExtentReports();
		this.reportInitializer = reportInitializer;
		this.reportProperties = reportProperties;
	}

	public void initialize(List<Feature> features) {
		Map<String, ExtentObserver<?>> reporters = reportInitializer.getReportKeyToInstance();
		extent.setReportUsesManualConfiguration(true);

		ExtentTestHeirarchy.builder().extent(extent).features(features)
				.displayAllHooks(reportProperties.isDisplayAllHooks())
				.strictCucumber6Behavior(reportProperties.isStrictCucumber6Behavior()).build().createTestHeirarchy();

		for (String key : reporters.keySet()) {
			ExtentObserver<?> reporter = reporters.get(key);
			extent.attachReporter(reporter);
		}
	}

	public void flushToReporters() {
		extent.flush();
	}
}
