package platform;

import gridworld.GridPosition;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.util.ExtendedProperties;
import jade.util.leap.Properties;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.stream.Collectors;

import classes.TileStack;
import classes.Hole;
import gridworld.AbstractGridEnvironment.GridAgentData;
import gridworld.AbstractGridEnvironment;
import my.MyAgentData;
import agents.MyAgent;
import agents.MyEnvironmentAgent;
import base.Agent;
/**
 * Launches a main container and associated agents.
 */
public class MainContainerLauncher {
	/**
	 * The main container.
	 */
	AgentContainer						mainContainer;
	/**
	 * Agent ID (as a number) -> Key -> Numeric value
	 */
	Map<String, GridPosition>	agentConfig	= new HashMap<>();
	Map<Integer, String>	agentColors	= new HashMap<>();
	Map<Integer, GridPosition> agentPositions = new HashMap<>();
	Set<GridPosition> obstacles = new HashSet<>();
	Map<GridPosition,  LinkedList<TileStack>> tileStackPositions= new HashMap<>();
	Map<GridPosition, Hole> holesPositions= new HashMap<>();
	int widthMap;
	int heightMap;
	int operationTime;
	int totalSimulationTime;

	
	/**
	 * Configures and launches the main container.
	 */
	void setupPlatform() {
		Properties mainProps = new ExtendedProperties();
		mainProps.setProperty(Profile.GUI, "true"); // start the JADE GUI
		mainProps.setProperty(Profile.MAIN, "true"); // is main container
		mainProps.setProperty(Profile.CONTAINER_NAME, "Proiect-Main");
		
		mainProps.setProperty(Profile.LOCAL_HOST, "localhost");
		mainProps.setProperty(Profile.LOCAL_PORT, "1099");
		mainProps.setProperty(Profile.PLATFORM_ID, "proiect");
		
		ProfileImpl mainProfile = new ProfileImpl(mainProps);
		mainContainer = Runtime.instance().createMainContainer(mainProfile);
	}
	
	public static boolean isNumeric(String string) {
	    int intValue;
			
	    System.out.println(String.format("Parsing string: \"%s\"", string));
			
	    if(string == null || string.equals("")) {
	        System.out.println("String cannot be parsed, it is null or empty.");
	        return false;
	    }
	    
	    try {
	        intValue = Integer.parseInt(string);
	        return true;
	    } catch (NumberFormatException e) {
	        System.out.println("Input String cannot be parsed to Integer.");
	    }
	    return false;
	}
	
	/**
	 * Read agent parent-child relationship configuration from the data/config.csv file.
	 * 
	 * @throws IOException
	 *             if the file is not found.
	 */
	@SuppressWarnings("serial")
	void readConfig() throws IOException {
//		try (BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\diana\\git\\ProiectSMA\\tests\\system__default.txt")))) {
//		InputStream in = FileLoader.class.getResourceAsStream("<relative path from this class to the file to be read>");
		try (BufferedReader br = new BufferedReader(new FileReader(new File("tests/system__default.txt")))) {

			String line;
			
			line = br.lines().collect(Collectors.joining(System.lineSeparator()));;
			//System.out.println(line);
			String[] values = line.split("\\s+");
			int  noAgents = Integer.parseInt(values[0]);
			operationTime = Integer.parseInt(values[1]);
			totalSimulationTime = Integer.parseInt(values[2]);
			widthMap = Integer.parseInt(values[3]);
			heightMap = Integer.parseInt(values[4]);
			int id = 1;
			
			//read agents(identified through colors)
			for(int i=5; i<5+noAgents; i++) {
				
				agentColors.put(id, values[i]);
				id++;
			}
			id = 1;
			//read agents positions
			for(int i=5+noAgents; i<5+noAgents+2*noAgents; i=i+2) {
				GridPosition agentPos = new GridPosition(Integer.parseInt(values[i]), Integer.parseInt(values[i+1]));
				agentPositions.put(id, agentPos);
				id++;
			}
			for(int idx: agentColors.keySet()) {
				agentConfig.put(agentColors.get(idx), agentPositions.get(idx));
			}
			int i = 5+noAgents+2*noAgents+1; //skip OBSTACLES keyword
			//read Obstacles
			while(values[i].equals("TILES")==false) {
				GridPosition obstacle = new GridPosition(Integer.parseInt(values[i]), Integer.parseInt(values[i+1]));
				obstacles.add(obstacle);
				i=i+2;
			}
			i++; //skip TILES keyword
			while(values[i].equals("HOLES")==false) {
				TileStack tileStack = new TileStack(Integer.parseInt(values[i]), values[i+1]);
				GridPosition tileStackPos = new GridPosition(Integer.parseInt(values[i+2]), Integer.parseInt(values[i+3]));
				if(tileStackPositions.containsKey(tileStackPos))
					tileStackPositions.get(tileStackPos).add(tileStack);
				else {
					LinkedList<TileStack> list = new LinkedList<>();
					list.add(tileStack);
					tileStackPositions.put(tileStackPos, list);
				}
				i=i+4;
			}
			i++; //skip HOLES keyword
			for(int j=i; j<values.length;j=j+4) {
				Hole hole = new Hole(Integer.parseInt(values[j]), values[j+1]);
				GridPosition holePos = new GridPosition(Integer.parseInt(values[j+2]), Integer.parseInt(values[j+3]));
				holesPositions.put(holePos, hole);
			}
			System.out.println(holesPositions);
			br.close();
		}
	}
	
	
	
	/**
	 * Starts the agents assigned to the main container.
	 */
	void startAgents() {
		try {
			AgentController agentEnvCtrl = mainContainer.createNewAgent("Environment",
					MyEnvironmentAgent.class.getName(), new Object[] { holesPositions, obstacles, tileStackPositions, widthMap, heightMap, operationTime, totalSimulationTime });
			agentEnvCtrl.start();

			for(String agColor : agentConfig.keySet()) {
				GridPosition agentPosition = agentConfig.get(agColor);
				
				
				AgentController agentCtrl = mainContainer.createNewAgent(agColor,
						MyAgent.class.getName(), new Object[] { agColor, agentPosition });
				agentCtrl.start();
			}
			//holesPositions, obstacles, tileStackPositions
			
		} catch(StaleProxyException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param ID
	 *            - a numeric ID.
	 * @return am agent name based on a numeric ID.
	 */
	public static String createAgentName(int ID) {
		return "agent_" + ID;
	}
	
	/**
	 * Launches the main container.
	 * 
	 * @param args
	 *            - not used.
	 */
	public static void main(String[] args) {
		MainContainerLauncher launcher = new MainContainerLauncher();
		
		try {
			launcher.readConfig();
			launcher.setupPlatform();
			launcher.startAgents();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
