package paperJavanise;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import meMatchMap.DeserializeFile;

public class PaperLookup {
	public static void main(String[] args) throws InterruptedException,
															IOException {
		LocalDateTime load2StartTime = LocalDateTime.now();

		ArrayedPaperRecord deserialPaperArray = new ArrayedPaperRecord();
		String serializedPaperPath ="./serializedPaper/1901180844.paper";
		int maxThreadIndex = 8;
		//Deserialize paper Records
		ThreadGroup paperThreadGourp = new ThreadGroup("paperThreadGourp");
		ArrayList<Thread> paperThreadList = new ArrayList<>(maxThreadIndex);
		DeserializeFile<ArrayedPaperRecord> paperDeserialRun = new
		  DeserializeFile<>(serializedPaperPath, maxThreadIndex);
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
		paperDeserialRun = null;
		deserialPaperOutput = null;

		System.out.println("Paper deserializing complete");		
		LocalDateTime loadingTime2 = LocalDateTime.now();
		System.out.print(load2StartTime.until(loadingTime2,ChronoUnit.MILLIS));
		System.out.println(" ms elapsed for loading.");
		
		BufferedReader paperIndexBuffer = new BufferedReader(new 
										InputStreamReader(System.in));
		int numRec = deserialPaperArray.size();
		System.out.println("Total "+numRec+" papers deserialized");		

		while (true) {
			System.out.println("Input index of paper to look up");
			String inputString = paperIndexBuffer.readLine();
			if (inputString == "exit") {
				break;				
			}
			try{ 
				PaperRecord recordSelected = deserialPaperArray.get(Integer.
													parseInt(inputString));
				ArrayList<String> fieldsToMatch = new ArrayList<>();
				String[] fieldKeyList = {"TI","ID","DE","AB"};
				for (String fieldKey : fieldKeyList) {
					ArrayList<String> ArrayedfieldString = new ArrayList<>();
					String fieldString = "";
					try {
						recordSelected.get(fieldKey).forEach(
										e->ArrayedfieldString.add(e.trim()));
						fieldString=String.join(" ",ArrayedfieldString);
					} catch (NullPointerException e ) {
						System.out.println("No item in "+fieldKey+" field");
						continue;
					} finally {
						fieldsToMatch.add(String.valueOf(fieldString));
					}
				}

				fieldsToMatch.forEach(e->System.out.println(e));

			} catch (NumberFormatException e) {
				System.out.println("Failed : Input integer");
			} catch (IndexOutOfBoundsException e) {
				System.out.println("Failed : Out of bounds");
			}
		}
	}
}