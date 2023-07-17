package smithy4s.example.guides.hello

import smithy4s.Hints
import smithy4s.Schema
import smithy4s.Service
import smithy4s.ShapeId
import smithy4s.StreamingSchema
import smithy4s.Transformation
import smithy4s.kinds.PolyFunction5
import smithy4s.kinds.toPolyFunction5.const5
import smithy4s.schema.Schema.unit

trait HelloWorldServiceGen[F[_, _, _, _, _]] {
  self =>

  def sayWorld(): F[Unit, Nothing, World, Nothing, Nothing]

  def transform: Transformation.PartiallyApplied[HelloWorldServiceGen[F]] = Transformation.of[HelloWorldServiceGen[F]](this)
}

object HelloWorldServiceGen extends Service.Mixin[HelloWorldServiceGen, HelloWorldServiceOperation] {

  val id: ShapeId = ShapeId("smithy4s.example.guides.hello", "HelloWorldService")
  val version: String = "1.0.0"

  val hints: Hints = Hints(
    alloy.SimpleRestJson(),
    smithy.api.Cors(origin = smithy.api.NonEmptyString("http://mysite.com"), maxAge = 600, additionalAllowedHeaders = Some(List(smithy.api.NonEmptyString("Authorization"))), additionalExposedHeaders = Some(List(smithy.api.NonEmptyString("X-Smithy4s")))),
  )

  def apply[F[_]](implicit F: Impl[F]): F.type = F

  object ErrorAware {
    def apply[F[_, _]](implicit F: ErrorAware[F]): F.type = F
    type Default[F[+_, +_]] = Constant[smithy4s.kinds.stubs.Kind2[F]#toKind5]
  }

  val endpoints: IndexedSeq[smithy4s.Endpoint[HelloWorldServiceOperation, _, _, _, _, _]] = IndexedSeq(
    HelloWorldServiceOperation.SayWorld,
  )

  def input[I, E, O, SI, SO](op: HelloWorldServiceOperation[I, E, O, SI, SO]): I = op.input
  def ordinal[I, E, O, SI, SO](op: HelloWorldServiceOperation[I, E, O, SI, SO]): Int = op.ordinal
  class Constant[P[-_, +_, +_, +_, +_]](value: P[Any, Nothing, Nothing, Nothing, Nothing]) extends HelloWorldServiceOperation.Transformed[HelloWorldServiceOperation, P](reified, const5(value))
  type Default[F[+_]] = Constant[smithy4s.kinds.stubs.Kind1[F]#toKind5]
  def reified: HelloWorldServiceGen[HelloWorldServiceOperation] = HelloWorldServiceOperation.reified
  def mapK5[P[_, _, _, _, _], P1[_, _, _, _, _]](alg: HelloWorldServiceGen[P], f: PolyFunction5[P, P1]): HelloWorldServiceGen[P1] = new HelloWorldServiceOperation.Transformed(alg, f)
  def fromPolyFunction[P[_, _, _, _, _]](f: PolyFunction5[HelloWorldServiceOperation, P]): HelloWorldServiceGen[P] = new HelloWorldServiceOperation.Transformed(reified, f)
  def toPolyFunction[P[_, _, _, _, _]](impl: HelloWorldServiceGen[P]): PolyFunction5[HelloWorldServiceOperation, P] = HelloWorldServiceOperation.toPolyFunction(impl)

}

sealed trait HelloWorldServiceOperation[Input, Err, Output, StreamedInput, StreamedOutput] {
  def run[F[_, _, _, _, _]](impl: HelloWorldServiceGen[F]): F[Input, Err, Output, StreamedInput, StreamedOutput]
  def ordinal: Int
  def input: Input
}

object HelloWorldServiceOperation {

  object reified extends HelloWorldServiceGen[HelloWorldServiceOperation] {
    def sayWorld() = SayWorld()
  }
  class Transformed[P[_, _, _, _, _], P1[_ ,_ ,_ ,_ ,_]](alg: HelloWorldServiceGen[P], f: PolyFunction5[P, P1]) extends HelloWorldServiceGen[P1] {
    def sayWorld() = f[Unit, Nothing, World, Nothing, Nothing](alg.sayWorld())
  }

  def toPolyFunction[P[_, _, _, _, _]](impl: HelloWorldServiceGen[P]): PolyFunction5[HelloWorldServiceOperation, P] = new PolyFunction5[HelloWorldServiceOperation, P] {
    def apply[I, E, O, SI, SO](op: HelloWorldServiceOperation[I, E, O, SI, SO]): P[I, E, O, SI, SO] = op.run(impl) 
  }
  final case class SayWorld() extends HelloWorldServiceOperation[Unit, Nothing, World, Nothing, Nothing] {
    def run[F[_, _, _, _, _]](impl: HelloWorldServiceGen[F]): F[Unit, Nothing, World, Nothing, Nothing] = impl.sayWorld()
    def ordinal = 0
    def input: Unit = ()
  }
  object SayWorld extends smithy4s.Endpoint[HelloWorldServiceOperation,Unit, Nothing, World, Nothing, Nothing] {
    val id: ShapeId = ShapeId("smithy4s.example.guides.hello", "SayWorld")
    val input: Schema[Unit] = unit.addHints(smithy4s.internals.InputOutput.Input.widen)
    val output: Schema[World] = World.schema.addHints(smithy4s.internals.InputOutput.Output.widen)
    val streamedInput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val streamedOutput: StreamingSchema[Nothing] = StreamingSchema.nothing
    val hints: Hints = Hints(
      smithy.api.Http(method = smithy.api.NonEmptyString("GET"), uri = smithy.api.NonEmptyString("/hello"), code = 200),
      smithy.api.Readonly(),
    )
    def wrap(input: Unit) = SayWorld()
    override val errorable: Option[Nothing] = None
  }
}

