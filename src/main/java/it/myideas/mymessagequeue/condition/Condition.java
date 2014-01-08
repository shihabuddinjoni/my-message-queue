package it.myideas.mymessagequeue.condition;

import it.myideas.mymessagequeue.Queue;
import it.myideas.mymessagequeue.listener.Listener;
import it.myideas.mymessagequeue.messages.Message;
import it.myideas.mymessagequeue.processor.Processor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

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
    
    protected String name;
    protected Logger log;
    
    /**
     * Creates a new instance of this listener. 
     * After calling this method, the manager will call the run method to start the linstener. 
     * @param confuguration
     * @param processor
     * @param recipients
     */
    public Condition(HierarchicalConfiguration configuration) {
        super(); 
        name = configuration.getString("name", Math.round(Math.random() * 10000) + "");
        log = Logger.getLogger(this.getClass());
    }
    
    @Override
    public String toString() {
        return this.getClass().getName() + "[" + this.name +"]";
    }
    
    /**
     * This method is invoked by the {@link Queue} to evaluate this {@link Condition}.
     * The call on this method may be executed by a separate thread. In any case, the {@link Condition} should not 
     * start any computation untill this method is invoked at least the first time, since the start of {@link Queue} can be delayed 
     * be the user. 
     * @see {@link Callable#call()}
     */
    @Override
    public abstract ConditionResult call() throws Exception;

    /**
     * This {@link Method} is invoked by a {@link Condition} to notify this {@link Condition} that nobody will call the {@link Condition#call()} method from now on.
     * This method is invoked after any running thread waiting on {@link Condition#call()} has been stopped.
     * The implementation shall close and release any resource here, untill a new invocation of {@link Condition#call()} is done. 
     */
    public abstract void stop();    
}
