package org.javiervegas.chess


//import org.junit.Assert._

//import org.junit.Test


object GNUChessXTest { //extends Application {
  
  def main(args: Array[String]) = {
    val mychess = new GNUChessX
    
    val start = mychess.respond(Game(1L, None))
    
    playWithYourself(start)
    
    def playWithYourself(ping: Move):Unit = {
      
      println("PING"+ping)
      val pong = mychess.respond(Game(1L,Some(ping.hints(0))))
      
      println("PONG"+pong)
      //playWithYourself(pong)
    println
    }
    println("test done")
  }
}
