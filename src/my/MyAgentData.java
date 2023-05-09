package my;

import base.Action;
import base.Agent;
import base.Perceptions;
import classes.Tile;
import gridworld.*;

import java.util.*;

/**
 * Your implementation of a reactive cleaner agent.
 * 
 * @author Andrei Olaru
 */
public class MyAgentData implements Agent
{
	public Boolean holdingTile;
	public Tile heldTile;

	@Override
	public Action response(Perceptions perceptions)
	{
		// TODO Auto-generated method stub
		MyEnvironment.MyAgentPerceptions percept = (MyEnvironment.MyAgentPerceptions) perceptions;
		System.out.println("MyAgent sees current tile is " + (percept.isOverJtile() ? "dirty" : "clean")
				+ "; current orientation is " + percept.getAbsoluteOrientation() + "; obstacles at: "
				+ percept.getObstacles());

		List<MyEnvironment.MyAction> possibleChoices = new ArrayList<>();
		List<MyEnvironment.MyAction> possibleBestChoices = new ArrayList<>();
		HashMap<MyEnvironment.MyAction, Integer> actionChances = new HashMap<>();

		int fronts, lefts, rights;
		fronts = lefts = rights = 0;

		// // clean
		if(percept.isOverJtile())
			return MyEnvironment.MyAction.PICK;

		if(!percept.getObstacles().contains(GridRelativeOrientation.FRONT)) {

			if (!percept.getObstacles().contains(GridRelativeOrientation.FRONT_LEFT)) {
				++fronts;
				++lefts;
			}
			if (!percept.getObstacles().contains(GridRelativeOrientation.FRONT_RIGHT)) {
				++fronts;
				++rights;
			}

			actionChances.put(MyEnvironment.MyAction.FORWARD, fronts);
		}

		if(!percept.getObstacles().contains(GridRelativeOrientation.LEFT)) {

			if (!percept.getObstacles().contains(GridRelativeOrientation.BACK_LEFT)) {
				++lefts;
			}

			actionChances.put(MyEnvironment.MyAction.TURN_LEFT, lefts);
		}

		if(!percept.getObstacles().contains(GridRelativeOrientation.RIGHT)) {

			if (!percept.getObstacles().contains(GridRelativeOrientation.BACK_RIGHT)) {
				++rights;
			}

			actionChances.put(MyEnvironment.MyAction.TURN_RIGHT, rights);
		}

		if (fronts == 0 && lefts == 0 && rights == 0) {
			return MyEnvironment.MyAction.TURN_RIGHT;
		}

		int max = Collections.max(actionChances.values());
		Random rand = new Random();
		int randomActionChance = rand.nextInt(4);

		for (MyEnvironment.MyAction a : actionChances.keySet()) {
			if (actionChances.get(a) == max) {
				possibleBestChoices.add(a);
			}
			possibleChoices.add(a);
		}

		MyEnvironment.MyAction randomBestAction = possibleChoices.get(rand.nextInt(possibleBestChoices.size()));
		MyEnvironment.MyAction randomAction = possibleChoices.get(rand.nextInt(possibleChoices.size()));

		if (randomActionChance == 2) {
			return randomAction;
		}

		return randomBestAction;
	}
	
	@Override
	public String toString()
	{
		// TODO Auto-generated method stub
		// please use a single character
		return "M";
	}

	private Action manageObstacle(GridOrientation absoluteOrientation) {


		return MyEnvironment.MyAction.FORWARD;
	}
	
}
