package org.javiervegas.chess

import java.io.{File,FileInputStream,ByteArrayOutputStream,BufferedOutputStream,OutputStream}
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

class ChessPlotter(position: Position) {
  
  def plot() = {
      //val output = new BufferedOutputStream(new ByteArrayOutputStream)
      val output = new File("tmp/"+position.toString+".png")
      val bi = new BufferedImage(32*8, 32*8, BufferedImage.TYPE_BYTE_BINARY)
      val g2d = bi.createGraphics
      g2d.setBackground(java.awt.Color.WHITE)
      position.getSquares.foldLeft(0)((b,a)=>{
        val row:Int = b/8
        val col = b%8
        val bck = b match {
          case i: Int if (i+i/8)%2 == 0 => 'w'
          case i: Int if (i+i/8)%2 == 1 => 'b'
        }
        val figure = a match {
          case s: Char if s == '.' => "no"
          case s: Char if s == s.toLowerCase => "b"+s
          case s: Char if s == s.toUpperCase => "w"+s.toLowerCase
        }
        g2d.drawImage(ImageIO.read(new File("img/chess/"+figure+bck+".png")),32*col,32*row,null)
        b+1
      })
      
      ImageIO.write(bi,"png", output)
      output
  }
  
}
