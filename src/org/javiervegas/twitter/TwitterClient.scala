package org.javiervegas.twitter

import java.io._

import org.javiervegas.utils.java2scala.Collections
import org.javiervegas.chess._
//import scala.io.Source
import scala.actors.Actor
import scala.actors.Actor._
import org.apache.log4j.Logger
import twitter4j._
import twitter4j.org.json._
//import twitter4j.http.AccessToken

trait TwitterClient extends Actor {
  
  //private var aggregators: Option[Map[User,Aggregator]] = None
  
  def act() = {
    val (start_id, start_cnt) = read_run_data
    //save_run_data(start_id+600,start_cnt+500)
    run(start_id,start_cnt)
  }
  
  private def run(last_fetched: Long, sent: Int):Unit = react {
    //case Client.Command.FetchFollowers => {
      //fetch_followers(last_fetched)
      //run(last_fetched,sent)
    //}
    case TwitterClient.Command.FetchMentions => {
      TwitterClient.LOG.debug("fetching mentions newer than id "+last_fetched)
      val (now_fetched,mentions) = fetch(last_fetched)
      dispatch(mentions)
      run(now_fetched, sent)
    }
    case Pair(text: String, replyTo: long) => {
      var replied = mytwitter.updateStatus(text, replyTo)
      TwitterClient.LOG.error("replid "+text)
      val serialize = new FileWriter(new File("last_turn")).write(last_fetched.toString+"|"+sent+1)
      save_run_data(last_fetched,sent+1)
      run(last_fetched,sent+1)
    }
  }

  private val mytwitter:Twitter = {
      TwitterClient.LOG.debug("initializing Twitter object")
      val mytwitter = new Twitter
      mytwitter setOAuthAccessToken(
          Configuration getProperty("twitter4j.oauth.userKey"),
          Configuration getProperty("twitter4j.oauth.userSecret")
      )
      mytwitter
  }

  def fetch(last_fetched: Long) = {
      val newMentions = Collections.asList(mytwitter.getMentions(new Paging(last_fetched)))
      val now_fetched = newMentions match {
        //case None => last_fetched
        case l if l.isEmpty => last_fetched
        case _ => newMentions.map(_.getId).sort(_>_).first
      }
      TwitterClient.LOG.debug(newMentions.size+" fetched,last one is "+now_fetched)
      (now_fetched,newMentions)
  }

  
  def dispatch(mentions: List[Status]) = { 
    mentions match {
      case l if l.isEmpty =>     TwitterClient.LOG.debug("nothing to  dispatch")
      case _ => {
    /* mentions.foldLeft(None: Option[Map[User,List[Status]]])((b,a)=> b match {
      case None => Map(a.getUser->List(a))
      case _ => b.get(a.getUser) match {
        case None => b+a.getUser->List(a)
        case _ => b+a.getUser->a::b(a.getUser)
    }}) */
    val one = mentions.reduceLeft((a,b)=>a)
    val map = Map(one.getUser->List(one)) //TODO
    TwitterClient.LOG.debug("dispatching "+one.getId+" for "+one.getUser)
    map.foreach(kv=>{
      val ag = new Aggregator
      ag.start
      ag ! kv._2
    })
  }
}}
  
  def save_run_data(start_id: Long, start_cnt: Int) = {
    val file = new FileWriter(new File("last_turn"))
    file.write(start_id+"|"+start_cnt)
    file.close
  }
  def read_run_data = {
    val in = new DataInputStream(new FileInputStream("last_turn"))
    val filereader = new BufferedReader(new InputStreamReader(in))
    val raw_data = filereader.readLine.split('|')
    in.close
    (raw_data(0).toLong,raw_data(1).toInt)
  }
}

object TwitterClient {
  private val LOG = Logger.getLogger(this.getClass)
  object Command extends Enumeration {
    type Command = Value
    val FetchMentions, FetchFollowers = Value
  }
}
