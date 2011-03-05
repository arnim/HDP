package de.uni_leipzig.informatik.asv.hdp;

import java.util.ArrayList;
import java.util.Collections;

public class DOCState {
	
	static int idCounter = 0;
	
	int docID, documentLength, numberOfTables;

    ArrayList<Integer> tableToTopic = new ArrayList<Integer>(); 
    ArrayList<Integer> wordCountByTable = new ArrayList<Integer>(); 
	WordInfo[] words;

	
	public DOCState(Document doc){  
		docID = idCounter++;
	    numberOfTables = 0;  
	    documentLength = doc.total;
	    words = new WordInfo[documentLength];	
		for (int k = 0; k < 2; k++){
			tableToTopic.add(null);
			wordCountByTable.add(0);
		}
	    int word, count, m = 0;
	    for (int n = 0; n < doc.numberOfUniquTerms; n++) {
	        word  = doc.words[n];
	        count = doc.counts[n];
	        for (int j = 0; j < count; j++) {
	            words[m] = new WordInfo(word, -1);
	            m++;
	        }
	    }
	}

	
	public void defragment(int[] kOldToKNew) {
	    int[] tOldToTNew = new int[numberOfTables];
	    int t, newNumberOfTables;
	    for (t = 0, newNumberOfTables = 0; t < numberOfTables; t++){
	        if (wordCountByTable.get(t) > 0){
	            tOldToTNew[t] = newNumberOfTables;
	            tableToTopic.set(newNumberOfTables, kOldToKNew[tableToTopic.get(t)]);
	            Collections.swap(tableToTopic, newNumberOfTables, t);
	            newNumberOfTables ++;
	        } else
	        	tableToTopic.set(t, -1);
	    }
	    numberOfTables = newNumberOfTables;
	    for (int i = 0; i < documentLength; i++)
	        words[i].tableAssignment = tOldToTNew[words[i].tableAssignment];
	}

	
	
}
