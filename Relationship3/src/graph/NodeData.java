// It is one raw of the table of nodes (NodesTableArray)
package graph;

import user.Concept;
import user.Concept.ConceptStatus;
import main.Constants;

public class NodeData {
	// parse data
	private String strIdNode; // is the same as blankName 
	private String shortName; // is the same as blankName in the Concept class = strIdNode
	private String fullName;  // is the same as fullName in the Concept class
	private org.graphstream.graph.Node streamNode;
	private org.gephi.graph.api.Node gephiNode;
	private ConceptStatus conceptStatus;

	// extra attributes
	private String homepageAttribute;
	private String abstractAttribute;
	private String commentAttribute;
	private String imageAttribute;
		
	// calculated data
	private int connectedComponent; 
	private double betweenness;
	private double closeness;
	private double eigenvector;
	private int partitioning; 
	private double average;  // arithmetic average of betweenness, closeness and eigenvector
	private double eccentricity;
	
	// constructor to permanent data
	public NodeData(String strIdNode, 
					String shortName,
					String fullName,
					org.graphstream.graph.Node streamNode, 
					org.gephi.graph.api.Node gephiNode,
					ConceptStatus conceptStatus) {
		this.strIdNode         = strIdNode;
		this.shortName         = shortName;
		this.fullName          = fullName;
		this.streamNode        = streamNode;
		this.gephiNode         = gephiNode;
		this.conceptStatus	    	   = conceptStatus;
		this.homepageAttribute = null;
		this.abstractAttribute = null;
		this.commentAttribute  = null;
		this.imageAttribute    = null;
	}
	public NodeData(String strIdNode, 
					String shortName, 
					String fullName,
					StreamGraphData streamGraphData, 
					GephiGraphData gephiGraphData, 
					ConceptStatus conceptStatus) {
		this(strIdNode, shortName, fullName,
			 streamGraphData.getStreamGraph().getNode(strIdNode), 
			 gephiGraphData.getGephiGraph().getNode(strIdNode), 
			 conceptStatus);
	}
	public String getStrIdNode() {
		return this.strIdNode;
	}
	public String getShortName() {
		return this.shortName;
	}
	public String getFullName() {
		return this.fullName;
	}
	public org.graphstream.graph.Node getStreamNode() {
		return this.streamNode;
	}
	public org.gephi.graph.api.Node getGephiNode() {
		return this.gephiNode;
	}
	public ConceptStatus getStatus() {
		return this.conceptStatus;
	}
	public void setStatus(ConceptStatus conceptStatus) {
		this.conceptStatus = conceptStatus;
	}

	public String getHomepageAttribute() {
		return this.homepageAttribute;
	}
	public void setHomepageAttribute(String homepageAttribute) {
		this.homepageAttribute = homepageAttribute;
	}
	public String getAbstractAttribute() {
		return this.abstractAttribute;
	}
	public void setAbstractAttribute(String abstractAttribute) {
		this.abstractAttribute = abstractAttribute;
	}
	public String getCommentAttribute() {
		return this.commentAttribute;
	}
	public void setCommentAttribute(String commentAttribute) {
		this.commentAttribute = commentAttribute;
	}
	public String getImageAttribute() {
		return this.imageAttribute;
	}
	public void setImageAttribute(String imageAttribute) {
		this.imageAttribute = imageAttribute;
	}

	public int getConnectedComponent() {
		return this.connectedComponent;
	}
	public void setConnectedComponent(int connectedComponent) {
		this.connectedComponent = connectedComponent;
	}
	public double getBetweenness() {
		return this.betweenness;
	}
	public void setBetweenness(double betweenness) {
		this.betweenness = betweenness;
		this.average     = (this.betweenness + this.closeness + this.eigenvector)/3.0;
	}
	public void setBetweennessCloseness(double betweenness, double closeness) {
		this.betweenness = betweenness;
		this.closeness   = closeness;
		this.average     = (this.betweenness + this.closeness + this.eigenvector)/3.0;
	}
	public double getCloseness() {
		return this.closeness;
	}
	public void setCloseness(double closeness) {
		this.closeness = closeness;
		this.average     = (this.betweenness + this.closeness + this.eigenvector)/3.0;
	}
	public double getEigenvector() {
		return this.eigenvector;
	}
	public void setEigenvector(double eigenvector) {
		this.eigenvector = eigenvector;
		this.average     = (this.betweenness + this.closeness + this.eigenvector)/3.0;
	}
	public int getPartitioning() {
		return this.partitioning;
	}
	public void setPartitioning(int partitioning) {
		this.partitioning = partitioning;
	}

	public double getAverage() {
		return this.average;
	}
	public double getEccentricity() {
		return this.eccentricity;
	}
	public void setEccentricity(double eccentricity) {
		this.eccentricity = eccentricity;
	}
	
	public int compareTo(NodeData nodeData) {
		return this.strIdNode.compareTo(nodeData.strIdNode);
	}
	
	@Override
	public String toString() {
		return  "Id: " + this.getStrIdNode() +
				"\nFull name:           " + this.getFullName() +
				"\nStatus:              " + Concept.statusToString(this.conceptStatus) +
				"\nConnected component: " + this.getConnectedComponent() +
				"\nBetweenness:         " + this.getBetweenness() +
				"\nCloseness:           " + this.getCloseness() +
				"\nEccentricity:        " + this.getEccentricity() +
				"\nEigenvector:         " + this.getEigenvector() +
				"\nAverage:             " + this.getAverage() +
				"\nPartitioning:        " + this.getPartitioning();
	}
}
