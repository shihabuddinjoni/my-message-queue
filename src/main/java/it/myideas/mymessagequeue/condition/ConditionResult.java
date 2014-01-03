package it.myideas.mymessagequeue.condition;

import it.myideas.mymessagequeue.listener.Listener;
import it.myideas.mymessagequeue.messages.Message;

import java.util.concurrent.locks.Condition;

/**
 * Contains the result of the elaboration of a {@link Condition}.
 * The {@link Condition} may have or not a {@link Message} that could be used as input for the {@link Listener}.
 * 
 * @author Tommaso Doninelli
 *
 */
public class ConditionResult {
    
    private boolean isConditionActive = false;
    private Message message = null;

    /**
     * Default constructor. The {@link Message} is left to null
     * @param isConditionActive
     */
    public ConditionResult(boolean isConditionActive) {
        this.isConditionActive = isConditionActive;
    }
    
    /**
     * Creates a postive resutl ( {@link ConditionResult#isConditionActive} is set to TRUE)
     * witha  {@link Message}
     * @param message
     */
    public ConditionResult(Message message) {
        this.isConditionActive = true;
        this.message = message;
    }
    
    /**
     * @return true if the condition is verified
     */
    public boolean isConditionActive() {
        return isConditionActive;
    }

    /**
     * @return the {@link Message} from the result of the {@link Condition} that instantiate this class. May be null. 
     */
    public Message getMessage() {
        return this.message;
    };
}
