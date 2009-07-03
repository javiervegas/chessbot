package org.javiervegas.chess

trait Engine {

  def respond(game: Game): Move

}

class ChessException extends Exception
