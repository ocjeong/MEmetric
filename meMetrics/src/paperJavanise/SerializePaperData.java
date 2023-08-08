/*
 * SerializePaperData class
 * 
 * 논문 요약 정보 데이터를 직렬화(Serialize)된 파일로 저장합니다.
 * This class saves the the paper abstracts records from raw data files into
 * serialized java data file.
 * 
 * 190118 WC Jung
 * 
 * - raw file reading stream / output stream
 * - read each line and split into key and value of paper attribute into hashed
 * 	map structure.
 * - serialize & save the loaded data 
 */
package paperJavanise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Stream;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.temporal.ChronoUnit;


public class SerializePaperData {
// /home/wcjung/Work/meMetrics/paperRec2/paperRecordTxts2_01
	String paperRawFileLocation; //"./paperRec2";
	String serializedFileName; //"./serializedPaper/190211.dummyPaper1";
	String paperIdFileName; //"./matchResult/1902201630.alt01Readable"

	
	public static void main(String[] args) 
				throws IOException, InterruptedException{

		SerializePaperData runInstance = new SerializePaperData(
												args[0], args[1], args[2]);
	}

	public SerializePaperData ( // Constructor
		  String inputDir, String serializedFilePath, String paperIdFilePath)
								throws IOException, InterruptedException{

	// public static void main(String[] args) throws IOException {
		LocalDateTime startTime = LocalDateTime.now();

		this.paperRawFileLocation = inputDir;
		this.serializedFileName = serializedFilePath;
		this.paperIdFileName = paperIdFilePath;

		File rawFileFolder = new File(this.paperRawFileLocation);
		Stream<Path> rawPathStream = Files.walk(rawFileFolder.toPath());
		ArrayedPaperRecord aPaperEntryArray = new ArrayedPaperRecord();
		rawPathStream.forEach((rawPath)->
			{
//				System.out.println(rawPath.toString());
				try {
					loadRawFile(rawPath, aPaperEntryArray);
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (NullPointerException e2) {
					// TODO Auto-generated catch block
//					e2.printStackTrace();
				}
			} );
		rawPathStream.close();
		int arrayedRecordlength = aPaperEntryArray.size();
		/* Test code.
		System.out.print("Number of records is ");
		System.out.println(arrayedRecordlength);
		aPaperEntryArray.get(0).printRecord();
		System.out.println(" ");
		aPaperEntryArray.get(1).printRecord();
		System.out.println(" ");
		aPaperEntryArray.get(2).printRecord();
		System.out.println(" ");
		aPaperEntryArray.get(arrayedRecordlength-3).printRecord();
		System.out.println(" ");
		aPaperEntryArray.get(arrayedRecordlength-2).printRecord();
		System.out.println(" ");
		aPaperEntryArray.get(arrayedRecordlength-1).printRecord();
				//*/
		LocalDateTime loadingTime = LocalDateTime.now();
		System.out.print(startTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println("\tms elapsed for loading.");		
		System.out.println(String.valueOf(arrayedRecordlength)+
				"\tpapers are loaded in a list instance.");		

		//* Serialization
		int serializedFileIndex = 1;
		int chunkSize = 50000;
		int proteinEntryIndex = 0;
		ArrayedPaperRecord chunkedPaperArray = new ArrayedPaperRecord();

		
		PrintWriter paperIdFileOutput =new PrintWriter(
													this.paperIdFileName);
	    
	    for (int i = 0; i < arrayedRecordlength; i++) {
	        
	        PaperRecord aPaperRecord = aPaperEntryArray.get(i);

			ArrayList<String> fieldsToPrint = new ArrayList<>();
			String[] fieldKeyList = {"UT","DI","PM","PY","PD"};
			String paperString = "";

			for (String fieldKey : fieldKeyList) {
				
				ArrayList<String> ArrayedfieldString = new ArrayList<>();
				String fieldString = "";
				
				try {
					aPaperRecord.get(fieldKey).forEach(
									e->ArrayedfieldString.add(e.trim()));
					fieldString = String.join(";",ArrayedfieldString);
				
				} catch (NullPointerException e ) {
					continue;
				
				} finally {
					fieldsToPrint.add(fieldString);
				
				}
			}
			
			paperString = String.join("\t",fieldsToPrint);
			paperIdFileOutput.println(paperString);


	    }
	    paperIdFileOutput.close();

		System.out.print(startTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println("\tms elapsed for exporting paper IDs.");		


		// while (true) {
			
		// 	String fileIndexString = String.valueOf(serializedFileIndex);
		// 	ObjectOutputStream oos = new ObjectOutputStream(
		// 	  new FileOutputStream(this.serializedFileName+fileIndexString));
			
		// 	try {//subList method returns object which cannot be serialized
		// 		chunkedPaperArray.addAll(aPaperEntryArray.
		// 		  subList(proteinEntryIndex, proteinEntryIndex+chunkSize));

		// 	}catch (IndexOutOfBoundsException indexE) {
		// 		chunkedPaperArray.addAll(aPaperEntryArray.
		// 		  subList(proteinEntryIndex, aPaperEntryArray.size()));
		// 		break;

		// 	}finally {
		// 		oos.writeObject(chunkedPaperArray);
		// 		oos.close();
		// 		System.out.println("No."+fileIndexString+
		// 				"\tserialized file is ready.");	
		// 		chunkedPaperArray.clear();
		// 		serializedFileIndex++;
		// 		proteinEntryIndex += chunkSize;
				
		// 	}
		// }
		
		LocalDateTime endTime = LocalDateTime.now();
		System.out.print(loadingTime.until(endTime,ChronoUnit.MILLIS));
		System.out.println("\tms elapsed for serializing.");
		//*/

	}
	
	// static void loadRawFile(Path rawFilePath, ArrayedPaperRecord aPaperArray)
	static void loadRawFile(Path rawFilePath, ArrayedPaperRecord aPaperArray)
	  throws IOException {	
		LocalDateTime loadStartTime = LocalDateTime.now();

		BufferedReader rawFileBuffer = new BufferedReader(new FileReader(
				rawFilePath.toString()));
		
		String bufferedLine;
		String[] lineAsArray;
		PaperRecord aPaperEntry = new PaperRecord();
		String keyFromline = null;
		ArrayList<String> valueFromLine;
		Boolean EndOfFile = false;
		int ProteinRecordIndex = 0;

		bufferedLine = rawFileBuffer.readLine();	//FN
		bufferedLine = rawFileBuffer.readLine();	//VR
		while (true) {
			bufferedLine = rawFileBuffer.readLine();
			try {
				lineAsArray = bufferedLine.split("(?<=^\\w{2})", 2);
			}catch (NullPointerException case3) {//Null pointer when EoF
			//	System.out.println("End of File.");
				EndOfFile = true;	break;//End of file
			}
			//System.out.println("While loop here,");
			valueFromLine = new ArrayList<>();
			try {
				valueFromLine.add(lineAsArray[1]);
				keyFromline = lineAsArray[0];
//				System.out.println(keyFromline);
				aPaperEntry.put(keyFromline, valueFromLine);
			}catch (ArrayIndexOutOfBoundsException case2) {
				if (bufferedLine.startsWith(" ")) {
				  //When no header but having value
//					System.out.println(keyFromline);
					aPaperEntry.get(keyFromline).add(lineAsArray[0]);
				}else {//No header, no Value indicating end of a record
//					System.out.println("End of record.");
					aPaperArray.add(aPaperEntry);
					aPaperEntry = new PaperRecord();
					ProteinRecordIndex++;
				}
			}
		}
				
		rawFileBuffer.close();
		/* lap time
		LocalDateTime loadEndTime = LocalDateTime.now();
		System.out.print(ProteinRecordIndex);		
		System.out.println("\trecord(s) loaded.");		
		System.out.print(loadStartTime.until(loadEndTime,ChronoUnit.MILLIS));
		System.out.println("\tms elapsed.");
		//*/	
	}

}
