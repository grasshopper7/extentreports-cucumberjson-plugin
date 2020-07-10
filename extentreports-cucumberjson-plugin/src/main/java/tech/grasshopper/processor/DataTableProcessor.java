package tech.grasshopper.processor;

import java.util.List;

import javax.inject.Singleton;

import com.aventstack.extentreports.markuputils.MarkupHelper;

@Singleton
public class DataTableProcessor {
	
	public String processTable(List<List<String>> data) {
		if(data.size() < 1)
			return "";	
		String[][] array = new String[data.size()][];
		int i = 0;
		for (List<String> nestedList : data) {
		    array[i++] = nestedList.toArray(new String[nestedList.size()]);
		}
		return MarkupHelper.createTable(array).getMarkup();
	}
}
