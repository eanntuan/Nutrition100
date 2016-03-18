package edu.mit.csail.sls.nut;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import sun.misc.BASE64Encoder;
import sun.net.www.URLConnection;

public class test {
	public static String path="/scratch/images/";

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
			try {

				while ((rawline = br.readLine()) != null) {
					try { String[] tokens=rawline.split(",");
					String foodItem = tokens[0];
					String url = tokens[tokens.length-1];
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

	static String getImageEncoding(final String foodSearch, final String imageUrl) throws IllegalArgumentException{
		//System.out.println(foodSearch + ", "+imageUrl);

		ArrayList<String> badImages = new ArrayList<String>();

		ScheduledExecutorService executor = NutritionContext.executor;
		int size=150;
		URL url;

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
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		/*
		try {
			url = new URL(imageUrl);


			HttpURLConnection conn = (HttpURLConnection) url.openConnection(); 
			conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"); 

			// Open a connection to the URL using the proxy information.
			//URLConnection conn = url.openConnection();
			conn.connect();
			//InputStream inStream = conn.getInputStream();
			//InputStream input = new BufferedInputStream(url.openStream());
			//InputStream inStream = url.openStream();
			//System.setProperty("http.proxyHost", null);


			BufferedImage image = ImageIO.read(url);
			BufferedImage resized = new BufferedImage(size, size, image.getType());
			Graphics2D g = resized.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.drawImage(image, 0, 0, size, size, 0, 0, image.getWidth(), image.getHeight(), null);
			g.dispose();
			File outputfile = new File(path+foodSearch + ".png");
			System.out.println("created file: " + path+foodSearch + ".png");

			if (image == null) {
				System.out.println("null image");
				throw new IllegalArgumentException();
			}

			ImageIO.write(resized, "png", outputfile);

			return "data:image/png;base64,"+encodeToString(resized, "png");

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			badImages.add(foodSearch);
			System.out.println(badImages);
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			badImages.add(foodSearch);
			System.out.println(badImages);
			e.printStackTrace();
		}
		 */
		return imageUrl;

	}

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

	public static void main(String[] args) {
		//writeImagestoCache();
		loadCacheImages();
	}
}