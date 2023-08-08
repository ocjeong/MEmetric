/*
 * ExportReadableResult class
 * 
 * 검색된 2단어 이상 키워드 중에서 각 단어가 다른 1단어 키워드의 검색결과를 완전히 포함하는 경우를 
 * 찾아 포함된 키워드를 필터링합니다.
 * This class filters keywords of 1-gram having match result which is entirely 
 * included in the result of other multi-gram keyword.
 * 
 * 190314 WC Jung
 * 
 * input readable line; keyword-occurance HashMap (unsorted)
 * (1902230830KO.matchResult, 1902230830KO.matchReadable)
 * Keyword string; #(Number of Links from keyword to occurance in papers)
 * paper index, field id number, location in field[, # items]
 * 
 * output1 readable line; keyword-occurance HashMap (filtrant, unsorted)
 * (1902230830KO.matchResult, 1902230830KO.matchReadable)
 * Keyword string; #(Number of Links from keyword to occurance in papers)
 * paper index, field id number, location in field[, # items]
 * 
 * output2 readable line; keyword-occurance HashMap (removed, unsorted)
 * (1902230830KO.matchResult, 1902230830KO.matchReadable)
 * Keyword string; #(Number of Links from keyword to occurance in papers)
 * paper index, field id number, location in field[, # items]
 */
package meMatchMap;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ExportReadableResult {

	String serializedResultPath; // "./matchResult/000000KO.filtrant3Result"
	String paperIdFilePath; // "./matchResult/000000KO.filtrant3Result"
	String readableResultPath; // "./matchResult/000000KP.removed3Readable"


	public static void main(String[] args) throws IOException,
								InterruptedException, ClassNotFoundException{
		
		ExportReadableResult runInstance = new ExportReadableResult(args[0], args[1],
												args[2]);
	}

	public ExportReadableResult (
					  		String matchIutputFile,
					  		String paperIdFileName,
					  		String readableOutputFile) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		LocalDateTime startTime = LocalDateTime.now();

		this.serializedResultPath = matchIutputFile;
		this.paperIdFilePath = paperIdFileName;
		this.readableResultPath = readableOutputFile;

		// Load the numerical IDs of WoS papers
		BufferedReader rawFileBuffer = new BufferedReader(
									new FileReader(paperIdFilePath.toString()) );
		
		String bufferedLine;
		String[] lineAsArray;
		Boolean EndOfFile = false;
		
		ArrayList<String> paperIDs = new ArrayList<>();

		while (true) {
			
			bufferedLine = rawFileBuffer.readLine();

			try {
				lineAsArray = bufferedLine.split("\t");
				paperIDs.add(lineAsArray[0].substring(4)); // remove the front 'WOS:'

			}catch (NullPointerException case3) {//Null pointer when EoF
				EndOfFile = true;
				break; //End of file

			}
		}


		//Deserialize keyword-oriented match result hash map
		HashMap<String,ArrayList<int[]>>deserializedResultMap=new HashMap<>();
		//KeywordA; #(Number of Links)
		//paperIdx, fieldIdx, stingLoc[* times #]
		
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
												this.serializedResultPath));
		deserializedResultMap=(HashMap<String, ArrayList<int[]>>)ois.
																readObject();
		ois.close();

		Set<Map.Entry<String, ArrayList<int[]>>> resultEntrySet =
				  							deserializedResultMap.entrySet();
		
		PrintWriter readableFileOutput =new PrintWriter(
													this.readableResultPath);
		
		String matchedKeyword;

		for (Map.Entry<String, ArrayList<int[]>> matchEntry:resultEntrySet) {
			
			matchedKeyword = matchEntry.getKey();
			HashSet<String> paperIDnumSet = new HashSet<>();
			ArrayList<int[]> matchInfo = matchEntry.getValue();

			readableFileOutput.print(matchedKeyword+"\t");

			for (int[] aMatch :matchInfo) {
				paperIDnumSet.add(paperIDs.get(aMatch[0]));
			}

			readableFileOutput.println(String.join(";",paperIDnumSet));
		}
		
		readableFileOutput.close();

		System.out.println("Match result exporting complete");		
		LocalDateTime loadingTime = LocalDateTime.now();
		System.out.print(startTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for match exporting.");
		startTime = LocalDateTime.now();
		
	}
}
