package org.jetbrains.plugins.scala
package lang
package psi
package api
package expr

import org.jetbrains.plugins.scala.lang.psi.controlFlow.CfgBuildingBlockStatement


/**
* @author Alexander Podkhalyuzin
* Date: 06.03.2008
*/

trait ScBlockStatement extends ScalaPsiElement with CfgBuildingBlockStatement