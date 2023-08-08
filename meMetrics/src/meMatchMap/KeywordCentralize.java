/**
 * KeywordCentralize class
 * 
 * 이 클래스는 단백질-논문 링크 검색결과 데이터를 바탕으로 기초적인 통계를 내어 텍스트파일로 저장합니다.
 * This class prints basic statistics of match results into text readable file
 * 190223 @author wcjung
 *
 * input readable line; paper-occurance HashMap in serialized file
 * (key-value pair; 1902081551.matchResult, 1902221700.matchReadable)
 * WoS raw file paper index; #(number of links from paper to keyword)
 * matching word in paper, field id number, location in field[, # items]
 * 
 * output readable line; keyword-occurance HashMap (unsorted)
 * (1902230830KO.matchResult, 1902230830KO.matchRadable)
 * Keyword string; #((Number of Links from keyword to occurance in papers)
 * paper index, field id number, location in field[, # items]
 */
package meMatchMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class KeywordCentralize {

	String serializedResultPath; //"./matchResult/1902081551.matchResult"
	String serializedFilePath; //"./matchResult/1902230830KO.matchResult"
	String readableFilePath; //"./matchResult/1902230830KO.matchRadable"


	public static void main(String[] args) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		KeywordCentralize runInstance = new KeywordCentralize(args[0], args[1],
																	args[2] );
	}

	public KeywordCentralize (String serializedInputFile,
							String serializedOutputFile,
							String readableOutputFile ) throws IOException,
								ClassNotFoundException, InterruptedException{
		
		LocalDateTime startTime = LocalDateTime.now();
//*		
		this.serializedResultPath = serializedInputFile;
		this.serializedFilePath = serializedOutputFile;
		this.readableFilePath = readableOutputFile;

		//Deserialize match result hash map
		HashMap<Integer, ArrayList<String[]>> deserializedResultMap = new 
																HashMap<>();
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
												this.serializedResultPath));
		deserializedResultMap=(HashMap<Integer, ArrayList<String[]>>)ois.
																readObject();
		ois.close();
		System.out.println("Match result deserializing complete");

		LocalDateTime loadingTime = LocalDateTime.now();
		System.out.print(startTime.until(loadingTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for loading match result.");

		//Convert paper-oriented map into keyword-oriented map
		int totalLinks = 0;
		HashMap<String, ArrayList<int[]>> keywordOrientedMap = new HashMap<>();

		for(Map.Entry<Integer, ArrayList<String[]>> entry: 
										deserializedResultMap.entrySet()) {
			
			int paperIndex = entry.getKey();
			TreeSet<String> keywSet = new TreeSet<>();
			
			for (String[] e : entry.getValue()) {
				
				ArrayList<int[]> paperInfo = new ArrayList<>();
				int[] paperInfoEntry = {paperIndex, Integer.parseInt(e[1]),
												   Integer.parseInt(e[2])};
				paperInfo.add( paperInfoEntry );
				keywordOrientedMap.merge(e[0], paperInfo, (v1, v2)->{
												v1.addAll(v2); return v1;});
				keywSet.add(e[0]);
			}

			totalLinks += keywSet.size();
		}

		System.out.println("Match result converting complete");		
		LocalDateTime loadingTime2 = LocalDateTime.now();
		System.out.print(loadingTime.until(loadingTime2,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for match converting.");
		System.out.print(String.valueOf(keywordOrientedMap.size()));
		System.out.println(" keywords found having one or more matches.");
		System.out.print(String.valueOf(deserializedResultMap.size()));
		System.out.println(" papers found having one or more matches.");
		System.out.print(String.valueOf(totalLinks));
		System.out.println(" links between keywords and papers found.");

		//*
		//Output match results to file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(
													this.serializedFilePath));
		oos.writeObject(keywordOrientedMap);
		oos.close();
		System.out.println(
				"Serialized file is ready for keyword oriented details.");	
		
		PrintWriter readableFileOutput=new PrintWriter(this.readableFilePath);
		
		keywordOrientedMap.entrySet().stream().forEach(matchEntry->{

			readableFileOutput.print(matchEntry.getKey());
			readableFileOutput.println("; "+String.valueOf(matchEntry.
														getValue().size()));
			for (int[] appearanceInfo: matchEntry.getValue()) {
				for (int infoItem: appearanceInfo) {
					readableFileOutput.print(String.valueOf(infoItem)+", ");
				}
			}
			readableFileOutput.println();

		});
		
		readableFileOutput.close();
		System.out.println(
				"Readable file is ready for keyword oriented details.");	
	
		LocalDateTime endTime = LocalDateTime.now();
		System.out.print(startTime.until(endTime,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed total.");		
		//*/
	}

}

