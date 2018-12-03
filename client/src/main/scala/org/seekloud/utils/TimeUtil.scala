package com.neo.sk.utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.github.nscala_time.time.Imports.{DateTime, DateTimeFormat}
import org.joda.time.format.DateTimeFormatter


/**
  * Created by hongruying on 2018/2/19
  */
object TimeUtil {

  val fmt_yyyyMMddHHmm = DateTimeFormat.forPattern("yyyyMMddHHmm")
  val fmt_yyyyMMddHHmmss = DateTimeFormat.forPattern("yyyyMMddHHmmss")


  def format_yyyyMMddHHmm(millis: Long) = format(millis,fmt_yyyyMMddHHmm)
  def format_yyyyMMddHHmmss(millis: Long) = format(millis,fmt_yyyyMMddHHmmss)
  val fmt_yyyyMMddHH = DateTimeFormat.forPattern("yyyyMMddHH")
  val fmt_yyyyMMdd = DateTimeFormat.forPattern("yyyyMMdd")
  val fmt_yyyyMM = DateTimeFormat.forPattern("yyyyMM")
  val fmt_yyyyWW = DateTimeFormat.forPattern("yyyyww")
  def format_yyyyWW(millis:Long)=format(millis,fmt_yyyyWW)
  def format_yyyyMM(millis:Long)=format(millis,fmt_yyyyMM)
  def format_yyyyMMddHH(millis: Long) = format(millis,fmt_yyyyMMddHH)
  def format_yyyyMMdd(millis: Long) = format(millis,fmt_yyyyMMdd)


  private def format(millis: Long, fmt: DateTimeFormatter):String = {
    val d = new DateTime(millis)
    d.toString(fmt)
  }

  import com.github.nscala_time.time.Imports._

  def formatMillisWithInterval(millis:Long,interval:String)={
    interval match {
      case "yyyyMMddHH24MI" => format_yyyyMMddHHmm(millis)
      case "yyyyMMddHH5M" => getLatest5Minute(format_yyyyMMddHHmm(millis))
      case "yyyyMMddHH24" => format_yyyyMMddHH(millis)
      case "yyyyMMdd" => format_yyyyMMdd(millis)
      case "yyyyww"=> format_yyyyWW(millis)
      case "yyyyMM" => format_yyyyMM(millis)
    }
  }



  def getCurHourEnd(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.clear(Calendar.MINUTE)
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.add(Calendar.HOUR,1)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis - 1
  }

  def getCurHourStart(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.clear(Calendar.MINUTE)
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.add(Calendar.MINUTE,0)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis
  }
  def getCurMinuteEnd(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.add(Calendar.MINUTE,1)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis - 1
  }

  def getCurMinuteStart(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.add(Calendar.MINUTE,0)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis
  }

  def getCurFiveEnd(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    val tmp = c.get(Calendar.MINUTE)
    val sec = c.get(Calendar.SECOND)
    val mil = c.get(Calendar.MILLISECOND)
    val x = if(tmp%5 > 0) 1 else if(sec>0 || mil>0) 1 else 0
    val t = (tmp/5 + x)*5
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.set(Calendar.MINUTE,0)
    c.add(Calendar.MINUTE,t)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis - 1
  }

  def getCurFiveStart(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    val tmp = c.get(Calendar.MINUTE)
    val sec = c.get(Calendar.SECOND)
    val mil = c.get(Calendar.MILLISECOND)
    val t = (tmp/5)*5
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.set(Calendar.MINUTE,0)
    c.add(Calendar.MINUTE,t)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis
  }

  def getCurWeekEnd(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.add(Calendar.WEEK_OF_YEAR,1)
    c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)
    c.set(Calendar.HOUR_OF_DAY,0)
    c.set(Calendar.MINUTE,0)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)

    c.add(Calendar.SECOND,0)

    c.getTimeInMillis - 1
  }

  def getCurWeekStart(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.add(Calendar.WEEK_OF_YEAR,0)
    c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY)
    c.set(Calendar.HOUR_OF_DAY,0)
    c.set(Calendar.MINUTE,0)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)

    c.add(Calendar.SECOND,0)

    c.getTimeInMillis
  }

  def getCurMonthEnd(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.add(Calendar.MONTH,1)
    c.set(Calendar.DAY_OF_MONTH,1)
    c.set(Calendar.HOUR_OF_DAY,0)
    c.set(Calendar.MINUTE,0)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)
    c.add(Calendar.SECOND,0)
    c.getTimeInMillis - 1
  }

  def getCurMonthStart(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.add(Calendar.MONTH,0)
    c.set(Calendar.DAY_OF_MONTH,1)
    c.set(Calendar.HOUR_OF_DAY,0)
    c.set(Calendar.MINUTE,0)
    c.set(Calendar.SECOND,0)
    c.set(Calendar.MILLISECOND,0)
    c.add(Calendar.SECOND,0)
    c.getTimeInMillis
  }

  def getCurDayEnd(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.add(Calendar.DAY_OF_YEAR,1)
    c.set(Calendar.HOUR_OF_DAY,0)
    c.clear(Calendar.MINUTE)
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.add(Calendar.HOUR,0)
    c.add(Calendar.MINUTE,0)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis - 1
  }

  def getCurDayStart(date:Date) = {
    val c = Calendar.getInstance()
    c.setTime(date)
    c.add(Calendar.DAY_OF_YEAR,0)
    c.set(Calendar.HOUR_OF_DAY,0)
    c.clear(Calendar.MINUTE)
    c.clear(Calendar.SECOND)
    c.clear(Calendar.MILLISECOND)
    c.add(Calendar.HOUR,0)
    c.add(Calendar.MINUTE,0)
    c.add(Calendar.SECOND,0)
    c.add(Calendar.MILLISECOND,0)
    c.getTimeInMillis
  }

  def getMonthStart(start:Long) = {
    val c = Calendar.getInstance()
    c.setTime(new Date(start))
    c.set(Calendar.DAY_OF_MONTH, 2);
    //将小时至0
    c.set(Calendar.HOUR_OF_DAY, 0);
    //将分钟至0
    c.set(Calendar.MINUTE, 0);
    //将秒至0
    c.set(Calendar.SECOND,0);
    //将毫秒至0
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.SECOND,0)
    c.getTimeInMillis
  }

  def getWeekStart(start:Long) = {
    val c = Calendar.getInstance()
    c.setTime(new Date(start))
    c.setFirstDayOfWeek(Calendar.MONDAY)
    c.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY)
    //将小时至0
    c.set(Calendar.HOUR_OF_DAY, 0);
    //将分钟至0
    c.set(Calendar.MINUTE, 0);
    //将秒至0
    c.set(Calendar.SECOND,0);
    //将毫秒至0
    c.set(Calendar.MILLISECOND, 0);
    c.add(Calendar.SECOND,0)
    c.getTimeInMillis
  }

  def getCurMinute(date:String) = {
    fmt_yyyyMMddHHmm.parseDateTime(date).withSecond(0).getMillis
  }

  def getCurHour(date:String) = {
    fmt_yyyyMMddHH.parseDateTime(date).withMinute(0).withSecond(0).getMillis
  }

  def getCurDate(date:String) = {
    fmt_yyyyMMdd.parseDateTime(date).withHour(0).withMinute(0).withSecond(0).getMillis
  }

  def getCurWeek(date:String) = {
    fmt_yyyyWW.parseDateTime(date).withHour(0).withMinute(0).withSecond(0).getMillis
  }

  def getCurMonth(date:String) = {
    fmt_yyyyMM.parseDateTime(date).withDay(1).withHour(0).withMinute(0).withSecond(0).getMillis
  }

  def plusDaysOfEnd(date:String,day:Int) = {
    fmt_yyyyMMdd.parseDateTime(date).plusDays(day).withHour(23).withMinute(59).withSecond(59).getMillis + 999
  }

  def plusHoursOfEnd(date:String,hour:Int) = {
    fmt_yyyyMMddHH.parseDateTime(date).plusDays(hour).withMinute(59).withSecond(59).getMillis + 999
  }

  def plusMinutesOfEnd(date:String,minute:Int) = {
    fmt_yyyyMMddHHmm.parseDateTime(date).plusDays(minute).withSecond(59).getMillis + 999
  }




  def sinaDate2TimeStamp(date: String): Long = {
    new SimpleDateFormat("yyyy年MM月dd日 HH:mm").parse(date).getTime
  }

  def ntesDate2TimeStamp(date: String): Long = {
    new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").parse(date).getTime
  }

  def date2TimeStampMMddHHss(date:String):Long = {
    try{
      val d = date.split(" ")(0)
      val t = date.split(" ")(1)
      new DateTime().withMonthOfYear(d.split("-")(0).toInt).withDayOfMonth(d.split("-")(1).toInt)
        .withHourOfDay(t.split(":")(0).toInt).withMinuteOfHour(t.split(":")(1).toInt)
          .withSecondOfMinute(0).withMillisOfSecond(0).getMillis
    }catch {
      case e:Exception =>
        throw e
    }

  }

  def dateMMddYY2TimeStamp(date:String):Long = {
    new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(date).getTime

  }

  def todayBegin():Long ={
    new DateTime().withTimeAtStartOfDay().getMillis
  }

  def minusMonth(m:Int):Long ={
    new DateTime().minusMonths(m).withTimeAtStartOfDay().getMillis
  }

  def minusHours(m:Int):Long ={
    new DateTime().minusHours(m).getMillis
  }


  def TimeStamp2Date(timeStamp:Long) = {
    val sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//这个是你要转成后的时间的格式
    sdf.format(new Date(timeStamp))   // 时间戳转换成时间
  }

  def getLatest5Minute(date:String) :String = {
    var result = ""
    for(i <- 0 to 55 by 5){
      if(date.takeRight(2).toInt >= i && date.takeRight(2).toInt < i+5){
        result = if(i < 10) "0" + i.toString else i.toString
      }
    }
    date.dropRight(2) + result
  }

  def main(args: Array[String]): Unit = {
//    println(TimeStamp2Date(getCurMonthStart(new Date(System.currentTimeMillis()))))
//    println(TimeStamp2Date(getCurMonthEnd(new Date(System.currentTimeMillis()))))

  }

}
