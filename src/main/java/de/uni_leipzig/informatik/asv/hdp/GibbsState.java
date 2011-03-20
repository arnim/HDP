/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.uni_leipzig.informatik.asv.io.TopicsWriter;
import de.uni_leipzig.informatik.asv.io.WordAssignmentsWriter;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class GibbsState {

	
	protected DOCState[] docStates;
	protected int[] numberOfTablesByTopic;
	protected int[] wordCountByTopic;
	protected int[][] wordCountByTopicAndTerm;
	
	
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
		docState.wordCountByTable[table]--; 
		wordCountByTopic[k]--; 		
		wordCountByTopicAndTerm[k][docState.words[i].termIndex] --;
		if (docState.wordCountByTable[table] == 0) { // table is removed
			totalNumberOfTables--; 
			numberOfTablesByTopic[k]--; 
			docState.tableToTopic[table] --; 
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
		docState.wordCountByTable[table]++; 
		wordCountByTopic[k]++; 
		wordCountByTopicAndTerm[k][docState.words[i].termIndex] ++;
		if (docState.wordCountByTable[table] == 1) { // a new table is created
			docState.numberOfTables++;
			docState.tableToTopic[table] = k;
			totalNumberOfTables++;
			numberOfTablesByTopic[k]++; 
			docState.tableToTopic = Utils.ensureCapacity(docState.tableToTopic, docState.numberOfTables);
			docState.wordCountByTable = Utils.ensureCapacity(docState.wordCountByTable, docState.numberOfTables);
			if (k == numberOfTopics) { // a new topic is created
				numberOfTopics++; 
				numberOfTablesByTopic = Utils.ensureCapacity(numberOfTablesByTopic, numberOfTopics); 
				wordCountByTopic = Utils.ensureCapacity(wordCountByTopic, numberOfTopics);
				wordCountByTopicAndTerm = Utils.add(wordCountByTopicAndTerm, new int[sizeOfVocabulary], numberOfTopics);
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
				Utils.swap(wordCountByTopicAndTerm, newNumberOfTopics, k);
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
			List<WordState> h2 = Arrays.asList(docStates[j].words);
			Collections.shuffle(h2);
			docStates[j].words = h2.toArray(new WordState[h2.size()]);
		}
	}
	
	
	/**
	 * Writes the current topic and table assignments
	 * 
	 * @param wordAssignmentsFileWriter 
	 * @param topicsWriter 
	 * @param iter 
	 * @throws IOException 
	 */
	protected void saveState(int iter, TopicsWriter topicsWriter, WordAssignmentsWriter wordAssignmentsWriter) throws IOException  {
		topicsWriter.writeWordCountByTopicAndTerm(wordCountByTopicAndTerm, numberOfTopics, sizeOfVocabulary, iter);
		wordAssignmentsWriter.openForIteration(iter);
		int t, docID;
		for (int d = 0; d < docStates.length; d++) {
			DOCState docState = docStates[d];
			docID = docState.docID;
			for (int i = 0; i < docState.documentLength; i++) {
				t = docState.words[i].tableAssignment;
				wordAssignmentsWriter.writeAssignment(docID, docState.words[i].termIndex, docState.tableToTopic[t], t);
			}
		}
		wordAssignmentsWriter.closeIteration();
	}
	
	
}
