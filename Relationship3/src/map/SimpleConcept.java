package map;

import user.Concept;
import graph.NodeData;

public class SimpleConcept {
	private String   label;
	private NodeData nodeData;
	private String   idConcept;  // c#
	
	public SimpleConcept(NodeData nodeData) {
		this.label     = nodeData.getShortName();
		this.nodeData  = nodeData;
		this.idConcept = null;
	}
	// special case (concept alon in map)
	public SimpleConcept(Concept concept) {
		this.label     = concept.getBlankName();
		this.nodeData  = null;
		this.idConcept = null;		
	}
	
	public String getLabel() {
		return this.label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public NodeData getNodeData() {
		return this.nodeData;
	}
	public void setNodeData(NodeData nodeData) {
		this.nodeData = nodeData;
	}
	public String getIdConcept() {
		return this.idConcept;
	}
	public void setIdConcept(String idConcept) {
		this.idConcept = idConcept;
	}
	public void setIdConcept(int n) {
		this.idConcept = "c"+n;
	}

	public String toString() {
		return  "label: "+this.label+
				"\nidConcept: "+(this.idConcept==null?"":this.idConcept)+
		        "\nnodeData: "+(this.nodeData==null?"":this.nodeData.toString());
	}
}
