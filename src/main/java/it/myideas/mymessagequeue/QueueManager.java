package it.myideas.mymessagequeue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

    private static QueueManager me() {
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
    public static boolean addQueue(HierarchicalConfiguration configuration, boolean autoStart) {
        
        String queueName = configuration.getRootNode().getAttributes("name").get(0).getValue().toString();
        if(me().queues.containsKey(queueName)){
            log.error(String.format("The queue %s already exists", queueName));
            return false;
        }

        Queue queue = null;
        
        try {
            queue = new Queue(configuration);            
        }
        catch (Exception e){
            log.error("Erro creating a queue", e);
            return false;
        }
        
        me().queues.put(queueName, queue);
        
        if(autoStart){
            return start(queueName);
        }
        
        return true;
    }
    
    public static boolean removeQueue(String name) {
        if(isQueueRunning(name)){
            stop(name);
        }
        
        me().queues.remove(name);
        return true;
    }
    
    public static boolean isQueueRunning(String name) {
        ExecutorService thread = me().queueThreads.get(name);
        
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
    public static boolean start(String name) {
        
        if(isQueueRunning(name)){
            log.info("Queue " + name + " is running.");
            return false;
        }
        
        Queue queue = me().queues.get(name);
        
        if(queue == null){
            return false;
        }
        
        ExecutorService thread = Executors.newSingleThreadExecutor();
        me().queueThreads.put(name, thread);
        
        thread.submit(queue);
        
        return true;
    }
    
    public static boolean startAll() {
        
        boolean allDeployed = true;
        Set<Entry<String, Queue>> set = me().queues.entrySet();
        for(Entry<String, Queue> entry : set){
            if(!start(entry.getKey())){
                allDeployed = false;
            }
        }
        return allDeployed;
    }
    
    /**
     * 
     * @return TRUE if the queue is NOT running
     */
    public static boolean stop(String name) {
        if(!isQueueRunning(name)) {
            log.info("Queue " + name + " was already stopped");
            return true;
        }
        
        log.info("Stopping queue " + name);
        
        me().queueThreads.get(name).shutdownNow();  // Stop the thread
        me().queues.get(name).stopAll();            // Stop the queue and release resources
        return true;    
    }
    
    public static boolean stopAll() {
        
        boolean allStopped = true;
        Set<Entry<String, Queue>> set = me().queues.entrySet();
        for(Entry<String, Queue> entry : set){
            if(!stop(entry.getKey())){
                allStopped = false;
            }
        }
        return allStopped;
    }

    /**
     * Stop and remove all the queues
     * @return
     */
    public static boolean removeAll() {

        stopAll();
        
        me().queues.clear();
        me().queueThreads.clear();
        
        return true;
    }
    
    public static Collection<Queue> listQueues() {

        return me.queues.values();
    }
    
    
}
