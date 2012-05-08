package ideaPortalRetrieval.model.duplicate;

/**
 * @author Julian Riediger
 *
 */

public class DuplicateDocument implements Comparable<DuplicateDocument>{
	
	double cosSim;
	int docID;
	
	public DuplicateDocument(double cosSim, int docID){
		this.cosSim = cosSim;
		this.docID = docID;
	}
	
	public double getCosSim(){
		return this.cosSim;
	}
	
	public int getDocID(){
		return this.docID;
	}
	
	public int compareTo(DuplicateDocument doc){
		if (this.cosSim>doc.getCosSim())
			return -1;
		else
			return 1;
	}

}
