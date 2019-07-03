package org.jetbrains.plugins.scala
package lang
package resolve
package processor

import com.intellij.openapi.util.Key
import com.intellij.psi._
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.ScalaPsiUtil.getCompanionModule
import org.jetbrains.plugins.scala.lang.psi.api.base.AuxiliaryConstructor
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScTypeAlias
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.typedef.ScTypeDefinition
import org.jetbrains.plugins.scala.lang.psi.types.recursiveUpdate.ScSubstitutor
import org.jetbrains.plugins.scala.lang.psi.types.{PhysicalMethodSignature, TermSignature}
import org.jetbrains.plugins.scala.lang.resolve.ScalaResolveState.ResolveStateExt
import org.jetbrains.plugins.scala.lang.resolve.processor.precedence.{MappedTopPrecedenceHolder, PrecedenceHelper, TopPrecedenceHolder}

import scala.collection.{Set, mutable}

object CompletionProcessor {

  private def getSignature(
    element:     PsiNamedElement,
    substitutor: ScSubstitutor
  ): Option[TermSignature] = element match {
    case method: PsiMethod            => Option(new PhysicalMethodSignature(method, substitutor))
    case _: ScTypeAlias | _: PsiClass => None
    case _                            => Option(TermSignature(element, substitutor))
  }

  private def createResolveResults(candidates: Seq[(PsiNamedElement, Boolean)],
                                   substitutor: ScSubstitutor,
                                   implcitConversion: Option[ScalaResolveResult])
                                  (implicit state: ResolveState): Seq[ScalaResolveResult] = {
    candidates.map {
      case (element, isNamedParameter) =>
        new ScalaResolveResult(element, substitutor,
          renamed            = state.renamed,
          implicitConversion = implcitConversion,
          isNamedParameter   = isNamedParameter,
          fromType           = state.fromType,
          importsUsed        = state.importsUsed,
          prefixCompletion   = state.isPrefixCompletion)
    }
  }
}

class CompletionProcessor(override val kinds: Set[ResolveTargets.Value],
                          override val getPlace: PsiElement,
                          val isImplicit: Boolean = false)
  extends BaseProcessor(kinds)(getPlace) with PrecedenceHelper {

  private object CompletionStrategy extends NameUniquenessStrategy {

    override def computeHashCode(result: ScalaResolveResult): Int =
      31 * result.isNamedParameter.hashCode() + super.computeHashCode(result)

    override def equals(left: ScalaResolveResult, right: ScalaResolveResult): Boolean =
      left.isNamedParameter == right.isNamedParameter && super.equals(left, right)
  }

  override def nameUniquenessStrategy: NameUniquenessStrategy = CompletionStrategy

  override protected val holder: TopPrecedenceHolder = new MappedTopPrecedenceHolder(nameUniquenessStrategy)

  private val signatures = mutable.HashSet[TermSignature]()

  val includePrefixImports: Boolean = true

  protected val forName: Option[String] = None

  protected def postProcess(result: ScalaResolveResult): Unit = {
  }

  override protected def isCheckForEqualPrecedence = false

  import CompletionProcessor._

  override protected def execute(namedElement: PsiNamedElement)
                                (implicit state: ResolveState): Boolean = {
    if (forName.exists(_ != namedElement.name)) return true

    val candidates = findCandidates(namedElement)
    if (candidates.isEmpty) return true

    val substitutor = state.substitutor
    val implicitConversion = state.implicitConversion

    val resolveResults = createResolveResults(candidates, substitutor, implicitConversion)
    val maybeSignature = getSignature(namedElement, substitutor)

    resolveResults.filter {
      case _ if implicitConversion.isDefined && maybeSignature.isDefined => implicitCase(maybeSignature.get)
      case result => regularCase(result, maybeSignature)
    }.foreach(addResult)

    true
  }

  private def findCandidates(namedElement: PsiNamedElement)
                            (implicit state: ResolveState): Seq[(PsiNamedElement, Boolean)] = {
    val results = namedElement match {
      case AuxiliaryConstructor(_) =>
        Seq.empty // do not add constructor
      case definition: ScTypeDefinition =>
        (Seq(definition) ++ getCompanionModule(definition)).map((_, false))
      case _ =>
        val isNamedParameter = state.isNamedParameter
        Seq((namedElement, isNamedParameter))
    }

    results.filter {
      case (e, _) => ResolveUtils.kindMatches(e, kinds)
    }
  }

  override def changedLevel: Boolean = {
    collectFromLevelSet()

    if (!levelSet.isEmpty) {
      uniqueNamesSet.addAll(levelUniqueNamesSet)
      levelSet.clear()
      levelUniqueNamesSet.clear()
    }

    super.changedLevel
  }

  override def candidatesS: Set[ScalaResolveResult] = {
    collectFromLevelSet()
    candidatesSet
  }


  private def collectFromLevelSet(): Unit = {
    val iterator = levelSet.iterator()
    while (iterator.hasNext) {
      val next = iterator.next()
      postProcess(next)
      candidatesSet += next
    }
  }

  private def regularCase(result: ScalaResolveResult,
                          maybeSignature: Option[TermSignature]): Boolean = {
    signatures ++= maybeSignature

    if (result.prefixCompletion) {
      levelSet.remove(result)
    }

    !levelSet.contains(result)
  }

  private def implicitCase(signature: TermSignature): Boolean =
    signatures.add(signature)
}
