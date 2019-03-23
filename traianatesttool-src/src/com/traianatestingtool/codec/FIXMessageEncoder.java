/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.codec;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecException;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;
import org.quickfixj.CharsetSupport;

//import quickfix.Message;  TEST

/**
 * Encodes a Message object or message string as a byte array to be
 * transmitted on MINA connection.
 */
public class FIXMessageEncoder implements MessageEncoder {

    private static final Set<Class<?>> TYPES;
    private final String charsetEncoding;
    
    static {
        Set<Class<?>> types = new HashSet<Class<?>>();
       // types.add(Message.class); //TEST
        types.add(String.class);
        TYPES = Collections.unmodifiableSet(types);
    }

    public FIXMessageEncoder() {
        charsetEncoding = CharsetSupport.getCharset();
    }
    
    public Set<Class<?>> getMessageTypes() {
        return TYPES;
    }

    public void encode(IoSession session, Object message, ProtocolEncoderOutput out)
            throws ProtocolCodecException {
        String fixMessageString=null; //TEST
        if (message instanceof String) {
            fixMessageString = (String) message;
        }else {
           /* try {
                fixMessageString = ((Message) message).toString();
            } catch (ClassCastException e) {
                throw new ProtocolCodecException("Invalid FIX message object type: "
                        + message.getClass(), e);
            }*/
        } 

        ByteBuffer buffer = ByteBuffer.allocate(fixMessageString.length());
        try {
            buffer.put(fixMessageString.getBytes(charsetEncoding));
        } catch (UnsupportedEncodingException e) {
            throw new ProtocolCodecException(e);
        }
        buffer.flip();
        out.write(buffer);
    }
}
