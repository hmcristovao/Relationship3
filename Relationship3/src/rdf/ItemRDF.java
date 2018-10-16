package rdf;

import java.io.Serializable;

import main.WholeSystem;
import user.Concept;

public class ItemRDF implements Serializable {
	
	private String fullName;           // complete: contains address 
	private String shortUnderlineName; // still contains underline
	private String shortBlankName;     // underline change for blank
	
	public ItemRDF(String fullName) {
		this.fullName           = fullName;
		this.shortUnderlineName = ItemRDF.doShortName(fullName);
		this.shortBlankName     = Concept.underlineToBlank(this.shortUnderlineName);
	}
	public String getFullName() {
		return this.fullName;
	}
	public String getShortUnderlineName() {
		return this.shortUnderlineName;
	}
	public String getShortBlankName() {
		return this.shortBlankName;
	}
	
	// shortening a name, normally cut the address part and keep on the suffix until the bar
	static private String doShortName(String longName) {
		// only shortening if base is DBPedia (if base is my knowledge then do nothing)
		if(WholeSystem.configTable.getInt("knowledgeBasePlace") == 1) {
			String shortName;
			int positionLastBar = longName.lastIndexOf("/");
			shortName = longName.substring(positionLastBar+1);
			return shortName;
		}
		return longName;
	}
	
	@Override
	public String toString() {
		return this.fullName + " (" + this.shortBlankName + ")";
	}
}
