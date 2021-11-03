package example

import cats.effect.IO
import cats.effect.unsafe.implicits.global
import sttp.client3.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client3._

import java.io.File

object Main extends App {
  private def createBasicRequest(fileName: String, repo: String, version: Int) =
    basicRequest.get(uri"https://github.com/nationalarchives/tdr-$repo/releases/download/v$version/$fileName")
      .response(asFile(new File(fileName)))


  def catsRequest(fileName: String, repo: String, version: Int) = AsyncHttpClientCatsBackend.resource[IO]().use { backend =>
    val req = createBasicRequest(fileName, repo, version)
    for {
      res <- backend.send(req)
      _ <- IO.print(res.body)
      _ <- IO.println(s"${res.statusText} ${res.code}")
    } yield ()
  }.unsafeRunSync()

  def syncRequest(fileName: String, repo: String, version: Int) = {
    val backend = HttpURLConnectionBackend()
    val req = createBasicRequest(fileName, repo, version)
    val res = backend.send(req)
    println(s"${res.body}${res.statusText} ${res.code}")
  }

  //Three identical files in the same GitHub release with different names.
  catsRequest("api-update.jar", "api-update", 28) //Left()Unauthorized 401
  catsRequest("file.jar", "api-update", 28) //Left()Unauthorized 401
  catsRequest("file", "api-update", 28) //Right(file)OK 200

  //Same but using the sync backend
  syncRequest("api-update.jar", "api-update", 28) //Left()Unauthorized 401
  syncRequest("file.jar", "api-update", 28) //Left()Unauthorized 401
  syncRequest("file", "api-update", 28) //Right(file)OK 200

  //Release in a different repo with a similar name which succeeds
  catsRequest("checksum.jar", "checksum", 18) //Right(checksum.jar)OK 200
  syncRequest("checksum.jar", "checksum", 18) //Right(checksum.jar)OK 200
}
