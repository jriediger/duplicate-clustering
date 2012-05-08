package ideaPortalRetrieval.model.duplicate;

import org.apache.lucene.search.QueryTermVector;
import org.apache.lucene.analysis.Analyzer;
import ideaPortalRetrieval.model.vectorspace.DocumentVector;
import ideaPortalRetrieval.model.vectorspace.SimilarityMatrix;
import java.io.*;
import java.util.TreeSet;

/**
 * @author Julian Riediger
 *
 */

public class SearchDuplicates {
	
	/*
	 * 
	 * This class is the "search" class, solely called from the portal backend.
	 * 
	 * IMPORTANT: Make sure that the same analyzer (stemmer) is used here as for indexing,
	 * otherwise duplicate recognition may not work properly.
	 * 
	 */
	
	private Analyzer analyzer;
	private SimilarityMatrix similarityMatrix;
	private double simThreshold;
	
	public SearchDuplicates(SimilarityMatrix simMatrix, Analyzer analyzer, double simThreshold){
		this.similarityMatrix = simMatrix;
		this.analyzer = analyzer;
		this.simThreshold = simThreshold;
	}
	
	public TreeSet<DuplicateDocument> findSimilar(String title, String desc) throws IOException{
		/*
		 * Description:
		 * 		* Creates a vector representation of entered title and description and returns
		 * 		  an ordered result set. Result set may be emtpy, if no duplicates could be found.
		 * 
		 * Parameters:
		 * 		* String title - title of the entered idea
		 * 		* String desc - body of the entered idea
		 */
		DocumentVector queryVector = this.createDocumentVector(title,desc);
		double[] cosSim = new double[5];
		TreeSet<DuplicateDocument> resultSet = new TreeSet<DuplicateDocument>();
		for (int i=0;i<similarityMatrix.getNumDocs();i++){
			cosSim = similarityMatrix.cosineSimilarity(queryVector, similarityMatrix.getDocumentVectorList().get(i));
			if (cosSim[4]>simThreshold){
				resultSet.add(new DuplicateDocument(cosSim[4],i));
			}
		}
		return resultSet;
	}
	
	private DocumentVector createDocumentVector(String title, String desc) throws IOException{
		/*
		 * 
		 * Description:
		 * 		* Construct a new Document Vector. Method is called from findSimilar().
		 * 
		 * Parameters:
		 * 		* String title - title of the entered idea
		 * 		* String desc - body of the entered idea
		 * 
		 */
		QueryTermVector ideaTitleVector = new QueryTermVector(title, analyzer);
		QueryTermVector ideaDescVector = new QueryTermVector(desc, analyzer);
		DocumentVector docVec = new DocumentVector(ideaTitleVector, similarityMatrix.computeTf_Idf(ideaTitleVector,"title",true),ideaDescVector, similarityMatrix.computeTf_Idf(ideaDescVector,"description",true));
		return docVec; 
	}
	
}
