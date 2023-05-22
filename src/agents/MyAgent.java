package agents;

import java.util.LinkedList;
import java.util.List;

import base.Environment;
import gridworld.*;
import gridworld.AbstractGridEnvironment.GridAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import platform.Log;
import jade.proto.AchieveREInitiator;
import my.MyAgentData;

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

    @SuppressWarnings("serial")
    @Override
    protected void setup() {
    	MyAgentData ag =new MyAgentData();
    	String agentColor = (String)getArguments()[0];
    	GridPosition agentPosition = (GridPosition)getArguments()[1];
        parentAID = (AID)getArguments()[2];
		this.gridAgentData = new GridAgentData(ag, agentColor, agentPosition, GridOrientation.NORTH);

//        Log.log(this, "Hello. Parent is", parentAID);

        // add the behavior that sends the registration message to the parent
        if(parentAID != null) {
//            Log.log(this, "Registration sender behavior for this agent starts in 1 second");
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
//                    Log.logIP(myAgent, REGISTRATION_PROTOCOL, parentAID, "message sent");
                    return super.onEnd();
                }
            });
        }

        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        request.addReceiver(parentAID);
//        request.setContent("Request message content"); // Set the request content
        request.setConversationId("Perceptions");

//        addBehaviour(new OneShotBehaviour() {
//            public void action() {
//                send(request); // Send the request message
//            }
//        });

        addBehaviour(new AchieveREInitiator(this, request) {
            protected void handleInform(ACLMessage inform) {
                System.out.println("Am primit perceptiile: "+inform.getContent());
            }
        });


    }

    @Override
    protected void takeDown() {
        String children = "";
        for(AID childAID : childAgents)
            children += childAID.getLocalName() + "  ";
//        Log.log(this, "has the following children: ", children);
    }
}

