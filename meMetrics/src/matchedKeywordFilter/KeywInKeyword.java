/*
 * KeywInKeyword class
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
package matchedKeywordFilter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeywInKeyword {

	String serializedKeywordPath; // "serializedKeyword/1902080602.keyword";
	String serializedResultPath; //  "./matchResult/000000KO.filtrant2Result"
	String filtrantResultPath; // "./matchResult/000000KO.filtrant3Result"
	String filtrantReadablePath; // "./matchResult/000000KO.filtrant3Readable"
	String removedResultPath; // "./matchResult/000000KP.removed3Readable"


	public static void main(String[] args) throws IOException,
								InterruptedException, ClassNotFoundException{
		
		KeywInKeyword runInstance = new KeywInKeyword(args[0], args[1],
												args[2], args[3], args[4]);
	}

	public KeywInKeyword (String keywordInputFile,
					  		String matchIutputFile,
					  		String filtrantOutputFile,
					  		String filtrantOutputReadable,
					  		String removedOutputFile) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		LocalDateTime startTime = LocalDateTime.now();

		this.serializedKeywordPath = keywordInputFile;
		this.serializedResultPath = matchIutputFile;
		this.filtrantResultPath = filtrantOutputFile;
		this.filtrantReadablePath = filtrantOutputReadable;
		this.removedResultPath = removedOutputFile;


		//Deserialize keyword-oriented match result hash map
		HashMap<String, ArrayList<int[]>> deserializedResultMap=new HashMap<>();
		HashMap<String, ArrayList<int[]>> filtrantResultMap=new HashMap<>();
		//KeywordA; #(Number of Links)
		//paperIdx, fieldIdx, stingLoc[* times #]
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
												this.serializedResultPath));
		deserializedResultMap=(HashMap<String, ArrayList<int[]>>)ois.
																readObject();
		ois.close();

		filtrantResultMap.putAll(deserializedResultMap);

		System.out.println("Match result deserializing complete");		
		LocalDateTime loadingTime = LocalDateTime.now();
		System.out.print(startTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for loading match result.");


		//Deserialize keyword hash map
		HashMap<String, ArrayList<int[]>> deserializedKeywMap = new HashMap<>();
		//KeywordA; #(Number of UniProt entries having this keyword)
		//entryIdx, fieldIdx[* times #]
		ois = new ObjectInputStream(new FileInputStream(
												this.serializedKeywordPath));
		deserializedKeywMap=(HashMap<String, ArrayList<int[]>>)ois.readObject();
		ois.close();

		System.out.println("Keyword deserializing complete");		
		LocalDateTime loadingTime2 = LocalDateTime.now();
		System.out.print(loadingTime.until(loadingTime2,ChronoUnit.MILLIS));
		loadingTime = LocalDateTime.now();
		System.out.println(" ms elapsed for loading keywords.");

		Set<Map.Entry<String, ArrayList<int[]>>> resultEntrySet =
				  deserializedResultMap.entrySet();
		Set<Map.Entry<String, ArrayList<int[]>>> keywEntrySet =
				  deserializedKeywMap.entrySet();


		PrintWriter removedFileOutput =new PrintWriter(this.removedResultPath);
		
		for (Map.Entry<String, ArrayList<int[]>> matchEntry : resultEntrySet) {

			String matchedKeyword = matchEntry.getKey();
			ArrayList<int[]> matchInfo = matchEntry.getValue();
			HashSet<Integer> matchSet = new HashSet<>();
//			int matchKeywSize = matchedKeyword.length();
			if (matchedKeyword.contains(" ")) {

				String[] keywSplitArray = matchedKeyword.split(" ");
				int includedKeywStart = 0;
				for (String keywSplit :keywSplitArray) {

					if (deserializedResultMap.containsKey(keywSplit)) {

						ArrayList<int[]> includedMatch = deserializedResultMap.
															get(keywSplit);
						ArrayList<int[]> filteredMatch = new ArrayList<>(
															includedMatch);
						for (int[] aMatch :matchInfo) {

							int paperIdx = aMatch[0];
							int fieldIdx = aMatch[1];
							int matchStart = aMatch[2];
							for (int[] iMatch :includedMatch) {
								if (iMatch[0] == paperIdx &&
									iMatch[1] == fieldIdx &&	
									//TODO beware
									iMatch[2] == matchStart+includedKeywStart) {
									//TODO beware
									filteredMatch.remove(iMatch);
								}
							}
						}
						if (filteredMatch.isEmpty()) {

							removedFileOutput.print(keywSplit+"; ");
							for (int[] aMatch : includedMatch) {;

								matchSet.add(aMatch[0]);
							}
							removedFileOutput.print(matchSet.size());
							removedFileOutput.print("\n");
							filtrantResultMap.remove(keywSplit);
						} else {
							filtrantResultMap.put(keywSplit, filteredMatch);
						}
					}
					includedKeywStart += keywSplit.length()+1;
				}
			}
		}
		removedFileOutput.close();
		
		System.out.println("Match result filtering complete");		
		loadingTime2 = LocalDateTime.now();
		System.out.print(loadingTime.until(loadingTime2,ChronoUnit.MILLIS));
		loadingTime = LocalDateTime.now();
		System.out.println(" ms elapsed for match filtering.");
		
		PrintWriter filtrantReadableOutput = new PrintWriter(
											this.filtrantReadablePath);

		filtrantResultMap.entrySet().stream().forEach(matchEntry->{
			String aKeyword = matchEntry.getKey();
			HashSet<Integer> aMatchSet = new HashSet<>();
			filtrantReadableOutput.print(aKeyword+"; ");
			for (int[] aMatch : matchEntry.getValue()) {;
				aMatchSet.add(aMatch[0]);
			}
			filtrantReadableOutput.print(aMatchSet.size());
			filtrantReadableOutput.print("; ");
			filtrantReadableOutput.println(matchEntry.getValue().size());
		});
		filtrantReadableOutput.close();

		System.out.println(
				"Readable file is ready for keyword oriented details.");	

		// /* Output match results to file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
													this.filtrantResultPath));
		oos.writeObject(filtrantResultMap);
		oos.close();
		
		loadingTime2 = LocalDateTime.now();
		System.out.print(loadingTime.until(loadingTime2,ChronoUnit.MILLIS));
		loadingTime = LocalDateTime.now();
		System.out.println(" ms elapsed for filterd match serialization.");
		// */
	}
}
