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

package com.github.everpeace.banditsbook.algorithm.exp3

import breeze.linalg.Vector._
import breeze.linalg._
import breeze.numerics.exp
import breeze.stats.distributions.{Rand, RandBasis}
import breeze.storage.Zero
import com.github.everpeace.banditsbook.algorithm._
import com.github.everpeace.banditsbook.arm.Arm

import scala.collection.immutable.Seq
import scala.reflect.ClassTag

/**
  * see: http://www.cs.nyu.edu/~mohri/pub/bandit.pdf
  */
object Exp3 {

  case class State(γ: Double, weights: Vector[Double], counts: Vector[Int])

  def Algorithm(γ: Double)(implicit zeroReward: Zero[Double], zeroInt: Zero[Int], tag: ClassTag[Double], rand: RandBasis = Rand)
  = {
    require(0< γ && γ <= 1, "γ must be in (0,1]")

    new Algorithm[Double, State] {

      override def initialState(arms: Seq[Arm[Double]]): State = State(
        γ, fill(arms.size)(1.0d), zeros[Int](arms.size)
      )

      override def selectArm(arms: Seq[Arm[Double]], state: State): Int =
        CategoricalDistribution(probs(state.γ, state.weights)).draw()

      override def updateState(arms: Seq[Arm[Double]], state: State, chosen: Int, reward: Double): State = {
        val counts = state.counts
        val weights = state.weights

        val count = counts(chosen) + 1
        counts.update(chosen, count)

        val K = weights.size
        val p = probs(state.γ, weights)
        val x = zeros[Double](K)
        x.update(chosen, reward/p(chosen))
        weights *= exp((state.γ * x) / K.toDouble)

        state.copy(weights = weights, counts = counts)
      }

      private def probs(γ: Double, weights: Vector[Double]): Vector[Double] = {
        val K = weights.size  // #arms
        ((1 - γ) * (weights / sum(weights))) + (γ / K)
      }
    }
  }
}
