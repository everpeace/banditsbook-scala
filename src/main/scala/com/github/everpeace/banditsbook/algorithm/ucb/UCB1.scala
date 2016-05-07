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

import breeze.numerics.sqrt
import breeze.stats.distributions.{Rand, RandBasis}
import breeze.storage.Zero
import com.github.everpeace.banditsbook.algorithm.Algorithm
import com.github.everpeace.banditsbook.arm.Arm

import scala.collection.immutable.Seq
import scala.reflect.ClassTag

/**
  * http://www.cs.mcgill.ca/~vkules/bandits.pdf
  */
object UCB1 {

  import breeze.linalg._
  import Vector._

  case class State(counts: Vector[Int], expectations: Vector[Double])

  def Algorithm(implicit zeroReward: Zero[Double], zeroInt: Zero[Int], tag: ClassTag[Double], rand: RandBasis = Rand)
  = {
    new Algorithm[Double, State] {

      override def initialState(arms: Seq[Arm[Double]]): State = State(
        zeros(arms.size), zeros(arms.size)
      )

      override def selectArm(arms: Seq[Arm[Double]], state: State): Int = {
        val counts = state.counts
        val expectations = state.expectations
        val step = sum(counts)
        val factor = fill(counts.size)(2 * scala.math.log(step))
        val bonus = sqrt(factor / counts.map(_.toDouble))
        val score = expectations + bonus
        argmax(score)
      }

      override def updateState(arms: Seq[Arm[Double]], state: State, chosen: Int, reward: Double): State = {
        val counts = state.counts
        val expectations = state.expectations
        val count = counts(chosen) + 1
        counts.update(chosen, count)

        val expectation = (((count - 1) / count.toDouble) * expectations(chosen)) + ((1 / count.toDouble) * reward)
        expectations.update(chosen, expectation)

        state.copy(counts = counts, expectations = expectations)
      }
    }
  }
}
