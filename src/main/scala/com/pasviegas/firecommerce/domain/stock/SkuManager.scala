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

import akka.actor.Props
import com.firebase.client.{ DataSnapshot, Firebase }
import com.pasviegas.firecommerce.domain.stock.messages.{ CompleteOrder, MakeOrder }
import com.pasviegas.firecommerce.domain.stock.models.{ Order, Sku }
import com.pasviegas.firekka.actors.FirebaseActor
import com.pasviegas.firekka.actors.firebase.FirebaseEvents.{ Added, Changed, Removed }
import com.pasviegas.firekka.actors.support.FirebaseActorCreator
import com.pasviegas.firekka.state.{ DataSnapshotDecoder, WithState, _ }

import scala.util.{ Success, Try }

class SkuManager(firebase: Firebase)
    extends FirebaseActor(firebase) with WithState[Sku] {

  implicit val decode = new DataSnapshotDecoder[Sku] {
    override def decode(ds: DataSnapshot): Try[Sku] =
      Success(Sku(ds))
  }

  override def receive: Receive = {
    case Added(ds) => ds.getKey match {
      case "orders" => attachChildren(ds, FirebaseActorCreator(OrderManager.props(self)))
      case "state"  => updateState(ds).foreach(log[Sku]("added"))
    }
    case Changed(ds)      => updateState(ds).foreach(log[Sku]("changed"))
    case Removed(ds)      => updateState(ds).foreach(log[Sku]("removed"))
    case MakeOrder(order) => makeOrder(order)
    case other            => unhandled(other)
  }

  private[this] def makeOrder(order: Order) =
    state.foreach(sku => {
      firebase.setState(sku.minus(order.quantity))
      sender ! CompleteOrder(order)
    })
}

object SkuManager {
  def props(firebase: Firebase): Props = Props(new SkuManager(firebase))
}
