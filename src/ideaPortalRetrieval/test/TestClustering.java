package ideaPortalRetrieval.test;

import java.io.*;
import org.apache.lucene.store.*;
import ideaPortalRetrieval.model.*;
import ideaPortalRetrieval.model.indexer.*;
import ideaPortalRetrieval.model.duplicate.*;
import ideaPortalRetrieval.model.vectorspace.*;

/**
 * @author Julian Riediger
 *
 */

public class TestClustering {
	
	/*
	 * 
	 * This class runs the clustering algorithm and presents the produced clusters on sysout.
	 * 
	 */
	
	public XMLParser parser;
	public Indexer indexer;
	public SimilarityMatrix simi;
	public ClusterDuplicates duplicate;
	
	public void startParserModule(Indexer indexer) throws IOException{
		parser = new XMLParser(indexer);
	}
	
	public void startIndexerModule(String indexFileLocation) throws IOException{
		indexer = new Indexer(indexFileLocation);
	}
	
	public static void main(String[] args) throws Exception{
		TestClustering ideaPortalRetrieval = new TestClustering();
		String dataSourceLocation = "C:\\Users\\Julian\\Documents\\Studium\\Bachelor\\Bachelor Thesis\\Project\\Data\\MyStarbucksIdeasXML"; //Specify the location of (XML) documents here
		String indexLocation = "C:\\Users\\Julian\\Documents\\Studium\\Bachelor\\Bachelor Thesis\\Project\\Index"; //Specify Lucene Index Location here
		//String indexLocation = "C:\\Index"; //Specify Lucene Index Location here
		
		//FSDirectory directory = FSDirectory.getDirectory(indexLocation);
		
		/*
		 * Uncomment the following lines to reindex all (XML) documents on each run of the main method
		 */
		
		FSDirectory directory = FSDirectory.getDirectory(new File(indexLocation));
		
		ideaPortalRetrieval.startIndexerModule(indexLocation);
		ideaPortalRetrieval.startParserModule(ideaPortalRetrieval.indexer);
		
		ideaPortalRetrieval.parser.parse(dataSourceLocation);
		
		//Indexing finished
		
		ideaPortalRetrieval.simi = new SimilarityMatrix(directory, 3); // 2nd parameter is similarity mode, see class SimilarityMatrix for details
		ideaPortalRetrieval.simi.openIndex();
		ideaPortalRetrieval.simi.createMatrix();
		ideaPortalRetrieval.duplicate = new ClusterDuplicates(ideaPortalRetrieval.simi);
		ideaPortalRetrieval.duplicate.clusterCentroid(0.3);
		
		/*
		 * 
		 * Uncomment the following lines to create and show thesaurus classes.
		 * Please note, that clusterCentroid() has to be called always in advance.
		 * 
		 */
		//ideaPortalRetrieval.duplicate.createThesaurus(20); //Parameter is low frequency threshold, see method description for details
		//ideaPortalRetrieval.duplicate.printThesaurus();
		
		/*
		 * Comment the following line to not show any output of the algorithm
		 */
		ideaPortalRetrieval.duplicate.printDuplicateClusters(true);

		//SimilarityMatrix created
		
		ideaPortalRetrieval.simi.closeIndex();
	}

}
