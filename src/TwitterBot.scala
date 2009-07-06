import org.javiervegas.twitter._

object TwitterBot extends Application {

  private val client: TwitterClient = ChessClient.get
  private val run_at_interval = 1000*41 //40 seconds
  //private val Client = new Client 

    /*
     TODO:
     auto followrs
     keep track of responded items across restarts
     respond to DM
     add hint diagrams to posterous
     add "nice move! comments"
     
     */
    
  init
  run

  def init {
    client start
  }

  def run {
    client ! TwitterClient.Command.FetchFollowers
    client ! TwitterClient.Command.FetchDMs
    Thread sleep run_at_interval
    run
  }

}

