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
	
	protected void removeWord(DOCState docState, int i){
		int table = docState.words[i].tableAssignment;
		int k = docState.tableToTopic.get(table);
		docState.wordCountByTable.set(table, docState.wordCountByTable.get(table) - 1);
		wordCountByTopic.set(k, wordCountByTopic.get(k) - 1);
		wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] -= 1;
		wordCountByTopicAndDocument.get(k)[docState.docID] -= 1;
		numberOfTablesByTopic.set(k, numberOfTablesByTopic.get(k) - 1);
		if (docState.wordCountByTable.get(table) == 0) { // table is removed
			totalNumberOfTables--; 
			docState.tableToTopic.set(table, - 1); // TODO thats the one to worry about
		}
	}
	
	protected void addWord(DOCState docState, int i, int table, int k) {
		docState.words[i].tableAssignment = table; 
		docState.wordCountByTable.set(table, docState.wordCountByTable.get(table) + 1);
		wordCountByTopic.set(k, wordCountByTopic.get(k) + 1);
		wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] += 1;
		wordCountByTopicAndDocument.get(k)[docState.docID] += 1;
		numberOfTablesByTopic.set(k, numberOfTablesByTopic.get(k) + 1);
		if (docState.wordCountByTable.get(table) == 1) { // a new table is created
			docState.numberOfTables++;
			docState.tableToTopic.set(table, k);
			totalNumberOfTables++;
			docState.tableToTopic.add(-1);
			docState.wordCountByTable.add(0);
			if (k == numberOfTopics) { // a new topic is created
				numberOfTopics++; 
				numberOfTablesByTopic.add(0);
				wordCountByTopic.add(0);
				wordCountByTopicAndDocument.add(new int[docStates.length]);
				wordCountByTopicAndTerm.add(new int[sizeOfVocabulary]);

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
	
	
	protected void saveState(String name) throws FileNotFoundException  {
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
