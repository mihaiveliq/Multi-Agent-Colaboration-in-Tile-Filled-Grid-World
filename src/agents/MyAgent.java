package agents;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.io.*;
import base.Environment;
import classes.Message;
import gridworld.*;
import gridworld.AbstractGridEnvironment.GridAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
//import platform.Log;
import jade.proto.*;
import my.MyAgentData;
import my.MyEnvironment.MyAgentPerceptions;

/**
 * The Agent.
 */
public class MyAgent extends Agent {
    //------------------------
    private AbstractGridEnvironment.GridAgentData gridAgentData;
    //-----------------------
    /**
     * The serial UID.
     */
    private static final long	serialVersionUID			= 2081456560111009192L;
    /**
     * The name of the registration protocol.
     */
    static final String			REGISTRATION_PROTOCOL		= "register-child";
    /**
     * Time between checking for messages.
     */
    static final int			TICK_PERIOD					= 100;
    /**
     * Number of ticks to wait for registration messages.
     */
    static final int			MAX_TICKS					= 50;
    /**
     * Template for registration messages.
     * <p>
     * Alternative: <code>
     * new MatchExpression() {
     *  &#64;Override
     *  public boolean match(ACLMessage msg) {
     *  	return (msg.getPerformative() == ACLMessage.INFORM && msg.getProtocol().equals("register-child"));
     *  }}
     * </code>
     */
    static MessageTemplate		registrationReceiptTemplate	= MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM), MessageTemplate.MatchProtocol(REGISTRATION_PROTOCOL));
    /**
     * Known child agents.
     */
    List<AID>					childAgents					= new LinkedList<>();
    /**
     * The ID of the parent.
     */
    AID							parentAID					= null;
    /**
     * The value associated to the agent.
     */
    int							agentValue;
    private final int MAX_EXECUTED_ACTIONS = 3;
    int currentActionInLoop = 0;
    boolean planWaiting = true;
    int operationTime;
    MyAgentPerceptions perceptions;
    Set<GridPosition> obstacles;
    int widthMap, heightMap;

    /**
     * @param childAID
     *            the ID of the child to add.
     */
    public void addChildAgent(AID childAID) {
        childAgents.add(childAID);
    }

    public void setGridAgentData(AbstractGridEnvironment.GridAgentData gridAgentData) {
        this.gridAgentData = gridAgentData;
    }

    public AbstractGridEnvironment.GridAgentData getGridAgentData() {
        return gridAgentData;
    }

    /**
     * @return the list of IDs of child agents.
     */
    public List<AID> getChildAgents() {
        return childAgents;
    }
    
    // convert a position to the corresponding position number 
    public int positionToNumber(GridPosition pos, int w) {
    	int res = pos.getX()*w+ pos.getY();
    	return res;
    }
    
    public GridPosition numberToPosition(int pos, int w) {
    	return new GridPosition(pos/w, pos%w);
    }
    
    public List<GridPosition> neighbors(GridPosition node) {
        int[][] dirs = {{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        List<GridPosition> result = new ArrayList<>();
        for (int[] dir : dirs) {
            GridPosition neighbor = new GridPosition(node.getX()+dir[0], node.getY()+dir[1]);
            if (0 <= neighbor.getX() && neighbor.getX() < widthMap && 0 <= neighbor.getY() && neighbor.getY() < heightMap && !obstacles.contains(node)) {
                result.add(neighbor);
            }
        }
        return result;
    }
    
 // function to print the shortest distance and path
    // between source vertex and destination vertex
    private void printShortestDistance(
                     ArrayList<ArrayList<Integer>> adj,
                             int s, int dest, int v, int w)
    {
        // predecessor[i] array stores predecessor of
        // i and distance array stores distance of i
        // from s
        int pred[] = new int[v];
        int dist[] = new int[v];
 
        if (BFS(adj, s, dest, v, pred, dist) == false) {
            System.out.println("Given source and destination" +
                                         "are not connected");
            return;
        }
 
        // LinkedList to store path
        LinkedList<Integer> path = new LinkedList<Integer>();
        int crawl = dest;
        path.add(crawl);
        while (pred[crawl] != -1) {
            path.add(pred[crawl]);
            crawl = pred[crawl];
        }
 
        // Print distance
        System.out.println("Shortest path length is: " + dist[dest]);
 
        // Print path
        System.out.println("Path is ::");
        for (int i = path.size() - 1; i >= 0; i--) {
            System.out.print(numberToPosition(path.get(i), w) + " ");
        }
    }
 
    // a modified version of BFS that stores predecessor
    // of each vertex in array pred
    // and its distance from source in array dist
    private static boolean BFS(ArrayList<ArrayList<Integer>> adj, int src,
                                  int dest, int v, int pred[], int dist[])
    {
        // a queue to maintain queue of vertices whose
        // adjacency list is to be scanned as per normal
        // BFS algorithm using LinkedList of Integer type
        LinkedList<Integer> queue = new LinkedList<Integer>();
 
        // boolean array visited[] which stores the
        // information whether ith vertex is reached
        // at least once in the Breadth first search
        boolean visited[] = new boolean[v];
 
        // initially all vertices are unvisited
        // so v[i] for all i is false
        // and as no path is yet constructed
        // dist[i] for all i set to infinity
        for (int i = 0; i < v; i++) {
            visited[i] = false;
            dist[i] = Integer.MAX_VALUE;
            pred[i] = -1;
        }
 
        // now source is first to be visited and
        // distance from source to itself should be 0
        visited[src] = true;
        dist[src] = 0;
        queue.add(src);
 
        // bfs Algorithm
        while (!queue.isEmpty()) {
            int u = queue.remove();
            for (int i = 0; i < adj.get(u).size(); i++) {
                if (visited[adj.get(u).get(i)] == false) {
                    visited[adj.get(u).get(i)] = true;
                    dist[adj.get(u).get(i)] = dist[u] + 1;
                    pred[adj.get(u).get(i)] = u;
                    queue.add(adj.get(u).get(i));
 
                    // stopping condition (when we find
                    // our destination)
                    if (adj.get(u).get(i) == dest)
                        return true;
                }
            }
        }
        return false;
    }


    @SuppressWarnings("serial")
    @Override
    protected void setup() { 	
    	
    	MyAgentData ag =new MyAgentData();
    	String agentColor = (String)getArguments()[0];
    	GridPosition agentPosition = (GridPosition)getArguments()[1];
        parentAID = (AID)getArguments()[2];
        operationTime = ((Integer) getArguments()[3]).intValue();
		this.gridAgentData = new GridAgentData(ag, agentColor, agentPosition, GridOrientation.NORTH);
		obstacles =(Set<GridPosition>)getArguments()[4];
		widthMap = ((Integer) getArguments()[5]).intValue();
    	heightMap = ((Integer) getArguments()[6]).intValue();
    	Set<GridPosition> all = new HashSet<>();
    	for(int i = 0; i < heightMap; i++)
			for(int j = 0; j < widthMap ; j++)
				all.add(new GridPosition(i, j));
		
    	int v = widthMap * heightMap;
    	ArrayList<ArrayList<Integer>> adj = new ArrayList<ArrayList<Integer>>(v);
		for(int i = 0; i < v; i++) {
			adj.add(new ArrayList<Integer>());
		}
		
		for(int i = 0; i < v; i++) {
			if(obstacles.contains(numberToPosition(i, widthMap))==false)
			for(GridPosition neighbor: neighbors(numberToPosition(i, widthMap))) {
				adj.get(i).add(positionToNumber(neighbor, widthMap));
				//System.out.println("one neighbor for " + numberToPosition(i, widthMap) + " is " + neighbor);
			}
		}
		//pentru testare am pus o pozitie de inceput si una de final sa gaseasca cel mai rapid drum
		GridPosition source = new GridPosition(0,0);
		GridPosition dest = new GridPosition(3,3);
		printShortestDistance(adj, positionToNumber(source, widthMap), positionToNumber(dest, widthMap), v, widthMap);
        if(parentAID != null) {
            addBehaviour(new WakerBehaviour(this, 0) {
                @Override
                protected void onWake() {
                    // Create the registration message as a simple INFORM message
                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                    msg.setProtocol(REGISTRATION_PROTOCOL);
                    msg.setConversationId("registration-" + myAgent.getName());
                    msg.addReceiver(parentAID);

                    myAgent.send(msg);
                }

                @Override
                public int onEnd() {
                    return super.onEnd();
                }
            });
        }

        // este initiator cand cere perceptii
        addBehaviour(new CyclicPerceptionsRequestBehaviour(this));

        // este initiator cand trimite un plan
        addBehaviour(new SendPlanBehaviour());

        // este responder cand i se trimite o actiune executata
        MessageTemplate sendPerceptionsResponderTemplate = MessageTemplate.and(MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)), MessageTemplate.MatchConversationId("ExecutedAnAction"));

        addBehaviour(new AchieveREResponder(this, sendPerceptionsResponderTemplate) {
            @Override
            @Deprecated
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                agree.setContent("executed_an_action_response");
//                System.out.println("Prepare agree:  " + request.getContent());
                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent("executed_an_action_inform");
                System.out.println("Prepare inform:  " + request.getContent());
                // reconfigurez planul in functie de ce validare a ultimei actiuni primesc / sau in functie de perceptii
                //
                planWaiting = true;
                return inform;
            }
        } );

    }

    // clasa care solicita perceptii
    private class CyclicPerceptionsRequestBehaviour extends CyclicBehaviour {
    	
        public CyclicPerceptionsRequestBehaviour(MyAgent myAgent) {
			super(myAgent);
		}

		public void action() {

            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            request.addReceiver(parentAID);
            request.setConversationId("Perceptions");
            addBehaviour(new AchieveREInitiator(myAgent, request) {
                protected void handleInform(ACLMessage inform) {
                    // Process the inform message received in response to the request
                	if(inform.getConversationId()=="Perceptions") {

							try {
								perceptions = (MyAgentPerceptions) inform.getContentObject();
							} catch (UnreadableException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.println("Am primit perceptiile: "+ perceptions.getHoles() + " of Agent: " + myAgent.getName());
						
                	}
                }
            });
            //block((long)2*operationTime);
            block();
        }
    }

    // odata executata o comanda, trimite planul nou
    private class SendPlanBehaviour extends CyclicBehaviour {
        public void action() {
            // planul asteapta sa fie trimis
            if (planWaiting) {
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                request.addReceiver(parentAID);
                request.setConversationId("SendPlan");

                // metoda de configurare a planului, momentan adaug un plan aiurea
                request.setContent("Plan al agentului " + myAgent.getName());
                planWaiting = false;
                addBehaviour(new AchieveREInitiator(myAgent, request) {
                    protected void handleInform(ACLMessage inform) {
                        // Process the inform message received in response to the request
                        System.out.println("Prima instructiune din plan a fost stocata in MessageBox: " + inform.getContent() + " of Agent: " + myAgent.getName());
                    }
                });
            }

            block();
        }
    }

    @Override
    protected void takeDown() {
        String children = "";
        for(AID childAID : childAgents)
            children += childAID.getLocalName() + "  ";
//        Log.log(this, "has the following children: ", children);
    }
}

