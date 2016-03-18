package edu.mit.csail.sls.nut;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;


import javax.imageio.ImageIO;

import org.apache.commons.lang.StringUtils;
import org.atteo.evo.inflector.English;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import edu.mit.csail.sls.nut.databaseLookup.usda.USDALookup;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class GetImages {

	public static String path="/scratch/images/";
	//	public static String path="/Users/rnaphtal/Desktop/imageCache/";

	//	public static String imageLink = "";
	private static Map<String, String> foodImages = new HashMap<>();
	//	private static Map<String, String> foodEncodingImages = new HashMap<>();
	private static Map<String, String> foodImageEncoding = new HashMap<>();
	private static ArrayList<String> foodID = new ArrayList<String>();

	public static Map<String, String> createImageHash(String ndb_no, String imageLink){
		System.out.println("");
		System.out.println("In GetImages.createImageHash");
		//System.out.println("food description: " + description + ", image name: " + imageLink);

		/*
		String foodDesc[] = description.split(" ", 2);
		String firstWord = foodDesc[0];
		firstWord = firstWord.replaceAll("[^A-Za-z]", "");
		 */
		foodID.add(ndb_no);
		foodImages.put(ndb_no, imageLink);

		boolean noRes = true;

		System.out.println("Printing contents of foodimages");

		for (Map.Entry<String, String> entry : foodImages.entrySet()) {
			String foodName = entry.getKey();
			String imagePath = entry.getValue();

			System.out.println ("Food ID: " + ndb_no + ", Image Path: " + imagePath);
			File f = new File(path+ ndb_no + ".png");
			System.out.println("File path: " + f.getAbsolutePath());

			if (f.exists() && !f.isDirectory()) {
				System.out.println("Found file name in database");
				BufferedImage buffImg;
				try {
					buffImg = ImageIO.read(f);
					foodImageEncoding.put(ndb_no, "data:image/png;base64,"+encodeToString(buffImg, "png"));
					//noRes = false;
					System.out.println("TESTING: Loaded image imageName:"+f.getAbsolutePath());
					System.out.println("");
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("Should not fetch from file");
				foodImageEncoding.put(ndb_no, "");
			}

			noRes=false;
			long startTime = System.currentTimeMillis(); // fetch starting time
		}

		System.out.println("foodImageEncoding size: " + foodImageEncoding.size());
		for (Map.Entry<String, String> entry : foodImageEncoding.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println ("Key: " + key);
		}
		return foodImageEncoding;
	}

	static Map<String, String> getImageEncodings(){
		System.out.println("");
		System.out.println("in getting image encodings method");
		//System.out.println("getImageEncoding size : " + foodImageEncoding.size());
		for (Map.Entry<String, String> entry : foodImageEncoding.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			System.out.println ("Key: " + key);
		}
		return foodImageEncoding;
	}

	static ArrayList<String> getFoodID(){
		System.out.println("getting food id");
		for(String s: foodID){
			System.out.println(s);
		}
		return foodID;
	}



	/**
	 * Get images of food items with attributes from Google image search
	 * 
	 * @param foodItems
	 * @param segmentDeps
	 * @param segmentation
	 * @return
	 * @throws IOException
	 */
	static Map<String, String> getImages(ArrayList<String> foods,
			Map<String, ArrayList<Segment>> segmentDeps,
			ArrayList<String> tokens) throws IOException {


		Map<String, String> foodImages = new HashMap<>();

		// for (String food : foods) {
		for (String food : segmentDeps.keySet()) {
			ArrayList<Segment> attributes = segmentDeps.get(food);
			System.out.println(attributes);
			String newFood = "";
			String foodNoPunc = "";
			// remove numeric values (i.e. indices) from food name
			newFood = food.replaceAll("[^A-Za-z]", "");
			foodNoPunc = food.replaceAll("[^A-Za-z]", "");

			System.out.println("food: " + food);
			System.out.println("food no punc: " + newFood);

			for (String foodItem : segmentDeps.keySet()) {
				System.out.println("segment deps food item: " + foodItem);
			}
			System.out.println();
			String description="";
			String brand="";
			for (Segment segment : attributes) {
				if (segment.label.equals("Brand")) {
					String attrString = StringUtils.join(
							tokens.subList(segment.start, segment.end), " ");
					System.out.println("attribute string: " + attrString);
					//					attrString = attrString.replaceAll("%", "%20percent");
					//					attrString = attrString.replaceAll(" ", "%20");
					//System.out.println(attrString);
					//					newFood += "%20";
					brand += " "+attrString;
					System.out.println("brand: " + brand);
				} else if (segment.label.equals("Description")) {
					String attrString = StringUtils.join(
							tokens.subList(segment.start, segment.end), " ");
					description += " "+attrString;
				}
				// String attrString =
				// StringUtils.join(tokens.subList(segment.start, segment.end),
				// "%20");

			}
			newFood=brand.trim()+" "+description.trim()+" "+food.replaceAll("[^A-Za-z]", "");
			newFood= newFood.replaceAll("  ", " ").trim();
			newFood= newFood.replaceAll("%", "%20percent");
			newFood= newFood.replaceAll(" ", "%20");

			System.out.println("new food: " + newFood);

			// "cereal%20bowl" doesn't work, but "cereal%20a%20bowl" does
			if (newFood.equals("cereal%20bowl")) {
				newFood = "cereal%20a%20bowl";
			} else if (newFood.equals("milk%20a%20glass%20whole")) {
				newFood = "milk%20glass%20whole";
			}
			newFood = newFood.replaceAll("/", "__");
			if (newFood.equals("nut%20a%20small%20ziplock%20bag%20full%20honey")){
				continue;
			}

			// retry sending URL request until get data back
			boolean noRes = true;

			//first check if it is cached
			//if there is an image link associated with it
			/*if(imageLink.length() > 1){
				File f = new File(path+imageLink);
			}*/

			File f = new File(path+newFood + ".png");

			if (f.exists() && !f.isDirectory()) {
				System.out.println("Should fetch from file and implemented");
				BufferedImage buffImg;
				try {
					buffImg = ImageIO.read(f);
					foodImages.put(foodNoPunc, "data:image/png;base64,"+encodeToString(buffImg, "png"));
					noRes = false;
					System.out.println("Loaded image"+f.getAbsolutePath());
				} catch (IOException e) {
					e.printStackTrace();
				}

			} else {
				System.out.println("new food: " + newFood);
				System.out.println("Should not fetch from file");
				foodImages.put(food, "");
			}

			noRes=false;
			long startTime = System.currentTimeMillis(); // fetch starting time
			while (noRes) {
				// break out of while loop after .1 secs
				if (System.currentTimeMillis() - startTime > 100) {
					break;
				}
				URL url = new URL(
						"https://ajax.googleapis.com/ajax/services/search/images?v=1.0&q="
								+ newFood + "&userip=USERS-IP-ADDRESS");
				URLConnection connection = url.openConnection();
				connection.addRequestProperty("Referer",
						"http://web.sls.csail.mit.edu/Nutrition/");

				String line;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}

				noRes = builder.toString().contains("qps rate exceeded");
				System.out.println(noRes + builder.toString());
				if (!noRes) {
					JSONObject json;
					try {
						json = new JSONObject(builder.toString());
						JSONObject responseData = (JSONObject) json
								.get("responseData");
						JSONArray results = (JSONArray) responseData
								.get("results");
						JSONObject img = (JSONObject) results.get(0);
						String imgUrl = (String) img.get("url");
						try {

							foodImages.put(food, getImageEncoding(newFood, imgUrl));
						} catch (Exception e) {
							//							e.printStackTrace();
							foodImages.put(food, imgUrl);
						}
						// foodImages.put(food, imgUrl);
						System.out.println("imgURL" + imgUrl);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}

		return foodImages;
	}

	/**
	 * Gets the new file, saves it, and returns the encoding
	 * @param foodSearch the name for the new file
	 * @param imageUrl the url of the image
	 * @return
	 * @throws IllegalArgumentException
	 */
	static String getImageEncoding(final String foodSearch, final String imageUrl) throws IllegalArgumentException{
		System.out.println(foodSearch + ", "+imageUrl);

		ScheduledExecutorService executor = NutritionContext.executor;

		int size=150;
		URL url;
		try {
			// This is where you'd define the proxy's host name and port.
			//SocketAddress address = new InetSocketAddress(ursa.csail.mit.edu, port);

			// Create an HTTP Proxy using the above SocketAddress.
			//Proxy proxy = new Proxy(Proxy.Type.HTTP, address);

			//System.setProperty("http.proxyHost","ursa.csail.mit.edu"); 
			//System.setProperty("http.proxyPort", "8002");

			url = new URL(imageUrl);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
			conn.addRequestProperty("User-Agent", "Mozilla/4.76"); 

			// Open a connection to the URL using the proxy information.
			//URLConnection conn = url.openConnection();
			//conn.setRequestProperty("User-Agent", "NING/1.0") ;
			InputStream inStream = conn.getInputStream();
			//InputStream inStream = url.openStream();
			//System.setProperty("http.proxyHost", null);


			BufferedImage image = ImageIO.read(url);
			BufferedImage resized = new BufferedImage(size, size, image.getType());
			Graphics2D g = resized.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, 0, 0, size, size, 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();
			//		Image bufferedimage= image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
			File outputfile = new File(path+foodSearch + ".png");

			if (image == null) {
				System.out.println("null image");
				throw new IllegalArgumentException();
			}
			ImageIO.write(resized, "png", outputfile);
			return "data:image/png;base64,"+encodeToString(resized, "png");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/*
		final Future<String> handler = executor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				try {
					int size=150;
					URL url = new URL(imageUrl);
					BufferedImage image = ImageIO.read(url);
					BufferedImage resized = new BufferedImage(size, size, image.getType());
				    Graphics2D g = resized.createGraphics();
				    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				    g.drawImage(image, 0, 0, size, size, 0, 0, image.getWidth(), image.getHeight(), null);
				    g.dispose();
//					Image bufferedimage= image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
					File outputfile = new File(path+foodSearch + ".png");

					if (image == null) {
						System.out.println("null image");
						throw new IllegalArgumentException();
					}
					ImageIO.write(resized, "png", outputfile);
					return "data:image/png;base64,"+encodeToString(resized, "png");
				} catch (javax.imageio.IIOException e) {
					return "";
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return imageUrl;
			}
		});

		executor.schedule(new Runnable() {
			@Override
			public void run() {
				handler.cancel(true);
			}
		}, 1000, TimeUnit.MILLISECONDS);
		try {
			return handler.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 */
		return imageUrl;
	}

	/**
	 * Encode image to string
	 * 
	 * @param image
	 *            The image to encode
	 * @param type
	 *            jpeg, bmp, ...
	 * @return encoded string
	 */
	public static String encodeToString(BufferedImage image, String type) {
		String imageString = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		if (image==null){
			return null;
		}
		try {
			ImageIO.write(image, type, bos);
			byte[] imageBytes = bos.toByteArray();

			BASE64Encoder encoder = new BASE64Encoder();
			imageString = encoder.encode(imageBytes);

			bos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageString;
	}

	/**
	 * Loads the images from the csv file (with formatting image name, url), and 
	 * stores them in the folder specified in getImageEncoding
	 */
	public static void loadCacheImages () {
		/**
		 * Finds which of the new items already have entries in the cache
		 */


		String path = "/afs/csail.mit.edu/u/e/eanntuan/Desktop/";
		String file = path+"javaLoadingURL.csv";

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));

			String rawline;
			String startsWith = "http";
			try {

				while ((rawline = br.readLine()) != null) {
					try { String[] tokens=rawline.split(",");
					String foodItem = tokens[0];
					//String url = tokens[tokens.length-1];
					String url = "";
					for (int i = 0; i<tokens.length; i++){
						if (tokens[i].contains(startsWith)){
							for (int j = i; j<tokens.length; j++){
								url = url + tokens[j];
							}
						}
					}
					//System.out.println("url: " + url);
					getImageEncoding(foodItem, url);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} finally {
				br.close();
			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/**
	 * Loads the images from the csv file (with formatting image name, url), and 
	 * stores them in the folder specified in getImageEncoding (used for actual data info and not url returned from AMT)
	 */
	public static void writeImagestoCache () {
		/**
		 * Finds which of the new items already have entries in the cache
		 */
		// Map<String, USDAResult> is what is needed to do lookup
		String path = "/afs/csail.mit.edu/u/e/eanntuan/Desktop/";
		String file = path+"javaLoadingData.csv";

		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));

			int size=150;
			String rawline;
			String imageEncoding = "";
			String startsWith = "data";
			try {
				while ((rawline = br.readLine()) != null) {
					try { 
						String[] tokens=rawline.split(",");
						System.out.println(Arrays.toString(tokens));
						String foodItem= tokens[0]; //should be the ID number
						System.out.println("food ID in cache: " + foodItem);
						imageEncoding = tokens[tokens.length - 1];

						/*
						for (int i = 0; i<tokens.length; i++){
							if (tokens[i].contains(startsWith)){
								for (int j = i; j<tokens.length; j++){
									imageEncoding = imageEncoding + tokens[j];
								}
							}
						}
						*/
						
						System.out.println("encoding: " + imageEncoding);
						BASE64Decoder decoder = new BASE64Decoder();
						//Write to image
						//System.out.println("is base 64: " + Base64.isbase64(imageEncoding));
						//byte[] data = Base64.decode(imageEncoding);
						byte[] data = decoder.decodeBuffer(imageEncoding);
						ByteArrayInputStream bis = new ByteArrayInputStream(data);  
						BufferedImage image = ImageIO.read(bis); 
						
						
						
						
						System.out.println("data: " + data);
						//InputStream in = new ByteArrayInputStream(data);
						//BufferedImage image = ImageIO.read(in);
						BufferedImage resized = new BufferedImage(size, size, image.getType());
						Graphics2D g = resized.createGraphics();
						g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g.drawImage(image, 0, 0, size, size, 0, 0, image.getWidth(), image.getHeight(), null);
						g.dispose();
						//					Image bufferedimage= image.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
						File outputfile = new File(path+foodItem + ".png");
						System.out.println("created file at: " + path+foodItem + ".png");

						if (image == null) {
							System.out.println("null image");
							throw new IllegalArgumentException();
						}
						ImageIO.write(resized, "png", outputfile);
						//					try (OutputStream stream = new FileOutputStream(path+foodItem+".png")) {
						//					    stream.write(data);
						//					}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} finally {
				br.close();
				//					resultWriter.close();
			}


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		writeImagestoCache();
		//loadCacheImages();
	}

	public static String getUpdatedImage(String food, String brand,
			String descriptionSpecified) {
		String newFood= brand+" "+descriptionSpecified+" "+food;
		newFood= newFood.replaceAll("  ", " ").trim();
		newFood= newFood.replaceAll("%", "%20percent");
		newFood= newFood.replaceAll(" ", "%20");
		System.out.println("Food for picture: "+newFood);
		File f = new File(path+newFood + ".png");
		if (f.exists() && !f.isDirectory()) {
			System.out.println("Should fetch from file and implemented");
			BufferedImage buffImg;
			try {
				buffImg = ImageIO.read(f);
				return "data:image/png;base64,"+encodeToString(buffImg, "png");
			} catch (IOException e) {
				e.printStackTrace();
			}

		} 
		return "";
	}

}
