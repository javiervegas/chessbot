package org.javiervegas.twitter

class ChessClient extends TwitterClient {
  
}

object ChessClient extends TwitterClient {

  
  private val singleton = new ChessClient
  def get = singleton
}

