package smithy4s.example

package object collision {
  type ReservedNameService[F[_]] = smithy4s.kinds.FunctorAlgebra[ReservedNameServiceGen, F]
  val ReservedNameService = ReservedNameServiceGen

  type MyList = smithy4s.example.collision.MyList.Type
  type MyMap = smithy4s.example.collision.MyMap.Type
  type MySet = smithy4s.example.collision.MySet.Type

}