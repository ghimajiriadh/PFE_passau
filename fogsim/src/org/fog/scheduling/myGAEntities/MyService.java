package org.fog.scheduling.myGAEntities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.fog.entities.FogDevice;
import org.fog.scheduling.MySchedulingAlgorithm;

public class MyService {
	
	
	/**
	 * Return a random value belongs to domain [min, max]
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public static int rand(int min, int max) {
        try {
            Random rn = new Random();
            int range = max - min + 1;
            int randomNum = min + rn.nextInt(range);
            return randomNum;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
	
	
	/**
     * Returns a copy of the object, or null if the object cannot
     * be serialized.
     */
    public static Object deepCopy(Object orig) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos = 
                    new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(orig);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in. 
            ObjectInputStream in = 
                new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        catch(ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }
        return obj;
    }
    
    
    /**
     * Shuffles an array
     * 
     * @param array
     */
    public static void shuffleArray(int[] array) {
    	int randomIndex, temp;
    	Random random = new Random();
    	for (int i = array.length - 1; i > 0; i--) {
    		randomIndex = random.nextInt(i + 1); // Element at i index can be unchanged (not be shuffled)
    		temp = array[randomIndex];
    		array[randomIndex] = array[i];
    		array[i] = temp;
    	}
    }
    
    
    
    /**
     * Creates random template only contains 2 digits : 0, 1
     * 
     * @param size : Size of random template
     * @param quantityDigitOne : Quantity of digits 1 in random template
     * @return The random template shuffled
     */
    public static int[] createRandomTemplate(int size, int quantityDigitsOne) {
    	int[] randomTemplate = new int[size];
    	for (int index = 0; index < size; index++) {
    		if (index < quantityDigitsOne)
    			randomTemplate[index] = 1;
    		else 
    			randomTemplate[index] = 0;
    	}
    	
    	// Shuffles the random template
    	shuffleArray(randomTemplate);
    	
    	return randomTemplate;
    }
    
	/**
	 * Calculates the lower boundary of cost (the possible minimum cost)
	 * 
	 * @param fogDevices
	 * @param cloudletList
	 * @return Lower boundary of cost
	 */
    public static double calcMinCost(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
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


	/**
	 * Calculates the cost when executing a specified task (cloud-let) on a specified fog device
	 * 
	 * @param cloudlet
	 * @param fogDevice
	 * @return 
	 */
    public static double calcCost(Cloudlet cloudlet, FogDevice fogDevice) {
		double cost = 0;
		// The processing cost
		cost += fogDevice.getCharacteristics().getCostPerSecond() * cloudlet.getCloudletLength() / fogDevice.getHost().getTotalMips();
		// The memory cost
		cost += fogDevice.getCharacteristics().getCostPerMem() * cloudlet.getMemRequired();
		// The bandwidth cost
		cost += fogDevice.getCharacteristics().getCostPerBw() * (cloudlet.getCloudletFileSize() + cloudlet.getCloudletOutputSize());
		return cost;
	}


	/**
	 * Calculates the lower boundary of time (the possible minimum make-span)
	 * 
	 * @param fogDevices
	 * @param cloudletList
	 * @return
	 */
    public static double calcMinTime(List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {
		double minTime = 0;
		double totalLength = 0;
		double totalMips = 0;
		for(Cloudlet cloudlet : cloudletList) {
			totalLength += cloudlet.getCloudletLength();
		}
		for(FogDevice fogDevice : fogDevices) {
			totalMips += fogDevice.getHost().getTotalMips();
		}

		// Catch divided by zero exception
		try {
			minTime = totalLength / totalMips;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return minTime;
	}
    
    
    
	/**
	 * Calculates fitness of an specified individual.
	 * 
	 * @param individual : The individual to evaluate
	 * @param minTime : The lower boundary of time
	 * @param minCost : The lower boundary of cost
	 * @return The fitness of the individual
	 */
	public static double calcFitness(MyIndividual individual, double minTime, double minCost, List<FogDevice> fogDevices, List<? extends Cloudlet> cloudletList) {

		// clear task list of each Fog Device before calculating fitness again
		for(FogDevice fogDevice : fogDevices) {
			fogDevice.getCloudletListAssignment().clear();
		}

		// Set again the task list for each fogDevice from the specified individual's chromosome
		for (int geneIndex = 0; geneIndex < individual.getChromosomeLength(); geneIndex++) {
			fogDevices.get(individual.getGene(geneIndex)) // Gets fog device
			.getCloudletListAssignment().add(cloudletList.get(geneIndex)); // Assigns task to the fog device's task list 
		}

		//Calculates the make-span and cost
		double makespan = 0;
		double execTime = 0;
		double totalCost = 0;
		for(FogDevice fogDevice : fogDevices) {
			double totalLength = 0;
			for(Cloudlet cloudlet : fogDevice.getCloudletListAssignment()) {
				totalLength += cloudlet.getCloudletLength();
				// the total cost is sum of the cost execution of each cloudlet
				totalCost += MyService.calcCost(cloudlet, fogDevice);
			}
			// execTime is the time that sfogDevice finishes its list cloudlet assignment
			execTime = totalLength / fogDevice.getHostList().get(0).getTotalMips();
			// make-span is defined as when the last cloudlet finished or when all fogDevices finish its work.
			if (execTime > makespan) {
				makespan = execTime;
			}
		}

		//Store makespan
		individual.setTime(makespan);
		
		//Store cost
		individual.setCost(totalCost);

		// Calculate fitness
		double fitness = MySchedulingAlgorithm.TIME_WEIGHT * minTime / makespan 
				+ (1 - MySchedulingAlgorithm.TIME_WEIGHT) * minCost / totalCost;

		// Store fitness
		individual.setFitness(fitness);
		return fitness;
	}
    
    public static void swapIndividual(MyIndividual individual, int geneIndex1, int geneIndex2) {
    	int temp = individual.getGene(geneIndex1);
    	individual.setGene(geneIndex1, individual.getGene(geneIndex2));
    	individual.setGene(geneIndex2, temp);
    }
    
	
	public static void main(String[] args) {
		Arrays a;
		
	}
}
