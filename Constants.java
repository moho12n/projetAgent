/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package testJade;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 *
 * @author juans
 */
interface Constants {
    AID JACK = new AID("jack",AID.ISLOCALNAME);
    AID JIM = new AID("jim",AID.ISLOCALNAME);
    AID LOLA = new AID("lola",AID.ISLOCALNAME);
    AID LILY = new AID("lily",AID.ISLOCALNAME);
    
    MessageTemplate CFP = MessageTemplate.MatchPerformative(ACLMessage.CFP);
    MessageTemplate PROPOSE = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
    MessageTemplate ACCEPT = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
    MessageTemplate REJECT = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
    MessageTemplate TEMPLATE_PROPOSE = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
    MessageTemplate SENDER = MessageTemplate.MatchSender(JACK);
}
