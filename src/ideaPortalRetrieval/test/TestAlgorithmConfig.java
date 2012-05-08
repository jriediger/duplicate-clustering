package ideaPortalRetrieval.test;

import ideaPortalRetrieval.model.vectorspace.SimilarityMatrix;
import ideaPortalRetrieval.model.duplicate.ClusterDuplicates;

import java.io.IOException;

import org.apache.lucene.store.FSDirectory;

public class TestAlgorithmConfig {
	
	/*
	 * 
	 * This class is a test class to evaluate the clustering algorithm's performance
	 * with various configurations as described in chapter 3.3 based on an internal criterion. 
	 * The output file location is specified in ClusterDuplicates/printDuplicateClusters(). 
	 * To run the test simply start the main method.
	 * 
	 */
	
	public SimilarityMatrix simi;
	public ClusterDuplicates duplicates;
	
	public static void main(String[] args) throws Exception{
		TestAlgorithmConfig ideaPortalRetrieval = new TestAlgorithmConfig();
		String indexLocation = "C:\\Index"; //Specify Lucene's index location here
		
		FSDirectory directory = FSDirectory.getDirectory(indexLocation);
		for (int i=1;i<6;i++){
			for (double simThreshold=0.1;simThreshold<1;simThreshold+=0.2){
				ideaPortalRetrieval.simi = new SimilarityMatrix(directory,i);
				ideaPortalRetrieval.simi.createMatrix();
				ideaPortalRetrieval.duplicates = new ClusterDuplicates(ideaPortalRetrieval.simi);
				ideaPortalRetrieval.duplicates.clusterCentroid(simThreshold);
				ideaPortalRetrieval.duplicates.printDuplicateClusters(false);
			}
			
		}

	}

}