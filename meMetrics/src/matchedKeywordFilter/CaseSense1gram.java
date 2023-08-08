/*
 * CaseSense1gram class
 * 
 * 검색된 1-gram 카워드 중에서 4자 이하인 것들에 대소문자 구별하는 규칙을 정해 필터링합니다.
 * This class filters keywords of 1-gram having different cases of spelling to 
 * those of UniProt-extracted keyword.
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

public class CaseSense1gram {

	String serializedKeywordPath; //"./serializedKeyword/000000.altkeyw01"
	String serializedResultPath; //  "./matchResult/000000KO.matchResult"
	String filtrantResultPath; // "./matchResult/000000KO.filtrant1Result"
	String removedResultPath; //  "./matchResult/000000KP.removed1Readable"


	public static void main(String[] args) throws IOException,
								InterruptedException, ClassNotFoundException{
		
		CaseSense1gram runInstance = new CaseSense1gram(args[0], args[1],
														args[2], args[3] );
	}

	public CaseSense1gram (String keywordInputFile,
					  String matchIutputFile,
					  String filtrantOutputFile,
					  String removedOutputFile) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		LocalDateTime startTime = LocalDateTime.now();

		this.serializedKeywordPath = keywordInputFile;
		this.serializedResultPath = matchIutputFile;
		this.filtrantResultPath = filtrantOutputFile;
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

		PrintWriter removedFileOutput =new PrintWriter(removedResultPath);
		
		for (Map.Entry<String, ArrayList<int[]>> matchEntry : resultEntrySet){
		    // System.out.println(matchEntry.getKey());
			String matchedKeyword = matchEntry.getKey();
			ArrayList<int[]> matchInfo = matchEntry.getValue();
			HashSet<Integer> matchSet = new HashSet<>();
			
			if (matchedKeyword.length() < 5 && !matchedKeyword.contains(" ")){
				
				if (deserializedKeywMap.containsKey(matchedKeyword)) {
					continue;
				
				} else {
					String lowerFirst = matchedKeyword.substring(0, 1).
								toLowerCase() + matchedKeyword.substring(1);
					
					if (deserializedKeywMap.containsKey(lowerFirst)
							&& matchedKeyword.length() > 2) {
						continue;
					
					} else {
						removedFileOutput.print(matchedKeyword+"; ");
						for (int[] aMatch : matchInfo) {;
							matchSet.add(aMatch[0]);
						}
						removedFileOutput.print(matchSet.size());
						removedFileOutput.print("\n");
						/*
						matchSet.forEach(v->{
							removedFileOutput.print(v);
							removedFileOutput.print(", ");
						});
						removedFileOutput.print("\n");
						*/
						filtrantResultMap.remove(matchedKeyword);
					}
				}
			}
		}

		System.out.println("Match result filtering complete");		
		loadingTime2 = LocalDateTime.now();
		System.out.print(loadingTime.until(loadingTime2,ChronoUnit.MILLIS));
		loadingTime = LocalDateTime.now();
		System.out.println(" ms elapsed for match filtering.");
		
		removedFileOutput.close();
		//Output match results to file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
													this.filtrantResultPath));
		oos.writeObject(filtrantResultMap);
		oos.close();
		
		loadingTime2 = LocalDateTime.now();
		System.out.print(loadingTime.until(loadingTime2,ChronoUnit.MILLIS));
		loadingTime = LocalDateTime.now();
		System.out.println(" ms elapsed for filterd match serialization.");
	}
}
