/*
 * Copyright 2011 Arnim Bleier
 * Licensed under the GNU Lesser General Public License.
 * http://www.gnu.org/licenses/lgpl.html
 */

package de.uni_leipzig.informatik.asv.io;

/**
 * @author <a href="mailto:arnim.bleier+hdp@gmail.com">Arnim Bleier</a>
 */
public class SVNDocument implements Document {

	public int[] words = null;
	public int[] counts = null;
	public int numberOfUniquTerms = 0;
	public int total = 0;

	public SVNDocument(int len) {
		numberOfUniquTerms = len;
		words = new int[numberOfUniquTerms];
		counts = new int[numberOfUniquTerms];
	}

	@Override
	public int getLength() {
		return total;
	}

	@Override
	public int getNumberOfUniquTerms() {
		return numberOfUniquTerms;
	}

	@Override
	public int getTerm(int n) {
		return words[n];
	}

	@Override
	public int getCount(int n) {
		return counts[n];
	}

}
