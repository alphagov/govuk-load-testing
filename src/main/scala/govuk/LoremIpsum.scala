package govuk.util

import scala.io.Source
import scala.util.Random

class LoremIpsum {
  val LoremIpsumFilePath = "test-data/lorem-ipsum.txt"
  val bufferedSource = Source.fromFile(LoremIpsumFilePath)
  val lines = bufferedSource.getLines.toList
  bufferedSource.close

  def text() = Random.shuffle(lines).mkString("\n")
}
