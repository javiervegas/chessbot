package org.javiervegas.utils.java2scala

import java.util.regex._

object Collections {

  def asList[T](javalist:java.util.List[T]) = {
    javalist.toArray[T](new Array[T](0)).toList
  }

  def asSeq[T](javalist:java.util.List[T]):Option[Seq[T]] = { 
    javalist match { 
      case ko if ko == Nil => None
      case ok => Some(ok.toArray[T](new Array[T](0)).toSeq)
    }
  }

  def max(x: Long, y: Long): Long = if (x < y) y else x
}

object Regex {
  def allLetters(s: String):Boolean = letters.matcher(s).matches
  private val letters = Pattern.compile("""[a-z]+""")
}
