package higherkindness.mu.sample

import cats.effect._
import higherkindness.mu.rpc._
import higherkindness.mu.protocols._
import higherkindness.mu.rpc.channel.netty._
import io.grpc.netty.NegotiationType

object RPCClient extends App {

  implicit val EC: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  implicit val timer: Timer[cats.effect.IO]     = IO.timer(EC)
  implicit val cs: ContextShift[cats.effect.IO] = IO.contextShift(EC)

  def clientWithNil: Resource[IO, SampleService[IO]] =
    SampleService.client[IO](ChannelForAddress("localhost", 8080), Nil)

  def clientWithTLSParam: Resource[IO, SampleService[IO]] = {
    val channelInterpreter: NettyChannelInterpreter = new NettyChannelInterpreter(
      ChannelForAddress("localhost", 8080),
      Nil,
      List(NettyNegotiationType(NegotiationType.TLS))
    )
    SampleService.clientFromChannel[IO](IO(channelInterpreter.build))
  }

  val request: SampleRequest = SampleRequest("fede")

  val app: IO[Unit] = for {
    _       <- IO(println(s"Calling server with request $request"))
    result  <- clientWithNil.use(_.getGreet(request))
    _       <- IO(println(s"Response $result"))
  } yield ()

  app.unsafeRunSync()

}
