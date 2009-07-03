package org.javiervegas.twitter


class ReplClient extends TwitterClient {
  
}

object ReplClient extends TwitterClient{

  private val singleton = new ReplClient
  def get = singleton
}

