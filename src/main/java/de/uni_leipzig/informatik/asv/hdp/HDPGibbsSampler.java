package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.management.RuntimeErrorException;

public class HDPGibbsSampler {

	public int sizeOfVocabulary;
	public int totalNumberOfWords;
	public int numberOfTopics;
	public int totalNumberOfTables;
	public double eta;
	public double gamma;
	public double alpha;

	private DOCState[] docStates;
	private ArrayList<Integer> numberOfTablesByTopic;
	private ArrayList<Integer> wordCountByTopic;
	private ArrayList<int[]> wordCountByTopicAndDocument;
	private ArrayList<int[]> wordCountByTopicAndTerm;
	
	private Random random = new Random();

	public void initGibbsState(Corpus corpus) {
		sizeOfVocabulary = corpus.sizeVocabulary;
		totalNumberOfWords = corpus.totalNumberOfWords;
		docStates = new DOCState[corpus.docs.size()];
		for (int d = 0; d < corpus.docs.size(); d++)
			docStates[d] = new DOCState(corpus.docs.get(d));
		int k, i, j;
		double prob, u;
		double[] q = new double[numberOfTopics];
		int[] v = new int[sizeOfVocabulary];
		DOCState docState;
		numberOfTablesByTopic = new ArrayList<Integer>();
		wordCountByTopic = new ArrayList<Integer>();
		wordCountByTopicAndDocument = new ArrayList<int[]>();
		wordCountByTopicAndTerm = new ArrayList<int[]>();
		for (k = 0; k <= numberOfTopics + 1; k++) {
			numberOfTablesByTopic.add(k);
			wordCountByTopic.add(k);
			wordCountByTopicAndDocument.add(new int[docStates.length]);
			wordCountByTopicAndTerm.add(new int[sizeOfVocabulary]);
		}	// var initialization done
		for (k = 0; k < numberOfTopics; k++) { 
			docState = docStates[k];
			totalNumberOfTables++; 
			Utils.changeAtAbout(numberOfTablesByTopic, k, 1);
			Utils.changeAtAbout(wordCountByTopic, k, docState.documentLength);
			wordCountByTopicAndDocument.get(k)[docState.docID] += docState.documentLength;
			docState.numberOfTables = 1;  
			docState.tableToTopic.set(0, k); 
			docState.wordCountByTable.set(0, docState.documentLength); 
			for (i = 0; i < docState.documentLength; i++) {
				wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex]++;
				docState.words[i].tableAssignment = 0;
			} 
		} // all topics have now one document
		for (j = numberOfTopics; j < docStates.length; j++) {
			docState = docStates[j]; 
			prob = 0;
			for (i = 0; i < docState.documentLength; i++) 
				v[docState.words[i].termIndex]++;
			for (i = 0; i < numberOfTopics; i++) {
				prob += Utils.similarity(v, wordCountByTopicAndTerm.get(i));
				q[i] = prob;
			}
			u = random.nextDouble() * q[numberOfTopics-1];
			for (k = 0; k <= numberOfTopics; k++)
				if (u < q[k])
					break; // selecting to which topic the document should belong to
			Utils.changeAtAbout(numberOfTablesByTopic, k, 1);
			Utils.changeAtAbout(wordCountByTopic, k, docState.documentLength);
			wordCountByTopicAndDocument.get(k)[docState.docID] = docState.documentLength;
			docState.numberOfTables = 1;
			docState.tableToTopic.set(0, k);
			docState.wordCountByTable.set(0, docState.documentLength);
			for (i = 0; i < docState.documentLength; i++) {
				wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex]++;
				docState.words[i].tableAssignment = 0;
			} // updating the bookkeeping 
		}
	}

	private void iterateGibbsState(boolean shuffle) {
		if (shuffle) {
			List<DOCState> h = Arrays.asList(docStates);
			Collections.shuffle(h);
			docStates = h.toArray(new DOCState[h.size()]);
			for (int j = 0; j < docStates.length; j ++){
				List<WordInfo> h2 = Arrays.asList(docStates[j].words);
				Collections.shuffle(h2);
				docStates[j].words = h2.toArray(new WordInfo[h2.size()]);
			}
		}
		DOCState docState;
		ArrayList<Double> q = new ArrayList<Double>(), f = new ArrayList<Double>();
		for (int j = 0; j < docStates.length; j++) {
			docState = docStates[j];
			for (int i = 0; i < docState.documentLength; i++) 
				sampleWordAssignment(docState, i, q, f);
		}
		defragment();
	}

	private void defragment() {
		int[] kOldToKNew = new int[numberOfTopics];
		int k, newNumberOfTopics = 0;
		for (k = 0; k < numberOfTopics; k++) {
			if (wordCountByTopic.get(k) > 0) {
				kOldToKNew[k] = newNumberOfTopics;
				Collections.swap(wordCountByTopic, newNumberOfTopics, k);
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

	private void sampleWordAssignment(DOCState docState, int i, ArrayList<Double> q, ArrayList<Double> f) {	
		int k, j;
		double total_q = 0.0, f_k = 0.0, f_new, u;
		updateDocState(docState, i, -1, -1);
		while (f.size() <= numberOfTopics)
			f.add(0.0);
		while (q.size() <= docState.numberOfTables)
			q.add(0.0);
		f_new = gamma / sizeOfVocabulary;
		for (k = 0; k < numberOfTopics; k++) {
			f.set(k, (wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] + eta) / (wordCountByTopic.get(k) + sizeOfVocabulary * eta));
			f_new += numberOfTablesByTopic.get(k) * f.get(k);
		}
		f_new = f_new / (totalNumberOfTables + gamma);
		for (j = 0; j < docState.numberOfTables; j++) {
			if (docState.wordCountByTable.get(j) > 0) 
				f_k = f.get(docState.tableToTopic.get(j));
			else
				f_k = 0.0;
			total_q += docState.wordCountByTable.get(j) * f_k;
			q.set(j, total_q);
		}
		total_q += alpha * f_new;
		q.set(docState.numberOfTables, total_q);
		u = random.nextDouble() * total_q;
		for (j = 0; j < docState.numberOfTables; j++)
			if (u < q.get(j))
				break;	// decided which table the word i is assigned to
		docState.words[i].tableAssignment = j;
		if (j == docState.numberOfTables) {  // new table
			while (q.size() <= numberOfTopics)
				q.add(0.0);
			total_q = 0.0;
			for (k = 0; k < numberOfTopics; k++) {
				total_q += numberOfTablesByTopic.get(k) * f.get(k);
				q.set(k, total_q);
			}
			total_q += gamma / sizeOfVocabulary;
			q.set(numberOfTopics, total_q);
			u = random.nextDouble() * total_q;
			for (k = 0; k <= numberOfTopics; k++)
				if (u < q.get(k))
					break;
			updateDocState(docState, i, +1, k);
		} else 
			updateDocState(docState, i, +1, -1);
	}

	private void updateDocState(DOCState docState, int i, int update, int k) {
		int table = docState.words[i].tableAssignment;
		if (k < 0)
				k = docState.tableToTopic.get(table);
		docState.wordCountByTable.set(table, docState.wordCountByTable.get(table) + update);
		try {
			wordCountByTopic.set(k, wordCountByTopic.get(k) + update);

		} catch (Exception e) {
			System.err.println("k="+k+ " wordCountByTopic="+wordCountByTopic.size());
			throw new RuntimeException(e);
		}
		wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] += update;
		wordCountByTopicAndDocument.get(k)[docState.docID] += update;
		if (update == -1 && docState.wordCountByTable.get(table) == 0) { 
			totalNumberOfTables--; 
			numberOfTablesByTopic.set(k, numberOfTablesByTopic.get(k) - 1);
			docState.tableToTopic.set(table, docState.tableToTopic.get(table) - 1);
		}
		if (update == 1 && docState.wordCountByTable.get(table) == 1) { 
			docState.numberOfTables++;
			docState.tableToTopic.set(table, k);
			numberOfTablesByTopic.set(k, numberOfTablesByTopic.get(k) + 1); 
			totalNumberOfTables++;
			if (docState.tableToTopic.size() < docState.numberOfTables + 1) {
				docState.tableToTopic.add(-1);
				docState.wordCountByTable.add(0);
			}
			if (k == numberOfTopics) {
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

	private void saveIteration(String name) throws FileNotFoundException  {
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

	public void run(String directory, boolean doShuffle, int shuffleLag, int maxIter, int saveLag) throws FileNotFoundException {
		System.out.println("starting with " + numberOfTopics + " topics");
		PrintStream file = new PrintStream(directory + "state.log");
		file.println("time iter num.topics num.tables likelihood gamma alpha");
		boolean shuffle = false;
		for (int iter = 0; iter < maxIter; iter++) {
			file.print("iter = " + iter + ", ");
			if (doShuffle && (iter > 0) && (iter % shuffleLag == 0))
				shuffle = true;
			else
				shuffle = false;
			iterateGibbsState(shuffle);
			System.out.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = "
					+ totalNumberOfTables + ", gamma = "
					+ gamma + ", alpha = " + alpha);

			if (saveLag != -1 && (iter % saveLag == 0)) 
				saveIteration(directory + "/" + iter);
		}
		file.close(); 
	}

	
	public static void main(String[] args) throws FileNotFoundException {

		String outputDir = "/Users/arnim/Desktop/hdp/test/";

		Corpus corpus = new Corpus();
		corpus.read("/Users/arnim/Desktop/hdp/test/test.corpus");

	    System.out.println("number of docs  : " + corpus.docs.size());
	    System.out.println("number of terms : " + corpus.sizeVocabulary);
	    System.out.println("number of total words : " + corpus.totalNumberOfWords);

		HDPGibbsSampler state = new HDPGibbsSampler();
		state.eta = 0.5;
		state.numberOfTopics = 4;
		state.gamma = 1.0; 
		state.alpha = 1.0;
		state.initGibbsState(corpus);
		state.run(outputDir, true, 10, 1000, 10);

	}
			
}