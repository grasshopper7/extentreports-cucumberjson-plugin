package tech.grasshopper.test;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.observer.ExtentObserver;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.reporters.ReporterInitializer;
import tech.grasshopper.test.heirarchy.DefaultExtentTestHeirarchy;

@Singleton
public class ExtentTestManager {

	private ExtentReports extent;
	private ReporterInitializer reportInitializer;
	private DefaultExtentTestHeirarchy testHeirarchy;

	@Inject
	public ExtentTestManager(ReporterInitializer reportInitializer, DefaultExtentTestHeirarchy testHeirarchy) {
		this.extent = new ExtentReports();
		this.reportInitializer = reportInitializer;
		this.testHeirarchy = testHeirarchy;
	}

	public void initialize(List<Feature> features) {
		Map<String, ExtentObserver<?>> reporters = reportInitializer.getReportKeyToInstance();
		extent.setReportUsesManualConfiguration(true);
		testHeirarchy.createTestHeirarchy(features, extent);
		
		for (String key : reporters.keySet()) {
			ExtentObserver<?> reporter = reporters.get(key);
			extent.attachReporter(reporter);
		}	
	}

	public void flushToReporters() {
		extent.flush();
	}
}
