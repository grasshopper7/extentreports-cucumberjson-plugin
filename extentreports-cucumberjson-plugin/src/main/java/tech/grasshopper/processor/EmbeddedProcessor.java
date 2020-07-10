package tech.grasshopper.processor;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;

import net.iharder.Base64;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Embedded;
import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;
import tech.grasshopper.properties.ReportProperties;

@Singleton
public class EmbeddedProcessor {

	private static final AtomicInteger EMBEDDED_INT = new AtomicInteger(0);

	@SuppressWarnings("serial")
	private static final Map<String, String> MIME_TYPES_EXTENSIONS = new HashMap<String, String>() {
		{
			put("image/bmp", "bmp");
			put("image/gif", "gif");
			put("image/jpeg", "jpg");
			put("image/png", "png");
			put("image/svg+xml", "svg");
			put("video/ogg", "ogg");
		}
	};
	
	private ReportProperties reportProperties;	
	private ExtentReportsCucumberLogger logger;
	
	@Inject
	public EmbeddedProcessor(ReportProperties reportProperties, ExtentReportsCucumberLogger logger) {
		this.reportProperties = reportProperties;
		this.logger = logger;
	}

	public void updateExtentTestWithEmbedding(ExtentTest test, List<Embedded> embeddings) {
		for (Embedded embed : embeddings) {
			String name = embed.getName() == null ? "" : embed.getName();
			String filePath = embed.getFilePath();

			if(filePath == null || filePath.isEmpty()) {
				logger.warn("Skipping adding embedded file as filepath is empty for step - '" + test.getModel().getName() + "'.");
				return;
			}
			try {
				test.info(name, MediaEntityBuilder.createScreenCaptureFromPath(filePath).build());
				// Embedding workaround for html report.
				test.addScreenCaptureFromPath(filePath);
			} catch (Exception e) {
				logger.warn("Skipping adding embedded file for step - '" + test.getModel().getName() + "' as error in processing.");
				return;
			}
		}
	}

	public void processFeatures(List<Feature> features) {
		List<Embedded> embeddings = collectAllEmbeddings(features);
		processEmbeddings(embeddings);
	}
	
	public void processEmbeddings(List<Embedded> embeddings) {
		for (Embedded embed : embeddings) {
			String mimeType = embed.getMimeType();
			String extension = MIME_TYPES_EXTENSIONS.get(mimeType);

			if (extension != null) {
				Path path = createEmbeddedFileStructure(extension);
				try {
					Files.write(path, Base64.decode(embed.getData()));
				} catch (IOException e) {
					logger.warn("Skipping embedded file creation at location - " + path.toString() + ", due to error in creating file.");
					continue;
				}
				// No need anymore
				embed.setData("");
				embed.setFilePath(path.toString());
			} else {
				logger.warn("Mime type '" + mimeType + "' not supported.");
			}
		}
	}

	private List<Embedded> collectAllEmbeddings(List<Feature> features) {
		List<Embedded> embeddings = new ArrayList<>();
		List<Scenario> scenarios = features.stream().flatMap(f -> f.getElements().stream())
				.collect(toList());
		List<Step> steps = scenarios.stream().flatMap(s -> s.getSteps().stream()).collect(toList());

		// scenarioHookEmbeds
		embeddings.addAll(scenarios.stream().flatMap(s -> s.getBeforeAfterHooks().stream())
				.flatMap(h -> h.getEmbeddings().stream()).collect(toList()));
		
		// stepEmbeds
		embeddings.addAll(steps.stream().flatMap(s -> s.getEmbeddings().stream()).collect(toList()));
		
		// stepHookEmbeds
		embeddings.addAll(steps.stream().flatMap(s -> s.getBeforeAfterHooks().stream())
				.flatMap(h -> h.getEmbeddings().stream()).collect(toList()));
		return embeddings;
	}
	
	private Path createEmbeddedFileStructure(String extension) {
		StringBuilder fileName = new StringBuilder("embedded").append(EMBEDDED_INT.incrementAndGet())
				.append(".").append(extension);
		String embedDirPath = reportProperties.getReportScreenshotLocation();

		File dir = new File(embedDirPath);
		//Create directory if not existing
	    if (!dir.exists()) 
	    	dir.mkdirs();
	    
		File file = new File(embedDirPath + "/" + fileName);
		Path path = Paths.get(file.getAbsolutePath());
		//Delete existing embedded stuff
		if(file.exists())
			file.delete();
		return path;
	}
}
