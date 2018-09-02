package ontology_creator;

import java.util.Scanner;

public class Start {
	
	public static void main(String[] args) {
//		Scanner sc = new Scanner(System.in);
//		
//		System.out.print("Input plant name: ");
//		String plantName = sc.nextLine();
//		System.out.print("[For Unit Operations] Input the CSV file generated by the Excel Macro: ");
//		String aswFile = sc.nextLine();
//		System.out.print("[For Heat Exchangers] Input the CSV file specifying the side (shell/tube) the utility streams are in: ");
//		String heatXfile = sc.nextLine();
//		System.out.print("[For Pipes and Streams] Input the TXT file (tab delimited) specifying the necessary details of the chemical species: ");
//		String chemSpecFile = sc.nextLine();
//		System.out.print("[For Feed, Product, and Waste Streams] Input the CSV file specifying the feed, product, and waste streams: ");
//		String specialStreamsFile = sc.nextLine();
//		System.out.print("[For Utility Streams] Input the starting number (6 digits) for the utility streams: ");
//		int utilStartNum = Integer.parseInt(sc.nextLine());
//		
//		sc.close();

		String plantName = "MTBE-Plant";
		String aswFile = "CSV Files/MTBE-Plant_CSV_2.csv";
		String heatXfile = "CSV Files/All_v2_HE_CSV.csv";
		String chemSpecFile = "CSV Files/All_v2_chemSpec.txt";
		String specialStreamsFile = "CSV Files/All_v2_SpecialStream_CSV.csv";
		int utilStartNum = 350201;
		
		Plant chemicalPlant = new Plant(plantName, aswFile, heatXfile, chemSpecFile, specialStreamsFile, utilStartNum);
		chemicalPlant.createOntology();
		
		System.out.println("Process completed. Created OWL files can be found at the same folder as this program.");
		
	}

}