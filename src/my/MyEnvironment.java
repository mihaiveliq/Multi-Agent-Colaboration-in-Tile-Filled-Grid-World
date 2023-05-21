package my;

import java.util.*;

import base.Action;
import base.Perceptions;
import classes.Hole;
import classes.TileStack;
import gridworld.*;

/**
 * Your implementation of the environment in which cleaner agents work.
 *
 * @author Andrei Olaru
 */
public class MyEnvironment extends AbstractGridEnvironment {
	/**
	 * Actions for the cleaner agent.
	 *
	 * @author Andrei Olaru
	 */
	public static enum MyAction implements Action {
		
		/**
		 * The robot must move forward.
		 */
		FORWARD,
		
		/**
		 * The robot must turn to the left.
		 */
		TURN_LEFT,
		
		/**
		 * The robot must turn to the right.
		 */
		TURN_RIGHT,
		
		/**
		 * The robot must clean the current tile.
		 */
		PICK,
		
	}
	
	/**
	 * The perceptions of an agent.
	 * 
	 * @author andreiolaru
	 */
	public static class MyAgentPerceptions implements Perceptions {
		protected Map<GridPosition, Hole> Holes;

		protected Set<GridPosition> Obstacles;

		protected Map<GridPosition, LinkedList<TileStack>> tileStacks;

		protected List<GridAgentData>	otherAgents;

		public MyAgentPerceptions(Map<GridPosition, Hole> holes, Set<GridPosition> obstacles,
				Map<GridPosition, LinkedList<TileStack>> tileStacks, List<GridAgentData> otherAgents) {
			super();
			Holes = holes;
			Obstacles = obstacles;
			this.tileStacks = tileStacks;
			this.otherAgents = otherAgents;
		}

		public MyAgentPerceptions(Map<GridPosition, Hole> holes, Set<GridPosition> obstacles,
				Map<GridPosition, LinkedList<TileStack>> tileStacks) {
			super();
			Holes = holes;
			Obstacles = obstacles;
			this.tileStacks = tileStacks;
		}

		public Map<GridPosition, Hole> getHoles() {
			return Holes;
		}

		public Set<GridPosition> getObstacles() {
			return Obstacles;
		}

		public Map<GridPosition, LinkedList<TileStack>> getTileStacks() {
			return tileStacks;
		}

		public List<GridAgentData> getOtherAgents() {
			return otherAgents;
		}
		
	}
	
	/**
	 * Default constructor. This should call one of the {@link #initialize} methods offered by the super class.
	 */
	public MyEnvironment() {
		this(System.currentTimeMillis()); // new random experiment
		// this(42L); // existing random experiment
	}
	
	/**
	 * Constructor with seed for random generator.
	 * 
	 * @param seed
	 *            the seed to use for randomly generating the environment.
	 */
	public MyEnvironment(long seed) {
		System.out.println("seed: [" + seed + "]");
		Random rand = new Random(seed);
		super.initialize(10, 10, 10, 5, rand);
	}
	
	@Override
	public void step() {
		// TODO Auto-generated method stub
		// this should iterate through all agents, provide them with perceptions, and apply the
		// action they return.
		GridAgentData myAgent = getAgentsData().get(0);

		GridPosition currGridPosition = myAgent.getPosition();
		GridOrientation currGridOrientation = myAgent.getOrientation();

//		boolean isOverJtile = getHoles().contains(currGridPosition);

		Set<GridRelativeOrientation> obstacles = new HashSet<>();

		for (GridRelativeOrientation currGridRelativeOrientation : GridRelativeOrientation.values()) {
			if (getObstacles().contains(currGridPosition.getNeighborPosition(currGridOrientation, currGridRelativeOrientation))) {
				obstacles.add(currGridRelativeOrientation);
			}
		}

//		MyAgentPerceptions myAgentPerceptions = new MyAgentPerceptions(obstacles, isOverJtile, currGridOrientation);
//		Action action = myAgent.getAgent().response(myAgentPerceptions);

//		switch (((MyAction) action)) {
//			case FORWARD -> myAgent.setPosition(currGridPosition.getNeighborPosition(currGridOrientation));
//			case TURN_LEFT ->
//					myAgent.setOrientation(currGridOrientation.computeRelativeOrientation(GridRelativeOrientation.LEFT));
//			case TURN_RIGHT ->
//					myAgent.setOrientation(currGridOrientation.computeRelativeOrientation(GridRelativeOrientation.RIGHT));
//			case PICK -> cleanTile(currGridPosition);
//		}
	}
}
