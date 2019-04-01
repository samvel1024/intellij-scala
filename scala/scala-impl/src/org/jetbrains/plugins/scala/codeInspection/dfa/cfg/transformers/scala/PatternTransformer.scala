package org.jetbrains.plugins.scala.codeInspection.dfa.cfg.transformers.scala

import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.{ScParenthesisedPattern, ScPattern, ScWildcardPattern}

import scala.annotation.tailrec

trait PatternTransformer { this: Transformer =>


  def buildPattern(pattern: ScPattern): Unit = pattern match {
    case wildcardPattern: ScWildcardPattern => buildWildcardPattern(wildcardPattern)
    case ScParenthesisedPattern(inner) => buildPattern(inner)
  }

  def buildWildcardPattern(wildcardPattern: ScWildcardPattern): Unit = {
    builder.pop()
  }

  def buildParenthesisedPattern(parenthesisedPattern: ScParenthesisedPattern): Unit = {

  }

  @tailrec
  final def canIgnorePattern(pattern: ScPattern): Boolean = pattern match {
    case _: ScWildcardPattern => true
    case ScParenthesisedPattern(inner) => canIgnorePattern(inner)
    case _ => false
  }
}
