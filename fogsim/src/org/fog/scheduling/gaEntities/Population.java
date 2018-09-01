package org.fog.scheduling.gaEntities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Population {
	private List<Individual> population;
	private double populationFitness = -1;

	/**
	 * Initializes blank population of individuals
	 * 
	 * @param populationSize
	 *            The number of individuals in the population
	 */
	public Population() {
		// Initial population
		this.population = new ArrayList<Individual>();
	}

	/**
	 * Initializes population of individuals
	 * 
	 * @param populationSize
	 *            The number of individuals in the population
	 * @param chromosomeLength
	 *            The size of each individual's chromosome
	 */
	public Population(int populationSize, int chromosomeLength, int maxValue) {
		// Initialize the population as an array of individuals
		this.population = new ArrayList<Individual>();

		// Create each individual in turn
		for (int individualCount = 0; individualCount < populationSize; individualCount++) {
			// Create an individual, initializing its chromosome to the give length
			Individual individual = new Individual(chromosomeLength, maxValue);
			// Add individual to population
			this.population.add(individual);
		}
		
	}

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
	 * Get individuals from the population
	 * 
	 * @return individuals Individuals in population
	 */
	public List<Individual> getPopulation(){
		return this.population;
	}

	public void setPopulation(List<Individual> population) {
		this.population = population;
	}
	
	/**
	 * Find an individual in the population by its fitness
	 * 
	 * This method lets you select an individual in order of its fitness. This
	 * can be used to find the single strongest individual (eg, if you're
	 * testing for a solution), but it can also be used to find weak individuals
	 * (if you're looking to cull the population) or some of the strongest
	 * individuals (if you're using "elitism").
	 * 
	 * @param offset
	 *            The offset of the individual you want, sorted by fitness. 0 is
	 *            the strongest, population.length - 1 is the weakest.
	 * @return individual Individual at offset
	 */
	public Individual getFittest(int offset) {
//		sortPopulation();

		// Return the fittest individual
		return this.population.get(offset);
	}

	public void sortPopulation() {
		// Order population by fitness
		Collections.sort(this.population, new Comparator<Individual>() {
			@Override
			public int compare(Individual o1, Individual o2) {
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
	 * Set population's group fitness
	 * 
	 * @param fitness
	 *            The population's total fitness
	 */
	public void setPopulationFitness(double fitness) {
		this.populationFitness = fitness;
	}

	/**
	 * Get population's group fitness
	 * 
	 * @return populationFitness The population's total fitness
	 */
	public double getPopulationFitness() {
		return this.populationFitness;
	}

	/**
	 * Set individual at offset
	 * 
	 * @param individual
	 * @param offset
	 * @return individual
	 */
	public Individual setIndividual(int offset, Individual individual) {
		return population.set(offset, individual);
	}

	/**
	 * Get individual at offset
	 * 
	 * @param offset
	 * @return individual
	 */
	public Individual getIndividual(int offset) {
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

	public int size() {
		// TODO Auto-generated method stub
		return population.size();
	}
}
