package it.myideas.mymessagequeue.listener;

import it.myideas.mymessagequeue.Queue;
import it.myideas.mymessagequeue.condition.Condition;
import it.myideas.mymessagequeue.messages.Message;

import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 * A {@link Listener} sends a message (a representation of a {@link Message} well known to a reciver, or something else) to someone or, 
 * more generally, performs some operations when all the {@link Condition}s of the {@link Queue} where this {@link List} belongs to are satisfied. 
 * Each "run" of the {@link Listener}, or each "send the message" operation,  is executed on a separate thread, using always the same instance of this class.
 * The implementation should not keep any resource opened, all the resources should be released within the {@link Listener#run()} method. 
 * 
 * The {@link Listener#setMessage(Message)} method is used by the queue to set the {@link Message} to send. 
 * The {@link Listener#run()} method should always use the last recived {@link Message}.
 * NOTE: The {@link Listener#setMessage(Message)} method may never be invoked, if the {@link Condition}s in the queue doesn't produce a message.
 * Also, it's not mandatory to send a representation of the recived {@link Message}, it's up to the implementation what to send and how.
 * 
 * @author Tommaso Doninelli
 *
 */
public abstract class Listener implements Runnable {

    protected HierarchicalConfiguration configuration; 
    protected String name;
    protected Logger log;
    
    public Listener(HierarchicalConfiguration configuration) {
        this.configuration = configuration;
        name = configuration.getString("name", Math.round(Math.random() * 10000) + "");
        log = Logger.getLogger(this.getClass());
    }

   public abstract void run(); 
    
    /**
     * A message *may* be passed by the queue. 
     * If a message is given, the implementation shall send a representation of this message
     * known to the reciver.
     * @param message
     */
    public abstract void setMessage(Message message);
    
    @Override
    public String toString() {
        return this.getClass().getName() + "[" + this.name +"]";
    }

    /**
     * This method is called by the {@link Queue} to notify to this instance that the queue has been stopped, and this instance won't be run
     * anymore for "some time", untill the queue is not started anymore. 
     * The implementation should close all resources handlers here. Remember that this instance could be used again, and that the only method that is invoked 
     * by the {@link Queue} is {@link Listener#run()}.
     */
    public abstract void stop();
    
}
