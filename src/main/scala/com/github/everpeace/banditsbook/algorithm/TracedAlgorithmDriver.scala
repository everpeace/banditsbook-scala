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

package com.github.everpeace.banditsbook.algorithm

import breeze.linalg.Vector
import breeze.linalg.Vector._
import breeze.storage.Zero
import cats.data.{State => CState}
import com.github.everpeace.banditsbook.arm._

import scala.reflect.ClassTag

object TracedAlgorithmDriver {

  // Note: breeze.linalg.Vector is mutable.
  final case class Trace[Reward: Zero](chosenArms: Vector[Int], counts: Vector[Int], rewards: Vector[Reward])

  final case class State[Reward, AlgorithmState](arms: Seq[Arm[Reward]], step: Int, horizon: Int,
                                                 algState: AlgorithmState, trace: Trace[Reward])
}

case class TracedAlgorithmDriver[Reward: Zero: ClassTag, AlgorithmState](algo: Algorithm[Reward, AlgorithmState])(implicit zeroInt: Zero[Int]) {
  import CState._
  import TracedAlgorithmDriver._

  private val incrementStep                          = modify[State[Reward, AlgorithmState]] { s => s.copy(step = s.step + 1) }
  private def setAlgState(s: AlgorithmState)         = modify[State[Reward, AlgorithmState]] { _.copy(algState = s) }
  private def updateTrace(a: Arm[Reward], r: Reward) = modify[State[Reward, AlgorithmState]] { s =>
    val step = s.step
    val chosen = s.arms.indexOf(a)
    val count = s.trace.counts(chosen)
    s.trace.chosenArms.update(step, chosen)
    s.trace.counts.update(chosen, count + 1)
    s.trace.rewards.update(step, r)
    s.copy()
  }

  // drive 'step' once.
  private val driveStep: CState[State[Reward, AlgorithmState], Unit] = for {
    state                   <- get[State[Reward, AlgorithmState]]
    chosenArm               =  algo.selectArm.runA((state.arms, state.algState)).value
    reward                  =  chosenArm.draw()
    newState                =  algo.updateState(chosenArm, reward).runA((state.arms, state.algState)).value
    _                       <- setAlgState(newState)
    _                       <- updateTrace(chosenArm, reward)
    _                       <- incrementStep
  } yield ()

  // drive 'step' $n times
  private def driveSteps(n: Int): CState[State[Reward, AlgorithmState], Unit] = n match {
    case 0 => pure( () ) // nop
    case _ => for {
      _ <- driveSteps(n - 1)
      _ <- driveStep
    } yield ()
  }

  /**
    * perform 'step' $steps times from initial state and return its final state.
    */
  final def run(arms: Seq[Arm[Reward]], horizon: Int, steps: Int): State[Reward, AlgorithmState] =
    runFrom(
      State(arms, 0, horizon,
            algo.initialState(arms),
            Trace[Reward](zeros(horizon), zeros[Int](arms.size), zeros[Reward](horizon))
      ),
      steps
    )
  final def run(arms: Seq[Arm[Reward]], horizon: Int): State[Reward, AlgorithmState] = run(arms, horizon, horizon)

  /**
    * resume the algorithm from given state and return its final state
    */
  final def runFrom(state: State[Reward, AlgorithmState], steps: Int): State[Reward, AlgorithmState] = {
    if ((state.horizon - state.step) <= steps)
      driveSteps(state.horizon - state.step).runS(state).value
    else
      driveSteps(steps).runS(state).value

  }
}
