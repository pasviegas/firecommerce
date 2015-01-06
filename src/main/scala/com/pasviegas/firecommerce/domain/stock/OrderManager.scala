// Copyright (C) 2011-2012 the original author or authors.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.pasviegas.firecommerce.domain.stock

import akka.actor.{ ActorRef, Props }
import com.firebase.client.{ DataSnapshot, Firebase }
import com.pasviegas.firecommerce.domain.stock.messages.{ CannotCompleteOrder, CompleteOrder, MakeOrder }
import com.pasviegas.firecommerce.domain.stock.models.Order
import com.pasviegas.firecommerce.domain.users.models.Notification
import com.pasviegas.firekka.actors.FirebaseActor
import com.pasviegas.firekka.actors.firebase.FirebaseEvents.{ Added, Removed }
import com.pasviegas.firekka.state.{ DataSnapshotDecoder, WithState, _ }

import scala.util.{ Success, Try }

class OrderManager(sku: ActorRef, firebase: Firebase)
    extends FirebaseActor(firebase) with WithState[Order] {

  implicit val decode = new DataSnapshotDecoder[Order] {
    override def decode(ds: DataSnapshot): Try[Order] =
      Success(Order(ds))
  }

  override def receive: Receive =
    handleFirebaseEvents.orElse(handleOrderEvents)

  def handleFirebaseEvents: Receive = {
    case Added(ds)   => updateState(ds).foreach(makeOrder)
    case Removed(ds) => updateState(ds).foreach(logEvent[Order]("removed"))
  }

  def handleOrderEvents: Receive = {
    case CompleteOrder(order)       => completeOrder(order)
    case CannotCompleteOrder(order) => cannotCompleteOrder(order)
  }

  private[this] def makeOrder(order: Order) = sku ! MakeOrder(order)

  private[this] def cannotCompleteOrder(order: Order) =
    notifyUser(order, "not-completed")

  private[this] def completeOrder(order: Order) =
    notifyUser(order, "completed")

  private[this] def completePartialOrder(order: Order) =
    notifyUser(order, "partial")

  def notifyUser(order: Order, status: String) {
    firebase.getRoot.child(s"users/${order.user}/notifications")
      .pushChild(Notification("order", status))
    firebase.removeValue()
  }

}

object OrderManager {
  def props(sku: ActorRef)(firebase: Firebase): Props =
    Props(new OrderManager(sku, firebase))
}

