package rdf;

import java.io.Serializable;
import java.util.LinkedList;

public class ListRDF  implements Serializable {
	private LinkedList<OneRDF> list;
	
	public ListRDF() {
		this.list = new LinkedList<OneRDF>();
	}
	public ListRDF(LinkedList<OneRDF> list) {
		super();
		this.list = list;
	}
	public LinkedList<OneRDF> getList() {
		return this.list;
	}
	public int size() {
		return this.list.size();
	}
	
	public void add(OneRDF item) {
		this.list.add(item);  
	}
	@Override
	public String toString() {
		return this.getList().toString();
	}
}
