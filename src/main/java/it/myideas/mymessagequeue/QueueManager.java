package it.myideas.mymessagequeue;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 * Singleton class for managing *all* the queues handled by a program.
 * 
 * @author Tommaso Doninelli
 *
 */
public class QueueManager {
    
    private static QueueManager me = null;    
    private static Logger log = Logger.getLogger(QueueManager.class);
    
    private HashMap<String, Queue> queues;
    private HashMap<String, ExecutorService> queueThreads;
    
    
    private QueueManager() {
        queues = new HashMap<>();
        queueThreads = new HashMap<>();
    }

    private QueueManager me() {
        if(me == null){
            me = new QueueManager();
        }
        return me;
    }
    
    /**
     * Add a new queue specified by a given configuration
     * @param configuration
     * @param autoStart
     * @return false if the queue specified by the configuration already exists
     */
    public boolean addQueue(HierarchicalConfiguration configuration, boolean autoStart) {
        
        String queueName = configuration.getRootNode().getAttribute(0).getValue().toString();
        if(queues.containsKey(queueName)){
            log.error(String.format("The queue %s already exists", queueName));
            return false;
        }
        
        log.info(String.format("Creating queue %s", queueName));
        Queue queue = null;
        
        try {
            queue = new Queue(configuration);            
        }
        catch (Exception e){
            log.error(e);
            return false;
        }
        
        queues.put(queueName, queue);
        
        if(autoStart){
            return start(queueName);
        }
        
        return true;
    }
    
    public boolean isQueueRunning(String name) {
        ExecutorService thread = queueThreads.get(name);
        
        if(thread == null || thread.isShutdown() || thread.isTerminated()) {
            return false;
        }
        return true;
    }
    
    /**
     * Starts a queue if it is not running, otherwise returns false
     * @param name
     * @return false if the queue does not exists or is already running
     */
    public boolean start(String name) {
        
        if(isQueueRunning(name)){
            return false;
        }
        
        Queue queue = queues.get(name);
        
        if(queue == null){
            return false;
        }
        
        ExecutorService thread = Executors.newSingleThreadExecutor();
        queueThreads.put(name, thread);
        thread.submit(queue);
        
        log.info("Starting queue " + queue.getName());
        
        return true;
    }
    
    public void startAll() {
        Set<Entry<String, Queue>> set = me().queues.entrySet();
        for(Entry<String, Queue> entry : set){
            start(entry.getKey());
        }
    }
    
    /**
     * 
     * @return TRUE if the queue is NOT running
     */
    public boolean stop(String name) {
        if(!isQueueRunning(name)) {
            log.info("Queue " + name + " was already stopped");
            return true;
        }
        
        log.info("Stopping queue " + name);
        queueThreads.get(name).shutdownNow();
        return true;
    }
    
    public void stopAll() {
        Set<Entry<String, Queue>> set = me().queues.entrySet();
        for(Entry<String, Queue> entry : set){
            stop(entry.getKey());
        }
    }
    
    
}
