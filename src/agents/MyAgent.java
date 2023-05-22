package agents;

import java.util.LinkedList;
import java.util.List;

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
//import platform.Log;
import jade.proto.*;
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
    boolean planWaiting = true;
    int operationTime;

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
        operationTime = ((Integer) getArguments()[3]).intValue();
		this.gridAgentData = new GridAgentData(ag, agentColor, agentPosition, GridOrientation.NORTH);

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
        addBehaviour(new CyclicPerceptionsRequestBehaviour());

        // este initiator cand trimite un plan
        addBehaviour(new SendPlanBehaviour());

        // este responder cand i se trimite o actiune executata
        MessageTemplate sendPerceptionsResponderTemplate = MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
        sendPerceptionsResponderTemplate.MatchConversationId("ExecutedAnAction");

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
        public void action() {

            ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
            request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
            request.addReceiver(parentAID);
            request.setConversationId("Perceptions");
            addBehaviour(new AchieveREInitiator(myAgent, request) {
                protected void handleInform(ACLMessage inform) {
                    // Process the inform message received in response to the request
                    System.out.println("Am primit perceptiile: "+inform.getContent() + " of Agent: " + myAgent.getName());
                }
            });
            block((long)2*operationTime);
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

