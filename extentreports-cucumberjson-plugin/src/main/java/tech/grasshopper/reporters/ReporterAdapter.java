package tech.grasshopper.reporters;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.observer.ExtentObserver;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.JsonFormatter;
import com.aventstack.extentreports.reporter.ReporterFilterable;

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
			ExtentSparkReporter spark = new ExtentSparkReporter(reportProperties.getReportOutProperty(id));
			filterReportStatus(spark);
			return spark;
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
			ExtentPDFCucumberReporter pdf = new ExtentPDFCucumberReporter(reportProperties.getReportOutProperty(id),
					reportProperties.getReportScreenshotLocation());
			filterReportStatus(pdf);
			return pdf;
		}
	}

	public static class HtmlReportAdapter extends ReporterAdapter {

		public HtmlReportAdapter(String id, ReportProperties reportProperties) {
			super(id, reportProperties);
		}

		@Override
		public ExtentObserver<?> createReporter() {
			ExtentHtmlReporter html = new ExtentHtmlReporter(reportProperties.getReportOutProperty(id));
			filterReportStatus(html);
			return html;
		}
	}

	public void filterReportStatus(ReporterFilterable<?> reporter) {
		try {
			String statusFilter = reportProperties.getStatusFilter();
			if (statusFilter == null || statusFilter.isEmpty())
				return;

			List<Status> statuses = Arrays.stream(statusFilter.split(",")).map(s -> convertToStatus(s))
					.collect(Collectors.toList());
			reporter.filter().statusFilter().as(statuses);
		} catch (Exception e) {
			// Do nothing. Uses no filter.
		}
	}

	private static Status convertToStatus(String status) {
		String lowerStatus = status.toLowerCase();

		switch (lowerStatus) {
		case "info":
			return Status.INFO;
		case "pass":
			return Status.PASS;
		case "Warning":
			return Status.WARNING;
		case "skip":
			return Status.SKIP;
		case "fail":
			return Status.FAIL;
		default:
			throw new IllegalArgumentException();
		}
	}
}
