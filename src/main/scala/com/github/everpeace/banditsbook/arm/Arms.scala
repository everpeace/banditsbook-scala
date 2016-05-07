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

package com.github.everpeace.banditsbook.arm

import breeze.stats.distributions.{Bernoulli, Gaussian}

trait Arms {

  def BernoulliArm(p: Double): Arm[Double] = Bernoulli.distribution(p).map {
    case true  => 1.0d
    case false => 0.0d
  }

  def NormalArm(μ: Double, σ: Double): Arm[Double]  = Gaussian.distribution(μ -> σ)

  def AdversarialArm(start: Int, activeStart: Int, activeEnd: Int) = new Arm[Double] {
    @volatile var t = start
    override def draw(): Double = {
      t += 1
      t match {
        case _t if _t < activeStart => 0.0d
        case _t if activeStart <= _t && _t <= activeEnd => 1.0
        case _t => 0.0d
      }
    }
  }
}
