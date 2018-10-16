package rdf;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.jena.riot.WebContent;

import main.Constants;
import main.Count;
import main.Log;
import main.WholeSystem;
import myBase.Links;
import myBase.Link;
import user.Concept;
import user.ConceptsGroup;

//import com.hp.hpl.jena....  - funcionava em versão antiga do Jena - troquei o caminho para org.apache.jena... na versão 3.2 do Jena
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

public class SetQuerySparql {
	private LinkedList<QuerySparql> listQuerySparql;
	private int totalRDF;

	public SetQuerySparql() {
		this.listQuerySparql = new LinkedList<QuerySparql>();
		this.totalRDF  = 0;
	}
	
	public LinkedList<QuerySparql> getListQuerySparql() {
		return this.listQuerySparql;
	}
	public int getTotalConcepts() {
		return this.listQuerySparql.size();
	}
	public int getTotalRDFs() {
		return this.totalRDF;
	}
	public void incTotalRDFs() {
		this.totalRDF++;
	}
	public void incTotalRDFs(int value) {
		this.totalRDF += value;
	}
	public void insertQuerySparql(QuerySparql querySparql) {
		this.listQuerySparql.add(querySparql);  
	}
	
	// get total RDFs of an especifc concept
	public int getTotalRdfsEspecificConcept(String blankName) {
		for(QuerySparql querySparql : this.listQuerySparql) {
			if(querySparql.getConcept().getBlankName().equals(blankName))
				return querySparql.sizeListRDF();
		}
		// it did not find concept
		return 0;
	}
	
	// create a QuerySparql object, fill it with concept, and insert it into listQuerySparql
	public void insertQuerySparql(Concept concept) {
		QueryString auxQuery = new QueryString();
		ListRDF auxListRDF = new ListRDF();
		QuerySparql querySparql = new QuerySparql(concept, auxQuery, auxListRDF);
		this.listQuerySparql.add(querySparql);  
	}

	// fill listQuerySparql with a concept list
	public void insertListConcept(ConceptsGroup conceptsGroup) {
		QuerySparql querySparql;
		for(Concept concept : conceptsGroup.getList()) { 
			querySparql = new QuerySparql(concept, new QueryString(), new ListRDF());
			this.listQuerySparql.add(querySparql); 
		}
	}

	// add all objects from old listQuerySparql
	// return quantity of the element copied 
	public int insertListQuerySparql(LinkedList<QuerySparql> listQuerySparql) {
		int count = 0;
		for(QuerySparql querySparql : listQuerySparql) { 
			this.listQuerySparql.add(querySparql);
			count++;
		}
		return count;
	}
		
	// exceptional function, direct access to current concepts listQuerySparql
	// return one concept short name per line
	public String getCurrentConcepts() {
		StringBuilder out = new StringBuilder();
		for(QuerySparql x: this.listQuerySparql) {
			out.append(x.getConcept().toString());
			out.append("\n");
		}
		return out.toString();
	}
	// exceptional function, direct access to original concepts listQuerySparql
	// return one object LinkedList containing all current concepts short name 
	public ConceptsGroup getListCurrentConcepts() {
		ConceptsGroup list = new ConceptsGroup();
		for(QuerySparql x: this.listQuerySparql) 		
			list.add(x.getConcept());
		return list;
	}

	private StringBuilder readFileQueryDefault() throws IOException {
		BufferedReader fileQueryDefault = new BufferedReader(new FileReader(WholeSystem.configTable.getString("nameQueryDefaultFile")));
		StringBuilder queryDefault = new StringBuilder();
		String linhaAux = null;
	    while (true) {
	       linhaAux = fileQueryDefault.readLine();
	       if(linhaAux == null)
	    	   break;
	       queryDefault.append(linhaAux);
	       queryDefault.append("\n");
	    }
        fileQueryDefault.close();
        return queryDefault;
	}
	// replace and make a copy of the query
	private StringBuilder replaceQueryDefault(StringBuilder queryDefault, String concept) {
		StringBuilder newQueryDefault = new StringBuilder(queryDefault);
		int start = 0;
		while( (start = newQueryDefault.indexOf(Constants.markQueryReplacement, start)) != -1)
		   newQueryDefault.replace(start, start+Constants.markQueryReplacement.length(), concept);
		return newQueryDefault;
	}
	
	public int assemblyQueries() throws IOException {	
		StringBuilder queryDefault = this.readFileQueryDefault();
		StringBuilder newQueryDefault = null;
	    String newConcept = null;
	    QueryString queryString = null;
	    int n = 0;
		for(QuerySparql x: this.getListQuerySparql()) {
			newConcept = x.getConcept().getUnderlineConcept();
			newQueryDefault = this.replaceQueryDefault(queryDefault, newConcept);
			queryString = new QueryString(newQueryDefault);
			x.setQuery(queryString);
			n++;
		}
		return n;
	}
	
	//  read all RDF triples belongs to each query concept 
	public int collectRDFsAllQueries(Count numRdfsInInternet, Count numRdfsInFile) throws Exception {
		int total = 0, subTotal;
		for(int i=0; i < this.getListQuerySparql().size(); i++) {
			QuerySparql querySparql = this.getListQuerySparql().get(i);
			
			subTotal = collectRDFsOneQuery(querySparql, numRdfsInInternet, numRdfsInFile);
			
			// update rdfs quantity in concept
			querySparql.getConcept().setQuantityRdfs(subTotal);
			// if exist concept into WholeSystem then update its quantity
			WholeSystem.getConceptsRegister().getConcept(querySparql.getConcept().getBlankName()).setQuantityRdfs(subTotal);
		
			total += subTotal;
		}
		return total;
	}
	
	// read all RDF triples belongs to one query concept 
	private int collectRDFsOneQuery(QuerySparql querySparql, Count numRdfsInInternet, Count numRdfsInFile) throws Exception {
		int count = 0;
		
		// decide to read the RDFs among three ways: 
		// 1. read from my knowledge base;
		// 2. read from RDFs files offline
		// 3. read from DBPEDIA Server
		
		// < 1. read from my knowledge base >
		// if dbpediaServer var do not contais "http", is because the RDFs are in my knowledge base
		if(WholeSystem.configTable.getInt("knowledgeBasePlace") == 2) { // 1=DBpedia, 2=my knowledge base
			count = readMyKnowledgeBaseRDFsOneQuery(querySparql);
			numRdfsInFile.incCount(count);
		}

		// < 2. read from RDFs files offline >
		// else, if concept exists in persistence data, read it
		else if(WholeSystem.getRdfsFileTable().containsKey(querySparql.getConcept().getBlankName())) {
			count = this.readPersistenceRDFsOneQuery(querySparql);
			numRdfsInFile.incCount(count);
		}
		
		// < 3. read from DBPEDIA Server >
		// else, read it of the Internet DataBase
		else {
			count = this.readInternetDataBaseOneQuery(querySparql);			
			numRdfsInInternet.incCount(count);
			
			// and save it in file
			String fileName = RdfsFilesTable.formatToFileName(querySparql.getConcept().getBlankName());
			ObjectOutputStream buffer = new ObjectOutputStream(
					new FileOutputStream(WholeSystem.configTable.getString("dirRdfsPersistenceFiles")+"\\"+fileName));
			buffer.writeObject(querySparql.getListRDF()) ; 
			buffer.flush(); 
			buffer.close();
		}
		return count;
	}
	
	private int readMyKnowledgeBaseRDFsOneQuery(QuerySparql querySparql) throws Exception {

		String strConcept = querySparql.getConcept().getBlankName();
		Links linkList = WholeSystem.getMyKnowledgeBase().get(strConcept);
		
		ListRDF listRDF = querySparql.getListRDF();

		ItemRDF subjectRDF, predicateRDF, objectRDF; 
		for(Link link : linkList.getLinkList()) {

			// verify the direction of arrow
			if(link.isNodeSubject()) {
				subjectRDF   = new ItemRDF(strConcept);
				objectRDF    = new ItemRDF(link.getNodeDesc());				
			}
			else {
				subjectRDF   = new ItemRDF(link.getNodeDesc());
				objectRDF    = new ItemRDF(strConcept);
			}
			
			predicateRDF  = new ItemRDF(link.getLinkDesc());
			OneRDF oneRDF = new OneRDF(subjectRDF, predicateRDF, objectRDF);

			// insert complete item into listQuerySparql of the RDFs
			listRDF.getList().add(oneRDF);

			this.incTotalRDFs();		
		}
		return querySparql.getListRDF().size();
	}

	private int readPersistenceRDFsOneQuery(QuerySparql querySparql) throws Exception {
		String strConcept = querySparql.getConcept().getBlankName();
		String fileName   = RdfsFilesTable.formatToFileName(strConcept);
		ObjectInputStream buffer = new ObjectInputStream(new FileInputStream(WholeSystem.configTable.getString("dirRdfsPersistenceFiles")+"\\"+fileName));
		querySparql.setListRDF((ListRDF)buffer.readObject());
		buffer.close();
		return querySparql.getListRDF().size();
	}
	
	private int readInternetDataBaseOneQuery(QuerySparql querySparql) {
		String queryStr = querySparql.getQueryString().getQueryStrString();
		Query query = QueryFactory.create(queryStr);
		QueryEngineHTTP queryEngineHTTP = (QueryEngineHTTP)QueryExecutionFactory.sparqlService(WholeSystem.configTable.getString("dbpediaServer"), query);
		queryEngineHTTP.setModelContentType(WebContent.contentTypeJSONLD);
		Model model = queryEngineHTTP.execConstruct();

		querySparql.setModel(model);
		ListRDF listRDF = querySparql.getListRDF();
		StmtIterator stmtIterator = model.listStatements();

		int count = 0;
		while (stmtIterator.hasNext()) {
			// read elements of the model
			Statement statement = stmtIterator.nextStatement();  
			Resource  subject   = statement.getSubject();     
			Property  predicate = statement.getPredicate();   
			RDFNode   object    = statement.getObject();

			// create complete registerRDF 
			ItemRDF subjectRDF   = new ItemRDF(subject.toString());
			ItemRDF predicateRDF = new ItemRDF(predicate.toString());
			ItemRDF objectRDF    = new ItemRDF(object.toString());
			OneRDF  oneRDF       = new OneRDF(subjectRDF, predicateRDF, objectRDF);

			// insert complete item into listQuerySparql of the RDFs
			listRDF.getList().add(oneRDF);
			count++;

			this.incTotalRDFs();
		}
		queryEngineHTTP.close();
		return count;
	}
	
	// consider basicConcept without underline character
	public boolean isCurrentConcept(String basicConcept) {
		for(QuerySparql x : this.getListQuerySparql()) {
			if(x.getConcept().equals(basicConcept))
				return true;
		}
		return false;
	}
	
	public ConceptsGroup removeConceptsWithZeroRdfs() {
		ConceptsGroup zeroRdfsConcepts = new ConceptsGroup();
		ArrayList<QuerySparql> auxList = new ArrayList<QuerySparql>();
		// find concepts with zero RDFs
		for(QuerySparql querySparql : this.listQuerySparql) {
			if(querySparql.getListRDF().size() == 0) {
				zeroRdfsConcepts.add(querySparql.getConcept());
				auxList.add(querySparql);
			}
		}
		// remove concepts with zero RDFs from current SetQuerySparql
		for(QuerySparql querySparql : auxList) {
			this.listQuerySparql.remove(querySparql);
		}
		// remove concepts with zero RDFs from static ConceptsGroup (in WholeSystem)
		for(Concept concept : zeroRdfsConcepts.getList()) {
			WholeSystem.getConceptsRegister().removeConcept(concept.getBlankName());
		}
		return zeroRdfsConcepts;
	}
	
	public String toStringShort() {
		StringBuilder out = new StringBuilder();
		int n = 1;
		for(QuerySparql x: this.listQuerySparql) {
			out.append("\nConcept number ");
			out.append(n++);
			out.append(": ");
			out.append(x.toStringShort());
		}
		return out.toString();
	}

	@Override
	public String toString() {
		StringBuilder out = new StringBuilder();
		int n = 1;
		for(QuerySparql x: this.listQuerySparql) {
			out.append("\n***** Concept number ");
			out.append(n++);
			out.append(" *****\n");
			out.append(x.toString());
			out.append("\n");
		}
		return out.toString();
	}
}
