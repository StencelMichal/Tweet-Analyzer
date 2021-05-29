package agh.cs.lab.scala.utils

import java.io.File

object ResultPrinter {
  private def printToFile(f: java.io.File)(op: java.io.PrintWriter => Unit) {
    val p = new java.io.PrintWriter(f)
    try {
      op(p)
    } finally {
      p.close()
    }
  }

  def print(path: String, text: String): Unit = {
    printToFile(new File(path)) { p =>
      p.println(text)
    }
  }
}
