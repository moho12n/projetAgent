/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testJade;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author juans
 */
public class FSMArgumentAgent extends DefaultAgent implements Constants{
    private String price;
    private ACLMessage message;
    
    
    protected void setup(){
        super.setup();
        doArguments();
        FSMPerformativeBehaviour fsm = new FSMPerformativeBehaviour(this);
        addBehaviour(fsm);
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setMessage(ACLMessage message) {
        this.message = message;
    }

    public String getPrice() {
        return price;
    }

    public ACLMessage getMessage() {
        return message;
    }

    private void doArguments() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            setPrice((String)args[0]);
        } else {
            System.out.println(" no price specified for Agent " + getAID().getName());
        }
       }

   
    class FSMPerformativeBehaviour extends FSMBehaviour{

        private FSMPerformativeBehaviour(Agent a) {
            super(a);
        }
        public void onstart(){
            this.registerFirstState(new HandleMessageBehaviour(this.getAgent()), "Waiting");
            this.registerState(new SendMessageBehaviour(this.getAgent()),"Propose");
            this.registerLastState(new WinnerBehaviour(this.getAgent()),"Wining");
            this.registerLastState(new LoserBehaviour(this.getAgent()),"Losing");
            
            this.registerDefaultTransition("Propose","Waiting");
            this.registerTransition("Waiting","Wining",ACLMessage.ACCEPT_PROPOSAL);
            this.registerTransition("Waiting","Losing",ACLMessage.REJECT_PROPOSAL);
            this.registerTransition("Waiting","Propose",ACLMessage.CFP);
            
        }
        @Override
        public int onEnd(){
            this.getAgent().doDelete();
            return super.onEnd();
        }
        
        
    }

    private class HandleMessageBehaviour extends SimpleBehaviour{
        public HandleMessageBehaviour(Agent a) {
            super(a);
        }
        MessageTemplate template;
        public void onStart(){
            MessageTemplate templateAccept = MessageTemplate.and(ACCEPT,SENDER);
            MessageTemplate templateCFP = MessageTemplate.and(CFP,SENDER);
            MessageTemplate templateReject = MessageTemplate.and(REJECT,SENDER);
            template = MessageTemplate.or(MessageTemplate.or(templateAccept,templateReject),templateCFP);
        }
        @Override
        public void action() {
            ACLMessage message = receive(template);
            if(message == null){
                block();
            }
            else{
                System.out.println("HandleMessageBehaviour"+this.getAgent().getAID().getName());
                doMessage();
            }
        }
        public void doMessage(){
            System.out.println("Message "+message);
            setMessage(message);
        }

        @Override
        public boolean done() {
            return getMessage()!=null;
        }
    }
    private class WinnerBehaviour extends MyfinalBehaviour{

        public WinnerBehaviour(Agent a) {
            super(a);
        }
        public void doAction(){
        System.out.println("Winer");
        }
        
    }
    private class LoserBehaviour extends MyfinalBehaviour{

        public LoserBehaviour(Agent a) {
            super(a);
        }
        public void doAction(){
            System.out.println("Loser");
        }
        
    }

    private static class MyfinalBehaviour extends OneShotBehaviour{

        public MyfinalBehaviour(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            doAction();
        }

        public void doAction(){} ;
    }

    private class SendMessageBehaviour extends OneShotBehaviour{
        ACLMessage propose;
        public SendMessageBehaviour(Agent a) {
            super(a);
        }
        @Override
            public void onStart(){
                propose = getMessage().createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.addReceiver(JACK);
                
                propose.setContent(getPrice());
                
                System.out.println("onStart"+this.toString());
            };
        @Override
        public void action() {
            send(propose);
            System.out.println("propose message "+ propose);
        }
        @Override
        public int onEnd(){
            System.out.println("propose message send from" + this.getAgent().getAID().getName());
            return super.onEnd();
        };
    }
    
}
