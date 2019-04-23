package higherkindness.mu.example

import cats.effect._
import higherkindness.mu.protocols._
import higherkindness.mu.rpc.server._

object RPCServer extends App {

  implicit val EC: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  implicit val timer: Timer[cats.effect.IO]     = IO.timer(EC)
  implicit val cs: ContextShift[cats.effect.IO] = IO.contextShift(EC)

  implicit val service: SampleService[IO] = new SampleServiceImpl

  val ts: UseTransportSecurity = UseTransportSecurity(
    new java.io.File("certs/certificate.pem"),
    new java.io.File("certs/key.pem"))

  (for {
    grpcConfig <- SampleService.bindService[IO].map(AddService(_))
    grpcServer <- GrpcServer.netty[IO](8080, ts :: grpcConfig :: Nil)
    _          <- GrpcServer.server[IO](grpcServer)
  } yield ()).unsafeRunSync()
}
