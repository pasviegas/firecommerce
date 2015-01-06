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

package com.pasviegas.firecommerce.domain.users

import akka.actor.{ ActorRef, Props }
import com.firebase.client.{ DataSnapshot, Firebase }
import com.pasviegas.firecommerce.domain.users.models.Notification
import com.pasviegas.firekka.actors.FirebaseActor
import com.pasviegas.firekka.actors.firebase.FirebaseEvents.{ Added, Changed, Removed }
import com.pasviegas.firekka.state.{ DataSnapshotDecoder, WithState }

import scala.util.{ Success, Try }

class NotificationManager(user: ActorRef, firebase: Firebase)
    extends FirebaseActor(firebase) with WithState[Notification] {

  implicit val decode = new DataSnapshotDecoder[Notification] {
    override def decode(ds: DataSnapshot): Try[Notification] =
      Success(Notification(ds))
  }

  override def receive: Receive = {
    case Added(ds)   => updateState(ds).foreach(logEvent[Notification]("added"))
    case Changed(ds) => updateState(ds).foreach(logEvent[Notification]("changed"))
    case Removed(ds) => updateState(ds).foreach(logEvent[Notification]("removed"))
    case other       => unhandled(other)
  }

}

object NotificationManager {
  def props(user: ActorRef)(firebase: Firebase): Props =
    Props(new NotificationManager(user, firebase))
}

