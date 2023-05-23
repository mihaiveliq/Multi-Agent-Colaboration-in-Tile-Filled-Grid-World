package agents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.sql.SQLOutput;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import base.Environment;
import classes.*;
import gridworld.*;
import gridworld.AbstractGridEnvironment.GridAgentData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.*;
import my.MyAgentData;
import my.MyEnvironment;
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
        //MyAgentPerceptions perceptions = new MyAgentPerceptions(holesPositions, obstacles, tileStackPositions);
    	
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

        addBehaviour(new WakerBehaviour(this, T) {
            protected void onWake() {
                System.out.println("Jocul s-a terminat.");
                myAgent.doDelete();
            }
        });

        addBehaviour(new TickerBehaviour(this, t) {
            protected void onTick() {
                env.printToString();
            }
        });

        addBehaviour(new TickerBehaviour(this, t) {
            protected void onTick() {
                ACLMessage receivedMsg = myAgent.receive(registrationReceiptTemplate);

                if(receivedMsg != null) {
                    AID childAID = receivedMsg.getSender();

                    ((MyEnvironmentAgent) myAgent).addChildAgent(childAID);
                    ((MyEnvironmentAgent) myAgent).env.addMessage(new Message("Agent " + childAID.toString() + " connected.", childAID));
                }
            }
        });

        // receptor atunci cand i se cere starea mediului
        MessageTemplate sendPerceptionsResponderTemplate = MessageTemplate.and(MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)),MessageTemplate.MatchConversationId("Perceptions"));

        addBehaviour(new AchieveREResponder(this, sendPerceptionsResponderTemplate) {
            @Override
            @Deprecated
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                agree.setContent("perceptii_response");
                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
                ACLMessage inform = request.createReply();
                inform.setConversationId("Perceptions");
                inform.setPerformative(ACLMessage.INFORM);
                MyAgentPerceptions perceptions = new MyAgentPerceptions(holesPositions, obstacles, tileStackPositions);
                
                try {
					inform.setContentObject((Serializable) perceptions);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                return inform;
            }
        } );

        // receptor atunci cand primeste planul
        MessageTemplate sendPlanProcessedResponderTemplate = MessageTemplate.and(MessageTemplate.and(
                MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST),
                MessageTemplate.MatchPerformative(ACLMessage.REQUEST)), MessageTemplate.MatchConversationId("SendPlan"));

        addBehaviour(new AchieveREResponder(this, sendPlanProcessedResponderTemplate) {
            @Override
            @Deprecated
            protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
                ACLMessage agree = request.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                agree.setContent("plan_procesat_response");
                return agree;
            }

            protected ACLMessage prepareResultNotification(ACLMessage request, ACLMessage response) throws FailureException {
                ACLMessage inform = request.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                inform.setContent("plan_procesat_inform");
                ((MyEnvironmentAgent) myAgent).env.addMessage(new Message(request.getContent(), request.getSender()));
                return inform;
            }
        } );

        // initiator atunci cand a executat o comanda din messagebox
        addBehaviour(new ExecuteCommandBehaviour(this, t));
    }

    // odata la niste secunde executa o comanda din messagebox si trimite feedback agentului
    private class ExecuteCommandBehaviour extends TickerBehaviour {

        public ExecuteCommandBehaviour(Agent agent, long period) {
            super(agent, period);
        }

        protected void onTick() {
            Message message;
            if (!((MyEnvironmentAgent) myAgent).env.getMessageBox().isEmpty()) {
                message = (((MyEnvironmentAgent) myAgent).env.getMessageBox().remove());

                // execute command, va trimite flag-ul care indica daca s a executat actiunea

                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
                request.addReceiver(message.sender);
                request.setConversationId("ExecutedAnAction");
                request.setContent(message.message);
                addBehaviour(new AchieveREInitiator(myAgent, request) {
                    protected void handleInform(ACLMessage inform) {
                        // Process the inform message received in response to the request
                        System.out.println(inform.getContent());
                    }
                });
            }
        }
    }

    @Override
    protected void takeDown() {
    }
}

