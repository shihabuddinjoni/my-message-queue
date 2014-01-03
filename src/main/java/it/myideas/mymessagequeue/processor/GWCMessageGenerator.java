package it.myideas.mymessagequeue.processor;

import it.myideas.mymessagequeue.messages.Message;
import it.myideas.mymessagequeue.messages.StringMessage;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;


/**
 * This class ignores the recived message and generates a message valid for a POST to GeoWebCache, with a reseed action
 * @author tommaso
 *
 */
public class GWCMessageGenerator extends Processor {

    private static Logger log = Logger.getLogger(GWCMessageGenerator.class);
    private StringMessage payLoad;
    
    public GWCMessageGenerator(HierarchicalConfiguration configuration) {
        super(configuration);
        payLoad = new StringMessage(configuration.getString("payload"));
        log.debug("Payload:" + payLoad);
        
    }

    @Override
    public Message doProcess(Message message) {
        return payLoad;
    }

}
