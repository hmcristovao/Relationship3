package graph;

import main.Constants;
import user.Concept;
import user.ConceptsGroup;

public class MeasuresRanks {
	private int connectedComponentNumber;  
	private GephiGraphData gephiGraphData; 
	private NodesTableArray basicTable; // without order, to be used in the building of the other tables
	private NodesTableArray betweenness; 
	private NodesTableArray closeness;
	private NodesTableArray eccentricity;
	private NodesTableArray eigenvector;
	private NodesTableArray betweennessCloseness;
	
	private ConceptsGroup originalGroupConcept;

	public MeasuresRanks(int number) {
		this.connectedComponentNumber         = number;
		this.gephiGraphData                   = new GephiGraphData();
		this.basicTable                       = null; // will be fill before to sort the ranks
		this.betweenness                      = null; // will be fill when happen the sorts to the ranks
		this.closeness                        = null;
		this.eccentricity                     = null;
		this.eigenvector                      = null;
		this.betweennessCloseness         	  = null;
		this.originalGroupConcept             = new ConceptsGroup();
	}
	
	public int getConnectedComponentNumber() {
		return this.connectedComponentNumber;
	}
	public void setConnectedComponentNumber(int connectedComponentNumber) {
		this.connectedComponentNumber = connectedComponentNumber;
	}
	public GephiGraphData getGephiGraphData() {
		return this.gephiGraphData;
	}
	public void setGephiGraphData(GephiGraphData gephiGraphData) {
		this.gephiGraphData = gephiGraphData;
	}
	public NodesTableArray getBasicTable() {
		return this.basicTable;
	}
	public void setBasicTable(NodesTableArray basicTable) {
		this.basicTable = basicTable;
	}
	public NodesTableArray getBetweenness() {
		return this.betweenness;
	}
	public void setBetweenness(NodesTableArray betweenness) {
		this.betweenness = betweenness;
	}
	public NodesTableArray getEccentricity() {
		return this.eccentricity;
	}
	public void setEccentricity(NodesTableArray eccentricity) {
		this.eccentricity = eccentricity;
	}
	public NodesTableArray getCloseness() {
		return this.closeness;
	}
	public void setCloseness(NodesTableArray closeness) {
		this.closeness = closeness;
	}
	public NodesTableArray getEigenvector() {
		return this.eigenvector;
	}
	public void setEigenvector(NodesTableArray eigenvector) {
		this.eigenvector = eigenvector;
	}
	public NodesTableArray getBetweennessCloseness() {
		return this.betweennessCloseness;
	}
	public void setBetweennessCloseness(NodesTableArray betweennessCloseness) {
		this.betweennessCloseness = betweennessCloseness;
	}
	public ConceptsGroup getOriginalGroupConcepts() {
		return this.originalGroupConcept;
	}

	public String toStringShort(int connectedComponentNumber, int quantityNodes) {
		return  Constants.doubleLine+"Table array (betweenness sorted) - Connected component number: "
				+ connectedComponentNumber + " (only the first "+quantityNodes+" nodes)"+Constants.singleLine  
				+ this.getBetweenness().toStringShort(quantityNodes) 
		        + "\n"+Constants.doubleLine+"Table array (closeness sorted) - Connected component number: "
		        + connectedComponentNumber + " (only the first "+quantityNodes+" nodes)"+Constants.singleLine  
		        + this.getCloseness().toStringShort(quantityNodes) 
		        + "\n"+Constants.doubleLine+"Table array (eccentricity sorted) - Connected component number: "
		        + connectedComponentNumber + " (only the first "+quantityNodes+" nodes)"+Constants.singleLine  
		        + this.getEccentricity().toStringShort(quantityNodes) 
		        + "\n"+Constants.doubleLine+"Table array (eingenvector sorted) - Connected component number: "
		        + connectedComponentNumber + " (only the first "+quantityNodes+" nodes)"+Constants.singleLine  
		        + this.getEigenvector().toStringShort(quantityNodes)		
				+ "\n"+Constants.doubleLine+"Table array (betweenness+closeness sorted) - Connected component number: "
		        + connectedComponentNumber + " (only the first "+quantityNodes+" nodes)"+Constants.singleLine  
		        + this.getBetweennessCloseness().toStringShort(quantityNodes);		
	}

	public String toString(int connectedComponentNumber) {
		return  Constants.doubleLine+"Table array (betweenness sorted) - Connected component number: "
				+ connectedComponentNumber + Constants.singleLine  
				+ this.getBetweenness().toString() 
		        + "\n"+Constants.doubleLine+"Table array (closeness sorted) - Connected component number: "
		        + connectedComponentNumber + Constants.singleLine  
		        + this.getCloseness().toString() 
		        + "\n"+Constants.doubleLine+"Table array (eccentricity sorted) - Connected component number: "
		        + connectedComponentNumber + Constants.singleLine  
		        + this.getEccentricity().toString() 
		        + "\n"+Constants.doubleLine+"Table array (eingenvector sorted) - Connected component number: "
		        + connectedComponentNumber + Constants.singleLine  
		        + this.getEigenvector().toString()		
				+ "\n"+Constants.doubleLine+"Table array (betweenness+closeness sorted) - Connected component number: "
		        + connectedComponentNumber + Constants.singleLine  
		        + this.getBetweennessCloseness().toString();		
	}
}
