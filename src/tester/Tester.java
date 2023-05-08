package tester;

import base.Environment;

/**
 * Class containing testing functionality.
 * 
 * @author Andrei Olaru
 */
public class Tester {
	/**
	 * The environment to test, containing the agents.
	 */
	protected Environment	env;
	
	/**
	 * Step counter.
	 */
	protected int			stepCount	= 0;
	
	/**
	 * Run a simulation.
	 */
	protected void makeSteps() {
		makeSteps(true, -1);
	}
	
	/**
	 * Calls the <code>step</code> method of the environment until the environment is clean.
	 * 
	 * @param print
	 *            if <code>true</code>, nothing is printed.
	 * @param maxSteps
	 *            maximum number of steps to run for.
	 */
	protected void makeSteps(boolean print, int maxSteps)
	{
		while(!env.goalsCompleted() && (maxSteps < 0 || stepCount < maxSteps))
		{
			env.step();
			stepCount++;
			
			
			if(print) {
				System.out.println(env.printToString());
				System.out.println("Num steps: " + stepCount);
				System.out.println();
			}
			
			try
			{
				Thread.sleep(getDelay());
			} catch(InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @return delay between successive steps.
	 */
	@SuppressWarnings("static-method")
	protected int getDelay() {
		return 0;
	}
}
