package rdf;

import java.io.Serializable;

public class OneRDF  implements Serializable {
	private ItemRDF subject;
	private ItemRDF predicate;
	private ItemRDF object;
	
	public OneRDF(ItemRDF subject, ItemRDF predicate, ItemRDF object) {
		this.subject   = subject;
		this.predicate = predicate;
		this.object    = object;
	}

	public ItemRDF getSubject() {
		return this.subject;
	}
	public ItemRDF getPredicate() {
		return this.predicate;
	}
	public ItemRDF getObject() {
		return this.object;
	}
	@Override
	public String toString() {
		return  "\nsubject = " + this.getSubject() + 
				"\npredicate = " + this.getPredicate() + 
				"\nobject = " + this.getObject();
	}
}

