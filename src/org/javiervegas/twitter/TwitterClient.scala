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
    run(start_id,start_cnt,mytwitter.getFriendsIDs.getIDs.toList)
  }
  
  private def run(last_fetched: Long, sent: Int, following: List[Int]):Unit = react {
    case TwitterClient.Command.FetchDMs => {
      TwitterClient.LOG.debug("fetching DMs newer than id "+last_fetched)
      val (now_fetched,dms) = fetchDMs(last_fetched)
      dispatch(dms)
      run(now_fetched, sent, following)
    }
    case TwitterClient.Command.FetchFollowers => {
      run(last_fetched,sent,addNewFollowers(following))
    }
    case Pair(text: String, replyTo: User) => {
      TwitterClient.LOG.error("DMing "+replyTo)
      var replied = mytwitter.sendDirectMessage(replyTo.getScreenName, text)
      TwitterClient.LOG.error("DMed "+text)
      val serialize = new FileWriter(new File("last_turn")).write(last_fetched.toString+"|"+sent+1)
      save_run_data(last_fetched,sent+1)
      run(last_fetched,sent+1,following)
    }
  }

  private val mytwitter:Twitter = {
      TwitterClient.LOG.debug("initializing Twitter object")
      val mytwitter = new Twitter
      mytwitter setOAuthAccessToken(TwitterClient.userKey,TwitterClient.userSecret)
      mytwitter
  }

  def fetchDMs(last_fetched: Long) = {
      val newDMs = Collections.asList(mytwitter.getDirectMessages(new Paging(last_fetched)))
      val now_fetched = newDMs match {
        //case None => last_fetched
        case l if l.isEmpty => last_fetched
        case _ => newDMs.map(_.getId.toLong).sort(_>_).first
      }
      TwitterClient.LOG.error(newDMs.size+" fetched,last one is "+now_fetched)
      (now_fetched,newDMs)
  }

  def addNewFollowers(following: List[Int]) = {
    TwitterClient.LOG.error(following)
    val followers = mytwitter.getFollowersIDs.getIDs
    followers.filter(!following.contains(_)).map(_.toString).foreach({s:String => 
      TwitterClient.LOG.error("friending "+s)
      try {
        mytwitter.createFriendship(s)
        val ag = new Aggregator
        ag.start
        ag ! mytwitter.getUserDetail(s)
      } catch {
        case e:Exception => TwitterClient.LOG.error(e);null
      }
    })
    following.union(followers.toList)
  }
  
  def dispatch(mentions: List[DirectMessage]) = { 
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
    val map = Map(one.getSender->List(one)) //TODO
    TwitterClient.LOG.debug("dispatching "+one.getId+" for "+one.getSender)
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
  protected val LOG = Logger.getLogger(this.getClass)
  
  //anatolykarpovot
  //val userKey = "50588699-2MPu4doo60Xu4v0s1h8KWY9hxGSJ7DGdfJHVYJaZK"
  //val userSecret = "4kSdO72rFpC786p2lNyS5zRe62hqRTRRF6lAWriGfE"
  
  //scalarepl
  //val userKey = "52423939-E2Xtrv5I8RNYwan3pCXKNnNprRhKpq82SMGirrgws"
  //val userSecret = "jsmezX2RNnKz6VGFpkMZGvoognOTgqZDFYtYgNwjybs"
  
  //anatolykarpobot
  val userKey = "54057881-ep0ZkSsi8fcR5uGePQtC0wIlLpGbRDSacTiOVVzk"
  val userSecret = "ceNcCj3mRth1szawOLzEyDDJVLep0MhSPHjklNO3pU"
  
  object Command extends Enumeration {
    type Command = Value
    val FetchDMs, FetchFollowers = Value
  }
}

