package map;

import user.Concept;
import graph.NodeData;

public class Proposition {
	private SimpleConcept sourceConcept;
	private SimpleConcept targetConcept;
	private String        link;
	
	// used in CXL file (will be filled in ConceptMap.fillAtributesOfFileCXL
	private String idSourceJoin;     // j#
	private String idLinkingPhrase;  // l#
	private String idTargetJoin;     // j#
	
	public Proposition(NodeData sourceNodeData, String link, NodeData targetNodeData) {
		this.sourceConcept  = new SimpleConcept(sourceNodeData);
		this.targetConcept  = new SimpleConcept(targetNodeData);
		this.link           = link;
		this.idSourceJoin   = null;
		this.idLinkingPhrase= null;
		this.idTargetJoin   = null;
	}
	
	// special case: alone concept (original concept with zero degree in Stream Graph)
	public Proposition(Concept concept) {
		this.sourceConcept  = new SimpleConcept(concept);
		this.targetConcept  = null;
		this.link           = null;
		this.idSourceJoin   = null;
		this.idLinkingPhrase= null;
		this.idTargetJoin   = null;
	}

	public String getLink() {
		return this.link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public SimpleConcept getSourceConcept() {
		return this.sourceConcept;
	}
	public void setSourceConcept(SimpleConcept simpleConcept) {
		this.sourceConcept = simpleConcept;
	}
	public void setSourcetConcept(String label) {
		this.sourceConcept.setLabel(label);
	}
	public SimpleConcept getTargetConcept() {
		return this.targetConcept;
	}
	public void setTargetConcept(SimpleConcept simpleConcept) {
		this.targetConcept = simpleConcept;
	}
	public void setTargetConcept(String label) {
		this.targetConcept.setLabel(label);
	}
	public String getIdSourceConcept() {
		return this.sourceConcept.getIdConcept();
	}
	public String getIdTargetConcept() {
		return this.targetConcept.getIdConcept();
	}
	public void setIdSourceConcept(int n) {
		this.sourceConcept.setIdConcept("c"+n);
	}
	public void setIdSourceConcept(String str) {
		this.sourceConcept.setIdConcept(str);
	}

	public String getIdSourceJoin() {
		return this.idSourceJoin;
	}

	public void setIdSourceJoin(int n) {
		this.idSourceJoin = "j"+n;
	}
	public void setIdSourceJoin(String str) {
		this.idSourceJoin = str;
	}

	public String getIdLinkingPhrase() {
		return this.idLinkingPhrase;
	}

	public void setIdLinkingPhrase(int n) {
		this.idLinkingPhrase = "l"+n;
	}
	public void setIdLinkingPhrase(String str) {
		this.idLinkingPhrase = str;
	}

	public void setIdTargetConcept(int n) {
		this.targetConcept.setIdConcept("c"+n);
	}
	public void setIdTargetConcept(String str) {
		this.targetConcept.setIdConcept(str);
	}

	public String getIdTargetJoin() {
		return this.idTargetJoin;
	}

	public void setIdTargetJoin(int n) {
		this.idTargetJoin = "j"+n;
	}
	public void setIdTargetJoin(String str) {
		this.idTargetJoin = str;
	}

	public String toString() {
		if(this.link == null)
			return this.sourceConcept.getLabel();
		else
			return this.sourceConcept.getLabel() + " -> " + this.link + " -> " + this.targetConcept.getLabel();
	}

	public String toStringAux() {
		if(this.link == null)
			return this.sourceConcept.getLabel();
		else
			return this.sourceConcept.getLabel() + " -> " + this.link+"("+this.getIdLinkingPhrase()+") -> " + this.targetConcept.getLabel();
	}

	public String toStringComplete() {
		if(this.link == null)
			return "Source: "+this.sourceConcept.getLabel();
		else
			return   "idSourceConcept: "+(this.sourceConcept.getIdConcept()==null?"(null)":this.sourceConcept.getIdConcept())+
				   "\nSourceConcept:   "+(this.sourceConcept.getLabel()==null?"(null)":this.sourceConcept.getLabel())+
				   "\nidSourceJoin:    "+(this.idSourceJoin==null?"(null)":this.idSourceJoin)+
				   "\nidLink:          "+(this.idLinkingPhrase==null?"(null)":this.idLinkingPhrase)+
				   "\nLink:            "+(this.link==null?"(null)":this.link)+
				   "\nidTargetJoin:    "+(this.idTargetJoin==null?"(null)":this.idTargetJoin)+
				   "\nidTargetConcept: "+(this.targetConcept.getIdConcept()==null?"(null)":this.targetConcept.getIdConcept())+
				   "\nTargetConcept:   "+(this.targetConcept.getLabel()==null?"(null)":this.targetConcept.getLabel());
				   	}
	
}
