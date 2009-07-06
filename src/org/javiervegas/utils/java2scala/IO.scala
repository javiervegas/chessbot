package org.javiervegas.utils.java2scala

import java.io._

object IO {
  
  def readFileToString(f: File) = {
    val bos = new ByteArrayOutputStream
    val ba = new Array[Byte](2048)
    val is = new FileInputStream(f)
    def read {
      is.read(ba) match {
        case n if n < 0 =>
        case 0 => read
        case n => bos.write(ba, 0, n); read
      }
    }
    read
    bos.toString("UTF-8")
  }
}
