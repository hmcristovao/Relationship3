    package rdf;

import user.Concept;

// import com.hp.hpl.jena.rdf.model.Model; - funcionava em versão antiga do Jena
import org.apache.jena.rdf.model.Model; // troquei o caminho na versão 3.2 do Jena

public class QuerySparql {
	
	private Concept concept;
	private QueryString queryString;
	private ListRDF listRDF;
	private Model model;
	
	public QuerySparql(Concept concept, QueryString queryString, ListRDF listRDF) {
		this.concept = concept;
		this.queryString = queryString;
		this.listRDF = listRDF;
	}
	
	public QuerySparql(Concept concept) {
		this.concept = concept;
		this.queryString = new QueryString(concept);
		this.listRDF = new ListRDF();
	}

	public Concept getConcept() {
		return this.concept;
	}
	public QueryString getQueryString() {
		return this.queryString;
	}
	public ListRDF getListRDF() {
		return this.listRDF;
	}
	public void setListRDF(ListRDF listRDF) {
		this.listRDF = listRDF;
	}
	public Model getModel() {
		return this.model;
	}
	public void setModel(Model model) {
		this.model = model;
	}

	public void setQuery(QueryString queryString) {
		this.queryString = queryString;
	}

	public int sizeListRDF() {
		return this.listRDF.size();
	}
	
	public String toStringShort() {
		return  this.getConcept().toStringShort();
	}

	@Override
	public String toString() {
		return  this.getConcept().toString() + 
				"\n\nQuery = \n" + this.getQueryString() + 
				"\nRDF list = \n" + this.getListRDF();
	}

	

}
