package tech.grasshopper.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.aventstack.extentreports.ExtentTest;

import net.bytebuddy.utility.RandomString;
import net.iharder.Base64;
import tech.grasshopper.logging.ExtentReportsCucumberLogger;
import tech.grasshopper.pojo.Embedded;
import tech.grasshopper.properties.ReportProperties;

public class EmbeddedProcessorTest {

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	private EmbeddedProcessor embeddedProcessor;
	private ReportProperties reportProperties;
	private ExtentReportsCucumberLogger logger;
	private List<Embedded> embeddings;
	private ExtentTest test;

	@Before
	public void setup() {
		reportProperties = mock(ReportProperties.class);
		logger = mock(ExtentReportsCucumberLogger.class);
		embeddings = new ArrayList<>();
		test = mock(ExtentTest.class, org.mockito.Answers.RETURNS_DEEP_STUBS);
		when(test.getModel().getName()).thenReturn("TEST STEP NAME");

		embeddedProcessor = new EmbeddedProcessor(reportProperties, logger);
	}

	@Test
	public void testAddAttachmentsToExtentTest() {
		Embedded embed = mock(Embedded.class);
		when(embed.getFilePath()).thenReturn(testFolder.getRoot().getAbsolutePath() + "/image.png");
		embeddings.add(embed);

		embeddedProcessor.updateExtentTestWithEmbedding(test, embeddings);
	}

	@Test
	public void testAddAttachmentsToExtentTestInvalidAttachmentFilePath() {
		Embedded embed = mock(Embedded.class);
		embeddings.add(embed);

		embeddedProcessor.updateExtentTestWithEmbedding(test, embeddings);
		verify(logger, times(1))
				.warn("Skipping adding embedded file as filepath is empty for step - 'TEST STEP NAME'.");
	}

	@Test
	public void testAddAttachmentsToExtentTestAttachmentException() {
		Embedded embed = mock(Embedded.class);
		when(embed.getFilePath()).thenReturn(testFolder.getRoot().getAbsolutePath() + "/image.png");
		embeddings.add(embed);

		try {
			doThrow(new IOException("failure")).when(test).addScreenCaptureFromPath(anyString());
		} catch (IOException e) {
			fail("Error in creating mock to throw IOException");
		}

		embeddedProcessor.updateExtentTestWithEmbedding(test, embeddings);
		verify(logger, times(1))
				.warn("Skipping adding embedded file for step - 'TEST STEP NAME' as error in processing.");
	}

	@Test
	public void testProcessAttachements() throws IOException {
		String tempFolder = testFolder.getRoot().getAbsolutePath() + "/" + (new RandomString(10)).nextString();
		when(reportProperties.getReportScreenshotLocation()).thenReturn(tempFolder);

		Embedded embed = new Embedded();
		embed.setData(Base64.encodeFromFile("src/test/resources/tech/grasshopper/processor/image.png"));
		embed.setMimeType("image/png");
		/*
		 * List<Embedded> embeddings = new ArrayList<>(); embeddings.add(embed);
		 * embeddedProcessor.processEmbeddings(embeddings);
		 */
		embeddedProcessor.processEmbedding(embed);

		assertEquals("Embedded object 'data' variable value should be empty.", "", embed.getData());
		File copiedFile = new File(embed.getFilePath());
		assertTrue("Attachment file not created in location.", copiedFile.exists());
	}

	@Test
	public void testInvalidFileExtension() {
		Embedded embed = new Embedded();
		embed.setMimeType("invalid/gibberish");
		/*
		 * List<Embedded> embeddings = new ArrayList<>(); embeddings.add(embed);
		 * embeddedProcessor.processEmbeddings(embeddings);
		 */
		embeddedProcessor.processEmbedding(embed);

		verify(logger, times(1))
				.warn("Mime type '" + embed.getMimeType() + "' not supported.");
	}
}
