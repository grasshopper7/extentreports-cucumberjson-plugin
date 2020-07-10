package tech.grasshopper.json;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import tech.grasshopper.exception.ExtentReportsCucumberPluginException;

@Singleton
public class JsonPathCollector {

	public List<Path> retrieveFilePaths(String jsonDirectory) {
		List<Path> jsonFilePaths = null;
		try {
			jsonFilePaths = Files.walk(Paths.get(jsonDirectory)).filter(Files::isRegularFile)
					.filter(p -> p.toString().toLowerCase().endsWith(".json")).collect(Collectors.toList());
		} catch (IOException e) {
			throw new ExtentReportsCucumberPluginException(
					"Unable to navigate Cucumber Json report folders. Stopping report creation. "
							+ "Check the 'extentreport.cucumberJsonReportDirectory' plugin configuration.");
		}
		if (jsonFilePaths == null || jsonFilePaths.size() == 0)
			throw new ExtentReportsCucumberPluginException("No Cucumber Json Report found. Stopping report creation. "
					+ "Check the 'extentreport.cucumberJsonReportDirectory' plugin configuration.");
		return jsonFilePaths;
	}
}
