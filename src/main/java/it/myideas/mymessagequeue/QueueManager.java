package it.myideas.mymessagequeue;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

/**
 * Use this EJB to crete, start and stop the queues. 
 * Usin
 * @author Tommaso Doninelli
 *
 */
@Singleton
@LocalBean
public class QueueManager {

    private static Logger log = Logger.getLogger(QueueManager.class);
    private static Hashtable<String, ArrayList<Queue>> map_queues;
    
    
    @PostConstruct
    private void constructor() {
        map_queues = new Hashtable<>();
    }
    
    /**
     * Stop all running queues.
     * This method is invoked internally on @PreDestroy. Subsequent calls on this method are idempotent
     * 
     */
    @PreDestroy
    private void stopAllQueues(){
        
        log.info("Stopping all queue listeners");
        
        Enumeration<ArrayList<Queue>> equeues = map_queues.elements();
        
        while(equeues.hasMoreElements()){
            ArrayList<Queue> queues = equeues.nextElement();
            
            for(Queue queue : queues) {
//                queue.killAllListener();
            }            
        }       
    }
    
    /**
     * Initialize all the queues found in the specified {@link HierarchicalConfiguration}.
     * 
     * @param name The name that will be used to identify all the queues that belongs to this node
     * @param hc The configuration for the queues
     */
    public void initializeQueues(String name, HierarchicalConfiguration hc) {
        log.info("Enabling MessageQueues for " + name);        
        ArrayList<Queue> queues = new ArrayList<Queue>();        
        
        Queue queue = new Queue(hc);
        queues.add(queue);
        
        map_queues.put(name, queues);     
        
        startQueues(name);
    }
    
    public void startQueues(String name){
        List<Queue> queues = map_queues.get(name);
        for(Queue queue : queues) {
//            queue.start();
        }
    }
    
}
