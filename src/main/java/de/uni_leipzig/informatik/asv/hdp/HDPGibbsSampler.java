package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HDPGibbsSampler extends GibbsState { 


	public double beta  = 0.5; // default only
	public double gamma = 1.0;
	public double alpha = 1.0;
	
	private Random random = new Random();

	public void initGibbsState(Corpus corpus) {
		sizeOfVocabulary = corpus.sizeVocabulary;
		totalNumberOfWords = corpus.totalNumberOfWords;
		docStates = new DOCState[corpus.docs.size()];
		for (int d = 0; d < corpus.docs.size(); d++)
			docStates[d] = new DOCState(corpus.docs.get(d), d);
		int k, i, j;
		double prob, u;
		double[] q = new double[numberOfTopics];
		int[] v = new int[sizeOfVocabulary];
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

	protected void iterate(boolean shuffle) {
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


	public void run(String directory, boolean doShuffle, int shuffleLag, int maxIter, int saveLag) throws FileNotFoundException {
		boolean shuffle = false;
		for (int iter = 0; iter < maxIter; iter++) {
			if (doShuffle && (iter > 0) && (iter % shuffleLag == 0))
				shuffle = true;
			else
				shuffle = false;
			iterate(shuffle);
			System.out.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = "
					+ totalNumberOfTables );
			if (saveLag != -1 && (iter % saveLag == 0)) 
				saveState(directory + "/" + iter);
		}
	}

			
}