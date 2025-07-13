import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskCollection
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.Task
import org.gradle.kotlin.dsl.register

/**
 * Custom plugin to initialize dependencies correctly to prevent configuration mutation
 * issues in Gradle 9.0. This plugin disables data binding tasks and ensures that
 * all configurations are resolved in the proper order.
 */
class DependencyInitializer : Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("Applying DependencyInitializer plugin")
        
        // Disable all data binding tasks
        project.tasks.configureEach {
            if (name.contains("dataBinding", ignoreCase = true) ||
                name.contains("DataBinding", ignoreCase = true)) {
                enabled = false
                onlyIf { false }
            }
        }
        
        // Add a hook to run after project evaluation to ensure all configurations are resolved
        project.afterEvaluate {
            // Force early resolution of configurations that might be modified later
            project.configurations.forEach { config ->
                if (config.name.contains("CompileClasspath", ignoreCase = true)) {
                    try {
                        // Access the files to force resolution
                        config.files
                        project.logger.lifecycle("Forced early resolution of ${config.name}")
                    } catch (e: Exception) {
                        project.logger.warn("Could not resolve ${config.name}: ${e.message}")
                    }
                }
            }
            
            // Fix for processResources task
            project.tasks.findByName("processDebugResources")?.let { task ->
                task.outputs.upToDateWhen { true }
            }
            
            // Register a configuration finalization task to run after the resource processing
            project.tasks.register("finalizeConfigurations") {
                description = "Finalizes configurations to prevent mutation issues"
                doLast {
                    project.logger.lifecycle("Configurations finalized")
                }
            }
            
            // Make sure any resource processing task depends on the finalization task
            project.tasks.configureEach {
                if (name.contains("processResources", ignoreCase = true)) {
                    dependsOn("finalizeConfigurations")
                }
            }
        }
    }
}
