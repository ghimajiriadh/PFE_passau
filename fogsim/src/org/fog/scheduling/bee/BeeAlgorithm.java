package org.fog.scheduling.bee;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.SchedulingAlgorithm;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.scheduling.gaEntities.Population;
import org.fog.scheduling.gaEntities.Service;
import org.fog.scheduling.localSearchAlgorithm.LocalSearchAlgorithm;

public class BeeAlgorithm {

        private int populationSize;

        /**
         * Mutation rate is the fractional probability than an individual gene will
         * mutate randomly in a given generation. The range is 0.0-1.0, but is
         * generally small (on the order of 0.1 or less).
         */
        private double mutationRate;

        /**
         * Crossover rate is the fractional probability that two individuals will
         * "mate" with each other, sharing genetic information, and creating
         * offspring with traits of each of the parents. Like mutation rate the
         * rance is 0.0-1.0 but small.
         */
        private double crossoverRate;

        /**
         * Elitism is the concept that the strongest members of the population
         * should be preserved from generation to generation. If an individual is
         * one of the elite, it will not be mutated or crossover.
         */
        private int numberDrones;
        private int numberWorkers;

        private double minTime;
        private double minCost;



        public BeeAlgorithm(int populationSize, double mutationRate, double crossoverRate, int numberDrones) {
                this.populationSize = populationSize;
                this.mutationRate = mutationRate;
                this.crossoverRate = crossoverRate;
                this.numberDrones = numberDrones;
                this.numberWorkers = populationSize - 1 - numberDrones;
        }
        /**
         * calculate the lower boundary of time and cost
         *
         */
        public void calcMinTimeCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList)  {
                this.minTime = calcMinTime(fogDevices, cloudletList);
                this.minCost = calcMinCost(fogDevices, cloudletList);
        }

        private double calcMinCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
                double minCost = 0;
                for(Cloudlet cloudlet : cloudletList) {
                        double minCloudletCost = Double.MAX_VALUE;
                        for(FogDevice fogDevice : fogDevices) {
                                double cost = calcCost(cloudlet,fogDevice);
                                if(minCloudletCost > cost) {
                                        minCloudletCost = cost;
                                }
                        }
                        // the minCost is defined as the sum of all minCloudletCost
                        minCost += minCloudletCost;
                }
                return minCost;
        }

        // the method calculates the cost (G$) when a fogDevice executes a cloudlet
        private double calcCost(Cloudlet cloudlet, FogDevice fogDevice) {
                double cost = 0;
                //cost includes the processing cost
                cost += fogDevice.getCharacteristics().getCostPerSecond() * cloudlet.getCloudletLength( ) / fogDevice.getHost().getTotalMips();
                // cost includes the memory cost
                cost += fogDevice.getCharacteristics().getCostPerMem() * cloudlet.getMemRequired();
                // cost includes the bandwidth cost
                cost += fogDevice.getCharacteristics().getCostPerBw() * (cloudlet.getCloudletFileSize()  + cloudlet.getCloudletOutputSize());
                return cost;
        }

        // the function calculate the lower bound of the solution about time execution
        private double calcMinTime(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
                double minTime = 0;
                double totalLength = 0;
                double totalMips = 0;
                for(Cloudlet cloudlet : cloudletList) {
                        totalLength += cloudlet.getCloudletLength();
                }
                for(FogDevice fogDevice : fogDevices) {
                        totalMips += fogDevice.getHost().getTotalMips();
                }
                minTime = totalLength / totalMips;
                return minTime;
        }

        /**
         * Initialize population
         *
         * @param chromosomeLength
         *            The length of the individuals chromosome
         * @return population The initial population generated
         */
        public Population initPopulation(int chromosomeLength, int maxValue) {
                // Initialize population
                Population population = new Population(this.populationSize, chromosomeLength, maxValue) ;
                return population;
        }

        /**
         * Calculate fitness for an individual.
         *
         * In this case, the fitness score is very simple: it's the number of ones
         * in the chromosome. Don't forget that this method, and this whole
         * GeneticAlgorithm class, is meant to solve the problem in the "AllOnesGA"
         * class and example. For different problems, you'll need to create a
         * different version of this method to appropriately calculate the fitness
         * of an individual.
         *
         * @param individual
         *            the individual to evaluate
         * @return double The fitness value for individual
         */
        public double calcFitness(Individual individual, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

                // clear the fogDevice - task list before calculate
                for(FogDevice fogDevice : fogDevices) {
                        fogDevice.getCloudletListAssignment().clear();
                }

                // Loop over individual's genes to all the task assigned to the fogDevice
                for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                        //add current cloudlet to fog device respectively
                        fogDevices.get(individual.getGene(geneIndex)).getCloudletListAssignment().add(cloudletList.get(geneIndex));
                }

                //Calculate makespan and cost
                double makespan = 0;
                double execTime = 0;
                double totalCost = 0;
                for(FogDevice fogDevice : fogDevices) {
                        double totalLength = 0;
                        for(Cloudlet cloudlet : fogDevice.getCloudletListAssignment()) {
                                totalLength += cloudlet.getCloudletLength();
                                // the total cost is sum of the cost execution of each cloudlet
                                totalCost += calcCost(cloudlet, fogDevice);
                        }
                        // execTime is the time that fogDevice finishes its list cloudlet assignment
                        execTime = totalLength / fogDevice.getHostList().get(0).getTotalMips();
                        // makespan is defined as when the last cloudlet finished or when all fogDevice s finish its work.
                        if (execTime > makespan) {
                                makespan = execTime;
                        }
                }

                //store makespan
                individual.setTime(makespan);
                //store cost
                individual.setCost(totalCost);

                // Calculate fitness
                double fitness = SchedulingAlgorithm.TIME_WEIGHT * minTime / makespan
                                        + (1 - SchedulingAlgorithm.TIME_WEIGHT) * minCost / totalCost;

                // Store fitness
                individual.setFitness(fitness);
                return fitness;
        }

        /**
         * Evaluate the whole population
         *
         * Essentially, loop over the individuals in the population, calculate the
         * fitness for each, and then calculate the entire population's fitness. The
         * population's fitness may or may not be important, but what is important
         * here is making sure that each individual gets evaluated.
         *
         * @param population
         *            the population to evaluate
         */
        public Population evalPopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

                double populationFitness = 0;

                // Loop over population evaluating individuals and summing population fitness
                for (Individual individual : population.getPopulation()) {
                        populationFitness += calcFitness(individual, fogDevices, cloudletList);
                }

                //sort population with increasing fitness value
                population.sortPopulation();

                population.setPopulationFitness(populationFitness);
                return population;
        }


        /**
         * Check if population has met termination condition
         *
         * For this simple problem, we know what a perfect solution looks like, so
         * we can simply stop evolving once we've reached a fitness of one.
         *
         * @param population
         * @return boolean True if termination condition met, otherwise, false
         */
        public boolean isTerminationConditionMet(Population population) {
                for (Individual individual : population.getPopulation()) {
                        if (individual.getFitness() == 1) {
                                return true;
                        }
                }

                return false;
        }

        /**
         * Select parent for crossover
         *
         * @param population
         *            The population to select parent from
         * @return The individual selected as a parent
         */
        public Individual selectIndividual(Population population) {
                // Get individuals
                List<Individual> individuals = population.getPopulation();

                // Spin roulette wheel
                double populationFitness = population.getPopulationFitness();
                double rouletteWheelPosition = Math.random() * populationFitness;

                // Find parent
                double spinWheel = 0;
                for (Individual individual : individuals) {
                        spinWheel += individual.getFitness();
                        if (spinWheel >= rouletteWheelPosition) {
                                return individual;
                        }
                }
                return individuals.get(population.size() - 1);
        }

        /**
         * Apply crossover to population
         * @param population
         *            The population to apply crossover to
         * @return The new population
         */
        public Population crossoverPopulation(Population population, List<FogDevice> fogDevices, List<?  extends Cloudlet> cloudletList) {
                Individual queen = population.getFittest(0);
                // Loop over current population by fitness
                for (int dronesIndex = 1; dronesIndex < (numberDrones + 1); dronesIndex++) {
                        Individual husband = population.getFittest(dronesIndex);

                        // Apply crossover to this individual?
                        if (this.crossoverRate > Math.random()) {
                                // Initialize offspring
                                Individual offspring = new Individual(husband.getChromosomeLength());

                                offspring = crossover2Point(husband, queen);

                                if(husband.getFitness() <= calcFitness(offspring, fogDevices, cloudletList)
                                                && !doesPopupationIncludeIndividual(population, offspring)) {
                                        population.getPopulation().remove(husband);
                                        population.getPopulation().add(offspring);
                                }
                        }
                }
                return population;
        }

        // crossover 2 points between 2 parents and create an offspring
        public Individual crossover2Point(Individual parent1, Individual parent2) {
                Individual offspring = new Individual(parent1.getChromosomeLength());
                int crossoverPoint1 = Service.rand(0, parent1.getChromosomeLength()-1);
                int crossoverPoint2 = Service.rand(crossoverPoint1 + 1, crossoverPoint1 + parent1.getChromosomeLength());

                for(int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
                        if (crossoverPoint2 >= parent1.getChromosomeLength()) {
                                if(geneIndex >= crossoverPoint1 || geneIndex < (crossoverPoint2 - parent1.getChromosomeLength())) {
                                        offspring.setGene(geneIndex, parent2.getGene(geneIndex));
                                } else {
                                        offspring.setGene(geneIndex, parent1.getGene(geneIndex));
                                }
                        }else {
                                if(geneIndex >= crossoverPoint1 && geneIndex < crossoverPoint2) {
                                        offspring.setGene(geneIndex, parent2.getGene(geneIndex));
                                } else {
                                        offspring.setGene(geneIndex, parent1.getGene(geneIndex));
                                }
                        }
                }
                return offspring;
        }

        // crossover 2 points between 2 parents and create 2 offspring
                public List<Individual> crossover2Point2(Individual parent1, Individual parent2) {
                        List <Individual> listOffsprings = new ArrayList<Individual>();
                        Individual offspring1 = new Individual(parent1.getChromosomeLength());
                        Individual offspring2 = new Individual(parent1.getChromosomeLength());
                        int crossoverPoint1 = Service.rand(0, parent1.getChromosomeLength()-1);
                        int crossoverPoint2 = Service.rand(crossoverPoint1 + 1, crossoverPoint1 + parent1.getChromosomeLength() - 1);

                        for(int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++)  {
                                if (crossoverPoint2 >= parent1.getChromosomeLength()) {
                                        if(geneIndex >= crossoverPoint1 || geneIndex < (crossoverPoint2  - parent1.getChromosomeLength())) {
                                                offspring1.setGene(geneIndex, parent2.getGene(geneIndex ));
                                                offspring2.setGene(geneIndex, parent1.getGene(geneIndex ));
                                        } else {
                                                offspring1.setGene(geneIndex, parent1.getGene(geneIndex ));
                                                offspring2.setGene(geneIndex, parent2.getGene(geneIndex ));
                                        }
                                }else {
                                        if(geneIndex >= crossoverPoint1 && geneIndex < crossoverPoint2)  {
                                                offspring1.setGene(geneIndex, parent2.getGene(geneIndex ));
                                                offspring2.setGene(geneIndex, parent1.getGene(geneIndex ));
                                        } else {
                                                offspring1.setGene(geneIndex, parent1.getGene(geneIndex ));
                                                offspring2.setGene(geneIndex, parent2.getGene(geneIndex ));
                                        }
                                }
                        }
                        listOffsprings.add(offspring1);
                        listOffsprings.add(offspring2);
                        return listOffsprings;
                }

        // crossover 1 points between 2 parents and create an offspring
        public Individual crossover1Point(Individual parent1, Individual parent2) {
                Individual offspring = new Individual(parent1.getChromosomeLength());
                int crossoverPoint = Service.rand(0, parent1.getChromosomeLength());
                for (int geneIndex = 0; geneIndex < parent1.getChromosomeLength(); geneIndex++) {
                        // Use half of parent1's genes and half of parent2's genes
                        if (crossoverPoint > geneIndex) {
                                offspring.setGene(geneIndex, parent1.getGene(geneIndex));
                        } else {
                                offspring.setGene(geneIndex, parent2.getGene(geneIndex));
                        }
                }
                return offspring;
        }



        /**
         * Apply mutation to population
         *
         * Mutation affects individuals rather than the population. We look at each
         * individual in the population, and if they're lucky enough (or unlucky, as
         * it were), apply some randomness to their chromosome. Like crossover, the
         * type of mutation applied depends on the specific problem we're solving.
         * In this case, we simply randomly flip 0s to 1s and vice versa.
         *
         * This method will consider the GeneticAlgorithm instance's mutationRate
         * and elitismCount
         *
         * @param population
         *            The population to apply mutation to
         * @return The mutated population
         */
        public Population mutatePopulation(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

                // Loop over current population by fitness
                for (int populationIndex = 1; populationIndex < population.size(); populationIndex++) {
                        // if the current individual is selected to mutation phase
                        if (this.mutationRate > Math.random()) {
                                Individual individual = population.getFittest(populationIndex);
                                individual = this.mutateIndividual(individual);
                        }
                }
                // Return mutated population
                return population;
        }

        public Individual mutateIndividual(Individual individual) {
                individual.setGene(Service.rand(0, individual.getChromosomeLength() - 1), Service.rand(0, individual.getMaxValue()));
                return individual;
        }

        public boolean doesPopupationIncludeIndividual(Population population, Individual individual) {
                boolean include = false;
                for(int index = 0; index < population.size(); index++) {
                        boolean similar = true;
                        if(individual.getFitness() == population.getIndividual(index).getFitness()) {
                                for(int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                                        if(individual.getGene(geneIndex) != population.getIndividual(index).getGene(geneIndex)) {
                                                similar = false;
                                        }
                                }
                                if(similar == true) {
                                        include = true;
                                        break;
                                }
                        }
                        if (include == true) break;
                }
                return include;
        }

        public double getMinTime() {
                return this.minTime;
        }

        public double getMinCost() {
                return this.minCost;
        }

        public boolean isSameIndividual(Individual individual1, Individual individual2) {
                boolean same = true;
                for(int geneIndex = 0; geneIndex < individual1.getChromosomeLength(); geneIndex++) {
                        if(individual1.getGene(geneIndex)!= individual2.getGene(geneIndex)) {
                                same = false;
                                break;
                        }
                }
                return same;
        }

        //find food sources - done by workers
        public Population findFoodSource(Population population, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

                for(int workerIndex = numberDrones + 1; workerIndex < population.size(); workerIndex++) {
                        this.findFoodSourceByWorker(population.getIndividual(workerIndex), fogDevices, cloudletList);
                }

                return population;
        }

        public Individual findFoodSourceByWorker(Individual individual, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
                Individual newIndividual = new Individual(individual.getChromosomeLength());
                for(int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
                        newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
                }
                int count = 100;
                do {
                        newIndividual.setGene(Service.rand(0, individual.getChromosomeLength() - 1), Service.rand(0, individual.getMaxValue()));
                        newIndividual.setGene(Service.rand(0, individual.getChromosomeLength() - 1), Service.rand(0, individual.getMaxValue()));
                        count--;
                } while (calcFitness(newIndividual, fogDevices, cloudletList) < individual.getFitness()
                                && count > 0);
                individual = newIndividual;
                return individual;
        }
}
