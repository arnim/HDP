package de.uni_leipzig.informatik.asv.hdp;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class HDPGibbsSampler extends GibbsState { 


	public double eta  = 0.5; // default only
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
		int table;
		ArrayList<Double> q = new ArrayList<Double>(), f = new ArrayList<Double>();
		for (int d = 0; d < docStates.length; d++) {
			docState = docStates[d];
			for (int i = 0; i < docState.documentLength; i++) {
				updateDocState(docState, i, -1, docState.words[i].tableAssignment, -1); // remove the word i from the state
				table = sampleTable(docState, i, q, f);
				if (table == docState.numberOfTables) // new Table
					updateDocState(docState, i, 1, table, sampleTopic(docState, i, q, f));
				updateDocState(docState, i, 1, table, -1);
			}
		}
		defragment();
	}

	
	
	private int sampleTopic(DOCState docState, int i, ArrayList<Double> q, ArrayList<Double> f) {
		double u, total_q = 0.0;
		int k;
		while (q.size() <= numberOfTopics)
			q.add(0.0);
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
		return k;
	}
	

	private int sampleTable(DOCState docState, int i, ArrayList<Double> q, ArrayList<Double> f) {	
		int k, j;
		double total_q = 0.0, f_k = 0.0, f_new, u;
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
		return j;
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
			iterateGibbsState(false);
			System.out.println("iter = " + iter + " #topics = " + numberOfTopics + ", #tables = "
					+ totalNumberOfTables + ", gamma = "
					+ gamma + ", alpha = " + alpha);

			if (saveLag != -1 && (iter % saveLag == 0)) 
				saveState(directory + "/" + iter);
		}
		file.close(); 
	}

	
			
}