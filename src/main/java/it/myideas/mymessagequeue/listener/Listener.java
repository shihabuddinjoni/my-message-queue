package it.myideas.mymessagequeue.listener;

import it.myideas.mymessagequeue.messages.Message;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * Each listener is run in a new Thread. 
 * In the {@link Runnable#run()} method the {@link Message} to be sent is the last one set using the {@link Listener#setMessage(Message)} method, or something else
 * @author Tommaso Doninelli
 *
 */
public abstract class Listener implements Runnable {

    protected HierarchicalConfiguration configuration; 
    
    public Listener(HierarchicalConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * A message *may* be passed by the queue. 
     * If a message is given, the implementation shall send a representation of this message
     * known to the reciver.
     * @param message
     */
    public abstract void setMessage(Message message);
    
}
