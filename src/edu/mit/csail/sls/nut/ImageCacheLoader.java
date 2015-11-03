package edu.mit.csail.sls.nut;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class ImageCacheLoader {
	
	public static void executeCacheLoad (String labelType, String tag_type) throws ClassNotFoundException {
		try {
		File file = new File("/afs/csail.mit.edu/u/k/korpusik/nutrition/nikki/food_diaries2");
		LineIterator it = FileUtils.lineIterator(file, "UTF-8");
		try {
		    while (it.hasNext()) {
		    String line = it.nextLine();
		    loadImages(getSegmentation(line, labelType, tag_type));
		    }
		} finally {
		    it.close();
		}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadImages (Segmentation segmentation) {
		try {
			GetImages.getImages(segmentation.foods, segmentation.attributes, segmentation.tokens);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Segmentation getSegmentation (String text, String labelType, String tag_type) throws ClassNotFoundException {
		// get the type of approach for associating foods with attributes
		String type = "Simple"; 
		
		// run CRF and get food-attribute dependencies
		PrintWriter FSTWriter = null;
		try {
			PrintWriter FSTwriter = null;
		    Segmentation result = Tag.runCRF(FSTwriter, text, type, NutritionContext.sentenceTagger, false, labelType, tag_type);
				
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
