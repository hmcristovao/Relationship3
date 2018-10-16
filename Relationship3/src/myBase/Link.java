package myBase;

public class Link {

	private String  linkDesc;
	private String  nodeDesc;
	private boolean isNodeSubject;
	
	public Link(String descLink, String descNode, boolean isNodeSubject) {
		super();
		this.linkDesc      = descLink;
		this.nodeDesc      = descNode;
		this.isNodeSubject = isNodeSubject;
	}

	public String getLinkDesc() {
		return this.linkDesc;
	}

	public String getNodeDesc() {
		return this.nodeDesc;
	}

	public boolean isNodeSubject() {
		return this.isNodeSubject;
	}
	
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(this.linkDesc);
		if(this.isNodeSubject)
			out.append(" -> ");
		else
			out.append(" <- ");
		out.append(this.nodeDesc);
		return out.toString();
	}
}
