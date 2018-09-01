package org.fog.scheduling.myGAEntities;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.MySchedulingAlgorithm;




public class MyGeneticAlgorithm {

	private int populationSize;
	private int offspringSize;
	private double mutatingSize;
	

	private double minTime; // Lower boundary of time
	private double minCost; // Lower boundary of cost

	
	/**
	 * Initializes an object for managing all GA methods
	 * 
	 * @param populationSize
	 * @param offspringSize
	 * @param mutationRate
	 */
	public MyGeneticAlgorithm (int populationSize, int offspringSize, double mutatingSize) {
		this.populationSize = populationSize;
		this.mutatingSize = mutatingSize;
		this.offspringSize = offspringSize;
	}


	/**
	 * Initialize population
	 * 
	 * @param chromosomeLength : The length of the individuals chromosome
	 * @param maxValue : Specified the value domain [0, maxValue]
	 * @return The initialized population
	 */
	public MyPopulation initPopulation(int chromosomeLength, int maxValue) {
		// Initialize population
		MyPopulation population = new MyPopulation(this.populationSize, chromosomeLength, maxValue);
		return population;
	}


	/**
	 * Calculates the lower boundary of Time and Cost 
	 */
	public void calcMinTimeCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
		this.minTime = MyService.calcMinTime(fogDevices, cloudletList);
		this.minCost = MyService.calcMinCost(fogDevices, cloudletList);
		
	}


	
	/**
	 * Evaluates the whole population
	 * 
	 * @param population : The population to evaluate
	 * @param fogDevices 
	 * @param cloudletList
	 * @return The population after evaluating
	 */
	public MyPopulation evalPopulation(MyPopulation population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
		double populationFitness = 0;

		// Loop over population evaluating individuals and summing population fitness
		for (MyIndividual individual : population.getPopulation()) {
			populationFitness += MyService.calcFitness(individual, this.minTime, this.minCost, fogDevices, cloudletList);
		}

		//sort population in descending order
		population.sortPopulation();

		population.setPopulationFitness(populationFitness);
		return population;
	}

	
	/**
	 * Selects the best parents randomly from the population
	 * 
	 * @param population
	 * @return The best parents choosed randomly
	 */
	public MyPopulation selectOffspringsRandomly(MyPopulation population) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();
		
		int size = population.size();
		int randomIndex;
		MyIndividual myClonedIndividual;
		
		for (int offspringIndex = 0; offspringIndex < this.offspringSize; offspringIndex++) {
			randomIndex = MyService.rand(0, size-1);
			myClonedIndividual = (MyIndividual) MyService.deepCopy(population.getIndividual(randomIndex));
			offsprings.add(myClonedIndividual);
		}
		
		return (new MyPopulation(offsprings));
	}
	
	// Choose the best individuals
	public MyPopulation selectOffspringsRandomly1(MyPopulation population) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();
		
		int size = population.size();
		int randomIndex;
		MyIndividual myClonedIndividual;
		
		for (int offspringIndex = 0; offspringIndex < this.offspringSize; offspringIndex++) {
			myClonedIndividual = (MyIndividual) MyService.deepCopy(population.getIndividual(offspringIndex));
			offsprings.add(myClonedIndividual);
		}
		
		return (new MyPopulation(offsprings));
	}
	
	// Choose the individuals uniquely, randomly
	public MyPopulation selectOffspringsRandomly2(MyPopulation population) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();
		
		int size = population.size();
		int randomIndex;
		MyIndividual myClonedIndividual;
		int[] array = new int[size];
		for (int index = 0; index < size; index++)
			array[index] = index;
		MyService.shuffleArray(array);
		
		for (int offspringIndex = 0; offspringIndex < this.offspringSize; offspringIndex++) {
			myClonedIndividual = (MyIndividual) MyService.deepCopy(population.getIndividual(array[offspringIndex]));
			offsprings.add(myClonedIndividual);
		}
		
		return (new MyPopulation(offsprings));
	}
	
	
	
	public MyPopulation selectOffspringsPressure(MyPopulation population, double PS) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();
		
		double bestFitness = population.getIndividual(0).getFitness();
		int populationSize = population.size();
		double averageFitness = population.getPopulationFitness() / populationSize;
		double selectionPressure = PS;
		double scalingValue = (selectionPressure*averageFitness - bestFitness)/(selectionPressure-1);
		
		double[] selectedProbability = new double[populationSize];
		double[] scaledFitness = new double[populationSize];
		double fitness, scalingFitnessSum = 0.0;
		for (int index = 0; index < populationSize; index++) {
			fitness = population.getIndividual(index).getFitness();
			scaledFitness[index] = ((fitness - scalingValue) >= 0.0) ? (fitness - scalingValue) : 0.0;
			scalingFitnessSum += scaledFitness[index];
		}
		
		for (int index = 0; index < populationSize; index++) {
			selectedProbability[index] = (this.offspringSize * scaledFitness[index])/scalingFitnessSum;
		}
		
//		double delta = scalingFitnessSum / offspringSize;
		double delta = 1.0;
		int[] array = new int[populationSize];
		for (int index = 0; index < populationSize; index++)
			array[index] = index;
		MyService.shuffleArray(array);
		
		MyIndividual myClonedIndividual;
		int i = 0;
		double sum1 = selectedProbability[array[i]];
		double sum2 = 0.0;
		while (true) {
			
			if (sum2 < sum1) {
				myClonedIndividual = (MyIndividual) MyService.deepCopy(population.getIndividual(array[i]));
				offsprings.add(myClonedIndividual);
				sum2 += delta;
				if (offsprings.size() >= this.offspringSize)
					break;
			} else {
				i++;
				sum1 += selectedProbability[array[i]];
			}
				
		}
		
		
		
		return (new MyPopulation(offsprings));
	}	
	
	
	/**
	 * One-Cut-Point Crossover the best parents to produce offsprings
	 * 
	 * @param parents
	 * @return The offsprings after crossing-over
	 */
	public MyPopulation crossoverOffsprings1Point(MyPopulation parents) {
		List<MyIndividual> crossoveredOffsprings = new ArrayList<MyIndividual>();
		
		int parentSize = parents.size();
		List<MyIndividual> coupleOffsprings;
		for (int parentIndex = 0; parentIndex < parentSize; parentIndex += 2) {
			coupleOffsprings = crossover1Point(parents.getIndividual(parentIndex), parents.getIndividual(parentIndex+1));
			crossoveredOffsprings.addAll(coupleOffsprings);
		}
		return (new MyPopulation(crossoveredOffsprings));
	}
	
	
	
	
	/**
	 * One-Cut-Point Crossover the two parents to produce two offsprings
	 * 
	 * @param parent1
	 * @param parent2
	 * @return The two offsprings crossed-over from the two given parents
	 */
	public List<MyIndividual> crossover1Point(MyIndividual parent1, MyIndividual parent2) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();
		
		int chromosomeLength = parent1.getChromosomeLength();
		int maxValue = parent1.getMaxValue();
		MyIndividual child1 = new MyIndividual(chromosomeLength, maxValue, false);
		MyIndividual child2 = new MyIndividual(chromosomeLength, maxValue, false);
		
		int randomCutPoint = MyService.rand(1, chromosomeLength-1);
		for (int geneIndex = 0; geneIndex < chromosomeLength; geneIndex++) {
			if (geneIndex < randomCutPoint) {
				child1.setGene(geneIndex, parent1.getGene(geneIndex));
				child2.setGene(geneIndex, parent2.getGene(geneIndex));
			} else {
				child1.setGene(geneIndex, parent2.getGene(geneIndex));
				child2.setGene(geneIndex, parent1.getGene(geneIndex));
			}
		}
		
		offsprings.add(child1);
		offsprings.add(child2);
		
		return offsprings;
	}
	
	
	/**
	 * Two-Cut-Point Crossover the two parents to produce two offsprings
	 * 
	 * @param parents
	 * @return The offsprings after crossing-over
	 */
	public MyPopulation crossoverOffsprings2Point(MyPopulation parents) {
		List<MyIndividual> crossoveredOffsprings = new ArrayList<MyIndividual>();
		
		int parentSize = parents.size();
		List<MyIndividual> coupleOffsprings;
		for (int parentIndex = 0; parentIndex < parentSize; parentIndex += 2) {
			coupleOffsprings = crossover2Point(parents.getIndividual(parentIndex), parents.getIndividual(parentIndex+1));
			crossoveredOffsprings.addAll(coupleOffsprings);
		}
		return (new MyPopulation(crossoveredOffsprings));
	}
	
	
	
	/**
	 * Two-Cut-Point Crossover the two parents to produce two offsprings
	 * 
	 * @param parent1
	 * @param parent2
	 * @return The two offsprings crossed-over from the two given parents
	 */
	public List<MyIndividual> crossover2Point(MyIndividual parent1, MyIndividual parent2) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();

		int chromosomeLength = parent1.getChromosomeLength();
		int maxValue = parent1.getMaxValue();
		MyIndividual child1 = new MyIndividual(chromosomeLength, maxValue, false);
		MyIndividual child2 = new MyIndividual(chromosomeLength, maxValue, false);
		
		int randomCutPoint1 = MyService.rand(0, chromosomeLength - 2);
		int randomCutPoint2 = MyService.rand(randomCutPoint1 + 1, chromosomeLength - 1);
		
		for (int geneIndex = 0; geneIndex < chromosomeLength; geneIndex++) {
			if (geneIndex < randomCutPoint1) {
				child1.setGene(geneIndex, parent1.getGene(geneIndex));
				child2.setGene(geneIndex, parent2.getGene(geneIndex));
			} else if (geneIndex < randomCutPoint2) {
				child1.setGene(geneIndex, parent2.getGene(geneIndex));
				child2.setGene(geneIndex, parent1.getGene(geneIndex));
			} else {
				child1.setGene(geneIndex, parent1.getGene(geneIndex));
				child2.setGene(geneIndex, parent2.getGene(geneIndex));
			}
		}
		
		offsprings.add(child1);
		offsprings.add(child2);
		
		return offsprings;
	}
	
	
	
	/**
	 * Random Template Crossover the two parents to produce two offsprings
	 * 
	 * @param parents
	 * @return The offsprings after crossing-over
	 */	
	public MyPopulation crossoverOffspringsRandomTemplate(MyPopulation parents, double digitsOneRate) {
		List<MyIndividual> crossoveredOffsprings = new ArrayList<MyIndividual>();
		
		int parentSize = parents.size();
		List<MyIndividual> coupleOffsprings;
		for (int parentIndex = 0; parentIndex < parentSize; parentIndex += 2) {
			coupleOffsprings = crossoverRandomTemplate(parents.getIndividual(parentIndex), parents.getIndividual(parentIndex+1), digitsOneRate);
			crossoveredOffsprings.addAll(coupleOffsprings);
		}
		return (new MyPopulation(crossoveredOffsprings));
	}
	
	/**
	 * Random Template Crossover the two parents to produce two offsprings
	 * 
	 * @param parent1
	 * @param parent2
	 * @return The two offsprings crossed-over from the two given parents
	 */	
	public List<MyIndividual> crossoverRandomTemplate(MyIndividual parent1, MyIndividual parent2, double digitsOneRate) {
		List<MyIndividual> offsprings = new ArrayList<MyIndividual>();
		
		int chromosomeLength = parent1.getChromosomeLength();
		int maxValue = parent1.getMaxValue();
		MyIndividual child1 = new MyIndividual(chromosomeLength, maxValue, false);
		MyIndividual child2 = new MyIndividual(chromosomeLength, maxValue, false);
		
//		int quantityDigitsOne = MyService.rand(0,(int) chromosomeLength/2);
		int quantityDigitsOne = (int) (chromosomeLength*digitsOneRate);
		int[] randomTemplate = MyService.createRandomTemplate(chromosomeLength, quantityDigitsOne);
		
		for (int geneIndex = 0; geneIndex < chromosomeLength; geneIndex++) {
			if (randomTemplate[geneIndex] == 1) {
				child1.setGene(geneIndex, parent2.getGene(geneIndex));
				child2.setGene(geneIndex, parent1.getGene(geneIndex));
			} else {
				child1.setGene(geneIndex, parent1.getGene(geneIndex));
				child2.setGene(geneIndex, parent2.getGene(geneIndex));
			}
		}
		
		offsprings.add(child1);
		offsprings.add(child2);
		
		return offsprings;
	}
	
	/**
	 * Mutates some parents to produce new offsprings
	 * 
	 * @param parents
	 * @return The offsprings has experienced the mutation
	 */
	public MyPopulation mutateOffsprings(MyPopulation parents) {

		int parentSize = parents.size();
		int chromosomeLength = parents.getIndividual(0).getChromosomeLength();
		int domainValueMax = parents.getIndividual(0).getMaxValue();
		int mutatingParentIndex;
		
		for (int mutatingIndex = 0; mutatingIndex < mutatingSize; mutatingIndex++) {
			// Gets mutating parent
			mutatingParentIndex = MyService.rand(0, parentSize-1);
			MyIndividual mutatingParent = parents.getIndividual(mutatingParentIndex);
			
			// Choose value pair for mutation
			int randomCloudletId = MyService.rand(0, chromosomeLength-1);
			int randomfogId = MyService.rand(0, domainValueMax);
			
			// Performs mutation
			mutatingParent.setGene(randomCloudletId, randomfogId);
		}

		return parents;
	}
	
	/**
	 * Selects the best individuals for next generation
	 * 
	 * @param parentsPopulation
	 * @param offsprings
	 * @param fogDevices
	 * @param cloudletList
	 * @return The population for next generation
	 */
	public MyPopulation selectNextGeneration(MyPopulation parentsPopulation, MyPopulation offsprings, 
			List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
		
		int parentSize = parentsPopulation.size();
		int offspringSize = offsprings.size();
		
		// Evaluates and sorts the offsprings
		evalPopulation(offsprings, fogDevices, cloudletList);
		offsprings.sortPopulation();
		
		// Chooses the best individual for the next generation using Merge Sort
		int offspringIndex = 0;
		int parentIndex = 0;
		int newGenerationIndex = 0;
		List<MyIndividual> nextGeneration = new ArrayList<MyIndividual>();
		
		while ((newGenerationIndex < parentSize) && (parentIndex < parentSize) 
				&& (offspringIndex < offspringSize)) {
			
			// Chooses the better individual from the two list's heads
			if (parentsPopulation.getIndividual(parentIndex).getFitness() 
					> offsprings.getIndividual(offspringIndex).getFitness()) {
				nextGeneration.add(parentsPopulation.getIndividual(parentIndex));
				parentIndex++;
			} else {
				nextGeneration.add(offsprings.getIndividual(offspringIndex));
				offspringIndex++;
			}
			
			newGenerationIndex++;
		}
		
		while (newGenerationIndex < parentSize) {
			if (parentIndex < parentSize) {
				nextGeneration.add(parentsPopulation.getIndividual(parentIndex));
				parentIndex++;
			} else if (offspringIndex < offspringSize) {
				nextGeneration.add(offsprings.getIndividual(offspringIndex));
				offspringIndex++;
			}
			
			newGenerationIndex++;
		}
		
		return (new MyPopulation(nextGeneration));
	}
	
	
	/**
	 * Get the lower boundary of make-span time
	 * 
	 * @return The lower boundary of make-span time
	 */
	public double getMinTime() {
		return this.minTime;
	}

	/**
	 * Get the lower boundary of cost
	 * 
	 * @return The lower boundary of cost
	 */
	public double getMinCost() {
		return this.minCost;
	}

	
	
	
//	
//
//	/**
//	 * Apply crossover to population
//	 * 
//	 * Crossover, more colloquially considered "mating", takes the population
//	 * and blends individuals to create new offspring. It is hoped that when two
//	 * individuals crossover that their offspring will have the strongest
//	 * qualities of each of the parents. Of course, it's possible that an
//	 * offspring will end up with the weakest qualities of each parent.
//	 * 
//	 * This method considers both the GeneticAlgorithm instance's crossoverRate
//	 * and the elitismCount.
//	 * 
//	 * The type of crossover we perform depends on the problem domain. We don't
//	 * want to create invalid solutions with crossover, so this method will need
//	 * to be changed for different types of problems.
//	 * 
//	 * This particular crossover method selects random genes from each parent.
//	 * 
//	 * @param population
//	 *            The population to apply crossover to
//	 * @return The new population
//	 */
//
//	public Population crossoverPopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
//		// Create new population
//		List<Individual> newPopulation = new ArrayList<Individual>();
//
//		newPopulation.clear();
//		// Loop over current population by fitness
//		for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
//			Individual parent1 = population.getFittest(populationIndex);
//
//			// Apply crossover to this individual
//			if (this.crossoverRate > Math.random() && populationIndex >= this.elitismCount) {
//				// Initialize offspring
//				Individual offspring = new Individual(parent1.getChromosomeLength());
//
//				// Find second parent
//				Individual parent2 = selectIndividual(population);
//				offspring = crossover2Point(parent1, parent2);
//
//				//				System.out.print("\nParent1: ");
//				//				parent1.printGene();
//				//				System.out.print("\nParent2: ");
//				//				parent2.printGene();
//				//				
//				//				System.out.print("\nOffspring: ");
//				//				offspring.printGene();
//
//				if(parent1.getFitness() <= calcFitness(offspring, fogDevices, cloudletList)
//						&& !doesPopupationIncludeIndividual(population, offspring)) {
//					newPopulation.add(offspring);
//				} else {
//					newPopulation.add(parent1);
//				}
//			} else {
//				newPopulation.add(population.getFittest(populationIndex));
//			}
//		}
//		population.getPopulation().clear();
//		population.setPopulation(newPopulation);
//
//		//		System.out.println("--------AFTER CROSSOVER--------");
//		//		population.printPopulation();
//		return population;
//	}
//
//	// crossover 2 points between 2 parents and create an offspring
//	public Individual crossover2Point(Individual parent1, Individual parent2) {
//		Individual offspring = new Individual(parent1.getChromosomeLength());
//		int crossoverPoint1 = Service.rand(0, parent1.getChromosomeLength()-1);
//		int crossoverPoint2 = Service.rand(crossoverPoint1 + 1, crossoverPoint1 + parent1.getChromosomeLength()/2);
//
//		for(int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
//			if (crossoverPoint2 >= parent1.getChromosomeLength()) {
//				if(geneIndex >= crossoverPoint1 || geneIndex < (crossoverPoint2 - parent1.getChromosomeLength())) {
//					offspring.setGene(geneIndex, parent2.getGene(geneIndex));
//				} else {
//					offspring.setGene(geneIndex, parent1.getGene(geneIndex));
//				}
//			}else {
//				if(geneIndex >= crossoverPoint1 && geneIndex < crossoverPoint2) {
//					offspring.setGene(geneIndex, parent2.getGene(geneIndex));
//				} else {
//					offspring.setGene(geneIndex, parent1.getGene(geneIndex));
//				}
//			}			
//		}
//		return offspring;
//	}
//
//	// crossover 1 points between 2 parents and create an offspring	
//	public Individual crossover1Point(Individual parent1, Individual parent2) {
//		Individual offspring = new Individual(parent1.getChromosomeLength());
//		int crossoverPoint = Service.rand(0, parent1.getChromosomeLength());
//		for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
//			// Use half of parent1's genes and half of parent2's genes
//			if (crossoverPoint > geneIndex) {
//				offspring.setGene(geneIndex, parent1.getGene(geneIndex));
//			} else {
//				offspring.setGene(geneIndex, parent2.getGene(geneIndex));
//			}
//		}
//		return offspring;
//	}
//
//
//
//	/**
//	 * Apply mutation to population
//	 * 
//	 * Mutation affects individuals rather than the population. We look at each
//	 * individual in the population, and if they're lucky enough (or unlucky, as
//	 * it were), apply some randomness to their chromosome. Like crossover, the
//	 * type of mutation applied depends on the specific problem we're solving.
//	 * In this case, we simply randomly flip 0s to 1s and vice versa.
//	 * 
//	 * This method will consider the GeneticAlgorithm instance's mutationRate
//	 * and elitismCount
//	 * 
//	 * @param population
//	 *            The population to apply mutation to
//	 * @return The mutated population
//	 */
//	public Population mutatePopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
//
//		// Loop over current population by fitness
//		for (int populationIndex = 0; populationIndex < population.size(); populationIndex++) {
//			// if the current individual is selected to mutation phase
//			if (this.mutationRate > Math.random()) {
//				Individual individual = population.getFittest(populationIndex);
//				Individual newIndividual = new Individual(individual.getChromosomeLength());
//				//listChange contains which gen change makes the individual better
//				List<Pair> listChange = new ArrayList<Pair>();
//
//				int randomCloudletId = Service.rand(0, individual.getChromosomeLength() - 1);
//				int randomfogId = Service.rand(0, individual.getMaxValue());
//
//
//				for(int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
//					newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
//				}
//				newIndividual.setGene(randomCloudletId, randomfogId);
//				if (calcFitness(newIndividual, fogDevices, cloudletList) >= calcFitness(individual, fogDevices, cloudletList))
//					individual.setGene(randomCloudletId, randomfogId);
//
//				//				for(int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
//				//					for(int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {					
//				//						
//				//						for(int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
//				//							newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
//				//						}
//				//						
//				//						// change a gene of individual to form newIndividual
//				//						newIndividual.setGene(cloudletId, fogId);
//				//						double newFitness = calcFitness(newIndividual, fogDevices, cloudletList); 
//				//						//if newIndividual is better then individual, store change in listChange
//				//						if(newFitness > individual.getFitness()) {
//				//							listChange.add(new Pair(cloudletId, fogId));
//				//						}
//				//					}
//				//				}
//				//				
//				//				// if exist any gene make individual better, select randomly a gene change to have newIndividual
//				//				if(!listChange.isEmpty()) {
//				//					int change = Service.rand(0, listChange.size() - 1);
//				//					individual.setGene(listChange.get(change).getCloudletId(), listChange.get(change).getFogId());
//				//				}
//			}
//		}
//		// Return mutated population
//		return population;
//	}
//
//	public boolean doesPopupationIncludeIndividual(Population population, Individual individual) {
//		boolean include = false;
//		for(int index = 0; index < population.size(); index++) {
//			boolean similar = true;
//			if(individual.getFitness() == population.getIndividual(index).getFitness()) {
//				for(int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
//					if(individual.getGene(geneIndex) != population.getIndividual(index).getGene(geneIndex)) {
//						similar = false;
//					}
//				}
//				if(similar == true) {
//					include = true;
//					break;
//				}
//			}
//			if (include == true) break;
//		}
//		return include;
//	}
//
//	public void selectPopulation(Population population) {
//		// remove similar individuals
//		//		population.sortPopulation();
//		////		System.out.println("--------before select--------");
//		////		population.printPopulation();
//		//		for(int populationIndex = 0; populationIndex < population.size()-1; populationIndex++) {
//		//			for(int index = populationIndex +1; index < population.size(); index++) {
//		//				if(population.getIndividual(populationIndex).getFitness() == population.getIndividual(index).getFitness()) {
//		//					boolean similar = true;
//		//					for(int geneIndex = 0; geneIndex < population.getIndividual(populationIndex).getChromosomeLength(); geneIndex++) {
//		//						if(population.getIndividual(populationIndex).getGene(geneIndex) != population.getIndividual(index).getGene(geneIndex)) {
//		//							similar = false;
//		//						}
//		//					}
//		//					if(similar == true) {
//		//						population.getPopulation().remove(populationIndex);
//		//						populationIndex--;
//		//						break;
//		//					}
//		//				}
//		//			}
//		//		}
//
//		population.sortPopulation();
//
//		System.out.println("Before Selection: ");
//		population.printPopulation();
//
//		while(population.size() > SchedulingAlgorithm.NUMBER_INDIVIDUAL) {
//			population.getPopulation().remove(SchedulingAlgorithm.NUMBER_INDIVIDUAL);
//		}
//		System.out.println("After Selection: ");
//		population.printPopulation();
//
//		//		System.out.println("--------AFTER select--------");
//		//		population.printPopulation();
//
//
//		//		// Create new population
//		//		List<Individual> newPopulation = new ArrayList<Individual>();
//		//		for(int populationIndex = 0; populationIndex < SchedulingAlgorithm.NUMBER_INDIVIDUAL; populationIndex++) {
//		////			if(populationIndex < SchedulingAlgorithm.NUMBER_ELITISM_INDIVIDUAL) {
//		////				newPopulation.add(population.getIndividual(populationIndex));
//		////			} else {
//		////				Individual individual = selectIndividual(population);
//		////				newPopulation.add(individual);
//		////			}	
//		//			newPopulation.add(population.getFittest(populationIndex));
//		//		}
//		//		population.getPopulation().clear();
//		//		population.setPopulation(newPopulation);
//	}
//

//
//	public static void main(String[] args) {
//
//	}
}
