package de.uni_leipzig.informatik.asv.hdp;

public class Document {

	public int[] words = null;
	public int[] counts = null;
	public int numberOfUniquTerms = 0;
	public int total = 0;

	public Document(int len) {
		numberOfUniquTerms = len;
		words = new int[numberOfUniquTerms];
		counts = new int[numberOfUniquTerms];
	}

}
