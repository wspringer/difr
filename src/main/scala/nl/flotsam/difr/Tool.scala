package nl.flotsam.difr

import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.util.{Resource, ResourceLoader}
import java.io._
import org.fusesource.scalate.support.ScalaCompiler
import scala.Some
import scala.Console


/**
 * A tool creating an HTML git diff view with the ability to add annotations.
 *
 * @see http://nxt.flotsam.nl/i-beg-to-difr
 */
object Tool {

  private val engine = new TemplateEngine()

  engine.resourceLoader = new ResourceLoader {
    def resource(uri: String) = {
      for {
        url <- Option(Tool.getClass.getResource(uri))
      } yield Resource.fromURL(url)
    }
  }

  import org.rogach.scallop._

  private def parseArgs(args: Array[String]) = new ScallopConf(args) {
    this.banner("Usage: difr ARGS\n")
    val ext = opt[Boolean](
      name = "ext",
      default = Some(false),
      descr = "Use external files. Mostly useful for development.",
      hidden = true,
      required = false
    )
    val help = opt[Boolean](
      name = "help",
      default = Some(false),
      descr = "Print help.",
      required = false
    )
    val in = opt[String](
      name = "in",
      descr = "Input file. Defaults to stdin if omitted."
    )
    val out = opt[String](
      name = "out",
      descr = "Output file. Defaults to stdout if omitted."
    )
  }

  /**
   * Act upon either stdin or the input file given.
   */
  private def usingIn[T](in: Option[String], fn: (Reader) => T): T = {
    if (in.isDefined) {
      val reader = new FileReader(in.get)
      try {
        fn(reader)
      } finally {
        reader.close()
      }
    } else {
      fn(Console.in)
    }
  }

  /**
   * Write to either stdout or the output file given.
   */
  private def usingOut[T](out: Option[String], fn: (PrintWriter) => T): T = {
    if (out.isDefined) {
      val writer = new PrintWriter(new FileWriter(out.get))
      try {
        fn(writer)
      } finally {
        writer.close()
      }
    } else {
      fn(new PrintWriter(System.out))
    }
  }

  def main(args: Array[String]) {
    val conf = parseArgs(args)
    if (conf.help()) conf.printHelp()
    else try {
      for (diffs <- usingIn(conf.in.get, parse)) {
        usingOut(conf.out.get, {
          writer =>
            engine.layout("/diff.scaml", writer, Map(
              "diffs" -> diffs,
              "ext" -> conf.ext()
            ))
            writer.flush()
        })
      }
    } finally {
      engine.compiler.asInstanceOf[ScalaCompiler].compiler.askShutdown()
    }
  }

  private def parse(reader: Reader): Option[List[GitDiff]] =
    GitDiffParser.asDiffs(reader)

}
