/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.codec;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;


/**
 * Provides the FIX codecs to MINA.
 */
public class FIXProtocolCodecFactory extends DemuxingProtocolCodecFactory {
    public static final String FILTER_NAME = "FIXCodec";
    
    public FIXProtocolCodecFactory() {
        super.register(FIXMessageDecoder.class);
        super.register(FIXMessageEncoder.class);
    }
}
