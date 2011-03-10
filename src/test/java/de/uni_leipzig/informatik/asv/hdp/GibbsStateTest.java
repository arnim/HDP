package de.uni_leipzig.informatik.asv.hdp;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GibbsStateTest {

	private static Corpus _corpus;
	private HDPGibbsSampler _state;
	private String _outputDir = "/Users/arnim/Desktop/hdp/test/";;


	@BeforeClass
	public static void setUpBefore() throws Exception {
		_corpus = new Corpus();
		_corpus.read(_corpus.getClass().getResourceAsStream("test.corpus"));
	}

	@Before
	public void setUp() throws Exception {
		_state = new HDPGibbsSampler();

	}


	@Test
	public void testCorpus() throws FileNotFoundException {
		assertEquals(20, _corpus.docs.size());
		assertEquals(100, _corpus.sizeVocabulary);
		assertEquals(400, _corpus.totalNumberOfWords);
	}
	
	
	@Test
	public void testDefaults() throws FileNotFoundException {
		assertEquals(.5, _state.eta, Double.MIN_VALUE);
		assertEquals(1, _state.numberOfTopics); // initial number of topics
		assertEquals(1, _state.numberOfTopics); // initial number of topics
		assertEquals(1, _state.gamma, Double.MIN_VALUE); // initial number of topics
		assertEquals(1, _state.alpha, Double.MIN_VALUE); // initial number of topics
	}
	
	@Test
	public void testInitGibbsState() throws FileNotFoundException {
		assertEquals(0, _state.sizeOfVocabulary);
		assertNull(_state.docStates); 
		_state.initGibbsState(_corpus);
		assertEquals(1, _state.numberOfTopics); 
		assertEquals(100, _state.sizeOfVocabulary); 
		assertEquals(10, _state.docStates[10].docID); 
		assertEquals(20, _state.docStates.length); 

		assertTrue(_state.wordCountByTopic.get(0).equals(400)); 
		assertTrue(_state.wordCountByTopic.get(1).equals(0)); 
		assertEquals(2, _state.wordCountByTopic.size());  
		assertEquals(_state.wordCountByTopic.size(), _state.wordCountByTopicAndDocument.size()); 
		assertEquals(_state.wordCountByTopic.size(), _state.numberOfTablesByTopic.size()); 
		assertEquals(_state.wordCountByTopic.size(), _state.wordCountByTopic.size()); 
		assertEquals(_state.wordCountByTopic.size(), _state.wordCountByTopicAndTerm.size()); 
	}
	
	

	
	
	@Test
	public void testDefragment() throws FileNotFoundException {
		_state.initGibbsState(_corpus);
		ArrayList<Integer> numberOfTablesByTopic = new ArrayList<Integer>(_state.numberOfTablesByTopic);
		ArrayList<Integer> wordCountByTopic = new ArrayList<Integer>(_state.wordCountByTopic);
		ArrayList<int[]> wordCountByTopicAndDocument = new ArrayList<int[]>(_state.wordCountByTopicAndDocument);
		ArrayList<int[]> wordCountByTopicAndTerm = new ArrayList<int[]>(_state.wordCountByTopicAndTerm);
		int numberOfTopicsCopy = _state.numberOfTopics;
		int totalNumberOfTablesCopy = _state.totalNumberOfTables;
		


		_state.defragment(); // in the initial setup there should be no fragmentation
		_state.defragment(); // if defragment has been applied, its second application shouldn't change anything
		
		
		assertTrue(_state.numberOfTablesByTopic.equals(numberOfTablesByTopic));
		assertTrue(_state.wordCountByTopic.equals(wordCountByTopic));
		assertTrue(_state.wordCountByTopicAndDocument.equals(wordCountByTopicAndDocument));
		assertTrue(_state.wordCountByTopicAndTerm.equals(wordCountByTopicAndTerm));
		assertTrue(_state.numberOfTopics == numberOfTopicsCopy);
		assertTrue(_state.totalNumberOfTables == totalNumberOfTablesCopy);  // Only shallow testing!
		

		
	
		ArrayList<Double> q = new ArrayList<Double>(), f = new ArrayList<Double>();
		System.out.println(_state.sampleTable(_state.docStates[0],1, q, f));
	}

	
	
	@Test
	public void testHDPGibbsSampler() throws FileNotFoundException {
	
		_state.eta = 0.5;
		_state.numberOfTopics = 4;
		_state.gamma = 1.0; 
		_state.alpha = 1.0;
		_state.initGibbsState(_corpus);
		_state.run(_outputDir, true, 10, 1000, 10);
	}

}
