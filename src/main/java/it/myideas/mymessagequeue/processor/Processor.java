package it.myideas.mymessagequeue.processor;

import it.myideas.mymessagequeue.messages.Message;

import org.apache.commons.configuration.HierarchicalConfiguration;


public abstract class Processor {

    protected HierarchicalConfiguration configuration; 
    
    public Processor(HierarchicalConfiguration configuration) {
        this.configuration = configuration;
    }
    
    protected abstract Message doProcess(Message message);
    
    public Message process(Message message){
        // Qua ci registro che ho processato un messaggio, a che ora, e cosi via
        return doProcess(message);
    }

}
