package agents;

import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import base.Environment;
import classes.Hole;
import classes.TileStack;
import gridworld.*;
import gridworld.AbstractGridEnvironment.GridAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import my.MyAgentData;
import my.MyEnvironment;
import classes.ConsoleColors;
import my.MyEnvironment.MyAgentPerceptions;
//import platform.Log;

/**
 * The Agent.
 */
public class MyEnvironmentAgent extends Agent {
    //---------------------------------
    protected MyEnvironment env;
    //---------------------------------
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

    /**
     * @param childAID
     *            the ID of the child to add.
     */
    public void addChildAgent(AID childAID) {
        childAgents.add(childAID);
    }

    /**
     * @return the list of IDs of child agents.
     */
    public List<AID> getChildAgents() {
        return childAgents;
    }

    @SuppressWarnings({ "serial", "unchecked"})
    @Override
    protected void setup() {
    	//holesPositions, obstacles, tileStackPositions
    	Map<GridPosition, Hole> holesPositions=(Map<GridPosition, Hole>)getArguments()[0];
    	Set<GridPosition> obstacles =(Set<GridPosition>)getArguments()[1];
    	Map<GridPosition,  LinkedList<TileStack>> tileStackPositions= (Map<GridPosition,  LinkedList<TileStack>>)getArguments()[2];
    	
    	//perceptiile initiale -> pot fi create si direct cand trimite mediul perceptiile agentilor
    	//la fiecare interogare a agentilor pentru perceptii se vor crea perceptii noi cu variabilele curente din env
    	MyAgentPerceptions perceptions = new MyAgentPerceptions(holesPositions, obstacles, tileStackPositions);
    	
    	int widthMap = ((Integer) getArguments()[3]).intValue();
    	int heightMap = ((Integer) getArguments()[4]).intValue();
        int t = ((Integer) getArguments()[5]).intValue();
        int T = ((Integer) getArguments()[6]).intValue();
        Map<String, GridPosition> agentConfig = (Map<String, GridPosition>)getArguments()[7];
    	env = new MyEnvironment();
    	Set<GridPosition> all = new HashSet<>();
    	for(int i = 0; i < widthMap; i++)
			for(int j = 0; j < heightMap ; j++)
				all.add(new GridPosition(i, j));
    	env.initialize(all, holesPositions, obstacles, tileStackPositions);
        for (Map.Entry<String,GridPosition> agent : agentConfig.entrySet()) {
            // aici sunt culoarea si pozitia agentului
        	MyAgentData ag =new MyAgentData();
        	String agentColor = agent.getKey();
        	GridPosition agentPosition = agent.getValue();
    		GridAgentData gridAgentData = new GridAgentData(ag, agentColor, agentPosition, GridOrientation.NORTH);
        	this.env.addAgent(gridAgentData);
        }
    	env.printToString();
    	//System.out.println(tileStackPositions);


//        ParallelBehaviour pb = new ParallelBehaviour(this, ParallelBehaviour.WHEN_ALL);

//        pb.addSubBehaviour(new WakerBehaviour(this, T) {
        addBehaviour(new WakerBehaviour(this, T) {
            protected void onWake() {
                System.out.println("Waker behaviour has completed after 5 seconds.");
                // de trimis tuturor mesaje de terminare
                myAgent.doDelete();
            }
        });

//        pb.addSubBehaviour(new TickerBehaviour(this, t) {
        addBehaviour(new TickerBehaviour(this, t) {
            protected void onTick() {
                env.printToString();
            }
        });

//        pb.addSubBehaviour(new TickerBehaviour(this, t) {
        addBehaviour(new TickerBehaviour(this, t) {
            protected void onTick() {
                ACLMessage receivedMsg = myAgent.receive(registrationReceiptTemplate);

                // register the agent if message received and is still
                if(receivedMsg != null) {
                    AID childAID = receivedMsg.getSender();

                    ((MyEnvironmentAgent) myAgent).addChildAgent(childAID);
                    ((MyEnvironmentAgent) myAgent).env.addMessage("Agent " + childAID.toString() + " connected.");
//                    System.out.println("Agent " + childAID.toString() + " connected.");
//                    System.out.println(((MyEnvironmentAgent) myAgent).getChildAgents().toArray().length);
                }
            }
        });

        addBehaviour(new TickerBehaviour(this, t) {
            protected void onTick() {
                // register the agent if message received and is still
                if(!((MyEnvironmentAgent) myAgent).env.getMessageBox().isEmpty())
                    System.out.println(((MyEnvironmentAgent) myAgent).env.getMessageBox().remove());
            }
        });

        // de customizat, eventual cu Publisher Subscriber
//        pb.addSubBehaviour(new AchieveREInitiator(this, null) {
        addBehaviour(new AchieveREInitiator(this, null) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("Received response message: " + inform.getContent());
            }

            protected void handleFailure(ACLMessage failure) {
                System.out.println("Received failure message: " + failure.getContent());
            }
        });

//        addBehaviour(pb);

        // add the behavior that sends the registration message to the parent
//        if(parentAID != null) {
////            Log.log(this, "Registration sender behavior for this agent starts in 1 second");
//            addBehaviour(new WakerBehaviour(this, 1000) {
//                @Override
//                protected void onWake() {
//                    // Create the registration message as a simple INFORM message
//                    ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//                    msg.setProtocol(REGISTRATION_PROTOCOL);
//                    msg.setConversationId("registration-" + myAgent.getName());
//                    msg.addReceiver(parentAID);
//
//                    myAgent.send(msg);
//                }
//
//                @Override
//                public int onEnd() {
////                    Log.logIP(myAgent, REGISTRATION_PROTOCOL, parentAID, "message sent");
//                    return super.onEnd();
//                }
//            });
//        }
//        else
////            Log.log(this, "Registration sender behavior need not start for agent", getAID().getName());
//
//            // add the RegistrationReceiveBehavior
//            addBehaviour(new TickerBehaviour(this, TICK_PERIOD) {
//                @Override
//                protected void onTick() {
//                    ACLMessage receivedMsg = myAgent.receive(registrationReceiptTemplate);
//                    // register the agent if message received
//                    if(receivedMsg != null) {
//                        AID childAID = receivedMsg.getSender();
//                        ((MyAgent) myAgent).addChildAgent(childAID);
//                    }
//                    // if number of ticks surpassed, take down the agent
//                    if(getTickCount() >= MAX_TICKS) {
//                        stop();
//
//                        // TODO: comment this out once you add the other behaviors as well
//                        //myAgent.doDelete();
//                    }
//                }
//            });
    }

    @Override
    protected void takeDown() {
//        String children = "";
//        for(AID childAID : childAgents)
//            children += childAID.getLocalName() + "  ";
//        Log.log(this, "has the following children: ", children);
        // de afisat punctajele
    }
}

