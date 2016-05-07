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

package com.github.everpeace.banditsbook.testing_framework

import breeze.linalg.Vector._
import breeze.linalg._
import breeze.stats.MeanAndVariance
import breeze.storage.Zero
import com.github.everpeace.banditsbook.algorithm.{Algorithm, TracedAlgorithmDriver}
import com.github.everpeace.banditsbook.arm._
import com.typesafe.config.Config

import scala.collection.immutable.Seq
import scala.reflect.ClassTag

object TestRunner {
  def toString(mv: MeanAndVariance) = s"(μ = ${mv.mean}, σ^2 = ${mv.variance})"

  def readConfig(conf: Config, baseKey: String, hyper_parameters_name: Option[String] = None) = {
    import scala.collection.convert.decorateAsScala._
    val means = Array(
      conf.getDoubleList(s"$baseKey.arm-means").asScala.map(_.toDouble): _*
    )
    val hyper_parameters = hyper_parameters_name.map(name =>
      conf.getDoubleList(s"$baseKey.$name").asScala.map(_.toDouble).toSeq
    )
    val horizon = conf.getInt(s"$baseKey.horizon")
    val nSims = conf.getInt(s"$baseKey.n-sims")
    (means, hyper_parameters, horizon, nSims)
  }

  case class TestRunnerResult(
    arms: Seq[Arm[Double]],
    numSims: Int,
    horizon: Int,
    simNums: Vector[Int],
    stepNums: Vector[Int],
    chosenArms: Vector[Int],
    rewards: Vector[Double],
    cumRewards: Vector[Double],
    // raw data format:
    // simNum, step, chosenArm, rewards, cumRewards
    rawResults: Vector[(Int, Int, Int, Double, Double)]
  )

  def run[AlgState](alg: Algorithm[Double, AlgState], arms: Seq[Arm[Double]], nSims: Int, horizon: Int)
                   (implicit zeroDouble: Zero[Double], zero:Zero[Int], classTag: ClassTag[Double] )
  = {
    val simNums = zeros[Int](nSims * horizon)
    val stepNums = zeros[Int](nSims * horizon)
    val chosenArms = zeros[Int](nSims * horizon)
    val rewards = zeros[Double](nSims * horizon)
    val cumRewards = zeros[Double](nSims * horizon)
    val rawResults = fill(nSims * horizon)((0, 0, 0, 0.0d, 0.0d))

    for { sim <- 0 until nSims }{
      val st = horizon * sim
      val end = (horizon * (sim + 1)) - 1

      val driver = TracedAlgorithmDriver(alg)
      val res = driver.run(arms, horizon)

      val cums = Vector(
        res.trace.rewards.valuesIterator.foldLeft(Array.empty[Double])((cum, r) => cum ++ Array(cum.lastOption.getOrElse(0d) + r))
      )
      simNums(st to end) := fill(horizon)(sim)
      stepNums(st to end) := tabulate(horizon)(identity)
      chosenArms(st to end) := res.trace.chosenArms
      rewards(st to end) := res.trace.rewards
      cumRewards(st to end) := cums
      rawResults(st to end) := tabulate(horizon)(i =>
        (sim, i, res.trace.chosenArms(i), res.trace.rewards(i), cums(i))
      )
      print((if (sim % 10 == 9) "." else "") + (if (sim % 1000 == 999 || sim == nSims - 1) s"[${sim + 1}]\n" else ""))
    }

    TestRunnerResult(
      arms, nSims, horizon, simNums, stepNums,
      chosenArms, rewards, cumRewards,
      rawResults
    )
  }

}
