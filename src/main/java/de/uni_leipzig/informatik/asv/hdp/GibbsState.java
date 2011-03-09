package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

public class GibbsState {

	
	protected DOCState[] docStates;
	protected ArrayList<Integer> numberOfTablesByTopic;
	protected ArrayList<Integer> wordCountByTopic;
	protected ArrayList<int[]> wordCountByTopicAndDocument;
	protected ArrayList<int[]> wordCountByTopicAndTerm;
	
	
	protected int sizeOfVocabulary;
	protected int totalNumberOfWords;
	protected int numberOfTopics = 1;
	protected int totalNumberOfTables;
	
	
	
	protected void updateDocState(DOCState docState, int i, int update, int k) {
//		System.out.println("-");
		int table = docState.words[i].tableAssignment;
		if (k < 0)
				k = docState.tableToTopic.get(table); 
//		if (update < 0)
//			System.out.println(docState.wordCountByTable.get(table));
		docState.wordCountByTable.set(table, docState.wordCountByTable.get(table) + update);
//		if (update < 0)
//			System.out.println(docState.wordCountByTable.get(table));
		try {
			int[] foo = wordCountByTopicAndTerm.get(k);
			foo[docState.words[i].termIndex] += update;

		} catch (Exception e) {
//			System.err.println(k);
		}
		wordCountByTopicAndDocument.get(k)[docState.docID] += update;
		if (update == -1 && docState.wordCountByTable.get(table) == 0) { 
//			System.out.println("totalNumberOfTables="+totalNumberOfTables);
			totalNumberOfTables--; 
			numberOfTablesByTopic.set(k, numberOfTablesByTopic.get(k) - 1);
			docState.tableToTopic.set(table, - 1); // TODO thats toe one to worry about
		}
		if (update == 1 && docState.wordCountByTable.get(table) == 1) { 
			if (table == docState.numberOfTables)
				docState.numberOfTables++;
			docState.tableToTopic.set(table, k);
			numberOfTablesByTopic.set(k, numberOfTablesByTopic.get(k) + 1); 
			totalNumberOfTables++;
			if (docState.tableToTopic.size() < docState.numberOfTables + 1) {
				docState.tableToTopic.add(-1);
				docState.wordCountByTable.add(0);
			}
			if (k == numberOfTopics) {
//				if (wordCountByTopic.get(k)!=1)
//					System.err.println(wordCountByTopic.get(k)+ "--------!wordCountByTopic.get(k)==1 ");
				numberOfTopics++; 
				if (numberOfTablesByTopic.size() < numberOfTopics + 1) {
					numberOfTablesByTopic.add(0);
					wordCountByTopic.add(0);
					wordCountByTopicAndDocument.add(new int[docStates.length]);
					wordCountByTopicAndTerm.add(new int[sizeOfVocabulary]);
				}
			}
		}
	}

	protected void defragment() {
		int[] kOldToKNew = new int[numberOfTopics];
		int k, newNumberOfTopics = 0;
		for (k = 0; k < numberOfTopics; k++) {
			if (wordCountByTopic.get(k) > 0) {
				kOldToKNew[k] = newNumberOfTopics;
				Collections.swap(wordCountByTopic, newNumberOfTopics, k);
				Collections.swap(numberOfTablesByTopic, newNumberOfTopics, k);
				Collections.swap(wordCountByTopicAndDocument, newNumberOfTopics, k);
				Collections.swap(wordCountByTopicAndTerm, newNumberOfTopics, k);
				newNumberOfTopics++;
			}
		}
		numberOfTopics = newNumberOfTopics;
		for (int j = 0; j < docStates.length; j++) 
			docStates[j].defragment(kOldToKNew);
	}
	
	
	protected void saveIteration(String name) throws FileNotFoundException  {
		PrintStream file = new PrintStream(name + "-topics.dat");
		for (int k = 0; k < numberOfTopics; k++) {
			for (int w = 0; w < sizeOfVocabulary; w++)
				file.println(wordCountByTopicAndTerm.get(k)[w]);
		}
		file.close();
		file = new PrintStream(name + "-word-assignments.dat");
		file.println("d w z t");
		int t, docID;
		for (int d = 0; d < docStates.length; d++) {
			DOCState d_state = docStates[d];
			docID = d_state.docID;
			for (int i = 0; i < d_state.documentLength; i++) {
				t = d_state.words[i].tableAssignment;
				file.println(docID + " " + 
						d_state.words[i].termIndex + " " + 
						d_state.tableToTopic.get(t) + " " + t);
			}
		}
		file.close();
	}
	
	
}
