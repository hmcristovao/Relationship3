package gui;

import org.apache.commons.collections15.Transformer;

public class EdgeType {
	private final String edgeTitle;

	EdgeType(String edgeTitle) {
		this.edgeTitle = edgeTitle;
	}

	@Override
	public String toString() {
		return edgeTitle;
	}
}

class EdgeLabelTransformer implements Transformer<EdgeType, String> {
	@Override
	public String transform(EdgeType edge) {
		return edge.toString();
	}
}
