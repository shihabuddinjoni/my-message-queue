package it.myideas.mymessagequeue.messages;



public class StringMessage extends Message {

    private byte[] bytes;
    
    public StringMessage(String message){
        bytes = message.getBytes();
    }
    
    @Override
    public byte[] getMessageBytes() {
        return bytes;
    }

}
