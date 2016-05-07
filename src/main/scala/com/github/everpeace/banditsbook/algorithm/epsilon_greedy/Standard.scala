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

package com.github.everpeace.banditsbook.algorithm.epsilon_greedy

import breeze.linalg.argmax
import breeze.stats.distributions.{Bernoulli, Rand, RandBasis}
import breeze.storage.Zero
import com.github.everpeace.banditsbook.algorithm.Algorithm
import com.github.everpeace.banditsbook.arm.Arm

import scala.collection.immutable.Seq
import scala.reflect.ClassTag

/**
  * see: http://www.cs.nyu.edu/~mohri/pub/bandit.pdf
  */
object Standard {

  import breeze.linalg.Vector
  import Vector._

  case class State(ε: Double, counts: Vector[Int], expectations: Vector[Double])

  def Algorithm(ε: Double)(implicit zeroDouble: Zero[Double], zeroInt: Zero[Int], tag: ClassTag[Double], rand: RandBasis = Rand)
  = new Algorithm[Double, State] {

    override def initialState(arms: Seq[Arm[Double]]): State =
      State(ε, zeros[Int](arms.size), zeros[Double](arms.size))

    override def selectArm(arms: Seq[Arm[Double]], state: State): Int =
      Bernoulli.distribution(state.ε).draw() match {
        case true =>
          // Exploit
          argmax(state.expectations)
        case false =>
          // Explore
          Rand.randInt(state.expectations.size).draw()
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
