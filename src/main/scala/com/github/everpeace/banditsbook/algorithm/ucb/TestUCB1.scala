/*
 * Copyright (c) 2016 Shingo Omura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.github.everpeace.banditsbook.algorithm.ucb

import java.io.{BufferedWriter, PrintWriter}

import breeze.linalg._
import com.github.everpeace.banditsbook.arm._
import com.github.everpeace.banditsbook.testing_framework.TestRunner
import com.github.everpeace.banditsbook.testing_framework.TestRunner._
import com.typesafe.config.ConfigFactory

import scala.collection.immutable.Seq

object TestUCB1 extends _TestUCB1 with App{
  run()
}

trait _TestUCB1 {
  def run() = {
//    implicit val randBasis = RandBasis.mt0

    val conf = ConfigFactory.load()
    val baseKey = "banditsbook.algorithm.ucb.test-ucb1"
    val (_means, _, horizon, nSims) = readConfig(conf, baseKey)
    val means = shuffle(_means)
    val arms = Seq(means:_*).map(μ => BernoulliArm(μ))


    val file =new BufferedWriter( new PrintWriter("test-ucb1-results.csv"), nSims * horizon)
    file.write("sim_num, step, chosen_arm, reward, cumulative_reward\n")
    try {
      println("-------------------------------")
      println("UCB1 Algorithm")
      println("-------------------------------")
      println(s"   arms = ${means.map("(μ="+_+")").mkString(", ")} (Best Arm = ${argmax(means)})")
      println(s"horizon = $horizon")
      println(s"  nSims = $nSims")
      println( "The algorithm has no hyper parameters.")
      println("")

      println(s"starts simulation.")

      val algo = UCB1.Algorithm
      val res = TestRunner.run(algo, arms, nSims, horizon)

      for {sim <- 0 until nSims} {
        val st = sim * horizon
        val end = ((sim + 1) * horizon) - 1
      }
      val finalRewards = res.cumRewards((horizon-1) until (nSims * horizon, horizon))
      import breeze.stats._
      val meanAndVar = meanAndVariance(finalRewards)
      println(s"reward stats: ${TestRunner.toString(meanAndVar)}")

      res.rawResults.valuesIterator.foreach{ v =>
        file.write(s"${Seq(v._1, v._2, v._3, v._4, v._5).mkString(",")}\n")
      }
      println(s"finished simulation.")
    } finally {
      file.close()
      println("")
      println("results are written to \"test-ucb1-results.csv\"")
    }
  }
}
