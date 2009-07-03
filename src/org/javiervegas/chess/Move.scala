package org.javiervegas.chess

case class Move(ply: Option[Ply], position: Option[Position], hints: List[Ply]) {
  
}

case class Game(opponent: Long, lastOppoPly: Option[Ply])

case class Ply(order: Int, ply :String) {
  
  def this(s: Array[String]) = {
    this(s(0).toInt,s(1))
  }
    
  def this(s: String) = {
    this(s.split('.'))
  }
  
  def format = {
    order+"."+ply
  }
}

class Position (squares: Seq[Char]){
  val _squares = squares
  def getSquares = {
    _squares
  }
  
}
