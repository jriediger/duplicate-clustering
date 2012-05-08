package ideaPortalRetrieval.model.vectorspace;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.*;
import java.io.*;
import java.util.*;

/**
 * @author Julian Riediger
 *
 */

public class SimilarityMatrix{
	
	/*
	 * 
	 * This class incorporates the similarity matrix as well IDF and
	 * cosine similarity calculations.
	 * 
	 */
	
	private IndexReader indexReader;
	private Directory indexDirectory;
	private List<DocumentVector> documentVectorList;
	private double[][] simMatrix;
	private double[][] simMatrixTitle;
	private double[][] simMatrixDesc;
	private double[][] duplicateMatrix;
	private int[] duplicateList;
	private int numDocs;
	private Integer sessionCounter;
	
	private TreeSet<String>[] termSet;

	private int similarityMode;
	
	public double[][] getSimMatrix(){
		return this.simMatrix;
	}
	
	public double[][] getSimMatrixTitle(){
		return this.simMatrixTitle;
	}
	
	public double[][] getSimMatrixDesc(){
		return this.simMatrixDesc;
	}
	
	public double[][] getDuplicateMatrix(){
		return this.duplicateMatrix;
	}
	
	public int[] getDuplicateList(){
		return this.duplicateList;
	}
	
	public int getNumDocs(){
		return this.numDocs;
	}
	
	public int getSimilarityMode(){
		return this.similarityMode;
	}
	
	public IndexReader getIndexReader(){
		return this.indexReader;
	}
	
	public Directory getIndexDirectory(){
		return this.indexDirectory;
	}
	
	public List<DocumentVector> getDocumentVectorList(){
		return this.documentVectorList;
	}
	
	public SimilarityMatrix(Directory indexDirectory, int similarityMode) throws IOException{
		this.sessionCounter = 0;
		this.indexDirectory = indexDirectory;
		this.openIndex();
		this.numDocs = this.indexReader.numDocs();
		this.similarityMode = similarityMode;
		this.duplicateMatrix = new double[numDocs][numDocs];
		this.simMatrix = new double[numDocs][numDocs];
		this.simMatrixTitle = new double[numDocs][numDocs];
		this.simMatrixDesc = new double[numDocs][numDocs];
		for (int i=0;i<numDocs;i++){
			for (int j=0;j<numDocs;j++){
				this.duplicateMatrix[i][j] = 0;
				this.simMatrix[i][j] = 0;
				this.simMatrixTitle[i][j] = 0;
				this.simMatrixDesc[i][j] = 0;
			}
		}
		
		this.createDocumentVectors();
		this.closeIndex();
	}
	
	public synchronized void openIndex() throws IOException{
		if (sessionCounter==0){
			this.indexReader = IndexReader.open(indexDirectory, true);
			System.out.println("Opening index...");
		}
		sessionCounter++;
	}
	
	public synchronized void closeIndex() throws IOException{
		sessionCounter--;
		if (sessionCounter==0){
			this.indexReader.close();
			System.out.println("Closing index...");
		}
	}
	
	public void updateDocumentVectors() throws IOException{
		this.numDocs = this.indexReader.numDocs();
		this.createDocumentVectors();
	}
	
	private void createDocumentVectors() throws IOException{
		
		/* 
		 * Description:
		 * 		* Stores the total number of documents indexed in numDocs
		 * 		  and creates a list of Document Vectors containing
		 * 		  the TermFreqVector and the IDF per vector.
		 */
		
		documentVectorList = new LinkedList<DocumentVector>();
		for (int i=0;i<numDocs;i++){
			documentVectorList.add(new DocumentVector(indexReader.getTermFreqVector(i,"title"),computeTf_Idf(indexReader.getTermFreqVector(i,"title"),"title",false),indexReader.getTermFreqVector(i,"description"),computeTf_Idf(indexReader.getTermFreqVector(i,"description"),"description",false)));	
		}
	}
	
	public void createMatrix() throws Exception{
		
		/* 
		 * Description:
		 * 		* Creates a similarity matrix (NxN).
		 *   
		 */
		
		double[] cosineSim = new double[5];
		for (int i=0;i<numDocs;i++){
			for (int j=i;j<numDocs;j++){
				cosineSim = cosineSimilarity(documentVectorList.get(i), documentVectorList.get(j));
				simMatrix[i][j]= cosineSim[4];
				simMatrix[j][i]= cosineSim[4];
				simMatrixTitle[i][j]= cosineSim[0];
				simMatrixTitle[j][i]= cosineSim[0];
				simMatrixDesc[i][j]= cosineSim[1];
				simMatrixDesc[j][i]= cosineSim[1];
			}
		}
	}
		
	public double[] cosineSimilarity(DocumentVector doc1, DocumentVector doc2) throws IOException{
		
		/* 
		 * Description:
		 * 		* Determines the longest of the two document vectors and calculates
		 * 		  v1*v1, v2*v2 as well as v1*v2 (dot products).
		 * 		  Then calculates the cosine similarity, defined as: 
		 * 		  v1*v2/Sqrt(v1*v1)*Sqrt(v2*v2).
		 * 
		 * Parameters:
		 * 		* DocumentVector - Two instances of document vectors for which a cosine similarity is calculated
		 * 
		 */
		
		double[] cosineSim = new double[5];
		
		double[] tf_Idf1;
		double[] tf_Idf2;
		TermFreqVector termFreqVector1;
		TermFreqVector termFreqVector2;
		
		if (doc1.getTf_IdfTitle()==null || doc2.getTf_IdfTitle()==null){
			cosineSim[0] = 0;
		} else {
			
			if (doc1.getTf_IdfTitle().length<doc2.getTf_IdfTitle().length){
				tf_Idf1 = doc2.getTf_IdfTitle();
				tf_Idf2 = doc1.getTf_IdfTitle();
				termFreqVector1 = doc2.getTermVectorTitle();
				termFreqVector2 = doc1.getTermVectorTitle();
			}
			else{
				tf_Idf1 = doc1.getTf_IdfTitle();
				tf_Idf2 = doc2.getTf_IdfTitle();
				termFreqVector1 = doc1.getTermVectorTitle();
				termFreqVector2 = doc2.getTermVectorTitle();
			}
			cosineSim[0] = calculateSimilarity(tf_Idf1, tf_Idf2, termFreqVector1, termFreqVector2);
		}
		
		if (doc1.getTf_IdfDesc()==null || doc2.getTf_IdfDesc()==null){
			cosineSim[1] = 0;
		} else {
			
			if (doc1.getTf_IdfDesc().length<doc2.getTf_IdfDesc().length){
				tf_Idf1 = doc2.getTf_IdfDesc();
				tf_Idf2 = doc1.getTf_IdfDesc();
				termFreqVector1 = doc2.getTermVectorDesc();
				termFreqVector2 = doc1.getTermVectorDesc();
			}
			else{
				tf_Idf1 = doc1.getTf_IdfDesc();
				tf_Idf2 = doc2.getTf_IdfDesc();
				termFreqVector1 = doc1.getTermVectorDesc();
				termFreqVector2 = doc2.getTermVectorDesc();
			}
			
			cosineSim[1] = calculateSimilarity(tf_Idf1, tf_Idf2, termFreqVector1, termFreqVector2);
		}
		
		if (doc1.getTf_IdfTitle()==null || doc2.getTf_IdfDesc()==null){
			cosineSim[2] = 0;
		} else {

			if (doc1.getTf_IdfTitle().length<doc2.getTf_IdfDesc().length){
				tf_Idf1 = doc2.getTf_IdfDesc();
				tf_Idf2 = doc1.getTf_IdfTitle();
				termFreqVector1 = doc2.getTermVectorDesc();
				termFreqVector2 = doc1.getTermVectorTitle();
			}
			else{
				tf_Idf1 = doc1.getTf_IdfTitle();
				tf_Idf2 = doc2.getTf_IdfDesc();
				termFreqVector1 = doc1.getTermVectorTitle();
				termFreqVector2 = doc2.getTermVectorDesc();
			}

			cosineSim[2] = calculateSimilarity(tf_Idf1, tf_Idf2, termFreqVector1, termFreqVector2);
		}
		
		if (doc1.getTf_IdfDesc()==null || doc2.getTf_IdfTitle()==null){
			cosineSim[3] = 0;
		} else {
			if (doc1.getTf_IdfDesc().length<doc2.getTf_IdfTitle().length){
				tf_Idf1 = doc2.getTf_IdfTitle();
				tf_Idf2 = doc1.getTf_IdfDesc();
				termFreqVector1 = doc2.getTermVectorTitle();
				termFreqVector2 = doc1.getTermVectorDesc();
			}
			else{
				tf_Idf1 = doc1.getTf_IdfDesc();
				tf_Idf2 = doc2.getTf_IdfTitle();
				termFreqVector1 = doc1.getTermVectorDesc();
				termFreqVector2 = doc2.getTermVectorTitle();
			}
			
			cosineSim[3] = calculateSimilarity(tf_Idf1, tf_Idf2, termFreqVector1, termFreqVector2);
		}
		
		/*
		 * 
		 * Depending on the chosen similarity mode, the weighting can be influenced. Default mode is 3,
		 * weighing title and body equally. similarityMode is an instance-wide variable, which is set
		 * during construction of this class.
		 * 
		 */
		
		switch (similarityMode){
			case 1:
				//2 * title + body
				cosineSim[4] = (4*cosineSim[0]+2*cosineSim[1]+cosineSim[2]+cosineSim[3])/8;
				return cosineSim;
			case 2:
				//title + 2 * body
				cosineSim[4] = (2*cosineSim[0]+4*cosineSim[1]+cosineSim[2]+cosineSim[3])/8;
				return cosineSim;
			case 3:
				//title + body
				cosineSim[4] = (2*cosineSim[0]+2*cosineSim[1]+cosineSim[2]+cosineSim[3])/6;
				return cosineSim;
			case 4:
				//only title
				cosineSim[4] = cosineSim[0];
				return cosineSim;
			case 5:
				//only body
				cosineSim[4] = cosineSim[1];
				return cosineSim;
			default:
				System.out.println("ERROR: Could not read similarity mode");
				return null;
		}
		
	}
	
	private double calculateSimilarity(double[] tf_Idf1, double[] tf_Idf2, TermFreqVector termFreqVector1, TermFreqVector termFreqVector2){
		
		/*
		 * Description:
		 * 		* Actual calculation of cosine similarity.
		 * 
		 * Parameters:
		 * 		* double[] tf_Idf1 - tf-Idf of term vector 1
		 * 		* double[] tf_Idf2 - tf-Idf of term vector 2
		 * 		* TermFreqVector termFreqVector1 - see above
		 * 		* TermFreqVector termFreqVector2 - see above
		 */
		
		double dotProductV1V2 = 0;
		double dotProductV1 = 0;
		double dotProductV2 = 0;
		
		for (int x=0;x<tf_Idf1.length;x++){
			dotProductV1 +=tf_Idf1[x]*tf_Idf1[x];
		}

		for (int y=0;y<tf_Idf2.length;y++){
			dotProductV2 += tf_Idf2[y]*tf_Idf2[y];
		}

		for (int i=0;i<tf_Idf1.length;i++){		
			for (int j=0;j<tf_Idf2.length;j++){
				if (termFreqVector1.getTerms()[i].equals(termFreqVector2.getTerms()[j])){
					dotProductV1V2 += tf_Idf1[i]*tf_Idf2[j];
				}
			}
		}
		
		return dotProductV1V2/(Math.sqrt(dotProductV1)*(Math.sqrt(dotProductV2)));
	}
	
	public double[] computeTf_Idf(TermFreqVector termVector, String field, boolean query) throws IOException{
		
		/* 
		 * Description:
		 * 		* Calculates the inverted document frequency per term, defined as
		 * 		  idf = log (numDocs/df) and multiplies it with the term frequency in 
		 * 		  the current document. Document frequency (df) measures the number of
		 * 		  appearances of a given term in all documents of the index. numDocs is the 
		 * 		  total number of documents in the index.
		 * 
		 * Parameters:
		 * 		* TermFreqVector termVector - see Lucene API for detailed description, stores all terms of a document
		 * 		  String field - specifies whether termVector includes title or body
		 * 		  boolean query - if query=true the input term vector is a query vector, resulting in an adjusted calculation of tf_idf 
		 * 
		 */
		if (termVector==null){
			return null;
		} else{
			double[] tf_idf = new double[termVector.size()];
			int queryDoc = 0;
			if (query){
				queryDoc = 1;
			}
			for (int i=0;i<termVector.size();i++){
				tf_idf[i]=termVector.getTermFrequencies()[i]*Math.log((numDocs+queryDoc)/(indexReader.docFreq((new Term(field,termVector.getTerms()[i])))+queryDoc));
			}
			return tf_idf;
		}
	}
	
	public void printSimMatrix(String field) throws IOException{
		
		/*
		 * 
		 * Description:
		 * 		* Prints similarity matrix to a CVS file.
		 * Parameters:
		 * 		* String field - either "title" or "body"
		 * 
		 */
		
		String filePath = "C:\\simMatrix_"+field+".csv"; //configure if necessary
		FileOutputStream out; 
        PrintStream p;
        double[][] printMatrix = new double[numDocs][numDocs];
        if (field.equals("title"))
        	printMatrix = simMatrixTitle;
        else if (field.equals("description"))
        	printMatrix = simMatrixDesc;
        else if (field.equals("all"))
        	printMatrix = simMatrix;
        try{
        	out = new FileOutputStream(filePath);
            p = new PrintStream(out);
            for (int i=0;i<numDocs;i++){
    			for (int j=0;j<numDocs;j++){
    				p.print(printMatrix[i][j]+";");
    			}
    			p.println();
    		}
            p.close();
        }
        catch (Exception e){
            System.err.println ("Error writing to file!");
        }
	}
	
	public void printDuplMatrix() throws IOException{
		
		/*
		 * 
		 * Description:
		 * 		* Prints similarity matrix to a CVS file.
		 * Parameters:
		 * 		* String field - either "title" or "body"
		 * 
		 */
		String filePath = "C:\\duplMatrix.csv"; //configure if necessary
		FileOutputStream out; 
        PrintStream p;
        try{
        	out = new FileOutputStream(filePath);
            p = new PrintStream(out);
            for (int i=0;i<numDocs;i++){
    			for (int j=0;j<numDocs;j++){
    				p.print(duplicateMatrix[i][j]+";");
    			}
    			p.println();
    		}
            p.close();
        }
        catch (Exception e){
            System.err.println ("Error writing to file!");
        }
	}
	
	public void printSimilarity(int doc1, int doc2){
		System.out.println("cosSimAll: "+simMatrix[doc1][doc2]);
		System.out.println("cosSimTitle: "+simMatrixTitle[doc1][doc2]);
		System.out.println("cosSimDesc: "+simMatrixDesc[doc1][doc2]);
	}

}
