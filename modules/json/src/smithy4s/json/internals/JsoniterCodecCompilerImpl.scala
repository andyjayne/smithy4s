/*
 *  Copyright 2021-2022 Disney Streaming
 *
 *  Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     https://disneystreaming.github.io/TOST-1.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package smithy4s.json
package internals

import smithy4s.HintMask
import smithy4s.schema._

private[smithy4s] case class JsoniterCodecCompilerImpl(
    maxArity: Int,
    explicitNullEncoding: Boolean,
    flexibleCollectionsSupport: Boolean,
    infinitySupport: Boolean,
    hintMask: Option[HintMask]
) extends CachedSchemaCompiler.Impl[JCodec]
    with JsoniterCodecCompiler {

  type Aux[A] = JCodec[A]

  def withMaxArity(max: Int): JsoniterCodecCompiler = copy(maxArity = max)

  def withExplicitNullEncoding(
      explicitNullEncoding: Boolean
  ): JsoniterCodecCompiler =
    copy(explicitNullEncoding = explicitNullEncoding)

  def withHintMask(hintMask: HintMask): JsoniterCodecCompiler =
    copy(hintMask = Some(hintMask))

  def withFlexibleCollectionsSupport(
      flexibleCollectionsSupport: Boolean
  ): JsoniterCodecCompiler =
    copy(flexibleCollectionsSupport = flexibleCollectionsSupport)

  def withInfinitySupport(infinitySupport: Boolean): JsoniterCodecCompiler =
    copy(infinitySupport = infinitySupport)

  def fromSchema[A](schema: Schema[A], cache: Cache): JCodec[A] = {
    val visitor = new SchemaVisitorJCodec(
      maxArity,
      explicitNullEncoding,
      infinitySupport,
      flexibleCollectionsSupport,
      cache
    )
    val amendedSchema =
      hintMask
        .map(mask => schema.transformHintsTransitively(mask.apply))
        .getOrElse(schema)
    amendedSchema.compile(visitor)
  }

}

private[smithy4s] object JsoniterCodecCompilerImpl {

  val defaultJsoniterCodecCompiler: JsoniterCodecCompiler =
    JsoniterCodecCompilerImpl(
      maxArity = JsoniterCodecCompiler.defaultMaxArity,
      explicitNullEncoding = false,
      infinitySupport = false,
      flexibleCollectionsSupport = false,
      hintMask = Some(JsoniterCodecCompiler.defaultHintMask)
    )

}