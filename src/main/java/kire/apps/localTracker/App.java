package kire.apps.localTracker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import boofcv.alg.filter.basic.GrayImageOps;
import boofcv.core.image.ConvertBufferedImage;
import boofcv.struct.image.ImageUInt8;


public class App {
	
	
	private static List<String> idList = new ArrayList<String>();
	
	private static Color red = new Color(255,0,0);
	private static int redInt = red.getRGB();	
	
	private static Color blue = new Color(0,0,255);
	private static int blueInt = blue.getRGB();	
	
	private static Color green = new Color(0,255,0);
	private static int greenInt = green.getRGB();	
	
	private static BufferedImage image;
	private static int minX;
	private static int maxX;
	private static int minY;
	private static int maxY;

	private static final int rowHeight = 48;
	
	private static ArrayList<Character> library = new ArrayList<Character>();
	
    public static void main( String[] args ) throws IOException    {
    	// test library
    	Character P = new Character(216, "P");
    	library.add(P);
    	Character r = new Character(108, "r");
    	library.add(r);
    	Character o = new Character(198, "o");
    	library.add(o);
    	Character t = new Character(153, "t");
    	library.add(t);
    	Character s = new Character(207, "s");
    	library.add(s);
    	
    	// Get the file, buffer it into memory, split it into component rows
    	File file = new File("playerNames.png");
    	image = ImageIO.read(file);

      	image = configureImage(image);
      	getRows(image);
          	 
		ArrayList<String> nameList = new ArrayList<String>();

		File dir = new File("imageRows");
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				image = ImageIO.read(child);
		    	ArrayList<Character> characters = new ArrayList<Character>();
		    	maxX = 0;
		    	characters = getCharacters(0, 0, characters, 0);
				String name = makeWord(characters);
				nameList.add(name);
				saveFile();
			}
		} else {
			// Handle the case where dir is not really a directory.
			// Checking dir.isDirectory() above would not be sufficient
			// to avoid race conditions with another process that deletes
			// directories.
		}
    	for(File imgFile: dir.listFiles()) {
    		imgFile.delete();
    	}
    	
		for(String name:nameList) {
			System.out.println(name);
		}
  
    }
    
		private static ArrayList<Character> getCharacters(int startX, int startY, ArrayList<Character> characters, int totalPixels) { 
	    	// Iterate through image
	    	for (int x = startX; x < image.getWidth(); x++) {
	    		for (int y = startY; y < image.getHeight(); y++) {
	    			if(!doesPixelExist(x,y)) {
	    				if(x > maxX + 20){
	            			idList.clear();
	            			image.setRGB(x, y, redInt);
		            		return characters;
	            		}
		            	int argb = image.getRGB(x, y);  
		            	image.setRGB(x, y, greenInt);
		            	// test to see if the pixel is black
	    			
		            	if(isDark(argb)) {
		                	image.setRGB(x, y, blueInt);	
		                	// add this pixels unique ID to the list
		            		idList.add(getID(x,y));
		            		// Search neighbor pixels recursively to map out a character
		            		searchPixels(x, y);
		            		// Create character and add this 
		            		int pixelCount = idList.size() - totalPixels;
		            		Character character = new Character(pixelCount,"");
		            		characters.add(character);
		            		
		            		System.out.println(minY);
		            		getCharacters(maxX, maxY , characters, idList.size());
		            		idList.clear();
		            		
		            		return characters;
		            		
		            		
		            	}
	            	}
            	}
            }
	    	idList.clear();
	    	return characters;
	    
	}

		    	

    	// Create a word, iterate through characters in above block
    	// if a pixel count matches a letter in the library add it to the word
    	// Otherwise add an underscore
    	
    	private static String makeWord(ArrayList<Character> characters) {
	    	String word = "";
	    	// use this to check to see if we found a match
	    	Boolean itemNotFound = true;
	    	for(Character newChar : characters) {
	    		int pixels = newChar.getPixels();
	    		for(Character libChar:library) {
	    			int libPixels = libChar.getPixels();
	    			if(pixels == libPixels) {
	    				newChar.setCharacter(libChar.getCharacter());
	    				itemNotFound = false;
	    			}
	    		}
	    		if(itemNotFound) {
	    			newChar.setCharacter("_");
	    		}
	    		word = word + newChar.getCharacter();
	    		itemNotFound = true;
	    	}
	    	
	    	return word;
    }
    
	private static void searchPixels(int x, int y) {
		
		if (!doesPixelExist(x + 1, y) && x + 1 < image.getWidth()) {
			int right = image.getRGB(x + 1, y);
			if (isDark(right)) {
				maxCheck(x+1, y);
				image.setRGB(x + 1, y, redInt);
				idList.add(getID(x + 1, y));
				searchPixels(x + 1, y);
			}
		}
		if (!doesPixelExist(x, y + 1) && y + 1 < image.getHeight()) {
			// Check down direction
			int down = image.getRGB(x, y + 1);
			if (isDark(down)) {
				maxCheck(x, y+1);
				image.setRGB(x, y + 1, redInt);
				idList.add(getID(x, y + 1));
				searchPixels(x, y + 1);
			}
		}
		if (!doesPixelExist(x - 1, y) && x - 1 > 0) {
			// Check left direction
			int left = image.getRGB(x - 1, y);
			if (isDark(left)) {
				maxCheck(x-1, y);
				image.setRGB(x - 1, y, redInt);
				idList.add(getID(x - 1, y));
				searchPixels(x - 1, y);
			}
		}
		if (!doesPixelExist(x, y - 1) && y - 1 > 0) {
			// Check up direction - shouldn't be used much
			int up = image.getRGB(x, y - 1);
			if (isDark(up)) {
				maxCheck(x, y-1);
				image.setRGB(x, y - 1, redInt);
				idList.add(getID(x, y - 1));
				searchPixels(x, y - 1);
			}
		}
		

	
} 
    
    private static void getRows(BufferedImage image) {
        int rows = (int) Math.floor(image.getHeight() / rowHeight);
        int cols = 1;
        int chunkWidth = image.getWidth();
        int chunkHeight = rowHeight;
        int count = 0;
        BufferedImage imgs[] = new BufferedImage[rows];
        
        for (int x = 0; x < rows; x++) {  
            for (int y = 0; y < cols; y++) {  
                //Initialize the image array with image chunks  
                imgs[count] = new BufferedImage(chunkWidth, chunkHeight, image.getType());  
                
                // draws the image chunk  
                Graphics2D gr = imgs[count++].createGraphics();  
                gr.drawImage(image, 0, 0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight * x,
                		chunkWidth * y + chunkWidth, chunkHeight * x + chunkHeight, null);  
                gr.dispose();  
            }  
        }  
        
        //writing mini images into image files  
        for (int i = 0; i < imgs.length; i++) {  
            try {
				ImageIO.write(imgs[i], "png", new File("imageRows/img" + i + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
        }    
    }  
    	
 
    
    
    private static Boolean doesPixelExist(int x, int y) {
    	String id = getID(x,y);
    	for(String str:idList){
    		if(str.trim().contains(id)) {
    			return true;

    		}
    	}
    	return false;
    }
    
   
    
    private static void maxCheck(int x, int y) {
    	if(x > maxX) {
    		maxX = x;
    	}
    	if(x < minX) {
    		minX = x;
    	}
    	if(y > maxY) {
    		maxY = y;
    	}
    	if(y < minY) {
    		minY = y;
    	}
    }
    
	private static String getID(int x, int y) {
		String id = Integer.toString(x) + "." + Integer.toString(y);
		return id;
	}	
    	
   
    
    private static Boolean isDark(int argb) {
    	int rgb[] = new int[] {
        	    (argb >> 16) & 0xff, //red
        	    (argb >>  8) & 0xff, //green
        	    (argb      ) & 0xff  //blue
        	};
        	
    	// Break pixel into its RGB parts
    	int R = rgb[0];
    	int G = rgb[1];
    	int B = rgb[2];
    	
    	if(R == 0 && G == 0 && G == 0) {
    		return true;
    	}
    	return false;
    }
    
    public static Boolean isColor(int argb, int lum) { 	
    	int rgb[] = new int[] {
        	    (argb >> 16) & 0xff, //red
        	    (argb >>  8) & 0xff, //green
        	    (argb      ) & 0xff  //blue
        	};
        	
    	// Break pixel into its RGB parts
    	int R = rgb[0];
    	int G = rgb[1];
    	int B = rgb[2];
    	
    	// Calculate the luminosity
    	int luminosity = (int) (0.2126*R + 0.7152*G + 0.0722*B);
    	// if Luminosity > 128, the pixel is not black
    	if(luminosity > lum) {
    		return false;
        }
        return true;
    }
    
    
    public static BufferedImage configureImage(BufferedImage image) {
    	// resize image
    	int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
    	int IMG_HEIGHT = image.getHeight()*2;
    	int IMG_WIDTH = image.getWidth()*2;
    	BufferedImage resizedImage1 = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
    	Graphics2D g = resizedImage1.createGraphics();
    	g.drawImage(image, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
    	g.dispose();
    	
    	//grayscale
    	ImageUInt8 gray = ConvertBufferedImage.convertFrom(resizedImage1, (ImageUInt8) null);
    	ImageUInt8 inverted = new ImageUInt8(gray.width,gray.height);
    	GrayImageOps.invert(gray,255,inverted);
    	    	
    	// invert
    	BufferedImage newImage = ConvertBufferedImage.convertTo(inverted, (BufferedImage) null);
       	
    	// Define black
    	Color black = new Color(0,0,0);
    	int blackInt = black.getRGB();
    	// Define white
    	Color white = new Color(255,255,255);
    	int whiteInt = white.getRGB();

    	// Iterate over all image pixels
    	for (int x = 0; x < newImage.getWidth(); x++) {
            for (int y = 0; y < newImage.getHeight(); y++) {
            	int argb = newImage.getRGB(x, y);
            	// if Luminosity > threshold
                if(isColor(argb, 187)) {
                	newImage.setRGB(x, y, blackInt);
                	
                }
                // else the image is black
                else {
                	newImage.setRGB(x, y, whiteInt);
                	
                }
            }
        }
  
    	return newImage;
    }
    
    private static void saveFile() {
    	try {
    	    File outputfile = new File("testImage.png");
    	    ImageIO.write(image, "png", outputfile);
    	    image.flush();
    	    
    	} catch (IOException e) {
    	   
    	}
    }
}
