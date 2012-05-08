package ideaPortalRetrieval.model.vectorspace;

import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.*;
import java.io.*;
import java.util.*;

/**
 * @author Julian Riediger
 *
 */

public class DocumentVector{
	
	private double[] tf_idfTitle;
	private double[] tf_idfDesc;
	private TermFreqVector termVectorTitle;
	private TermFreqVector termVectorDesc;
	
	public DocumentVector(TermFreqVector termVectorTitle, double[] tf_idfTitle, TermFreqVector termVectorDesc, double[] tf_idfDesc){
		this.termVectorTitle = termVectorTitle;
		this.tf_idfTitle = tf_idfTitle;
		this.termVectorDesc = termVectorDesc;
		this.tf_idfDesc = tf_idfDesc;
	}
	
	public TermFreqVector getTermVectorTitle(){
		return this.termVectorTitle;
	}
	
	public TermFreqVector getTermVectorDesc(){
		return this.termVectorDesc;
	}
	
	public double[] getTf_IdfTitle(){
		return this.tf_idfTitle;
	}
	
	public double[] getTf_IdfDesc(){
		return this.tf_idfDesc;
	}

}
