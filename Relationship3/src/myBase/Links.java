package myBase;

import java.util.LinkedList;
import java.util.List;

public class Links {

	private List<Link> linkList;
	
	public Links() {
		this.linkList = new LinkedList<Link>();
	}
	
	public List<Link> getLinkList() {
		return this.linkList;
	}
	
	public boolean add(Link link) {
		return this.linkList.add(link);
	}
	
	public String toString() {
		StringBuilder out = new StringBuilder();
		out.append(" (#");
		out.append(this.linkList.size());
		out.append(")\n");
		for(Link link : this.linkList) {
			out.append("   ");
			out.append(link.toString());
			out.append("\n");
		}
		return out.toString();
	}
}
