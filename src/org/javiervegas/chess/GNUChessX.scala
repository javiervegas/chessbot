package org.javiervegas.chess

import scala.actors.Actor
import scala.actors.Actor._
import org.apache.log4j.Logger
import java.io._
import java.util.regex._

class GNUChessX extends Actor with Engine {
  
  
  def respond(game: Game): Move = {
      val rt = Runtime.getRuntime()
      val proc = try { rt.exec("/opt/local/bin/gnuchess") } catch { case _ => rt.exec("/usr/games/gnuchess") }
      val stdin = proc.getOutputStream
      val stderr = proc.getErrorStream
      val stdout = proc.getInputStream
      //int exitVal = proc.exitValue()
      val input = new BufferedReader(new InputStreamReader(stdout))
      val output  =new PrintStream(new BufferedOutputStream(stdin), true)
      val sb = new StringBuilder
      GNUChessX.LOG.error("responding to "+game)
      game match {
        case Game(i: Long,Some(Ply(j,_))) => output.println("load data/chess/fen/"+i+"_"+j+".fen")
        case _ => println
      }
      output.println(game match {
        case Game(_,None) => "go"
        case Game(_,Some(Ply(_,ply:String))) => ply
      })
      eachline(input, l => l.contains("My move is : ") || l.contains("Illegal move")) {
        line => println(line);sb.append(line)
      }
      def eachline(buf: BufferedReader, filter: Function[String,Boolean])(f: String => Unit) {
        var line = buf.readLine
        f(line)
        while (line != null && !filter(line)) {
          line = buf.readLine
          f(line)
        }
      }
      val positionMatcher = GNUChessX.positionPattern.matcher(sb.toString)
      val position = positionMatcher.find match {
        case false => None
        case true => Some(new Position(positionMatcher.group(1).filter(c => c.toString != " " && c.toString != "\n")))
      }
      val order = game match {
        case Game(_,None) => 1
        case Game(_: Long,Some(Ply(i: Int,_))) => i+1
      }
      output.println("hint")
      val hints =  List(Ply(order, input.readLine.split("Hint: ")(1).trim))
      
      val ply = sb.toString match { 
        case s:String if s.contains("My move is :") => {
            val saveTo = "data/chess/fen/"+{game match{case Game(i: Long,_)=>i}}+"_"+order+".fen"
            (new File(saveTo)).delete//so it can be overwritten
            output.println("save "+saveTo)
            Some(new Ply(order, s.split("My move is :")(1).trim))
        }
        case _ => None
      }
      output.println("quit")
      Move(ply, position, hints)
  }
  

  
  def act = loop {
    GNUChessX.LOG.error("reacting")
    react {
      case g:Game => reply(respond(g))
    }
  }
}

object GNUChessX {
  private val LOG = Logger.getLogger(GNUChessX.getClass)
  val positionPattern = Pattern.compile("""((([\w\.]\s){8}){8})(\s*My move)""")
}