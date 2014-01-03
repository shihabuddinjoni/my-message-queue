package it.myideas.mymessagequeue.messages;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;


public class MessageMarshaller {

    
    private static MessageMarshaller instance;
    private static Logger log = Logger.getLogger(MessageMarshaller.class);
    
    private Marshaller marshaller = null;
    
    private MessageMarshaller(){
        
        try {
            JAXBContext context = JAXBContext.newInstance("");
            marshaller = context.createMarshaller();
        }
        catch (JAXBException e) {            
            e.printStackTrace();
        }
    }
    
    public static byte[] marshal(Object o) {
        
        if(instance == null)
            instance = new MessageMarshaller();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));  
        
        byte[] bytes = {};
        
        try {
            instance.marshaller.marshal(o, writer);
            bytes = baos.toByteArray();            
        }
        catch (JAXBException e) {
            log.fatal("Can't marshal message", e);
        }
        finally {
            try {writer.close();}catch (IOException e) {}
            try {baos.close();}catch (IOException e) {}
        }
        
        return bytes;
    }
    
}
