package org.fog.scheduling.myLocalSearchAlgorithm;

public class MyAssignLocalMove extends MyAbstractLocalMove {
	
	private int geneIndex;// The index of gene in the chain of ADN
	private int geneValue;// The value assigned to the gene at geneIndex position

	public MyAssignLocalMove(int geneIndex, int geneValue) {
		this.geneIndex = geneIndex;
		this.geneValue = geneValue;
	}

	
	
	public int getGeneIndex() {
		return geneIndex;
	}


	public void setGeneIndex(int geneIndex) {
		this.geneIndex = geneIndex;
	}


	public int getGeneValue() {
		return geneValue;
	}


	public void setGeneValue(int geneValue) {
		this.geneValue = geneValue;
	}


}
