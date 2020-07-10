package tech.grasshopper.processor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class DataTableProcessorTest {

	private DataTableProcessor dataTableProcessor;

	@Before
	public void setup() {
		dataTableProcessor = new DataTableProcessor();
	}

	@Test
	public void testDataTableMarkup() {
		List<List<String>> data = new ArrayList<>();
		String[] header = { "First Column", "Second Column", "Third Column" };
		String[] firstRow = { "11", "12", "13" };
		String[] secondRow = { "21", "22", "23" };

		data.add(0, Arrays.asList(header));
		data.add(1, Arrays.asList(firstRow));
		data.add(2, Arrays.asList(secondRow));

		assertEquals("DataTable markup does not match.",
				"<table class='runtime-table table-striped table'><tr><td>First Column</td><td>Second Column</td><td>Third Column</td></tr>"
						+ "<tr><td>11</td><td>12</td><td>13</td></tr><tr><td>21</td><td>22</td><td>23</td></tr></table>",
				dataTableProcessor.processTable(data));
	}
}
