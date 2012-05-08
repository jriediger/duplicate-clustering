package ideaPortalRetrieval.test;

import java.io.*;

import org.apache.lucene.store.*;

import ideaPortalRetrieval.model.indexer.*;
import ideaPortalRetrieval.model.duplicate.*;
import ideaPortalRetrieval.model.stemmer.PorterAnalyzer;
import ideaPortalRetrieval.model.vectorspace.*;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * @author Julian Riediger
 *
 */

public class TestSearch {
	
	/*
	 * 
	 * This class is a test class to test duplicate detection as used by DuplicateServlet. 
	 * 
	 */
	
	public XMLParser parser;
	public Indexer indexer;
	public SimilarityMatrix simi;
	public ClusterDuplicates duplicate;
	public SearchDuplicates searchDupl;
	
	public void startParserModule(Indexer indexer) throws IOException{
		parser = new XMLParser(indexer);
	}
	
	public void startIndexerModule(String indexFileLocation) throws IOException{
		indexer = new Indexer(indexFileLocation);
	}
	
	
	public static void main(String[] args) throws Exception{
		TestSearch ideaPortalRetrieval = new TestSearch();
		String dataSourceLocation = "C:\\MyStarbucksIdeasXML"; //Specify the location of (XML) documents here
		String indexLocation = "C:\\Index"; //Specify Lucene Index Location here
		
		FSDirectory directory = FSDirectory.getDirectory(indexLocation);
		
		/*
		 * 
		 * Uncomment following lines to avoid reindexing of XML documents wac
		 */
		//FSDirectory directory = FSDirectory.getDirectory(new File(indexLocation));
		
		//ideaPortalRetrieval.startIndexerModule(indexLocation);
		//ideaPortalRetrieval.startParserModule(ideaPortalRetrieval.indexer);
		
		//ideaPortalRetrieval.parser.parse(dataSourceLocation);
		
		//Indexing finished
		
		ideaPortalRetrieval.simi = new SimilarityMatrix(directory, 3);
		ideaPortalRetrieval.searchDupl = new SearchDuplicates(ideaPortalRetrieval.simi,(new PorterAnalyzer()),0.3);
		
		
		ideaPortalRetrieval.simi.openIndex();
		
		/*
		 * Search for duplicates based on document with title "test" and empty body
		 */
		TreeSet<DuplicateDocument> result = ideaPortalRetrieval.searchDupl.findSimilar("test","");
		Iterator<DuplicateDocument> resultIterator = result.iterator();
		
		while(resultIterator.hasNext()){
			DuplicateDocument duplDoc = resultIterator.next();
			System.out.println(ideaPortalRetrieval.simi.getIndexReader().document(duplDoc.getDocID()).get("title")+" "+duplDoc.getCosSim());
		}
		
		ideaPortalRetrieval.simi.closeIndex();
		
	}

}
