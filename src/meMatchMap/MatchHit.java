/* MatchHit class
 * 
 * 단백질 유전자 항목을 구분하는 단어가 각 논문 요약에 등장하는지를 테이블에 매핑합니다. 
 * This class maps the appearance of match hit between the protein-gene entity
 * and paper abstract on a table structured data.
 * 
 * - Make loop over or stream iterate every entity in the paper/protein data
 * 
 * input source
 * keyword pattern (lexicographic sorted)
 * paper data ("TI","ID","DE","AB" fields)
 * 
 * output readable line; paper-oriented HashMap in serialized file
 * (key-value pair; 1902221700.matchReadable, 1902081551.matchResult)
 * WoS raw file paper index; #(number of keyword matches)
 * matching word in paper, field id, location in field[, # times]
 */
package meMatchMap;

import proteinJavanise.*;
import paperJavanise.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.lang.NullPointerException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class MatchHit {

	String serializedKeywordPath; //"./serializedKeyword/1902080602.altkeyw01"
	String serializedPaperPath; //"./serializedPaper/1901180844.paper"
	String serializedFilePath; //"./matchResult/1902201630.alt01Result"
	String readableFilePath; //"./matchResult/1902201630.alt01Readable"


	public static void main(String[] args) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		MatchHit runInstance = new MatchHit(args[0], args[1],
											args[2], args[3], args[4]);
	}

	public MatchHit (String keywordInputFile,
					  String paperInputFile,
					  String serializedOutputFile,
					  String readableOutputFile,
					  String isTest) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		LocalDateTime startTime = LocalDateTime.now();
		ArrayedPaperRecord deserialPaperArray = new ArrayedPaperRecord();
//*		
		this.serializedKeywordPath = keywordInputFile;
		this.serializedPaperPath = paperInputFile;
		this.serializedFilePath = serializedOutputFile;
		this.readableFilePath = readableOutputFile;


		HashMap<String, ArrayList<int[]>> deserializedKeywordMap;
		
		//Deserialize protein keyword hash map
		ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream(this.serializedKeywordPath));
		deserializedKeywordMap=(HashMap<String,ArrayList<int[]>>)ois.
																readObject();
		ois.close();
		System.out.println("Keyword deserializing complete");

		LocalDateTime loadingTime = LocalDateTime.now();
		System.out.print(startTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for loading keyword.");

//*/
		//Deserialize paper Records
		int maxThreadIndex = Runtime.getRuntime().availableProcessors();
		System.out.println(maxThreadIndex+" processors available.");

		LocalDateTime load2StartTime = LocalDateTime.now();
		ThreadGroup paperThreadGourp = new ThreadGroup("paperThreadGourp");
		ArrayList<Thread> paperThreadList = new ArrayList<>(maxThreadIndex);
		DeserializeFile<ArrayedPaperRecord> paperDeserialRun = new
		  DeserializeFile<>(this.serializedPaperPath, maxThreadIndex);
		
		for (int paperThreadIndex = 0; paperThreadIndex < maxThreadIndex;
				paperThreadIndex++) {
			Thread paperDeserialThread = new Thread(paperThreadGourp,
														paperDeserialRun);
			paperDeserialThread.start();
			paperThreadList.add(paperDeserialThread);
		}
		
		for (Thread deserialThread: paperThreadList) {
			deserialThread.join();
		}
		
		ArrayList<ArrayedPaperRecord> deserialPaperOutput =
												paperDeserialRun.makeArray();
		
		for (ArrayedPaperRecord deserialArray : deserialPaperOutput) {
			deserialPaperArray.addAll(deserialArray);
		}

		if (isTest.equals("--test")){
			System.out.println("Test mode operating");		
			deserialPaperArray = new ArrayedPaperRecord(
										deserialPaperArray.subList(0, 500));
		} else{
			System.out.println(isTest + " : Normal mode operating");
		}
		
		paperDeserialRun = null;
		
		System.out.println("Paper deserializing complete :"
												+ deserialPaperArray.size());		
		loadingTime = LocalDateTime.now();
		System.out.print(load2StartTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for loading paper.");

		//Multi-thread matching code
		ThreadGroup matchingThreadGourp =new ThreadGroup("matchingThreadGourp");
		ArrayList<Thread> matchingThreadList=new ArrayList<>(maxThreadIndex);
		PaperMatchRun matchingRun = new PaperMatchRun(deserialPaperArray,
									deserializedKeywordMap, maxThreadIndex);
		
		for (int proteinThreadIndex = 0; proteinThreadIndex < maxThreadIndex;
												proteinThreadIndex++) {
			Thread matchingThread =new Thread(matchingThreadGourp, matchingRun);
			matchingThread.start();
			matchingThreadList.add(matchingThread);
		}
		
		for (Thread deserialThread: matchingThreadList) {
			deserialThread.join();
		}
		
		HashMap<Integer, ArrayList<String[]>> matchResult = matchingRun.
																outputHashMap;
		
		LocalDateTime endTime = LocalDateTime.now();
		System.out.print(loadingTime.until(endTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for matching.");	
		
		System.out.print(String.valueOf(matchResult.size()));
		System.out.println(" papers found having one or more matches.");

		//Output match results to file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
												this.serializedFilePath));
		oos.writeObject(matchResult);
		oos.close();
		System.out.println("Serialized file is ready for match details.");	
		
		PrintWriter readableFileOutput =new PrintWriter(
													this.readableFilePath);
		
		for (Map.Entry<Integer, ArrayList<String[]>>matchEntry: matchResult.
															entrySet() ) {
			
			readableFileOutput.print(matchEntry.getKey());
			readableFileOutput.println("; "+String.valueOf(matchEntry.
														getValue().size()));
			
			for (String[] appearanceInfo: matchEntry.getValue()) {
				
				for (String infoItem: appearanceInfo) {
					readableFileOutput.print(infoItem+", ");
				}
			
			}
			readableFileOutput.println();
		
		}
		readableFileOutput.close();
		
		System.out.println("Readable file is ready for match details.");	
		endTime = LocalDateTime.now();
		System.out.print(startTime.until(endTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed total.");		
	}
}

