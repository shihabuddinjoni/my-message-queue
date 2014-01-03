package it.myideas.mymessagequeue.condition;

import it.myideas.mymessagequeue.listener.Listener;
import it.myideas.mymessagequeue.messages.Message;
import it.myideas.mymessagequeue.processor.Processor;

import java.util.concurrent.Callable;

import org.apache.commons.configuration.HierarchicalConfiguration;

/**
 * If all {@link Condition}s in a queue are evaluated as true, the queue is triggered.
 * The logic of evaluation of the condition must be implemented in the {@link Callable#call()} method.
 * This condition can trigger all the queue (if is the first in the queue's definition).
 * 
 *  The {@link Condition} shall release all the resource in the {@link Callable#call()} method. 
 *  The {@link Callable#call()} method must return an instance of {@link ConditionResult}, to signal to 
 *  the queue if the condition has been evaluated TRUE or FALSE; the {@link ConditionResult} may also have a {@link Message},
 *  that will be passed to the {@link Processor} and to any {@link Listener} of the queue.
 * 
 * @author Tommaso Doninelli
 *
 */
public abstract class Condition implements Callable<ConditionResult> {
    
    
    /**
     * Creates a new instance of this listener. 
     * After calling this method, the manager will call the run method to start the linstener. 
     * @param confuguration
     * @param processor
     * @param recipients
     */
    public Condition(HierarchicalConfiguration configuration) {
        super(); 
    }
    
}
