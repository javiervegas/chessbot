package org.javiervegas.chess

object MoveTest {
  def main(args : Array[String]) : Unit = {
    
    println(Ply(1,"foo"))
    val s = "1.foo"
    val dot:Char = '.'
    val sp = s.split(dot)
    println(sp)
    println(sp(1))
    println(new Ply("1.foo"))
  }
}
