package de.uni_leipzig.informatik.asv.hdp;

import java.util.List;

class Utils {


	public static double similarity(int[] a1, int[] other) {
		double sim = 0.0, norm1 = 0.0, norm2 = 0.0;
		for (int i = 0; i < a1.length; i++) {
			sim += a1[i] * other[i];
			norm1 += a1[i] * a1[i];
			norm2 += other[i] * other[i];
		}
		return sim / Math.sqrt(norm1 * norm2);
	}

	public static void changeAtAbout(List<Double> numTablesByZ, int position,
			double about) {
		double x = numTablesByZ.get(position);
		numTablesByZ.set(position, x + about);
	}

	public static void changeAtAbout(List<Integer> numTablesByZ, int position,
			Integer about) {
		if (numTablesByZ.size() <= position)
			numTablesByZ.add(about);
		else {
			Integer x = numTablesByZ.get(position);
			numTablesByZ.set(position, x + about);
		}
	}

}