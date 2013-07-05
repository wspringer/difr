package nl.flotsam.difr

import util.parsing.combinator._
import java.io.Reader

object GitDiff {
  // file names have "a/" or "b/" as prefix, need to drop that to compare
  def apply (files: (String,String), op: FileOperation, chunks: List[ChangeChunk]) = {
    def strip(s: String) = s.dropWhile(_ != '/').drop(1)
    new GitDiff( strip( files._1 ), strip( files._2 ), op, chunks )
  }
}

case class GitDiff(oldFile: String, newFile: String, op: FileOperation, chunks: List[ChangeChunk]) {
  val isRename = oldFile != newFile
}

sealed trait FileOperation
case class NewFile(mode: Int) extends FileOperation
case class DeletedFile(mode: Int) extends FileOperation
case object UpdatedFile extends FileOperation

sealed trait LineChange { def line: String }
case class ContextLine(line: String) extends LineChange
case class LineRemoved(line: String) extends LineChange
case class LineAdded(line: String) extends LineChange
case class RangeInformation(oldOffset: Int, oldLength: Int, newOffset: Int, newLength: Int)
case class ChangeChunk(rangeInformation: RangeInformation, changeLines: List[LineChange])

// Code taken from http://stackoverflow.com/questions/3560073/how-to-write-parser-for-unified-diff-syntax
object GitDiffParser extends RegexParsers {

  override def skipWhitespace = false

  def allDiffs: Parser[List[GitDiff]] = rep1(gitDiff)

  def gitDiff: Parser[GitDiff] = filesChanged ~ fileOperation ~ diffChunks ^^ {
    case files ~ op ~ chunks => GitDiff(files, op, chunks)
  }

  def filesChanged: Parser[(String, String)] =
    "diff --git " ~> filename ~ (" " ~> filename) <~ newline ^^ { case f1 ~ f2 => (f1,f2) }

  def fileOperation: Parser[FileOperation] =
    opt(deletedFileMode | newFileMode) <~ index ^^ { _ getOrElse UpdatedFile }

  def index: Parser[Any] = ( "index " ~ hash ~ ".." ~ hash ) ~> opt(" " ~> mode) <~ newline
  def deletedFileMode: Parser[DeletedFile] = "deleted file mode " ~> mode <~ newline ^^ { m => DeletedFile(m) }
  def newFileMode: Parser[NewFile] = "new file mode " ~> mode <~ newline ^^ { m => NewFile(m) }
  def hash: Parser[String] = """[0-9a-f]{7,8}""".r
  def mode: Parser[Int] = """\d{6}""".r ^^ { _.toInt }

  def diffChunks: Parser[List[ChangeChunk]] = (oldFile ~ newFile) ~> rep1(changeChunk)

  def oldFile: Parser[String] = "--- " ~> filename <~ newline
  def newFile: Parser[String] = "+++ " ~> filename <~ newline
  def filename: Parser[String] = """[\S]+""".r

  def changeChunk: Parser[ChangeChunk] = rangeInformation ~ opt(contextLine) ~ (opt(newline) ~> rep1(lineChange)) ^^ {
    case ri ~ opCtx ~ lines => ChangeChunk(ri, opCtx map (_ :: lines) getOrElse (lines))
  }
  def rangeInformation: Parser[RangeInformation] =
    ("@@ " ~> "-" ~> number) ~ opt("," ~> number) ~ (" +" ~> number) ~ opt("," ~> number) <~ " @@" ^^ {
      case a ~ b ~ c ~ d => RangeInformation(a, b getOrElse 0, c, d getOrElse 0)
    }

  def lineChange: Parser[LineChange] = contextLine | addedLine | deletedLine
  def contextLine: Parser[ContextLine] = " " ~> """.*""".r <~ newline ^^ { l => ContextLine(l) }
  def addedLine: Parser[LineAdded] = "+" ~> """.*""".r <~ newline ^^ { l => LineAdded(l) }
  def deletedLine: Parser[LineRemoved] = "-" ~> """.*""".r <~ newline ^^ { l => LineRemoved(l) }

  def newline: Parser[String] = """\n""".r
  def number: Parser[Int] = """\d+""".r ^^ { _.toInt }

  def parse(str: String) = parseAll(allDiffs, str)

  def asDiffs(reader: Reader) = parseAll(allDiffs, reader) match {
    case Success(s, _) => Some(s)
    case NoSuccess(msg, _) => sys.error(msg)
  }

//  def main(args: Array[String]) {
//    val reader = {
//      if (args.length == 0) {
//        // read from stdin
//        Console.in
//      } else {
//        new java.io.FileReader(args(0))
//      }
//    }
//    parseAll(allDiffs, reader) match {
//      case Success(s,_) => println( s )
//      case NoSuccess(msg,_) => sys.error("ERROR: " + msg)
//    }
//  }
}