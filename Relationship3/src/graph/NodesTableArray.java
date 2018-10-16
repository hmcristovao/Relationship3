package graph;

import java.util.Arrays;
import java.util.Comparator;

import user.Concept;
import user.Concept.ConceptStatus;
import user.ConceptsGroup;
import main.*;

public class NodesTableArray {
	private NodeData table[]; 
	private int count; 
	private int max;
	
	public NodesTableArray(NodeData table[]) {
		this.table   = table;
		this.max = this.count = table.length;
	}
	public NodesTableArray(int maxQuantity) {
		this.table   = new NodeData[maxQuantity];
		this.count = 0;
		this.max   = maxQuantity;
	}
	
	public int getCount() {
			return this.count;
	}
	
	public void insert(NodeData nodeData) throws Exception {
		if(this.count == this.max)
			throw new Exception("Quantity of nodes is larger than capacity");
		table[count] = nodeData;
		this.count++;
	}
	public NodeData getNodeData(int position) throws Exception {
		if(position >= this.max)
			throw new Exception("Tried to read NodeData over the table");
		return this.table[position];
	}
	
	public int calculateOriginalConceptQuantity() {
		int originalQuantity = 0;
		for(int i=0; i<this.max; i++) {
			if(this.table[i].getStatus() == ConceptStatus.originalConcept)
			   originalQuantity++;
		}
		return originalQuantity;
	}
	public ConceptsGroup getGroupOriginalConcepts() {
		ConceptsGroup conceptsGroup = new ConceptsGroup();
		Concept concept;
		for(int i=0; i<this.count; i++) {
			if(this.table[i].getStatus() == ConceptStatus.originalConcept) {
				concept = WholeSystem.getConceptsRegister().getConcept(this.table[i].getShortName());
				// concept = new Concept(this.table[i].getStrIdNode(), this.table[i].getShortName(), this.table[i].getStatus(), 0, Config.Category.no, 0, Config.withoutConnectedComponent);  
				conceptsGroup.add(concept);
			}
		}
		return conceptsGroup;
	}
	
	public NodesTableArray createSortedNodesTableArrayBetweenness() {
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortBetweenness());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayBetweenness(int theFirst) {
		if(theFirst > this.max)
			theFirst = this.max;
		NodeData newTable[] = Arrays.copyOf(this.table, theFirst);
		Arrays.sort(newTable, new SortBetweenness());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayCloseness() {
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortCloseness());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayCloseness(int theFirst) {
		if(theFirst > this.max)
			theFirst = this.max;
		NodeData newTable[] = Arrays.copyOf(this.table, theFirst);
		Arrays.sort(newTable, new SortCloseness());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayEccentricity() {
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortEccentricity());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayEccentricity(int theFirst) {
		if(theFirst > this.max)
			theFirst = this.max;
		NodeData newTable[] = Arrays.copyOf(this.table, theFirst);
		Arrays.sort(newTable, new SortEccentricity());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayEigenvector() {
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortEigenvector());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayEigenvector(int theFirst) {
		if(theFirst > this.max)
			theFirst = this.max;
		NodeData newTable[] = Arrays.copyOf(this.table, theFirst);
		Arrays.sort(newTable, new SortEigenvector());
		return new NodesTableArray(newTable);
	}
	// sort by betweenness, after sort only the first records by closeness
	public NodesTableArray createSortedNodesTableArrayBetweennessCloseness(int theFirst) {
		if(theFirst > this.max)
			theFirst = this.max;
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortBetweenness());
		NodeData newNewTable[] = Arrays.copyOf(newTable, theFirst);
		Arrays.sort(newNewTable, new SortCloseness());
		return new NodesTableArray(newNewTable);
	}
	public NodesTableArray createSortedNodesTableArrayCrescentAverage() {
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortCrescentAverage());
		return new NodesTableArray(newTable);
	}
	public NodesTableArray createSortedNodesTableArrayEccentricityAndAverage() {
		NodeData newTable[] = Arrays.copyOf(this.table, this.count);
		Arrays.sort(newTable, new SortEccentricityAndAverage());
		return new NodesTableArray(newTable);
	}
	public String toStringShort(int quantityNodes) {
		StringBuilder str = new StringBuilder();
		str.append("Parcial quantity: "+quantityNodes+"\n\n");
		for(int i=0; i<quantityNodes && i<this.count; i++) {
			str.append(this.table[i].toString());
			str.append("\n\n");
		}
		return  str.toString();
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("Total quantity: "+this.count+" (intern), "+this.table.length+" (max size).\n\n");
		for(int i=0; i<this.count; i++) {
			str.append(this.table[i].toString());
			str.append("\n\n");
		}
		return str.toString();
	}
}

class SortBetweenness implements Comparator<NodeData> {
	public int compare(NodeData nodeData1, NodeData nodeData2) {
		if(nodeData1.getBetweenness() < nodeData2.getBetweenness())
			return 1;
		else if(nodeData1.getBetweenness() > nodeData2.getBetweenness())
			return -1;
		else
			return 0;
	}
}
class SortCloseness implements Comparator<NodeData> {
	public int compare(NodeData nodeData1, NodeData nodeData2) {
		if(nodeData1.getCloseness() < nodeData2.getCloseness())
			return 1;
		else if(nodeData1.getCloseness() > nodeData2.getCloseness())
			return -1;
		else
			return 0;
	}
}
class SortEccentricity implements Comparator<NodeData> {
	public int compare(NodeData nodeData1, NodeData nodeData2) {
		if(nodeData1.getEccentricity() < nodeData2.getEccentricity())
			return 1;
		else if(nodeData1.getEccentricity() > nodeData2.getEccentricity())
			return -1;
		else
			return 0;
	}
}
class SortEigenvector implements Comparator<NodeData> {
	public int compare(NodeData nodeData1, NodeData nodeData2) {
		if(nodeData1.getEigenvector() < nodeData2.getEigenvector())
			return 1;
		else if(nodeData1.getEigenvector() > nodeData2.getEigenvector())
			return -1;
		else
			return 0;
	}
}
class SortCrescentAverage implements Comparator<NodeData> {
	public int compare(NodeData nodeData1, NodeData nodeData2) {
		if(nodeData1.getAverage() < nodeData2.getAverage())
			return -1;
		else if(nodeData1.getAverage() > nodeData2.getAverage())
			return 1;
		else
			return 0;
	}
}
// decrescent in eccentricity and crescent in average (betweenness,closeness,eigenvector)
class SortEccentricityAndAverage implements Comparator<NodeData> {
	public int compare(NodeData nodeData1, NodeData nodeData2) {
		if(nodeData1.getEccentricity() < nodeData2.getEccentricity())
			return 1;
		else if(nodeData1.getEccentricity() > nodeData2.getEccentricity())
			return -1;
		else // if eccentricity is equals then it uses average (betweenness, closeness, eigenvector)
			if(nodeData1.getAverage() < nodeData2.getAverage())
				return -1;
			else if(nodeData1.getAverage() > nodeData2.getAverage())
				return 1;
			else
				return 0;
	}
}


