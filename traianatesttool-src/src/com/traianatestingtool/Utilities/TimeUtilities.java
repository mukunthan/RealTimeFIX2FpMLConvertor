/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.traianatestingtool.Utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mukunthant
 */
public class TimeUtilities {
    
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TimeUtilities.class);
    public static String ConvertUTCFormatToTraianaFormat(String sUTCTime)
    {   
       if(IsUTCTime(sUTCTime))
       {
           return CovertUTCFormatToTrainaFormatWithoutMilliSecond(sUTCTime);
       }
       
       else
       {
           LOGGER.debug("Time field is not in UTC format");
           return sUTCTime;
       }
    }
    
    
    
    private static boolean IsUTCTime(String sUTCTime)
    {
        SimpleDateFormat sdfutc = new SimpleDateFormat("yyyyMMdd'-'HH:mm:ss");
        try {
            Date TraianaTime=sdfutc.parse(sUTCTime);
            return true;
        } catch (ParseException ex) {
            return false;
        }
    }
    
 /*   public static String GetTimeZoneOffset()
    {
          TimeZone tz = Calendar.getInstance().getTimeZone();
          StringBuilder timeZoneStr = new StringBuilder();
          int iRawOffset=tz.getRawOffset();
          int iOffsethour=iRawOffset/(60 * 60 * 1000);
          int iOffsetMinute=(iRawOffset/(60*1000))-(iOffsethour*60);
          if(iOffsethour >= 0)
          {
              timeZoneStr.append("+");
          }
          if(iOffsetMinute <0)
          {
              iOffsetMinute *=-1;
          }
          String sOffsethour=String.format("%02d", iOffsethour);
          timeZoneStr.append(sOffsethour);
          timeZoneStr.append(":");
          String sformattedOffsetMinute = String.format("%02d", iOffsetMinute);
          timeZoneStr.append(sformattedOffsetMinute);
          System.out.println(timeZoneStr.toString());
          return timeZoneStr.toString();
    } */
    
    private static String CovertUTCFormatToTrainaFormatWithoutMilliSecond(String sUTCTimeWithoutms)
    {
         SimpleDateFormat sdfutc = new SimpleDateFormat("yyyyMMdd'-'HH:mm:ss");
         SimpleDateFormat sdftraiana = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
         
         try 
         {
             Date TraianaTime=sdfutc.parse(sUTCTimeWithoutms);
             return sdftraiana.format(TraianaTime);
         } catch (ParseException ex) 
         {
             LOGGER.error("Conversion error in formatign UTC to Traiana ");
             return sUTCTimeWithoutms;
         }
    }
    
   
}
