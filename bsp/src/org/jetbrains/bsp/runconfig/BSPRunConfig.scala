package org.jetbrains.bsp.runconfig

import com.intellij.execution.{ExecutionException, Executor}
import com.intellij.execution.configurations.{ConfigurationFactory, ConfigurationType, RunConfiguration, RunConfigurationBase, RunProfileState, RuntimeConfigurationException}
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.{ConfigurationException, SettingsEditor}
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{ComponentWithBrowseButton, LabeledComponent, TextFieldWithBrowseButton}
import javax.swing.{Icon, JComponent, JPanel}
import org.jetbrains.bsp.Icons



class BSPRunConfigurationType extends ConfigurationType {
  override def getDisplayName: String = "BSP"

  override def getConfigurationTypeDescription: String = "BSP Run configuration"

  override def getIcon: Icon = Icons.BSP

  override def getId: String = "BSPRunConfiguration"

  override def getConfigurationFactories: Array[ConfigurationFactory] = Array(new BSPRunConfigurationFactory(this))
}

class BSPRunConfigurationFactory(typ: ConfigurationType) extends ConfigurationFactory(typ) {
  override def createTemplateConfiguration(project: Project): RunConfiguration = {
    new BSPRunConfiguration(project, this, typ.getDisplayName, 1)
  }
}

class BSPRunConfiguration protected (val project: Project, val factory: ConfigurationFactory, val name: String) extends RunConfigurationBase(project, factory, name) {
  override def getConfigurationEditor: SettingsEditor[_ <: RunConfiguration] = null

  // Created this constructor to avoid using the protected one above, the last argument is to be removed
  def this(project: Project, factory: ConfigurationFactory, name: String, i: Int){
    this(project, factory, name)
  }

  @throws[RuntimeConfigurationException]
  override def checkConfiguration(): Unit = {
  }

  @throws[ExecutionException]
  override def getState(executor: Executor, executionEnvironment: ExecutionEnvironment): RunProfileState = null
}


class BSPSettingsEditor extends SettingsEditor[BSPRunConfiguration] {
  private val myPanel = new JPanel
  private var myMainClass = null

  override protected def resetEditorFrom(demoRunConfiguration: BSPRunConfiguration): Unit = {
  }

  @throws[ConfigurationException]
  override protected def applyEditorTo(demoRunConfiguration: BSPRunConfiguration): Unit = {
  }

  override protected def createEditor: JComponent = myPanel

}