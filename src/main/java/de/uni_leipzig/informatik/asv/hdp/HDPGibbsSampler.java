package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Random;

public class HDPGibbsSampler extends GibbsState { 


	public double beta  = 0.5; // default only
	public double gamma = 1.0;
	public double alpha = 1.0;
	
	private Random random = new Random();

	/**
	 * Initially assign the words to tables and topics
	 * 
	 * @param corpus
	 */
	public void initGibbsState(Corpus corpus) {
		sizeOfVocabulary = corpus.sizeVocabulary;
		totalNumberOfWords = corpus.totalNumberOfWords;
		docStates = new DOCState[corpus.docs.size()];
		for (int d = 0; d < corpus.docs.size(); d++)
			docStates[d] = new DOCState(corpus.docs.get(d), d);
		int k, i, j;
		DOCState docState;
		numberOfTablesByTopic = new ArrayList<Integer>();
		wordCountByTopic = new ArrayList<Integer>();
		wordCountByTopicAndDocument = new ArrayList<int[]>();
		wordCountByTopicAndTerm = new ArrayList<int[]>();
		for (k = 0; k <= numberOfTopics; k++) {
			numberOfTablesByTopic.add(0);
			wordCountByTopic.add(0);
			wordCountByTopicAndDocument.add(new int[docStates.length]);
			wordCountByTopicAndTerm.add(new int[sizeOfVocabulary]);
		}	// var initialization done
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
	protected void iterate() {
		int table;
		ArrayList<Double> p = new ArrayList<Double>(), f = new ArrayList<Double>();
		for (int d = 0; d < docStates.length; d++) {
			for (int i = 0; i < docStates[d].documentLength; i++) {
				removeWord(d, i); // remove the word i from the state
				table = sampleTable(d, i, p, f);
				if (table == docStates[d].numberOfTables) // new Table
					addWord(d, i, table, sampleTopic(p, f)); // sampling its Topic
				else
					addWord(d, i, table, docStates[d].tableToTopic.get(table)); // existing Table
			}
		}
		defragment();
	}

	
	/**
	 * Decide to which topic the table should be assigned to
	 * 
	 * @param p
	 * @param f
	 * @return the index of the topic
	 */
	private int sampleTopic(ArrayList<Double> p, ArrayList<Double> f) {
		double u, pSum = 0.0;
		int k;
		while (p.size() <= numberOfTopics)
			p.add(0.0);
		for (k = 0; k < numberOfTopics; k++) {
			pSum += numberOfTablesByTopic.get(k) * f.get(k);
			p.set(k, pSum);
		}
		pSum += gamma / sizeOfVocabulary;
		p.set(numberOfTopics, pSum);
		u = random.nextDouble() * pSum;
		for (k = 0; k <= numberOfTopics; k++)
			if (u < p.get(k))
				break;
		return k;
	}
	

	/**	 
	 * Decide to which table the word should be assigned to
	 * 
	 * @param docID the index of the document of the current word
	 * @param i the index of the current word
	 * @param p
	 * @param f
	 * @return the index of the table
	 */
	int sampleTable(int docID, int i, ArrayList<Double> p, ArrayList<Double> f) {	
		DOCState docState = docStates[docID];
		int k, j;
		double pSum = 0.0, fk = 0.0, fNew, u;
		while (f.size() <= numberOfTopics)
			f.add(0.0);
		while (p.size() <= docState.numberOfTables)
			p.add(0.0);
		fNew = gamma / sizeOfVocabulary;
		for (k = 0; k < numberOfTopics; k++) {
			f.set(k, (wordCountByTopicAndTerm.get(k)[docState.words[i].termIndex] + beta) / 
					(wordCountByTopic.get(k) + sizeOfVocabulary * beta));
			fNew += numberOfTablesByTopic.get(k) * f.get(k);
		}
		fNew = fNew / (totalNumberOfTables + gamma);
		for (j = 0; j < docState.numberOfTables; j++) {
			if (docState.wordCountByTable.get(j) > 0) 
				fk = f.get(docState.tableToTopic.get(j));
			else
				fk = 0.0;
			pSum += docState.wordCountByTable.get(j) * fk;
			p.set(j, pSum);
		}
		pSum += alpha * fNew;
		p.set(docState.numberOfTables, pSum);
		u = random.nextDouble() * pSum;
		for (j = 0; j < docState.numberOfTables; j++)
			if (u < p.get(j)) 
				break;	// decided which table the word i is assigned to
		return j;
	}


	/**
	 * 
	 * @param directory
	 * @param doShuffle
	 * @param shuffleLag
	 * @param maxIter
	 * @param saveLag
	 * @throws FileNotFoundException
	 */
	public void run(String directory, boolean doShuffle, int shuffleLag, int maxIter, int saveLag) 
	throws FileNotFoundException {
		for (int iter = 0; iter < maxIter; iter++) {
			if (doShuffle && (iter > 0) && (iter % shuffleLag == 0))
				doShuffle();
			iterate();
			System.out.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = "
					+ totalNumberOfTables );
			if (saveLag != -1 && (iter % saveLag == 0)) 
				saveState(directory + "/" + iter);
		}
	}

			
}