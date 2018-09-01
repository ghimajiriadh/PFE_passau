package org.fog.scheduling.myLocalSearchAlgorithm;

public class MySwapLocalMove extends MyAbstractLocalMove{
	private int geneIndex1;
	private int geneIndex2;
	
	public MySwapLocalMove(int geneIndex1, int geneIndex2) {
		this.geneIndex1 = geneIndex1;
		this.geneIndex2 = geneIndex2;
	}

	@Override
	public String toString() {
		return "MySwapLocalMove [geneIndex1=" + geneIndex1 + ", geneIndex2=" + geneIndex2 + "]";
	}

	public int getGeneIndex1() {
		return geneIndex1;
	}

	public void setGeneIndex1(int geneIndex1) {
		this.geneIndex1 = geneIndex1;
	}

	public int getGeneIndex2() {
		return geneIndex2;
	}

	public void setGeneIndex2(int geneIndex2) {
		this.geneIndex2 = geneIndex2;
	}
	
	
}
