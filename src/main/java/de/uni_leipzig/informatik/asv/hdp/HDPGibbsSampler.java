/*
 * Copyright 2011 Arnim Bleier, Andreas Niekler and Patrick Jaehnichen
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.hdp;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;

/**
 * Hierarchical Dirichlet Processes  
 * Chinese Restaurant Franchise Sampler
 * 
 * For more information on the algorithm see:
 * Hierarchical Bayesian Nonparametric Models with Applications. 
 * Y.W. Teh and M.I. Jordan. Bayesian Nonparametrics, 2010. Cambridge University Press.
 * http://www.gatsby.ucl.ac.uk/~ywteh/research/npbayes/TehJor2010a.pdf
 * 
 * For other known implementations see README.txt
 * 
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class HDPGibbsSampler { 


	public double beta  = 0.5; // default only
	public double gamma = 1.5;
	public double alpha = 1.0;
	
	private Random random = new Random();
	private double[] p;
	private double[] f;
	
	protected DOCState[] docStates;
	protected int[] numberOfTablesByTopic;
	protected int[] wordCountByTopic;
	protected int[][] wordCountByTopicAndTerm;
	
	
	protected int sizeOfVocabulary;
	protected int totalNumberOfWords;
	protected int numberOfTopics = 1;
	protected int totalNumberOfTables;
	private InstanceList data;
	

	/**
	 * Initially assign the words to tables and topics
	 * 
	 * @param corpus {@link Corpus} on which to fit the model
	 */
	public void addInstances(InstanceList corpus) {
		this.data = corpus;
		sizeOfVocabulary = corpus.getDataAlphabet().size();
		totalNumberOfWords = 0;
		docStates = new DOCState[corpus.size()];
		for (int d = 0; d < corpus.size(); d++) {
			docStates[d] = new DOCState(corpus.get(d), d);
			FeatureSequence tokens = (FeatureSequence) corpus.get(d).getData();
			for (int position = 0; position < tokens.getLength(); position++) 
				totalNumberOfWords++;	
		}
		int k, i, j;
		DOCState docState;
		p = new double[20]; 
		f = new double[20];
		numberOfTablesByTopic = new int[numberOfTopics+1];
		wordCountByTopic = new  int[numberOfTopics+1];
		wordCountByTopicAndTerm = new int[numberOfTopics+1][];
		for (k = 0; k <= numberOfTopics; k++) 	// var initialization done
			wordCountByTopicAndTerm[k] = new int[sizeOfVocabulary];
		for (k = 0; k < numberOfTopics; k++) { 
			docState = docStates[k];
			for (i = 0; i < docState.documentLength; i++) 
				addWord(docState.docID, i, 0, k);
		} // all topics have now one document
		for (j = numberOfTopics; j < docStates.length; j++) {
			docState = docStates[j]; 
			k = random.nextInt(numberOfTopics);
			for (i = 0; i < docState.documentLength; i++) 
				addWord(docState.docID, i, 0, k);
		} // the words in the remaining documents are now assigned too
	}

	
	/**
	 * Step one step ahead
	 * 
	 */
	protected void nextGibbsSweep() {
		int table;
		for (int d = 0; d < docStates.length; d++) {
			for (int i = 0; i < docStates[d].documentLength; i++) {
				removeWord(d, i); // remove the word i from the state
				table = sampleTable(d, i);
				if (table == docStates[d].numberOfTables) // new Table
					addWord(d, i, table, sampleTopic()); // sampling its Topic
				else
					addWord(d, i, table, docStates[d].tableToTopic[table]); // existing Table
			}
		}
		defragment();
	}

	
	/**
	 * Decide at which topic the table should be assigned to
	 * 
	 * @return the index of the topic
	 */
	private int sampleTopic() {
		double u, pSum = 0.0;
		int k;
		p = Utils.ensureCapacity(p, numberOfTopics);
		for (k = 0; k < numberOfTopics; k++) {
			pSum += numberOfTablesByTopic[k] * f[k];
			p[k] = pSum;
		}
		pSum += gamma / sizeOfVocabulary;
		p[numberOfTopics] = pSum;
		u = random.nextDouble() * pSum;
		for (k = 0; k <= numberOfTopics; k++)
			if (u < p[k])
				break;
		return k;
	}
	

	/**	 
	 * Decide at which table the word should be assigned to
	 * 
	 * @param docID the index of the document of the current word
	 * @param i the index of the current word
	 * @return the index of the table
	 */
	int sampleTable(int docID, int i) {	
		int k, j;
		double pSum = 0.0, vb = sizeOfVocabulary * beta, fNew, u;
		DOCState docState = docStates[docID];
		f = Utils.ensureCapacity(f, numberOfTopics);
		p = Utils.ensureCapacity(p, docState.numberOfTables);
		fNew = gamma / sizeOfVocabulary;
		for (k = 0; k < numberOfTopics; k++) {
			f[k] = (wordCountByTopicAndTerm[k][docState.words[i].termIndex] + beta) / 
					(wordCountByTopic[k] + vb);
			fNew += numberOfTablesByTopic[k] * f[k];
		}
		for (j = 0; j < docState.numberOfTables; j++) {
			if (docState.wordCountByTable[j] > 0) 
				pSum += docState.wordCountByTable[j] * f[docState.tableToTopic[j]];
			p[j] = pSum;
		}
		pSum += alpha * fNew / (totalNumberOfTables + gamma); // Probability for t = tNew
		p[docState.numberOfTables] = pSum;
		u = random.nextDouble() * pSum;
		for (j = 0; j <= docState.numberOfTables; j++)
			if (u < p[j]) 
				break;	// decided which table the word i is assigned to
		return j;
	}


	/**
	 * Method to call for fitting the model.
	 * 
	 * @param doShuffle
	 * @param shuffleLag
	 * @param maxIter number of iterations to run
	 * @param saveLag save interval 
	 * @param wordAssignmentsWriter {@link WordAssignmentsWriter}
	 * @param topicsWriter {@link TopicsWriter}
	 * @throws IOException 
	 */
	public void run(int shuffleLag, int maxIter, PrintStream log) 
	throws IOException {
		for (int iter = 0; iter < maxIter; iter++) {
			if ((shuffleLag > 0) && (iter > 0) && (iter % shuffleLag == 0))
				doShuffle();
			nextGibbsSweep();
			log.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = "
					+ totalNumberOfTables );
		}
	}
		
	
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
	 *  Outputs the topic composition of the documents
	 *  Inspired by printDocumentTopics in mallet ParallelTopicModel
	 * 
	 *  @param out         A {@link PrintStream}
	 *  @param threshold   Only print topics with proportion greater than this parameter
	 *  @param maxNumberOfTopics         Maximal number of topics to print
	 */
	public void printDocumentTopics(PrintStream out, double threshold, int maxNumberOfTopics) {
		out.println("#doc name topic proportion ...");
		IDSorter[] sortedTopics = new IDSorter[numberOfTopics];
		for (int k = 0; k < numberOfTopics; k++) 
			sortedTopics[k] = new IDSorter(k, k);
		if (maxNumberOfTopics <= 0 || maxNumberOfTopics > numberOfTopics) 
			maxNumberOfTopics = numberOfTopics;
		for (int d = 0; d < docStates.length; d++) {
			DOCState doc = docStates[d];
			int[] topicCounts = new int[numberOfTopics];
			out.print(d + "    ");
			String source = "NA";
			if (data.get(d).getSource() != null) 
				source = data.get(d).getSource().toString(); 
			out.print(source + "    ");
			for (int i = 0; i < doc.documentLength; i++) 
				topicCounts[doc.tableToTopic[doc.words[i].tableAssignment]]++;
			for (int k = 0; k < numberOfTopics; k++) 
				sortedTopics[k].set(k, topicCounts[k] / (doc.documentLength*1.0));
			Arrays.sort(sortedTopics);
			for (int k = 0; k < maxNumberOfTopics; k++) {
				if (sortedTopics[k].getWeight() < threshold) { break; }
				out.print(sortedTopics[k].getID() + "    " + 
							   sortedTopics[k].getWeight() + "    ");
			}
			out.println();
		}
	}
	
	/**
	 * Outputs the top words for each topic
	 * 
	 * @param out
	 * @param maxNumberOfWords
	 */
	public void printTopicWords(PrintStream out, int maxNumberOfWords){
		if (maxNumberOfWords <= 0 || maxNumberOfWords > sizeOfVocabulary) 
			maxNumberOfWords = sizeOfVocabulary;
		IDSorter[] sortedWords = new IDSorter[sizeOfVocabulary];
		for (int v = 0; v < sizeOfVocabulary; v++) 
			sortedWords[v] = new IDSorter(v, v);
		for (int k = 0; k < numberOfTopics; k++) {
			out.print(k + "    ");
			for (int v = 0; v < sizeOfVocabulary; v++) 
				sortedWords[v].set(v, wordCountByTopicAndTerm[k][v]);
			Arrays.sort(sortedWords);
			for (int v = 0; v < maxNumberOfWords; v++) {
				out.print(data.getAlphabet().lookupObject(sortedWords[v].getID()) +"    ");
			}
			out.println();
		}
	}
	
	
	/**
	 * Outputs to a {@link PrintStream} the words in the corpus with their topic assignments
	 * Inspired by printState in mallet ParallelTopicModel
	 * 
	 * @param out A {@link PrintStream}
	 */
	public void printState(PrintStream out) {
		out.println ("#doc source pos typeindex type topic");
		out.println("#alpha : " + alpha);
		out.println("#beta : " + beta);
		out.println("#gamma : " + gamma);
		for (int d = 0; d < docStates.length; d++) {
			String source = "NA";
			if (data.get(d).getSource() != null) 
				source = data.get(d).getSource().toString();
			DOCState doc = docStates[d];
			for (int i = 0; i < doc.documentLength; i++) {
				int term = doc.words[i].termIndex; 
				out.print(d + " ");
				out.print(source + " "); 
				out.print(i + " ");
				out.print(term + " ");
				out.print(data.getAlphabet().lookupObject(term) + " ");
				out.println(doc.tableToTopic[doc.words[i].tableAssignment]);
			}
		}
	}
	
	
	
	class DOCState {
		
		int docID, documentLength, numberOfTables;
		int[] tableToTopic; 
	    int[] wordCountByTable;
		WordState[] words;

		public DOCState(Instance instance, int docID) {
			this.docID = docID;
		    numberOfTables = 0;  
		    FeatureSequence tokens = (FeatureSequence) instance.getData();
		    documentLength = tokens.getLength();
		    words = new WordState[documentLength];	
		    wordCountByTable = new int[2];
		    tableToTopic = new int[2];
			for (int position = 0; position < documentLength; position++) 
				words[position] = new WordState(tokens.getIndexAtPosition(position), -1);
		}


		public void defragment(int[] kOldToKNew) {
		    int[] tOldToTNew = new int[numberOfTables];
		    int t, newNumberOfTables = 0;
		    for (t = 0; t < numberOfTables; t++){
		        if (wordCountByTable[t] > 0){
		            tOldToTNew[t] = newNumberOfTables;
		            tableToTopic[newNumberOfTables] = kOldToKNew[tableToTopic[t]];
		            Utils.swap(wordCountByTable, newNumberOfTables, t);
		            newNumberOfTables ++;
		        } else 
		        	tableToTopic[t] = -1;
		    }
		    numberOfTables = newNumberOfTables;
		    for (int i = 0; i < documentLength; i++)
		        words[i].tableAssignment = tOldToTNew[words[i].tableAssignment];
		}

	}
	
	
	class WordState {   
	
		int termIndex;
		int tableAssignment;
		
		public WordState(int wordIndex, int tableAssignment){
			this.termIndex = wordIndex;
			this.tableAssignment = tableAssignment;
		}

	}
	
	
	public static void main(String[] args) throws IOException {
		 int iter = 0;
		 String inputFile = null, outputDir = null;
		 HDPGibbsSampler state = new HDPGibbsSampler();
		try {
			state.beta = Double.parseDouble(args[0]);
			state.alpha = Double.parseDouble(args[1]);
			state.gamma = Double.parseDouble(args[2]);
			iter = Integer.parseInt(args[3]);
			inputFile = args[4];
			state.numberOfTopics = Integer.parseInt(args[5]);
			outputDir = args[6];
		} catch (Exception e) {
			System.out.println("CRF Gibbs sampling for the Hierarchical Dirichlet Processes");
			System.out.println("The application nees the folowing params in exact order");
			System.out.println("beta alpha gamma iterations inputFile initialNumberOfTOpics outputDir");
			System.out.println("Example:");
			System.out.println("HDP 0.5 1.5 1.0 2000 ./topic-input.mallet 5 ./output/ ");
			System.exit(0);
		}
		
		
		state.addInstances(InstanceList.load(new File(inputFile)));
		
		System.out.println("sizeOfVocabulary="+state.sizeOfVocabulary);
		System.out.println("totalNumberOfWords="+state.totalNumberOfWords);
		System.out.println("NumberOfDocs="+state.docStates.length);

		state.run(0, iter, System.out);
		state.printState(new PrintStream(new File(outputDir + "state.txt")));
		state.printDocumentTopics(new PrintStream(new File(outputDir + "topics.txt")), 0.0001, 0);
		state.printTopicWords(new PrintStream(new File(outputDir + "words.txt")), 10);
	}
		
}