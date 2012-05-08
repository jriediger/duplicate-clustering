package ideaPortalRetrieval.model.duplicate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import ideaPortalRetrieval.model.vectorspace.*;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.*;
import org.apache.lucene.index.Term;

/**
 * @author Julian Riediger
 *
 */

public class ClusterDuplicates{
	
	/*
	 * 
	 * This class contains methods for clustering, thesaurus creation
	 * and their respective print methods.
	 * 
	 */
	
	private double simThreshold;
	
	private int numDocs;
	private int[] duplicateList;
	private int similarityMode;
	private double[][] simMatrix;
	private double[][] simMatrixTitle;
	private double[][] simMatrixDesc;
	private double[][] duplicateMatrix;
	private SimilarityMatrix similarityMatrix;
	private TreeSet<String>[] termSet;
	
	public ClusterDuplicates(SimilarityMatrix similarityMatrix) throws IOException{
		this.numDocs = similarityMatrix.getNumDocs();
		this.duplicateList = similarityMatrix.getDuplicateList();
		this.simMatrix = similarityMatrix.getSimMatrix();
		this.simMatrixTitle = similarityMatrix.getSimMatrixTitle();
		this.simMatrixDesc = similarityMatrix.getSimMatrixDesc();
		this.duplicateMatrix = similarityMatrix.getDuplicateMatrix();
		this.similarityMode = similarityMatrix.getSimilarityMode();
		this.similarityMatrix = similarityMatrix;
	}
	
	public void clusterCentroid(double simThreshold) throws IOException{
		/*
		 * Description:
		 * 		* Clusters based on a pre-calculated Similarity Matrix
		 *
		 * Parameters: 
		 * 		* double simThreshold - Value has to be 0.0 <= simThreshold < 1.0, e.g. 0.3
		 * 
		 * Configuration:
		 * 		* For no threshold (clustering the whole document set), use value 0.0 
		 * 
		 */
		
		this.simThreshold = simThreshold;
		duplicateList = new int[numDocs]; // array for marking whether a document is a cluster centroid
		double[] maxSimValue = new double[numDocs]; // array for tracking max SimValue per document
		int[] maxSimValuePos = new int[numDocs]; // array for tracking the document ID with max SimValue per document
		int[] numDuplicates = new int[numDocs]; // tracking the number of duplicates per cluster
		
		PriorityQueue<Double>[] clusterSet = new PriorityQueue[numDocs]; // priority queue for tracking cluster centroid's simValues to cluster members

		for (int x=0;x<numDocs;x++){
			clusterSet[x] = new PriorityQueue<Double>();
			duplicateList[x] = 0;
			maxSimValue[x] = 0;
			maxSimValuePos[x] = -1;
			numDuplicates[x] = 0;
		}
		int iter = 0;

		while (iter<2){
			for(int i=0;i<numDocs;i++){
				for (int j=0;j<numDocs;j++){
					
					/*
					 * Reset cluster members' max SimValue if their cluster centroid has 
					 * been removed (2nd iteration only)  
					 */
					if (maxSimValuePos[j]>-1 && maxSimValuePos[maxSimValuePos[j]]>-1){
						maxSimValuePos[j] = -1;
						maxSimValue[j] = 0;
					}
					
					/*
					 * i := document y
					 * j := document x
 					 * 
					 * Check whether the current document x's similarity is 
					 * a) greater than simThreshold
					 * b) skip if self-similarity (i!=j)
					 * c) greater/equals max SimValue for x
					 * d) either document y is cluster centroid/in no cluster at all OR
					 * e) document y's similarity with x is greater than document y's max SimValue	
					 */
					if (simMatrix[i][j]>simThreshold  && i!=j && (simMatrix[i][j]>=maxSimValue[j])
					    && (maxSimValuePos[i]==-1 || 
					    	(simMatrix[i][j]>maxSimValue[i]))){
						
						/*
						 * If j has been cluster centroid of a different cluster, delete priority queue
						 */
						if (!clusterSet[j].isEmpty()){
							while(!clusterSet[j].isEmpty())
								clusterSet[j].remove();
						}
						
						/*
						 * If j has been part of another cluster, remove it from the old (previous) 
						 * cluster's priority queue
						 */
						if (maxSimValuePos[j]!=-1 && !clusterSet[maxSimValuePos[j]].isEmpty()){
							clusterSet[maxSimValuePos[j]].remove(simMatrix[maxSimValuePos[j]][j]);
							
							/*
							 * If j has been the last element of the old (previous) cluster('s priortiy queue), 
							 * delete the old cluster,else assign the next element of the priority queue to 
							 * the old cluster centroid's max SimValue
							 */
							if (!clusterSet[maxSimValuePos[j]].isEmpty()){
								if (maxSimValue[maxSimValuePos[j]] == simMatrix[maxSimValuePos[j]][j]){
									maxSimValue[maxSimValuePos[j]] = clusterSet[maxSimValuePos[j]].peek();
								}
							}else {
								maxSimValue[maxSimValuePos[j]] = 0;
								maxSimValuePos[maxSimValuePos[j]] = -1;
							}
							
						}
						
						/*
						 * Set j's max SimValue to the current simValue
						 */
						maxSimValue[j] = simMatrix[i][j];
						maxSimValuePos[j] = i;
						
						/*
						 * Mark i as new cluster centroid (if it has not already been cluster centroid)
						 */
						maxSimValuePos[i] = -1;
						
						/*
						 * Add current sim Value to to i's priority queue, if higher than old max SimValue, 
						 * assign simValue to i's max SimValue
						 */
						clusterSet[i].add(simMatrix[i][j]);
						if (simMatrix[i][j]>maxSimValue[i]){
							maxSimValue[i] = simMatrix[i][j];
						}
						
					} 
				}
			}
			/*
			 * Increment iter to run 2nd iteration
			 */
			iter++;
		}
		/*
		 * Write max SimValues to duplicate matrix, determine number of duplicates per cluster
		 */
		for (int a=0;a<numDocs;a++){
			if (maxSimValuePos[a]>-1){
				duplicateMatrix[maxSimValuePos[a]][a] = maxSimValue[a];
				numDuplicates[maxSimValuePos[a]]++;
				duplicateList[maxSimValuePos[a]] = 1;
			}
		}
	}
	
	public void printDuplicateClusters(boolean verbose) throws IOException{
		/*
		 * Description:
		 * 		* Show duplicate clusters on command line
		 * 
		 * Parameters:
		 * 		* boolean verbose - if true (default), write output to sysout, 
		 * 							else, write cluster analysis data to file 
		 * 							(for file location see String outputFile)
		 */
		
		String outputFile = "C:\\algoTest.csv"; //Change to desired name and location, only relevant if verbose=false
		
		int clusterIndex = 1;
		int numDuplicates = 0;
		double[] cosSimAvgCluster = new double[numDocs];
		int[] clusterCount = new int[numDocs];
		for (int i=0;i<numDocs;i++){
			cosSimAvgCluster[i] = 0.0;
			clusterCount[i] = 0;
		}
		double cosSimAllAvg = 0;
		double cosSimTitleAvg = 0;
		double cosSimDescAvg = 0;
		Double ratioDupl;
		for (int i=0;i<numDocs;i++){
			if(duplicateList[i]==1){
				if (verbose){
					System.out.println("\n++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					System.out.println("Cluster #"+clusterIndex);
					System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
					System.out.println("DocID: \""+i+"\" | "+similarityMatrix.getIndexReader().document(i).get("title"));
				}
				for (int j=0;j<numDocs;j++){
					if (duplicateMatrix[i][j]>0){
						if (verbose){
							System.out.println("DocID: \""+j+"\" | "+similarityMatrix.getIndexReader().document(j).get("title")+ " | cosSimAll: "+simMatrix[i][j]+" | cosSimTitle: "+simMatrixTitle[i][j]+" | cosSimDesc: "+simMatrixDesc[i][j]);
						}
						clusterCount[i]++;
						cosSimAvgCluster[i] += simMatrix[i][j];
						cosSimAllAvg += simMatrix[i][j];
						cosSimTitleAvg += simMatrixTitle[i][j];
						cosSimDescAvg += simMatrixDesc[i][j];
						numDuplicates++;
					}
				}
				if (verbose){
					System.out.println();
				}
				clusterIndex++;
			}
		}
		System.out.println("\nSimilarity Mode: "+similarityMode);
		System.out.println("Sim Threshold: "+simThreshold);
		System.out.println("Total number of duplicates: "+numDuplicates);
		ratioDupl = (double) numDuplicates/numDocs;
		System.out.println("% of duplicates: "+ratioDupl*100);
		System.out.println("\ncosSimAllAvg: "+cosSimAllAvg/numDuplicates);
		System.out.println("cosSimTitleAvg: "+cosSimTitleAvg/numDuplicates);
		System.out.println("cosSimDescAvg: "+cosSimDescAvg/numDuplicates);
		
		double clusterAvgResult = 0;
		
		for (int i=0;i<numDocs;i++){
			if (clusterCount[i]>0){
				cosSimAvgCluster[i] = cosSimAvgCluster[i]/clusterCount[i];
				clusterAvgResult += cosSimAvgCluster[i];
			}
		}
		
		System.out.println("clusterAvgResult: "+clusterAvgResult/(clusterIndex-1));

		
		FileOutputStream out; 
        PrintStream p;
		
        if (!verbose){
        	try{
            	out = new FileOutputStream(outputFile,true);
                p = new PrintStream(out);
        		p.println(similarityMode+";"+simThreshold+";"+numDuplicates+";"+(clusterIndex-1)+";"+(cosSimTitleAvg/numDuplicates)+";"+(cosSimDescAvg/numDuplicates));
                p.close();
            }
            catch (Exception e){
                System.err.println ("Error writing to file!");
            }
        }
	}
	
	public TreeSet<String>[] createThesaurus(int lowFreqThreshold) throws IOException{
		/*
		 * ***PROTOTYPE method***
		 * 
		 * Description:
		 * 		* Creates thesaurus classes based on document clusters 
		 * 		  (uses duplicateList & duplicateMatrix), for basic 
		 * 		  understanding see chapters 3.4.1 and 3.4.2.
		 * 
		 * Parameters:
		 * 		* int lowFreqThreshold - defines the term frequency threshold that is 
		 * 		  considered for low-frequency terms
		 * 
		 * Configuration:
		 * 		* See chapter 3.4.1 for understanding idea behind low frequency terms, 
		 * 		  threshold depends on document size, literature (Salton) suggests n/100 
		 * 		  as threshold. Use higher threshold if results turn out only few 
		 * 		  terms per thesaurus class. See printThesaurus() for sysout output.
		 */
		List<DocumentVector> documentVectorList = similarityMatrix.getDocumentVectorList();
		int thesaurusClassCounter = 0;
		for (int i=0;i<numDocs;i++){
			if(duplicateList[i]==1){
				thesaurusClassCounter++;
			}
		}
		TreeSet<String>[] termSet = new TreeSet[thesaurusClassCounter];
		this.termSet = termSet;
		int classIndex = 0;
		for (int i=0;i<numDocs;i++){
			if(duplicateList[i]==1){
				termSet[classIndex] = new TreeSet<String>();
				for (int h=0;h<numDocs;h++){
					if (duplicateMatrix[i][h]>0 && i!=h){
						for (int a=0;a<documentVectorList.get(i).getTermVectorTitle().getTerms().length;a++){
							if (similarityMatrix.getIndexReader().docFreq((new Term("title",documentVectorList.get(i).getTermVectorTitle().getTerms()[a])))<=lowFreqThreshold){
								for (int b=0;b<documentVectorList.get(h).getTermVectorTitle().getTerms().length;b++){
									if (documentVectorList.get(h).getTermVectorTitle().getTerms()[b].equals(documentVectorList.get(i).getTermVectorTitle().getTerms()[a])){
										termSet[classIndex].add(documentVectorList.get(h).getTermVectorTitle().getTerms()[b]);
									}
								}
								for (int b=0;b<documentVectorList.get(h).getTermVectorDesc().getTerms().length;b++){
									if (documentVectorList.get(h).getTermVectorDesc().getTerms()[b].equals(documentVectorList.get(i).getTermVectorTitle().getTerms()[a])){
										termSet[classIndex].add(documentVectorList.get(h).getTermVectorDesc().getTerms()[b]);
									}
								}
							}
						}
						for (int a=0;a<documentVectorList.get(i).getTermVectorDesc().getTerms().length;a++){
							if (similarityMatrix.getIndexReader().docFreq((new Term("description",documentVectorList.get(i).getTermVectorDesc().getTerms()[a])))<=lowFreqThreshold){
								for (int b=0;b<documentVectorList.get(h).getTermVectorDesc().getTerms().length;b++){
									if (documentVectorList.get(h).getTermVectorDesc().getTerms()[b].equals(documentVectorList.get(i).getTermVectorDesc().getTerms()[a])){
										termSet[classIndex].add(documentVectorList.get(h).getTermVectorDesc().getTerms()[b]);
									}
								}
								for (int b=0;b<documentVectorList.get(h).getTermVectorTitle().getTerms().length;b++){
									if (documentVectorList.get(h).getTermVectorTitle().getTerms()[b].equals(documentVectorList.get(i).getTermVectorDesc().getTerms()[a])){
										termSet[classIndex].add(documentVectorList.get(h).getTermVectorTitle().getTerms()[b]);
									}
								}
							}
						}
					}
				}

				for (int j=0;j<numDocs;j++){
					if (duplicateMatrix[i][j]>0){
						for (int h=0;h<numDocs;h++){
							if (duplicateMatrix[i][h]>0 && j!=h){
								for (int a=0;a<documentVectorList.get(j).getTermVectorTitle().getTerms().length;a++){
									if (similarityMatrix.getIndexReader().docFreq((new Term("title",documentVectorList.get(j).getTermVectorTitle().getTerms()[a])))<=lowFreqThreshold){
										for (int b=0;b<documentVectorList.get(h).getTermVectorTitle().getTerms().length;b++){
											if (documentVectorList.get(h).getTermVectorTitle().getTerms()[b].equals(documentVectorList.get(j).getTermVectorTitle().getTerms()[a])){
												termSet[classIndex].add(documentVectorList.get(h).getTermVectorTitle().getTerms()[b]);
											}
										}
										for (int b=0;b<documentVectorList.get(h).getTermVectorDesc().getTerms().length;b++){
											if (documentVectorList.get(h).getTermVectorDesc().getTerms()[b].equals(documentVectorList.get(j).getTermVectorTitle().getTerms()[a])){
												termSet[classIndex].add(documentVectorList.get(h).getTermVectorDesc().getTerms()[b]);
											}
										}
									}
								}
								for (int a=0;a<documentVectorList.get(j).getTermVectorDesc().getTerms().length;a++){
									if (similarityMatrix.getIndexReader().docFreq((new Term("description",documentVectorList.get(j).getTermVectorDesc().getTerms()[a])))<=lowFreqThreshold){
										for (int b=0;b<documentVectorList.get(h).getTermVectorDesc().getTerms().length;b++){
											if (documentVectorList.get(h).getTermVectorDesc().getTerms()[b].equals(documentVectorList.get(j).getTermVectorDesc().getTerms()[a])){
												termSet[classIndex].add(documentVectorList.get(h).getTermVectorDesc().getTerms()[b]);
											}
										}
										for (int b=0;b<documentVectorList.get(h).getTermVectorTitle().getTerms().length;b++){
											if (documentVectorList.get(h).getTermVectorTitle().getTerms()[b].equals(documentVectorList.get(j).getTermVectorDesc().getTerms()[a])){
												termSet[classIndex].add(documentVectorList.get(h).getTermVectorTitle().getTerms()[b]);
											}
										}
									}
								}
							}
						}

					}
				}
				classIndex++;
			}
		}
		return termSet;
		
	}
	
	public void printThesaurus(){
		/*
		 * Show thesaurus classes on sysout 
		 *
		 */
		for (int i=0;i<this.termSet.length;i++){
			System.out.println("Thesaurus Class "+ i);
			Iterator<String> iter = this.termSet[i].iterator();
			while (iter.hasNext()){
				System.out.println(iter.next());
			}
			System.out.println();
		}
	}

}
