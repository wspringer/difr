package nl.flotsam.difr

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.{Resource, ResourceLoader}
import java.io.PrintWriter
import org.fusesource.scalate.support.ScalaCompiler


object Tool {

  private val engine = new TemplateEngine()

  engine.resourceLoader = new ResourceLoader {
    def resource(uri: String) = {
      for {
        url <- Option(Tool.getClass.getResource(uri))
      } yield Resource.fromURL(url)
    }
  }

  def main(args: Array[String]) {
    try {
      val (done, reader) = {
        if (args.length == 0) {
          // read from stdin
          ( {
            () =>
          }, Console.in)
        } else {
          val result = new java.io.FileReader(args(0))
          ( {
            () => result.close()
          }, result)
        }
      }
      val diffs = try {
        GitDiffParser.asDiffs(reader)
      } finally {
        done()
      }

      if (diffs.isDefined) {
        val writer = new PrintWriter(System.out)
        engine.layout("/diff.scaml", writer, Map("diffs" -> diffs.get))
        writer.flush()
      }
    } finally {
      engine.compiler.asInstanceOf[ScalaCompiler].compiler.askShutdown()
    }
  }

}
