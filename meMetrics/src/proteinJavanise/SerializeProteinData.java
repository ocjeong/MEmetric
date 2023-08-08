/*
 * SerializeProteinData class
 * 
 * 단백질/유전자 데이터를 직렬화(Serialize)된 파일로 저장합니다.
 * This class saves the the protein-gene records from raw data files into
 * serialized java data file.
 * 
 * 190112 WC Jung
 * 
 * - raw file reading stream / output stream
 * - read each line and split it into a key and value attribute of protein in
 * 	a hash structured data.
 * - serialize & save the loaded data 
 */

package proteinJavanise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Pattern;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.ArrayIndexOutOfBoundsException;
import java.lang.NullPointerException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;	

public class SerializeProteinData {

	String proteinFileLocation;
	String rawFileName;
	String serializeToPath;
	String serializedFileName;
	// String proteinFileLocation = "./proteinRec/";
	// String rawFileName = "dummy_proteinRecord";
	// String serializeToPath = "./serializedProtein/";
	// String serializedFileName = "190211.dummyProtein";

	
	public static void main(String[] args)
				throws IOException, InterruptedException{

		SerializeProteinData runInstance = new SerializeProteinData(
										args[0], args[1], args[2], args[3]);
	}

	public SerializeProteinData ( // Constructor
				String inputDir, String inputFileName,
				String outputDir, String outputFileName)
				throws IOException, InterruptedException{

		LocalDateTime startTime = LocalDateTime.now();

		this.proteinFileLocation = inputDir;
		this.rawFileName = inputFileName;
		this.serializeToPath = outputDir;
		this.serializedFileName = outputFileName;

		BufferedReader rawFileBuffer = new BufferedReader(new FileReader(
											proteinFileLocation+rawFileName));
		String bufferedLine;
		String[] lineAsArray;
		ArrayedProteinRecord aProteinEntryArray = new ArrayedProteinRecord();
		ProteinRecord aProteinEntry = new ProteinRecord();
		String keyFromline = null;
		ArrayList<String> valueFromLine;
		
		int chunkIndex = 1;
		String indexString;
		Boolean EndOfFile = false;
		while (EndOfFile != true) {
			LocalDateTime loopStartTime = LocalDateTime.now();
			indexString = String.valueOf(chunkIndex);
			int ProteinRecordIndex = 0;
			while (ProteinRecordIndex < 50000) {
				bufferedLine = rawFileBuffer.readLine();
				try {
					lineAsArray = bufferedLine.split("(?<=^\\w{2})   ", 2);
				}catch (NullPointerException case3) {
					EndOfFile = true;
					break;//End of file
				}
				try {
					valueFromLine = new ArrayList<>();
					//No updated without two members in array.(go to case2)
					valueFromLine.add(lineAsArray[1]);
					aProteinEntry.putIfAbsent(lineAsArray[0], valueFromLine)
					  //By here WITHOUT the key in the map.(go to case1)
					  .add(lineAsArray[1]);	
				}catch (NullPointerException case1) {
					keyFromline = lineAsArray[0];
				}catch (ArrayIndexOutOfBoundsException case2) {
					if (bufferedLine.charAt(0) == ' ') {
						aProteinEntry.get(keyFromline).add(lineAsArray[0]);
					}else if (bufferedLine.charAt(0) == '/') {
						aProteinEntryArray.add(aProteinEntry);
						aProteinEntry = new ProteinRecord();
						ProteinRecordIndex++;
					}
				}
			}
			LocalDateTime loadedTime = LocalDateTime.now();
			System.out.println("No."+indexString+" file with "
								+ProteinRecordIndex+" records");
			System.out.print(loopStartTime.until(loadedTime,ChronoUnit.MILLIS));
			System.out.println(" ms elapsed for loadinng.");

			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream
					  (serializeToPath+serializedFileName+indexString));
			oos.writeObject(aProteinEntryArray);
			oos.close();
			aProteinEntryArray.clear();
			chunkIndex++;
			
			System.out.print(loadedTime.until(LocalDateTime.now(),
					ChronoUnit.MILLIS));
			System.out.println(" ms elapsed for serializing.");
		}
		rawFileBuffer.close();

		/* Test code
		int ArrayedEntrySize = aProteinEntryArray.size();
		aProteinEntryArray.get(0).forEach(
				(k, v) -> {v.add(0, k); System.out.println(v);} );
		System.out.println("_________________________________________________");
		aProteinEntryArray.get(1).forEach(
				(k, v) -> {v.add(0, k); System.out.println(v);} );
		System.out.println("_________________________________________________");
		aProteinEntryArray.get(2).forEach(
				(k, v) -> {v.add(0, k); System.out.println(v);} );
		System.out.println("_________________________________________________");
		aProteinEntryArray.get(ArrayedEntrySize-3).forEach(
				(k, v) -> {v.add(0, k); System.out.println(v);} );
		System.out.println("_________________________________________________");
		aProteinEntryArray.get(ArrayedEntrySize-2).forEach(
				(k, v) -> {v.add(0, k); System.out.println(v);} );
		System.out.println("_________________________________________________");
		aProteinEntryArray.get(ArrayedEntrySize-1).forEach(
				(k, v) -> {v.add(0, k); System.out.println(v);} );
		//*/

		System.out.print(startTime.until(LocalDateTime.now(),
				ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for total.");	
	}
	
}