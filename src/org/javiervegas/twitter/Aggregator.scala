package org.javiervegas.twitter

import org.javiervegas.chess._
import org.javiervegas.mail.JavaxMailer

import scala.actors.Actor
import scala.actors.Actor._
import org.apache.log4j.Logger
import twitter4j.Status

class Aggregator extends Actor {
  
  private val chess = new GNUChessX
  chess.start
  
  
  //aggregators listen to twitter TwitterClient,receive lists of status updates
  //from a given user and respond to the TwitterClient with a chess move option
  //
  def act = loop {
    Aggregator.LOG.debug("acting")
    react {
      case l: List[Status] if !l.isEmpty  => {
        val replyTo = l.reduceLeft((a,b)=>a) //TODO improve this to better extract status info
        //message the chess engine and waits for a chess move response
        val response = chess !? getGame(replyTo)
            def update_position_image(position: Option[Position]):String = position match {
              case Some(pos: Position) => {
                //TODO graph the position and prepare it for attachment
                val positionGraph = new ChessPlotter(pos).plot
                //TODO mail text and attachment to posterous
                val postId = replyTo.getUser.getScreenName+ {response match {
                  case Move(Some(Ply(i:Int,s)),_,_) => " is your turn to make move "+(i+1)
                  case _ => 0
                }}
                (new JavaxMailer("anatolykarpovot@gmail.com","post@akv.posterous.com",postId,"\n\nIt is your turn now, "+replyTo.getUser.getScreenName+
                                   {response match {
                                     case Move(Some(Ply(i:Int,s)),_,_) => "\n\nI just made move "+i+": "+s.toString
                                     case _ => ""
                                   }}+
                                   "\n\nReply to me with your move "+ {response match {
                                       case Move(_,_,hints) => "(for example, \"@karpovbot "+hints(0).format+"\") "
                                       case _ =>""
                                     }}
                                     +"and we will continue playing.", List(positionGraph))).send
                //TODO get short link for the posterous post
                //TODO return text and short link
                "Watch the game at http://akv.posterous.com/"+postId.toLowerCase.replaceAll(" ","-")
              }
              case _ => ""
        }
        //prepare status update for the TwitterClient with response move data
        val update  = response match {
          case Move(Some(Ply(i:Int,s:String)),position,hints) => "@"+replyTo.getUser.getScreenName+" "+
          update_ply(Ply(i,s)) + update_hints(hints)+ update_position_image(position)
          case Move(None,position,hints) => "I am afraid I cant do that, @"+replyTo.getUser.getScreenName+" "+
          update_position_image(position) + update_hints(hints)
          case ko:Any => Aggregator.LOG.error(ko); "I am afraid I cant do that, @"+replyTo.getUser.getScreenName
        }
        //send status update to the TwitterClient
        ChessClient.get ! Pair(update,replyTo.getId)
      }
      //got something weird from the TwitterClient, not sure what to about it
      case o:Any => Aggregator.LOG.debug("Houston, we have a problem, the TwitterClient is sending gibberish "+o)
    }
  }
  
  //extracts chessmove from status update
  def getGame(message: Status):Game =  {
      val z = message.getText.split(' ')
      z match {
        case z if z(0).contains(".") => Game(message.getUser.getId,Some(new Ply(z(0))))
        case z if z.reverse(0).contains(".") => Game(message.getUser.getId,Some(new Ply(z.reverse(0))))
        case _ => Game(message.getUser.getId,None)
      }
  }
  
  def update_ply(ply: Ply):String = {
      "My move is: "+ply.format+" "
  }
  
  def update_hints(hints: List[Ply]):String = {
      hints.foldLeft("Reply with your move (hint: ")((b,a)=>b+" "+a.format)+") to continue."
  }
}

object Aggregator {
  private val LOG = Logger.getLogger(Aggregator.getClass)
}