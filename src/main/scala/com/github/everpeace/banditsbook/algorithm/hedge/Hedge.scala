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

package com.github.everpeace.banditsbook.algorithm.hedge

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
  * http://www.dklevine.com/archive/refs4462.pdf
  */
object Hedge {

  case class State(η: Double, counts: Vector[Int], gains: Vector[Double])

  def Algorithm(η: Double)(implicit zeroReward: Zero[Double], zeroInt: Zero[Int], tag: ClassTag[Double], rand: RandBasis = Rand)
  = {
    require(η > 0, "η must be positive.")
    new Algorithm[Double, State] {

      override def initialState(arms: Seq[Arm[Double]]): State = State(
        η, zeros(arms.size), zeros(arms.size)
      )

      override def selectArm(arms: Seq[Arm[Double]], state: State): Int = {
        val gains = state.gains
        val η = state.η
        val p = exp(gains / η) / sum(exp(gains / η))
        CategoricalDistribution(p).draw
      }

      override def updateState(arms: Seq[Arm[Double]], state: State, chosen: Int, reward: Double): State = {
        val counts = state.counts
        val gains = state.gains

        val count = counts(chosen) + 1
        counts.update(chosen, count)

        val expectation = gains(chosen) + reward
        gains.update(chosen, expectation)

        state.copy(counts = counts, gains = gains)
      }
    }
  }
}
