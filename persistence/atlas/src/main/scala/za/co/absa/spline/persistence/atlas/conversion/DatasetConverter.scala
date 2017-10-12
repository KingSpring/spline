/*
 * Copyright 2017 Barclays Africa Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.spline.persistence.atlas.conversion

import za.co.absa.spline.model.{MetaDataset, op, Attribute}
import za.co.absa.spline.persistence.atlas.model._

/**
  * The object is responsible for conversion of [[za.co.absa.spline.model.MetaDataset Spline meta data sets]] to [[za.co.absa.spline.persistence.atlas.model.Dataset Atlas data sets]].
  */
object DatasetConverter {
  val datasetSuffix = "_Dataset"

  /**
    * The method converts [[za.co.absa.spline.model.MetaDataset Spline meta data sets]] to [[za.co.absa.spline.persistence.atlas.model.Dataset Atlas data sets]].
    * @param operations A sequence of [[za.co.absa.spline.model.op.Operation Spline operations]]
    * @param datasets A sequence of [[za.co.absa.spline.model.MetaDataset Spline meta data sets]]
    * @param attributes A sequence of [[za.co.absa.spline.model.Attribute Spline attributes]]
    * @return A sequence of [[za.co.absa.spline.persistence.atlas.model.Dataset Atlas data sets]]
    */
  def convert(operations: Seq[op.Operation], datasets : Seq[MetaDataset], attributes: Seq[Attribute]) : Seq[Dataset] = {
    val attributeMap = attributes.map(a => a.id -> a).toMap
    for (
      operation <- operations;
      dataset <- datasets.withFilter(d => d.id == operation.mainProps.output);
      name = operation.mainProps.name + datasetSuffix;
      qualifiedName = dataset.id;
      attributes = dataset.schema.attrs.map(i => AttributeConverter.convert(qualifiedName.toString, attributeMap(i)));
      translated = operation match {
        case op.Source(m, st, paths) =>
          val path = paths.mkString(", ")
          new EndpointDataset(name, qualifiedName, attributes, new FileEndpoint(path, path), EndpointType.file, EndpointDirection.input, st)
        case op.Destination(m, dt, path) => new EndpointDataset(name, qualifiedName, attributes, new FileEndpoint(path, path), EndpointType.file, EndpointDirection.output, dt)
        case _ => new Dataset(name, qualifiedName, attributes)
      };
      a = attributes.foreach(a => a.assingDataset(translated.getId))
    )
    yield translated
  }
}
