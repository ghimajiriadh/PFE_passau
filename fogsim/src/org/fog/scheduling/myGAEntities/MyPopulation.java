package org.fog.scheduling.myGAEntities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fog.scheduling.myGAEntities.MyIndividual;

public class MyPopulation {
	private List<MyIndividual> population;
	private double populationFitness = -1;

	/**
	 * Initializes blank population of individuals
	 * 
	 * @param populationSize : The number of individuals in the population
	 */
	public MyPopulation() {
		// Initial population
		this.population = new ArrayList<MyIndividual>();
	}
	
	
	/**
	 * Initializes the population from the given new population
	 * 
	 * @param newPopulation
	 */
	public MyPopulation(List<MyIndividual> newPopulation) {
		// Initial population
		this.population = newPopulation;
	}

	/**
	 * Initializes population of individuals
	 * 
	 * @param populationSize : The number of individuals in the population
	 * @param chromosomeLength : The size of each individual's chromosome
	 * @param maxValue : The value domain of each gene [0, maxValue]
	 */
	public MyPopulation(int populationSize, int chromosomeLength, int maxValue) {
		// Initialize the population as an array of individuals
		this.population = new ArrayList<MyIndividual>();

		// Create each individual in turn
		for (int individualCount = 0; individualCount < populationSize; individualCount++) {
			// Create an individual, initializing its chromosome to the give length
			MyIndividual individual = new MyIndividual(chromosomeLength, maxValue, true);

			// Add individual to population
			this.population.add(individual);
		}
		
	}

	
	/**
	 * Print the information of all individuals in population
	 */
	public void printPopulation() {
		for (int individualCount = 0; individualCount < this.population.size(); individualCount++) {
			System.out.println("\nIndividual " + individualCount + ": ");
			this.population.get(individualCount).printGene();
			System.out.print("Fitness: " + this.population.get(individualCount).getFitness()
					+ "  \\\\Time execution :" + this.population.get(individualCount).getTime()
					+ "  \\\\Total cost: " + this.population.get(individualCount).getCost());
		}
	}
	
	
	/**
	 * Get population of all individuals
	 * 
	 * @return Population of all individuals
	 */
	public List<MyIndividual> getPopulation(){
		return this.population;
	}

	/**
	 * Set population
	 * 
	 * @param population
	 */
	public void setPopulation(List<MyIndividual> population) {
		this.population = population;
	}
	


	/**
	 * Sort population in descending order
	 */
	public void sortPopulation() {
		Collections.sort(this.population, new Comparator<MyIndividual>() {
			@Override
			public int compare(MyIndividual o1, MyIndividual o2) {
//				return o1.getFitness() > o2.getFitness() ? -1 : 1;
				
				if (o1.getFitness() > o2.getFitness()) {
					return -1;
				} else if (o1.getFitness() < o2.getFitness()) {
					return 1;
				}
				return 0;
			}
		});
	}

	/**
	 * Set population's fitness total
	 * 
	 * @param fitness : Population's fitness total
	 */
	public void setPopulationFitness(double fitness) {
		this.populationFitness = fitness;
	}

	
	
	/**
	 * Get population's fitness total
	 * 
	 * @return populationFitness
	 */
	public double getPopulationFitness() {
		return this.populationFitness;
	}

	
	/**
	 * Set individual at offset index of population ArrayList
	 * 
	 * @param individual : A new individual
	 * @param offset : Index of the element to replace
	 */
	public void setIndividual(int offset, MyIndividual individual) {
		population.set(offset, individual);
	}

	
	/**
	 * Get individual at offset index
	 * 
	 * @param offset
	 * @return individual
	 */
	public MyIndividual getIndividual(int offset) {
		return population.get(offset);
	}
//	
//	/**
//	 * Shuffles the population in-place
//	 * 
//	 * @param void
//	 * @return void
//	 */
//	public void shuffle() {
//		Random rnd = new Random();
//		for (int i = population.length - 1; i > 0; i--) {
//			int index = rnd.nextInt(i + 1);
//			Individual a = population[index];
//			population[index] = population[i];
//			population[i] = a;
//		}
//	}

	/**
	 * Get size of population
	 * 
	 * @return size of population
	 */
	public int size() {
		// TODO Auto-generated method stub
		return population.size();
	}
}
