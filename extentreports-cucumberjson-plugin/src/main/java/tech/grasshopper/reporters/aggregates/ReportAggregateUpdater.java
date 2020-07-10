package tech.grasshopper.reporters.aggregates;

import java.util.List;

import com.aventstack.extentreports.ReportAggregates;
import com.aventstack.extentreports.reporter.AbstractReporter;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.reporters.aggregates.DurationCalculator.ReporterDuration;

public class ReportAggregateUpdater extends AbstractReporter {

	public static final String REPORTER_NAME = "dummy";
	private ReporterDuration durationData;
	private List<Feature> features;

	public ReportAggregateUpdater(List<Feature> features) {
		this.features = features;
	}

	@Override
	public synchronized void flush(ReportAggregates reportAggregates) {		
		DurationCalculator timestampCalculator = new DurationCalculator(features, reportAggregates.getTestList());

		durationData = timestampCalculator.calculateReportDuration();
		reportAggregates.setStartTime(durationData.getStartTime());
		reportAggregates.setEndTime(durationData.getEndTime());

		timestampCalculator.updateExtentTestTimeData();
	}

	@Override
	public String getReporterName() {
		return REPORTER_NAME;
	}

	@Override
	public void start() {
		
	}
}
