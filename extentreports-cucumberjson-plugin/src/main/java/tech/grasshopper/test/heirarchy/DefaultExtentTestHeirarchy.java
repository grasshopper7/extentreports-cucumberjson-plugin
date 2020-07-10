package tech.grasshopper.test.heirarchy;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.processor.EmbeddedProcessor;
import tech.grasshopper.processor.ErrorMessageProcessor;
import tech.grasshopper.processor.FeatureProcessor;
import tech.grasshopper.processor.ScenarioProcessor;
import tech.grasshopper.processor.StepProcessor;

@Singleton
public class DefaultExtentTestHeirarchy extends ExtentTestHeirarchy {
	
	@Inject
	public DefaultExtentTestHeirarchy(FeatureProcessor featureProcessor, ScenarioProcessor scenarioProcessor,
			StepProcessor stepProcessor, ErrorMessageProcessor errorMessageProcessor,
			EmbeddedProcessor embeddedProcessor) {
		super(featureProcessor, scenarioProcessor, stepProcessor, errorMessageProcessor, embeddedProcessor);
	}
	
	DefaultExtentTestHeirarchy(FeatureProcessor featureProcessor, ScenarioProcessor scenarioProcessor,
			StepProcessor stepProcessor, ErrorMessageProcessor errorMessageProcessor,
			EmbeddedProcessor embeddedProcessor, ExtentReports extent) {
		super(featureProcessor, scenarioProcessor, stepProcessor, errorMessageProcessor, embeddedProcessor, extent);
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
