package classes;

public class TileStack {
	int noTiles;
	String color;
	
	public TileStack(int noTiles, String color) {
		this.noTiles = noTiles;
		this.color = color;
		
	}

	public int getNoTiles() {
		return noTiles;
	}

	public void setNoTiles(int noTiles) {
		this.noTiles = noTiles;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}
	
}
