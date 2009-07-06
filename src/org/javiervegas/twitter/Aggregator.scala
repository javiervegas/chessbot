package org.javiervegas.twitter

import org.javiervegas.chess._
import org.javiervegas.mail.JavaxMailer

import scala.actors.Actor
import scala.actors.Actor._
import org.apache.log4j.Logger
import twitter4j.{User,DirectMessage}
import java.io._
import org.javiervegas.utils.java2scala.IO

class Aggregator extends Actor {
  
  private val chess = new GNUChessX
  chess.start
  
  
  //aggregators listen to twitter TwitterClient,receive lists of status updates
  //from a given user and respond to the TwitterClient with a chess move option
  //
  def act = loop {
    Aggregator.LOG.debug("acting")
    react {
      case newFriend: User => {
        val response = chess !? Game(newFriend.getId.toLong,None)
        ChessClient.get ! Pair(writeUpdate(response, newFriend.getScreenName,None),newFriend)
      }
      case l: List[DirectMessage] if !l.isEmpty  => {
        val replyTo = l.reduceLeft((a,b)=>a) //TODO improve this to better extract status info
        //message the chess engine and waits for a chess move response
        val response = chess !? getGame(replyTo)
        //send status update to the TwitterClient
        ChessClient.get ! Pair(writeUpdate(response,replyTo.getSender.getScreenName,Some(getGame(replyTo))),replyTo.getSender)
      }
      //got something weird from the TwitterClient, not sure what to about it
      case o:Any => Aggregator.LOG.debug("Houston, we have a problem, the TwitterClient is sending gibberish "+o)
    }
  }
  
  //prepare status update for the TwitterClient with response move data
  def writeUpdate(response: Any, name: String, challenge: Option[Game])  = response match {
    case Move(Some(Ply(i:Int,s:String)),position,hints) => {
      challenge match {
        case None =>
        case Some(Game(_,Some(Ply(_,m)))) => logBlack(name, m)
      }
      logWhite(name,i,s)
      "@"+name+" "+update_ply(Ply(i,s)) + update_hints(hints)+ update_position_image(response, name)
    }
    case Move(None,position,hints) => "I am afraid I cant do that, @"+name+" "+
      update_position_image(response, name) + update_hints(hints)
    case ko:Any => Aggregator.LOG.error(ko); "I am afraid I cant do that, @"+name
  }
  
  def update_position_image(response: Any, name: String):String = response match {
    //position: Option[Position]
    case Move(_,Some(pos),_) => { //Some(pos: Position)
      val order = response match{
        case Move(Some(Ply(i:Int,_)),_,_) => i
        case _ => 0
      }
      val positionGraph = new ChessPlotter(pos).plot(name+"_"+order)
      val postId = name+ {order match {
        case i:Int if i > 0 => " is your turn to make your move "+i
        case _ => 0
      }}
      (new JavaxMailer("anatolykarpovot@gmail.com","post@akv.posterous.com",postId,"\n\nIt is your turn now, "+name+
          {response match {
            case Move(Some(Ply(i:Int,s)),_,_) => "\n\nI just made move "+i+": "+s.toString
            case _ => ""
          }}+
          "\n\nReply to me with your move "+ {response match {
            case Move(_,_,hints) => "(for example, \"@karpovbot "+hints(0).format+"\") "
            case _ =>""
          }}
          +"and we will continue playing.\n\nGame history:\n"+
          IO.readFileToString(new File("data/chess/csv/"+name+".txt"))+" "
            , List(positionGraph))).send
          //TODO get short link for the posterous post
          //TODO return text and short link
          " See the game at http://akv.posterous.com/"+postId.toLowerCase.replaceAll(" ","-")
    }
    case _ => ""
  }
  
  //extracts chessmove from status update
  def getGame(message: DirectMessage):Game =  {
      val z = message.getText.split(' ')
      z match {
        case z if z(0).contains(".") => Game(message.getSender.getId,Some(new Ply(z(0))))
        case z if z.reverse(0).contains(".") => Game(message.getSender.getId,Some(new Ply(z.reverse(0))))
        case _ => Game(message.getSender.getId,None)
      }
  }
  
  def update_ply(ply: Ply):String = {
      "My move is: "+ply.format+" "
  }
  
  def update_hints(hints: List[Ply]):String = {
      hints.foldLeft("Reply with your move (hint: ")((b,a)=>b+" "+a.format)+") to continue."
  }
  
  def logWhite(name: String, order: Int, ply:String) = {
    logGame(name,+order+". "+ply)
  }
  
  def logBlack(name: String, ply:String) = {
    logGame(name, " "+ply+"\n")
  }
  def logGame(name: String, s:String) = {
    val file = new BufferedWriter(new FileWriter("data/chess/csv/"+name+".txt",true))
    file.write(s)
    file.close
  }
  /*
  def read_run_data = {
    val in = new DataInputStream(new FileInputStream("last_turn"))
    val filereader = new BufferedReader(new InputStreamReader(in))
    val raw_data = filereader.readLine.split('|')
    in.close
    (raw_data(0).toLong,raw_data(1).toInt)
  }
  */
}

object Aggregator {
  private val LOG = Logger.getLogger(Aggregator.getClass)
}