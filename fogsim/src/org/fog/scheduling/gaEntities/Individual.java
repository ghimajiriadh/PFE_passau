package org.fog.scheduling.gaEntities;

public class Individual {
	
	private int[] chromosome;
	private double cost;
	private double time;
	private double fitness = -1;
	private int maxValue;
	
	public Individual(int chromosomeLength, int maxValue) {
		this.chromosome = new int[chromosomeLength];
		this.maxValue = maxValue;
		for (int gene = 0; gene < chromosomeLength; gene++) {
			this.setGene(gene, Service.rand(0, maxValue));
		}
	}
	
	public Individual(int chromosomeLength, int maxValue, int value) {
		this.chromosome = new int[chromosomeLength];
		this.maxValue = maxValue;
		for (int gene = 0; gene < chromosomeLength; gene++) {
			this.setGene(gene, value);
		}
	}
	
	public Individual(int chromosomeLength) {
		this.chromosome = new int[chromosomeLength];
	}
	
	public void printGene() {
		for (int gene = 0; gene < this.getChromosomeLength(); gene++) {
			System.out.print(this.getGene(gene) + " ");
		}
	}
	
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public double getTime() {
		return time;
	}
	public void setTime(double time) {
		this.time = time;
	}
	public double getFitness() {
		return fitness;
	}
	public void setFitness(double fitness) {
		this.fitness = fitness;
	}

	/**
	 * Gets individual's chromosome
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
	 * Set gene at offset
	 * 
	 * @param gene
	 * @param offset
	 * @return gene
	 */
	public void setGene(int offset, int gene) {
		this.chromosome[offset] = gene;
	}

	/**
	 * Get gene at offset
	 * 
	 * @param offset
	 * @return gene
	 */
	public int getGene(int offset) {
		return this.chromosome[offset];
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	
	
}
