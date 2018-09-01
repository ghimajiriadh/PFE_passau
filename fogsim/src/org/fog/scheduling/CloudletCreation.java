package org.fog.scheduling;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.fog.scheduling.gaEntities.Service;	

public class CloudletCreation {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;
	public static final int NUMBER_OF_CLOUDLETS = 500;
	
	// Delimiter used in CSV file
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";
    
    // The boundary of task attribute
    // Length of task (MIPS)
    public static final long LENGTH_MIN = 10000;
    public static final long LENGTH_MAX = 100000;
    // File size (MB)
    public static final long FILESIZE_MIN = 10;
    public static final long FILESIZE_MAX = 100;
    // Output size (MB)
    public static final long OUTPUTSIZE_MIN = 10;
    public static final long OUTPUTSIZE_MAX = 100;
    // Memmory (Ram) required size (MB)
    public static final long MEM_REQUIRED_MIN = 50;
    public static final long MEM_REQUIRED_MAX = 200;
    
 
//    private static final String FILE_HEADER = "id,length,filesize,outputsize";
    
    public static final String FILENAME_CLOULET = "data/data" + NUMBER_OF_CLOUDLETS;

	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		Log.printLine("Starting Cloudlet Creation program.");
		Log.printLine("The data will be stored in a xml file.");

		cloudletList = createCloudlet(NUMBER_OF_CLOUDLETS, 0); // creating NUMBER_OF_CLOUDLETS cloudlets
		writeCsvFile(FILENAME_CLOULET, cloudletList);	
	}

	private static List<Cloudlet> createCloudlet(int cloudlets, int idShift) {
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();

		// cloudlet parameters
		long length = 2000;
		long fileSize = 50;
		long outputSize = 50;
		int pesNumber = 1;
		long memRequired = 100;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for (int i = 0; i < cloudlets; i++) {
			length = Service.rand((int) LENGTH_MIN, (int) LENGTH_MAX);
			fileSize = Service.rand((int) FILESIZE_MIN, (int) FILESIZE_MAX);
			outputSize = Service.rand((int) OUTPUTSIZE_MIN, (int) OUTPUTSIZE_MAX);
			memRequired = Service.rand((int) MEM_REQUIRED_MIN, (int) MEM_REQUIRED_MAX);
			cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, memRequired, utilizationModel,
					utilizationModel, utilizationModel);
			list.add(cloudlet[i]);
		}

		return list;
	}
	
	public static void writeCsvFile(String fileName, List<Cloudlet> listCloudlet) {
         
        FileWriter fileWriter = null;
 
        try {
            fileWriter = new FileWriter(fileName);
 
//            // Write the CSV file header
//            fileWriter.append(FILE_HEADER);
// 
//            // Add a new line separator after the header
//            fileWriter.append(NEW_LINE_SEPARATOR);
// 
            // Write a new Country object list to the CSV file
            for (Cloudlet cloudlet : listCloudlet) {
                fileWriter.append(String.valueOf(cloudlet.getCloudletId()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(cloudlet.getCloudletLength()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(cloudlet.getCloudletFileSize()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(cloudlet.getCloudletOutputSize()));
                fileWriter.append(COMMA_DELIMITER);
                fileWriter.append(String.valueOf(cloudlet.getMemRequired()));
                fileWriter.append(NEW_LINE_SEPARATOR);
            }
 
            System.out.println("CSV file was created successfully !!!");
 
        } catch (Exception e) {
            System.out.println("Error in CsvFileWriter !!!");
            e.printStackTrace();
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while flushing/closing fileWriter !!!");
                e.printStackTrace();
            }
        }
    }
	
}
