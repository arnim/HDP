package de.uni_leipzig.informatik.asv.hdp;

import java.util.List;

class Utils {


	public static void changeAtAbout(List<Double> list, int position,
			double about) {
		double x = list.get(position);
		list.set(position, x + about);
	}

	public static void changeAtAbout(List<Integer> list, int position,
			int about) {
		if (list.size() <= position)
			list.add(about);
		else {
			int x = list.get(position);
			list.set(position, x + about);
		}
	}

	
	public static void shuffle(int[] arr){
		int l = arr.length; 
		for (int i = 0; i < l; i++) 
		   swap(arr, i, i + (int) (Math.random() * (l-i)));
	}
	
	
	public static void swap(int[] wordCountByTopic, int arg1, int arg2){
		   int t = wordCountByTopic[arg1]; 
		   wordCountByTopic[arg1] = wordCountByTopic[arg2]; 
		   wordCountByTopic[arg2] = t; 
	}
	
	
	public static double[] ensureCapacity(double[] arr, int min){
		int length = arr.length;
		if (min < length)
			return arr;
		double[] arr2 = new double[min*2];
		for (int i = 0; i < length; i++) 
			arr2[i] = arr[i];
		return arr2;
	}

	public static int[] ensureCapacity(int[] arr, int min) {
		int length = arr.length;
		if (min < length)
			return arr;
		int[] arr2 = new int[min*2];
		for (int i = 0; i < length; i++) 
			arr2[i] = arr[i];
		return arr2;
	}
	
	
}