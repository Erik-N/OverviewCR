package kire.apps.localTracker;

public class Character {
	private int pixels;
	private String character = "";
	
	
	
	public Character(int pixels, String character) {
		super();
		this.pixels = pixels;
		this.character = character;
	}
	public int getPixels() {
		return pixels;
	}
	public void setPixels(int pixels) {
		this.pixels = pixels;
	}
	public String getCharacter() {
		return character;
	}
	public void setCharacter(String character) {
		this.character = character;
	}
	
	
	
	
}
