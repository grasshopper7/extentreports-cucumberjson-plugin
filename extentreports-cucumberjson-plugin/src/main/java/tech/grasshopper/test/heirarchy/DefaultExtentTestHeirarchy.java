package tech.grasshopper.test.heirarchy;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.processor.EmbeddedProcessor;
import tech.grasshopper.processor.FeatureProcessor;
import tech.grasshopper.processor.ScenarioProcessor;
import tech.grasshopper.processor.StepProcessor;
import tech.grasshopper.properties.ReportProperties;

@Singleton
public class DefaultExtentTestHeirarchy extends ExtentTestHeirarchy {

	@Inject
	public DefaultExtentTestHeirarchy(FeatureProcessor featureProcessor, ScenarioProcessor scenarioProcessor,
			StepProcessor stepProcessor, EmbeddedProcessor embeddedProcessor, ReportProperties reportProperties) {
		super(featureProcessor, scenarioProcessor, stepProcessor, embeddedProcessor, reportProperties);
	}

	DefaultExtentTestHeirarchy(FeatureProcessor featureProcessor, ScenarioProcessor scenarioProcessor,
			StepProcessor stepProcessor, EmbeddedProcessor embeddedProcessor, ExtentReports extent,
			ReportProperties reportProperties) {
		super(featureProcessor, scenarioProcessor, stepProcessor, embeddedProcessor, extent, reportProperties);
	}

	@Override
	public void createTestHeirarchy(List<Feature> features, ExtentReports extent) {
		this.features = features;
		this.extent = extent;
		features.forEach(feature -> {
			featureProcessor.process(feature);
			ExtentTest featureTest = createFeatureExtentTest(feature);

			feature.getElements().forEach(scenario -> {
				scenarioProcessor.process(scenario, feature);
				ExtentTest scenarioTest = createScenarioExtentNode(featureTest, scenario);

				createBeforeHookExtentNodes(scenarioTest, scenario);
				scenario.getSteps().forEach(step -> {
					stepProcessor.process(step);

					createBeforeStepHookExtentNodes(scenarioTest, step);
					createStepExtentNode(scenarioTest, step);
					createAfterStepHookExtentNodes(scenarioTest, step);
				});
				createAfterHookExtentNodes(scenarioTest, scenario);
			});
		});
	}
}
