package gridworld;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

import base.Agent;
import base.Environment;
import classes.*;

/**
 * Abstract implementation of an environment.
 *
 * @author Andrei Olaru
 */
public abstract class AbstractGridEnvironment implements Environment
{
	/**
	 *
	 * @author Andrei Olaru
	 */
	public static class GridAgentData extends AgentData
	{
		/**
		 * he agent's position.
		 */
		protected GridPosition		position;
		/**
		 * The agent's orientation.
		 */
		protected GridOrientation	orientation;
		
		/**
		 * The number of points held by the agent, according to the point system.
		 */
		private float				points	= 0;

		/**
		 * The color of the agent
		 */
		private String color;
		
		/**
		 * Constructor.
		 *
		 * @param linkedAgent
		 *            - the agent.
		 * @param currentPosition
		 *            - the position.
		 * @param currentOrientation
		 *            - the orientation.
		 */
		public GridAgentData(Agent linkedAgent, String color, GridPosition currentPosition, GridOrientation currentOrientation)
		{
			super(linkedAgent);
			position = currentPosition;
			orientation = currentOrientation;
			this.color = color;
		}
		
		/**
		 * @return the agent
		 */
		public Agent getAgent()
		{
			return agent;
		}
		
		/**
		 * @return the position
		 */
		public GridPosition getPosition()
		{
			return new GridPosition(position);
		}
		
		/**
		 * @return the orientation
		 */
		public GridOrientation getOrientation()
		{
			return orientation;
		}
		
		/**
		 * @param position
		 *            the position to set
		 */
		public void setPosition(GridPosition position)
		{
			this.position = position;
		}
		
		/**
		 * @param orientation
		 *            the orientation to set
		 */
		public void setOrientation(GridOrientation orientation)
		{
			this.orientation = orientation;
		}

		public void setColor(String color) {this.color = color;}
		public String getColor() { return this.color;}
		
		/**
		 * @return the points
		 */
		public float getPoints()
		{
			return points;
		}
		
		/**
		 * @param delta
		 *            - number of points to add; may be negative.
		 */
		public void addPoints(float delta)
		{
			points += delta;
		}
	}
	
	/**
	 * Structure storing information about a nearby agent.
	 * 
	 * @author Andrei Olaru
	 */
	public static class NearbyAgent
	{
		/**
		 * The relative orientation of the position of the nearby agent, relative to the position and orientation of the
		 * perceiving agent.
		 */
		public GridRelativeOrientation	orientation;
		/**
		 * Flag indicating whether the nearby agent is cognitive.
		 */
		public boolean					isCognitive;
		/**
		 * The number of points of the nearby agent.
		 */
		public int						points;
		
		/**
		 * Creates a new structure indicating information about a nearby agent.
		 * 
		 * @param agentOrientation
		 *            - its orientation relative to this agent.
		 * @param isAgentCognitive
		 *            - is it cognitive?
		 * @param agentPoints
		 *            - its points.
		 */
		public NearbyAgent(GridRelativeOrientation agentOrientation, boolean isAgentCognitive, int agentPoints)
		{
			orientation = agentOrientation;
			isCognitive = isAgentCognitive;
			points = agentPoints;
		}
	}
	
	/**
	 * List of all the positions in the environment.
	 */
	protected Set<GridPosition>		positions;
	/**
	 * List of all the J-tiles in the environment. It must be dynamically updated when J-tiles are cleaned.
	 */
	protected Map<GridPosition, Hole> Holes;
	/**
	 * List of all the X-tiles in the environment.
	 */
	protected Set<GridPosition> Obstacles;

	protected Map<GridPosition, LinkedList<TileStack>> tileStacks;
	/**
	 * List of all the agents in the environment.
	 */
	protected List<GridAgentData>	agents	= new ArrayList<>();
	
	/**
	 * @return <code>true</code> if there are no more JTiles.
	 */
	@Override
	public boolean goalsCompleted()
	{
		return Holes.isEmpty();
	}
	
	@Override
	public void addAgent(AgentData agentData)
	{
		agents.add((GridAgentData) agentData);
	}
	
	/**
	 * @return the agents
	 */
	protected List<GridAgentData> getAgentsData()
	{
		return agents;
	}
	
	/**
	 * Minimum x coordinate.
	 */
	protected int	x0;
	/**
	 * Maximum x coordinate.
	 */
	protected int	x1;
	/**
	 * Minimum y coordinate.
	 */
	protected int	y0;
	/**
	 * Maximum y coordinate.
	 */
	protected int	y1;
	/**
	 * Width of displayed cells.
	 */
	protected int	cellW	= 8;
	/**
	 * Height of displayed cells.
	 */
	protected int	cellH	= 2;
	
	/**
	 * Initializes the environment with the specified lists of positions, J-tiles, and X-tiles.
	 * <p>
	 * Adds all positions, computes boundaries.
	 *
	 * @param allPositions
	 *            - the set of all existing positions in the environment.
	 * @param environmentHoles
	 *            - the set of positions (included in <code>allPositions</code>) that contain junk.
	 * @param environmentObstacles
	 *            - the set of positions (included in <code>allPositions</code>) that contain objects.
	 */
	public void initialize(Set<GridPosition> allPositions, Map<GridPosition, Hole> environmentHoles,
			Set<GridPosition> environmentObstacles, Map<GridPosition, LinkedList<TileStack>> tileStacks )
	{
		this.positions = allPositions;
		this.Holes = environmentHoles;
		this.Obstacles = environmentObstacles;
		this.tileStacks = tileStacks;
		
		GridPosition pos = allPositions.iterator().next();
		x0 = x1 = pos.positionX;
		y0 = y1 = pos.positionY;
		for(GridPosition p : allPositions)
		{
			GridPosition gp = p;
			if(gp.positionX < x0)
				x0 = gp.positionX;
			if(gp.positionX > x1)
				x1 = gp.positionX;
			if(gp.positionY < y0)
				y0 = gp.positionY;
			if(gp.positionY > y1)
				y1 = gp.positionY;
		}
	}
	
	/**
	 * Initializes the environment with the provided width, height and number of J- and X-tiles.
	 *
	 * @param w
	 *            - width
	 * @param h
	 *            - height
	 * @param nHoles
	 *            - number of generated J-tiles.
	 * @param nObstacles
	 *            - number of generated X-tiles.
	 * @param rand
	 *            - random number generator to use.
	 */
	protected void initialize(int w, int h, int nHoles, int nObstacles, Random rand)
	{
//		Set<GridPosition> all = new HashSet<>();
//		for(int i = 0; i <= w + 1; i++)
//			for(int j = 0; j <= h + 1; j++)
//				all.add(new GridPosition(i, j));
//		Set<GridPosition> xs = new HashSet<>();
//		for(int i = 0; i <= w + 1; i++)
//		{
//			xs.add(new GridPosition(i, 0));
//			xs.add(new GridPosition(i, h + 1));
//		}
//		for(int j = 0; j <= h + 1; j++)
//		{
//			xs.add(new GridPosition(0, j));
//			xs.add(new GridPosition(w + 1, j));
//		}
//
//		int attempts = nObstacles * nObstacles;
//		int generated = 0;
//		while(attempts > 0 && generated < nObstacles)
//		{
//			int x = rand.nextInt(w) + 1;
//			int y = rand.nextInt(h) + 1;
//			GridPosition pos = new GridPosition(x, y);
//
//			boolean ok = true;
//			for(GridPosition xtile : xs)
//				if(pos.getDistanceTo(xtile) <= 2)
//					ok = false;
//			if(ok)
//			{
//				generated++;
//				xs.add(pos);
//			}
//
//			attempts--;
//		}
//		if(generated < nObstacles)
//			System.out.println("Failed to generate all required X-tiles");
//
//		HashMap<Hole, GridPosition> js = new HashMap<>();
//		attempts = nHoles * nHoles;
//		generated = 0;
//		while((attempts > 0) && (generated < nHoles))
//		{
//			int x = rand.nextInt(w) + 1;
//			int y = rand.nextInt(h) + 1;
//			GridPosition pos = new GridPosition(x, y);
//
//			if(!js.contains(pos) && !xs.contains(pos))
//			{
//				js.add(pos);
//				generated++;
//			}
//
//			attempts--;
//		}
//		if(generated < nHoles)
//			System.out.println("Failed to generate all required J-tiles");
//
//		initialize(all, js, xs);
	}
	
	@Override
	public String printToString()
	{
		// border top
		String res = "";
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println(sdf1.format(ts));
		System.out.print("  |");
		res += "  |";
		for(int i = x0; i <= x1; i++)
		{
			for(int k = 0; k < cellW - (i >= 10 ? 2 : 1); k++) {
				System.out.print(" ");
				res += " ";
				
			}
			System.out.print(i + "|");
			res += i + "|";
		}
		System.out.print("\n");
		res += "\n";
		System.out.print("--+");
		res += "--+";
		for(int i = x0; i <= x1; i++)
		{
			for(int k = 0; k < cellW; k++) {
				System.out.print("-");
				res += "-";
			}
			System.out.print("+");
			res += "+";
		}
		System.out.print("\n");
		res += "\n";
		// for each line
		for(int j = y1; j >= y0; j--)
		{
			// first cell row
			System.out.print((j < 10 ? " " : "") + j + "|");
			res += (j < 10 ? " " : "") + j + "|";
			for(int i = x0; i <= x1; i++)
			{
				GridPosition pos = new GridPosition(i, j);
				String agentString = "";
				for(GridAgentData agent : agents)
					if(agent.getPosition().equals(pos)) {
						agentString += "@";
						String agentColor = agent.getColor();
					}
				int k = 0;
				if(Obstacles.contains(pos))
					for(; k < cellW; k++) {
						System.out.print("X");
						res += "X";
					}
				if(tileStacks.containsKey(pos)==true)
				{
					for(int l=0; l<tileStacks.get(pos).size();l++) {
						switch(tileStacks.get(pos).get(l).getColor()) {
						case "blue":
							System.out.print(ConsoleColors.BLUE + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "green":
							System.out.print(ConsoleColors.GREEN + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "red":
							System.out.print(ConsoleColors.RED + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "yellow":
							System.out.print(ConsoleColors.YELLOW_BRIGHT + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "pink":
							System.out.print(ConsoleColors.PURPLE_BRIGHT + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "orange":
							System.out.print(ConsoleColors.YELLOW + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "violet":
							System.out.print(ConsoleColors.PURPLE + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "maroon":
							System.out.print(ConsoleColors.BLACK_BRIGHT + Integer.toString(tileStacks.get(pos).get(l).getNoTiles()) + "t" + ConsoleColors.RESET);
							//res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						}
					}
					
//					if(Holes.get(pos).getColor().equals("blue")) {
//						System.out.print(ConsoleColors.BLUE + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
//						res += "#" + Integer.toString(Holes.get(pos).getDepth());
//					}
//					else if(Holes.get(pos).getColor().equals("green")) {
//						System.out.print(ConsoleColors.GREEN + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
//						res += "#" + Integer.toString(Holes.get(pos).getDepth());
//					}
					k+=2*tileStacks.get(pos).size();
				}
				if(agentString.length() > 0)
				{
					if(cellW == 1)
					{
						if(agentString.length() > 1) {
							System.out.print(".");
							res += ".";
						}
						else {
							System.out.print(agentString);
							res += agentString;
						}
						k++;
					}
					else
					{
						
						System.out.print(ConsoleColors.GREEN +agentString.substring(0, Math.min(agentString.length(), cellW - k)));
						res += agentString.substring(0, Math.min(agentString.length(), cellW - k));
						k += Math.min(agentString.length(), cellW - k);
					}
				}
				for(; k < cellW; k++) {
					System.out.print(" ");
					res += " ";
				}
				System.out.print("|");
				res += "|";
			}
			System.out.print("\n");
			res += "\n";
			// second cellrow
			System.out.print("  |");
			res += "  |";
			for(int i = x0; i <= x1; i++)
			{
				GridPosition pos = new GridPosition(i, j);
				for(int k = 0; k < cellW; k++)
					if(Obstacles.contains(pos)) {
						System.out.print("X");
						res += "X";
					}
					else if((k == 0) && Holes.containsKey(pos)) {
						switch(Holes.get(pos).getColor()) {
						case "blue":
							System.out.print(ConsoleColors.BLUE + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "green":
							System.out.print(ConsoleColors.GREEN + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "red":
							System.out.print(ConsoleColors.RED + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "yellow":
							System.out.print(ConsoleColors.YELLOW_BRIGHT + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "pink":
							System.out.print(ConsoleColors.PURPLE_BRIGHT + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "orange":
							System.out.print(ConsoleColors.YELLOW + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "violet":
							System.out.print(ConsoleColors.PURPLE + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						case "maroon":
							System.out.print(ConsoleColors.BLACK_BRIGHT + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
							res += "#" + Integer.toString(Holes.get(pos).getDepth());
							break;
						}
//						if(Holes.get(pos).getColor().equals("blue")) {
//							System.out.print(ConsoleColors.BLUE +  "#" +  Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
//							res += "#"+Integer.toString(Holes.get(pos).getDepth());
//						}
//						else if(Holes.get(pos).getColor().equals("green")) {
//							System.out.print(ConsoleColors.GREEN  + "#" + Integer.toString(Holes.get(pos).getDepth()) + ConsoleColors.RESET);
//							res += "#" + Integer.toString(Holes.get(pos).getDepth());
//						}
						k++;
					}
					else {
						System.out.print(" ");
						res += " ";
					}
				System.out.print("|");
				res += "|";
			}
			System.out.print("\n");
			res += "\n";
			// other cell rows
			for(int ky = 0; ky < cellH - 2; ky++)
			{
				System.out.print("|");
				res += "|";
				for(int i = x0; i <= x1; i++)
				{
					for(int k = 0; k < cellW; k++) {
						System.out.print(Obstacles.contains(new GridPosition(i, j)) ? "X" : " ");
						res += Obstacles.contains(new GridPosition(i, j)) ? "X" : " ";
					}
					System.out.print("|");
					res += "|";
				}
				System.out.print("\n");
				res += "\n";
			}
			System.out.print("--+");
			res += "--+";
			for(int i = x0; i <= x1; i++)
			{
				for(int k = 0; k < cellW; k++) {
					System.out.print("-");
					res += "-";
				}
				System.out.print("+");
				res += "+";
			}
			System.out.print("\n");
			res += "\n";

		}
		System.out.print("\n\n");
		return res;
	}
	
	/**
	 * @return a {@link Set} of {@link GridPosition} instances indicating all positions in the environment.
	 */
	protected Set<GridPosition> getPositions()
	{
		return convertPositions(positions);
	}
	
	/**
	 * @return the bottom-left available position in the grid (minimum x and y)
	 */
	public GridPosition getBottomLeft()
	{
		return new GridPosition(x0 + 1, y0 + 1);
	}
	
	/**
	 * @return the top-left available position in the grid (minimum x, maximum y)
	 */
	public GridPosition getTopLeft()
	{
		return new GridPosition(x0 + 1, y1 - 1);
	}
	
	/**
	 * @return the bottom-right available position in the grid (maximum x, minimum y)
	 */
	public GridPosition getBottomRight()
	{
		return new GridPosition(x1 - 1, y0 + 1);
	}
	
	/**
	 * @return the top-right available position in the grid (maximum x and y)
	 */
	public GridPosition getTopRight()
	{
		return new GridPosition(x1 - 1, y1 - 1);
	}
	
	/**
	 * @return a {@link Set} of {@link GridPosition} instances indicating all positions of J-tiles in the environment.
	 */
//	protected Set<GridPosition> getHoles()
//	{
//		return convertPositions(Holes);
//	}
	
	/**
	 * @return a {@link Set} of {@link GridPosition} instances indicating all positions of X-tiles in the environment.
	 */
	protected Set<GridPosition> getObstacles()
	{
		return convertPositions(Obstacles);
	}
	
	/**
	 * Converts a set of {@link GridPosition} instances to a set of {@link GridPosition} instances.
	 *
	 * @param toConvert
	 *            - the set of positions to convert.
	 * @return the result.
	 */
	protected static Set<GridPosition> convertPositions(Set<GridPosition> toConvert)
	{
		Set<GridPosition> ret = new HashSet<>();
		for(GridPosition pos : toConvert)
			ret.add(pos);
		return ret;
	}
	
	/**
	 * Removes a position from the list of dirty tiles.
	 * 
	 * @param position
	 *            the J-tile to remove.
	 */
	protected void cleanTile(GridPosition position)
	{
//		if(!Holes.contains(position))
//			throw new IllegalArgumentException("GridPosition was not dirty");
//		Holes.remove(position);
	}
}
