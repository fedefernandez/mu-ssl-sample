package higherkindness.mu.example

import java.io.{File, FileInputStream}
import java.security.cert.{CertificateFactory, X509Certificate}

import cats.effect._
import higherkindness.mu.protocols._
import higherkindness.mu.rpc._
import higherkindness.mu.rpc.channel.OverrideAuthority
import higherkindness.mu.rpc.channel.netty._
import io.grpc.netty.{GrpcSslContexts, NegotiationType}
import io.netty.handler.ssl.SslContext

object RPCClientWithSSLContext extends App {

  implicit val EC: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  implicit val timer: Timer[cats.effect.IO]     = IO.timer(EC)
  implicit val cs: ContextShift[cats.effect.IO] = IO.contextShift(EC)

  val serverCertFile: File                      = new File("certs/server1.pem")
  val serverPrivateKeyFile: File                = new File("certs/server1.key")

  def generateTrustedCA: IO[X509Certificate] = for {
    caFactory   <- IO(CertificateFactory.getInstance("X.509"))
    certificate <- IO(caFactory.generateCertificate(new FileInputStream("certs/ca.pem")))
    x509Cert    <- IO.fromEither {
      certificate match {
        case c: X509Certificate => Right(c)
        case _                  => Left(new IllegalStateException("Invalid certificate type: " + certificate.getType))
      }
    }
  } yield x509Cert

  def generateSSLContext(cert: X509Certificate): SslContext =
    GrpcSslContexts.forClient
      .keyManager(serverCertFile, serverPrivateKeyFile)
      .trustManager(cert)
      .build()

  def client(context: SslContext): Resource[IO, SampleService[IO]] = {
    val channelInterpreter: NettyChannelInterpreter = new NettyChannelInterpreter(
      initConfig = ChannelForAddress("localhost", 8080),
      configList = List(OverrideAuthority("foo.test.google.fr")),
      nettyConfigList = List(
        NettyNegotiationType(NegotiationType.TLS),
        NettySslContext(context))
    )
    SampleService.clientFromChannel[IO](IO(channelInterpreter.build))
  }

  val request: SampleRequest = SampleRequest("fede")

  (for {
    _           <- IO(println(s"Calling server with request $request"))
    sslContext  <- generateTrustedCA.map(generateSSLContext)
    result      <- client(sslContext).use(_.getGreet(request))
    _           <- IO(println(s"Response $result"))
  } yield ()).unsafeRunSync()

}
