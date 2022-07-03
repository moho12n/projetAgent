/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testJade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hugochung
 */
public class ProspectorAgent extends DefaultAgent implements Constants{ 
    int message_count;
    ACLMessage winner;
    List<ACLMessage> losers;
    ACLMessage message;
    
    public ProspectorAgent(){
        super();
        this.message_count = 0;
        this.losers = new ArrayList<ACLMessage>();
    }

    public ACLMessage getWinner() {
        return winner;
    }

    public List<ACLMessage> getLosers() {
        return losers;
    }
    
    private void setWinner(ACLMessage m){
        this.winner = m;
    }
    @Override
    protected void setup(){
        super.setup();
        // ProspectBehaviour : oneshot puis parallelhandle puis parallelchoose
        // parallelhandle : 3 handlepropose : a la fin il reponds a chaque message
        // handle propose : met a jour le message winner et la liste des messages loosers a fur et a mesure
        this.message_count = 0;
        this.losers = new ArrayList<ACLMessage>();
        addBehaviour(new ProspectorBehaviour(this));
        
    }
    
    private boolean isWinner(String price){
        boolean isFirstTime = (this.winner == null);
        if(isFirstTime) return isFirstTime;
        
        int min = Math.min(Integer.parseInt(this.winner.getContent()), Integer.parseInt(price));
        return Integer.parseInt(price) == min;
    }
    private void doWinner(ACLMessage message){
        if(this.winner != null) addLoser(this.winner);
        setWinner(message);
    }
    private void addLoser(ACLMessage m){
        this.losers.add(m); 
    }
    
    private void doLoser(ACLMessage m){
        addLoser(m);
    }
    
    class ProspectorBehaviour extends SequentialBehaviour{
        public ProspectorBehaviour(Agent agent){
            super(agent);
        }
        
        @Override
        public void onStart(){
            System.out.println("Comportement PropectorBehaviour "+this.toString());
            this.addSubBehaviour(new OneShotBehaviour(this.getAgent()){
             @Override
             public void onStart(){
                    System.out.println("onStart:: Comportement à un coup "+ this.getAgent().getAID().getName());
                    message = new ACLMessage(ACLMessage.CFP);
                    message.addReceiver(LOLA);
                    message.addReceiver(LILY);
                    message.addReceiver(JIM);
                    message.setSender(JACK);
                    message.setContent("Acheter un CD du groupe XX, XX coexist, au meilleur prix");
                    message.setProtocol("Auction-first-round");
                    message.setConversationId("Achat");
                }
                public void action(){
                    System.out.println("action :: envoie de message de " + this.getAgent().getAID().getName());
                    send(message);
                }
            });
            this.addSubBehaviour(new ParallelHandleBehaviour(this.getAgent(), ParallelBehaviour.WHEN_ALL));
            this.addSubBehaviour(new ParallelChooseBehaviour(this.getAgent(), ParallelBehaviour.WHEN_ALL));
        }
    }
    
    class ParallelHandleBehaviour extends ParallelBehaviour {
        public ParallelHandleBehaviour(Agent agent, int type){
            super(agent, type);
        }
        
        @Override
        public void onStart(){
            super.onStart();
            System.out.println("onStart : "+this.toString());
            
            this.addSubBehaviour(new HandleProposeBehaviour(this.getAgent(), LOLA));
            this.addSubBehaviour(new HandleProposeBehaviour(this.getAgent(), LILY));
            this.addSubBehaviour(new HandleProposeBehaviour(this.getAgent(), JIM));
        }

        class MySimpleBehaviour extends SimpleBehaviour {
            private boolean finished = false;

            public MySimpleBehaviour(Agent a){
                super(a);
            }
            @Override
            public void action() {
               System.out.println("Behaviour name " + this.toString());
            }
            @Override
            public boolean done() {
                return finished;
            }
            public void finish() {
                finished = true;
            }
            @Override
            public int onEnd() {
                System.out.println("onEnd :: " + this.toString());
                return super.onEnd();
            }

        }
        
        class HandleProposeBehaviour extends MySimpleBehaviour {
            private AID receiver;
            private MessageTemplate template;
            
            public HandleProposeBehaviour(Agent a, AID receiver) {
                super(a);
                setReceiver(receiver);
            }
            
            private void setReceiver(AID agentID){
                receiver = agentID;
            }
            private AID getReceiver(){
                return receiver;
            }
            
            @Override
            public void onStart() {
                System.out.println("onStart : " + this.toString());
                MessageTemplate template_sender = MessageTemplate.MatchSender(getReceiver());
                template = MessageTemplate.and(TEMPLATE_PROPOSE, template_sender);
            }
            
            @Override
            public void action() {
                System.out.println("action : HandleProposeBehaviour " + this.toString());
                ACLMessage msgReceive = receive(template);
                if(msgReceive != null) {
                    doMessage(msgReceive);
                    finish();
                } else {
                    System.out.println("Behaviour blocked : " + this.toString());
                    block();
                }
            }
            
            private void doMessage(ACLMessage message) {
                System.out.println("Do message : " + message); 
                if(isWinner(message.getContent())) {
                    doWinner(message);
                } else {
                    doLoser(message);
                }
            }
        }
    }
    
    class ParallelChooseBehaviour extends ParallelBehaviour{
        public ParallelChooseBehaviour(Agent agent, int type){
            super(agent, type);
        }
        
        @Override
        public void onStart(){
            System.out.println("onStart : "+this.toString());
            this.addSubBehaviour(new MyReplyWinnerBehaviour(this.getAgent()));
            this.addSubBehaviour(new MyReplyLoserBehaviour(this.getAgent()));
        }
        @Override
        public int onEnd() {
            System.out.println("onEnd :: " + this.toString());
            doDelete();
            return super.onEnd();
        }
        
        class MyReplyWinnerBehaviour extends OneShotBehaviour {
            ACLMessage reply_message;
            public MyReplyWinnerBehaviour(Agent agent){
                super(agent);
            }
            @Override
            public void onStart(){
                System.out.println("onStart :: " + this.toString());
                reply_message = getWinner().createReply();
                reply_message.setContent(getWinner().getContent());
                reply_message.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
            
            @Override
            public void action() {
                send(reply_message);
            }
        }
        class MyReplyLoserBehaviour extends OneShotBehaviour {
            List<ACLMessage> reply_messages;
            public MyReplyLoserBehaviour(Agent agent){
                super(agent);
                this.reply_messages = new ArrayList<ACLMessage>();
            }
                
            @Override
            public void onStart(){
                System.out.println("onStart :: " + this.toString());
                ACLMessage reply_message;
                for(ACLMessage m : getLosers()){
                    reply_message = m.createReply();
                    reply_message.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    reply_message.setContent(m.getContent());
                    reply_messages.add(reply_message);
                }
            }

            @Override
            public void action() {
                System.out.println("action :: " + this.toString());
                for(ACLMessage m : reply_messages){
                    send(m);
                }
            }
        }
    }
}


