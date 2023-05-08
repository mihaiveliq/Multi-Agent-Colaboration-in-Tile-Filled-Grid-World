package my;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import base.Action;
import base.Perceptions;
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
		/**
		 * Obstacles perceived. They are perceived as a relative orientation at which an obstacle is neighbor to the
		 * agent. The orientation is relative to the absolute orientation of the agent.
		 */
		protected Set<GridRelativeOrientation>	obstacles;
		/**
		 * <code>true</code> if there is trash in the current tile.
		 */
		protected boolean						isOverJtile;
		/**
		 * The indication of the agent's compass.
		 */
		protected GridOrientation				absoluteOrientation;
		
		/**
		 * @param obstacleOrientations
		 *            - Obstacles perceived. They are perceived as a relative orientation at which an obstacle is
		 *            neighbor to the agent. The orientation is relative to the absolute orientation of the agent.
		 * @param jTile
		 *            - <code>true</code> if there is trash in the current tile.
		 * @param orientation
		 *            - The indication of the agent's compass.
		 */
		public MyAgentPerceptions(Set<GridRelativeOrientation> obstacleOrientations, boolean jTile,
				GridOrientation orientation) {
			obstacles = new HashSet<>(obstacleOrientations);
			isOverJtile = jTile;
			absoluteOrientation = orientation;
		}
		
		/**
		 * @return the obstacles, each obstacle perceived as a relative orientation at which the obstacle is neighbor to
		 *         the agent's current position. The orientation is relative to the absolute orientation of the agent.
		 */
		public Set<GridRelativeOrientation> getObstacles() {
			return obstacles;
		}
		
		/**
		 * @return <code>true</code> if there is trash at the agent's current position.
		 */
		public boolean isOverJtile() {
			return isOverJtile;
		}
		
		/**
		 * @return the indication of the agent's compass.
		 */
		public GridOrientation getAbsoluteOrientation() {
			return absoluteOrientation;
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

		boolean isOverJtile = getHoles().contains(currGridPosition);

		Set<GridRelativeOrientation> obstacles = new HashSet<>();

		for (GridRelativeOrientation currGridRelativeOrientation : GridRelativeOrientation.values()) {
			if (getObstacles().contains(currGridPosition.getNeighborPosition(currGridOrientation, currGridRelativeOrientation))) {
				obstacles.add(currGridRelativeOrientation);
			}
		}

		MyAgentPerceptions myAgentPerceptions = new MyAgentPerceptions(obstacles, isOverJtile, currGridOrientation);
		Action action = myAgent.getAgent().response(myAgentPerceptions);

		switch (((MyAction) action)) {
			case FORWARD -> myAgent.setPosition(currGridPosition.getNeighborPosition(currGridOrientation));
			case TURN_LEFT ->
					myAgent.setOrientation(currGridOrientation.computeRelativeOrientation(GridRelativeOrientation.LEFT));
			case TURN_RIGHT ->
					myAgent.setOrientation(currGridOrientation.computeRelativeOrientation(GridRelativeOrientation.RIGHT));
			case PICK -> cleanTile(currGridPosition);
		}
	}
}
