package govuk.util

import scala.io.Source
import scala.util.Random

class LoremIpsum {
  val LoremIpsumFilePath = "src/test/resources/test-data/lorem-ipsum.txt"
  val bufferedSource = Source.fromFile(LoremIpsumFilePath)
  val lines = bufferedSource.getLines.toList
  bufferedSource.close

  def text() = Random.shuffle(lines).mkString("\n")

  def slug() {
    val words = Random.shuffle(lines).head.split(" ")
    words.slice(0,4).mkString("-")
  }

  def path() {
    val words = Random.shuffle(lines).head.split(" ")
    words.slice(0,2).mkString("/")
  }

  def sentence() {
    val sentences = Random.shuffle(lines).head.split(". ")
    val index = Random.nextInt(sentences.length)
    sentences(index)
  }
}
