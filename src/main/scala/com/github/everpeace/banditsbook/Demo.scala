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

package com.github.everpeace.banditsbook

trait DemoBase {
  // As of breeze 0.12, configuring random seed is difficult.
  // ref: https://github.com/scalanlp/breeze/issues/492
  //      https://github.com/scalanlp/breeze/issues/493
  // below will be available in future release.
  //
  // import breeze.stats.distributions.RandBasis
  //  RandBasis.withSeed(1.0)

  // please import breeze.linalg._ first!
  import breeze.linalg.{softmax => brz_softmax}

  // Several Arm type is defined in arm package.
  import arm._

  val arm1 = BernoulliArm(0.2)
  arm1.draw()
  arm1.draw()

  val arm2 = NormalArm(10.0, 1.0)
  arm2.draw()
  arm2.draw()

  val arm3 = BernoulliArm(0.2)
  arm3.draw()
  arm3.draw()

  // create algorithm instances
  import algorithm._

  val algo1 = epsilon_greedy.Standard.Algorithm(ε = 0.1d)
  val algo2 = softmax.Standard.Algorithm(τ = 1.0d)
  val algo3 = ucb.UCB1.Algorithm
  val algo4 = exp3.Exp3.Algorithm(γ = 0.2)
  val algos = Seq(algo1, algo2, algo3, algo4)

  val arms = scala.collection.immutable.Seq(arm1, arm2, arm3)
  val t = 1000
  val sep = ","

}

object Demo extends DemoBase with App {
  def t_times(f: => Unit) = for { t <- 0 until t} { f }

  println()

  println("Standard Epsilon Greedy algorithm result")
  var algo1State = algo1.initialState(arms)
  t_times {
    val chosen_arm = algo1.selectArm(arms, algo1State)
    val reward = arms(chosen_arm).draw
    algo1State = algo1.updateState(arms, algo1State, chosen_arm, reward)
  }
  println(s"      counts: [${algo1State.counts.valuesIterator.mkString(sep)}]")
  println(s"expectations: [${algo1State.expectations.valuesIterator.mkString(sep)}]")

  println()

  println("Standard Softmax algorithm result")
  var algo2State = algo2.initialState(arms)
  t_times {
    val chosen_arm = algo2.selectArm(arms, algo2State)
    val reward = arms(chosen_arm).draw
    algo2State = algo2.updateState(arms, algo2State, chosen_arm, reward)
  }
  println(s"      counts: [${algo2State.counts.valuesIterator.mkString(sep)}]")
  println(s"expectations: [${algo2State.expectations.valuesIterator.mkString(sep)}]")

  println()

  println("UCB1 algorithm result")
  var algo3State = algo3.initialState(arms)
  t_times {
    val chosen_arm = algo3.selectArm(arms, algo3State)
    val reward = arms(chosen_arm).draw
    algo3State = algo3.updateState(arms, algo3State, chosen_arm, reward)
  }
  println(s"      counts: [${algo3State.counts.valuesIterator.mkString(sep)}]")
  println(s"expectations: [${algo3State.expectations.valuesIterator.mkString(sep)}]")

  println()

  println("Exp3 algorithm result")
  var algo4State = algo4.initialState(arms)
  t_times {
    val chosen_arm = algo4.selectArm(arms, algo4State)
    val reward = arms(chosen_arm).draw
    algo4State = algo4.updateState(arms, algo4State, chosen_arm, reward)
  }
  println(s" counts: [${algo4State.counts.valuesIterator.mkString(sep)}]")
  println(s"weights: [${algo4State.weights.valuesIterator.mkString(sep)}]")

  println()
}


object DemoMonadic extends DemoBase with App {
  import algorithm._
  import arm._

  // algorithm behavior is also implemented by State Monad (by using cats)
  import cats.data.State
  import State._

  // simple driver here.
  object SimpleAlgorithmDriver {
    private def simulation[S](algo: Algorithm[Double, S], n: Int) = {
      def trial =
        for {
          chosenArm <- algo.selectArm
          reward = chosenArm.draw()
          _ <- algo.updateState(chosenArm, reward)
        } yield ()

      def trials(n: Int): State[(Seq[Arm[Double]], S), Unit] = n match {
        case 0 => pure(())
        case n => for {
          _ <- trials(n - 1)
          _ <- trial
        } yield ()
      }

      trials(n)
    }

    def run[S](algo: Algorithm[Double, S], nStep: Int) =
      simulation(algo, nStep).runS((arms, algo.initialState(arms))).value
  }

  println()

  println("Standard Epsilon Greedy algorithm result")
  val algo1Result = SimpleAlgorithmDriver.run(algo1, t)
  println(s"      counts: [${algo1Result._2.counts.valuesIterator.mkString(sep)}]")
  println(s"expectations: [${algo1Result._2.expectations.valuesIterator.mkString(sep)}]")

  println()

  println("Standard Softmax algorithm result")
  val algo2Result = SimpleAlgorithmDriver.run(algo2, t)
  println(s"      counts: [${algo2Result._2.counts.valuesIterator.mkString(sep)}]")
  println(s"expectations: [${algo2Result._2.expectations.valuesIterator.mkString(sep)}]")

  println()

  println("UCB1 algorithm result")
  val algo3Result = SimpleAlgorithmDriver.run(algo3, t)
  println(s"      counts: [${algo3Result._2.counts.valuesIterator.mkString(sep)}]")
  println(s"expectations: [${algo3Result._2.expectations.valuesIterator.mkString(sep)}]")

  println()

  println("Exp3 algorithm result")
  val algo4Result = SimpleAlgorithmDriver.run(algo4, t)
  println(s" counts: [${algo4Result._2.counts.valuesIterator.mkString(sep)}]")
  println(s"weights: [${algo4Result._2.weights.valuesIterator.mkString(sep)}]")

  println()
}
