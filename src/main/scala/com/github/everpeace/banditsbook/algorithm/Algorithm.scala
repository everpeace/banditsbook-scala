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

import breeze.storage.Zero
import cats.data.{State => CState}
import com.github.everpeace.banditsbook.arm.Arm

import scala.reflect.ClassTag


abstract class Algorithm[Reward: ClassTag: Zero, AlgorithmState] {
  import cats.data.{State => CState}
  import CState._

  def initialState(arms: Seq[Arm[Reward]]): AlgorithmState
  def selectArm(arms: Seq[Arm[Reward]], state: AlgorithmState): Int
  def updateState(arms: Seq[Arm[Reward]], state: AlgorithmState, chosen: Int, reward: Reward): AlgorithmState

  def selectArm: CState[(Seq[Arm[Reward]], AlgorithmState), Arm[Reward]] =
    inspect {
      case (arms, state) =>
        arms(selectArm(arms, state))
    }

  def updateState(chosenArm:Arm[Reward], reward: Reward): CState[(Seq[Arm[Reward]], AlgorithmState), AlgorithmState] =
    inspect {
      case (arms, state) =>
        updateState(arms, state, arms.indexOf(chosenArm), reward)
    }
}
