package tech.grasshopper.pojo;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Row {

	private List<String> cells = new ArrayList<>();
}
