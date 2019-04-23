package higherkindness.mu.example

import cats.effect._
import higherkindness.mu.protocols._

class SampleServiceImpl extends SampleService[IO] {
  def getGreet(request: SampleRequest): IO[SampleResponse] =
    for {
      _ <- IO(println(s"Receiving request $request"))
      response = SampleResponse(s"Hello ${request.name}")
      _ <- IO(println(s"Generating response $response"))
    } yield response
}