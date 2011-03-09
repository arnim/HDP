package de.uni_leipzig.informatik.asv.hdp;


import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;

public class GibbsStateTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void testGibbsState() throws FileNotFoundException {
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
