package nl.knaw.dans.lib.dataverse.model

case class WorkflowStep(stepType: String,
                        provider: String,
                        parameters: Map[String, String])
