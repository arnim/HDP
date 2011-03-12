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

	
	public void shuffle(Object[] arr){
		int l = arr.length; 
		for (int i = 0; i < l; i++) 
		   swap(arr, i, i + (int) (Math.random() * (l-i)));
	}
	
	
	public void swap(Object[] arr, int arg1, int arg2){
		   Object t = arr[arg1]; 
		   arr[arg1] = arr[arg2]; 
		   arr[arg2] = t; 
	}
	
	
	
}