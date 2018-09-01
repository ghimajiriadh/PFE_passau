package org.fog.scheduling.myGAEntities;
import org.fog.scheduling.gaEntities.Service;



/**
 * @author DungBachViet
 *
 */
public class MyIndividual implements java.io.Serializable{

	private int[] chromosome; // The chain of DNA (or solution)
	private double cost;
	private double time;
	private double fitness = -1;
	private int maxValue; // value domain : [0, maxValue]

	/**
	 * Constructor for initializing an individual randomly
	 * 
	 * @param chromosomeLength : Number of genes in DNA
	 * @param maxValue : value domain of each gene
	 * @param isInitialized : true or false
	 * 
	 */
	public MyIndividual(int chromosomeLength, int maxValue, boolean isInitialized) {
		this.chromosome = new int[chromosomeLength];
		this.maxValue = maxValue;
		
		if (isInitialized) {
			for (int gene = 0; gene < chromosomeLength; gene++) {
				this.setGene(gene, Service.rand(0, maxValue));
			}
		}
	}
	
	/**
	 * Constructor for initializing an individual has the same value on all genes
	 * 
	 * @param chromosomeLength : Number of genes in DNA
	 * @param maxValue : value domain of each gene
	 * @param value : the value assigned for all genes
	 * 
	 */
	public MyIndividual(int chromosomeLength, int maxValue, int value) {
		this.chromosome = new int[chromosomeLength];
		this.maxValue = maxValue;
		for (int gene = 0; gene < chromosomeLength; gene++) {
			this.setGene(gene, value);
		}
	}
	
	
	/**
	 * Print the chain of ADN (a solution) to screen
	 */
	public void printGene() {
		for (int gene = 0; gene < this.getChromosomeLength(); gene++) {
			System.out.print(this.getGene(gene) + " ");
		}
	}

	/**
	 * @return The cost of the solution
	 */
	public double getCost() {
		return cost;
	}
	
	
	/**
	 * Set cost for the solution
	 * 
	 * @param cost
	 */
	public void setCost(double cost) {
		this.cost = cost;
	}

	
	/**
	 * @return The executing time of the solution
	 */
	public double getTime() {
		return time;
	}

	
	/**
	 * Set executing time for the solution
	 * 
	 * @param time
	 */
	public void setTime(double time) {
		this.time = time;
	}

	
	/**
	 * Get fitness of the solution
	 * 
	 * @return The fitness of the solution
	 */
	public double getFitness() {
		return fitness;
	}

	
	/**
	 * Set fitness of the solution
	 * 
	 * @param fitness
	 */
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}
	

	/**
	 * Gets individual's chromosome (the chain of ADN)
	 * 
	 * @return The individual's chromosome
	 */
	public int[] getChromosome() {
		return this.chromosome;
	}

	/**
	 * Gets individual's chromosome length
	 * 
	 * @return The individual's chromosome length
	 */
	public int getChromosomeLength() {
		return this.chromosome.length;
	}

	
	/**
	 * Set the gene value at offset index of chromosome (ADN)
	 * 
	 * @param offset
	 * @param geneValue
	 */
	public void setGene(int offset, int geneValue) {
		this.chromosome[offset] = geneValue;
	}

	/**
	 * Get the gene value at offset index of chromosome
	 * 
	 * @param offset
	 * @return gene
	 */
	public int getGene(int offset) {
		return this.chromosome[offset];
	}

	
	/**
	 * Get max value of gene's value domain
	 * 
	 * @return The max value of gene's value domain
	 */
	public int getMaxValue() {
		return maxValue;
	}

	
	/**
	 * Set the max value of gene's value domain
	 * 
	 * @param maxValue
	 */
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}
	
	

}
