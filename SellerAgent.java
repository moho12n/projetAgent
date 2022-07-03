/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package testJade;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import static testJade.Constants.ACCEPT;
import static testJade.Constants.CFP;
import static testJade.Constants.REJECT;
import static testJade.Constants.SENDER;
import static testJade.Constants2.ACCEPT;
import static testJade.Constants2.CFP;
import static testJade.Constants2.SENDER;
import java.lang.String;
import static testJade.Constants.JACK;
import static testJade.Constants2.JACK;

/**
 *
 * @author juans
 */
public class SellerAgent extends DefaultAgent implements Constants2{
    private String price;
    private ACLMessage message;
    private String decrement;

    public void setDecrement(String decrement) {
        this.decrement = decrement;
    }

    public String getDecrement() {
        return decrement;
    }

    @Override
    protected void setup(){
        super.setup();
        doArguments();
        addBehaviour(new SellerBehaviour(this));
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
        if (args[0] != null && args.length > 0) {
            setPrice((String)args[0]);
        } else {
            System.out.println(" no price specified for Agent " + getAID().getName());
        }
        if (args[1] != null && args.length > 0) {
            setDecrement((String)args[1]);
        } else {
            System.out.println(" no price decrement for Agent " + getAID().getName());
        }
        
       }
    


   
    class SellerBehaviour extends FSMBehaviour{

        public SellerBehaviour(Agent a) {
            super(a);
        };
        public void onstart(){
            this.registerFirstState(new SellerAgent.HandleBehaviour(this.getAgent()), "Waiting");
            this.registerState(new SellerAgent.ChooseBehaviour(this.getAgent()),"Choosing");
            this.registerState(new SellerAgent.ProposeBehaviour(this.getAgent()),"Proposing");
            this.registerLastState(new SellerAgent.WinnerBehaviour(this.getAgent()),"Wining");
            this.registerLastState(new SellerAgent.LoserBehaviour(this.getAgent()),"Losing");
            
            this.registerDefaultTransition("Propose","Waiting",new String[]{"Waiting","Propose"});
            this.registerTransition("Waiting","Wining",ACLMessage.ACCEPT_PROPOSAL);
            this.registerTransition("Waiting","Choosing",ACLMessage.CFP);
            this.registerTransition("Choosing","Losing",ACLMessage.CANCEL);
            this.registerTransition("Choosing","Proposing",ACLMessage.PROPOSE);
            
            message.setProtocol("reverse english auction");
            message.setConversationId("reflex Nixon 780");
        }
        @Override
        public int onEnd(){
            this.getAgent().doDelete();
            return super.onEnd();
        }
    }
        
    public class ChooseBehaviour extends SimpleBehaviour {
        ACLMessage cancel;
        ACLMessage propose;
        public ChooseBehaviour(Agent a) {
            super(a);
        }

        @Override
        public void action() {
            if (Integer.parseInt((message.getContent()))<Integer.parseInt(getPrice())){
                cancel.setPerformative(ACLMessage.CANCEL);
            }
            else{
                propose.setPerformative(ACLMessage.PROPOSE);
                System.out.println("Competitive agent "+this.getAgent().getAID().getName());
            }
        }

        @Override
        public boolean done() {
            System.out.println("ChooseBehaviour Done");
            return true;
        }
        
    }

    public class HandleBehaviour extends SimpleBehaviour{

        public HandleBehaviour(Agent a) {
            super(a);
        }
        MessageTemplate template;
        @Override
        public void onStart(){
            MessageTemplate templateCFP = MessageTemplate.and(CFP,SENDER);
            template = templateCFP;
        }
        @Override
        public void action() {
            ACLMessage message = receive(template);
            if(message == null){
                block();
            }
            else{
                System.out.println("HandleBehaviour"+this.getAgent().getAID().getName());
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
    public class ProposeBehaviour extends SimpleBehaviour{

        public ProposeBehaviour(Agent a) {
            super(a);
        }
        ACLMessage propose;
        @Override
        public void onStart(){
            setPrice(Integer.toString(Integer.parseInt(getPrice())-Integer.parseInt(getDecrement())));
            propose = getMessage().createReply();
            propose.addReceiver(JACK);
            propose.setContent(getPrice());
                
            }
        @Override
        public void action() {
            System.out.println("ProposeBehaviour price : "+propose.getContent()+"from "+this.getAgent().getAID().getName());
            send(propose);
        }

        @Override
        public boolean done() {
            return true;
        }
      
    }
    private class WinnerBehaviour extends MyfinalBehaviour{

        public WinnerBehaviour(Agent a) {
            super(a);
        }
        @Override
        public void doAction(){
        System.out.println("Winner "+getMessage().getContent());
        }
        
    }
    private class LoserBehaviour extends MyfinalBehaviour{
        ACLMessage cancel;
        public LoserBehaviour(Agent a) {
            super(a);
        }
        @Override
        public void doAction(){
            cancel = getMessage().createReply();
            cancel.addReceiver(JACK);
            cancel.setContent(getPrice());
            send(cancel);
            System.out.println("Loser "+getMessage().getContent());
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
}


