package tech.grasshopper.reporters;

import com.aventstack.extentreports.observer.ExtentObserver;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.JsonFormatter;

import tech.grasshopper.pdf.extent.ExtentPDFCucumberReporter;
import tech.grasshopper.properties.ReportProperties;

public abstract class ReporterAdapter {

	protected String id;
	protected ReportProperties reportProperties;

	public ReporterAdapter(String id, ReportProperties reportProperties) {
		this.id = id;
		this.reportProperties = reportProperties;
	}

	public abstract ExtentObserver<?> createReporter();

	public static class SparkReportAdapter extends ReporterAdapter {

		public SparkReportAdapter(String id, ReportProperties reportProperties) {
			super(id, reportProperties);
		}

		@Override
		public ExtentObserver<?> createReporter() {
			return new ExtentSparkReporter(reportProperties.getReportOutProperty(id));
		}
	}

	public static class JsonReportAdapter extends ReporterAdapter {

		public JsonReportAdapter(String id, ReportProperties reportProperties) {
			super(id, reportProperties);
		}

		@Override
		public ExtentObserver<?> createReporter() {
			return new JsonFormatter(reportProperties.getReportOutProperty(id));
		}
	}

	public static class PDFReportAdapter extends ReporterAdapter {

		public PDFReportAdapter(String id, ReportProperties reportProperties) {
			super(id, reportProperties);
		}

		@Override
		public ExtentObserver<?> createReporter() {
			return new ExtentPDFCucumberReporter(reportProperties.getReportOutProperty(id),
					reportProperties.getReportScreenshotLocation());
		}
	}
}
