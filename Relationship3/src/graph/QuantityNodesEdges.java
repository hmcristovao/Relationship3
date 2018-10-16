package graph;

public class QuantityNodesEdges {
	private int numNodes;
	private int numEdges;
	
	public QuantityNodesEdges(int numNodes, int numEdges) {
		this.numNodes      = numNodes;
		this.numEdges      = numEdges;
	}
	public QuantityNodesEdges() {
		this(0,0);
	}
	public int getNumNodes() {
		return this.numNodes;
	}
	public int getNumEdges() {
		return this.numNodes;
	}
	public void incNumNodes() {
		this.numNodes++;
	}
	public void incNumNodes(int value) {
		this.numNodes+=value;
	}
	public void incNumEdges() {
		this.numEdges++;
	}
	public void incNumEdges(int value) {
		this.numEdges+=value;
	}
	public void reset() {
		this.numNodes      = 0;
		this.numEdges      = 0;
	}
	public String toString() {
		return this.numNodes + " nodes, " + this.numEdges + " edges";
	}
	
}
