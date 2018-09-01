package org.fog.scheduling.localSearchAlgorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.SchedulingAlgorithm;
import org.fog.scheduling.gaEntities.Individual;
import org.fog.scheduling.gaEntities.Service;

public class LocalSearchAlgorithm {

        private double minTime;
        private double minCost;

        public LocalSearchAlgorithm() {

        }

        public Individual hillCliming(Individual individual, List<FogDevice> fogDevices,
                        List<? extends Cloudlet> cloudletList) {

                // listChange contains which gene change makes the individual better
                List<Pair> listChange = new ArrayList<Pair>();

                int numberRound = 0;

                // Start local search loop
                do {
                        numberRound++;
                        System.out.println("\n--------------------------------------");
                        System.out.println("Round " + numberRound + ": ");
                        listChange.clear();
                        // fitness stores the fitness value of current individual
                        double fitness = calcFitness(individual, fogDevices, cloudletList);

                        // consider which gene changed makes individual better
                        for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                                for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {

                                        // create newIndividual similar to individual
                                        Individual newIndividual = new Individual(individual.getChromosomeLength());
                                        for (int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
                                                newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
                                        }

                                        // change a gene of individual to form newIndividual
                                        newIndividual.setGene(cloudletId, fogId);
                                        double newFitness = calcFitness(newIndividual, fogDevices, cloudletList);
                                        // if newIndividual is better then individual, store change
                                        // in listChange
                                        if (newFitness > fitness) {
                                                listChange.add(new Pair(cloudletId, fogId));
                                        }
                                }
                        }

                        // if exist any gene make individual better, select randomly a gene
                        // change to have newIndividual
                        System.out.println("Number of changelist: " + listChange.size());
                        if (!listChange.isEmpty()) {
                                int change = Service.rand(0, listChange.size() - 1);
                                individual.setGene(listChange.get(change).getCloudletId(), listChange.get(change).getFogId());
                                System.out.println("change possition: " + listChange.get(change).getCloudletId() + " "
                                                + listChange.get(change).getFogId());
                        }
                        individual.printGene();

                        System.out.println("\nFitness value: " + individual.getFitness());
                        System.out.println("Min Time: " + this.getMinTime() + "/// Makespan: " + individual.getTime());
                        System.out.println("Min Cost: " + this.getMinCost() + "/// TotalCost: " + individual.getCost());

                } while (!listChange.isEmpty());
                return individual;
        }

        public void restart(Individual individual, int tabu[][]) {
                individual = new Individual(individual.getChromosomeLength(), individual.getMaxValue());


                for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                        for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                                tabu[cloudletId][fogId] = -1;
                        }
                }

        }

        public Individual tabuSearch(Individual individual, List<FogDevice> fogDevices,
                        List<? extends Cloudlet> cloudletList, int maxStable, int maxInteration, int maxTime, int tabuLength) {

                // initiate Tabu metric, the value of each element is -1
                int[][] tabuMetric = new int[individual.getChromosomeLength()][individual.getMaxValue() + 1];
                for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                        for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                                tabuMetric[cloudletId][fogId] = -1;
                        }
                }

                Individual bestSolution = new Individual(cloudletList.size(), fogDevices.size() - 1);
                double bestValue = calcFitness(bestSolution, fogDevices, cloudletList);

                // listChange contains which gene change makes the individual better
                List<Pair> listChange = new ArrayList<Pair>();

                double start = System.currentTimeMillis();
                int count = 0;
                maxTime = maxTime * 1000;
                Random R = new Random();
                int nic = 0;

                while (System.currentTimeMillis() - start < maxTime && count < maxInteration) {
                        int sel_i = -1;
                        int sel_v = -1;
                        listChange.clear();
                        double min = -10000;
                        double valueIndividual = calcFitness(individual, fogDevices, cloudletList);
                        // consider which gene changed makes individual better
                        for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                                for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {

                                        if(tabuMetric[cloudletId][fogId] <= count) {
                                                // create newIndividual similar to individual
                                                Individual newIndividual = new Individual(individual.getChromosomeLength());
                                                for (int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
                                                        newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
                                                }

                                                // change a gene of individual to form newIndividual
                                                newIndividual.setGene(cloudletId, fogId);
                                                double newFitness = calcFitness(newIndividual, fogDevices, cloudletList);
                                                double deltaF = newFitness - valueIndividual;
                                                // if newIndividual is better then individual, store change
                                                // in listChange
                                                if (deltaF > min) {
                                                        min = deltaF;
                                                        sel_i = cloudletId;
                                                        sel_v = fogId;
                                                        listChange.clear();
                                                        listChange.add(new Pair(sel_i, sel_v));
                                                } else if (deltaF == min){
                                                        listChange.add(new Pair(cloudletId, fogId));
                                                }
                                        }

                                }
                        }
                        if(listChange.size() > 0) {
                                int k = R.nextInt(listChange.size());
                                Pair p = listChange.get(k);
                                sel_i = p.getCloudletId();
                                sel_v = p.getFogId();
                                individual.setGene(sel_i, sel_v);
                                tabuMetric[sel_i][sel_v] = count + tabuLength;
                                valueIndividual = calcFitness(individual, fogDevices, cloudletList);
                                System.out.println("Step: " + count + "----Current value: " + valueIndividual + "----Best value: " + bestValue + "----Delta: " + min + "----Nic: " + nic);

                                if(valueIndividual > bestValue) {
                                        bestValue = valueIndividual;
                                        for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                                                bestSolution.setGene(geneIndex, individual.getGene(geneIndex));
                                        }
                                }
                                if (valueIndividual <= bestValue) {
                                        nic++;
                                        if(nic > maxStable) {
                                                nic = 0;
                                                System.out.println("Tabu restart:");
//                                              restart(individual, tabuMetric);
                                                individual = new Individual(individual.getChromosomeLength(), individual.getMaxValue());
                                                for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                                                        for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                                                                tabuMetric[cloudletId][fogId] = -1;
                                                        }
                                                }
                                        }
                                }
                        } else {
                                nic = 0;
                                System.out.println("Tabu restart:");
//                              restart(individual, tabuMetric);
                                individual = new Individual(individual.getChromosomeLength(), individual.getMaxValue());
                                for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
                                        for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
                                                tabuMetric[cloudletId][fogId] = -1;
                                        }
                                }
                        }
                        count++;
                }
                return bestSolution;

//              int numberRound = 0;
//
//              // Start local search loop
//              do {
//                      numberRound++;
//                      System.out.println("\n--------------------------------------");
//                      System.out.println("Round " + numberRound + ": ");
//                      listChange.clear();
//                      // fitness stores the fitness value of current individual
//                      double fitness = calcFitness(individual, fogDevices, cloudletList);
//
//                      // consider which gene changed makes individual better
//                      for (int cloudletId = 0; cloudletId < individual.getChromosomeLength(); cloudletId++) {
//                              for (int fogId = 0; fogId < individual.getMaxValue() + 1; fogId++) {
//
//                                      // create newIndividual similar to individual
//                                      Individual newIndividual = new Individual(individual.getChromosomeLength());
//                                      for (int geneIndex = 0; geneIndex < newIndividual.getChromosomeLength(); geneIndex++) {
//                                              newIndividual.setGene(geneIndex, individual.getGene(geneIndex));
//                                      }
//
//                                      // change a gene of individual to form newIndividual
//                                      newIndividual.setGene(cloudletId, fogId);
//                                      double newFitness = calcFitness(newIndividual, fogDevices, cloudletList);
//                                      // if newIndividual is better then individual, store change
//                                      // in listChange
//                                      if (newFitness > fitness) {
//                                              listChange.add(new Pair(cloudletId, fogId));
//                                      }
//                              }
//                      }
//
//                      // if exist any gene make individual better, select randomly a gene
//                      // change to have newIndividual
//                      System.out.println("Number of changelist: " + listChange.size());
//                      if (!listChange.isEmpty()) {
//                              int change = Service.rand(0, listChange.size() - 1);
//
//                              if (tabuMetric[listChange.get(change).getCloudletId()][listChange.get(change).getFogId()] < 0) {
//                                      individual.setGene(listChange.get(change).getCloudletId(), listChange.get(change).getFogId());
//                                      System.out.println("change possition: " + listChange.get(change).getCloudletId() + " "
//                                                      + listChange.get(change).getFogId());
//                                      tabuMetric[listChange.get(change).getCloudletId()][listChange.get(change)
//                                                      .getFogId()] = SchedulingAlgorithm.TABU_CONSTANT;
//                              } else {
//                                      System.out.println("No gene changed!!!");
//                              }
//
//                              System.out.println("change possition: " + listChange.get(change).getCloudletId() + " "
//                                              + listChange.get(change).getFogId());
//                      }
//                      individual.printGene();
//
//                      System.out.println("\nFitness value: " + individual.getFitness());
//                      System.out.println("Min Time: " + this.getMinTime() + "/// Makespan: " + individual.getTime());
//                      System.out.println("Min Cost: " + this.getMinCost() + "/// TotalCost: " + individual.getCost());
//
//              } while (!listChange.isEmpty());
//              return individual;
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
        public double calcFitness(Individual individual, List<FogDevice> fogDevices,
                        List<? extends Cloudlet> cloudletList) {

                // clear the fogDevice - task list before calculate
                for (FogDevice fogDevice : fogDevices) {
                        fogDevice.getCloudletListAssignment().clear();
                }

                // Loop over individual's genes to all the task assigned to the
                // fogDevice
                for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
                        // add current cloudlet to fog device respectively
                        fogDevices.get(individual.getGene(geneIndex)).getCloudletListAssignment().add(cloudletList.get(geneIndex));
                }

                // Calculate makespan and cost
                double makespan = 0;
                double execTime = 0;
                double totalCost = 0;
                for (FogDevice fogDevice : fogDevices) {
                        double totalLength = 0;
                        for (Cloudlet cloudlet : fogDevice.getCloudletListAssignment()) {
                                totalLength += cloudlet.getCloudletLength();
                                // the total cost is sum of the cost execution of each cloudlet
                                totalCost += calcCost(cloudlet, fogDevice);
                        }
                        // execTime is the time that fogDevice finishes its list cloudlet
                        // assignment
                        execTime = totalLength / fogDevice.getHostList().get(0).getTotalMips();
                        // makespan is defined as when the last cloudlet finished or when
                        // all fogDevices finish its work.
                        if (execTime > makespan) {
                                makespan = execTime;
                        }
                }

                // store makespan
                individual.setTime(makespan);
                // store cost
                individual.setCost(totalCost);

                // Calculate fitness
                double fitness = SchedulingAlgorithm.TIME_WEIGHT * minTime / makespan
                                + (1 - SchedulingAlgorithm.TIME_WEIGHT) * minCost / totalCost;

                // double fitness = minTime / makespan;
                // double fitness = minCost / totalCost;
                // Store fitness
                individual.setFitness(fitness);
                return fitness;
        }

        /**
         * calculate the lower boundary of time and cost
         *
         */
        public void calcMinTimeCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
                this.minTime = calcMinTime(fogDevices, cloudletList);
                this.minCost = calcMinCost(fogDevices, cloudletList);
        }

        private double calcMinCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
                double minCost = 0;
                for (Cloudlet cloudlet : cloudletList) {
                        double minCloudletCost = Double.MAX_VALUE;
                        for (FogDevice fogDevice : fogDevices) {
                                double cost = calcCost(cloudlet, fogDevice);
                                if (minCloudletCost > cost) {
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
                // cost includes the processing cost
                cost += fogDevice.getCharacteristics().getCostPerSecond() * cloudlet.getCloudletLength()
                                / fogDevice.getHost().getTotalMips();
                // cost includes the memory cost
                cost += fogDevice.getCharacteristics().getCostPerMem() * cloudlet.getMemRequired();
                // cost includes the bandwidth cost
                cost += fogDevice.getCharacteristics().getCostPerBw()
                                * (cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize());
                return cost;
        }

        // the function calculate the lower bound of the solution about time
        // execution
        private double calcMinTime(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
                double minTime = 0;
                double totalLength = 0;
                double totalMips = 0;
                for (Cloudlet cloudlet : cloudletList) {
                        totalLength += cloudlet.getCloudletLength();
                }
                for (FogDevice fogDevice : fogDevices) {
                        totalMips += fogDevice.getHost().getTotalMips();
                }
                minTime = totalLength / totalMips;
                return minTime;
        }

        public double getMinTime() {
                return minTime;
        }

        public double getMinCost() {
                return minCost;
        }
}
