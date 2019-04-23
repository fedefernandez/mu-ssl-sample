package higherkindness.mu.protocols

import higherkindness.mu.rpc.protocol._

@service(Protobuf) trait SampleService[F[_]] {
  def getGreet(request: SampleRequest): F[SampleResponse]
}