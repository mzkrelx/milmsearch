package utils

import org.scalatest.BeforeAndAfterAll
import org.scalatest.Suite
import akka.actor.ActorSystem
import akka.testkit.ImplicitSender
import akka.testkit.TestKit

abstract class TestKitSpec(name: String) extends TestKit(ActorSystem(name))
    with ImplicitSender
    with Suite
    with BeforeAndAfterAll {

  override def afterAll() {
    system.shutdown()
  }
}
