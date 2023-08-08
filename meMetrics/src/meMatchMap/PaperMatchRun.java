package meMatchMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import paperJavanise.*;
import proteinJavanise.*;


public class PaperMatchRun implements Runnable{
	private int maxThreadIndex = 8;
	private int callIndex = 0;
	private int cap = 590000;
	private int paperStartIndex = 0;
	public int paperCumulation = 0;
	public HashMap<Integer,ArrayList<String[]>>outputHashMap=new HashMap<>(cap);
	private ArrayedPaperRecord inputPaperList;
	private ArrayList<Pattern> proteinPatternList;
	
	public PaperMatchRun(ArrayedPaperRecord paperInput,HashMap<String,
			  ArrayList<int[]>> proteinKeywordInput, int threadNumber) {
		this.inputPaperList = paperInput;
		this.proteinPatternList = new ArrayList<>();
		this.maxThreadIndex = threadNumber;
		Set<String> proteinKeywordSet = proteinKeywordInput.keySet();
		for (String aKeyword : proteinKeywordSet) {
			Pattern proteinKeywordPattern = Pattern.compile(
							"(?i)\\b"+Pattern.quote(aKeyword)+"\\b");
			this.proteinPatternList.add(proteinKeywordPattern);
		}
		PatternComparator<Pattern> comaparePattern = new PatternComparator<>();
		proteinPatternList.sort(comaparePattern);
		System.out.println("Keyword patterning complete");	
	}
	public PaperMatchRun(ArrayedPaperRecord paperInput,HashMap<String,
		  ArrayList<int[]>>proteinKeywordInput,int threadNumber,int paperStart){
		this.inputPaperList = paperInput;
		this.proteinPatternList = new ArrayList<>();
		paperStartIndex = paperStart;
		Set<String> proteinKeywordSet = proteinKeywordInput.keySet();
		for (String aKeyword : proteinKeywordSet) {
			Pattern proteinKeywordPattern = Pattern.compile(
							"(?i)\\b"+Pattern.quote(aKeyword)+"\\b");
			this.proteinPatternList.add(proteinKeywordPattern);
		}
		PatternComparator<Pattern> comaparePattern = new PatternComparator<>();
		proteinPatternList.sort(comaparePattern);
		System.out.println("Keyword patterning complete");	
	}
	
	public void run() {
		int threadIndex;
		PaperRecord aPaperRecord = new PaperRecord();
		synchronized (this) {
			threadIndex = callIndex;
			addIndex();
		}
		int paperArrayIndex = threadIndex;
		while (true) {
			try {
				aPaperRecord = inputPaperList.get(paperArrayIndex);
			}catch (IndexOutOfBoundsException e) {
				System.out.println("No more paper record for "+
				  Thread.currentThread().getName());
				break;
			}
			ArrayList<String> fieldsToMatch = new ArrayList<>();
			ArrayList<String[]> paperMatchList = new ArrayList<>();
			String[] fieldKeyList = {"TI","ID","DE","AB"};
			for (String fieldKey : fieldKeyList) {
				ArrayList<String> ArrayedfieldString = new ArrayList<>();
				String fieldString = "";
				try {
					aPaperRecord.get(fieldKey).forEach(
									e->ArrayedfieldString.add(e.trim()));
					fieldString=String.join(" ",ArrayedfieldString);
				} catch (NullPointerException e ) {
					continue;
				} finally {
					fieldsToMatch.add(fieldString);
				}
			}

			for (Pattern proteinKeywordPattern : proteinPatternList) {
				int paperFieldIndex = 0;
				for (String paperFieldString: fieldsToMatch) {
					Matcher paperKeywordMatcher = proteinKeywordPattern.
											matcher(paperFieldString);
					while (paperKeywordMatcher.find()) {
						String matchRegex = paperKeywordMatcher.pattern().
																	pattern();
						String[] matchInfo = {
						  //matchRegex.substring(8, matchRegex.length()-4 ),
						  paperKeywordMatcher.group(),
						  String.valueOf(paperFieldIndex),
						  String.valueOf(paperKeywordMatcher.start())};
						paperMatchList.add(matchInfo);
					}
					paperFieldIndex++;
				}
			}
			if (!paperMatchList.isEmpty()) {
				int matchKey = paperArrayIndex + paperStartIndex;
				outputHashMap.put(matchKey, paperMatchList);
//				System.out.println(paperMatchList);
			}
			paperArrayIndex += maxThreadIndex;
			if (paperArrayIndex % (maxThreadIndex*10000) == threadIndex) {
				int idx = paperArrayIndex / (maxThreadIndex*10000);
				System.out.println(String.valueOf(idx)+" x 10000"+
				  " records processed in "+Thread.currentThread().getName());
			}
		}
	}

	private void addIndex() {
		System.out.println("No."+String.valueOf(callIndex)+
				" thread is running.");
		callIndex++;
	}	
/*
	public synchronized ArrayList<T> makeArray() {
		ArrayList<T> resultArray = new ArrayList<>();
		int hashSize = this.outputHashMap.size();
		for (int mapIndex = 0; mapIndex < hashSize; mapIndex++) {
			resultArray.add(this.outputHashMap.get(mapIndex));
		}
		this.outputHashMap=null;
		System.out.println("Protein deserializing has been completed!!");		
		return resultArray;
	}
*/
}
