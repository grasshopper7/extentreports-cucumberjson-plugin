package tech.grasshopper;

import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import tech.grasshopper.json.JsonFileConverter;
import tech.grasshopper.json.JsonPathCollector;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.properties.ReportProperties;
import tech.grasshopper.reporters.ReporterInitializer;
import tech.grasshopper.test.ExtentTestManager;

@Mojo(name = "extentreport")
public class ExtentReportsCucumberPlugin extends AbstractMojo {

	@Parameter(property = "extentreport.cucumberJsonReportDirectory", required = true)
	private String cucumberJsonReportDirectory;

	@Parameter(property = "extentreport.extentPropertiesDirectory", defaultValue = "")
	private String extentPropertiesDirectory;
	
	@Parameter(property = "extentreport.displayAllHooks", defaultValue = "false")
	private String displayAllHooks;

	private JsonPathCollector jsonPathCollector;
	private JsonFileConverter jsonFileConverter;
	private ReportProperties reportProperties;
	private ReporterInitializer reportInitializer;
	private ExtentTestManager extentTestManager;
	private ExtentReportsCucumberLogger logger;

	@Inject
	public ExtentReportsCucumberPlugin(JsonPathCollector jsonPathCollector, JsonFileConverter jsonFileConverter,
			ReportProperties reportProperties, ReporterInitializer reportInitializer,
			ExtentTestManager extentTestManager, ExtentReportsCucumberLogger logger) {
		this.jsonPathCollector = jsonPathCollector;
		this.jsonFileConverter = jsonFileConverter;
		this.reportProperties = reportProperties;
		this.reportInitializer = reportInitializer;
		this.extentTestManager = extentTestManager;
		this.logger = logger;
	}

	public void execute() {
		try {
			logger.initializeLogger(getLog());
			logger.info("STARTED EXTENT REPORT GENERATION PLUGIN");

			reportProperties.setDisplayAllHooks(displayAllHooks);
			reportProperties.loadPropertyFiles(extentPropertiesDirectory);
			
			List<Path> jsonFilePaths = jsonPathCollector.retrieveFilePaths(cucumberJsonReportDirectory);
			List<Feature> features = jsonFileConverter.retrieveFeaturesFromReport(jsonFilePaths);

			reportProperties.loadPropertyFiles(extentPropertiesDirectory);

			reportInitializer.instantiate();

			extentTestManager.initialize(features);
			extentTestManager.flushToReporters();

			logger.info("FINISHED EXTENT REPORT GENERATION PLUGIN");
		} catch (Throwable t) {
			// Report will not result in build failure.
			t.printStackTrace();
			logger.error(String.format("STOPPING EXTENT REPORT GENERATION - %s", t.getMessage()));
		}
	}
}
