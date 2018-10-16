package rdf;

import user.Concept;

public class QueryString {
	
	private StringBuilder queryStr;
	
	public QueryString() {
		this.queryStr = new StringBuilder();
	}
	public QueryString(StringBuilder queryStr) {
		this.queryStr = queryStr;
	}
	public QueryString(Concept concept) {
		this.queryStr = this.buildQuery(concept);
	}
	public StringBuilder buildQuery(Concept concept) {
		StringBuilder aux = new StringBuilder();
		return aux;
	}
	public StringBuilder getQueryStr() {
		return this.queryStr;
	}
	public String getQueryStrString() {
		return this.queryStr.toString();
	}
	public void append(String str) {
		this.queryStr.append(str);
		return;
	}
	public void appendLine(String str) {
		this.queryStr.append("\n");
		this.queryStr.append(str);
		return;
	}
	@Override
	public String toString() {
		return this.getQueryStr().toString();
	}
}
