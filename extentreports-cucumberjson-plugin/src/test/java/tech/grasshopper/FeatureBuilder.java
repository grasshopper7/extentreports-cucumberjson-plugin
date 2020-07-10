package tech.grasshopper;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import tech.grasshopper.pojo.Feature;
import tech.grasshopper.pojo.Hook;
import tech.grasshopper.pojo.Match;
import tech.grasshopper.pojo.Result;
import tech.grasshopper.pojo.Scenario;
import tech.grasshopper.pojo.Step;
import tech.grasshopper.pojo.Tag;

public class FeatureBuilder {

	private Feature feature;
	private Scenario scenario;
	private Step step;
	private Hook hook;

	public FeatureBuilder() {
		feature = spy(Feature.class);
	}
	
	public FeatureBuilder setUri(String uri) {
		when(feature.getUri()).thenReturn(uri);
		return this;
	}
	
	public FeatureBuilder setName(String name) {
		when(feature.getName()).thenReturn(name);
		return this;
	}
	
	public FeatureBuilder setDescription(String description) {
		when(feature.getDescription()).thenReturn(description);
		return this;
	}
	
	public FeatureBuilder setStartTime(String startTime) {
		when(feature.getStartTime()).thenReturn(Date.from(ZonedDateTime.parse(startTime).toInstant()));
		return this;
	}
	
	public FeatureBuilder setEndTime(String endTime) {
		when(feature.getEndTime()).thenReturn(Date.from(ZonedDateTime.parse(endTime).toInstant()));
		return this;
	}
	
	public FeatureBuilder addFeatureTag(String tagName) {
		Tag tag = mock(Tag.class);
		when(tag.getName()).thenReturn(tagName);
		feature.getTags().add(tag);
		return this;
	}

	public FeatureBuilder addScenario() {
		scenario = spy(Scenario.class);
		feature.getElements().add(scenario);
		return this;
	}
	
	public FeatureBuilder setScenarioName(String name) {
		when(scenario.getName()).thenReturn(name);
		return this;
	}
	
	public FeatureBuilder setScenarioDescription(String description) {
		when(scenario.getDescription()).thenReturn(description);
		return this;
	}
	
	public FeatureBuilder setScenarioKeyword(String keyword) {
		when(scenario.getKeyword()).thenReturn(keyword);
		return this;
	}

	public FeatureBuilder setScenarioStartTimestamp(String timestamp) {
		when(scenario.getStartTimestamp()).thenReturn(timestamp);
		return this;
	}
	
	public FeatureBuilder setScenarioId(String id) {
		when(scenario.getId()).thenReturn(id);
		return this;
	}
	
	public FeatureBuilder setScenarioUriStepLines(String uriLines) {
		doReturn(uriLines).when(scenario).getUriStepLines();
		return this;
	}
	
	public FeatureBuilder addScenarioTag(String tagName) {
		Tag tag = mock(Tag.class);
		when(tag.getName()).thenReturn(tagName);
		scenario.getTags().add(tag);
		return this;
	}

	public FeatureBuilder addScenarioBeforeHook() {
		addHook();
		scenario.getBefore().add(hook);
		return this;
	}

	public FeatureBuilder addScenarioAfterHook() {
		addHook();
		scenario.getAfter().add(hook);
		return this;
	}
	
	private void addHook() {
		hook = spy(Hook.class);
		Result result = mock(Result.class);
		when(hook.getResult()).thenReturn(result);
		Match match = mock(Match.class);
		when(hook.getMatch()).thenReturn(match);
	}
	
	public FeatureBuilder setHookDuration(Long duration) {	
		Result result = hook.getResult();
		when(result.getDuration()).thenReturn(duration);
		return this;
	}
	
	public FeatureBuilder setHookResult(String result) {
		Result res = hook.getResult();
		when(res.getStatus()).thenReturn(result);
		when(hook.getResult()).thenReturn(res);
		return this;
	}
	
	public FeatureBuilder setHookLocation(String location) {
		Match match = hook.getMatch();
		when(match.getLocation()).thenReturn(location);
		return this;
	}

	public FeatureBuilder addStep() {
		step = spy(Step.class);
		scenario.getSteps().add(step);
		Result result = mock(Result.class);
		when(step.getResult()).thenReturn(result);
		Match match = mock(Match.class);
		when(step.getMatch()).thenReturn(match);
		return this;
	}
	
	public FeatureBuilder setStepKeyword(String keyword) {	
		when(step.getKeyword()).thenReturn(keyword);
		return this;
	}
	
	public FeatureBuilder setStepText(String text) {	
		when(step.getName()).thenReturn(text);
		return this;
	}
	
	public FeatureBuilder setStepDuration(Long duration) {	
		Result result = step.getResult();
		when(result.getDuration()).thenReturn(duration);
		return this;
	}
	
	public FeatureBuilder setStepOutput(List<String> output) {	
		when(step.getOutput()).thenReturn(output);
		return this;
	}
	
	public FeatureBuilder setStepResult(String result) {
		Result res = step.getResult();
		when(res.getStatus()).thenReturn(result);
		when(step.getResult()).thenReturn(res);
		return this;
	}
	
	public FeatureBuilder setStepErrorMessage(String message) {
		Result res = step.getResult();
		when(res.getErrorMessage()).thenReturn(message);
		return this;
	}
	
	public FeatureBuilder setStepLocation(String location) {
		Match match = step.getMatch();
		when(match.getLocation()).thenReturn(location);
		return this;
	}

	public FeatureBuilder addStepBeforeHook() {
		addHook();
		step.getBefore().add(hook);
		return this;
	}
	
	public FeatureBuilder addStepAfterHook() {
		addHook();
		step.getAfter().add(hook);
		return this;
	}

	public Feature build() {
		return feature;
	}
}
