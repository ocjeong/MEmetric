package meMatchMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;


public class DeserializeFile<T extends ArrayList<?>> implements Runnable {
	private String serializedFilePath;//./serializedProtein/1901180112.protein1
	private int maxThreadIndex = 8;
	private int callIndex = 0;
	private HashMap<Integer, T> outputHashMap = new HashMap<>();
	

	public DeserializeFile() {
	}
	public DeserializeFile(String filePath) {
		this.serializedFilePath = filePath;
	}
	public DeserializeFile(String filePath, int totalThreadNumber) {
		this.serializedFilePath = filePath;
		this.maxThreadIndex = totalThreadNumber;
			}
	
	public void run() {
		int threadIndex;	
		synchronized (this) {
			threadIndex = callIndex;
			addIndex();
		}
		int fileIndex = threadIndex;
		while (true) {
			try {
				ObjectInputStream ois=new ObjectInputStream(new FileInputStream(
					serializedFilePath+String.valueOf(fileIndex+1)));
				this.outputHashMap.put(fileIndex, (T) ois.readObject());
				ois.close();
				System.out.println(String.valueOf(fileIndex+1)+"th run in "
				  +"No."+String.valueOf(threadIndex)+" thread finished.");
				fileIndex += this.maxThreadIndex;
			}catch (IOException e) {
				System.out.println("No more file for "+
				  Thread.currentThread().getName());
				break;
			}catch (ClassNotFoundException e) {
				e.printStackTrace();
				break;
			} 
		}
		synchronized (this) {
			int activeThread = Thread.currentThread().getThreadGroup().
															activeCount();
			System.out.println(String.valueOf(activeThread)+" active threads");
			if (activeThread==1) {
				notify();
			}
		}
	}

	private void addIndex() {
		System.out.println("No."+String.valueOf(callIndex)+
				" thread is running.");
		callIndex++;
	}	

	public synchronized ArrayList<T> makeArray() {
		ArrayList<T> resultArray = new ArrayList<>();
		int hashSize = this.outputHashMap.size();
		for (int mapIndex = 0; mapIndex < hashSize; mapIndex++) {
			resultArray.add(this.outputHashMap.get(mapIndex));
		}
		this.outputHashMap = new HashMap<>();
		System.out.println("deserializing has been completed!!");		
		return resultArray;
	}
}
