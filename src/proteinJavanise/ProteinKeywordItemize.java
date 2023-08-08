/*
 * ProteinKeywordItemize class
 * 
 * 직렬화된 단백질/유전자 데이터 파일에서 해당 개체를 구분하는 단어를 찾아 해쉬맵에
 * 저장합니다.
 * extract keywords into keyword hash map
 * key is the keyword string and value is list of location with entry 
 */


package proteinJavanise;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import meMatchMap.DeserializeFile;

public class ProteinKeywordItemize {
	
	String serializedProteinPath; //"./serializedProtein/1901180112.protein";
	String readabledFilePath; //"./serializedKeyword/1903040545.testReable";
	String serializedFilePath; //"./serializedKeyword/1903040545.testKeyword";

	public static void main(String[] args) 
								throws InterruptedException, IOException{
		
		ProteinKeywordItemize runInstance = new ProteinKeywordItemize(
												args[0], args[1], args[2]);
	}
	
	public ProteinKeywordItemize (String proteinInputFile,
								  String readableOutputFile,
								  String serializedOutputFile
								) throws InterruptedException, IOException{
		
		LocalDateTime startTime = LocalDateTime.now();
//*		Deserialize protein Records
		this.serializedProteinPath = proteinInputFile;
		this.readabledFilePath = readableOutputFile;
		this.serializedFilePath = serializedOutputFile;
		
		int maxThreadIndex = Runtime.getRuntime().availableProcessors();
		System.out.println(maxThreadIndex+" processors available.");
		
		ThreadGroup proteinThreadGourp= new ThreadGroup("proteinThreadGourp");

		/*
		Pattern proteinDescriptionPattern = Pattern.
						compile("(?<=[^C]=)[^=]+(?=[;( {])");
		Pattern proteinGeneNamePattern = Pattern.
						compile("(?<==|, )[^,=]+(?=[;,( {)])");
*/
		Pattern proteinDescriptionPattern = Pattern.
						compile("(?<=[^C]=)[^=;{}]+(?=[;( {)])");
		Pattern proteinGeneNamePattern = Pattern.
						compile("(?<==|, )[^,=;{}]+(?=[;,( {)])");
//*///				
		ArrayList<Thread> proteinThreadList=new ArrayList<>(maxThreadIndex);
		DeserializeFile<ArrayedProteinRecord>proteinDeserialRun=new 
				DeserializeFile<>(this.serializedProteinPath,maxThreadIndex);
		
		for (int proteinThreadIndex = 0; proteinThreadIndex < maxThreadIndex;
												proteinThreadIndex++) {
			Thread proteinDeserialThread = new Thread(proteinThreadGourp,
														proteinDeserialRun);
			proteinDeserialThread.start();
			proteinThreadList.add(proteinDeserialThread);
		}
		
		for (Thread deserialThread: proteinThreadList) {
			deserialThread.join();
		}
		
		ArrayList<ArrayedProteinRecord> deserialProteinOutput =
											proteinDeserialRun.makeArray();

		LocalDateTime loadingTime1 = LocalDateTime.now();
		System.out.print(startTime.until(loadingTime1,ChronoUnit.MILLIS));
		System.out.println("\tms elapsed for loading.");
		
		//Extract keywords
		HashMap<String, ArrayList<int[]>>proteinKeywordMap=new HashMap<>();
		int proteinArrayIndex = 0;
		int proteinIndexTotal = 0;

		for (ArrayedProteinRecord deserialArray : deserialProteinOutput) {
			
			for (ProteinRecord aProtein: deserialArray) {
				
				ArrayList<String>proteinDescription = aProtein.get("DE");
				ArrayList<String>proteinGeneName = aProtein.get("GN");
				int keyworIdx = 0;
				
				if (proteinDescription == null && proteinGeneName == null) {
					continue;
				}
				
				try {
					for (String descriptionLine: proteinDescription) {
						Matcher proteinMatcher = proteinDescriptionPattern.
								matcher(descriptionLine);
						while (proteinMatcher.find()) {
						  ArrayList<int[]>proteinIndexList= new ArrayList<>();
						  int[] keywordEntry= {proteinIndexTotal,keyworIdx++};
						  proteinIndexList.add(keywordEntry);
						  proteinKeywordMap.merge(proteinMatcher.group().
										trim(), proteinIndexList,(v1, v2)->
										{v1.addAll(v2);return v1;} );
						}
					}
				}catch (NullPointerException e) {}
				
				try {
					for (String geneNameLine: proteinGeneName) {
						Matcher proteinMatcher = proteinGeneNamePattern.
								matcher(geneNameLine);
						while (proteinMatcher.find()) {
						  ArrayList<int[]>proteinIndexList= new ArrayList<>();
						  int[] keywordEntry= {proteinIndexTotal,keyworIdx++};
						  proteinIndexList.add(keywordEntry);
						  proteinKeywordMap.merge(proteinMatcher.group().
										trim(), proteinIndexList,(v1, v2)->
										{v1.addAll(v2);return v1;} );
						}
					}
				}catch (NullPointerException e) {}

				proteinIndexTotal++;
			}

			System.out.print(String.valueOf(proteinArrayIndex+1));
			System.out.println(" th protein file's been looked up.");
			proteinArrayIndex++;
		}

		System.out.format("%d keywords extracted.", proteinKeywordMap.size());
		System.out.println();
//*
		//Output keyword to file
		ObjectOutputStream oos= new ObjectOutputStream(new FileOutputStream(
													this.serializedFilePath));
		oos.writeObject(proteinKeywordMap);
		oos.close();
		System.out.println("Serialized file is ready for protein keywords.");	
//*/	
		PrintWriter readableFileOutput = new PrintWriter(
													this.readabledFilePath);

		proteinKeywordMap.entrySet().stream().sorted(
									Map.Entry.comparingByKey()).forEach(e->{
			try {
				printMapEntry(e, readableFileOutput);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
		readableFileOutput.close();

		System.out.println("Readable file is ready for protein keywords.");	

	}
	
	static void printMapEntry(Map.Entry<String,ArrayList<int[]>>keywordEntry,
			PrintWriter readableFileWriter) throws IOException{	
		String aKeyword = keywordEntry.getKey();
		ArrayList<int[]> keywordInfo = keywordEntry.getValue();
		readableFileWriter.print(aKeyword);
		readableFileWriter.println("; "+String.valueOf(
				keywordEntry.getValue().size()));
		
		for (int[] appearanceInfo: keywordInfo) {
			readableFileWriter.print(appearanceInfo[0]);
			readableFileWriter.print(", ");
			readableFileWriter.print(appearanceInfo[1]);
			readableFileWriter.print("; ");
		}
		readableFileWriter.println();
	}
}

