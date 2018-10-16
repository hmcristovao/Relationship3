package graph;

import java.util.HashMap;
import java.util.Map;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.graph.api.Node;
import org.gephi.statistics.plugin.GraphDistance;

public class NodesTableHash {

	private Map<String,NodeData> table; 
	
	public NodesTableHash() {
		this.table = new HashMap<String, NodeData>();
	}
	
	public NodeData get(String nodeId) {
		return this.table.get(nodeId);
	}
	public void put(String nodeId, NodeData nodeData) {
		this.table.put(nodeId, nodeData);
	}

	private void putBetweenness(String nodeId, double valueBetweenness) {
		this.table.get(nodeId).setBetweenness(valueBetweenness);
	}
	private void putCloseness(String nodeId, double valueCloseness) {
		this.table.get(nodeId).setCloseness(valueCloseness);
	}
	private void putBetweennessCloseness(String nodeId, double valueBetweenness, double valueCloseness) {
		this.table.get(nodeId).setBetweennessCloseness(valueBetweenness, valueCloseness);
	}
	
	public void buildNodesTableHash(GephiGraphData gephiGraphData) {
		Double valueBetweenness, valueCloseness;
		String nodeId;
		// copy betweenness and closeness to table
		AttributeColumn attributeColumnBetweenness = gephiGraphData.getAttributeTable().getColumn(GraphDistance.BETWEENNESS);
		AttributeColumn attributeColumnCloseness   = gephiGraphData.getAttributeTable().getColumn(GraphDistance.CLOSENESS);
		for(Node gephiNode: gephiGraphData.getGephiGraph().getNodes()) {
			nodeId = gephiNode.getNodeData().getId();
			valueBetweenness = (Double)gephiNode.getNodeData().getAttributes().getValue(attributeColumnBetweenness.getIndex());
			valueCloseness   = (Double)gephiNode.getNodeData().getAttributes().getValue(attributeColumnCloseness.getIndex());
			this.putBetweennessCloseness(nodeId, valueBetweenness.doubleValue(),valueCloseness.doubleValue());
		}	
	}
	
	public String toString() {
		return  this.table.toString();
	}
}


