package it.myideas.mymessagequeue;

import it.myideas.mymessagequeue.condition.Condition;
import it.myideas.mymessagequeue.condition.ConditionResult;
import it.myideas.mymessagequeue.listener.Listener;
import it.myideas.mymessagequeue.messages.Message;
import it.myideas.mymessagequeue.processor.Processor;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 * A {@link Queue} contains different conditions. When all the conditions are active, the queue invokes the {@link Processor},
 * and the result is sent to all the {@link Condition}.
 * Each queue runs in a separate thread.
 * @author Tommaso Doninelli
 *
 */
public class Queue implements Runnable {

    private static Logger log = Logger.getLogger(Queue.class);
    
    /** The name for the Queue */
    private String name;
    
    /** The ordered list of conditions */
    private Condition[] conditions;
    
    /** The processor converts the input of the conditions in the message to send to the listener. Can be null */
    private Processor processor; 
    
    /** The list of listeners that need to be notified by this queue */
    private ArrayList<Listener> listeners;
    
    /** FLAG: When true, the thread is running and this queue is running */
    private AtomicBoolean isQueueRunning = new AtomicBoolean(false);
    
    /** The thread for the first condition */
    private ExecutorService thread;
    
    /** ThreadPool for executing the listeners */
    private ExecutorService listenerThreapPool; 
    
    /**
     * Creates an instance of a {@link Queue} with a given configuration
     * @param hc
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Queue(HierarchicalConfiguration hc) {
        
        this.name = hc.getRootNode().getAttributes("name").get(0).getValue().toString();
        log.info("Initializing queue " + this.name);
        
        ///////////////////////        
        //LOADING CONDITIONS //
        ///////////////////////
        List<HierarchicalConfiguration> hcconditions = hc.configurationsAt("conditions/condition");
        conditions = new Condition[hcconditions.size()];
        
        for(HierarchicalConfiguration hcListener : hcconditions) {
            
            String className = hcListener.getRootNode().getAttributes("type").get(0).getValue().toString();
            int index = Integer.parseInt(hcListener.getRootNode().getAttributes("order").get(0).getValue().toString());
            
            Class[] constructorSign = new Class[]{HierarchicalConfiguration.class};
            Object[] constructorParams = new Object[]{hcListener};
            try {
                Class c = Class.forName(className);            
                Constructor ct = c.getConstructor(constructorSign);
                Condition listener = (Condition)ct.newInstance(constructorParams);                
                conditions[index] =  listener;                
            }
            catch(Exception e) {
                log.fatal("Error initializing Listener for queue " + this.name, e);
            }
        }
        
        ///////////////////////
        // LOADING PROCESSOR //
        ///////////////////////
        HierarchicalConfiguration hcProcessor = null;
        try {
            hcProcessor = hc.configurationAt("processor");
        }
        catch(Exception e){}
        
        if(hcProcessor != null ) {
            String classNameProcessor = hcProcessor.getRootNode().getAttributes("type").get(0).getValue().toString();
            Class[] constructorSignProcessor = new Class[]{HierarchicalConfiguration.class};
            Object[] constructorParamsProcessor = new Object[]{hcProcessor};
            try {
                Class c = Class.forName(classNameProcessor);            
                Constructor ct = c.getConstructor(constructorSignProcessor);
                processor = (Processor)ct.newInstance(constructorParamsProcessor);
            }
            catch(Exception e) {
                log.fatal("Error initializing Listener for queue " + this.name, e);
            }
        }
        
        ///////////////////////
        // LOADING LISTENERS //
        ///////////////////////
        List<HierarchicalConfiguration> recs = hc.configurationsAt("/listeners/listener");
        listeners = new ArrayList<>(recs.size());
        
        for(HierarchicalConfiguration hh : recs) {
            String classNameRecipient = hh.getRootNode().getAttributes("type").get(0).getValue().toString();
            Class[] constructorSignRecipient = new Class[]{HierarchicalConfiguration.class};
            Object[] constructorParamsRecipient = new Object[]{hh};
            
            try {
                Class c = Class.forName(classNameRecipient);            
                Constructor ct = c.getConstructor(constructorSignRecipient);
                Listener recipient = (Listener)ct.newInstance(constructorParamsRecipient);
                listeners.add(recipient);
            }
            catch(Exception e) {
                log.fatal("Error initializing recipient for queue " + this.name, e);
            } 
        }       
        
        if(listeners.size() == 0){
            log.error("No listener defined for the queue! - can not happen");
            return;
        }
           
    }

    /**
     * When its thread starts, the queue waits for a notification from the first {@link Condition}.
     * As soon as the listener produce an activation message, all the other conditions are verified, and if 
     * all the conditions are active, the que invokes firt the {@link Processor}, and the forks each listener on a separate thread.
     * All this process is runned sequentially; any notification produced by the first condition is ignored (actually is not checked at all) 
     * during the process. 
     * NOTICE: Once the thread of the listeners is run, the process is considered completed, and new activation may occurr before a listener has completed 
     * his job. 
     */
    @Override
    public void run() {

        log.debug("Starting " + this.name);
        thread = Executors.newSingleThreadExecutor();        // "Fork" a thread for the first condition 
        
        isQueueRunning.set(true);
        
        while(isQueueRunning.get()) {
            
            ConditionResult result = null;
            boolean allConditionsAreVerified = false;
            Message message = null;
            
            try {
                result = thread.submit(conditions[0]).get();    // Wait for the first condition. 
                
                if(!result.isConditionActive()) {   // Don't know if could neve happen
                    continue;
                }
                
                log.debug("Queue " + this.name + " activated ");
                
                // Process all the remaining conditions
                try {
                    allConditionsAreVerified = true;
                    for(int i=1; i < conditions.length; i++) {
                        
                        if(allConditionsAreVerified){
                            ConditionResult evaluation = conditions[i].call();
                            allConditionsAreVerified = evaluation.isConditionActive();
                            
                            // Each condition override the message, so only the last condition returns a value.
                            message = evaluation.getMessage();   
                        }
                        else {
                            break;  // If the last condition was false, exit
                        }                        
                    }
                }
                catch(Exception e){
                    log.error("Error evaluating a condition", e);
                    allConditionsAreVerified = false;
                }               
            }
            catch (InterruptedException | CancellationException e) {
                // The queue as been stopped! No problem here
                allConditionsAreVerified = false; // Not needed here
                isQueueRunning.set(false);
            }
            catch(Exception e) {
                log.error("Error waiting for activationByCondition; retry", e);
            }
            
            if(!allConditionsAreVerified){
                log.debug("Not all conditions are active. Return");
                continue;
            }
            
            log.trace("Sending a message to all listeners");
            
            // Process the input (Do I really need it?)
            if(processor != null){
                message = processor.process(message);
            }
            
            // Notify all the listeners
            // The listeners are processed as "launch and forget" thread
            // The listeners will be invoked using a threadpool.
            // Here we try to do not create too much threads
            listenerThreapPool = Executors.newFixedThreadPool(Math.min(2, listeners.size()));
            
            for(Listener rec : listeners) {
                try {
                    rec.setMessage(message);
                    listenerThreapPool.submit(rec);
                }
                catch(Exception e) {
                    log.error("Can't send message to listener " + rec.toString());
                }
            }
            
            listenerThreapPool.shutdown();  // Wait for all the message to be broadcasted
        }        
    }
    
    /**
     * Stop this queue and release any resources associated by this queue 
     * (this method propagate the stop to all the {@link Listener}s and to all the {@link Condition}s).
     */
    public void stopAll() {
        isQueueRunning.compareAndSet(true, false);
        if(thread != null && !thread.isShutdown() && !thread.isTerminated()) {
            thread.shutdownNow();
        }
        
        listenerThreapPool.shutdownNow();
        
        for(Condition c : conditions){
            c.stop();
        }
        
        for(Listener l : listeners){
            l.stop();
        }
    }

    public String getName() {
        return this.name;
    }
    
    
}
