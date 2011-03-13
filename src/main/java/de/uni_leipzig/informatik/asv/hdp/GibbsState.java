package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GibbsState {

	
	protected DOCState[] docStates;
	protected int[] numberOfTablesByTopic;
	protected int[] wordCountByTopic;
	protected ArrayList<int[]> wordCountByTopicAndDocument;
	protected ArrayList<int[]> wordCountByTopicAndTerm;
	
	
	protected int sizeOfVocabulary;
	protected int totalNumberOfWords;
	protected int numberOfTopics = 1;
	protected int totalNumberOfTables;
	
	/**
	 * Removes a word from the bookkeeping
	 * 
	 * @param docID the id of the document the word belongs to 
	 * @param i the index of the word
	 */
	protected void removeWord(int docID, int i){
		DOCState docState = docStates[docID];
		int table = docState.words[i].tableAssignment;
		int k = docState.tableToTopic[table];
		docState.wordCountByTable[table]--; //, docState.wordCountByTable.get(table) - 1);
		wordCountByTopic[k]--; //.set(k, wordCountByTopic.get(k) - 1);		
		wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] -= 1;
		wordCountByTopicAndDocument.get(k)[docState.docID] -= 1;
		if (docState.wordCountByTable[table] == 0) { // table is removed
			totalNumberOfTables--; 
			numberOfTablesByTopic[k]--; //.set(k, numberOfTablesByTopic.get(k) - 1);
			docState.tableToTopic[table] = -1; 
		}
	}
	
	/**
	 * Add a word to the bookkeeping
	 * 
	 * @param docID	docID the id of the document the word belongs to 
	 * @param i the index of the word
	 * @param table the table to which the word is assigned to
	 * @param k the topic to which the word is assigned to
	 */
	protected void addWord(int docID, int i, int table, int k) {
		DOCState docState = docStates[docID];
		docState.words[i].tableAssignment = table; 
		docState.wordCountByTable[table]++; //.set(, docState.wordCountByTable.get(table) + 1);
		wordCountByTopic[k]++; //.set(k, wordCountByTopic.get(k) + 1);
		wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] += 1;
		wordCountByTopicAndDocument.get(k)[docState.docID] += 1;
		if (docState.wordCountByTable[table] == 1) { // a new table is created
			docState.numberOfTables++;
			docState.tableToTopic[table] = k;
			totalNumberOfTables++;
			numberOfTablesByTopic[k]++; //.set(k, numberOfTablesByTopic.get(k) + 1);
			docState.tableToTopic = Utils.ensureCapacity(docState.tableToTopic, docState.numberOfTables);
//			docState.tableToTopic.add(-1);
			docState.wordCountByTable = Utils.ensureCapacity(docState.wordCountByTable, docState.numberOfTables);
//			docState.wordCountByTable.add(0);
			if (k == numberOfTopics) { // a new topic is created
				numberOfTopics++; 
				numberOfTablesByTopic = Utils.ensureCapacity(numberOfTablesByTopic, numberOfTopics); 
				wordCountByTopic = Utils.ensureCapacity(wordCountByTopic, numberOfTopics);
				wordCountByTopicAndDocument.add(new int[docStates.length]);
				wordCountByTopicAndTerm.add(new int[sizeOfVocabulary]);

			}
		}
	}

	/**
	 * Removes topics from the bookkeeping that have no words assigned to
	 */
	protected void defragment() {
		int[] kOldToKNew = new int[numberOfTopics];
		int k, newNumberOfTopics = 0;
		for (k = 0; k < numberOfTopics; k++) {
			if (wordCountByTopic[k] > 0) {
				kOldToKNew[k] = newNumberOfTopics;
				Utils.swap(wordCountByTopic, newNumberOfTopics, k);
				Utils.swap(numberOfTablesByTopic, newNumberOfTopics, k);
				Collections.swap(wordCountByTopicAndDocument, newNumberOfTopics, k);
				Collections.swap(wordCountByTopicAndTerm, newNumberOfTopics, k);
				newNumberOfTopics++;
			} 
		}
		numberOfTopics = newNumberOfTopics;
		for (int j = 0; j < docStates.length; j++) 
			docStates[j].defragment(kOldToKNew);
	}
	
	
	/**
	 * Permute the ordering of documents and words in the bookkeeping
	 */
	protected void doShuffle(){
		List<DOCState> h = Arrays.asList(docStates);
		Collections.shuffle(h);
		docStates = h.toArray(new DOCState[h.size()]);
		for (int j = 0; j < docStates.length; j ++){
			List<WordInfo> h2 = Arrays.asList(docStates[j].words);
			Collections.shuffle(h2);
			docStates[j].words = h2.toArray(new WordInfo[h2.size()]);
		}
	}
	
	
	/**
	 * Writes the current topic and table assignments on disc
	 * 
	 * @param name
	 * @throws FileNotFoundException
	 */
	protected void saveState(String name) throws FileNotFoundException  {
		PrintStream file = new PrintStream(name + "-topics.dat");
		for (int k = 0; k < numberOfTopics; k++) {
			for (int w = 0; w < sizeOfVocabulary; w++)
				file.format("%05d ",wordCountByTopicAndTerm.get(k)[w]);
			file.println();
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
						d_state.tableToTopic[t] + " " + t);
			}
		}
		file.close();
	}
	
	
}
