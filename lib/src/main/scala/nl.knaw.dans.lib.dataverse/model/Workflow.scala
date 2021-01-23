package nl.knaw.dans.lib.dataverse.model

case class Workflow(id: Int, name: String, steps: List[WorkflowStep])
