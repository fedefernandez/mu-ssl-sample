package higherkindness.mu.example

import java.io.{File, FileInputStream}
import java.security.cert.{CertificateFactory, X509Certificate}

import cats.effect._
import higherkindness.mu.protocols._
import higherkindness.mu.rpc.server._
import higherkindness.mu.rpc.server.netty.SetSslContext
import io.grpc.netty.GrpcSslContexts
import io.netty.handler.ssl.{ClientAuth, SslContext, SslProvider}

object RPCServerWithSSLContext extends App {

  implicit val EC: scala.concurrent.ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  implicit val timer: Timer[cats.effect.IO]     = IO.timer(EC)
  implicit val cs: ContextShift[cats.effect.IO] = IO.contextShift(EC)

  implicit val service: SampleService[IO] = new SampleServiceImpl

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
    GrpcSslContexts
      .configure(
        GrpcSslContexts.forServer(serverCertFile, serverPrivateKeyFile),
        SslProvider.OPENSSL)
      .trustManager(cert)
      .clientAuth(ClientAuth.REQUIRE)
      .build()

  (for {
    sslContext <- generateTrustedCA.map(generateSSLContext)
    grpcConfig <- SampleService.bindService[IO].map(AddService(_))
    grpcServer <- GrpcServer.netty[IO](8080, SetSslContext(sslContext) :: grpcConfig :: Nil)
    _          <- GrpcServer.server[IO](grpcServer)
  } yield ()).unsafeRunSync()
}
