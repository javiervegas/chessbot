package org.javiervegas.twitter

class ChessClient extends TwitterClient {
  
}

object ChessClient {

  private val singleton = new ChessClient
  def get = singleton
}

