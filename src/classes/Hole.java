package classes;

import java.io.Serializable;

public class Hole implements Serializable {
	int depth;
	String color;
	
	public Hole(int depth, String color) {
		this.depth = depth;
		this.color = color;
	}
	
	public int getDepth() {
		return depth;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public String getColor() {
		return color;
	}
	public void setColor(String color) {
		this.color = color;
	}

}
