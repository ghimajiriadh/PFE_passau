package org.fog.scheduling.myLocalSearchAlgorithm;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.myGAEntities.MyIndividual;
import org.fog.scheduling.myGAEntities.MyService;

public class MyLocalSearchAlgorithm {
	private double minTime;
	private double minCost;

	public MyLocalSearchAlgorithm() {}
	
	
	public MyIndividual hillClimbing(MyIndividual individual, double INITIAL_SACRIFICE, double DESCENDING_SPEED, int numIterations, List<FogDevice> fogDevices,
			List<? extends Cloudlet> cloudletList) {

		// Contains good local moves for upcoming states 
		List<MyAbstractLocalMove> listLocalMoves = new ArrayList<MyAbstractLocalMove>();
		double bestFitness = Double.MIN_VALUE;
		
		double currentSacrifice = INITIAL_SACRIFICE; // 0.005
		
		double fitness;
		MyIndividual neighborIndividual;
		int iterationIndex = 0;
		int chromosomeLength = individual.getChromosomeLength();
		int maxGeneValue = individual.getMaxValue();
		do {
			iterationIndex++;
			System.out.println("\n--------------------------------------");
			System.out.println("Iteration : " + iterationIndex + " : ");
			
			if (iterationIndex % 200 == 0) currentSacrifice = currentSacrifice/DESCENDING_SPEED; 
			
			// Clear all local moves at previous step
			listLocalMoves.clear();
			
			// Get fitness of the individual
			fitness = MyService.calcFitness(individual, this.minTime, this.minCost, fogDevices, cloudletList);
			
			// Updates the best fitness
			if (fitness > bestFitness) 
				bestFitness = fitness;
			
			// Create a new neighbor
			neighborIndividual = (MyIndividual) MyService.deepCopy(individual);

			// Choose the better neighbors
			int oldGeneValue;
			double neighborFitness;
			for (int geneIndex = 0; geneIndex < chromosomeLength; geneIndex++) {
				for (int geneValue = 0; geneValue <= maxGeneValue; geneValue++) {
					oldGeneValue = individual.getGene(geneIndex);
					
					// Moves to new neighbor and calculates its fitness
					neighborIndividual.setGene(geneIndex, geneValue);
					neighborFitness = MyService.calcFitness(neighborIndividual, this.minTime, this.minCost, fogDevices, cloudletList);
					
					// Checks condition of taking part in the better neighbors
					if (neighborFitness > bestFitness - currentSacrifice) {
						listLocalMoves.add(new MyAssignLocalMove(geneIndex, geneValue));
					}
					
					// Moves back to current state
					neighborIndividual.setGene(geneIndex, oldGeneValue);
				}
			}
			
			int geneIndex1, geneIndex2;
			int oldGeneValue1, oldGeneValue2;
			
			for (geneIndex1 = 0; geneIndex1 < chromosomeLength; geneIndex1++) {
				for (geneIndex2 = geneIndex1 + 1; geneIndex2 < chromosomeLength; geneIndex2++) {
					// Moves to new neighbor and calculates its fitness
					MyService.swapIndividual(neighborIndividual, geneIndex1, geneIndex2);
					neighborFitness = MyService.calcFitness(neighborIndividual, this.minTime, this.minCost, fogDevices, cloudletList);
					if (neighborFitness > bestFitness - currentSacrifice) {
						listLocalMoves.add(new MySwapLocalMove(geneIndex1, geneIndex2));
					}
					
					// Moves back to current state
					MyService.swapIndividual(neighborIndividual, geneIndex1, geneIndex2);
				}
			}
			
			System.out.println("Number of changelist: " + listLocalMoves.size());
			
			// Chooses randomly a better neighbor from listLocalMoves 
			if (!listLocalMoves.isEmpty()) {
				
				int localMoveIndex = MyService.rand(0, listLocalMoves.size() - 1);
				MyAbstractLocalMove abstractLocalMove = listLocalMoves.get(localMoveIndex);
				
				if (abstractLocalMove instanceof MyAssignLocalMove) {
					int geneIndex = ((MyAssignLocalMove) abstractLocalMove).getGeneIndex();
					int geneValue = ((MyAssignLocalMove) abstractLocalMove).getGeneValue();
					individual.setGene(geneIndex, geneValue);
					System.out.println("Local Move : Assign Individual[" + geneIndex + "] = " + geneValue);
					
				} else if (abstractLocalMove instanceof MySwapLocalMove) {
					geneIndex1 = ((MySwapLocalMove) abstractLocalMove).getGeneIndex1();
					geneIndex2 = ((MySwapLocalMove) abstractLocalMove).getGeneIndex2();
					MyService.swapIndividual(individual, geneIndex1, geneIndex2);
					System.out.println("Local Move : Swap geneIndex1 = " + geneIndex1 + ", geneIndex2 = " + geneIndex2);
				}
				
			} else { // Restarts if list of local moves is empty
				System.out.println("\nRestart ...\n");
				individual = new MyIndividual(individual.getChromosomeLength(), individual.getMaxValue(), true);
			}
			
			individual.printGene();
			System.out.println("\nFitness value: " + individual.getFitness());
			System.out.println("Min Time: " + this.getMinTime() + "/// Makespan: " + individual.getTime());
			System.out.println("Min Cost: " + this.getMinCost() + "/// TotalCost: " + individual.getCost());
			System.out.println("Best Fitness : " + bestFitness);

		} while (iterationIndex < numIterations);
		return individual;
	}
	
	
	
	/**
	 * Calculates the lower boundary of Time and Cost 
	 */
	public void calcMinTimeCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
		this.minTime = MyService.calcMinTime(fogDevices, cloudletList);
		this.minCost = MyService.calcMinCost(fogDevices, cloudletList);
		
	}


	public double getMinTime() {
		return minTime;
	}


	public void setMinTime(double minTime) {
		this.minTime = minTime;
	}


	public double getMinCost() {
		return minCost;
	}


	public void setMinCost(double minCost) {
		this.minCost = minCost;
	}
	
	
}
