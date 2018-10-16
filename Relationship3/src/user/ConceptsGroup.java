package user;

import graph.NodeData;

import java.util.ArrayList;
import java.util.HashMap;

import user.Concept.ConceptCategory;
import user.Concept.ConceptStatus;
import main.Constants;
import main.Log;
import main.WholeSystem;

public class ConceptsGroup {
	private ArrayList<Concept> list;
	private HashMap<String, Concept> hash;
	
	public ConceptsGroup() {
		this.list = new ArrayList<Concept>();
		this.hash = new HashMap<String, Concept>(); 
	}

	public ArrayList<Concept> getList() {
		return this.list;
	}
	
	public boolean add(Concept concept) {
		// if there is not repeted key (concept), add in HashMap and ArrayList too 
		if(!this.hash.containsKey(concept.getBlankName())) {
			this.hash.put(concept.getBlankName(), concept);
			this.list.add(concept);
			return true;
		}
		return false;
	}

	// CALCULATE QUANTITIES ========================================================================
	
	public int size() {
		return this.list.size();
	}
	
	// rather must WholeSystem.getQuantityOriginalConcepts()
	// because it is only consult
	public int getQuantityOriginalConcept() {
		int count = 0;
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.originalConcept) {
				count++;
			}
		}		
		return count;
	}

	// GET GROUP CONCEPTS ========================================================================
		
	public ConceptsGroup getCurrentConcepts(int iteration) {  // original and selected concepts of all previous iterations (except the current iteration) 
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if( concept.getStatus() == ConceptStatus.originalConcept)
			   result.add(concept);
			else if(iteration > 0) {
				// get all concepts in each previous iterations
				for(int i=0; i<iteration; i++) {
					if( ( concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept || 
						  concept.getStatus() == ConceptStatus.selectedEigenvectorConcept ||
						  concept.getStatus() == ConceptStatus.selected ) && 				  
						  concept.getIteration() == i ) {
						result.add(concept);
					}
				}
			}
		}		
		return result;
	}
	
	// rather must WholeSystem.getOriginalConcepts()
	// because it static and faster
	public ConceptsGroup getOriginalConcepts() {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.originalConcept) {
				result.add(concept);
			}
		}		
		return result;
	}
	
	
	public ConceptsGroup getSelectedBetweennessClosenessConcepts() {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedEigenvectorConcepts() {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.selectedEigenvectorConcept) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedBetweennessClosenessConcepts(int iteration) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept &&
			   concept.getIteration() == iteration) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedEigenvectorConcepts(int iteration) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.selectedEigenvectorConcept &&
			   concept.getIteration() == iteration) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedBetweennessClosenessConcepts(int iteration, int connectedComponent) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept &&
			   concept.getIteration() == iteration &&
			   concept.getConnectedComponent(iteration) == connectedComponent
			  ) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedEigenvectorConcepts(int iteration, int connectedComponent) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getStatus() == ConceptStatus.selectedEigenvectorConcept &&
			   concept.getIteration() == iteration &&
			   concept.getConnectedComponent(iteration) == connectedComponent
			  ) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedConcepts(int iteration, int connectedComponent) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if( ( concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept ||
				  concept.getStatus() == ConceptStatus.selectedEigenvectorConcept ||
				  concept.getStatus() == ConceptStatus.selected 				  
					) &&
			    concept.getIteration() == iteration &&
			    concept.getConnectedComponent(iteration) == connectedComponent
			  ) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedConcepts(int iteration) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if( ( concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept ||
				  concept.getStatus() == ConceptStatus.selectedEigenvectorConcept ||
				  concept.getStatus() == ConceptStatus.selected 				  
				) &&
				concept.getIteration() == iteration
				) {
				result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getSelectedConcepts() {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if( concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept ||
				concept.getStatus() == ConceptStatus.selectedEigenvectorConcept  ||
                concept.getStatus() == ConceptStatus.selected 				  
			   ) {
			   result.add(concept);
			}
		}		
		return result;
	}
	public ConceptsGroup getConcepts(int iteration) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if(concept.getIteration() == iteration) {
				result.add(concept);
			}
		}		
		return result;
	}
	

	// SPECIAL  ========================================================================

	public boolean removeConcept(String blankName) {
		if(!this.hash.containsKey(blankName))
			return false;
		Concept concept = this.hash.get(blankName);
		this.list.remove(concept);
		this.hash.remove(blankName);
		return true;
	}

	// if is concept "ConceptCategory", add pure concept too 
	// only concepts of current iteration and selected by betweenness+closeness and eigenvector
	// and can not be equal to original concept 
	public ConceptsGroup duplicateConceptsWithoutCategory(int iteration) {
		ConceptsGroup result = new ConceptsGroup();
		for(Concept concept : this.list) {
			if( concept.getCategory() == ConceptCategory.yes && 
				concept.getIteration() == iteration &&
				( concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept ||
				  concept.getStatus() == ConceptStatus.selectedEigenvectorConcept
				)) {
				    // verify whether it is not original concept
				    if(WholeSystem.getOriginalConcepts().getConcept(Concept.extractCategory(concept.getBlankName())) != null)
				    	continue;
					// create new concept without "Category"
					Concept newConceptWithoutCategory = new Concept(
							Concept.extractCategoryFullName(concept.getFullName()),
							Concept.extractCategory(concept.getBlankName()), 
							ConceptStatus.selected, 
    						iteration,
    						ConceptCategory.was,
    						0,    // still is not possible to know the quantity RDFs because it was not yet search rdfs
    						-1);  // it is not possible to know the component connected  (??? need to be filled ???)
    				// this.add(newConceptWithoutCategory); não é possível alterar o lista do laço...
    				result.add(newConceptWithoutCategory);
       		}
		}
		// it is necessary another loop to insert new concepts (they can not insert into: for(Concept concept : this.list) {... )
		for(Concept concept : result.list) {
			this.add(concept);
		}
		return result;
	}
		
		
	// IS... ========================================================================

	public boolean isOriginalConcept(String blankName) {
		Concept concept = this.hash.get(blankName);
		if(concept == null)
			return false;
		return concept.getStatus() == ConceptStatus.originalConcept;
	}
	public boolean isSelectedBetweennessClosenessConcept(String blankName) {
		Concept concept = this.hash.get(blankName);
		if(concept == null)
			return false;
		return concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept;
	}
	public boolean isSelectedEigenvectorConcept(String blankName) {
		Concept concept = this.hash.get(blankName);
		if(concept == null)
			return false;
		return concept.getStatus() == ConceptStatus.selectedEigenvectorConcept;
	}
	public boolean isSelectedConcept(String blankName) {
		Concept concept = this.hash.get(blankName);
		if(concept == null)
			return false;
		return concept.getStatus() == ConceptStatus.selectedBetweennessClosenessConcept ||
			   concept.getStatus() == ConceptStatus.selectedEigenvectorConcept;
	}
	public boolean isConcept(String blankName) {
		return this.hash.containsKey(blankName);
	}


	// GET ELEMENTS AND INFORMATION ========================================================================
	
	public Concept getConcept(String blankName) {
		if(this.hash.containsKey(blankName))
			return this.hash.get(blankName);
		else
			return null;
	}
	public int getConnectedComponent(String blankName, int iteration) {
		Concept concept = this.hash.get(blankName);
		if(concept == null)
			return -1;
		return concept.getConnectedComponent(iteration);
	}
	public ConceptStatus getStatus(String blankName) {
		Concept concept = this.hash.get(blankName);
		if(concept == null)
			return ConceptStatus.noStatus;
		return concept.getStatus();
	}
	public Concept getConcept(int pos) {
		return this.list.get(pos);
	}
	
	// toString's() ========================================================================
	
	public String toStringLong() {
		StringBuilder str = new StringBuilder();
		if(this.list != null)
    		for(Concept x : this.list) {
	    		str.append("- ");
    			str.append(x.toStringLong());
	    		str.append("\n");
		    }
		return str.toString();
	}	
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		if(this.list != null)
    		for(Concept x : this.list) {
	    		str.append("- ");
    			str.append(x.toStringShort());
	    		str.append("\n");
		    }
		return str.toString();
	}


}