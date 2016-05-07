package com.github.everpeace.banditsbook

import breeze.linalg._
import breeze.stats.distributions.{ApacheDiscreteDistribution, Rand}
import org.apache.commons.math3.distribution.{EnumeratedIntegerDistribution, AbstractIntegerDistribution}

package object algorithm {

  def CategoricalDistribution(probs: Vector[Double]): Rand[Int] = new ApacheDiscreteDistribution {
      override protected val inner: AbstractIntegerDistribution
      = new EnumeratedIntegerDistribution((0 until probs.size).toArray, probs.copy.valuesIterator.toArray)
    }

}
