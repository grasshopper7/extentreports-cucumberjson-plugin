package tech.grasshopper.processor;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import tech.grasshopper.pojo.Step;

@Singleton
public class StepProcessor {
	
	private DataTableProcessor dataTableProcessor;
	
	@Inject
	public StepProcessor(DataTableProcessor dataTableProcessor) {
		this.dataTableProcessor = dataTableProcessor;
	}

	public void process(Step step) {
		updateDataTableMarkup(step);
		updateDocString(step);
	}
	
	protected void updateDataTableMarkup(Step step) {
		List<List<String>> cells = step.getRows().stream().map(r -> r.getCells()).collect(Collectors.toList());		
		if(cells.size() < 1)
			return;	
		
		step.setDataTableMarkup(dataTableProcessor.processTable(cells));
	}
	
	protected void updateDocString(Step step) {
		if(step.getDocString().getValue() == null || step.getDocString().getValue().isEmpty())
			return;
		else
			step.setDocStringMarkup(step.getDocString().getValue().replaceAll("(\r\n|\n)", "<br />"));
	}
}
