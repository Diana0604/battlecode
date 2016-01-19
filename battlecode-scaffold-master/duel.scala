#!/usr/bin/env scala
!#

import java.io.File
import sys.process._

val maps = Vector(
  "arena",
  "barred",
  "barricade",
  "boxy",
  "caverns",
  "closequarters",
  "collision",
  "desert",
  "diffusion",
  "factory",
  "farm",
  "forts",
  "goodies",
  "helloworld",
  "nexus",
  "space",
  "spiral",
  "streets",
  "swamp",
  "zigzag"
)

def matchWinner(playerA: String, playerB: String, map: String): Option[String] = {
  def genConfig(teamA: String, teamB: String, map: String): String = {
    val configLines = Vector(
      "bc.game.maps=" + map,
      "bc.game.team-a=" + teamA,
      "bc.game.team-b=" + teamB,
      "bc.server.save-file=match.rms",
      "bc.game.disable-zombies=false"
    )
    configLines.mkString("\n")
  }

  val config = genConfig(playerA, playerB, map)
  val configFilename = "temp-" + playerA + "-" + playerB + "-" + map + ".conf"
  (Seq("echo", config) #> new File(configFilename)).!
  val result = Seq("ant", "headless", "-Dbc.conf=" + configFilename).!!
  Seq("rm", configFilename).!
  val lines = result.split("\n")
  if (lines.length < 6) {
    None
  } else {
    val winnerRE = """\s*\[java\] \[server\]\s+.+ \((A|B)\) wins \(round \d+\)""".r
    lines(lines.length - 6) match {
      case winnerRE("A") => Some(playerA)
      case winnerRE("B") => Some(playerB)
      case _ => None
    }
  }
}

if (args.length < 2) {
  println("usage: ./duel.scala playerA playerB")
} else {
  val playerA = args(0)
  val playerB = args(1)
  val results = maps.map(
    matchWinner(playerA, playerB, _)
  ) ++ maps.map(
    matchWinner(playerB, playerA, _)
  )
  val winners = results.collect({
    case Some(x) => x
  })

  val numErrors = results.length - winners.length
  if (numErrors > 0) {
    println(numErrors + " errors")
  }

  assert(winners.forall(x => x == playerA || x == playerB))
  val winsA = winners.count(_ == playerA)
  val winsB = winners.length - winsA
  val winPercentA = winsA * 100 / winners.length
  val winPercentB = winsB * 100 / winners.length
  println(playerA + " won " + winsA + " of " + winners.length + " (" + winPercentA + "%)")
  println(playerB + " won " + winsB + " of " + winners.length + " (" + winPercentB + "%)")
}
