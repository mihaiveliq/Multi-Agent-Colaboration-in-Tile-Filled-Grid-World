package agents;

import java.util.LinkedList;
import java.util.List;

import base.Environment;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
//import platform.Log;

/**
 * The Agent.
 */
public class MyEnvironmentAgent extends Agent {
    //---------------------------------
    protected Environment env;
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

    @SuppressWarnings("serial")
    @Override
    protected void setup() {
        parentAID = (AID) getArguments()[0];
        agentValue = ((Integer) getArguments()[1]).intValue();

//        Log.log(this, "Hello. Parent is", parentAID);

        // add the behavior that sends the registration message to the parent
        if(parentAID != null) {
//            Log.log(this, "Registration sender behavior for this agent starts in 1 second");
            addBehaviour(new WakerBehaviour(this, 1000) {
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
        else
//            Log.log(this, "Registration sender behavior need not start for agent", getAID().getName());

            // add the RegistrationReceiveBehavior
            addBehaviour(new TickerBehaviour(this, TICK_PERIOD) {
                @Override
                protected void onTick() {
                    ACLMessage receivedMsg = myAgent.receive(registrationReceiptTemplate);
                    // register the agent if message received
                    if(receivedMsg != null) {
                        AID childAID = receivedMsg.getSender();
                        ((MyAgent) myAgent).addChildAgent(childAID);
                    }
                    // if number of ticks surpassed, take down the agent
                    if(getTickCount() >= MAX_TICKS) {
                        stop();

                        // TODO: comment this out once you add the other behaviors as well
                        myAgent.doDelete();
                    }
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

