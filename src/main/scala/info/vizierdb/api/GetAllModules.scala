/* -- copyright-header:v2 --
 * Copyright (C) 2017-2021 University at Buffalo,
 *                         New York University,
 *                         Illinois Institute of Technology.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -- copyright-header:end -- */
package info.vizierdb.api

import scalikejdbc.DB
import play.api.libs.json._
import info.vizierdb.util.HATEOAS
import info.vizierdb.VizierAPI
import info.vizierdb.catalog.{ Branch, Workflow, Module }
import org.mimirdb.api.Response
import info.vizierdb.types.Identifier
import info.vizierdb.api.response._
import info.vizierdb.api.handler.SimpleHandler

object GetAllModulesHandler
  extends SimpleHandler
{
  def handle(pathParameters: Map[String, JsValue]): Response =
  {
    val projectId = pathParameters("projectId").as[Long]
    val branchId = pathParameters("branchId").as[Long]
    val workflowId = pathParameters.get("workflowId").map { _.as[Long] }
    DB.readOnly { implicit session => 
      val workflowMaybe: Option[Workflow] = 
        workflowId match {
          case Some(workflowIdActual) => 
            Workflow.getOption(projectId, branchId, workflowIdActual)
          case None => 
            Branch.getOption(projectId, projectId).map { _.head }
        } 
      workflowMaybe match {
        case Some(workflow) => RawJsonResponse(
          Json.toJson(
            Module.describeAll(
              projectId = projectId,
              branchId = branchId, 
              workflowId = workflow.id,
              workflow.cellsAndModulesInOrder
            )
          )
        )
        case None => NoSuchEntityResponse()
      }
    }
  } 
}

